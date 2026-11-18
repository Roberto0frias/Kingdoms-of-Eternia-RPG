package dev.eternia.quests;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.List;

/** Fixed bounty catalog; all objectives and rewards are server-owned. */
public record QuestContract(String id, String name, String description, EntityType<?> target,
                            String targetName, int requiredKills, int minimumLevel,
                            int rewardXp, long rewardCopper) {
    private static final List<QuestContract> CONTRACTS = List.of(
            new QuestContract("zombie_bounty", "Erradicación de zombis", "Caza zombis", entityType("zombie"),
                    "Zombis", 8, 1, 90, 220),
            new QuestContract("skeleton_bounty", "Silencio para los esqueletos", "Caza esqueletos", entityType("skeleton"),
                    "Esqueletos", 6, 5, 160, 450),
            new QuestContract("spider_bounty", "Control de arácnidos", "Caza arañas", entityType("spider"),
                    "Arañas", 5, 10, 280, 800)
    );

    public static List<QuestContract> all() { return CONTRACTS; }
    public static QuestContract find(String id) { return CONTRACTS.stream().filter(contract -> contract.id.equals(id)).findFirst().orElse(null); }

    private static EntityType<?> entityType(String id) {
        return BuiltInRegistries.ENTITY_TYPE.get(Identifier.withDefaultNamespace(id))
                .map(reference -> reference.value()).orElseThrow();
    }
}
