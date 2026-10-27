package dev.turtywurty.industria.block.abstraction;

import dev.turtywurty.industria.blockentity.DoorContainerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DoorContainerBlock extends IndustriaBlock {
    public DoorContainerBlock(Properties settings, BlockProperties properties) {
        super(settings, properties);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int data) {
        super.triggerEvent(state, level, pos, id, data);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(id, data);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof DoorContainerBlockEntity blockEntity) {
            blockEntity.recheckOpen();
        }
    }
}
