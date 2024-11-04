package dev.turtywurty.industria.block;

import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class OilPumpJackBlock extends MultiblockControllerBlock {
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;

    public OilPumpJackBlock(Settings settings) {
        super(settings, MultiblockTypeInit.OIL_PUMP_JACK);

        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeInit.OIL_PUMP_JACK.instantiate(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return TickableBlockEntity.createTicker(world);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }
}
