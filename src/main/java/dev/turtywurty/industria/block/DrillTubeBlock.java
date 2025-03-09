package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class DrillTubeBlock extends IndustriaBlock {
    private static final VoxelShape VOXEL_SHAPE = VoxelShapes.union(
            VoxelShapes.cuboid(0, 0, 0, 0.375, 1, 1),
            VoxelShapes.cuboid(0.625, 0, 0, 1, 1, 1),
            VoxelShapes.cuboid(0.375, 0, 0, 0.625, 1, 0.375),
            VoxelShapes.cuboid(0.375, 0, 0.625, 0.625, 1, 1)
    ).simplify();

    public DrillTubeBlock(Settings settings) {
        super(settings, new BlockProperties().constantShape(VOXEL_SHAPE));
    }
}
