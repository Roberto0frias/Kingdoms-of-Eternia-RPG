package dev.eternia.dungeons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eternia.core.EterniaMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DungeonData(String dungeonId, String ownerId, int x, int y, int z,
                          long availableAt, boolean bossDefeated, boolean rewardClaimed) {
    public static final Codec<DungeonData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("dungeon", "").forGetter(DungeonData::dungeonId),
            Codec.STRING.optionalFieldOf("owner", "").forGetter(DungeonData::ownerId),
            Codec.INT.optionalFieldOf("x", 0).forGetter(DungeonData::x),
            Codec.INT.optionalFieldOf("y", 0).forGetter(DungeonData::y),
            Codec.INT.optionalFieldOf("z", 0).forGetter(DungeonData::z),
            Codec.LONG.optionalFieldOf("available_at", 0L).forGetter(DungeonData::availableAt),
            Codec.BOOL.optionalFieldOf("boss_defeated", false).forGetter(DungeonData::bossDefeated),
            Codec.BOOL.optionalFieldOf("reward_claimed", false).forGetter(DungeonData::rewardClaimed)
    ).apply(instance, DungeonData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DungeonData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, data.dungeonId());
                ByteBufCodecs.STRING_UTF8.encode(buf, data.ownerId());
                buf.writeInt(data.x()); buf.writeInt(data.y()); buf.writeInt(data.z());
                buf.writeVarLong(data.availableAt());
                buf.writeBoolean(data.bossDefeated()); buf.writeBoolean(data.rewardClaimed());
            },
            buf -> new DungeonData(ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.STRING_UTF8.decode(buf),
                    buf.readInt(), buf.readInt(), buf.readInt(), buf.readVarLong(), buf.readBoolean(), buf.readBoolean())
    );
    public static final AttachmentType<DungeonData> ATTACHMENT = AttachmentRegistry.create(
            EterniaMod.id("expedition"), builder -> builder.initializer(DungeonData::empty)
                    .persistent(CODEC).syncWith(STREAM_CODEC, AttachmentSyncPredicate.targetOnly()).copyOnDeath());

    public DungeonData {
        dungeonId = dungeonId == null ? "" : dungeonId;
        ownerId = ownerId == null ? "" : ownerId;
    }

    public static DungeonData empty() { return new DungeonData("", "", 0, 0, 0, 0, false, false); }
    public boolean active() { return !dungeonId.isEmpty() && !rewardClaimed; }
    public DungeonData begin(String id, String owner, int atX, int atY, int atZ) {
        return new DungeonData(id, owner, atX, atY, atZ, availableAt, false, false);
    }
    public DungeonData completeBoss() {
        return new DungeonData(dungeonId, ownerId, x, y, z, availableAt, true, rewardClaimed);
    }
    public DungeonData claim(long nextAvailableAt) {
        return new DungeonData(dungeonId, ownerId, x, y, z, nextAvailableAt, true, true);
    }
    public static void register() { }
}
