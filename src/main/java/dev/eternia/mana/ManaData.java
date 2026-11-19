package dev.eternia.mana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eternia.core.EterniaMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Persisted mana pool; the maximum is derived from the character's intelligence. */
public record ManaData(int current) {
    public static final Codec<ManaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("current", 50).forGetter(ManaData::current)
    ).apply(instance, ManaData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ManaData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ManaData::current, ManaData::new);
    public static final AttachmentType<ManaData> ATTACHMENT = AttachmentRegistry.create(
            EterniaMod.id("mana"), builder -> builder.initializer(() -> new ManaData(50))
                    .persistent(CODEC).syncWith(STREAM_CODEC, AttachmentSyncPredicate.targetOnly()).copyOnDeath());

    public ManaData { current = Math.max(0, Math.min(1000, current)); }
    public ManaData withCurrent(int value) { return new ManaData(value); }
    public static void register() { }
}
