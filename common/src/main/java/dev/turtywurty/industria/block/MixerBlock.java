package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MixerBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = createShape();

    public MixerBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasComparatorOutput()
                .hasBlockEntityRenderer()
                .useRotatedShapes(VOXEL_SHAPE)
                .blockEntityProperties(
                        new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.MIXER)
                                .shouldTick()
                ));
    }

    private static VoxelShape createShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(-0.375, 0, -0.375, 1.375, 0.5, 1.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.25, 0.5, 1.25, 1.375, 2.75, 1.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.375, 2.75, -0.375, 1.375, 2.875, 1.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.375, 0.5, 1.25, -0.25, 2.75, 1.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.375, 0.5, -0.375, -0.25, 2.75, -0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.25, 0.5, -0.375, 1.375, 2.75, -0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.125, 2.75, 0.3125, 0, 3.125, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 2.875, 0.375, 0.625, 3, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.375, 0.3125, 0.3125, 1.5, 0.6875, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.28125, 0.5, -0.25, 1.34375, 2.75, 1.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.34375, 0.5, -0.25, -0.28125, 2.75, 1.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.21875, 0.5, -0.375, -0.15625, 2.75, 1.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.28125, 0.5, 1.1875, -0.21875, 2.75, 2.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.25, -0.4375, 1.0625, 0.4375, -0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.25, -0.4375, 1.0625, 0.4375, -0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.125, 0.25, -0.4375, 1.3125, 0.4375, -0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.84375, 0.21875, -0.40625, 1.34375, 0.46875, -0.28125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.46875, 0.15625, 0.46875, 0.21875, 0.84375, 0.53125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.6875, 0.75, 0.4375, -0.625, 0.8125, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.5, 0.1875, 0.4375, -0.4375, 0.25, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.3125, 0.1875, 0.4375, -0.25, 0.25, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.125, 0.1875, 0.4375, -0.0625, 0.25, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.6875, 0.1875, 0.4375, -0.625, 0.25, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.125, 0.375, 0.4375, -0.0625, 0.4375, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.6875, 0.375, 0.4375, -0.625, 0.4375, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.125, 0.5625, 0.4375, -0.0625, 0.625, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.6875, 0.5625, 0.4375, -0.625, 0.625, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.125, 0.75, 0.4375, -0.0625, 0.8125, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.3125, 0.75, 0.4375, -0.25, 0.8125, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.5, 0.75, 0.4375, -0.4375, 0.8125, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.375, 0.5, 0.125, -0.25, 0.875, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.25, 0.5, 0.25, 1.375, 0.75, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.125, 0.25, -0.4375, 1.3125, 0.4375, -0.375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1, 2.75, 0.3125, 1.125, 3.125, 0.6875), BooleanOp.OR);

        return shape.optimize();
    }
}
