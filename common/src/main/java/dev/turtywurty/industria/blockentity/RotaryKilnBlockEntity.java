package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RotaryKilnBlockEntity extends IndustriaBlockEntity {
    public RotaryKilnBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ROTARY_KILN.get(), ModBlockEntityTypes.ROTARY_KILN.get(), pos, state);
    }
}
