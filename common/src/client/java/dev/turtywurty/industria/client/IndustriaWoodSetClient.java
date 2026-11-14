package dev.turtywurty.industria.client;

import dev.turtywurty.industria.init.ModWoodSets;
import dev.turtywurty.turtymultiloader.client.registration.ClientRegistrations;
import dev.turtywurty.turtymultiloader.client.registration.WoodSetClient;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Client registrations associated with Industria's common wood sets. */
public final class IndustriaWoodSetClient {
    private IndustriaWoodSetClient() {
    }

    public static void init() {
        WoodSetClient.register(ModWoodSets.RUBBER);
        ClientRegistrations.registerBlockTintSources(List.of(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return 0x00BB0A;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return BiomeColors.getAverageFoliageColor(level, pos) + 0x00220A;
            }
        }), ModWoodSets.RUBBER.leaves());
    }
}
