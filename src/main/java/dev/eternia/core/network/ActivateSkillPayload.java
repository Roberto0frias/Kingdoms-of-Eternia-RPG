package dev.eternia.core.network;

import dev.eternia.core.EterniaMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ActivateSkillPayload(int ignoredSequence) implements CustomPacketPayload {
    public static final Type<ActivateSkillPayload> TYPE = new Type<>(EterniaMod.id("activate_skill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActivateSkillPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ActivateSkillPayload::ignoredSequence, ActivateSkillPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
