package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RotaryKilnBlockEntity extends IndustriaBlockEntity {
    public RotaryKilnBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.ROTARY_KILN, BlockEntityTypeInit.ROTARY_KILN, pos, state);
    }
}
