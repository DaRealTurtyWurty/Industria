package dev.turtywurty.industria.neoforge.mixin;

import com.mojang.blaze3d.vertex.QuadInstance;
import dev.turtywurty.industria.init.ModBlocks;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.ao.EnhancedBlockModelLighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EnhancedBlockModelLighter.class, remap = false)
public abstract class EnhancedBlockModelLighterMixin extends BlockModelLighter {
    @Inject(method = "prepareQuadAmbientOcclusion", at = @At("HEAD"), cancellable = true)
    private void industria$useVanillaControllerLighting(
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        BakedQuad quad,
        QuadInstance outputInstance,
        CallbackInfo ci
    ) {
        if (state.is(ModBlocks.SMELTERY_CONTROLLER.get())) {
            // The tilted panels extend outside the solid cube; use the same AO sampling as Fabric.
            super.prepareQuadAmbientOcclusion(level, state, pos, quad, outputInstance);
            ci.cancel();
        }
    }
}
