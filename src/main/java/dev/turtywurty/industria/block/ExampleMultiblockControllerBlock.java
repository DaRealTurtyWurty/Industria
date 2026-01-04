package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.Wrenchable;
import dev.turtywurty.industria.blockentity.MultiblockControllerBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockDefinitionInit;
import dev.turtywurty.industria.multiblock.MultiblockAssembler;
import dev.turtywurty.industria.multiblock.MultiblockController;
import dev.turtywurty.industria.multiblock.MultiblockDefinition;
import dev.turtywurty.industria.multiblock.MultiblockMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ExampleMultiblockControllerBlock extends Block implements MultiblockController, EntityBlock, Wrenchable {
    public ExampleMultiblockControllerBlock(Properties settings) {
        super(settings);
    }

    @Override
    public MultiblockDefinition getDefinition() {
        return MultiblockDefinitionInit.EXAMPLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeInit.EXAMPLE_MULTIBLOCK_CONTROLLER.create(pos, state);
    }

    @Override
    public void onAssembled(ServerLevel world, BlockPos pos, MultiblockMatcher.MatchResult matchResult) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof MultiblockControllerBlockEntity controller) {
            controller.addPositions(matchResult.positions().toList());
        }
    }

    @Override
    public InteractionResult onWrenched(ServerLevel world, BlockPos pos, Player player, UseOnContext context) {
        MultiblockDefinition definition = getDefinition();
        Optional<MultiblockMatcher.MatchResult> matchResultOpt = new MultiblockMatcher(definition)
                .tryMatch(world, pos, true);
        if (matchResultOpt.isEmpty()) {
            player.displayClientMessage(MultiblockController.NO_VALID_COMBINATION, false);
            return InteractionResult.SUCCESS;
        }

        MultiblockMatcher.MatchResult matchResult = matchResultOpt.get();
        if(!matchResult.isValid()) {
            for (MultiblockMatcher.MatchResult.Problem problem : matchResult.problems()) {
                String message = problem.message();
                if (message != null) {
                    player.displayClientMessage(Component.translatable(message), false);
                }
            }

            return InteractionResult.SUCCESS;
        }

        var assembler = new MultiblockAssembler(definition, matchResult);
        MultiblockAssembler.AssembleResult assembleResult = assembler.assemble(world);
        if(assembleResult != null) {
            player.displayClientMessage(MultiblockController.ASSEMBLY_COMPLETE, false);
        }

        return InteractionResult.SUCCESS;
    }
}
