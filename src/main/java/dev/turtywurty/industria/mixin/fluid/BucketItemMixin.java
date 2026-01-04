package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BucketItem.class)
public class BucketItemMixin {
    @Shadow @Final private Fluid content;

    @ModifyExpressionValue(method = "emptyContents",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/Fluid;is(Lnet/minecraft/tags/TagKey;)Z"))
    @SuppressWarnings("deprecation") // We're just doing what vanilla does
    private boolean industria$placeFluid(boolean original) {
        if(original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::shouldEvaporateInUltrawarm)
                .anyMatch(fluidData -> this.content.is(fluidData.fluidTag()));
    }
}
