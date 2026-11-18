package dev.eternia.core.network;

import dev.eternia.core.EterniaMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record QuestAcceptPayload(String questId) implements CustomPacketPayload {
    public static final Type<QuestAcceptPayload> TYPE = new Type<>(EterniaMod.id("quest_accept"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuestAcceptPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, QuestAcceptPayload::questId, QuestAcceptPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
