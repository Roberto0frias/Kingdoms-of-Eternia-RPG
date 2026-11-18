package dev.eternia.talents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eternia.core.EterniaMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

public record TalentData(int talentPoints, Map<String, Integer> ranks) {
    public static final Codec<TalentData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("talent_points", 0).forGetter(TalentData::talentPoints),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("ranks", Map.of()).forGetter(TalentData::ranks)
    ).apply(instance, TalentData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TalentData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeVarInt(data.talentPoints());
                buf.writeVarInt(data.ranks().size());
                data.ranks().forEach((id, rank) -> {
                    ByteBufCodecs.STRING_UTF8.encode(buf, id);
                    buf.writeVarInt(rank);
                });
            },
            buf -> {
                int points = buf.readVarInt();
                int count = Math.max(0, Math.min(256, buf.readVarInt()));
                Map<String, Integer> ranks = new HashMap<>();
                for (int i = 0; i < count; i++) {
                    String id = ByteBufCodecs.STRING_UTF8.decode(buf);
                    ranks.put(id, buf.readVarInt());
                }
                return new TalentData(points, ranks);
            }
    );

    public static final AttachmentType<TalentData> ATTACHMENT = AttachmentRegistry.create(
            EterniaMod.id("talents"), builder -> builder
                    .initializer(TalentData::empty)
                    .persistent(CODEC)
                    .syncWith(STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
                    .copyOnDeath()
    );

    public TalentData {
        ranks = Map.copyOf(ranks);
    }

    public static TalentData empty() {
        return new TalentData(0, Map.of());
    }

    public int rank(TalentNode node) {
        return Math.max(0, ranks.getOrDefault(node.id(), 0));
    }

    public TalentData addPoints(int amount) {
        return new TalentData(Math.max(0, talentPoints + amount), ranks);
    }

    public TalentData upgrade(TalentNode node) {
        Map<String, Integer> updated = new HashMap<>(ranks);
        updated.put(node.id(), rank(node) + 1);
        return new TalentData(talentPoints - 1, updated);
    }

    public static void register() { }
}
