package dev.eternia.skills;

import dev.eternia.attributes.AttributeStat;
import dev.eternia.classes.PlayerClass;
import dev.eternia.core.CharacterData;
import dev.eternia.mana.ManaService;
import dev.eternia.progression.PlayerAttributeService;
import dev.eternia.relics.RelicForm;
import dev.eternia.relics.RelicService;
import dev.eternia.talents.TalentEffect;
import dev.eternia.talents.TalentService;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SkillService {
    private static final Map<UUID, Long> READY_AT = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> CHARGE_STARTED_AT = new ConcurrentHashMap<>();
    private static final int MIN_CHARGE_TICKS = 4;
    private static final int MAX_CHARGE_TICKS = 40;

    private SkillService() { }

    public static void beginCharge(ServerPlayer player) {
        if (!CharacterData.get(player).isCreated()) return;
        if (!RelicService.isEquipped(player)) {
            player.sendSystemMessage(Component.literal("Empuña tu Reliquia de clase para canalizar habilidades."));
            return;
        }
        ActiveSkill skill = skillFor(player);
        if (skill == null) return;
        long now = player.level().getGameTime();
        if (now < READY_AT.getOrDefault(player.getUUID(), 0L)) return;
        if (ManaService.current(player) < skill.manaCost()) {
            player.sendSystemMessage(Component.literal("Maná insuficiente para " + skill.displayName() + "."));
            return;
        }
        CHARGE_STARTED_AT.putIfAbsent(player.getUUID(), now);
        player.sendSystemMessage(Component.literal("Canalizando " + skill.displayName() + " · mantén R para cargar."));
    }

    public static void tickCharge(ServerPlayer player) {
        Long started = CHARGE_STARTED_AT.get(player.getUUID());
        if (started == null) return;
        long elapsed = Math.min(MAX_CHARGE_TICKS, Math.max(0L, player.level().getGameTime() - started));
        if (elapsed == 0L || elapsed % 4L != 0L || !(player.level() instanceof ServerLevel level)) return;
        Vec3 hand = player.getEyePosition().add(player.getLookAngle().scale(0.65));
        level.sendParticles(ParticleTypes.ENCHANT, hand.x, hand.y, hand.z, 7, 0.12, 0.12, 0.12, 0.02);
        level.sendParticles(ParticleTypes.END_ROD, hand.x, hand.y, hand.z, 2, 0.08, 0.08, 0.08, 0.01);
    }

    public static void releaseCharge(ServerPlayer player) {
        Long started = CHARGE_STARTED_AT.remove(player.getUUID());
        if (started == null || !CharacterData.get(player).isCreated() || !RelicService.isEquipped(player)) return;
        ActiveSkill skill = skillFor(player);
        RelicForm form = RelicService.activeForm(player);
        if (skill == null || form == null) return;
        long now = player.level().getGameTime();
        if (now < READY_AT.getOrDefault(player.getUUID(), 0L)) return;
        int chargeTicks = (int) Math.max(MIN_CHARGE_TICKS,
                Math.min(MAX_CHARGE_TICKS, now - started));
        double charge = chargeTicks / (double) MAX_CHARGE_TICKS;
        int manaCost = (int) Math.ceil(skill.manaCost() * (1.0 + charge * 0.5));
        if (!ManaService.spend(player, manaCost)) return;

        double chargedPower = 1.0 + charge * 1.5;
        boolean used = switch (skill) {
            case WARRIOR -> hitTarget(player, skill.range(), 1.65, 2.0, form, chargedPower);
            case MAGE -> hitTarget(player, skill.range(), 0.0,
                    4.0 + PlayerAttributeService.effectiveValue(player, AttributeStat.INTELLIGENCE) * 0.45,
                    form, chargedPower);
            case ARCHER -> hitTarget(player, skill.range(), 0.0,
                    4.0 + PlayerAttributeService.effectiveValue(player, AttributeStat.AGILITY) * 0.35,
                    form, chargedPower);
            case ASSASSIN -> hitTarget(player, skill.range(), 2.0,
                    PlayerAttributeService.effectiveValue(player, AttributeStat.AGILITY) * 0.4,
                    form, chargedPower);
            case PRIEST -> form.skillStyle() == RelicForm.SkillStyle.SOLAR_JUDGMENT
                    ? hitTarget(player, 6.0, 1.25, 5.0, form, chargedPower)
                    : healSelf(player, form, chargedPower);
        };

        if (!used) {
            ManaService.restore(player, manaCost);
            return;
        }
        double cooldownMultiplier = Math.max(0.5,
                1.0 - TalentService.totalEffect(player, TalentEffect.SKILL_COOLDOWN));
        READY_AT.put(player.getUUID(), now + Math.max(1,
                (long) (skill.cooldownTicks() * cooldownMultiplier)));
        player.swing(InteractionHand.MAIN_HAND, true);
        player.sendSystemMessage(Component.literal(form.skillName() + " · carga "
                + Math.round(charge * 100) + "% · " + ManaService.current(player) + "/"
                + ManaService.maximum(player) + " maná."));
    }

    private static ActiveSkill skillFor(ServerPlayer player) {
        return ActiveSkill.forClass(PlayerClass.fromId(CharacterData.get(player).classId()));
    }

    private static boolean hitTarget(ServerPlayer player, double range, double attackMultiplier,
                                     double bonusDamage, RelicForm form, double chargedPower) {
        LivingEntity target = findTarget(player, range);
        if (target == null) {
            player.sendSystemMessage(Component.literal("No hay un enemigo a la vista para " + form.skillName() + "."));
            return false;
        }
        double baseAttack = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double damage = (baseAttack * attackMultiplier + bonusDamage) * form.skillPower() * chargedPower
                * (1.0 + TalentService.totalEffect(player, TalentEffect.SKILL_DAMAGE));
        if (!target.hurtServer(player.level(), player.damageSources().playerAttack(player), (float) damage)) {
            player.sendSystemMessage(Component.literal("El objetivo no recibió daño."));
            return false;
        }
        applyFormEffect(player, target, form, damage);
        emitImpact(player, target, form);
        return true;
    }

    private static LivingEntity findTarget(ServerPlayer player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(range));
        BlockHitResult blockHit = player.level().clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double maxDistanceSq = range * range;
        if (blockHit.getType() != HitResult.Type.MISS) {
            maxDistanceSq = Math.min(maxDistanceSq, start.distanceToSqr(blockHit.getLocation()));
        }
        AABB searchArea = player.getBoundingBox().inflate(range);
        LivingEntity nearest = null;
        double nearestDistanceSq = maxDistanceSq;
        for (var entity : player.level().getEntities(player, searchArea,
                candidate -> candidate instanceof Monster monster && monster.isAlive())) {
            if (!(entity instanceof Monster monster)) continue;
            var intersection = monster.getBoundingBox().inflate(0.35).clip(start, end);
            if (intersection.isEmpty()) continue;
            double distanceSq = start.distanceToSqr(intersection.get());
            if (distanceSq <= nearestDistanceSq) {
                nearest = monster;
                nearestDistanceSq = distanceSq;
            }
        }
        return nearest;
    }

    private static boolean healSelf(ServerPlayer player, RelicForm form, double chargedPower) {
        double missingHealth = player.getMaxHealth() - player.getHealth();
        if (missingHealth <= 0.0) {
            player.sendSystemMessage(Component.literal("No necesitas sanación ahora."));
            return false;
        }
        double healing = (6.0 + PlayerAttributeService.effectiveValue(player, AttributeStat.INTELLIGENCE) * 0.35)
                * form.skillPower() * chargedPower;
        healing *= 1.0 + TalentService.totalEffect(player, TalentEffect.SKILL_HEALING);
        player.heal((float) Math.min(missingHealth, healing));
        if (form.skillStyle() == RelicForm.SkillStyle.AEGIS_HEAL) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 80, 0), player);
        }
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0,
                    player.getZ(), 16, 0.5, 0.65, 0.5, 0.02);
            level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0,
                    player.getZ(), 8, 0.35, 0.5, 0.35, 0.015);
        }
        return true;
    }

    private static void applyFormEffect(ServerPlayer player, LivingEntity target, RelicForm form, double damage) {
        switch (form.skillStyle()) {
            case CINDER_WAVE, PRISM_BURST, TEMPEST_FAN -> {
                for (var entity : player.level().getEntities(player, target.getBoundingBox().inflate(2.5),
                        candidate -> candidate instanceof Monster monster && monster.isAlive() && candidate != target)) {
                    entity.hurtServer(player.level(), player.damageSources().playerAttack(player), (float) (damage * 0.3));
                }
                if (form.skillStyle() == RelicForm.SkillStyle.CINDER_WAVE) {
                    target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 80));
                }
            }
            case EMBER_BOLT -> target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 80));
            case BASTION_BASH -> player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 80, 0), player);
            case SOUL_DRAIN -> player.heal((float) Math.min(player.getMaxHealth() - player.getHealth(), damage * 0.3));
            case STARFALL_MARK -> target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0), player);
            case VENOM_CUT -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), player);
            case MOON_ECLIPSE -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0), player);
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 0), player);
            }
            default -> { }
        }
    }

    private static void emitImpact(ServerPlayer player, LivingEntity target, RelicForm form) {
        if (!(player.level() instanceof ServerLevel level)) return;
        var particle = switch (form.playerClass()) {
            case WARRIOR -> ParticleTypes.CRIT;
            case MAGE -> ParticleTypes.FLAME;
            case ARCHER -> ParticleTypes.ELECTRIC_SPARK;
            case ASSASSIN -> ParticleTypes.SMOKE;
            case PRIEST -> ParticleTypes.END_ROD;
        };
        level.sendParticles(particle, target.getX(), target.getY() + target.getBbHeight() * 0.6,
                target.getZ(), 18, 0.28, 0.4, 0.28, 0.08);
        level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                SoundSource.PLAYERS, 0.65F, 0.85F + level.getRandom().nextFloat() * 0.3F);
    }

    public static void clearCharge(UUID id) { CHARGE_STARTED_AT.remove(id); }
}
