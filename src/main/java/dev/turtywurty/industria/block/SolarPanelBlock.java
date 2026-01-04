package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SolarPanelBlock extends IndustriaBlock {
    private static final VoxelShape VOXEL_SHAPE = createShape();

    public SolarPanelBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .useRotatedShapes(VOXEL_SHAPE)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.SOLAR_PANEL)
                        .shouldTick()
                        .rightClickToOpenGui()));
    }

    private static VoxelShape createShape() {
        var shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0.1875, 0.875, 0.375, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.375, 0.3125, 0.75, 0.6875, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.625, 0.0625, 0.9375, 0.8125, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.6875, 0.25, 0.9375, 0.875, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.75, 0.4375, 0.9375, 0.9375, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.8125, 0.625, 0.9375, 1, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.6875, 0.4375, 0.75, 0.75, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.75, 0.625, 0.75, 0.8125, 0.6875), BooleanOp.OR);

        return shape.optimize();
    }
}
