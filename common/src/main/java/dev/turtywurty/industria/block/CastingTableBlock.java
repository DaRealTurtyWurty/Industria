package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CastingTableBlock extends IndustriaBlock {
    private static final VoxelShape SHAPE = makeShape();

    public CastingTableBlock(Properties settings) {
        super(settings, new BlockProperties()
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(
                ModBlockEntityTypes.CASTING_TABLE)
                .dropContentsOnBreak()
                .rightClickToOpenGui()
                .shouldTick())
            .hasHorizontalFacing()
            .constantShape(SHAPE));
    }

    private static VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.0625, 0.25, 0.625, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0, 0.75, 0.25, 0.625, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0, 0.75, 0.9375, 0.625, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0, 0.0625, 0.9375, 0.625, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.75, 0, 1, 0.9375, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.9375, 0, 0.9375, 1, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.9375, 0.9375, 0.9375, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.9375, 0, 0.0625, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.9375, 0, 1, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.625, 0.75, 0.25, 0.75, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.625, 0, 0.25, 0.75, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.625, 0, 1, 0.75, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.625, 0.75, 1, 0.75, 1), BooleanOp.OR);
        return shape.optimize();
    }
}
