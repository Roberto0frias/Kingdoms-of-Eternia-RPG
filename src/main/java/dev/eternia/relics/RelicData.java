package dev.eternia.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eternia.core.EterniaMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record RelicData(String ownerId, String classId, String activeFormId, List<String> unlockedForms) {
    public static final Codec<RelicData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("owner", "").forGetter(RelicData::ownerId),
            Codec.STRING.optionalFieldOf("class", "").forGetter(RelicData::classId),
            Codec.STRING.optionalFieldOf("active_form", "").forGetter(RelicData::activeFormId),
            Codec.list(Codec.STRING).optionalFieldOf("unlocked_forms", List.of()).forGetter(RelicData::unlockedForms)
    ).apply(instance, RelicData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, RelicData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, data.ownerId());
                ByteBufCodecs.STRING_UTF8.encode(buf, data.classId());
                ByteBufCodecs.STRING_UTF8.encode(buf, data.activeFormId());
                buf.writeVarInt(data.unlockedForms().size());
                data.unlockedForms().forEach(id -> ByteBufCodecs.STRING_UTF8.encode(buf, id));
            },
            buf -> {
                String owner = ByteBufCodecs.STRING_UTF8.decode(buf);
                String playerClass = ByteBufCodecs.STRING_UTF8.decode(buf);
                String active = ByteBufCodecs.STRING_UTF8.decode(buf);
                int count = Math.max(0, Math.min(RelicForm.all().size(), buf.readVarInt()));
                java.util.ArrayList<String> unlocked = new java.util.ArrayList<>(count);
                for (int i = 0; i < count; i++) unlocked.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                return new RelicData(owner, playerClass, active, unlocked);
            }
    );
    public static final AttachmentType<RelicData> ATTACHMENT = AttachmentRegistry.create(
            EterniaMod.id("class_relic_data"), builder -> builder.initializer(RelicData::empty)
                    .persistent(CODEC).syncWith(STREAM_CODEC, AttachmentSyncPredicate.targetOnly()).copyOnDeath());

    public RelicData {
        ownerId = ownerId == null ? "" : ownerId;
        classId = classId == null ? "" : classId;
        activeFormId = activeFormId == null ? "" : activeFormId;
        unlockedForms = List.copyOf(unlockedForms);
    }

    public static RelicData empty() { return new RelicData("", "", "", List.of()); }
    public boolean owns(String formId) { return unlockedForms.contains(formId); }
    public RelicData select(String formId) { return new RelicData(ownerId, classId, formId, unlockedForms); }
    public RelicData unlock(String formId) {
        java.util.ArrayList<String> updated = new java.util.ArrayList<>(unlockedForms);
        if (!updated.contains(formId)) updated.add(formId);
        return new RelicData(ownerId, classId, formId, updated);
    }
    public static void register() { }
}
