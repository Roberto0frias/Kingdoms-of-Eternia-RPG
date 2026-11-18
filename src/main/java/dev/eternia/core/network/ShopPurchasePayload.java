package dev.eternia.core.network;

import dev.eternia.core.EterniaMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ShopPurchasePayload(String offerId) implements CustomPacketPayload {
    public static final Type<ShopPurchasePayload> TYPE = new Type<>(EterniaMod.id("shop_purchase"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShopPurchasePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ShopPurchasePayload::offerId, ShopPurchasePayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
