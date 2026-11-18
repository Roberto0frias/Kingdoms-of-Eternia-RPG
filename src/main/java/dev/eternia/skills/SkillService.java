package dev.eternia.skills;

import dev.eternia.attributes.AttributeStat;
import dev.eternia.classes.PlayerClass;
import dev.eternia.core.CharacterData;
import dev.eternia.progression.PlayerAttributeService;
import dev.eternia.talents.TalentEffect;
import dev.eternia.talents.TalentService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import dev.eternia.relics.RelicForm;
import dev.eternia.relics.RelicService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SkillService {
    private static final Map<UUID, Long> READY_AT = new ConcurrentHashMap<>();

    private SkillService() { }

    public static void activate(ServerPlayer player) {
        CharacterData.Profile profile = CharacterData.get(player);
        if (!profile.isCreated()) return;
        if (!RelicService.isEquipped(player)) {
            player.sendSystemMessage(Component.literal("Empuña tu Reliquia de clase para canalizar habilidades."));
            return;
        }
        RelicForm form = RelicService.activeForm(player);
        if (form == null) return;
        PlayerClass playerClass = PlayerClass.fromId(profile.classId());
        ActiveSkill skill = ActiveSkill.forClass(playerClass);
        if (skill == null) return;

        long now = player.level().getGameTime();
        long readyAt = READY_AT.getOrDefault(player.getUUID(), 0L);
        if (now < readyAt) return;

        boolean used = switch (skill) {
            case WARRIOR -> hitTarget(player, skill.range(), 1.65, 2.0, form);
            case MAGE -> hitTarget(player, skill.range(),
                    0.0, 4.0 + PlayerAttributeService.effectiveValue(player, AttributeStat.INTELLIGENCE) * 0.45, form);
            case ARCHER -> hitTarget(player, skill.range(),
                    0.0, 4.0 + PlayerAttributeService.effectiveValue(player, AttributeStat.AGILITY) * 0.35, form);
            case ASSASSIN -> hitTarget(player, skill.range(), 2.0,
                    PlayerAttributeService.effectiveValue(player, AttributeStat.AGILITY) * 0.4, form);
            case PRIEST -> form.skillStyle() == RelicForm.SkillStyle.SOLAR_JUDGMENT
                    ? hitTarget(player, 6.0, 1.25, 5.0, form) : healSelf(player, form);
        };

        if (used) {
            double cooldownMultiplier = Math.max(0.5, 1.0 - TalentService.totalEffect(player, TalentEffect.SKILL_COOLDOWN));
            READY_AT.put(player.getUUID(), now + Math.max(1, (long) (skill.cooldownTicks() * cooldownMultiplier)));
            player.sendSystemMessage(Component.literal(form.skillName() + " activada."));
        }
    }

    private static boolean hitTarget(ServerPlayer player, double range, double attackMultiplier,
                                     double bonusDamage, RelicForm form) {
        LivingEntity target = findTarget(player, range);
        if (target == null) {
            player.sendSystemMessage(Component.literal("No hay un enemigo a la vista para " + form.skillName() + "."));
            return false;
        }
        double baseAttack = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double damage = (baseAttack * attackMultiplier + bonusDamage)
                * form.skillPower() * (1.0 + TalentService.totalEffect(player, TalentEffect.SKILL_DAMAGE));
        if (target.hurtServer(player.level(), player.damageSources().playerAttack(player), (float) damage)) {
            applyFormEffect(player, target, form, damage);
            return true;
        }
        player.sendSystemMessage(Component.literal("El objetivo no recibió daño."));
        return false;
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

    private static boolean healSelf(ServerPlayer player, RelicForm form) {
        double missingHealth = player.getMaxHealth() - player.getHealth();
        if (missingHealth <= 0.0) {
            player.sendSystemMessage(Component.literal("No necesitas sanación ahora."));
            return false;
        }
        double healing = (6.0 + PlayerAttributeService.effectiveValue(player, AttributeStat.INTELLIGENCE) * 0.35) * form.skillPower();
        healing *= 1.0 + TalentService.totalEffect(player, TalentEffect.SKILL_HEALING);
        double amount = Math.min(missingHealth, healing);
        player.heal((float) amount);
        if (form.skillStyle() == RelicForm.SkillStyle.AEGIS_HEAL) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 80, 0), player);
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
}
