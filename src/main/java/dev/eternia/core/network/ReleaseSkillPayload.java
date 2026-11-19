package dev.eternia.core.network;

import dev.eternia.core.EterniaMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ReleaseSkillPayload() implements CustomPacketPayload {
    public static final Type<ReleaseSkillPayload> TYPE = new Type<>(EterniaMod.id("release_skill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReleaseSkillPayload> CODEC = StreamCodec.unit(new ReleaseSkillPayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
