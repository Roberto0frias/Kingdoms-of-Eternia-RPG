package dev.eternia.mana;

import dev.eternia.attributes.AttributeStat;
import dev.eternia.core.CharacterData;
import dev.eternia.progression.PlayerAttributeService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

public final class ManaService {
    private ManaService() { }

    public static ManaData get(Entity entity) { return entity.getAttachedOrCreate(ManaData.ATTACHMENT); }
    public static int maximum(Entity entity) {
        if (!CharacterData.get(entity).isCreated()) return 0;
        return Math.min(1000, 50 + PlayerAttributeService.effectiveValue(entity, AttributeStat.INTELLIGENCE) * 5);
    }
    public static int current(Entity entity) { return Math.min(get(entity).current(), maximum(entity)); }
    public static int regenerationPerSecond(Entity entity) {
        return 2 + Math.max(0, PlayerAttributeService.effectiveValue(entity, AttributeStat.INTELLIGENCE)) / 20;
    }
    public static boolean spend(ServerPlayer player, int amount) {
        int current = current(player);
        if (amount < 0 || current < amount) {
            player.sendSystemMessage(Component.literal("Maná insuficiente: necesitas " + amount + "."));
            return false;
        }
        player.setAttached(ManaData.ATTACHMENT, get(player).withCurrent(current - amount));
        return true;
    }
    public static void restore(ServerPlayer player, int amount) {
        int value = Math.min(maximum(player), current(player) + Math.max(0, amount));
        player.setAttached(ManaData.ATTACHMENT, get(player).withCurrent(value));
    }
    public static void regenerate(ServerPlayer player) {
        if (!CharacterData.get(player).isCreated()) return;
        int current = current(player);
        int max = maximum(player);
        if (current < max) player.setAttached(ManaData.ATTACHMENT,
                get(player).withCurrent(Math.min(max, current + regenerationPerSecond(player))));
    }
}
