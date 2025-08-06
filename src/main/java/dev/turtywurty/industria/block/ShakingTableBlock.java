package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import static net.minecraft.util.shape.VoxelShapes.cuboid;

public class ShakingTableBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = VoxelShapes.union(
            cuboid(-0.625, 1.125, -0.6875, 1.625, 1.25, 2.6875),
            cuboid(1.3125, 0, -2, 1.4375, 0.125, 2.6875),
            cuboid(-0.4375, 0, -2, -0.3125, 0.125, 2.6875),
            cuboid(-0.3125, 0, -0.125, 1.3125, 0.125, -0.0625),
            cuboid(-0.3125, 0, -0.25, 1.3125, 0.125, -0.1875),
            cuboid(1, 0, -0.1875, 1.0625, 0.125, -0.125),
            cuboid(0, 0, -0.1875, 0.0625, 0.125, -0.125),
            cuboid(0.9375, 1, -0.3125, 1, 1.125, 0),
            cuboid(0.0625, 1, -0.3125, 0.125, 1.125, 0),
            cuboid(0.0625, 1, 2, 0.125, 1.125, 2.3125),
            cuboid(0.9375, 1, 2, 1, 1.125, 2.3125),
            cuboid(0, 0, 2.125, 0.0625, 0.125, 2.1875),
            cuboid(1, 0, 2.125, 1.0625, 0.125, 2.1875),
            cuboid(-0.3125, 0, 2.1875, 1.3125, 0.125, 2.25),
            cuboid(-0.3125, 0, 2.0625, 1.3125, 0.125, 2.125),
            cuboid(-0.3125, 0, -1, 1.3125, 0.125, -0.875),
            cuboid(-0.3125, 0, -2, 1.3125, 0.125, -1.875),
            cuboid(-0.3125, 0, -1.5625, 1.3125, 0.125, -1.5),
            cuboid(-0.3125, 0, -1.375, 1.3125, 0.125, -1.3125),
            cuboid(-0.625, 1.25, -0.6875, 1.625, 1.5, -0.625),
            cuboid(-0.625, 1.25, -0.625, -0.5625, 1.5, 2.6875),
            cuboid(-0.5625, 1.25, 2.625, -0.0625, 1.5, 2.6875),
            cuboid(-0.3125, 0.125, -2, 1.3125, 0.4375, -0.875),
            cuboid(0.125, 0.75, -2, 0.875, 1.4375, -0.875),
            cuboid(-0.125, 0.4375, -2, 1.125, 0.75, -0.875),
            cuboid(-0.6875, 1.375, -0.75, -0.3125, 1.625, 1.625),
            cuboid(0.25, 1.125, -0.875, 0.75, 1.25, -0.6875),
            cuboid(-0.125, 0.75, -0.4375, 1.1875, 1.125, 0.125),
            cuboid(-0.125, 0.75, 1.875, 1.1875, 1.125, 2.4375),
            cuboid(0, 0.125, 2.0625, 1.0625, 0.75, 2.25),
            cuboid(0, 0.125, -0.25, 1.0625, 0.75, -0.0625)
    ).simplify();

    public ShakingTableBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasComparatorOutput()
                .hasBlockEntityRenderer()
                .useRotatedShapes(VOXEL_SHAPE)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.SHAKING_TABLE)
                        .shouldTick()
                        .multiblockProperties(MultiblockTypeInit.SHAKING_TABLE)
                        .build()));
    }
}
