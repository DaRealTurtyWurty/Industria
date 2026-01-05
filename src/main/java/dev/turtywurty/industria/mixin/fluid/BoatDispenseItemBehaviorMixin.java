package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BoatDispenseItemBehavior.class)
public class BoatDispenseItemBehaviorMixin {
    @ModifyExpressionValue(method = "execute",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$dispenseSilently(boolean original, BlockSource pointer, ItemStack stack, @Local(name = "level") ServerLevel serverWorld, @Local BlockPos blockPos) {
        if(original)
            return true;

        FluidData data = FluidData.FLUID_DATA.get(serverWorld.getFluidState(blockPos).getType());
        return data != null && data.shouldDispenseBoatsAbove();
    }
}
