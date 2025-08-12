package dev.turtywurty.industria.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {
    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractBlock$AbstractBlockState;asBlockState()Lnet/minecraft/block/BlockState;"))
    private BlockState onConstruct(BlockState original, @Local(argsOnly = true) Block block, @Local AbstractBlock.Settings settings) {
        if (block instanceof IndustriaBlock) {
            if (original.propertyMap.containsKey(Properties.LIT))
                return original;

            Reference2ObjectArrayMap<Property<?>, Comparable<?>> clone = original.propertyMap.clone();
            clone.put(Properties.LIT, false);
            return new BlockState(block, clone, null);
        }

        return original;
    }
}
