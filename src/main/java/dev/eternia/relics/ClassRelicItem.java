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
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import dev.eternia.mana.ManaService;
import dev.eternia.attributes.AttributeStat;
import dev.eternia.progression.PlayerAttributeService;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class ClassRelicItem extends BowItem {
    private static final ResourceKey<Item> KEY = ResourceKey.create(Registries.ITEM, EterniaMod.id("class_relic"));
    public static final ClassRelicItem ITEM = (ClassRelicItem) Registry.register(
            BuiltInRegistries.ITEM, KEY, new ClassRelicItem(new Item.Properties().setId(KEY).stacksTo(1)));

    private ClassRelicItem(Properties properties) { super(properties); }

    public static void register() { }

    @Override
    public boolean canFitInsideContainerItems() { return false; }

    @Override
    public InteractionResult use(net.minecraft.world.level.Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isBoundTo(stack, player) || !"archer".equals(dev.eternia.core.CharacterData.get(player).classId())) {
            return InteractionResult.FAIL;
        }
        if (!player.getProjectile(stack).isEmpty() || player.hasInfiniteMaterials()) return super.use(level, player, hand);
        if (ManaService.current(player) < 9) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.literal("Sin flechas: necesitas al menos 9 de maná para crear una Flecha Astral."));
            }
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, net.minecraft.world.level.Level level,
                                LivingEntity entity, int timeLeft) {
        if (!(entity instanceof ServerPlayer player) || !isBoundTo(stack, player)
                || !"archer".equals(dev.eternia.core.CharacterData.get(player).classId())) return false;
        if (!player.getProjectile(stack).isEmpty()) return super.releaseUsing(stack, level, entity, timeLeft);

        int drawnTicks = getUseDuration(stack, entity) - timeLeft;
        float pull = BowItem.getPowerForTime(drawnTicks);
        if (pull < 0.1F || !(level instanceof ServerLevel serverLevel)) return false;
        int manaCost = player.hasInfiniteMaterials() ? 0 : 8 + (int) Math.ceil(pull * 8.0F);
        if (manaCost > 0 && !ManaService.spend(player, manaCost)) return false;

        ItemStack visualArrow = new ItemStack(Items.ARROW);
        Arrow arrow = new Arrow(serverLevel, player, visualArrow, stack);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        arrow.setCritArrow(pull >= 1.0F);
        double agility = PlayerAttributeService.effectiveValue(player, AttributeStat.AGILITY);
        arrow.setBaseDamage(2.0 + agility * 0.12 + pull * 3.0);
        RelicForm form = RelicService.activeForm(player);
        if (form != null && form.id().equals("archer_starfall")) {
            arrow.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
        } else if (form != null && form.id().equals("archer_tempest")) {
            arrow.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0));
        }
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, pull * 3.0F, 0.5F);
        serverLevel.addFreshEntity(arrow);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                player.getX(), player.getEyeY(), player.getZ(), 10, 0.12, 0.12, 0.12, 0.03);
        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
        return true;
    }

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
