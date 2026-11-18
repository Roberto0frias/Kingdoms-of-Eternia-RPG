package dev.eternia.core.network;

import dev.eternia.core.EterniaMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RelicFormPayload(String formId, boolean fuse) implements CustomPacketPayload {
    public static final Type<RelicFormPayload> TYPE = new Type<>(EterniaMod.id("relic_form"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RelicFormPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RelicFormPayload::formId,
            ByteBufCodecs.BOOL, RelicFormPayload::fuse,
            RelicFormPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
