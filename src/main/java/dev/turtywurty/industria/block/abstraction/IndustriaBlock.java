package dev.turtywurty.industria.block.abstraction;

import com.mojang.datafixers.util.Function4;
import dev.turtywurty.industria.block.abstraction.state.StateProperties;
import dev.turtywurty.industria.block.abstraction.state.StateProperty;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockable;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public class IndustriaBlock extends Block implements EntityBlock {
    public final StateProperties stateProperties;
    public final boolean placeFacingOpposite;
    public final Supplier<BlockEntityType<?>> blockEntityTypeSupplier;
    public final boolean shouldTick;
    public final BlockEntityFactory<?> blockEntityFactory;
    public final BlockEntityTickerFactory<?> blockEntityTicker;
    public final boolean hasComparatorOutput;
    public final Function4<BlockState, Level, BlockPos, Direction, Integer> comparatorOutput;
    public final RenderShape renderType;
    public final ShapeFactory shapeFactory;
    public final MultiblockType<?> multiblockType;
    public final boolean rightClickToOpenGui;
    public final BiPredicate<LevelReader, BlockPos> canExistAt;
    public final Map<Direction, VoxelShape> cachedDirectionalShapes;
    public final boolean dropContentsOnBreak;

    @SuppressWarnings("unchecked")
    public IndustriaBlock(Properties settings, BlockProperties properties) {
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

        StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(this);
        createBlockStateDefinition(builder);
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
        registerDefaultState(this.stateDefinition.any());

        BlockState state = this.stateDefinition.any();
        state = this.stateProperties.applyDefaults(state);
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        if (this.stateProperties != null) {
            this.stateProperties.addToBuilder(builder);
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        if (this.stateProperties.containsProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
        }

        return super.rotate(state, rotation);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        if (this.stateProperties.containsProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, mirror.mirror(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
        }

        return super.mirror(state, mirror);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (state == null)
            return null;

        if (this.stateProperties.containsProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            Direction facing = ctx.getHorizontalDirection();
            if (this.placeFacingOpposite) {
                facing = facing.getOpposite();
            }

            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        }

        if (this.stateProperties.containsProperty(BlockStateProperties.AXIS)) {
            state = state.setValue(BlockStateProperties.AXIS, ctx.getClickedFace().getAxis());
        }

        return state;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.blockEntityTypeSupplier != null ? this.blockEntityFactory.create(pos, state) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return this.shouldTick ? (BlockEntityTicker<T>) this.blockEntityTicker.create(world, state, type) : EntityBlock.super.getTicker(world, state, type);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return this.hasComparatorOutput;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, Direction direction) {
        return this.hasComparatorOutput ? this.comparatorOutput.apply(state, world, pos, direction) : super.getAnalogOutputSignal(state, world, pos, direction);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return this.renderType;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return this.shapeFactory.create(state, world, pos, context);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (this.multiblockType != null) {
            if (!world.isClientSide()) {
                this.multiblockType.onPrimaryBlockUse(world, player, hit, pos);
            }

            return InteractionResult.SUCCESS;
        }

        if (this.rightClickToOpenGui) {
            if (!world.isClientSide()) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (player instanceof ServerPlayer sPlayer && blockEntity instanceof BlockEntityWithGui<?> blockEntityWithGui) { // TODO: Replace with component access maybe?
                    sPlayer.openMenu(blockEntityWithGui);
                }
            }

            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (this.multiblockType != null) {
            if (!world.isClientSide()) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof AutoMultiblockable multiblockable) {
                    multiblockable.buildMultiblock(world, pos, state, placer, itemStack, blockEntity::setChanged);
                }
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return this.canExistAt.test(world, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return !state.canSurvive(world, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    public static class BlockProperties {
        private boolean placeFacingOpposite = true;
        private BlockBlockEntityProperties<?> blockEntityProperties;
        private boolean hasComparatorOutput = false;
        private Function4<BlockState, Level, BlockPos, Direction, Integer> comparatorOutput =
                (state, world, pos, direction) -> AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
        private RenderShape renderType = RenderShape.MODEL;
        private ShapeFactory shapeFactory =
                (state, world, pos, context) -> Shapes.block();
        private final StateProperties stateProperties = new StateProperties();
        private BiPredicate<LevelReader, BlockPos> canExistAt = (world, pos) -> true;
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
            this.stateProperties.addProperty(new StateProperty<>(BooleanProperty.create(name), defaultValue));
            return this;
        }

        public <T extends Enum<T> & StringRepresentable> BlockProperties addEnumStateProperty(String name, Class<T> clazz, T defaultValue) {
            this.stateProperties.addProperty(new StateProperty<>(EnumProperty.create(name, clazz), defaultValue));
            return this;
        }

        public <T extends Enum<T> & StringRepresentable> BlockProperties addEnumStateProperty(String name, Class<T> clazz, T defaultValue, List<T> values) {
            this.stateProperties.addProperty(new StateProperty<>(EnumProperty.create(name, clazz, values), defaultValue));
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

        public BlockProperties comparatorOutput(Function4<BlockState, Level, BlockPos, Direction, Integer> comparatorOutput) {
            this.comparatorOutput = comparatorOutput;
            return this;
        }

        public BlockProperties hasBlockEntityRenderer() {
            return renderType(RenderShape.INVISIBLE);
        }

        public BlockProperties renderType(RenderShape renderType) {
            this.renderType = renderType;
            return this;
        }

        public BlockProperties emptyShape() {
            return constantShape(Shapes.empty());
        }

        public BlockProperties constantShape(VoxelShape shape) {
            return shapeFactory((state, world, pos, context) -> shape);
        }

        public BlockProperties shapeFactory(ShapeFactory shapeFactory) {
            this.shapeFactory = shapeFactory;
            return this;
        }

        public BlockProperties canExistAt(BiPredicate<LevelReader, BlockPos> canExistAt) {
            this.canExistAt = canExistAt;
            return this;
        }

        public BlockProperties useRotatedShapes(VoxelShape shape) {
            if (!this.stateProperties.containsProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                hasHorizontalFacing();
            }

            if (this.cachedDirectionalShapes.isEmpty()) {
                runShapeCalculation(this.cachedDirectionalShapes, shape);
            }

            return shapeFactory((state, world, pos, context) -> this.cachedDirectionalShapes.get(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
        }

        public static void runShapeCalculation(Map<Direction, VoxelShape> shapeCache, VoxelShape shape) {
            for (final Direction direction : Direction.values()) {
                shapeCache.put(direction, calculateShape(direction, shape));
            }
        }

        public static VoxelShape calculateShape(Direction to, VoxelShape shape) {
            final VoxelShape[] buffer = {shape, Shapes.empty()};

            final int times = (to.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
            for (int i = 0; i < times; i++) {
                buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                        buffer[1] = Shapes.or(buffer[1],
                                Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
                buffer[0] = buffer[1];
                buffer[1] = Shapes.empty();
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
                this.blockEntityFactory = (pos, state) -> this.blockEntityTypeSupplier.get().create(pos, state);
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
        BlockEntityTicker<T> create(Level world, BlockState state, BlockEntityType<?> type);
    }

    @FunctionalInterface
    public interface ShapeFactory {
        VoxelShape create(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context);
    }
}