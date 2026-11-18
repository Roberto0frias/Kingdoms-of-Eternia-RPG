package dev.eternia.quests;

import dev.eternia.core.CharacterData;
import dev.eternia.economy.EconomyData;
import dev.eternia.progression.ProgressionData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;

public final class QuestService {
    private static final long COOLDOWN_TICKS = 24_000L;

    private QuestService() { }

    public static void accept(ServerPlayer player, String questId) {
        if (!CharacterData.get(player).isCreated()) return;
        QuestContract contract = QuestContract.find(questId);
        if (contract == null) return;
        QuestData data = player.getAttachedOrCreate(QuestData.ATTACHMENT);
        if (!data.activeQuestId().isEmpty()) {
            player.sendSystemMessage(Component.literal("Ya tienes un contrato activo."));
            return;
        }
        if (ProgressionData.get(player).level() < contract.minimumLevel()) {
            player.sendSystemMessage(Component.literal("Requiere nivel " + contract.minimumLevel() + "."));
            return;
        }
        long now = player.level().getGameTime();
        if (now < data.cooldownUntil(questId)) {
            player.sendSystemMessage(Component.literal("Ese contrato aún está en descanso."));
            return;
        }
        player.setAttached(QuestData.ATTACHMENT, data.activate(contract.id()));
        player.sendSystemMessage(Component.literal("Contrato aceptado: " + contract.name() + "."));
    }

    public static void onMonsterKilled(ServerPlayer player, Monster monster) {
        if (!CharacterData.get(player).isCreated()) return;
        QuestData data = player.getAttachedOrCreate(QuestData.ATTACHMENT);
        if (data.activeQuestId().isEmpty()) return;
        QuestContract contract = QuestContract.find(data.activeQuestId());
        if (contract == null || monster.getType() != contract.target()) return;
        int progress = data.progress() + 1;
        if (progress < contract.requiredKills()) {
            player.setAttached(QuestData.ATTACHMENT,
                    new QuestData(contract.id(), progress, data.cooldowns()));
            player.sendSystemMessage(Component.literal(contract.targetName() + ": " + progress + "/" + contract.requiredKills()));
            return;
        }
        player.setAttached(QuestData.ATTACHMENT,
                data.finish(contract.id(), player.level().getGameTime() + COOLDOWN_TICKS));
        int xp = contract.rewardXp();
        if ("human".equals(CharacterData.get(player).raceId())) xp = (xp * 105 + 99) / 100;
        ProgressionData.awardXp(player, xp);
        EconomyData.award(player, contract.rewardCopper());
        player.sendSystemMessage(Component.literal("¡Contrato completado: " + contract.name() + "! +"
                + xp + " XP, +" + contract.rewardCopper() + " Cobre."));
    }
}
