package dev.eternia.professions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eternia.core.EterniaMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public record ProfessionData(Map<String, Long> totalXp) {
    public static final int MAX_LEVEL = 100;
    public static final Codec<ProfessionData> CODEC = Codec.unboundedMap(Codec.STRING, Codec.LONG)
            .xmap(ProfessionData::new, ProfessionData::totalXp);
    public static final StreamCodec<RegistryFriendlyByteBuf, ProfessionData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeVarInt(data.totalXp().size());
                data.totalXp().forEach((id, xp) -> {
                    ByteBufCodecs.STRING_UTF8.encode(buf, id);
                    buf.writeVarLong(xp);
                });
            },
            buf -> {
                int count = Math.max(0, Math.min(Profession.values().length, buf.readVarInt()));
                Map<String, Long> xp = new HashMap<>();
                for (int i = 0; i < count; i++) xp.put(ByteBufCodecs.STRING_UTF8.decode(buf), buf.readVarLong());
                return new ProfessionData(xp);
            }
    );
    public static final AttachmentType<ProfessionData> ATTACHMENT = AttachmentRegistry.create(
            EterniaMod.id("professions"), builder -> builder.initializer(ProfessionData::empty)
                    .persistent(CODEC).syncWith(STREAM_CODEC, AttachmentSyncPredicate.targetOnly()).copyOnDeath());
    private static final Map<UUID, Map<String, Long>> LAST_ACTION_TICK = new ConcurrentHashMap<>();

    public ProfessionData {
        totalXp = Map.copyOf(totalXp);
    }

    public static ProfessionData empty() { return new ProfessionData(Map.of()); }

    public static ProfessionData get(ServerPlayer player) { return player.getAttachedOrCreate(ATTACHMENT); }

    public long xp(Profession profession) { return Math.max(0L, totalXp.getOrDefault(profession.id(), 0L)); }

    public int level(Profession profession) {
        long xp = xp(profession);
        int level = 1;
        while (level < MAX_LEVEL && xp >= xpToNext(level)) {
            xp -= xpToNext(level);
            level++;
        }
        return level;
    }

    public long progressInLevel(Profession profession) {
        long xp = xp(profession);
        int level = 1;
        while (level < MAX_LEVEL && xp >= xpToNext(level)) {
            xp -= xpToNext(level);
            level++;
        }
        return level == MAX_LEVEL ? 0 : xp;
    }

    public static int xpToNext(int level) { return 50 + level * 10; }

    public ProfessionData addXp(Profession profession, int amount) {
        Map<String, Long> updated = new HashMap<>(totalXp);
        updated.merge(profession.id(), (long) Math.max(0, amount), Long::sum);
        return new ProfessionData(updated);
    }

    public static void award(ServerPlayer player, Profession profession, int amount) {
        if (amount <= 0) return;
        long gameTick = player.level().getGameTime();
        Map<String, Long> perProfession = LAST_ACTION_TICK.computeIfAbsent(player.getUUID(), ignored -> new ConcurrentHashMap<>());
        Long previousTick = perProfession.put(profession.id(), gameTick);
        if (previousTick != null && previousTick == gameTick) return;
        ProfessionData before = player.getAttachedOrCreate(ATTACHMENT);
        int oldLevel = before.level(profession);
        ProfessionData after = before.addXp(profession, amount);
        player.setAttached(ATTACHMENT, after);
        int newLevel = after.level(profession);
        if (newLevel > oldLevel) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "¡" + profession.displayName() + " nivel " + newLevel + "!"));
        }
    }

    public static void clearActionHistory(UUID playerId) { LAST_ACTION_TICK.remove(playerId); }

    public static void register() { }
}
