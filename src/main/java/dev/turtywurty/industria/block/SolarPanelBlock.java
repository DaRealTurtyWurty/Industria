package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class SolarPanelBlock extends IndustriaBlock {
    private static final VoxelShape VOXEL_SHAPE = createShape();

    public SolarPanelBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .useRotatedShapes(VOXEL_SHAPE)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.SOLAR_PANEL)
                        .shouldTick()
                        .rightClickToOpenGui()));
    }

    private static VoxelShape createShape() {
        var shape = VoxelShapes.empty();
        shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.125, 0, 0.1875, 0.875, 0.375, 0.8125), BooleanBiFunction.OR);
        shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.25, 0.375, 0.3125, 0.75, 0.6875, 0.6875), BooleanBiFunction.OR);
        shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.0625, 0.625, 0.0625, 0.9375, 0.8125, 0.25), BooleanBiFunction.OR);
        shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.0625, 0.6875, 0.25, 0.9375, 0.875, 0.4375), BooleanBiFunction.OR);
        shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.0625, 0.75, 0.4375, 0.9375, 0.9375, 0.625), BooleanBiFunction.OR);
        shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.0625, 0.8125, 0.625, 0.9375, 1, 0.8125), BooleanBiFunction.OR);
        shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.25, 0.6875, 0.4375, 0.75, 0.75, 0.6875), BooleanBiFunction.OR);
        shape = VoxelShapes.combineAndSimplify(shape, VoxelShapes.cuboid(0.25, 0.75, 0.625, 0.75, 0.8125, 0.6875), BooleanBiFunction.OR);

        return shape.simplify();
    }
}
