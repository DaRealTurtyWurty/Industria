package dev.turtywurty.industria.block;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.blockentity.CrusherBlockEntity;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CrusherBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public static final Box PICKUP_AREA = new Box(0, 0, 0, 1, 0.7, 1);

    private static final MapCodec<CrusherBlock> CODEC = createCodec(CrusherBlock::new);

    public static final BooleanProperty RUNNING = BooleanProperty.of("running");
    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0, 0, 0, 1, 0.625, 1);

    public CrusherBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(RUNNING, false));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if(!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if(blockEntity instanceof CrusherBlockEntity crusherBlockEntity) {
                player.openHandledScreen(crusherBlockEntity);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CrusherBlockEntity crusherBlockEntity) {
                crusherBlockEntity.getWrappedInventoryStorage().dropContents(world, pos);
                world.updateComparators(pos, this);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, RUNNING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeInit.CRUSHER.instantiate(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return TickableBlockEntity.createTicker(world);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if(world.getBlockEntity(pos) instanceof CrusherBlockEntity blockEntity) {
            if(blockEntity.getProgress() <= 0)
                return;

            ItemStack stack = blockEntity.getWrappedInventoryStorage().getInventory(CrusherBlockEntity.INPUT_SLOT).getStack(0);
            var particle = new ItemStackParticleEffect(ParticleTypes.ITEM, stack);
            for (int i = 0; i < random.nextInt(2) + 1; i++) {
                world.addParticle(particle, pos.getX() + 0.25 + nextFloat(random, -0.1f, 0.1f),
                        pos.getY() + 0.55 + nextFloat(random, -0.1f, 0.1f),
                        pos.getZ() + 0.25 + nextFloat(random, -0.1f, 0.1f), 0, 0, 0);
            }

            for (int i = 0; i < random.nextInt(2) + 1; i++) {
                world.addParticle(particle, pos.getX() + 0.75 + nextFloat(random, -0.1f, 0.1f),
                        pos.getY() + 0.55 + nextFloat(random, -0.1f, 0.1f),
                        pos.getZ() + 0.25 + nextFloat(random, -0.1f, 0.1f), 0, 0, 0);
            }

            for (int i = 0; i < random.nextInt(2) + 1; i++) {
                world.addParticle(particle, pos.getX() + 0.25 + nextFloat(random, -0.1f, 0.1f),
                        pos.getY() + 0.55 + nextFloat(random, -0.1f, 0.1f),
                        pos.getZ() + 0.75 + nextFloat(random, -0.1f, 0.1f), 0, 0, 0);
            }

            for (int i = 0; i < random.nextInt(2) + 1; i++) {
                world.addParticle(particle, pos.getX() + 0.75 + nextFloat(random, -0.1f, 0.1f),
                        pos.getY() + 0.55 + nextFloat(random, -0.1f, 0.1f),
                        pos.getZ() + 0.75 + nextFloat(random, -0.1f, 0.1f), 0, 0, 0);
            }
        }
    }

    private static float nextFloat(Random random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }
}
