package dev.turtywurty.industria.multiblock;

import dev.turtywurty.industria.Industria;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public interface MultiblockController {
    Text NO_VALID_COMBINATION = Text.translatable("error." + Industria.MOD_ID + ".multiblock.no_valid_combination");
    Text ASSEMBLY_COMPLETE = Text.translatable("info." + Industria.MOD_ID + ".multiblock.assembly_complete");

    MultiblockDefinition getDefinition();

    default void onAssembled(ServerWorld world, BlockPos pos, MultiblockMatcher.MatchResult matchResult) {}
}
