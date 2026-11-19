package dev.turtywurty.industria.block;

import dev.turtywurty.industria.mechanical.MechanicalNetworkManager;
import dev.turtywurty.industria.mechanical.MechanicalNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Set;

public class ShaftBlock extends RotatedPillarBlock implements MechanicalNode {
    private static final VoxelShape X_SHAPE = box(0, 6, 6, 16, 10, 10);
    private static final VoxelShape Y_SHAPE = box(6, 0, 6, 10, 16, 10);
    private static final VoxelShape Z_SHAPE = box(6, 6, 0, 10, 10, 16);

    private static final Set<Direction> X_PORTS = Set.of(Direction.EAST, Direction.WEST);
    private static final Set<Direction> Y_PORTS = Set.of(Direction.UP, Direction.DOWN);
    private static final Set<Direction> Z_PORTS = Set.of(Direction.NORTH, Direction.SOUTH);

    public ShaftBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Set<Direction> getMechanicalPorts(BlockState state) {
        return switch (state.getValue(AXIS)) {
            case X -> X_PORTS;
            case Y -> Y_PORTS;
            case Z -> Z_PORTS;
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(AXIS)) {
            case X -> X_SHAPE;
            case Y -> Y_SHAPE;
            case Z -> Z_SHAPE;
        };
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel) {
            MechanicalNetworkManager.getOrCreate(serverLevel).markDirty(pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(
        BlockState state,
        ServerLevel level,
        BlockPos pos,
        boolean movedByPiston
    ) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        MechanicalNetworkManager.getOrCreate(level).markDirty(pos);
    }
}
