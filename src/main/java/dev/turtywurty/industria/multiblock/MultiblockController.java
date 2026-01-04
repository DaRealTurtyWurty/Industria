package dev.turtywurty.industria.multiblock;

import dev.turtywurty.industria.Industria;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public interface MultiblockController {
    Component NO_VALID_COMBINATION = Component.translatable("error." + Industria.MOD_ID + ".multiblock.no_valid_combination");
    Component ASSEMBLY_COMPLETE = Component.translatable("info." + Industria.MOD_ID + ".multiblock.assembly_complete");

    MultiblockDefinition getDefinition();

    default void onAssembled(ServerLevel world, BlockPos pos, MultiblockMatcher.MatchResult matchResult) {}
}
