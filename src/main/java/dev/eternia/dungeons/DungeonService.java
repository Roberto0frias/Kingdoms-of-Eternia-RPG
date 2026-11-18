package dev.eternia.dungeons;

import dev.eternia.core.CharacterData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public final class DungeonService {
    public static final long COOLDOWN_TICKS = 24_000L;
    private DungeonService() { }

    public static DungeonData get(ServerPlayer player) { return player.getAttachedOrCreate(DungeonData.ATTACHMENT); }

    public static void start(ServerPlayer player) {
        if (!CharacterData.get(player).isCreated()) return;
        DungeonData data = get(player);
        if (data.active()) {
            player.sendSystemMessage(Component.literal("Tu expedición sigue abierta. Busca a Iriel en la entrada de la Ruina del Umbral."));
            return;
        }
        long now = player.level().getGameTime();
        if (now < data.availableAt()) {
            long seconds = (data.availableAt() - now + 19) / 20;
            player.sendSystemMessage(Component.literal("La ruina se estabilizará dentro de " + seconds + " segundos."));
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) return;
        DungeonGenerator.Site site = DungeonGenerator.findSite(level, player.blockPosition().getX(),
                player.blockPosition().getZ(), player.getUUID().getMostSignificantBits() ^ now);
        if (site == null) {
            player.sendSystemMessage(Component.literal("No encontramos terreno firme para la ruina. Aléjate de océanos o montañas e inténtalo otra vez."));
            return;
        }
        String dungeonId = UUID.randomUUID().toString();
        DungeonGenerator.build(level, site, level.getSeed() ^ site.x() * 341873128712L ^ site.z() * 132897987541L ^ now);
        if (DungeonNpc.spawn(level, site.x() + 1.5, site.y() + 1.0, site.z() + 1.5, dungeonId, player.getUUID()) == null
                || DungeonBosses.spawn(level, site.x() + 19.5, site.y() + 1.0, site.z() + 19.5, dungeonId, player.getUUID()) == null) {
            player.sendSystemMessage(Component.literal("La ruina se formó, pero sus guardianes no respondieron. Pide ayuda al administrador."));
            return;
        }
        data = data.begin(dungeonId, player.getUUID().toString(), site.x(), site.y(), site.z());
        player.setAttached(DungeonData.ATTACHMENT, data);
        player.sendSystemMessage(Component.literal("La Ruina del Umbral se ha formado cerca de tu posición. Coordenadas: "
                + site.x() + ", " + site.y() + ", " + site.z() + "."));
        player.sendSystemMessage(Component.literal("Encuentra a Iriel en la entrada, atraviesa el laberinto y derrota a Varkh."));
    }

    public static void onBossDeath(Entity entity, MinecraftServer server) {
        if (!entity.entityTags().contains(DungeonBosses.BOSS_TAG)) return;
        DungeonBosses.remove(entity);
        String ownerTag = DungeonBosses.ownerTagPrefix();
        String ownerValue = entity.entityTags().stream().filter(tag -> tag.startsWith(ownerTag)).findFirst().orElse("");
        if (ownerValue.isEmpty()) return;
        UUID ownerId;
        try { ownerId = UUID.fromString(ownerValue.substring(ownerTag.length())); }
        catch (IllegalArgumentException ignored) { return; }
        ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
        if (owner == null) return;
        DungeonData data = get(owner);
        String dungeonTag = entity.entityTags().stream().filter(tag -> tag.startsWith("eternia_dungeon="))
                .findFirst().orElse("");
        if (!data.dungeonId().isEmpty() && dungeonTag.equals("eternia_dungeon=" + data.dungeonId())) {
            owner.setAttached(DungeonData.ATTACHMENT, data.completeBoss());
            owner.sendSystemMessage(Component.literal("Varkh ha caído. Vuelve con Iriel para reclamar la recompensa de la expedición."));
        }
    }

    public static void register() { }
}
