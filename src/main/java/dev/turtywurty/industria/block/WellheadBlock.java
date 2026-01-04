package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.WellheadBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WellheadBlock extends IndustriaBlock {
    public WellheadBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasBlockEntityRenderer()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.WELLHEAD)
                        .shouldTick()));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        loadDrillTubes(world, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if(direction == Direction.DOWN) {
            loadDrillTubes(world, pos);
        }

        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    private static void loadDrillTubes(LevelReader world, BlockPos pos) {
        List<BlockPos> positions = findDrillTubes(world, pos);
        if(!positions.isEmpty() && world.getBlockEntity(pos) instanceof WellheadBlockEntity wellheadBlockEntity) {
            wellheadBlockEntity.modifyDrillTubes(positions);
        }
    }

    private static List<BlockPos> findDrillTubes(LevelReader world, BlockPos pos) {
        if (world == null || pos == null)
            return Collections.emptyList();

        BlockPos.MutableBlockPos checkPos = pos.below().mutable();
        BlockState checkState = world.getBlockState(checkPos);

        List<BlockPos> drillTubes = new ArrayList<>();
        while (checkState.is(BlockInit.DRILL_TUBE)) {
            drillTubes.add(checkPos.immutable());

            checkPos.move(Direction.DOWN);
            checkState = world.getBlockState(checkPos);
        }

        return drillTubes;
    }
}
