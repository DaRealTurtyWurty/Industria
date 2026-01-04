package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// TODO: Update voxel shape
public class CentrifugalConcentratorBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = Shapes.block();

    public CentrifugalConcentratorBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasComparatorOutput()
                .hasBlockEntityRenderer()
                .useRotatedShapes(VOXEL_SHAPE)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.CENTRIFUGAL_CONCENTRATOR)
                        .shouldTickAllowClient(true)
                        .multiblockProperties(MultiblockTypeInit.CENTRIFUGAL_CONCENTRATOR)
                        .build()));
    }
}
