package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClarifierBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = Shapes.or(
            Shapes.box(-0.125, 0, -0.9375, 1.125, 1, 2),
            Shapes.box(1, 1, -0.125, 1.125, 1.3125, 1.125),
            Shapes.box(-0.375, 1.3125, -0.3125, 1.375, 1.375, 0),
            Shapes.box(-0.375, 1.3125, 1, 1.375, 1.375, 1.3125),
            Shapes.box(-0.375, 1.3125, 0, 0, 1.375, 1),
            Shapes.box(1, 1.3125, 0, 1.375, 1.375, 1),
            Shapes.box(-0.125, 1, -0.125, 0, 1.3125, 1.125),
            Shapes.box(0, 1, 1, 1, 1.3125, 1.125),
            Shapes.box(0, 1, -0.125, 1, 1.3125, 0),
            Shapes.box(-0.8125, 1.3125, 1.3125, 1.8125, 1.75, 1.75),
            Shapes.box(-0.8125, 1.3125, -0.75, 1.8125, 1.75, -0.3125),
            Shapes.box(1.375, 1.3125, -0.3125, 1.8125, 1.75, 0.125),
            Shapes.box(1.375, 1.3125, -0.3125, 1.8125, 1.75, 1.3125),
            Shapes.box(-0.8125, 1.3125, -0.3125, -0.375, 1.75, 1.3125),
            Shapes.box(-1, 1.75, -0.9375, -0.75, 2, 1.9375),
            Shapes.box(1.75, 1.75, -0.9375, 2, 2, 1.9375),
            Shapes.box(-0.75, 1.75, -0.9375, 1.75, 2, -0.6875),
            Shapes.box(-0.75, 1.75, 1.6875, 1.75, 2, 1.9375)
    ).optimize();

    public ClarifierBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasComparatorOutput()
                .hasBlockEntityRenderer()
                .useRotatedShapes(VOXEL_SHAPE)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.CLARIFIER)
                        .shouldTick()
                        .rightClickToOpenGui()
                        .dropContentsOnBreak()
                        .multiblockProperties(MultiblockTypeInit.CLARIFIER).build()));
    }
}
