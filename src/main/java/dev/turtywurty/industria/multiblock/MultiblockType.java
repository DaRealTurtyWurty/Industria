package dev.turtywurty.industria.multiblock;

import dev.turtywurty.industria.util.QuadConsumer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
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
public class MultiblockType<T extends BlockEntity> {
    private final QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse;
    private final BiConsumer<World, BlockPos> onMultiblockBreak;
    private final boolean hasDirectionProperty; // Default: true
    private final int numBlocks;
    private final Map<Direction, VoxelShape> shapes;

    /**
     * @param hasDirectionProperty Whether the multiblock has a direction property
     * @param numBlocks            The number of blocks in the multiblock
     * @param onPrimaryBlockUse    The action to perform when the primary block is used
     * @param onMultiblockBreak    The action to perform when the multiblock is broken
     */
    private MultiblockType(boolean hasDirectionProperty, int numBlocks, QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse, BiConsumer<World, BlockPos> onMultiblockBreak, Map<Direction, VoxelShape> shapes) {
        this.hasDirectionProperty = hasDirectionProperty;
        this.numBlocks = numBlocks;
        this.onPrimaryBlockUse = onPrimaryBlockUse;
        this.onMultiblockBreak = onMultiblockBreak;
        this.shapes = shapes;
    }

    public void onPrimaryBlockUse(World world, PlayerEntity player, BlockHitResult hitResult, BlockPos pos) {
        this.onPrimaryBlockUse.accept(world, player, hitResult, pos);
    }

    public void onMultiblockBreak(World world, BlockPos pos) {
        this.onMultiblockBreak.accept(world, pos);
    }

    /**
     * Checks if the multiblock has a direction property
     *
     * @return {@code true} if the multiblock has a direction property, {@code false} otherwise
     */
    public boolean hasDirectionProperty() {
        return this.hasDirectionProperty;
    }

    /**
     * Gets the minimum number of blocks in the multiblock
     *
     * @return The minimum number of blocks in the multiblock
     * @apiNote This method should return the minimum number of blocks, not the maximum
     */
    public int numBlocks() {
        return this.numBlocks;
    }

    public VoxelShape getShape(Direction direction) {
        return this.shapes.get(direction);
    }

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
                blockEntity.breakMultiblock(world, pos);
            }
        };

        private final Map<Direction, VoxelShape> shapes = new HashMap<>();

        public Builder(int numBlocks) {
            this.numBlocks = numBlocks;
        }

        public Builder<T> setHasDirectionProperty(boolean hasDirectionProperty) {
            this.hasDirectionProperty = hasDirectionProperty;
            return this;
        }

        public Builder<T> setOnPrimaryBlockUse(@NotNull QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse) {
            this.onPrimaryBlockUse = onPrimaryBlockUse;
            return this;
        }

        public Builder<T> setOnMultiblockBreak(@NotNull BiConsumer<World, BlockPos> onMultiblockBreak) {
            this.onMultiblockBreak = onMultiblockBreak;
            return this;
        }

        public Builder<T> shape(Direction direction, VoxelShape shape) {
            this.shapes.put(direction, shape);
            return this;
        }

        public Builder<T> shapes(VoxelShape shape) {
            for (Direction direction : Direction.values()) {
                shape(direction, shape);
            }

            return this;
        }

        public MultiblockType<T> build() {
            return new MultiblockType<>(this.hasDirectionProperty, this.numBlocks, this.onPrimaryBlockUse, this.onMultiblockBreak, this.shapes);
        }
    }
}
