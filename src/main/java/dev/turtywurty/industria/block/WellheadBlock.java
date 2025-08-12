package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.WellheadBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WellheadBlock extends IndustriaBlock {
    public WellheadBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasBlockEntityRenderer()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.WELLHEAD)
                        .shouldTick()));
    }

    private static void loadDrillTubes(WorldView world, BlockPos pos) {
        List<BlockPos> positions = findDrillTubes(world, pos);
        if (!positions.isEmpty() && world.getBlockEntity(pos) instanceof WellheadBlockEntity wellheadBlockEntity) {
            wellheadBlockEntity.modifyDrillTubes(positions);
        }
    }

    private static List<BlockPos> findDrillTubes(WorldView world, BlockPos pos) {
        if (world == null || pos == null)
            return Collections.emptyList();

        BlockPos.Mutable checkPos = pos.down().mutableCopy();
        BlockState checkState = world.getBlockState(checkPos);

        List<BlockPos> drillTubes = new ArrayList<>();
        while (checkState.isOf(BlockInit.DRILL_TUBE)) {
            drillTubes.add(checkPos.toImmutable());

            checkPos.move(Direction.DOWN);
            checkState = world.getBlockState(checkPos);
        }

        return drillTubes;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        loadDrillTubes(world, pos);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (direction == Direction.DOWN) {
            loadDrillTubes(world, pos);
        }

        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }
}
