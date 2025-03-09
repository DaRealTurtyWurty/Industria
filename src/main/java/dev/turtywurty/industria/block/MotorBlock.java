package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class MotorBlock extends IndustriaBlock {
    private static final VoxelShape VOXEL_SHAPE = VoxelShapes.union(
            VoxelShapes.cuboid(0.25, 0, 0.25, 0.75, 0.4375, 0.75),
            VoxelShapes.cuboid(0.0625, 0.1875, 0.4375, 0.25, 0.3125, 0.5625)
    ).simplify();

    public MotorBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasBlockEntityRenderer()
                .useRotatedShapes(VOXEL_SHAPE)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.MOTOR)
                        .shouldTick()
                        .rightClickToOpenGui()));
    }
}
