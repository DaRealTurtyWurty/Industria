package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class CentrifugalConcentratorBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = VoxelShapes.fullCube();

    public CentrifugalConcentratorBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasComparatorOutput()
                .hasBlockEntityRenderer()
                .useRotatedShapes(VOXEL_SHAPE)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.CENTRIFUGAL_CONCENTRATOR)
                        .shouldTick()
                        .multiblockProperties(MultiblockTypeInit.CENTRIFUGAL_CONCENTRATOR)
                        .build()));
    }
}
