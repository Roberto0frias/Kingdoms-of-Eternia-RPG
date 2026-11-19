package dev.eternia.dungeons;

import dev.eternia.core.EterniaMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DungeonBosses {
    public static final String BOSS_TAG = "eternia_threshold_king";
    private static final String OWNER_TAG = "eternia_owner=";
    private static final Map<UUID, ServerBossEvent> BARS = new HashMap<>();
    private static final Map<UUID, Long> NEXT_ATTACK = new HashMap<>();
    private static final Map<UUID, Long> STRIKE_AT = new HashMap<>();

    private DungeonBosses() { }

    public static Entity spawn(ServerLevelAccessor accessor, double x, double y, double z, String dungeonId, UUID owner) {
        ServerLevel level = accessor.getLevel();
        var type = BuiltInRegistries.ENTITY_TYPE.get(Identifier.withDefaultNamespace("wither_skeleton"))
                .map(reference -> reference.value()).orElse(null);
        if (type == null) return null;
        Entity entity = type.create(level, EntitySpawnReason.STRUCTURE);
        if (!(entity instanceof WitherSkeleton boss)) return null;
        boss.setPos(x, y, z);
        boss.setCustomName(Component.literal("Varkh, Rey del Laberinto"));
        boss.setCustomNameVisible(true);
        boss.setPersistenceRequired();
        boss.setGlowingTag(true);
        boss.addTag(BOSS_TAG);
        if (owner != null) boss.addTag(OWNER_TAG + owner);
        else boss.addTag("eternia_world_dungeon");
        boss.addTag("eternia_dungeon=" + dungeonId);
        var health = boss.getAttribute(Attributes.MAX_HEALTH);
        var attack = boss.getAttribute(Attributes.ATTACK_DAMAGE);
        var speed = boss.getAttribute(Attributes.MOVEMENT_SPEED);
        var armor = boss.getAttribute(Attributes.ARMOR);
        if (health != null) health.setBaseValue(240.0);
        if (attack != null) attack.setBaseValue(16.0);
        if (speed != null) speed.setBaseValue(0.31);
        if (armor != null) armor.setBaseValue(10.0);
        boss.setHealth(240.0F);
        boss.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        boss.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
        if (accessor instanceof WorldGenRegion region) {
            if (!region.addFreshEntity(boss)) return null;
        } else if (!level.addFreshEntity(boss)) return null;
        if (!(accessor instanceof WorldGenRegion)) ensureBar(boss);
        return boss;
    }

    public static void tick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof WitherSkeleton boss) || !boss.entityTags().contains(BOSS_TAG) || !boss.isAlive()) continue;
                ServerBossEvent bar = ensureBar(boss);
                bar.setProgress(Math.max(0.0F, boss.getHealth() / boss.getMaxHealth()));
                updateViewers(server, boss, bar);
                performSpecialAttack(boss);
            }
        }
    }

    private static ServerBossEvent ensureBar(WitherSkeleton boss) {
        return BARS.computeIfAbsent(boss.getUUID(), ignored -> new ServerBossEvent(boss.getUUID(),
                Component.literal("Varkh, Rey del Laberinto"), BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarOverlay.PROGRESS));
    }

    private static void updateViewers(MinecraftServer server, WitherSkeleton boss, ServerBossEvent bar) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean nearby = player.level() == boss.level() && player.distanceToSqr(boss) <= 48.0 * 48.0;
            if (nearby && !bar.getPlayers().contains(player)) bar.addPlayer(player);
            else if (!nearby && bar.getPlayers().contains(player)) bar.removePlayer(player);
        }
    }

    private static void performSpecialAttack(WitherSkeleton boss) {
        long now = boss.level().getGameTime();
        ServerLevel level = (ServerLevel) boss.level();
        var nearby = level.getEntities(boss, boss.getBoundingBox().inflate(7.0), entity -> entity instanceof ServerPlayer);
        if (nearby.isEmpty()) return;
        Long strikeAt = STRIKE_AT.get(boss.getUUID());
        if (strikeAt != null && now >= strikeAt) {
            STRIKE_AT.remove(boss.getUUID());
            NEXT_ATTACK.put(boss.getUUID(), now + 180L);
            for (Entity entity : nearby) {
                if (!(entity instanceof ServerPlayer player)) continue;
                player.hurtServer(level, level.damageSources().mobAttack(boss), 8.0F);
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1), boss);
            }
            return;
        }
        if (strikeAt != null || now < NEXT_ATTACK.getOrDefault(boss.getUUID(), 0L)) return;
        STRIKE_AT.put(boss.getUUID(), now + 30L);
        NEXT_ATTACK.put(boss.getUUID(), now + 30L);
        Component telegraph = Component.literal("Varkh prepara un golpe de área: ¡apártate!");
        for (Entity entity : nearby) {
            if (!(entity instanceof ServerPlayer player)) continue;
            player.sendSystemMessage(telegraph);
        }
    }

    public static String ownerTagPrefix() { return OWNER_TAG; }

    public static void remove(Entity entity) {
        ServerBossEvent bar = BARS.remove(entity.getUUID());
        NEXT_ATTACK.remove(entity.getUUID());
        STRIKE_AT.remove(entity.getUUID());
        if (bar != null) bar.removeAllPlayers();
    }
}
