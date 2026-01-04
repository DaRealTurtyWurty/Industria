package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DrillTubeBlock extends IndustriaBlock {
    private static final VoxelShape VOXEL_SHAPE = Shapes.or(
            Shapes.box(0, 0, 0, 0.375, 1, 1),
            Shapes.box(0.625, 0, 0, 1, 1, 1),
            Shapes.box(0.375, 0, 0, 0.625, 1, 0.375),
            Shapes.box(0.375, 0, 0.625, 0.625, 1, 1)
    ).optimize();

    public DrillTubeBlock(Properties settings) {
        super(settings, new BlockProperties().constantShape(VOXEL_SHAPE));
    }
}
