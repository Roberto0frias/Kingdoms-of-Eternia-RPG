package dev.eternia.relics;

import dev.eternia.classes.PlayerClass;
import dev.eternia.core.CharacterData;
import dev.eternia.progression.ProgressionData;
import dev.eternia.talents.TalentData;
import dev.eternia.talents.TalentNode;
import dev.eternia.talents.TalentService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class RelicService {
    private RelicService() { }

    public static RelicData get(ServerPlayer player) { return player.getAttachedOrCreate(RelicData.ATTACHMENT); }

    public static void initialize(ServerPlayer player) {
        CharacterData.Profile profile = CharacterData.get(player);
        PlayerClass playerClass = PlayerClass.fromId(profile.classId());
        RelicForm base = RelicForm.baseFor(playerClass);
        if (base == null) return;
        RelicData data = get(player);
        String owner = player.getUUID().toString();
        if (!owner.equals(data.ownerId()) || !playerClass.id().equals(data.classId())) {
            data = new RelicData(owner, playerClass.id(), base.id(), java.util.List.of(base.id()));
            player.setAttached(RelicData.ATTACHMENT, data);
        } else if (!data.owns(base.id())) {
            data = data.unlock(base.id());
            player.setAttached(RelicData.ATTACHMENT, data);
        }
        ensureInventoryItem(player, data);
    }

    public static void ensureInventoryItem(ServerPlayer player, RelicData data) {
        if (!CharacterData.get(player).isCreated()) return;
        int keptSlot = -1;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(ClassRelicItem.ITEM)) continue;
            if (keptSlot < 0 && ClassRelicItem.isBoundTo(stack, player)) keptSlot = slot;
            else inventory.setItem(slot, ItemStack.EMPTY);
        }
        if (keptSlot < 0) {
            RelicForm form = activeForm(data);
            if (form == null) form = RelicForm.baseFor(PlayerClass.fromId(data.classId()));
            if (form == null) return;
            ItemStack relic = ClassRelicItem.createBound(player, data.classId(), form);
            if (!inventory.add(relic)) {
                if (player.level().getGameTime() % 200L == 0L) {
                    player.sendSystemMessage(Component.literal("Libera un espacio para recibir tu Reliquia de clase."));
                }
            } else {
                player.sendSystemMessage(Component.literal("Has recibido tu Reliquia de clase: " + form.name() + "."));
            }
        } else {
            ClassRelicItem.applyForm(inventory.getItem(keptSlot), activeForm(data));
        }
    }

    public static RelicForm activeForm(ServerPlayer player) { return activeForm(get(player)); }
    public static RelicForm activeForm(RelicData data) {
        RelicForm form = RelicForm.find(data.activeFormId());
        return form != null && data.owns(form.id()) ? form : null;
    }

    public static boolean isEquipped(ServerPlayer player) {
        return ClassRelicItem.isBoundTo(player.getMainHandItem(), player)
                || ClassRelicItem.isBoundTo(player.getOffhandItem(), player);
    }

    public static void select(ServerPlayer player, String formId) {
        if (!CharacterData.get(player).isCreated()) return;
        RelicForm form = RelicForm.find(formId);
        RelicData data = get(player);
        if (form == null || !form.playerClass().id().equals(CharacterData.get(player).classId()) || !data.owns(formId)) {
            player.sendSystemMessage(Component.literal("Esa forma no está desbloqueada para tu clase."));
            return;
        }
        data = data.select(formId);
        player.setAttached(RelicData.ATTACHMENT, data);
        ensureInventoryItem(player, data);
        player.sendSystemMessage(Component.literal("Forma activa: " + form.name() + ". Empúñala para canalizar " + form.skillName() + " con R."));
    }

    public static void fuse(ServerPlayer player, String formId) {
        if (!CharacterData.get(player).isCreated()) return;
        RelicForm form = RelicForm.find(formId);
        PlayerClass playerClass = PlayerClass.fromId(CharacterData.get(player).classId());
        RelicData data = get(player);
        if (form == null || form.isBaseForm() || form.playerClass() != playerClass || data.owns(formId)) {
            player.sendSystemMessage(Component.literal("Forma de fusión inválida o ya desbloqueada."));
            return;
        }
        if (ProgressionData.get(player).level() < form.requiredLevel()) {
            player.sendSystemMessage(Component.literal("Requiere nivel de personaje " + form.requiredLevel() + "."));
            return;
        }
        TalentNode requiredTalent = TalentNode.fromId(form.requiredTalentId());
        if (requiredTalent == null || TalentService.get(player).rank(requiredTalent) < 1) {
            player.sendSystemMessage(Component.literal("Desbloquea primero el talento "
                    + (requiredTalent == null ? "requerido" : requiredTalent.displayName()) + " en tu árbol."));
            return;
        }
        if (!ClassRelicItem.isBoundTo(findRelic(player), player)) {
            player.sendSystemMessage(Component.literal("Tu Reliquia debe estar en el inventario para fusionar."));
            return;
        }
        Item ingredient = BuiltInRegistries.ITEM.get(Identifier.parse(form.fusionItemId()))
                .map(reference -> reference.value()).orElse(null);
        if (ingredient == null) return;
        int ingredientSlot = findIngredient(player.getInventory(), ingredient);
        if (ingredientSlot < 0) {
            player.sendSystemMessage(Component.literal("Necesitas " + form.fusionItemName() + " para fusionar esta forma."));
            return;
        }
        player.getInventory().removeItem(ingredientSlot, 1);
        data = data.unlock(form.id());
        player.setAttached(RelicData.ATTACHMENT, data);
        ensureInventoryItem(player, data);
        player.sendSystemMessage(Component.literal("¡Fusión completada! Forma desbloqueada: " + form.name() + "."));
    }

    private static ItemStack findRelic(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (ClassRelicItem.isBoundTo(stack, player)) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static int findIngredient(Inventory inventory, Item item) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) return slot;
        }
        return -1;
    }

    public static void register() { }
}
