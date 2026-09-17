package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DigesterBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = createShape();

    public DigesterBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasComparatorOutput()
                .hasHorizontalFacing()
                .useRotatedShapes(VOXEL_SHAPE)
                .hasBlockEntityRenderer()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.DIGESTER)
                        .shouldTick()
                        .rightClickToOpenGui()
                        .dropContentsOnBreak()
                ));
    }

    private static VoxelShape createShape() {
        VoxelShape body = octagonalPrism(40, 24, 0, 36);
        VoxelShape lid = octagonalPrism(28, 16, 36, 44);
        return Shapes.or(body, lid,
                Shapes.box(0.125, 2.6875, 0.125, 0.875, 3.01, 0.875),
                Shapes.box(0.3125, 0.3125, -2.125, 0.6875, 0.6875, -2),
                Shapes.box(0.375, 1.375, 3, 0.625, 1.625, 3.125)).optimize();
    }

    private static VoxelShape octagonalPrism(int radius, int bevel, int bottom, int top) {
        VoxelShape shape = Shapes.empty();
        for (int x = -radius; x < radius; x += 4) {
            int edge = Math.max(Math.abs(x), Math.abs(x + 4));
            int depth = radius - Math.max(0, edge - (radius - bevel));
            shape = Shapes.or(shape, Shapes.box(
                    0.5 + x / 16.0, bottom / 16.0, 0.5 - depth / 16.0,
                    0.5 + (x + 4) / 16.0, top / 16.0, 0.5 + depth / 16.0));
        }
        return shape;
    }
}
