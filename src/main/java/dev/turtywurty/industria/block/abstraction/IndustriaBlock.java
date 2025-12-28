package dev.turtywurty.industria.block.abstraction;

import com.mojang.datafixers.util.Function4;
import dev.turtywurty.industria.block.abstraction.state.StateProperties;
import dev.turtywurty.industria.block.abstraction.state.StateProperty;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockable;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class IndustriaBlock extends Block implements BlockEntityProvider {
    public final StateProperties stateProperties;
    public final boolean placeFacingOpposite;
    public final Supplier<BlockEntityType<?>> blockEntityTypeSupplier;
    public final boolean shouldTick;
    public final BlockEntityFactory<?> blockEntityFactory;
    public final BlockEntityTickerFactory<?> blockEntityTicker;
    public final boolean hasComparatorOutput;
    public final Function4<BlockState, World, BlockPos, Direction, Integer> comparatorOutput;
    public final BlockRenderType renderType;
    public final ShapeFactory shapeFactory;
    public final MultiblockType<?> multiblockType;
    public final boolean rightClickToOpenGui;
    public final BiPredicate<WorldView, BlockPos> canExistAt;
    public final Map<Direction, VoxelShape> cachedDirectionalShapes;
    public final boolean dropContentsOnBreak;

    @SuppressWarnings("unchecked")
    public IndustriaBlock(Settings settings, BlockProperties properties) {
        super(settings);

        this.stateProperties = properties.stateProperties;
        this.placeFacingOpposite = properties.placeFacingOpposite;
        this.hasComparatorOutput = properties.hasComparatorOutput;
        this.comparatorOutput = properties.comparatorOutput;
        this.renderType = properties.renderType;
        this.shapeFactory = properties.shapeFactory;
        this.canExistAt = properties.canExistAt;
        this.cachedDirectionalShapes = properties.cachedDirectionalShapes;

        if (properties.blockEntityProperties != null) {
            this.blockEntityTypeSupplier = (Supplier<BlockEntityType<?>>) (Object) properties.blockEntityProperties.blockEntityTypeSupplier;
            this.shouldTick = properties.blockEntityProperties.shouldTick;
            this.blockEntityFactory = properties.blockEntityProperties.blockEntityFactory;
            this.blockEntityTicker = properties.blockEntityProperties.blockEntityTicker;
            this.rightClickToOpenGui = properties.blockEntityProperties.rightClickToOpenGui;
            this.dropContentsOnBreak = properties.blockEntityProperties.dropContentsOnBreak;

            if (properties.blockEntityProperties.multiblockProperties != null) {
                this.multiblockType = properties.blockEntityProperties.multiblockProperties.type;
            } else {
                this.multiblockType = null;
            }
        } else {
            this.blockEntityTypeSupplier = null;
            this.shouldTick = false;
            this.blockEntityFactory = null;
            this.blockEntityTicker = null;
            this.multiblockType = null;
            this.rightClickToOpenGui = false;
            this.dropContentsOnBreak = false;
        }

        StateManager.Builder<Block, BlockState> builder = new StateManager.Builder<>(this);
        appendProperties(builder);
        this.stateManager = builder.build(Block::getDefaultState, BlockState::new);
        setDefaultState(this.stateManager.getDefaultState());

        BlockState state = this.stateManager.getDefaultState();
        state = this.stateProperties.applyDefaults(state);
        setDefaultState(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);

        if (this.stateProperties != null) {
            this.stateProperties.addToBuilder(builder);
        }
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        if (this.stateProperties.containsProperty(Properties.HORIZONTAL_FACING)) {
            return state.with(Properties.HORIZONTAL_FACING, rotation.rotate(state.get(Properties.HORIZONTAL_FACING)));
        }

        return super.rotate(state, rotation);
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        if (this.stateProperties.containsProperty(Properties.HORIZONTAL_FACING)) {
            return state.with(Properties.HORIZONTAL_FACING, mirror.apply(state.get(Properties.HORIZONTAL_FACING)));
        }

        return super.mirror(state, mirror);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if (state == null)
            return null;

        if (this.stateProperties.containsProperty(Properties.HORIZONTAL_FACING)) {
            Direction facing = ctx.getHorizontalPlayerFacing();
            if (this.placeFacingOpposite) {
                facing = facing.getOpposite();
            }

            state = state.with(Properties.HORIZONTAL_FACING, facing);
        }

        if (this.stateProperties.containsProperty(Properties.AXIS)) {
            state = state.with(Properties.AXIS, ctx.getSide().getAxis());
        }

        return state;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return this.blockEntityTypeSupplier != null ? this.blockEntityFactory.create(pos, state) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return this.shouldTick ? (BlockEntityTicker<T>) this.blockEntityTicker.create(world, state, type) : BlockEntityProvider.super.getTicker(world, state, type);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return this.hasComparatorOutput;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return this.hasComparatorOutput ? this.comparatorOutput.apply(state, world, pos, direction) : super.getComparatorOutput(state, world, pos, direction);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return this.renderType;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapeFactory.create(state, world, pos, context);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (this.multiblockType != null) {
            if (!world.isClient()) {
                this.multiblockType.onPrimaryBlockUse(world, player, hit, pos);
            }

            return ActionResult.SUCCESS;
        }

        if (this.rightClickToOpenGui) {
            if (!world.isClient()) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (player instanceof ServerPlayerEntity sPlayer && blockEntity instanceof BlockEntityWithGui<?> blockEntityWithGui) { // TODO: Replace with component access maybe?
                    sPlayer.openHandledScreen(blockEntityWithGui);
                }
            }

            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (this.multiblockType != null) {
            if (!world.isClient()) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof AutoMultiblockable multiblockable) {
                    multiblockable.buildMultiblock(world, pos, state, placer, itemStack, blockEntity::markDirty);
                }
            }
        }
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return this.canExistAt.test(world, pos);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView,
                                                   BlockPos pos, Direction direction, BlockPos neighborPos,
                                                   BlockState neighborState, Random random) {
        return !state.canPlaceAt(world, pos)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    public static class BlockProperties {
        private boolean placeFacingOpposite = true;
        private BlockBlockEntityProperties<?> blockEntityProperties;
        private boolean hasComparatorOutput = false;
        private Function4<BlockState, World, BlockPos, Direction, Integer> comparatorOutput =
                (state, world, pos, direction) -> ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
        private BlockRenderType renderType = BlockRenderType.MODEL;
        private ShapeFactory shapeFactory =
                (state, world, pos, context) -> VoxelShapes.fullCube();
        private final StateProperties stateProperties = new StateProperties();
        private BiPredicate<WorldView, BlockPos> canExistAt = (world, pos) -> true;
        private final Map<Direction, VoxelShape> cachedDirectionalShapes = new HashMap<>();

        public BlockProperties hasHorizontalFacing() {
            return hasHorizontalFacing(true);
        }

        public BlockProperties hasHorizontalFacing(boolean hasHorizontalFacing) {
            if (hasHorizontalFacing) {
                this.stateProperties.addHorizontalFacing();
            }

            return this;
        }

        public BlockProperties defaultDirection(Direction defaultDirection) {
            this.stateProperties.setDefaultValue("facing", defaultDirection);
            return this;
        }

        public BlockProperties hasAxisProperty() {
            return hasAxisProperty(true);
        }

        public BlockProperties hasAxisProperty(boolean hasAxisProperty) {
            if (hasAxisProperty) {
                this.stateProperties.addAxis();
            }

            return this;
        }

        public BlockProperties defaultAxis(Direction.Axis defaultAxis) {
            this.stateProperties.setDefaultValue("axis", defaultAxis);
            return this;
        }

        public BlockProperties hasLitProperty() {
            return hasLitProperty(true);
        }

        public BlockProperties hasLitProperty(boolean hasLitProperty) {
            if (hasLitProperty) {
                this.stateProperties.addLit();
            }

            return this;
        }

        public BlockProperties defaultLit(boolean defaultLit) {
            this.stateProperties.setDefaultValue("lit", defaultLit);
            return this;
        }

        public BlockProperties hasWaterloggableProperty() {
            return hasWaterloggableProperty(true);
        }

        public BlockProperties hasWaterloggableProperty(boolean hasWaterloggableProperty) {
            if (hasWaterloggableProperty) {
                this.stateProperties.addWaterlogged();
            }

            return this;
        }

        public BlockProperties defaultWaterlogged(boolean defaultWaterlogged) {
            this.stateProperties.setDefaultValue("waterlogged", defaultWaterlogged);
            return this;
        }

        public <T extends Comparable<T>> BlockProperties addStateProperty(Property<T> property, T defaultValue) {
            this.stateProperties.addProperty(new StateProperty<>(property, defaultValue));
            return this;
        }

        public BlockProperties addBooleanStateProperty(String name, boolean defaultValue) {
            this.stateProperties.addProperty(new StateProperty<>(BooleanProperty.of(name), defaultValue));
            return this;
        }

        public <T extends Enum<T> & StringIdentifiable> BlockProperties addEnumStateProperty(String name, Class<T> clazz, T defaultValue) {
            this.stateProperties.addProperty(new StateProperty<>(EnumProperty.of(name, clazz), defaultValue));
            return this;
        }

        public <T extends Enum<T> & StringIdentifiable> BlockProperties addEnumStateProperty(String name, Class<T> clazz, T defaultValue, List<T> values) {
            this.stateProperties.addProperty(new StateProperty<>(EnumProperty.of(name, clazz, values), defaultValue));
            return this;
        }

        public BlockProperties notPlaceFacingOpposite() {
            return placeFacingOpposite(false);
        }

        public BlockProperties placeFacingOpposite(boolean placeFacingOpposite) {
            this.placeFacingOpposite = placeFacingOpposite;
            return this;
        }

        public BlockProperties blockEntityProperties(BlockBlockEntityProperties<?> blockEntityProperties) {
            this.blockEntityProperties = blockEntityProperties;
            return this;
        }

        public BlockProperties hasComparatorOutput() {
            return hasComparatorOutput(true);
        }

        public BlockProperties hasComparatorOutput(boolean hasComparatorOutput) {
            this.hasComparatorOutput = hasComparatorOutput;
            return this;
        }

        public BlockProperties comparatorOutput(Function4<BlockState, World, BlockPos, Direction, Integer> comparatorOutput) {
            this.comparatorOutput = comparatorOutput;
            return this;
        }

        public BlockProperties hasBlockEntityRenderer() {
            return renderType(BlockRenderType.INVISIBLE);
        }

        public BlockProperties renderType(BlockRenderType renderType) {
            this.renderType = renderType;
            return this;
        }

        public BlockProperties emptyShape() {
            return constantShape(VoxelShapes.empty());
        }

        public BlockProperties constantShape(VoxelShape shape) {
            return shapeFactory((state, world, pos, context) -> shape);
        }

        public BlockProperties shapeFactory(ShapeFactory shapeFactory) {
            this.shapeFactory = shapeFactory;
            return this;
        }

        public BlockProperties canExistAt(BiPredicate<WorldView, BlockPos> canExistAt) {
            this.canExistAt = canExistAt;
            return this;
        }

        public BlockProperties useRotatedShapes(VoxelShape shape) {
            if (!this.stateProperties.containsProperty(Properties.HORIZONTAL_FACING)) {
                hasHorizontalFacing();
            }

            if (this.cachedDirectionalShapes.isEmpty()) {
                runShapeCalculation(this.cachedDirectionalShapes, shape);
            }

            return shapeFactory((state, world, pos, context) -> this.cachedDirectionalShapes.get(state.get(Properties.HORIZONTAL_FACING)));
        }

        public static void runShapeCalculation(Map<Direction, VoxelShape> shapeCache, VoxelShape shape) {
            for (final Direction direction : Direction.values()) {
                shapeCache.put(direction, calculateShape(direction, shape));
            }
        }

        public static VoxelShape calculateShape(Direction to, VoxelShape shape) {
            final VoxelShape[] buffer = {shape, VoxelShapes.empty()};

            final int times = (to.getHorizontalQuarterTurns() - Direction.NORTH.getHorizontalQuarterTurns() + 4) % 4;
            for (int i = 0; i < times; i++) {
                buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) ->
                        buffer[1] = VoxelShapes.union(buffer[1],
                                VoxelShapes.cuboid(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
                buffer[0] = buffer[1];
                buffer[1] = VoxelShapes.empty();
            }

            return buffer[0];
        }

        public static class BlockBlockEntityProperties<T extends BlockEntity> {
            public final Supplier<BlockEntityType<T>> blockEntityTypeSupplier;
            public boolean shouldTick = false;
            public BlockEntityFactory<T> blockEntityFactory;
            public BlockEntityTickerFactory<T> blockEntityTicker = (world, state, type) -> TickableBlockEntity.createTicker(world);
            public MultiblockProperties<T> multiblockProperties;
            public boolean rightClickToOpenGui = false;
            public boolean dropContentsOnBreak = false;

            public BlockBlockEntityProperties(Supplier<BlockEntityType<T>> blockEntityTypeSupplier) {
                this.blockEntityTypeSupplier = blockEntityTypeSupplier;
                this.blockEntityFactory = (pos, state) -> this.blockEntityTypeSupplier.get().instantiate(pos, state);
            }

            public BlockBlockEntityProperties<T> shouldTick() {
                return shouldTick(true);
            }

            public BlockBlockEntityProperties<T> shouldTick(boolean shouldTick) {
                this.shouldTick = shouldTick;
                return this;
            }

            public BlockBlockEntityProperties<T> shouldTickAllowClient(boolean allowClient) {
                this.shouldTick = true;
                this.blockEntityTicker = (world, state, type) -> TickableBlockEntity.createTicker(world, allowClient);
                return this;
            }

            public BlockBlockEntityProperties<T> blockEntityFactory(BlockEntityFactory<T> blockEntityFactory) {
                this.blockEntityFactory = blockEntityFactory;
                return this;
            }

            public BlockBlockEntityProperties<T> blockEntityTickerFactory(BlockEntityTickerFactory<T> blockEntityTicker) {
                this.blockEntityTicker = blockEntityTicker;
                return this;
            }

            public MultiblockProperties<T> multiblockProperties(MultiblockType<T> type) {
                return new MultiblockProperties<>(this, type);
            }

            public BlockBlockEntityProperties<T> rightClickToOpenGui() {
                return rightClickToOpenGui(true);
            }

            public BlockBlockEntityProperties<T> rightClickToOpenGui(boolean rightClickToOpenGui) {
                this.rightClickToOpenGui = rightClickToOpenGui;
                return this;
            }

            public BlockBlockEntityProperties<T> dropContentsOnBreak() {
                return dropContentsOnBreak(true);
            }

            public BlockBlockEntityProperties<T> dropContentsOnBreak(boolean dropContentsOnBreak) {
                this.dropContentsOnBreak = dropContentsOnBreak;
                return this;
            }

            public static class MultiblockProperties<T extends BlockEntity> {
                private final BlockBlockEntityProperties<T> blockEntityProperties;
                private final MultiblockType<T> type;

                public MultiblockProperties(BlockBlockEntityProperties<T> blockEntityProperties, MultiblockType<T> type) {
                    this.blockEntityProperties = blockEntityProperties;
                    this.type = type;
                }

                public BlockBlockEntityProperties<T> build() {
                    this.blockEntityProperties.multiblockProperties = this;
                    return this.blockEntityProperties;
                }
            }
        }
    }

    @FunctionalInterface
    public interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    public interface BlockEntityTickerFactory<T extends BlockEntity> {
        BlockEntityTicker<T> create(World world, BlockState state, BlockEntityType<?> type);
    }

    @FunctionalInterface
    public interface ShapeFactory {
        VoxelShape create(BlockState state, BlockView world, BlockPos pos, ShapeContext context);
    }
}