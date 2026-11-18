package dev.eternia.relics;

import dev.eternia.core.EterniaMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ClassRelicItem extends Item {
    private static final ResourceKey<Item> KEY = ResourceKey.create(Registries.ITEM, EterniaMod.id("class_relic"));
    public static final ClassRelicItem ITEM = (ClassRelicItem) Registry.register(
            BuiltInRegistries.ITEM, KEY, new ClassRelicItem(new Item.Properties().setId(KEY).stacksTo(1)));

    private ClassRelicItem(Properties properties) { super(properties); }

    public static void register() { }

    @Override
    public boolean canFitInsideContainerItems() { return false; }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        return slot.container != player.getInventory();
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action,
                                            Player player, SlotAccess access) {
        return slot.container != player.getInventory();
    }

    public static ItemStack createBound(Player player, String classId, RelicForm form) {
        ItemStack stack = new ItemStack(ITEM);
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("owner", player.getUUID().toString());
        tag.putString("class", classId);
        net.minecraft.world.item.component.CustomData.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag);
        applyForm(stack, form);
        return stack;
    }

    public static void applyForm(ItemStack stack, RelicForm form) {
        if (stack.isEmpty() || form == null) return;
        stack.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,
                Identifier.parse(form.modelItemId()));
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(form.name()));
        stack.set(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                net.minecraft.world.item.component.ItemAttributeModifiers.builder()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                                new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                        EterniaMod.id("relic_attack"), form.attackDamage(),
                                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                                net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
                                new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                        EterniaMod.id("relic_speed"), -2.4,
                                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                                net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                        .build());
        net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack,
                tag -> tag.putString("active_form", form.id()));
    }

    public static boolean isBoundTo(ItemStack stack, Player player) {
        if (!stack.is(ITEM)) return false;
        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) return false;
        var tag = customData.copyTag();
        return tag.getStringOr("owner", "").equals(player.getUUID().toString())
                && tag.getStringOr("class", "").equals(dev.eternia.core.CharacterData.get(player).classId());
    }

}
