package dev.eternia.core;

import dev.eternia.attributes.AttributeStat;
import dev.eternia.core.network.AllocateAttributePayload;
import dev.eternia.core.network.CharacterChoicePayload;
import dev.eternia.classes.PlayerClass;
import dev.eternia.classes.Specialization;
import dev.eternia.progression.PlayerAttributeService;
import dev.eternia.progression.ProgressionData;
import dev.eternia.races.Race;
import dev.eternia.core.network.SpecializationChoicePayload;
import dev.eternia.core.network.ActivateSkillPayload;
import dev.eternia.core.network.ReleaseSkillPayload;
import dev.eternia.core.network.TalentUpgradePayload;
import dev.eternia.core.network.ShopPurchasePayload;
import dev.eternia.economy.ShopService;
import dev.eternia.core.network.QuestAcceptPayload;
import dev.eternia.quests.QuestData;
import dev.eternia.quests.QuestService;
import dev.eternia.relics.ClassRelicItem;
import dev.eternia.relics.RelicData;
import dev.eternia.relics.RelicService;
import dev.eternia.core.network.RelicFormPayload;
import dev.eternia.dungeons.DungeonData;
import dev.eternia.dungeons.DungeonService;
import dev.eternia.dungeons.DungeonNpc;
import dev.eternia.dungeons.DungeonBosses;
import dev.eternia.dungeons.DungeonWorldgen;
import dev.eternia.skills.SkillService;
import dev.eternia.mana.ManaData;
import dev.eternia.mana.ManaService;
import dev.eternia.talents.TalentData;
import dev.eternia.talents.TalentService;
import dev.eternia.professions.Profession;
import dev.eternia.professions.ProfessionData;
import dev.eternia.economy.EconomyData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.CropBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EterniaMod implements ModInitializer {
    public static final String MOD_ID = "eternia";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        CharacterData.register();
        ProgressionData.register();
        ManaData.register();
        TalentData.register();
        ProfessionData.register();
        EconomyData.register();
        QuestData.register();
        RelicData.register();
        ClassRelicItem.register();
        DungeonData.register();
        DungeonWorldgen.register();
        DungeonNpc.registerInteraction();
        PayloadTypeRegistry.serverboundPlay().register(CharacterChoicePayload.TYPE, CharacterChoicePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(AllocateAttributePayload.TYPE, AllocateAttributePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SpecializationChoicePayload.TYPE, SpecializationChoicePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ActivateSkillPayload.TYPE, ActivateSkillPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ReleaseSkillPayload.TYPE, ReleaseSkillPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(TalentUpgradePayload.TYPE, TalentUpgradePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ShopPurchasePayload.TYPE, ShopPurchasePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(QuestAcceptPayload.TYPE, QuestAcceptPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RelicFormPayload.TYPE, RelicFormPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(CharacterChoicePayload.TYPE, (payload, context) -> {
            Race race = Race.fromId(payload.raceId());
            PlayerClass playerClass = PlayerClass.fromId(payload.classId());
            if (race == null || playerClass == null) {
                context.player().sendSystemMessage(Component.literal("Selección inválida."));
                return;
            }
            CharacterData.Profile current = context.player().getAttachedOrCreate(CharacterData.PROFILE);
            if (current.isCreated()) {
                context.player().sendSystemMessage(Component.literal("Tu personaje ya fue creado."));
                return;
            }
            context.player().setAttached(CharacterData.PROFILE, current.withChoices(race.id(), playerClass.id()));
            ProgressionData.get(context.player());
            RelicService.initialize(context.player());
            PlayerAttributeService.refresh(context.player());
            context.player().sendSystemMessage(Component.literal("Personaje creado: " + race.displayName() + " " + playerClass.displayName() + "."));
            LOGGER.info("Character created for {}: race={}, class={}", context.player().getGameProfile().name(), race.id(), playerClass.id());
        });
        ServerPlayNetworking.registerGlobalReceiver(AllocateAttributePayload.TYPE, (payload, context) -> {
            AttributeStat stat = AttributeStat.fromId(payload.statId());
            ServerPlayer player = context.player();
            if (stat == null || !CharacterData.get(player).isCreated()) return;
            if (ProgressionData.allocate(player, stat)) {
                PlayerAttributeService.refresh(player);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(SpecializationChoicePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            CharacterData.Profile profile = CharacterData.get(player);
            Specialization specialization = Specialization.fromId(payload.specializationId());
            if (!profile.isCreated() || specialization == null) return;
            if (ProgressionData.get(player).level() < 10) {
                player.sendSystemMessage(Component.literal("Desbloqueas la especialización al nivel 10."));
                return;
            }
            PlayerClass playerClass = PlayerClass.fromId(profile.classId());
            if (specialization.playerClass() != playerClass) {
                player.sendSystemMessage(Component.literal("Esa especialización no corresponde a tu clase."));
                return;
            }
            if (!profile.specializationId().isBlank()) {
                player.sendSystemMessage(Component.literal("Tu especialización ya fue elegida."));
                return;
            }
            player.setAttached(CharacterData.PROFILE, profile.withSpecialization(specialization.id()));
            player.sendSystemMessage(Component.literal("Especialización elegida: " + specialization.displayName() + "."));
        });
        ServerPlayNetworking.registerGlobalReceiver(ActivateSkillPayload.TYPE,
                (payload, context) -> SkillService.beginCharge(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(ReleaseSkillPayload.TYPE,
                (payload, context) -> SkillService.releaseCharge(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(TalentUpgradePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (TalentService.upgrade(player, payload.nodeId())) {
                PlayerAttributeService.refresh(player);
                player.sendSystemMessage(Component.literal("Talento mejorado: " + payload.nodeId()));
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(ShopPurchasePayload.TYPE,
                (payload, context) -> ShopService.purchase(context.player(), payload.offerId()));
        ServerPlayNetworking.registerGlobalReceiver(QuestAcceptPayload.TYPE,
                (payload, context) -> QuestService.accept(context.player(), payload.questId()));
        ServerPlayNetworking.registerGlobalReceiver(RelicFormPayload.TYPE, (payload, context) -> {
            if (payload.fuse()) RelicService.fuse(context.player(), payload.formId());
            else RelicService.select(context.player(), payload.formId());
        });
        ServerPlayerEvents.JOIN.register(player -> {
            PlayerAttributeService.refresh(player);
            RelicService.initialize(player);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            PlayerAttributeService.refresh(newPlayer);
            RelicService.initialize(newPlayer);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long tick = server.overworld().getGameTime();
            if (tick % 10L == 0L) DungeonBosses.tick(server);
            if (tick % 20L == 0L) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (CharacterData.get(player).isCreated()) {
                        RelicService.ensureInventoryItem(player, RelicService.get(player));
                        ManaService.regenerate(player);
                    }
                }
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) SkillService.tickCharge(player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ProfessionData.clearActionHistory(handler.player.getUUID());
            SkillService.clearCharge(handler.player.getUUID());
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity.level().getServer() != null) DungeonService.onBossDeath(entity, damageSource, entity.level().getServer());
            if (!(entity instanceof Monster monster) || !(damageSource.getEntity() instanceof ServerPlayer player)) return;
            if (!CharacterData.get(player).isCreated()) return;
            int baseReward = Math.max(5, Math.min(100, 5 + (int) Math.ceil(monster.getMaxHealth() / 2.0)));
            int reward = Math.max(1, (baseReward * PlayerAttributeService.xpMultiplierPercent(player) + 99) / 100);
            ProgressionData.awardXp(player, reward);
            ProfessionData.award(player, Profession.HUNTER, Math.min(25, 5 + (int) (monster.getMaxHealth() / 5.0)));
            EconomyData.award(player, Math.max(1L, Math.min(250L, 1L + (long) Math.ceil(monster.getMaxHealth() / 10.0))));
            QuestService.onMonsterKilled(player, monster);
        });
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayer serverPlayer) || !CharacterData.get(serverPlayer).isCreated()) return;
            if (state.is(BlockTags.LOGS)) {
                ProfessionData.award(serverPlayer, Profession.LUMBERJACK, 3);
            } else if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                ProfessionData.award(serverPlayer, Profession.FARMER, 2);
            } else if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
                ProfessionData.award(serverPlayer, Profession.MINER, state.requiresCorrectToolForDrops() ? 5 : 2);
            }
        });
        LOGGER.info("Kingdoms of Eternia RPG core initialized.");
    }
}
