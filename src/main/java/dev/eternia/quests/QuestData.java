package dev.eternia.quests;

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

public record QuestData(String activeQuestId, int progress, Map<String, Long> cooldowns) {
    public static final Codec<QuestData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("active_quest", "").forGetter(QuestData::activeQuestId),
            Codec.INT.optionalFieldOf("progress", 0).forGetter(QuestData::progress),
            Codec.unboundedMap(Codec.STRING, Codec.LONG).optionalFieldOf("cooldowns", Map.of()).forGetter(QuestData::cooldowns)
    ).apply(instance, QuestData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuestData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, data.activeQuestId());
                buf.writeVarInt(data.progress());
                buf.writeVarInt(data.cooldowns().size());
                data.cooldowns().forEach((id, tick) -> {
                    ByteBufCodecs.STRING_UTF8.encode(buf, id);
                    buf.writeVarLong(tick);
                });
            },
            buf -> {
                String active = ByteBufCodecs.STRING_UTF8.decode(buf);
                int progress = Math.max(0, buf.readVarInt());
                int size = Math.max(0, Math.min(QuestContract.all().size(), buf.readVarInt()));
                Map<String, Long> cooldowns = new HashMap<>();
                for (int i = 0; i < size; i++) cooldowns.put(ByteBufCodecs.STRING_UTF8.decode(buf), buf.readVarLong());
                return new QuestData(active, progress, cooldowns);
            }
    );
    public static final AttachmentType<QuestData> ATTACHMENT = AttachmentRegistry.create(
            EterniaMod.id("quests"), builder -> builder.initializer(QuestData::empty)
                    .persistent(CODEC).syncWith(STREAM_CODEC, AttachmentSyncPredicate.targetOnly()).copyOnDeath());

    public QuestData {
        activeQuestId = activeQuestId == null ? "" : activeQuestId;
        progress = Math.max(0, progress);
        cooldowns = Map.copyOf(cooldowns);
    }

    public static QuestData empty() { return new QuestData("", 0, Map.of()); }

    public long cooldownUntil(String questId) { return cooldowns.getOrDefault(questId, 0L); }

    public QuestData activate(String questId) { return new QuestData(questId, 0, cooldowns); }

    public QuestData advance() { return new QuestData(activeQuestId, progress + 1, cooldowns); }

    public QuestData finish(String questId, long cooldownUntil) {
        Map<String, Long> updated = new HashMap<>(cooldowns);
        updated.put(questId, cooldownUntil);
        return new QuestData("", 0, updated);
    }

    public static void register() { }
}
