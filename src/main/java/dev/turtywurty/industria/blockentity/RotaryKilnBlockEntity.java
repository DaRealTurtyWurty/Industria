package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockable;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import dev.turtywurty.industria.multiblock.old.Multiblockable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RotaryKilnBlockEntity extends IndustriaBlockEntity implements AutoMultiblockable {
    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    public RotaryKilnBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.ROTARY_KILN, BlockEntityTypeInit.ROTARY_KILN, pos, state);
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.ROTARY_KILN;
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        Multiblockable.write(this, view);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        Multiblockable.read(this, view);
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        if (this.level == null || this.level.isClientSide())
            return List.of();

        // 5x5x1 structure
        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();

        int widthRange = 2;  // -2 to 2 = 5 blocks
        int heightRange = 4; // 0 to 4 = 5 blocks
        int depthRange = 0;  // 0 only = 1 block

        if (facing == null)
            throw new NullPointerException("Unexpected facing direction: null");

        // Define axis-aligned directions based on facing
        Direction right, forward;
        switch (facing) {
            case NORTH -> {
                right = Direction.EAST;
                forward = Direction.NORTH;
            }
            case SOUTH -> {
                right = Direction.WEST;
                forward = Direction.SOUTH;
            }
            case EAST -> {
                right = Direction.SOUTH;
                forward = Direction.EAST;
            }
            case WEST -> {
                right = Direction.NORTH;
                forward = Direction.WEST;
            }
            default -> throw new IllegalStateException("Unexpected facing direction: " + facing);
        }

        // The main loop structure is the same for all directions
        for (int w = -widthRange; w <= widthRange; w++) {
            for (int h = 0; h <= heightRange; h++) {
                for (int d = 0; d <= depthRange; d++) {
                    if (w == 0 && h == 0 && d == 0)
                        continue;

                    BlockPos pos = this.worldPosition
                            .relative(right, w)
                            .relative(Direction.UP, h)
                            .relative(forward, d);

                    if (this.level.getBlockState(pos).canBeReplaced()) {
                        positions.add(pos);
                    } else {
                        invalidPositions.add(pos);
                    }
                }
            }
        }

        return invalidPositions.isEmpty() ? positions : List.of();
    }

    @Override
    public List<BlockPos> getMultiblockPositions() {
        return this.multiblockPositions;
    }

    @Override
    public void onMultiblockBreak(Level world, BlockPos pos) {
        AutoMultiblockable.super.onMultiblockBreak(world, pos);

        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos offsetPos = pos.relative(facing);
        BlockState offsetState = world.getBlockState(offsetPos);
        if (offsetState.is(BlockInit.ROTARY_KILN)) {
            world.setBlockAndUpdate(offsetPos, BlockInit.ROTARY_KILN_CONTROLLER.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
        }
    }
}