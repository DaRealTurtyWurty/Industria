package dev.turtywurty.industria.renderer.conveyor;

import dev.turtywurty.industria.conveyor.block.impl.HatchConveyorBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class HatchConveyorSpecialRenderer extends AbstractFlapConveyorSpecialRenderer {
    public static final HatchConveyorSpecialRenderer INSTANCE = new HatchConveyorSpecialRenderer();

    private static final float FLAP_MODEL_Z_OFFSET = 1f / 16f;

    private HatchConveyorSpecialRenderer() {
    }

    @Override
    protected Direction getFacing(BlockState state) {
        return state.getValue(HatchConveyorBlock.FACING);
    }

    @Override
    protected float getFlapModelZOffset() {
        return FLAP_MODEL_Z_OFFSET;
    }
}
