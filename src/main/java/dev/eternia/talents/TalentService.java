package dev.eternia.talents;

import dev.eternia.classes.PlayerClass;
import dev.eternia.core.CharacterData;
import dev.eternia.progression.ProgressionData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class TalentService {
    private TalentService() { }

    public static TalentData get(Entity entity) {
        return entity.getAttachedOrCreate(TalentData.ATTACHMENT);
    }

    public static void grantLevelPoints(ServerPlayer player, int levelsGained) {
        ProgressionData.Progression current = ProgressionData.get(player);
        int points = current.level() / 5 - (current.level() - levelsGained) / 5;
        if (points > 0) player.setAttached(TalentData.ATTACHMENT, get(player).addPoints(points));
    }

    public static boolean canUpgrade(ServerPlayer player, TalentNode node) {
        if (node == null) return false;
        PlayerClass playerClass = PlayerClass.fromId(CharacterData.get(player).classId());
        if (playerClass != node.playerClass()) return false;

        ProgressionData.Progression progression = ProgressionData.get(player);
        TalentData data = get(player);
        if (progression.level() < node.requiredLevel() || data.talentPoints() <= 0 || data.rank(node) >= TalentNode.MAX_RANK) return false;

        var branchNodes = TalentNode.forBranch(node.branch());
        int index = branchNodes.indexOf(node);
        return index == 0 || data.rank(branchNodes.get(index - 1)) > 0;
    }

    public static boolean upgrade(ServerPlayer player, String nodeId) {
        TalentNode node = TalentNode.fromId(nodeId);
        if (!canUpgrade(player, node)) return false;
        player.setAttached(TalentData.ATTACHMENT, get(player).upgrade(node));
        return true;
    }

    public static double totalEffect(Entity entity, TalentEffect effect) {
        PlayerClass playerClass = PlayerClass.fromId(CharacterData.get(entity).classId());
        if (playerClass == null) return 0.0;
        TalentData data = get(entity);
        double total = 0.0;
        for (TalentNode node : TalentNode.values()) {
            if (node.playerClass() == playerClass && node.effect() == effect) {
                total += node.amountPerRank() * data.rank(node);
            }
        }
        return total;
    }
}
