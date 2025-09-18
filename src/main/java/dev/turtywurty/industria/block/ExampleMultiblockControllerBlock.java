package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.Wrenchable;
import dev.turtywurty.industria.blockentity.MultiblockControllerBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockDefinitionInit;
import dev.turtywurty.industria.multiblock.MultiblockAssembler;
import dev.turtywurty.industria.multiblock.MultiblockController;
import dev.turtywurty.industria.multiblock.MultiblockDefinition;
import dev.turtywurty.industria.multiblock.MultiblockMatcher;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ExampleMultiblockControllerBlock extends Block implements MultiblockController, BlockEntityProvider, Wrenchable {
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
        if (blockEntity instanceof MultiblockControllerBlockEntity controller) {
            controller.addPositions(matchResult.positions().toList());
        }
    }

    @Override
    public ActionResult onWrenched(ServerWorld world, BlockPos pos, PlayerEntity player, ItemUsageContext context) {
        MultiblockDefinition definition = getDefinition();
        Optional<MultiblockMatcher.MatchResult> matchResultOpt = new MultiblockMatcher(definition)
                .tryMatch(world, pos, true);
        if (matchResultOpt.isEmpty()) {
            player.sendMessage(MultiblockController.NO_VALID_COMBINATION, false);
            return ActionResult.SUCCESS;
        }

        MultiblockMatcher.MatchResult matchResult = matchResultOpt.get();
        if(!matchResult.isValid()) {
            for (MultiblockMatcher.MatchResult.Problem problem : matchResult.problems()) {
                String message = problem.message();
                if (message != null) {
                    player.sendMessage(Text.translatable(message), false);
                }
            }

            return ActionResult.SUCCESS;
        }

        var assembler = new MultiblockAssembler(definition, matchResult);
        MultiblockAssembler.AssembleResult assembleResult = assembler.assemble(world);
        if(assembleResult != null) {
            player.sendMessage(MultiblockController.ASSEMBLY_COMPLETE, false);
        }

        return ActionResult.SUCCESS;
    }
}
