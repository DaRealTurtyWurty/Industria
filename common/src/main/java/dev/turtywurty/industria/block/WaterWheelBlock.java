package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.mechanical.MechanicalNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class WaterWheelBlock extends IndustriaBlock implements MechanicalNode {
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 8);
    private static final List<BlockPos> OFFSETS = List.of(
        BlockPos.ZERO,
        new BlockPos(-1, -1, 0), new BlockPos(0, -1, 0), new BlockPos(1, -1, 0),
        new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0),
        new BlockPos(-1, 1, 0), new BlockPos(0, 1, 0), new BlockPos(1, 1, 0));

    public WaterWheelBlock(Properties properties) {
        super(properties.pushReaction(PushReaction.IMMOVEABLE), new BlockProperties()
            .hasHorizontalFacing()
            .addStateProperty(PART, 0)
            .hasBlockEntityRenderer()
            .hasWaterloggableProperty()
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.WATER_WHEEL)
                .shouldTick()
                .blockEntityFactory((pos, state) -> state.getValue(PART) == 0
                    ? ModBlockEntityTypes.WATER_WHEEL.get().create(pos, state)
                    : null)
                .blockEntityTickerFactory((level, state, _) -> state.getValue(PART) == 0
                    ? TickableBlockEntity.createTicker(level)
                    : null)));
    }

    public static BlockPos getPartOffset(int part, Direction facing) {
        BlockPos offset = OFFSETS.get(part);
        int x = offset.getX(), y = offset.getY(), z = offset.getZ();
        return switch (facing) {
            case EAST -> new BlockPos(-z, y, x);
            case SOUTH -> new BlockPos(-x, y, -z);
            case WEST -> new BlockPos(z, y, -x);
            default -> offset;
        };
    }

    public static BlockPos getControllerPos(BlockPos pos, BlockState state) {
        return pos.subtract(getPartOffset(state.getValue(PART),
            state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)
            return null;

        Level level = context.getLevel();
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for (int part = 0; part < OFFSETS.size(); part++) {
            BlockPos partPos = context.getClickedPos().offset(getPartOffset(part, facing));
            if (level.isOutsideBuildHeight(partPos) || !level.getWorldBorder().isWithinBounds(partPos)
                || !level.getBlockState(partPos).canBeReplaced()
                || !level.isUnobstructed(state.setValue(PART, part), partPos, CollisionContext.empty())
                || (context.getPlayer() != null
                    && (!level.mayInteract(context.getPlayer(), partPos)
                        || !context.getPlayer().mayUseItemAt(partPos, context.getClickedFace(),
                            context.getItemInHand()))))
                return null;
        }

        return state;
    }

    @Override
    public void setPlacedBy(
        Level level,
        BlockPos pos,
        BlockState state,
        @Nullable LivingEntity placer,
        ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (state.getValue(PART) != 0)
            return;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for (int part = 1; part < OFFSETS.size(); part++) {
            level.setBlock(pos.offset(getPartOffset(part, facing)), state.setValue(PART, part), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
        BlockPos controllerPos = getControllerPos(pos, state);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for (int part = 0; part < OFFSETS.size(); part++) {
            BlockPos partPos = controllerPos.offset(getPartOffset(part, facing));
            BlockState partState = level.getBlockState(partPos);
            if (partState.is(this) && partState.getValue(PART) == part
                && partState.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing) {
                level.destroyBlock(partPos, false);
            }
        }
    }

    @Override
    public Set<Direction> getMechanicalPorts(BlockState state) {
        if (state.getValue(PART) != 0)
            return Set.of();

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return Set.of(facing, facing.getOpposite());
    }
}
