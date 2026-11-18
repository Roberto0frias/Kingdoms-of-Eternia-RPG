package dev.eternia.dungeons;

import dev.eternia.core.CharacterData;
import dev.eternia.economy.EconomyData;
import dev.eternia.progression.ProgressionData;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public final class DungeonNpc {
    public static final String NPC_TAG = "eternia_threshold_cartographer";

    private DungeonNpc() { }

    public static Villager spawn(ServerLevel level, double x, double y, double z, String dungeonId, UUID owner) {
        var type = BuiltInRegistries.ENTITY_TYPE.get(Identifier.withDefaultNamespace("villager"))
                .map(reference -> reference.value()).orElse(null);
        if (type == null) return null;
        Entity entity = type.create(level, EntitySpawnReason.STRUCTURE);
        if (!(entity instanceof Villager villager)) return null;
        villager.setPos(x, y, z);
        villager.setCustomName(Component.literal("Iriel, Cartógrafo del Umbral"));
        villager.setCustomNameVisible(true);
        villager.setInvulnerable(true);
        villager.setNoAi(true);
        villager.addTag(NPC_TAG);
        villager.addTag(DungeonBosses.ownerTagPrefix() + owner);
        villager.addTag("eternia_dungeon=" + dungeonId);
        return level.addFreshEntity(villager) ? villager : null;
    }

    public static void registerInteraction() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!entity.entityTags().contains(NPC_TAG)) return InteractionResult.PASS;
            if (player instanceof ServerPlayer serverPlayer) interact(serverPlayer, entity);
            return InteractionResult.SUCCESS;
        });
    }

    private static void interact(ServerPlayer player, Entity npc) {
        if (!CharacterData.get(player).isCreated()) return;
        DungeonData data = player.getAttachedOrCreate(DungeonData.ATTACHMENT);
        String ownerTag = DungeonBosses.ownerTagPrefix() + player.getUUID();
        String idTag = "eternia_dungeon=" + data.dungeonId();
        if (!npc.entityTags().contains(ownerTag) || data.dungeonId().isEmpty() || !npc.entityTags().contains(idTag)) {
            player.sendSystemMessage(Component.literal("Iriel: Este mapa pertenece a otra expedición."));
            return;
        }
        if (!data.bossDefeated()) {
            player.sendSystemMessage(Component.literal("Iriel: Varkh custodia la cámara oriental. Escucha el golpe y abandona su alcance."));
            player.sendSystemMessage(Component.literal("La ruta cambia en cada expedición; observa los muros y las antorchas."));
            return;
        }
        if (data.rewardClaimed()) {
            player.sendSystemMessage(Component.literal("Iriel: La ruina ya te entregó su secreto. Volverá a abrirse tras un día de descanso."));
            return;
        }
        player.setAttached(DungeonData.ATTACHMENT, data.claim(player.level().getGameTime() + DungeonService.COOLDOWN_TICKS));
        ProgressionData.awardXp(player, 1_200);
        EconomyData.award(player, 3_000);
        ItemStack trophy = new ItemStack(Items.NETHER_STAR);
        if (!player.getInventory().add(trophy)) player.drop(trophy, false);
        player.sendSystemMessage(Component.literal("Iriel te entrega el Sello del Guardián: +1200 XP, +3000 Cobre y una Estrella del Nether."));
    }
}
