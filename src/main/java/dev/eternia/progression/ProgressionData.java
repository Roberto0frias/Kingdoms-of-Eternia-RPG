package dev.eternia.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eternia.attributes.AttributeStat;
import dev.eternia.core.EterniaMod;
import dev.eternia.talents.TalentService;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public final class ProgressionData {
    private ProgressionData() { }

    public static final Codec<Progression> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("schema_version", Progression.CURRENT_SCHEMA).forGetter(Progression::schemaVersion),
            Codec.INT.optionalFieldOf("level", 1).forGetter(Progression::level),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(Progression::xp),
            Codec.LONG.optionalFieldOf("total_xp", 0L).forGetter(Progression::totalXp),
            Codec.INT.optionalFieldOf("attribute_points", 0).forGetter(Progression::attributePoints),
            Codec.INT.optionalFieldOf("vitality", 0).forGetter(Progression::vitality),
            Codec.INT.optionalFieldOf("resistance", 0).forGetter(Progression::resistance),
            Codec.INT.optionalFieldOf("strength", 0).forGetter(Progression::strength),
            Codec.INT.optionalFieldOf("agility", 0).forGetter(Progression::agility),
            Codec.INT.optionalFieldOf("intelligence", 0).forGetter(Progression::intelligence),
            Codec.INT.optionalFieldOf("luck", 0).forGetter(Progression::luck)
    ).apply(instance, Progression::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Progression> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeVarInt(value.schemaVersion());
                buf.writeVarInt(value.level());
                buf.writeVarInt(value.xp());
                buf.writeVarLong(value.totalXp());
                buf.writeVarInt(value.attributePoints());
                buf.writeVarInt(value.vitality());
                buf.writeVarInt(value.resistance());
                buf.writeVarInt(value.strength());
                buf.writeVarInt(value.agility());
                buf.writeVarInt(value.intelligence());
                buf.writeVarInt(value.luck());
            },
            buf -> new Progression(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarLong(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt())
    );

    public static final AttachmentType<Progression> ATTACHMENT = AttachmentRegistry.create(
            EterniaMod.id("progression"), builder -> builder
                    .initializer(Progression::initial)
                    .persistent(CODEC)
                    .syncWith(STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
                    .copyOnDeath()
    );

    public static void register() { }

    public static Progression get(net.minecraft.world.entity.Entity entity) {
        return entity.getAttachedOrCreate(ATTACHMENT);
    }

    public static int requiredXp(int level) {
        if (level >= Progression.MAX_LEVEL) return 0;
        return 100 + 40 * level + 5 * level * level;
    }

    public static int awardXp(ServerPlayer player, int amount) {
        if (amount <= 0) return 0;
        Progression before = get(player);
        Progression after = before.award(amount);
        player.setAttached(ATTACHMENT, after);
        TalentService.grantLevelPoints(player, after.level() - before.level());
        if (after.level() != before.level()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "¡Nivel " + after.level() + "! Has obtenido " + (after.level() - before.level()) * 2 + " puntos de atributo."));
        }
        return after.level() - before.level();
    }

    public static boolean allocate(ServerPlayer player, AttributeStat stat) {
        Progression current = get(player);
        if (current.attributePoints() <= 0 || current.value(stat) >= Progression.MAX_STAT) return false;
        player.setAttached(ATTACHMENT, current.allocate(stat));
        return true;
    }

    public record Progression(int schemaVersion, int level, int xp, long totalXp, int attributePoints,
                              int vitality, int resistance, int strength, int agility, int intelligence, int luck) {
        public static final int CURRENT_SCHEMA = 1;
        public static final int MAX_LEVEL = 200;
        public static final int MAX_STAT = 100;

        public static Progression initial() {
            return new Progression(CURRENT_SCHEMA, 1, 0, 0L, 0, 0, 0, 0, 0, 0, 0);
        }

        public Progression award(int amount) {
            int newLevel = Math.max(1, Math.min(MAX_LEVEL, level));
            int newXp = Math.max(0, xp) + amount;
            int newPoints = Math.max(0, attributePoints);
            while (newLevel < MAX_LEVEL && newXp >= requiredXp(newLevel)) {
                newXp -= requiredXp(newLevel);
                newLevel++;
                newPoints += 2;
            }
            if (newLevel == MAX_LEVEL) newXp = 0;
            return new Progression(CURRENT_SCHEMA, newLevel, newXp, totalXp + amount, newPoints,
                    vitality, resistance, strength, agility, intelligence, luck);
        }

        public int value(AttributeStat stat) {
            return switch (stat) {
                case VITALITY -> vitality;
                case RESISTANCE -> resistance;
                case STRENGTH -> strength;
                case AGILITY -> agility;
                case INTELLIGENCE -> intelligence;
                case LUCK -> luck;
            };
        }

        public Progression allocate(AttributeStat stat) {
            if (attributePoints <= 0 || value(stat) >= MAX_STAT) return this;
            return switch (stat) {
                case VITALITY -> new Progression(CURRENT_SCHEMA, level, xp, totalXp, attributePoints - 1, vitality + 1, resistance, strength, agility, intelligence, luck);
                case RESISTANCE -> new Progression(CURRENT_SCHEMA, level, xp, totalXp, attributePoints - 1, vitality, resistance + 1, strength, agility, intelligence, luck);
                case STRENGTH -> new Progression(CURRENT_SCHEMA, level, xp, totalXp, attributePoints - 1, vitality, resistance, strength + 1, agility, intelligence, luck);
                case AGILITY -> new Progression(CURRENT_SCHEMA, level, xp, totalXp, attributePoints - 1, vitality, resistance, strength, agility + 1, intelligence, luck);
                case INTELLIGENCE -> new Progression(CURRENT_SCHEMA, level, xp, totalXp, attributePoints - 1, vitality, resistance, strength, agility, intelligence + 1, luck);
                case LUCK -> new Progression(CURRENT_SCHEMA, level, xp, totalXp, attributePoints - 1, vitality, resistance, strength, agility, intelligence, luck + 1);
            };
        }
    }
}
