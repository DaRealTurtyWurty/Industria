package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrystallizerBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = Shapes.or(
        Shapes.box(-0.3375, 0, -0.34375, -0.15, 1.0625, -0.15625),
        Shapes.box(1.1625, 0, -0.34375, 1.35, 1.0625, -0.15625),
        Shapes.box(-0.3375, 0, 1.15, -0.15, 1.0625, 1.3375),
        Shapes.box(1.1625, 0, 1.15, 1.35, 1.0625, 1.3375),
        Shapes.box(0, 1.19375, -0.70625, 1, 3.31875, 1.70625),
        Shapes.box(-0.70625, 1.19375, 0, 1.70625, 3.31875, 1),
        Shapes.box(-0.375, 1.19375, -0.375, 1.375, 3.31875, 1.375),
        Shapes.box(-0.0625, 0.56875, -0.0625, 1.0625, 1.19375, 1.0625),
        Shapes.box(0.125, 0.31875, 0.125, 0.875, 0.56875, 0.875),
        Shapes.box(-0.0625, 3.31875, -0.0625, 1.0625, 3.56875, 1.0625),
        Shapes.box(0.25, 3.3125, 0.25, 0.75, 3.9375, 0.75),
        Shapes.box(0.375, 3.875, 0.375, 0.625, 4, 0.625),
        Shapes.box(1.6875, 2.625, 0.3125, 1.8125, 3, 0.6875),
        Shapes.box(0.3125, 2.625, -0.8125, 0.6875, 3, -0.6875),
        Shapes.box(0.375, 0.13125, 0.375, 1.75, 0.44375, 0.625),
        Shapes.box(1.75, 0.06875, 0.3125, 1.875, 0.44375, 0.6875)).optimize();

    public CrystallizerBlock(Properties settings) {
        super(settings, new BlockProperties()
            .hasHorizontalFacing()
            .hasComparatorOutput()
            .hasBlockEntityRenderer()
            .useRotatedShapes(VOXEL_SHAPE)
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.CRYSTALLIZER)
                .dropContentsOnBreak()
                .rightClickToOpenGui()
                .shouldTick()));
    }
}
