package dev.eternia.economy;

import com.mojang.serialization.Codec;
import dev.eternia.core.EterniaMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

/** Persistent wallet stored as copper units: 100 copper = 1 silver. */
public record EconomyData(long copperBalance) {
    public static final Codec<EconomyData> CODEC = Codec.LONG.xmap(EconomyData::new, EconomyData::copperBalance);
    public static final StreamCodec<RegistryFriendlyByteBuf, EconomyData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> buf.writeVarLong(data.copperBalance()),
            buf -> new EconomyData(buf.readVarLong()));
    public static final AttachmentType<EconomyData> ATTACHMENT = AttachmentRegistry.create(
            EterniaMod.id("wallet"), builder -> builder.initializer(() -> new EconomyData(0))
                    .persistent(CODEC).syncWith(STREAM_CODEC, AttachmentSyncPredicate.targetOnly()).copyOnDeath());

    public EconomyData {
        copperBalance = Math.max(0, copperBalance);
    }

    public static long balance(ServerPlayer player) {
        return player.getAttachedOrCreate(ATTACHMENT).copperBalance();
    }

    public static void award(ServerPlayer player, long amount) {
        if (amount <= 0) return;
        EconomyData current = player.getAttachedOrCreate(ATTACHMENT);
        long next = current.copperBalance() > Long.MAX_VALUE - amount ? Long.MAX_VALUE : current.copperBalance() + amount;
        player.setAttached(ATTACHMENT, new EconomyData(next));
    }

    public static boolean spend(ServerPlayer player, long amount) {
        if (amount <= 0) return false;
        EconomyData current = player.getAttachedOrCreate(ATTACHMENT);
        if (current.copperBalance() < amount) return false;
        player.setAttached(ATTACHMENT, new EconomyData(current.copperBalance() - amount));
        return true;
    }

    public static void register() { }
}
