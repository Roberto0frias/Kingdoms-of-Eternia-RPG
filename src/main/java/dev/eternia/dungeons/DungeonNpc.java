package dev.eternia.dungeons;

import dev.eternia.core.CharacterData;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ServerLevelAccessor;


public final class DungeonNpc {
    public static final String NPC_TAG = "eternia_threshold_cartographer";

    private DungeonNpc() { }

    public static Villager spawn(ServerLevelAccessor accessor, double x, double y, double z, String dungeonId) {
        ServerLevel level = accessor.getLevel();
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
        villager.addTag("eternia_dungeon=" + dungeonId);
        if (accessor instanceof WorldGenRegion region) return region.addFreshEntity(villager) ? villager : null;
        return level.addFreshEntity(villager) ? villager : null;
    }

    public static void registerInteraction() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!entity.entityTags().contains(NPC_TAG)) return InteractionResult.PASS;
            if (player instanceof ServerPlayer serverPlayer) DungeonService.visit(serverPlayer, entity);
            return InteractionResult.SUCCESS;
        });
    }
}
