package dev.turtywurty.industria.multiblock.old;

import com.mojang.datafixers.util.Function3;
import dev.turtywurty.industria.util.QuadConsumer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Represents a type of multiblock that can be created and provides information such as:
 * <ul>
 * <li>The number of blocks in the multiblock</li>
 * <li>Whether the multiblock has a direction property</li>
 * <li>The action to perform when the primary block is used</li>
 * <li>The action to perform when the multiblock is broken</li>
 * </ul>
 */
public record MultiblockType<T extends BlockEntity>(boolean hasDirectionProperty, int numBlocks,
                                                    QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse,
                                                    BiConsumer<World, BlockPos> onMultiblockBreak,
                                                    Function3<WorldView, BlockPos, Direction, VoxelShape> shapeFactory) {
    /**
     * @param hasDirectionProperty Whether the multiblock has a direction property
     * @param numBlocks            The number of blocks in the multiblock
     * @param onPrimaryBlockUse    The action to perform when the primary block is used
     * @param onMultiblockBreak    The action to perform when the multiblock is broken
     */
    public MultiblockType {
    }

    /**
     * Performs the action when the primary block is used
     *
     * @param world     The world in which the block is located
     * @param player    The player who used the block
     * @param hitResult The result of the block hit
     * @param pos       The position of the block
     */
    public void onPrimaryBlockUse(World world, PlayerEntity player, BlockHitResult hitResult, BlockPos pos) {
        this.onPrimaryBlockUse.accept(world, player, hitResult, pos);
    }

    /**
     * Performs the action when the multiblock is broken
     *
     * @param world The world in which the block is located
     * @param pos   The position of the block
     */
    public void onMultiblockBreak(World world, BlockPos pos) {
        this.onMultiblockBreak.accept(world, pos);
    }

    /**
     * Checks if the multiblock has a direction property
     *
     * @return {@code true} if the multiblock has a direction property, {@code false} otherwise
     */
    @Override
    public boolean hasDirectionProperty() {
        return this.hasDirectionProperty;
    }

    /**
     * Gets the minimum number of blocks in the multiblock
     *
     * @return The minimum number of blocks in the multiblock
     * @apiNote This method should return the minimum number of blocks, not the maximum
     */
    @Override
    public int numBlocks() {
        return this.numBlocks;
    }

    /**
     * Gets the shape of the multiblock at the specified position and direction
     *
     * @param world     The world in which the block is located
     * @param pos       The position of the block
     * @param direction The direction of the block
     * @return The shape of the multiblock at the specified position and direction
     */
    public VoxelShape getShape(WorldView world, BlockPos pos, Direction direction) {
        return this.shapeFactory.apply(world, pos, direction);
    }

    /**
     * Builder for creating instances of {@link MultiblockType}.
     *
     * @param <T> The type of BlockEntity associated with the multiblock.
     */
    public static class Builder<T extends BlockEntity> {
        private final int numBlocks;
        private boolean hasDirectionProperty = true;
        private QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse = (world, player, hitResult, pos) -> {
            if (world.getBlockEntity(pos) instanceof NamedScreenHandlerFactory blockEntity) {
                player.openHandledScreen(blockEntity);
            }
        };

        private BiConsumer<World, BlockPos> onMultiblockBreak = (world, pos) -> {
            if (world.getBlockEntity(pos) instanceof Multiblockable blockEntity) {
                blockEntity.onMultiblockBreak(world, pos);
            }
        };

        private final Map<Direction, VoxelShape> shapes = new HashMap<>();
        private Function3<WorldView, BlockPos, Direction, VoxelShape> shapeFactory =
                (world, pos, direction) -> shapes.get(direction);

        /**
         * Constructs a new Builder with the specified number of blocks.
         *
         * @param numBlocks The number of blocks in the multiblock.
         */
        public Builder(int numBlocks) {
            this.numBlocks = numBlocks;
        }

        /**
         * Sets whether the multiblock has a direction property.
         *
         * @param hasDirectionProperty Whether the multiblock has a direction property.
         * @return This Builder instance for method chaining.
         */
        public Builder<T> setHasDirectionProperty(boolean hasDirectionProperty) {
            this.hasDirectionProperty = hasDirectionProperty;
            return this;
        }

        /**
         * Sets the action to perform when the primary block is used.
         *
         * @param onPrimaryBlockUse The action to perform when the primary block is used.
         * @return This Builder instance for method chaining.
         */
        public Builder<T> setOnPrimaryBlockUse(@NotNull QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse) {
            this.onPrimaryBlockUse = onPrimaryBlockUse;
            return this;
        }

        /**
         * Sets the action to perform when the multiblock is broken.
         *
         * @param onMultiblockBreak The action to perform when the multiblock is broken.
         * @return This Builder instance for method chaining.
         */
        public Builder<T> setOnMultiblockBreak(@NotNull BiConsumer<World, BlockPos> onMultiblockBreak) {
            this.onMultiblockBreak = onMultiblockBreak;
            return this;
        }

        /**
         * Adds a shape for a specific direction.
         *
         * @param direction The direction for which to add the shape.
         * @param shape     The VoxelShape to associate with the direction.
         * @return This Builder instance for method chaining.
         */
        public Builder<T> shape(Direction direction, VoxelShape shape) {
            this.shapes.put(direction, shape);
            return this;
        }

        /**
         * Adds the same shape for all directions.
         *
         * @param shape The VoxelShape to associate with all directions.
         * @return This Builder instance for method chaining.
         */
        public Builder<T> shapes(VoxelShape shape) {
            for (Direction direction : Direction.values()) {
                shape(direction, shape);
            }

            return this;
        }

        /**
         * Sets the shape factory for the multiblock.
         *
         * @param shapeFactory A function that takes a WorldView, BlockPos, and Direction and returns a VoxelShape.
         * @return This Builder instance for method chaining.
         */
        public Builder<T> shapeFactory(Function3<WorldView, BlockPos, Direction, VoxelShape> shapeFactory) {
            this.shapeFactory = shapeFactory;
            return this;
        }

        /**
         * Builds a new instance of {@link MultiblockType} with the specified properties.
         *
         * @return A new instance of {@link MultiblockType}.
         */
        public MultiblockType<T> build() {
            return new MultiblockType<>(this.hasDirectionProperty, this.numBlocks, this.onPrimaryBlockUse, this.onMultiblockBreak, this.shapeFactory);
        }
    }
}
