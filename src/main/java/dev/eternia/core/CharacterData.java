package dev.eternia.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class CharacterData {
    private CharacterData() { }

    public static final Codec<Profile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("schema_version", Profile.CURRENT_SCHEMA).forGetter(Profile::schemaVersion),
            Codec.STRING.optionalFieldOf("race", "").forGetter(Profile::raceId),
            Codec.STRING.optionalFieldOf("class", "").forGetter(Profile::classId),
            Codec.STRING.optionalFieldOf("specialization", "").forGetter(Profile::specializationId)
    ).apply(instance, Profile::new));

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Profile> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, Profile::schemaVersion,
                    ByteBufCodecs.STRING_UTF8, Profile::raceId,
                    ByteBufCodecs.STRING_UTF8, Profile::classId,
                    ByteBufCodecs.STRING_UTF8, Profile::specializationId,
                    Profile::new
            );

    public static final AttachmentType<Profile> PROFILE = AttachmentRegistry.create(
            EterniaMod.id("character_profile"), builder -> builder
                    .initializer(Profile::empty)
                    .persistent(CODEC)
                    .syncWith(STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
                    .copyOnDeath()
    );

    public static void register() {
        // Referencing this class from the mod initializer triggers attachment registration.
    }

    public static Profile get(net.minecraft.world.entity.Entity entity) {
        return entity.getAttachedOrCreate(PROFILE);
    }

    public record Profile(int schemaVersion, String raceId, String classId, String specializationId) {
        public static final int CURRENT_SCHEMA = 2;

        public static Profile empty() {
            return new Profile(CURRENT_SCHEMA, "", "", "");
        }

        public boolean isCreated() {
            return !raceId.isBlank() && !classId.isBlank();
        }

        public Profile withChoices(String race, String playerClass) {
            return new Profile(CURRENT_SCHEMA, race, playerClass, "");
        }

        public Profile withSpecialization(String specialization) {
            return new Profile(CURRENT_SCHEMA, raceId, classId, specialization);
        }
    }
}
