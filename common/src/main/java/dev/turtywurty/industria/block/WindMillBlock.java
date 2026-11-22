package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.WindMillBlockEntity;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.industria.mechanical.MechanicalNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Set;

public class WindMillBlock extends IndustriaBlock implements MechanicalNode {
    public WindMillBlock(Properties settings) {
        super(settings.pushReaction(PushReaction.IMMOVEABLE), new BlockProperties()
            .hasHorizontalFacing()
            .hasBlockEntityRenderer()
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.WIND_MILL)
                .shouldTick()
                .dropContentsOnBreak()));
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack itemStack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        if (!itemStack.is(ModItems.WIND_MILL_SAIL.get()))
            return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);

        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        if (!(level.getBlockEntity(pos) instanceof WindMillBlockEntity mill) || !mill.installSail(itemStack))
            return InteractionResult.PASS;

        if (!player.isCreative()) {
            itemStack.shrink(1);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public Set<Direction> getMechanicalPorts(BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return Set.of(facing, facing.getOpposite());
    }
}
