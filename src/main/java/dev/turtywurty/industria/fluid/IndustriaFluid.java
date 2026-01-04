package dev.turtywurty.industria.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.function.Supplier;

public abstract class IndustriaFluid extends FlowingFluid {
    private final Supplier<Fluid> stillSupplier, flowingSupplier;
    private final Supplier<Item> bucketSupplier;
    private final Supplier<Block> blockSupplier;

    public IndustriaFluid(Supplier<Fluid> stillSupplier, Supplier<Fluid> flowingSupplier, Supplier<Item> bucketSupplier, Supplier<Block> blockSupplier) {
        this.stillSupplier = stillSupplier;
        this.flowingSupplier = flowingSupplier;
        this.bucketSupplier = bucketSupplier;
        this.blockSupplier = blockSupplier;
    }

    @Override
    public Fluid getFlowing() {
        return this.flowingSupplier.get();
    }

    @Override
    public Fluid getSource() {
        return this.stillSupplier.get();
    }

    @Override
    protected boolean canConvertToSource(ServerLevel world) {
        return false;
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == getFlowing() || fluid == getSource();
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropResources(state, world, pos, blockEntity);
    }

    @Override
    protected int getSlopeFindDistance(LevelReader world) {
        return 5;
    }

    @Override
    protected int getDropOff(LevelReader world) {
        return 1;
    }

    @Override
    public Item getBucket() {
        return this.bucketSupplier.get();
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter world, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    public int getTickDelay(LevelReader world) {
        return 45;
    }

    @Override
    protected float getExplosionResistance() {
        return 120f;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return this.blockSupplier.get().defaultBlockState().setValue(BlockStateProperties.LEVEL, getLegacyLevel(state));
    }

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        super.createFluidStateDefinition(builder);
        builder.add(LEVEL);
    }

    public static class Flowing extends IndustriaFluid {
        public Flowing(Supplier<Fluid> stillSupplier, Supplier<Fluid> flowingSupplier, Supplier<Item> bucketSupplier, Supplier<Block> blockSupplier) {
            super(stillSupplier, flowingSupplier, bucketSupplier, blockSupplier);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }
    }

    public static class Still extends IndustriaFluid {
        public Still(Supplier<Fluid> stillSupplier, Supplier<Fluid> flowingSupplier, Supplier<Item> bucketSupplier, Supplier<Block> blockSupplier) {
            super(stillSupplier, flowingSupplier, bucketSupplier, blockSupplier);
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }
    }
}
