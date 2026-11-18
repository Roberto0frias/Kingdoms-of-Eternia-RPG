package dev.eternia.core.network;

import dev.eternia.core.EterniaMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SpecializationChoicePayload(String specializationId) implements CustomPacketPayload {
    public static final Type<SpecializationChoicePayload> TYPE = new Type<>(EterniaMod.id("specialization_choice"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpecializationChoicePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SpecializationChoicePayload::specializationId, SpecializationChoicePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
