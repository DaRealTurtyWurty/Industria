package dev.turtywurty.industria.multiblock;

import dev.turtywurty.industria.init.AttachmentTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public interface Multiblockable {
    MultiblockType type();

    List<BlockPos> findPositions(@Nullable Direction facing);

    List<BlockPos> getMultiblockPositions();

    default void buildMultiblock(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack, Runnable onSuccessfulBuild) {
        if (world == null || world.isClient)
            return;

        long startTime = System.nanoTime();
        Direction facing = type().hasDirectionProperty() ? state.get(Properties.HORIZONTAL_FACING) : null;
        List<BlockPos> checkPositions = findPositions(facing);
        long findValidPositionsEndTime = System.nanoTime();
        if(checkPositions.size() < type().numBlocks()) {
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

    default void breakMultiblock(World world, BlockPos pos) {
        if (world == null)
            return;

        for (BlockPos machinePos : getMultiblockPositions()) {
            if(!world.getBlockState(machinePos).isOf(BlockInit.MULTIBLOCK_BLOCK))
                continue;

            world.breakBlock(machinePos, false);

            Chunk chunk = world.getChunk(machinePos);
            Map<String, MultiblockData> map = chunk.getAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT);
            if (map == null)
                return;

            Map<String, MultiblockData> copy = new HashMap<>(map);
            copy.remove(machinePos.toShortString());
            if(copy.isEmpty()) {
                chunk.removeAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT);
            } else {
                chunk.setAttached(AttachmentTypeInit.MULTIBLOCK_ATTACHMENT, copy);
            }
        }

        world.breakBlock(pos, true);
    }

    static NbtList writeMultiblockToNbt(Multiblockable multiblockable) {
        var machinePositions = new NbtList();
        for (BlockPos machinePosition : multiblockable.getMultiblockPositions()) {
            machinePositions.add(NbtHelper.fromBlockPos(machinePosition));
        }

        return machinePositions;
    }

    static void readMultiblockFromNbt(Multiblockable multiblockable, NbtList nbt) {
        List<BlockPos> machinePositions = multiblockable.getMultiblockPositions();
        machinePositions.clear();
        for (int i = 0; i < nbt.size(); i++) {
            int[] machinePosition = nbt.getIntArray(i);
            machinePositions.add(new BlockPos(machinePosition[0], machinePosition[1], machinePosition[2]));
        }
    }
}
