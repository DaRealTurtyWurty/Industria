package dev.turtywurty.industria.blockentity.util.energy;

import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.turtymultiloader.transfer.StorageTransfer;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.SimpleEnergyStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferContext;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * A generator whose two output sides can connect through an aligned row of generators.
 * Energy skips every generator in the row and is inserted only into receivers at its ends.
 */
public interface ChainedGeneratorEnergyOutput extends EnergySpreader {
    SimpleEnergyStorage getEnergyStorage();

    @Override
    default void spread(Level world, BlockPos pos, SimpleEnergyStorage energyStorage) {
        Direction negative = isEnergyOutputDirection(Direction.WEST) ? Direction.WEST : Direction.NORTH;
        Direction positive = negative.getOpposite();

        BlockPos beforeStart = pos.relative(negative);
        if (world.isLoaded(beforeStart) && canEnergyPassThrough(world.getBlockEntity(beforeStart), negative))
            return;

        List<SimpleEnergyStorage> sources = new ArrayList<>();
        BlockPos cursor = pos;
        while (world.isLoaded(cursor)) {
            BlockEntity blockEntity = world.getBlockEntity(cursor);
            if (!(blockEntity instanceof ChainedGeneratorEnergyOutput output)
                    || !output.isEnergyOutputDirection(positive))
                break;

            sources.add(output.getEnergyStorage());
            cursor = cursor.relative(positive);
        }

        ResourceStorage<ResourceVariant<UnitResource>> negativeTarget = findTarget(world, beforeStart, negative);
        ResourceStorage<ResourceVariant<UnitResource>> positiveTarget = findTarget(world, cursor, positive);
        if (negativeTarget == null && positiveTarget == null)
            return;

        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            int left = 0;
            int right = sources.size() - 1;
            while (left <= right) {
                if (negativeTarget != null) {
                    moveToTargets(sources.get(left++), negativeTarget, positiveTarget, transaction);
                }

                if (left <= right && positiveTarget != null) {
                    moveToTargets(sources.get(right--), positiveTarget, negativeTarget, transaction);
                }
            }

            transaction.commit();
        }
    }

    private static ResourceStorage<ResourceVariant<UnitResource>> findTarget(
            Level world, BlockPos pos, Direction travelDirection) {
        if (!world.isLoaded(pos))
            return null;

        ResourceStorage<ResourceVariant<UnitResource>> target = TransferType.ENERGY.lookup(
                world, pos, travelDirection.getOpposite());
        return target != null && target.supportsInsertion() ? target : null;
    }

    private static void moveToTargets(SimpleEnergyStorage source,
                                      ResourceStorage<ResourceVariant<UnitResource>> primaryTarget,
                                      ResourceStorage<ResourceVariant<UnitResource>> secondaryTarget,
                                      TransferContext transaction) {
        long remaining = Math.min(source.getAmount(), source.getMaxOutput());
        if (remaining <= 0)
            return;

        remaining -= StorageTransfer.move(source, primaryTarget, SimpleEnergyStorage.ENERGY, remaining, transaction);
        if (remaining > 0 && secondaryTarget != null) {
            StorageTransfer.move(source, secondaryTarget, SimpleEnergyStorage.ENERGY, remaining, transaction);
        }
    }

    @Override
    default Iterable<Direction> getEnergyOutputDirections(Level world, BlockPos pos) {
        Direction facing = world.getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING);
        return List.of(facing.getCounterClockWise(), facing.getClockWise());
    }

    @Override
    default boolean canEnergyPassThrough(BlockEntity blockEntity, Direction direction) {
        return blockEntity instanceof ChainedGeneratorEnergyOutput output && output.isEnergyOutputDirection(direction);
    }

    default boolean isEnergyOutputDirection(Direction direction) {
        if (!(this instanceof BlockEntity blockEntity) || direction == null)
            return false;

        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return direction == facing.getCounterClockWise() || direction == facing.getClockWise();
    }
}
