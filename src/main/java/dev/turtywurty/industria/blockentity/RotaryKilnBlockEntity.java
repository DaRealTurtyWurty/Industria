package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RotaryKilnBlockEntity extends UpdatableBlockEntity implements Multiblockable {
    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    public RotaryKilnBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.ROTARY_KILN, pos, state);
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.ROTARY_KILN;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        nbt.put("MachinePositions", Multiblockable.writeMultiblockToNbt(this));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        Multiblockable.readMultiblockFromNbt(this, nbt.getListOrEmpty("MachinePositions"));
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        if(this.world == null || this.world.isClient)
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
                    if(w == 0 && h == 0 && d == 0)
                        continue;

                    BlockPos pos = this.pos
                            .offset(right, w)
                            .offset(Direction.UP, h)
                            .offset(forward, d);

                    if (this.world.getBlockState(pos).isReplaceable()) {
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
    public void breakMultiblock(World world, BlockPos pos) {
        Multiblockable.super.breakMultiblock(world, pos);

        Direction facing = getCachedState().get(Properties.HORIZONTAL_FACING);
        BlockPos offsetPos = pos.offset(facing);
        BlockState offsetState = world.getBlockState(offsetPos);
        if(offsetState.isOf(BlockInit.ROTARY_KILN)) {
            world.setBlockState(offsetPos, BlockInit.ROTARY_KILN_CONTROLLER.getDefaultState().with(Properties.HORIZONTAL_FACING, facing));
        }
    }
}
