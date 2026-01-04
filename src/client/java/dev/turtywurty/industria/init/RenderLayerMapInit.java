package dev.turtywurty.industria.init;

import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.client.rendering.v1.ChunkSectionLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class RenderLayerMapInit {
    public static void init() {
        ChunkSectionLayerMap.putFluids(ChunkSectionLayer.TRANSLUCENT,
                FluidInit.CRUDE_OIL.still(), FluidInit.CRUDE_OIL.flowing(),
                FluidInit.DIRTY_SODIUM_ALUMINATE.still(), FluidInit.DIRTY_SODIUM_ALUMINATE.flowing(),
                FluidInit.SODIUM_ALUMINATE.still(), FluidInit.SODIUM_ALUMINATE.flowing());

        ChunkSectionLayerMap.putBlocks(ChunkSectionLayer.TRANSLUCENT,
                BlockInit.CASSITERITE_ORE, BlockInit.DEEPSLATE_CASSITERITE_ORE,
                BlockInit.SPHALERITE_ORE, BlockInit.DEEPSLATE_SPHALERITE_ORE,
                BlockInit.BAUXITE_ORE, BlockInit.DEEPSLATE_BAUXITE_ORE,
                BlockInit.ARGENTITE_ORE, BlockInit.DEEPSLATE_ARGENTITE_ORE,
                BlockInit.GALENA_ORE, BlockInit.DEEPSLATE_GALENA_ORE,
                BlockInit.CASSITERITE_ORE, BlockInit.DEEPSLATE_CASSITERITE_ORE,
                BlockInit.SPHALERITE_ORE, BlockInit.DEEPSLATE_SPHALERITE_ORE,
                BlockInit.COBALTITE_ORE, BlockInit.DEEPSLATE_COBALTITE_ORE,
                BlockInit.PENTLANDITE_ORE, BlockInit.DEEPSLATE_PENTLANDITE_ORE,
                BlockInit.NETHER_PYRITE_ORE, BlockInit.END_PYRITE_ORE,
                BlockInit.QUARTZ_ORE, BlockInit.DEEPSLATE_QUARTZ_ORE,
                BlockInit.ILMENITE_ORE, BlockInit.DEEPSLATE_ILMENITE_ORE,
                BlockInit.IRIDIUM_ORE, BlockInit.DEEPSLATE_IRIDIUM_ORE);

        ChunkSectionLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, BlockInit.FLUID_TANK);

        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            ChunkSectionLayerMap.putBlocks(ChunkSectionLayer.CUTOUT,
                    woodSet.door,
                    woodSet.trapdoor,
                    woodSet.leaves,
                    woodSet.sapling);
        }
    }
}