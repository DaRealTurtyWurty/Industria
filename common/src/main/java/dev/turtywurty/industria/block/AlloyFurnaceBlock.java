package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.DoorContainerBlock;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AlloyFurnaceBlock extends DoorContainerBlock {
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 8);
    private static final List<BlockPos> OFFSETS = List.of(
        new BlockPos(0, 0, 0), new BlockPos(1, 0, 0),
        new BlockPos(0, 0, 1), new BlockPos(1, 0, 1),
        new BlockPos(0, 1, 0), new BlockPos(1, 1, 0),
        new BlockPos(0, 1, 1), new BlockPos(1, 1, 1),
        new BlockPos(0, 2, 1));
    private static final Map<Direction, List<VoxelShape>> SHAPES = createShapes();

    public AlloyFurnaceBlock(Properties settings) {
        super(settings.pushReaction(PushReaction.IMMOVEABLE), new BlockProperties()
            .hasHorizontalFacing()
            .hasLitProperty()
            .addStateProperty(PART, 0)
            .hasBlockEntityRenderer()
            .shapeFactory((state, _, _, _) -> SHAPES
                .get(state.getValue(BlockStateProperties.HORIZONTAL_FACING)).get(state.getValue(PART)))
            .hasComparatorOutput()
            .comparatorOutput((state, level, pos, _) -> AbstractContainerMenu.getRedstoneSignalFromBlockEntity(
                level.getBlockEntity(getControllerPos(pos, state))))
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.ALLOY_FURNACE)
                .blockEntityFactory((pos, state) -> state.getValue(PART) == 0
                    ? ModBlockEntityTypes.ALLOY_FURNACE.get().create(pos, state)
                    : null)
                .shouldTick()
                .blockEntityTickerFactory((level, state, _) -> state.getValue(PART) == 0
                    ? TickableBlockEntity.createTicker(level, true)
                    : null)
                .dropContentsOnBreak()));
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

    private static Map<Direction, List<VoxelShape>> createShapes() {
        VoxelShape body = Shapes.or(
            Block.box(0, 0, 1, 32, 32, 32),
            Block.box(0, 0, 0, 7, 32, 1), Block.box(25, 0, 0, 32, 32, 1),
            Block.box(7, 0, 0, 25, 12, 1), Block.box(7, 26, 0, 25, 32, 1));
        VoxelShape chimney = Shapes.or(
            Block.box(4.5, 32, 18.5, 13.5, 34, 27.5),
            Block.box(5, 34, 19, 13, 44, 27));
        VoxelShape fullShape = Shapes.or(body, chimney);
        Map<Direction, List<VoxelShape>> shapes = new EnumMap<>(Direction.class);
        for (Direction facing : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            shapes.put(facing, OFFSETS.stream().map(offset -> {
                VoxelShape localShape = Shapes.join(
                    fullShape.move(-offset.getX(), -offset.getY(), -offset.getZ()),
                    Shapes.block(), BooleanOp.AND).optimize();
                return BlockProperties.calculateShape(facing, localShape);
            }).toList());
        }
        return shapes;
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
                        || !context.getPlayer().mayUseItemAt(partPos, context.getClickedFace(), context.getItemInHand()))))
                return null;
        }
        return state;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (state.getValue(PART) != 0)
            return;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for (int part = 1; part < OFFSETS.size(); part++) {
            level.setBlock(pos.offset(getPartOffset(part, facing)), state.setValue(PART, part), Block.UPDATE_ALL);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockPos controllerPos = getControllerPos(pos, state);
        if (player instanceof ServerPlayer serverPlayer
            && level.getBlockEntity(controllerPos) instanceof BlockEntityWithGui<?> furnace) {
            furnace.openMenu(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(PART) != 0 || !state.getValue(BlockStateProperties.LIT))
            return;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction right = facing.getClockWise();
        Direction back = facing.getOpposite();
        double sideOffset = 1.0 / 16.0 + (random.nextDouble() - 0.5) * 0.25;
        double backOffset = 15.0 / 16.0 + (random.nextDouble() - 0.5) * 0.25;
        level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
            pos.getX() + 0.5 + right.getStepX() * sideOffset + back.getStepX() * backOffset,
            pos.getY() + 2.8,
            pos.getZ() + 0.5 + right.getStepZ() * sideOffset + back.getStepZ() * backOffset,
            (random.nextDouble() - 0.5) * 0.01, 0.04, (random.nextDouble() - 0.5) * 0.01);
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
}
