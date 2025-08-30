package dev.turtywurty.industria.block;

import dev.turtywurty.industria.blockentity.ExampleMultiblockControllerBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockDefinitionInit;
import dev.turtywurty.industria.multiblock.MultiblockController;
import dev.turtywurty.industria.multiblock.MultiblockDefinition;
import dev.turtywurty.industria.multiblock.MultiblockMatcher;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class ExampleMultiblockControllerBlock extends Block implements MultiblockController, BlockEntityProvider {
    public ExampleMultiblockControllerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public MultiblockDefinition getDefinition() {
        return MultiblockDefinitionInit.EXAMPLE;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeInit.EXAMPLE_MULTIBLOCK_CONTROLLER.instantiate(pos, state);
    }

    @Override
    public void onAssembled(ServerWorld world, BlockPos pos, MultiblockMatcher.MatchResult matchResult) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ExampleMultiblockControllerBlockEntity controller) {
            controller.addPositions(matchResult.positions().toList());
        }
    }
}
