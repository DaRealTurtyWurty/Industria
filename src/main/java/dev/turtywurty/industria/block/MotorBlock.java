package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MotorBlock extends IndustriaBlock {
    private static final VoxelShape VOXEL_SHAPE = Shapes.or(
            Shapes.box(0.25, 0, 0.25, 0.75, 0.4375, 0.75),
            Shapes.box(0.0625, 0.1875, 0.4375, 0.25, 0.3125, 0.5625)
    ).optimize();

    public MotorBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasBlockEntityRenderer()
                .useRotatedShapes(VOXEL_SHAPE)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.MOTOR)
                        .shouldTick()
                        .rightClickToOpenGui()));
    }
}
