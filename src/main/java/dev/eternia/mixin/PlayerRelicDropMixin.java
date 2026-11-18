package dev.eternia.mixin;

import dev.eternia.relics.ClassRelicItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerRelicDropMixin {
    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void eternia$bindClassRelic(ItemStack stack, boolean throwRandomly,
                                       CallbackInfoReturnable<net.minecraft.world.entity.item.ItemEntity> cir) {
        if (!stack.is(ClassRelicItem.ITEM)) return;
        Player player = (Player) (Object) this;
        if (player instanceof ServerPlayer serverPlayer && !serverPlayer.getInventory().contains(stack)
                && serverPlayer.getInventory().getFreeSlot() >= 0) {
            serverPlayer.getInventory().add(stack.copy());
        }
        cir.setReturnValue(null);
    }
}
