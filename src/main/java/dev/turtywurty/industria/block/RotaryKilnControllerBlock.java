package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class RotaryKilnControllerBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = VoxelShapes.fullCube();

    public RotaryKilnControllerBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasComparatorOutput()
                .hasHorizontalFacing()
                .useRotatedShapes(VOXEL_SHAPE)
                .hasBlockEntityRenderer()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.ROTARY_KILN_CONTROLLER)
                        .shouldTick()
                        .multiblockProperties(MultiblockTypeInit.ROTARY_KILN_CONTROLLER).build()));
    }
}
