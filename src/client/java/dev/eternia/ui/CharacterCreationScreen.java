package dev.eternia.ui;

import dev.eternia.attributes.AttributeStat;
import dev.eternia.classes.PlayerClass;
import dev.eternia.classes.Specialization;
import dev.eternia.core.CharacterData;
import dev.eternia.core.network.SpecializationChoicePayload;
import dev.eternia.core.network.TalentUpgradePayload;
import dev.eternia.core.network.AllocateAttributePayload;
import dev.eternia.core.network.CharacterChoicePayload;
import dev.eternia.progression.PlayerAttributeService;
import dev.eternia.progression.ProgressionData;
import dev.eternia.professions.Profession;
import dev.eternia.professions.ProfessionData;
import dev.eternia.economy.EconomyData;
import dev.eternia.economy.ShopOffer;
import dev.eternia.core.network.ShopPurchasePayload;
import dev.eternia.core.network.QuestAcceptPayload;
import dev.eternia.quests.QuestContract;
import dev.eternia.quests.QuestData;
import dev.eternia.relics.RelicData;
import dev.eternia.relics.RelicForm;
import dev.eternia.core.network.RelicFormPayload;
import dev.eternia.dungeons.DungeonData;
import dev.eternia.core.network.DungeonStartPayload;
import dev.eternia.races.Race;
import dev.eternia.skills.ActiveSkill;
import dev.eternia.talents.TalentBranch;
import dev.eternia.talents.TalentData;
import dev.eternia.talents.TalentNode;
import dev.eternia.talents.TalentService;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CharacterCreationScreen extends Screen {
    private enum Page { CHARACTER, TALENTS, PROFESSIONS, ECONOMY, MARKET, QUESTS, RELIC, DUNGEON }

    private Page page = Page.CHARACTER;
    private Race selectedRace = Race.HUMAN;
    private PlayerClass selectedClass = PlayerClass.WARRIOR;
    private Specialization selectedSpecialization = Specialization.KNIGHT;
    private TalentBranch selectedBranch = TalentBranch.WARRIOR_DEFENSE;

    public CharacterCreationScreen() {
        super(Component.literal("Kingdoms of Eternia RPG"));
        if (net.minecraft.client.Minecraft.getInstance().player != null
                && net.minecraft.client.Minecraft.getInstance().player.hasAttached(CharacterData.PROFILE)) {
            CharacterData.Profile profile = net.minecraft.client.Minecraft.getInstance().player.getAttached(CharacterData.PROFILE);
            if (profile != null) {
                Race race = Race.fromId(profile.raceId());
                PlayerClass playerClass = PlayerClass.fromId(profile.classId());
                if (race != null) selectedRace = race;
                if (playerClass != null) selectedClass = playerClass;
                Specialization specialization = Specialization.fromId(profile.specializationId());
                if (specialization != null) selectedSpecialization = specialization;
                else if (playerClass != null) selectedSpecialization = Specialization.forClass(playerClass).getFirst();
                if (playerClass != null) selectedBranch = TalentBranch.forClass(playerClass)[0];
            }
        }
    }

    @Override
    protected void init() {
        if (hasCreatedCharacter()) {
            switch (page) {
                case TALENTS -> initTalentSheet();
                case PROFESSIONS -> initProfessionSheet();
                case ECONOMY -> initEconomySheet();
                case MARKET -> initMarketSheet();
                case QUESTS -> initQuestSheet();
                case RELIC -> initRelicSheet();
                case DUNGEON -> initDungeonSheet();
                default -> initCharacterSheet();
            }
            return;
        }
        initCreationForm();
    }

    private boolean hasCreatedCharacter() {
        return this.minecraft.player != null
                && this.minecraft.player.hasAttached(CharacterData.PROFILE)
                && CharacterData.get(this.minecraft.player).isCreated();
    }

    private void initCreationForm() {
        int left = this.width / 2 - 205;
        int right = this.width / 2 + 5;
        int top = this.height / 2 - 70;
        for (int i = 0; i < Race.values().length; i++) {
            Race race = Race.values()[i];
            this.addRenderableWidget(Button.builder(Component.literal((race == selectedRace ? "✓ " : "") + race.displayName()),
                    button -> { selectedRace = race; rebuildWidgets(); }).bounds(left, top + i * 24, 195, 20).build());
        }
        for (int i = 0; i < PlayerClass.values().length; i++) {
            PlayerClass playerClass = PlayerClass.values()[i];
            this.addRenderableWidget(Button.builder(Component.literal((playerClass == selectedClass ? "✓ " : "") + playerClass.displayName()),
                    button -> { selectedClass = playerClass; selectedSpecialization = Specialization.forClass(playerClass).getFirst(); rebuildWidgets(); }).bounds(right, top + i * 24, 195, 20).build());
        }
        this.addRenderableWidget(Button.builder(Component.literal("Crear personaje"), button -> confirmChoice())
                .bounds(this.width / 2 - 100, top + 142, 200, 22).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cerrar"), button -> this.onClose())
                .bounds(this.width / 2 - 100, top + 168, 200, 20).build());
    }

    private void initCharacterSheet() {
        int top = this.height / 2 - 120;
        CharacterData.Profile profile = CharacterData.get(this.minecraft.player);
        ProgressionData.Progression progression = ProgressionData.get(this.minecraft.player);
        if (profile.specializationId().isBlank() && progression.level() >= 10) {
            this.addRenderableWidget(Button.builder(Component.literal("‹"), button -> changeSpecialization(-1))
                    .bounds(this.width / 2 - 100, top + 46, 24, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("›"), button -> changeSpecialization(1))
                    .bounds(this.width / 2 + 76, top + 46, 24, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Elegir especialización"), button -> chooseSpecialization())
                    .bounds(this.width / 2 - 88, top + 68, 176, 20).build());
        }
        for (int i = 0; i < AttributeStat.values().length; i++) {
            AttributeStat stat = AttributeStat.values()[i];
            int y = top + 109 + i * 17;
            this.addRenderableWidget(Button.builder(Component.literal("+"), button -> allocate(stat))
                    .bounds(this.width / 2 + 124, y - 3, 22, 20).build());
        }
        addPageNavigation(top + 218);
    }

    private void initTalentSheet() {
        TalentBranch[] branches = TalentBranch.forClass(PlayerClass.fromId(CharacterData.get(this.minecraft.player).classId()));
        int gap = 4;
        int width = (this.width - 16 - gap * (branches.length - 1)) / branches.length;
        for (int i = 0; i < branches.length; i++) {
            TalentBranch branch = branches[i];
            this.addRenderableWidget(Button.builder(Component.literal(branch.displayName()), button -> {
                selectedBranch = branch;
                rebuildWidgets();
            }).bounds(8 + i * (width + gap), 21, width, 20).build());
        }
        addPageNavigation(this.height - 22);

        for (int i = 0; i < TalentNode.forBranch(selectedBranch).size(); i++) {
            TalentNode node = TalentNode.forBranch(selectedBranch).get(i);
            int y = 53 + i * 50;
            this.addRenderableWidget(Button.builder(Component.literal("Mejorar"), button -> upgradeTalent(node))
                    .bounds(this.width - 76, y, 68, 20).build());
        }
    }

    private void initProfessionSheet() {
        addPageNavigation(this.height - 22);
    }

    private void initEconomySheet() {
        addPageNavigation(this.height - 22);
        this.addRenderableWidget(Button.builder(Component.literal("Abrir mercado"), button -> {
            page = Page.MARKET;
            rebuildWidgets();
        }).bounds(this.width / 2 - 150, this.height / 2 + 76, 140, 22).build());
        this.addRenderableWidget(Button.builder(Component.literal("Contratos de caza"), button -> {
            page = Page.QUESTS;
            rebuildWidgets();
        }).bounds(this.width / 2 + 10, this.height / 2 + 76, 140, 22).build());
    }

    private void initMarketSheet() {
        addPageNavigation(this.height - 22);
        int columns = 2;
        int gap = 8;
        int cardWidth = (this.width - 16 - gap) / columns;
        for (int i = 0; i < ShopOffer.all().size(); i++) {
            ShopOffer offer = ShopOffer.all().get(i);
            int column = i % columns;
            int row = i / columns;
            int x = 8 + column * (cardWidth + gap);
            int y = 24 + row * 47;
            this.addRenderableWidget(Button.builder(Component.literal("Comprar · " + offer.priceCopper() + " C"), button -> buy(offer))
                    .bounds(x, y + 25, cardWidth, 18).build());
        }
    }

    private void initQuestSheet() {
        addPageNavigation(this.height - 22);
        QuestData data = this.minecraft.player.getAttachedOrCreate(QuestData.ATTACHMENT);
        int level = ProgressionData.get(this.minecraft.player).level();
        long now = this.minecraft.level == null ? 0 : this.minecraft.level.getGameTime();
        for (int i = 0; i < QuestContract.all().size(); i++) {
            QuestContract contract = QuestContract.all().get(i);
            int y = 48 + i * 55;
            boolean active = contract.id().equals(data.activeQuestId());
            boolean unavailable = !data.activeQuestId().isEmpty() && !active;
            boolean levelLocked = level < contract.minimumLevel();
            boolean cooldown = now < data.cooldownUntil(contract.id());
            String label = active ? "En curso" : unavailable ? "Otro contrato activo"
                    : levelLocked ? "Nivel " + contract.minimumLevel()
                    : cooldown ? "En descanso" : "Aceptar";
            Button button = Button.builder(Component.literal(label), widget -> acceptQuest(contract))
                    .bounds(this.width - 154, y + 8, 146, 20).build();
            button.active = !active && !unavailable && !levelLocked && !cooldown;
            this.addRenderableWidget(button);
        }
    }

    private void initRelicSheet() {
        addPageNavigation(this.height - 22);
        CharacterData.Profile profile = CharacterData.get(this.minecraft.player);
        PlayerClass playerClass = PlayerClass.fromId(profile.classId());
        RelicData data = this.minecraft.player.getAttachedOrCreate(RelicData.ATTACHMENT);
        for (int i = 0; i < RelicForm.forClass(playerClass).size(); i++) {
            RelicForm form = RelicForm.forClass(playerClass).get(i);
            int y = 45 + i * 69;
            boolean unlocked = data.owns(form.id());
            boolean active = form.id().equals(data.activeFormId());
            String buttonLabel = active ? "Equipada" : unlocked ? "Cambiar forma" : "Fusionar";
            Button button = Button.builder(Component.literal(buttonLabel), widget -> {
                if (unlocked) changeRelicForm(form, false);
                else changeRelicForm(form, true);
            }).bounds(this.width - 154, y + 17, 146, 20).build();
            button.active = !active;
            this.addRenderableWidget(button);
        }
    }

    private void initDungeonSheet() {
        addPageNavigation(this.height - 22);
        DungeonData data = this.minecraft.player.getAttachedOrCreate(DungeonData.ATTACHMENT);
        long now = this.minecraft.level == null ? 0 : this.minecraft.level.getGameTime();
        boolean available = !data.active() && now >= data.availableAt();
        String label = data.active() ? "Expedición activa"
                : now < data.availableAt() ? "Ruina en descanso" : "Abrir una expedición";
        Button start = Button.builder(Component.literal(label), button -> startDungeon())
                .bounds(this.width / 2 - 100, this.height / 2 + 48, 200, 22).build();
        start.active = available;
        this.addRenderableWidget(start);
    }

    private void addPageNavigation(int y) {
        String[] labels = {"Perfil", "Talentos", "Oficios", "Monedas", "Reliquia", "Mazmorra", "Cerrar"};
        Page[] pages = {Page.CHARACTER, Page.TALENTS, Page.PROFESSIONS, Page.ECONOMY, Page.RELIC, Page.DUNGEON, null};
        int buttonWidth = Math.min(76, (this.width - 16 - 12) / labels.length);
        int gap = 2;
        int total = buttonWidth * labels.length + gap * (labels.length - 1);
        int x = (this.width - total) / 2;
        for (int i = 0; i < labels.length; i++) {
            final Page destination = pages[i];
            Button.Builder builder = Button.builder(Component.literal(labels[i]), button -> {
                if (destination == null) this.onClose();
                else { page = destination; rebuildWidgets(); }
            });
            this.addRenderableWidget(builder.bounds(x + i * (buttonWidth + gap), y, buttonWidth, 20).build());
        }
    }

    private void upgradeTalent(TalentNode node) {
        if (this.minecraft.getConnection() != null) {
            ClientPlayNetworking.send(new TalentUpgradePayload(node.id()));
        }
    }

    private void buy(ShopOffer offer) {
        if (this.minecraft.getConnection() != null) {
            ClientPlayNetworking.send(new ShopPurchasePayload(offer.id()));
        }
    }

    private void changeRelicForm(RelicForm form, boolean fuse) {
        if (this.minecraft.getConnection() != null) {
            ClientPlayNetworking.send(new RelicFormPayload(form.id(), fuse));
        }
    }

    private void startDungeon() {
        if (this.minecraft.getConnection() != null) {
            ClientPlayNetworking.send(new DungeonStartPayload());
        }
    }

    private void acceptQuest(QuestContract contract) {
        if (this.minecraft.getConnection() != null) {
            ClientPlayNetworking.send(new QuestAcceptPayload(contract.id()));
        }
    }

    private void changeSpecialization(int direction) {
        PlayerClass playerClass = PlayerClass.fromId(CharacterData.get(this.minecraft.player).classId());
        var choices = Specialization.forClass(playerClass);
        int current = choices.indexOf(selectedSpecialization);
        int next = Math.floorMod(current + direction, choices.size());
        selectedSpecialization = choices.get(next);
        rebuildWidgets();
    }

    private void chooseSpecialization() {
        if (selectedSpecialization != null && this.minecraft.getConnection() != null) {
            ClientPlayNetworking.send(new SpecializationChoicePayload(selectedSpecialization.id()));
            this.minecraft.gui.setScreen(null);
        }
    }

    private void allocate(AttributeStat stat) {
        if (this.minecraft.player != null && this.minecraft.getConnection() != null) {
            ClientPlayNetworking.send(new AllocateAttributePayload(stat.id()));
        }
    }

    private void confirmChoice() {
        if (this.minecraft.player != null && this.minecraft.getConnection() != null) {
            ClientPlayNetworking.send(new CharacterChoicePayload(selectedRace.id(), selectedClass.id()));
            this.onClose();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        if (hasCreatedCharacter()) {
            switch (page) {
                case TALENTS -> renderTalentSheet(graphics);
                case PROFESSIONS -> renderProfessionSheet(graphics);
                case ECONOMY -> renderEconomySheet(graphics);
                case MARKET -> renderMarketSheet(graphics);
                case QUESTS -> renderQuestSheet(graphics);
                case RELIC -> renderRelicSheet(graphics);
                case DUNGEON -> renderDungeonSheet(graphics);
                default -> renderCharacterSheet(graphics);
            }
            return;
        }
        int left = this.width / 2 - 205;
        int right = this.width / 2 + 5;
        int top = this.height / 2 - 92;
        graphics.text(this.font, "ELIGE TU RAZA", left + 50, top, 0xFFFFD780, true);
        graphics.text(this.font, "ELIGE TU CLASE", right + 45, top, 0xFFFFD780, true);
        graphics.text(this.font, Component.literal(selectedRace.bonuses()), left, top + 132, 0xFFFFFFFF, true);
        graphics.text(this.font, Component.literal(selectedRace.racialSkill()), left, top + 145, 0xFFB7D8FF, true);
        graphics.text(this.font, Component.literal(selectedClass.specializations()), right, top + 132, 0xFFFFFFFF, true);
        graphics.text(this.font, "La selección se guarda en el servidor y se conserva al morir.",
                this.width / 2 - 150, this.height / 2 + 116, 0xFFAAAAAA, true);
    }

    private void renderProfessionSheet(GuiGraphicsExtractor graphics) {
        ProfessionData data = this.minecraft.player.getAttachedOrCreate(ProfessionData.ATTACHMENT);
        graphics.text(this.font, "PROFESIONES · nivel máximo " + ProfessionData.MAX_LEVEL, 8, 5, 0xFFFFD780, true);
        for (int i = 0; i < Profession.values().length; i++) {
            Profession profession = Profession.values()[i];
            int level = data.level(profession);
            int y = 19 + i * 28;
            String progress = level >= ProfessionData.MAX_LEVEL ? "MAX" : data.progressInLevel(profession) + "/" + ProfessionData.xpToNext(level) + " XP";
            graphics.text(this.font, profession.displayName() + " · Nv. " + level + " · " + progress,
                    8, y, 0xFFFFFFFF, true);
            graphics.text(this.font, profession.activity(), 8, y + 11, 0xFFAAAAAA, true);
        }
    }

    private void renderEconomySheet(GuiGraphicsExtractor graphics) {
        EconomyData wallet = this.minecraft.player.getAttachedOrCreate(EconomyData.ATTACHMENT);
        long copper = wallet.copperBalance();
        long platinum = copper / 1_000_000;
        long gold = (copper / 10_000) % 100;
        long silver = (copper / 100) % 100;
        long remainingCopper = copper % 100;
        int center = this.width / 2;
        graphics.text(this.font, "MONEDERO · Corona Dorada", center - 83, this.height / 2 - 80, 0xFFFFD780, true);
        graphics.text(this.font, platinum + " Platino   " + gold + " Oro", center - 90, this.height / 2 - 48, 0xFFFFFFFF, true);
        graphics.text(this.font, silver + " Plata   " + remainingCopper + " Cobre", center - 88, this.height / 2 - 30, 0xFFFFFFFF, true);
        graphics.text(this.font, "Saldo total: " + copper + " Cobre", center - 75, this.height / 2 + 2, 0xFFB7D8FF, true);
        graphics.text(this.font, "1 Platino = 100 Oro · 1 Oro = 100 Plata · 1 Plata = 100 Cobre",
                center - 153, this.height / 2 + 24, 0xFFAAAAAA, true);
        graphics.text(this.font, "Gana monedas al derrotar mobs hostiles.", center - 118,
                this.height / 2 + 48, 0xFFAAAAAA, true);
    }

    private void renderMarketSheet(GuiGraphicsExtractor graphics) {
        long balance = this.minecraft.player.getAttachedOrCreate(EconomyData.ATTACHMENT).copperBalance();
        graphics.text(this.font, "MERCADO · saldo " + balance + " Cobre", 8, 5, 0xFFFFD780, true);
        int columns = 2;
        int gap = 8;
        int cardWidth = (this.width - 16 - gap) / columns;
        for (int i = 0; i < ShopOffer.all().size(); i++) {
            ShopOffer offer = ShopOffer.all().get(i);
            int column = i % columns;
            int row = i / columns;
            int x = 8 + column * (cardWidth + gap);
            int y = 24 + row * 47;
            graphics.text(this.font, offer.name(), x + 2, y, 0xFFFFFFFF, true);
            graphics.text(this.font, offer.profession().displayName() + " Nv. " + offer.requiredLevel(),
                    x + 2, y + 12, 0xFFAAAAAA, true);
        }
    }

    private void renderQuestSheet(GuiGraphicsExtractor graphics) {
        QuestData data = this.minecraft.player.getAttachedOrCreate(QuestData.ATTACHMENT);
        graphics.text(this.font, "CONTRATOS DE CAZA · una misión activa a la vez", 8, 5, 0xFFFFD780, true);
        for (int i = 0; i < QuestContract.all().size(); i++) {
            QuestContract contract = QuestContract.all().get(i);
            int y = 48 + i * 55;
            boolean active = contract.id().equals(data.activeQuestId());
            graphics.text(this.font, contract.name(), 8, y, 0xFFFFFFFF, true);
            String objective = active ? contract.targetName() + " " + data.progress() + "/" + contract.requiredKills()
                    : contract.description() + " · " + contract.requiredKills() + " " + contract.targetName();
            graphics.text(this.font, objective, 8, y + 13, 0xFFBBBBBB, true);
            graphics.text(this.font, "Recompensa: " + contract.rewardXp() + " XP · " + contract.rewardCopper() + " Cobre",
                    8, y + 27, 0xFFB7D8FF, true);
        }
    }

    private void renderRelicSheet(GuiGraphicsExtractor graphics) {
        CharacterData.Profile profile = CharacterData.get(this.minecraft.player);
        PlayerClass playerClass = PlayerClass.fromId(profile.classId());
        RelicData data = this.minecraft.player.getAttachedOrCreate(RelicData.ATTACHMENT);
        graphics.text(this.font, "RELIQUIA DE CLASE · " + playerClass.displayName(), 8, 5, 0xFFFFD780, true);
        graphics.text(this.font, "Empúñala y pulsa R para canalizar su habilidad. Fusiona armas para revelar formas.",
                8, 19, 0xFFAAAAAA, true);
        var forms = RelicForm.forClass(playerClass);
        for (int i = 0; i < forms.size(); i++) {
            RelicForm form = forms.get(i);
            int y = 45 + i * 69;
            boolean unlocked = data.owns(form.id());
            String marker = unlocked ? "✦ " : "◇ ";
            graphics.text(this.font, marker + form.name() + (form.id().equals(data.activeFormId()) ? " · ACTIVA" : ""),
                    8, y, unlocked ? 0xFFFFFFFF : 0xFF999999, true);
            String requirement = form.isBaseForm() ? "Forma inicial de tu clase"
                    : "Fusiona: " + form.fusionItemName() + " · Nivel " + form.requiredLevel();
            graphics.text(this.font, requirement, 8, y + 13, 0xFFAAAAAA, true);
            if (!form.isBaseForm()) {
                TalentNode talent = TalentNode.fromId(form.requiredTalentId());
                graphics.text(this.font, "Talento requerido: " + (talent == null ? "?" : talent.displayName()),
                        8, y + 26, 0xFFB7D8FF, true);
            }
            graphics.text(this.font, "Habilidad: " + form.skillName() + " · Poder " + Math.round(form.skillPower() * 100) + "%",
                    8, y + 39, 0xFFFFD780, true);
        }
    }

    private void renderDungeonSheet(GuiGraphicsExtractor graphics) {
        DungeonData data = this.minecraft.player.getAttachedOrCreate(DungeonData.ATTACHMENT);
        int center = this.width / 2;
        graphics.text(this.font, "RUINA DEL UMBRAL", center - 57, this.height / 2 - 79, 0xFFFFD780, true);
        graphics.text(this.font, "Un laberinto de piedra distinto en cada expedición.", center - 131,
                this.height / 2 - 55, 0xFFBBBBBB, true);
        if (data.active()) {
            graphics.text(this.font, "Expedición activa · entrada " + data.x() + " / " + data.y() + " / " + data.z(),
                    center - 153, this.height / 2 - 22, 0xFFFFFFFF, true);
            String status = data.bossDefeated()
                    ? "Varkh cayó. Habla con Iriel para cobrar el sello."
                    : "Busca a Iriel en la entrada; Varkh espera en la cámara oriental.";
            graphics.text(this.font, status, center - 151, this.height / 2, 0xFFB7D8FF, true);
        } else if (this.minecraft.level != null && this.minecraft.level.getGameTime() < data.availableAt()) {
            long ticks = data.availableAt() - this.minecraft.level.getGameTime();
            graphics.text(this.font, "La ruina se estabiliza en " + ((ticks + 19) / 20) + " segundos.",
                    center - 92, this.height / 2 - 12, 0xFFAAAAAA, true);
        } else {
            graphics.text(this.font, "Explora el trazado, escucha el aviso de Varkh y regresa con Iriel.",
                    center - 143, this.height / 2 - 12, 0xFFAAAAAA, true);
        }
        graphics.text(this.font, "Recompensa: 1200 XP · 3000 Cobre · Sello del Guardián",
                center - 139, this.height / 2 + 18, 0xFFFFD780, true);
    }

    private void renderTalentSheet(GuiGraphicsExtractor graphics) {
        CharacterData.Profile profile = CharacterData.get(this.minecraft.player);
        PlayerClass playerClass = PlayerClass.fromId(profile.classId());
        TalentData data = TalentService.get(this.minecraft.player);
        graphics.text(this.font, "ÁRBOL DE TALENTOS · " + playerClass.displayName(), 8, 5, 0xFFFFD780, true);
        graphics.text(this.font, "Puntos: " + data.talentPoints() + "    Ganas 1 punto cada 5 niveles", 8, 42, 0xFFB7D8FF, true);
        var nodes = TalentNode.forBranch(selectedBranch);
        for (int i = 0; i < nodes.size(); i++) {
            TalentNode node = nodes.get(i);
            int y = 56 + i * 50;
            int rank = data.rank(node);
            graphics.text(this.font, node.displayName() + "  " + rank + "/" + TalentNode.MAX_RANK,
                    8, y, 0xFFFFFFFF, true);
            graphics.text(this.font, node.description(), 8, y + 14, 0xFFBBBBBB, true);
            String requirement = "Nivel " + node.requiredLevel() + " · rangos aplican a clase";
            graphics.text(this.font, requirement, 8, y + 28, 0xFF888888, true);
        }
    }

    private void renderCharacterSheet(GuiGraphicsExtractor graphics) {
        CharacterData.Profile profile = CharacterData.get(this.minecraft.player);
        ProgressionData.Progression progression = ProgressionData.get(this.minecraft.player);
        Race race = Race.fromId(profile.raceId());
        PlayerClass playerClass = PlayerClass.fromId(profile.classId());
        int center = this.width / 2;
        int top = this.height / 2 - 120;
        String raceName = race == null ? "?" : race.displayName();
        String className = playerClass == null ? "?" : playerClass.displayName();

        graphics.text(this.font, "PERSONAJE", center - 34, top, 0xFFFFD780, true);
        graphics.text(this.font, raceName + " · " + className, center - 90, top + 17, 0xFFFFFFFF, true);
        graphics.text(this.font, "Nivel " + progression.level() + " / " + ProgressionData.Progression.MAX_LEVEL
                        + "     XP " + progression.xp() + " / " + ProgressionData.requiredXp(progression.level()),
                center - 120, top + 33, 0xFFB7D8FF, true);
        Specialization savedSpec = Specialization.fromId(profile.specializationId());
        if (progression.level() < 10) {
            graphics.text(this.font, "Especialización disponible al nivel 10", center - 96, top + 53, 0xFFAAAAAA, true);
        } else if (savedSpec != null) {
            graphics.text(this.font, "Especialización: " + savedSpec.displayName(), center - 90, top + 53, 0xFFB7D8FF, true);
        } else {
            graphics.text(this.font, "Elige: " + selectedSpecialization.displayName(), center - 50, top + 51, 0xFFFFD780, true);
        }
        graphics.text(this.font, "Puntos de atributo: " + progression.attributePoints(),
                center - 90, top + 91, 0xFFFFD780, true);
        ActiveSkill activeSkill = ActiveSkill.forClass(playerClass);
        if (activeSkill != null) {
            graphics.text(this.font, "Habilidad [R]: " + activeSkill.displayName(), center - 85, top + 103, 0xFFB7D8FF, true);
        }

        for (int i = 0; i < AttributeStat.values().length; i++) {
            AttributeStat stat = AttributeStat.values()[i];
            int y = top + 116 + i * 16;
            int base = progression.value(stat);
            int racial = race == null ? 0 : race.attributeBonus(stat);
            int effective = PlayerAttributeService.effectiveValue(this.minecraft.player, stat);
            graphics.text(this.font, stat.displayName() + ": " + base + " + " + racial + " raza = " + effective,
                    center - 126, y, 0xFFFFFFFF, true);
        }
        graphics.text(this.font, "R usa tu habilidad de clase · Nivel otorga 2 puntos de atributo",
                center - 145, top + 204, 0xFFAAAAAA, true);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
