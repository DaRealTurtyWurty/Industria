package dev.turtywurty.industria.blockentity;

import com.mojang.serialization.Codec;
import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdateableBlockEntityLike;
import dev.turtywurty.multiblocklib.block.entity.MultiblockControllerBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;

public abstract class IndustriaMultiblockControllerBlockEntity extends MultiblockControllerBlockEntity implements SyncableTickableBlockEntity, UpdateableBlockEntityLike {
    public static final Codec<ResourceKey<Recipe<?>>> RECIPE_CODEC = ResourceKey.codec(Registries.RECIPE);

    protected final IndustriaBlock blockRef;
    protected boolean isDirty;

    protected IndustriaMultiblockControllerBlockEntity(IndustriaBlock blockRef, BlockEntityType<? extends IndustriaMultiblockControllerBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.blockRef = blockRef;
    }

    @Override
    public final void tick() {
        if (this.level == null)
            return;

        if (this.level.isClientSide()) {
            onClientTick();
        } else {
            MultiblockControllerBlockEntity.tick(this.level, this.worldPosition, getBlockState(), this);
        }

        getSyncableStorages().forEach(SyncableStorage::sync);
        endTick();
    }

    @Override
    protected final void tickServer(ServerLevel serverLevel) {
        onTick();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        if (this.level == null || pos == null || oldState == null)
            return;

        BlockState newState = this.level.getBlockState(pos);
        if (!oldState.is(newState.getBlock()) && this.blockRef.dropContentsOnBreak && this instanceof BlockEntityContentsDropper dropper) {
            dropper.dropContents(this.level, pos);
        }
    }

    @Override
    public void update() {
        this.isDirty = true;
        if (!shouldWaitForEndTick()) {
            forceUpdate();
        }
    }

    @Override
    public boolean shouldWaitForEndTick() {
        return true;
    }

    @Override
    public void endTick() {
        if (this.isDirty) {
            forceUpdate();
        }
    }

    @Override
    public void forceUpdate() {
        this.isDirty = false;
        setChanged();

        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    protected BlockPos getLocalOffsetFromController(BlockPos pos) {
        BlockPos offset = pos.subtract(this.worldPosition);
        Direction facing = getControllerFacing();

        return switch (facing) {
            case NORTH -> offset;
            case SOUTH -> new BlockPos(-offset.getX(), offset.getY(), -offset.getZ());
            case WEST -> new BlockPos(offset.getZ(), offset.getY(), -offset.getX());
            case EAST -> new BlockPos(-offset.getZ(), offset.getY(), offset.getX());
            default -> offset;
        };
    }

    protected Direction getControllerFacing() {
        return getBlockState().getOptionalValue(BlockStateProperties.HORIZONTAL_FACING).orElse(Direction.NORTH);
    }

    protected Direction getFrontDirection() {
        return getControllerFacing();
    }

    protected Direction getBackDirection() {
        return getControllerFacing().getOpposite();
    }

    protected Direction getLeftDirection() {
        return getControllerFacing().getCounterClockWise();
    }

    protected Direction getRightDirection() {
        return getControllerFacing().getClockWise();
    }

    @Override
    public Storage<ItemVariant> getItemStorageForExternal(BlockPos pos) {
        return !isFormed() ? null : getItemStorageForExternal(pos, getLocalOffsetFromController(pos));
    }

    @Override
    public Storage<FluidVariant> getFluidStorageForExternal(BlockPos pos) {
        return !isFormed() ? null : getFluidStorageForExternal(pos, getLocalOffsetFromController(pos));
    }

    @Override
    public EnergyStorage getEnergyStorageForExternal(BlockPos pos) {
        return !isFormed() ? null : getEnergyStorageForExternal(pos, getLocalOffsetFromController(pos));
    }

    protected @Nullable Storage<ItemVariant> getItemStorageForExternal(BlockPos worldPos, BlockPos localOffset) {
        return null;
    }

    protected @Nullable Storage<FluidVariant> getFluidStorageForExternal(BlockPos worldPos, BlockPos localOffset) {
        return null;
    }

    protected @Nullable EnergyStorage getEnergyStorageForExternal(BlockPos worldPos, BlockPos localOffset) {
        return null;
    }

    public @Nullable Storage<SlurryVariant> getSlurryStorageForExternal(BlockPos worldPos, @Nullable Direction side) {
        return !isFormed() ? null : getSlurryStorageForExternal(worldPos, getLocalOffsetFromController(worldPos), side);
    }

    protected @Nullable Storage<SlurryVariant> getSlurryStorageForExternal(BlockPos worldPos, BlockPos localOffset, @Nullable Direction side) {
        return null;
    }

    public @Nullable SingleGasStorage getGasStorageForExternal(BlockPos worldPos, @Nullable Direction side) {
        return null;
    }

    public @Nullable HeatStorage getHeatStorageForExternal(BlockPos worldPos, @Nullable Direction side) {
        return !isFormed() ? null : getHeatStorageForExternal(worldPos, getLocalOffsetFromController(worldPos), side);
    }

    protected @Nullable HeatStorage getHeatStorageForExternal(BlockPos worldPos, BlockPos localOffset, @Nullable Direction side) {
        return null;
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of();
    }
}
