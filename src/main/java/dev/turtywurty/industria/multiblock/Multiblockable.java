package dev.turtywurty.industria.multiblock;

import dev.turtywurty.industria.init.AttachmentTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an object that acts as a multiblock controller.
 *
 * @apiNote This interface is designed to be implemented on a {@link net.minecraft.block.entity.BlockEntity} instance.
 */
@SuppressWarnings("UnstableApiUsage")
public interface Multiblockable {
    /**
     * Gets the type of multiblock this controller is.
     *
     * @return The type of multiblock this controller is.
     * @apiNote This method should return the same value every time it is called.
     * @see MultiblockType
     */
    MultiblockType<?> type();

    /**
     * Finds the positions of the blocks that make up the multiblock.
     *
     * @param facing The direction the multiblock is facing.
     * @return A list of positions that make up the multiblock.
     * @apiNote This method should return all the positions (if it is valid) or a list of positions that are invalid.
     * If the list of positions is less than the number of blocks the multiblock requires, the multiblock will not be built.
     * @see MultiblockType#numBlocks()
     */
    List<BlockPos> findPositions(@Nullable Direction facing);

    /**
     * Gets the positions of the blocks that make up the multiblock.
     *
     * @return A list of positions that make up the multiblock.
     * @apiNote This method should return a mutable list.
     */
    List<BlockPos> getMultiblockPositions();

    default EnergyStorage getEnergyStorage(Vec3i offsetFromPrimary, @Nullable Direction direction) {
        return null;
    }

    default InventoryStorage getInventoryStorage(Vec3i offsetFromPrimary, @Nullable Direction direction) {
        return null;
    }

    default Storage<FluidVariant> getFluidStorage(Vec3i offsetFromPrimary, @Nullable Direction direction) {
        return null;
    }

    /**
     * Builds the multiblock in the world.
     *
     * @param world             The world the multiblock is being built in.
     * @param pos               The position of the block that is being used to build the multiblock.
     * @param state             The state of the block that is being used to build the multiblock.
     * @param placer            The entity that placed the block that is being used to build the multiblock or {@code null} if it was not placed by an entity.
     * @param itemStack         The item stack that was used to build the multiblock.
     * @param onSuccessfulBuild The action to perform when the multiblock is successfully built.
     * @apiNote This method should only be called on the server side.
     */
    // TODO: Validate that the positions are valid before building the multiblock
    default void buildMultiblock(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack, Runnable onSuccessfulBuild) {
        if (world == null || world.isClient)
            return;

        long startTime = System.nanoTime();
        Direction facing = type().hasDirectionProperty() ? state.get(Properties.HORIZONTAL_FACING) : null;
        List<BlockPos> checkPositions = findPositions(facing);
        long findValidPositionsEndTime = System.nanoTime();
        if (checkPositions.size() < type().numBlocks()) {
            world.breakBlock(pos, true);
            return;
        }

        for (BlockPos position : checkPositions) {
            world.setBlockState(position, BlockInit.MULTIBLOCK_BLOCK.getDefaultState());
            getMultiblockPositions().add(position);

            Chunk chunk = world.getChunk(position);
            Map<String, MultiblockData> map = chunk.getAttachedOrCreate(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT, HashMap::new);
            Map<String, MultiblockData> copy = new HashMap<>(map);
            copy.put(position.toShortString(), new MultiblockData(pos, type()));
            chunk.setAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT, copy);
        }

        onSuccessfulBuild.run();

        long endTime = System.nanoTime();
        System.out.println("Time to find valid positions: " + (findValidPositionsEndTime - startTime) + "ns");
        System.out.println("Time to build machine: " + (endTime - findValidPositionsEndTime) + "ns");
        System.out.println("Total time: " + (endTime - startTime) + "ns");
    }

    /**
     * Breaks the multiblock in the world.
     *
     * @param world The world the multiblock is being broken in.
     * @param pos   The position of the block that is being used to break the multiblock.
     * @apiNote This method should only be called on the server side.
     */
    default void breakMultiblock(World world, BlockPos pos) {
        if (world == null)
            return;

        for (BlockPos machinePos : getMultiblockPositions()) {
            if (!world.getBlockState(machinePos).isOf(BlockInit.MULTIBLOCK_BLOCK))
                continue;

            world.breakBlock(machinePos, false);

            Chunk chunk = world.getChunk(machinePos);
            Map<String, MultiblockData> map = chunk.getAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT);
            if (map == null)
                return;

            Map<String, MultiblockData> copy = new HashMap<>(map);
            copy.remove(machinePos.toShortString());
            if (copy.isEmpty()) {
                chunk.removeAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT);
            } else {
                chunk.setAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT, copy);
            }
        }

        world.breakBlock(pos, true);
    }

    /**
     * Writes the multiblock positions to NBT.
     *
     * @return An NBT list of type {@link net.minecraft.nbt.NbtIntArray} that represents the multiblock positions.
     */
    static NbtList writeMultiblockToNbt(Multiblockable multiblockable) {
        var machinePositions = new NbtList();
        for (BlockPos machinePosition : multiblockable.getMultiblockPositions()) {
            machinePositions.add(NbtHelper.fromBlockPos(machinePosition));
        }

        return machinePositions;
    }

    /**
     * Reads the multiblock positions from NBT.
     *
     * @param multiblockable The multiblock controller to read the positions to.
     * @param nbt            The NBT list of type {@link net.minecraft.nbt.NbtIntArray} that represents the multiblock positions.
     */
    static void readMultiblockFromNbt(Multiblockable multiblockable, NbtList nbt) {
        List<BlockPos> machinePositions = multiblockable.getMultiblockPositions();
        machinePositions.clear();
        for (int i = 0; i < nbt.size(); i++) {
            int[] machinePosition = nbt.getIntArray(i);
            machinePositions.add(new BlockPos(machinePosition[0], machinePosition[1], machinePosition[2]));
        }
    }
}
