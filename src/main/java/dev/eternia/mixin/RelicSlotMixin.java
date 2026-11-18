package dev.eternia.mixin;

import dev.eternia.relics.ClassRelicItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class RelicSlotMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void eternia$keepRelicInPlayerInventory(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.is(ClassRelicItem.ITEM)) return;
        Slot slot = (Slot) (Object) this;
        if (!(slot.container instanceof Inventory)) cir.setReturnValue(false);
    }
}
