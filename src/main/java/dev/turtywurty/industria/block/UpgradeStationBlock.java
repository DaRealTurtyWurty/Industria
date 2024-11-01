package dev.turtywurty.industria.block;

import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class UpgradeStationBlock extends Block implements BlockEntityProvider {
    public UpgradeStationBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeInit.UPGRADE_STATION.instantiate(pos, state);
    }
}
