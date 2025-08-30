package dev.turtywurty.industria.item;

import dev.turtywurty.industria.multiblock.MultiblockAssembler;
import dev.turtywurty.industria.multiblock.MultiblockController;
import dev.turtywurty.industria.multiblock.MultiblockDefinition;
import dev.turtywurty.industria.multiblock.MultiblockMatcher;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class WrenchItem extends Item {
    public WrenchItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!(world instanceof ServerWorld serverWorld))
            return ActionResult.PASS;

        PlayerEntity player = context.getPlayer();
        if (player == null)
            return ActionResult.PASS;

        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof MultiblockController controller))
            return ActionResult.PASS;

        MultiblockDefinition definition = controller.getDefinition();
        Optional<MultiblockMatcher.MatchResult> matchResultOpt = new MultiblockMatcher(definition)
                .tryMatch(serverWorld, pos, true);
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
        MultiblockAssembler.AssembleResult assembleResult = assembler.assemble(serverWorld);
        if(assembleResult != null) {
            player.sendMessage(MultiblockController.ASSEMBLY_COMPLETE, false);
        }

        return ActionResult.SUCCESS;
    }
}
