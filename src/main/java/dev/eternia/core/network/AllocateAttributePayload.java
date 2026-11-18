package dev.eternia.core.network;

import dev.eternia.core.EterniaMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AllocateAttributePayload(String statId) implements CustomPacketPayload {
    public static final Type<AllocateAttributePayload> TYPE = new Type<>(EterniaMod.id("allocate_attribute"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AllocateAttributePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AllocateAttributePayload::statId, AllocateAttributePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
