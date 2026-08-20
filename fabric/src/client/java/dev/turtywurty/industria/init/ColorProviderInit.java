package dev.turtywurty.industria.init;

import net.minecraft.client.color.block.BlockTintSource;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ColorProviderInit {
    public static void init() {
        BlockColorRegistry.register(List.of(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return 0x00BB0A;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter world, BlockPos pos) {
                return BiomeColors.getAverageFoliageColor(world, pos) + 0x00220A;
            }
        }), WoodSetInit.RUBBER.leaves);
    }
}
