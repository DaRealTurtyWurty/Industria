package dev.turtywurty.industria.util;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public final class FluidRenderUtils {
    private FluidRenderUtils() {
    }

    public static @Nullable GuiFluidRenderData getRenderData(Fluid fluid, @Nullable Level level, @Nullable BlockPos pos) {
        if (fluid == null || fluid == Fluids.EMPTY)
            return null;

        FluidState fluidState = fluid.defaultFluidState();
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState);
        BlockState blockState = fluidState.createLegacyBlock();
        int tintColor = getTintColor(model.tintSource(), blockState, level, pos);
        return new GuiFluidRenderData(model.stillMaterial().sprite(), tintColor);
    }

    public static @Nullable GuiFluidRenderData getRenderData(@Nullable FluidVariant fluidVariant, @Nullable Level level, @Nullable BlockPos pos) {
        if (fluidVariant == null || fluidVariant.isBlank())
            return null;

        return getRenderData(fluidVariant.getFluid(), level, pos);
    }

    private static int getTintColor(@Nullable BlockTintSource tintSource, BlockState blockState, @Nullable Level level, @Nullable BlockPos pos) {
        if (tintSource == null)
            return 0xFFFFFFFF;

        int tintColor;
        if (level instanceof BlockAndTintGetter blockAndTintGetter && pos != null) {
            tintColor = tintSource.colorInWorld(blockState, blockAndTintGetter, pos);
        } else {
            tintColor = tintSource.color(blockState);
        }

        return 0xFF000000 | (tintColor & 0xFFFFFF);
    }

    public record GuiFluidRenderData(TextureAtlasSprite stillSprite, int tintColor) {
    }
}
