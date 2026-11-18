package dev.eternia.progression;

import dev.eternia.attributes.AttributeStat;
import dev.eternia.classes.PlayerClass;
import dev.eternia.core.CharacterData;
import dev.eternia.core.EterniaMod;
import dev.eternia.races.Race;
import dev.eternia.talents.TalentEffect;
import dev.eternia.talents.TalentService;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.Holder;

public final class PlayerAttributeService {
    private PlayerAttributeService() { }

    public static int effectiveValue(net.minecraft.world.entity.Entity player, AttributeStat stat) {
        ProgressionData.Progression progression = ProgressionData.get(player);
        CharacterData.Profile profile = CharacterData.get(player);
        Race race = Race.fromId(profile.raceId());
        int racialBonus = race == null ? 0 : race.attributeBonus(stat);
        return progression.value(stat) + racialBonus;
    }

    public static void refresh(ServerPlayer player) {
        CharacterData.Profile profile = CharacterData.get(player);
        ProgressionData.Progression progression = ProgressionData.get(player);
        if (!profile.isCreated()) return;

        apply(player, Attributes.MAX_HEALTH, "vitality", effectiveValue(player, AttributeStat.VITALITY) * 0.5
                + TalentService.totalEffect(player, TalentEffect.HEALTH));
        apply(player, Attributes.ARMOR, "resistance", effectiveValue(player, AttributeStat.RESISTANCE) * 0.2
                + TalentService.totalEffect(player, TalentEffect.ARMOR));
        apply(player, Attributes.ATTACK_DAMAGE, "strength", effectiveValue(player, AttributeStat.STRENGTH) * 0.05
                + TalentService.totalEffect(player, TalentEffect.ATTACK_DAMAGE));
        apply(player, Attributes.MOVEMENT_SPEED, "agility", effectiveValue(player, AttributeStat.AGILITY) * 0.0005);
        apply(player, Attributes.LUCK, "luck", effectiveValue(player, AttributeStat.LUCK) * 0.1);
        // Intelligence is persisted and visible now; it becomes the mana/magic scaling input in the magic milestone.
    }

    private static void apply(ServerPlayer player, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                              String key, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        Identifier id = EterniaMod.id("rpg_" + key);
        instance.removeModifier(id);
        if (amount > 0) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static int xpMultiplierPercent(ServerPlayer player) {
        CharacterData.Profile profile = CharacterData.get(player);
        Race race = Race.fromId(profile.raceId());
        return race == null ? 100 : race.experiencePercent();
    }
}
