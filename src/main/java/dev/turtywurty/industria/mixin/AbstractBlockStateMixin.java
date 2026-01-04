package dev.turtywurty.industria.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class AbstractBlockStateMixin {
    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase;asState()Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState onConstruct(BlockState original, @Local(argsOnly = true) Block block, @Local BlockBehaviour.Properties settings) {
        if(block instanceof IndustriaBlock) {
            if(original.values.containsKey(BlockStateProperties.LIT))
                return original;

            Reference2ObjectArrayMap<Property<?>, Comparable<?>> clone = original.values.clone();
            clone.put(BlockStateProperties.LIT, false);
            return new BlockState(block, clone, null);
        }

        return original;
    }
}
