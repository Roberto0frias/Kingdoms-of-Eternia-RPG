package dev.eternia.core.network;

import dev.eternia.core.EterniaMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TalentUpgradePayload(String nodeId) implements CustomPacketPayload {
    public static final Type<TalentUpgradePayload> TYPE = new Type<>(EterniaMod.id("talent_upgrade"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TalentUpgradePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TalentUpgradePayload::nodeId, TalentUpgradePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
