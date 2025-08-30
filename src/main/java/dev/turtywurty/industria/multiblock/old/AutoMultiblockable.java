package dev.turtywurty.industria.multiblock.old;

import dev.turtywurty.industria.init.AttachmentTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.multiblock.LocalDirection;
import dev.turtywurty.industria.multiblock.LocalPos;
import dev.turtywurty.industria.multiblock.PortType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents an object that acts as a multiblock controller.
 *
 * @apiNote This interface is designed to be implemented on a {@link BlockEntity} instance.
 */
@SuppressWarnings("UnstableApiUsage")
public interface AutoMultiblockable extends Multiblockable {
    static Map<Direction, Port> toIOPortMap(Map<Direction, List<PortType>> portTypes) {
        Map<Direction, Port> ports = new EnumMap<>(Direction.class);
        portTypes.forEach((dir, types) -> ports.put(dir, new Port(dir, types)));
        return ports;
    }

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
            Vec3i offset = MultiblockBlock.getOffsetFromPrimary(pos, position, facing);
            Block toSet = BlockInit.MULTIBLOCK_BLOCK;
            for (Direction direction : Direction.values()) {
                Map<Direction, Port> ports = getPorts(offset, direction);
                if (ports.isEmpty())
                    continue;

                toSet = BlockInit.AUTO_MULTIBLOCK_IO;
            }

            world.setBlockState(position, toSet.getDefaultState());
            getMultiblockPositions().add(position);

            Chunk chunk = world.getChunk(position);
            Map<BlockPos, MultiblockData> map = chunk.getAttachedOrCreate(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT, HashMap::new);
            Map<BlockPos, MultiblockData> copy = new HashMap<>(map);
            copy.put(position, new MultiblockData(pos, type()));
            chunk.setAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT, copy);
        }

        onSuccessfulBuild.run();

        long endTime = System.nanoTime();
        System.out.println("Time to find valid positions: " + (findValidPositionsEndTime - startTime) + "ns");
        System.out.println("Time to build machine: " + (endTime - findValidPositionsEndTime) + "ns");
        System.out.println("Total time: " + (endTime - startTime) + "ns");
    }

    @Override
    default void onMultiblockBreak(World world, BlockPos pos) {
        if (world == null)
            return;

        for (BlockPos machinePos : getMultiblockPositions()) {
            BlockState blockState = world.getBlockState(machinePos);
            if (!blockState.isOf(BlockInit.MULTIBLOCK_BLOCK) && !blockState.isOf(BlockInit.AUTO_MULTIBLOCK_IO))
                continue;

            world.breakBlock(machinePos, false);

            Chunk chunk = world.getChunk(machinePos);
            Map<BlockPos, MultiblockData> map = chunk.getAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT);
            if (map == null)
                return;

            Map<BlockPos, MultiblockData> copy = new HashMap<>(map);
            copy.remove(machinePos);
            if (copy.isEmpty()) {
                chunk.removeAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT);
            } else {
                chunk.setAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT, copy);
            }
        }

        world.breakBlock(pos, true);
    }

    /**
     * Gets the ports of the multiblock.
     *
     * @param offsetFromPrimary The offset from the primary block of the multiblock.
     * @param direction         The direction the ports are in.
     * @return A map of ports that are in the specified direction.
     */
    default Map<Direction, Port> getPorts(Vec3i offsetFromPrimary, Direction direction) {
        Map<Direction, List<PortType>> output = new EnumMap<>(Direction.class);
        Direction facing = getFacing();
        var localPos = LocalPos.from(offsetFromPrimary);

        for (PositionedPortRule rule : getPortRules()) {
            if (!rule.positionMatch().test(localPos))
                continue;

            for (LocalDirection localSide : rule.sides()) {
                Direction worldSide = localSide.toWorld(facing);
                if (worldSide != direction)
                    continue;

                output.computeIfAbsent(worldSide, k -> new ArrayList<>()).addAll(rule.types());
            }
        }

        return toIOPortMap(output);
    }

    default List<PositionedPortRule> getPortRules() {
        return Collections.emptyList();
    }
}
