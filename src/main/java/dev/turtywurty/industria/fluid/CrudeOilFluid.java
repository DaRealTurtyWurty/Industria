package dev.turtywurty.industria.fluid;

import dev.turtywurty.industria.init.FluidInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class CrudeOilFluid extends FlowableFluid {
    @Override
    public Fluid getFlowing() {
        return FluidInit.CRUDE_OIL_FLOWING;
    }

    @Override
    public Fluid getStill() {
        return FluidInit.CRUDE_OIL;
    }

    @Override
    protected boolean isInfinite(ServerWorld world) {
        return false;
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == getFlowing() || fluid == getStill();
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }

    @Override
    protected int getMaxFlowDistance(WorldView world) {
        return 5;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView world) {
        return 1;
    }

    @Override
    public Item getBucketItem() {
        return FluidInit.CRUDE_OIL_BUCKET;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    public int getTickRate(WorldView world) {
        return 45;
    }

    @Override
    protected float getBlastResistance() {
        return 120f;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return FluidInit.CRUDE_OIL_BLOCK.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(state));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
        super.appendProperties(builder);
        builder.add(LEVEL);
    }

    public static class Flowing extends CrudeOilFluid {
        @Override
        public boolean isStill(FluidState state) {
            return false;
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }
    }

    public static class Still extends CrudeOilFluid {
        @Override
        public boolean isStill(FluidState state) {
            return true;
        }

        @Override
        public int getLevel(FluidState state) {
            return 8;
        }
    }
}
