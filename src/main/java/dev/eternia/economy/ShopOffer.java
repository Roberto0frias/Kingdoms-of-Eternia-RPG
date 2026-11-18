package dev.eternia.economy;

import dev.eternia.professions.Profession;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

/** Server-owned catalogue: unlock, item and price are never accepted from the client. */
public record ShopOffer(String id, String name, String itemId, int count, long priceCopper,
                        Profession profession, int requiredLevel) {
    private static final List<ShopOffer> OFFERS = List.of(
            new ShopOffer("miner_coal", "Carbón x8", "coal", 8, 80, Profession.MINER, 1),
            new ShopOffer("miner_iron", "Lingote de hierro x4", "iron_ingot", 4, 500, Profession.MINER, 10),
            new ShopOffer("miner_gold", "Lingote de oro x4", "gold_ingot", 4, 2_000, Profession.MINER, 25),
            new ShopOffer("miner_diamond", "Diamante", "diamond", 1, 12_000, Profession.MINER, 50),
            new ShopOffer("farmer_bread", "Pan x8", "bread", 8, 100, Profession.FARMER, 1),
            new ShopOffer("farmer_carrot", "Zanahoria dorada x2", "golden_carrot", 2, 1_200, Profession.FARMER, 25),
            new ShopOffer("smith_iron_sword", "Espada de hierro", "iron_sword", 1, 800, Profession.BLACKSMITH, 1),
            new ShopOffer("smith_diamond_sword", "Espada de diamante", "diamond_sword", 1, 25_000, Profession.BLACKSMITH, 50)
    );

    public static List<ShopOffer> all() { return OFFERS; }
    public static ShopOffer find(String id) { return OFFERS.stream().filter(offer -> offer.id.equals(id)).findFirst().orElse(null); }
    public Item item() {
        return BuiltInRegistries.ITEM.get(Identifier.withDefaultNamespace(itemId))
                .map(reference -> reference.value()).orElse(Items.AIR);
    }
}
