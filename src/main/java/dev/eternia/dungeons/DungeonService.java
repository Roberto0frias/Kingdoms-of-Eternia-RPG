package dev.eternia.dungeons;

import dev.eternia.core.CharacterData;
import dev.eternia.economy.EconomyData;
import dev.eternia.progression.ProgressionData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.UUID;

/** Per-player reward claims for a shared, naturally generated world ruin. */
public final class DungeonService {
    public static final long COOLDOWN_TICKS = 24_000L;
    private static final String DUNGEON_TAG = "eternia_dungeon=";
    private DungeonService() { }

    public static DungeonData get(ServerPlayer player) {
        return player.getAttachedOrCreate(DungeonData.ATTACHMENT);
    }

    public static void visit(ServerPlayer player, Entity npc) {
        if (!CharacterData.get(player).isCreated()) {
            player.sendSystemMessage(Component.literal("Iriel solo orienta a aventureros con un personaje creado."));
            return;
        }
        String dungeonTag = npc.entityTags().stream().filter(tag -> tag.startsWith(DUNGEON_TAG)).findFirst().orElse("");
        if (dungeonTag.isEmpty()) return;
        String dungeonId = dungeonTag.substring(DUNGEON_TAG.length());
        DungeonData data = get(player);
        long now = player.level().getGameTime();
        if (now < data.availableAt()) {
            long seconds = (data.availableAt() - now + 19) / 20;
            player.sendSystemMessage(Component.literal("Iriel: Tu descanso termina en " + seconds + " segundos."));
            return;
        }
        if (!dungeonId.equals(data.dungeonId())) {
            if (data.active()) {
                player.sendSystemMessage(Component.literal("Iriel: Primero reclama la recompensa de la Ruina del Umbral que ya exploraste."));
                return;
            }
            data = data.begin(dungeonId, "world", npc.blockPosition().getX(), npc.blockPosition().getY(), npc.blockPosition().getZ());
        }

        var nearbyBosses = npc.level().getEntities(npc, npc.getBoundingBox().inflate(64.0),
                entity -> entity.entityTags().contains(DungeonBosses.BOSS_TAG)
                        && entity.entityTags().contains(DUNGEON_TAG + dungeonId) && entity.isAlive());
        if (!nearbyBosses.isEmpty()) {
            player.setAttached(DungeonData.ATTACHMENT, data);
            player.sendSystemMessage(Component.literal("Iriel: Varkh aún protege la cámara final. Recorre el laberinto de 41 por 41 y prepara una carga para romper su guardia."));
            return;
        }

        if (!data.bossDefeated()) {
            data = data.completeBoss();
            player.setAttached(DungeonData.ATTACHMENT, data);
        }
        if (data.rewardClaimed()) {
            player.sendSystemMessage(Component.literal("Iriel: Ya recibiste el botín de esta ruina. Descansa antes de reclamar otra."));
            return;
        }
        player.setAttached(DungeonData.ATTACHMENT, data.claim(now + COOLDOWN_TICKS));
        ProgressionData.awardXp(player, 1_200);
        EconomyData.award(player, 3_000);
        ItemStack trophy = new ItemStack(Items.NETHER_STAR);
        if (!player.getInventory().add(trophy)) player.drop(trophy, false);
        player.sendSystemMessage(Component.literal("Iriel te entrega el Sello del Guardián: +1200 XP, +3000 Cobre y una Estrella del Nether."));
    }

    public static void onBossDeath(Entity entity, DamageSource source, MinecraftServer server) {
        if (!entity.entityTags().contains(DungeonBosses.BOSS_TAG)) return;
        DungeonBosses.remove(entity);
        if (!(source.getEntity() instanceof ServerPlayer killer) || !CharacterData.get(killer).isCreated()) return;
        String dungeonTag = entity.entityTags().stream().filter(tag -> tag.startsWith(DUNGEON_TAG)).findFirst().orElse("");
        if (dungeonTag.isEmpty()) return;
        String dungeonId = dungeonTag.substring(DUNGEON_TAG.length());
        DungeonData data = get(killer);
        if (data.active() && !dungeonId.equals(data.dungeonId())) return;
        if (!dungeonId.equals(data.dungeonId())) {
            data = data.begin(dungeonId, "world", entity.blockPosition().getX(), entity.blockPosition().getY(), entity.blockPosition().getZ());
        }
        killer.setAttached(DungeonData.ATTACHMENT, data.completeBoss());
        killer.sendSystemMessage(Component.literal("Varkh ha caído. Cualquier aventurero puede reclamar su parte hablando con Iriel."));
    }

    public static void register() { }
}
