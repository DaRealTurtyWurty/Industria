package dev.turtywurty.industria.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class DrillTubeBlock extends Block {
    private static final VoxelShape SHAPE = VoxelShapes.union(
            VoxelShapes.cuboid(0, 0, 0, 0.375, 1, 1),
            VoxelShapes.cuboid(0.625, 0, 0, 1, 1, 1),
            VoxelShapes.cuboid(0.375, 0, 0, 0.625, 1, 0.375),
            VoxelShapes.cuboid(0.375, 0, 0.625, 0.625, 1, 1)
    ).simplify();

    public DrillTubeBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
}
