package dev.eternia.economy;

import dev.eternia.core.CharacterData;
import dev.eternia.professions.ProfessionData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ShopService {
    private ShopService() { }

    public static void purchase(ServerPlayer player, String offerId) {
        if (!CharacterData.get(player).isCreated()) return;
        ShopOffer offer = ShopOffer.find(offerId);
        if (offer == null) return;
        if (ProfessionData.get(player).level(offer.profession()) < offer.requiredLevel()) {
            player.sendSystemMessage(Component.literal("Requiere " + offer.profession().displayName()
                    + " nivel " + offer.requiredLevel() + "."));
            return;
        }
        if (EconomyData.balance(player) < offer.priceCopper()) {
            player.sendSystemMessage(Component.literal("No tienes suficiente Cobre."));
            return;
        }
        ItemStack stack = new ItemStack(offer.item(), offer.count());
        if (stack.isEmpty() || !EconomyData.spend(player, offer.priceCopper())) return;
        player.getInventory().add(stack);
        if (!stack.isEmpty()) player.drop(stack, false);
        player.sendSystemMessage(Component.literal("Compraste " + offer.name() + " por " + offer.priceCopper() + " Cobre."));
    }
}
