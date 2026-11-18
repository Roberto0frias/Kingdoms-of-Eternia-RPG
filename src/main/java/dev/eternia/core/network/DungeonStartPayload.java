package dev.eternia.core.network;

import dev.eternia.core.EterniaMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DungeonStartPayload() implements CustomPacketPayload {
    public static final Type<DungeonStartPayload> TYPE = new Type<>(EterniaMod.id("dungeon_start"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonStartPayload> CODEC = StreamCodec.unit(new DungeonStartPayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
