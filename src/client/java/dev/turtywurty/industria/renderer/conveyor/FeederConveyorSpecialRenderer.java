package dev.turtywurty.industria.renderer.conveyor;

import dev.turtywurty.industria.conveyor.block.impl.FeederConveyorBlock;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FeederConveyorSpecialRenderer extends AbstractFlapConveyorSpecialRenderer {
    public static final FeederConveyorSpecialRenderer INSTANCE = new FeederConveyorSpecialRenderer();

    private FeederConveyorSpecialRenderer() {
    }

    @Override
    protected Direction getFacing(BlockState state) {
        return state.getValue(FeederConveyorBlock.FACING);
    }

    public void onFeederRemoved(ResourceKey<Level> dimension, BlockPos pos) {
        onConveyorRemoved(dimension, pos);
    }
}
