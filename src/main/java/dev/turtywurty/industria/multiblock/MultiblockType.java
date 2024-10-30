package dev.turtywurty.industria.multiblock;

import dev.turtywurty.industria.util.QuadConsumer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

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
    private final BiFunction<T, @Nullable Direction, EnergyStorage> energyProvider;
    private final BiFunction<T, @Nullable Direction, InventoryStorage> inventoryProvider;
    private final BiFunction<T, @Nullable Direction, Storage<FluidVariant>> fluidProvider;

    /**
     * @param hasDirectionProperty Whether the multiblock has a direction property
     * @param numBlocks            The number of blocks in the multiblock
     * @param onPrimaryBlockUse    The action to perform when the primary block is used
     * @param onMultiblockBreak    The action to perform when the multiblock is broken
     * @param energyProvider       The energy provider for the multiblock
     * @param inventoryProvider    The inventory provider for the multiblock
     * @param fluidProvider        The fluid provider for the multiblock
     */
    private MultiblockType(boolean hasDirectionProperty, int numBlocks, QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse, BiConsumer<World, BlockPos> onMultiblockBreak, BiFunction<T, @Nullable Direction, EnergyStorage> energyProvider, BiFunction<T, @Nullable Direction, InventoryStorage> inventoryProvider, BiFunction<T, @Nullable Direction, Storage<FluidVariant>> fluidProvider) {
        this.hasDirectionProperty = hasDirectionProperty;
        this.numBlocks = numBlocks;
        this.onPrimaryBlockUse = onPrimaryBlockUse;
        this.onMultiblockBreak = onMultiblockBreak;
        this.energyProvider = energyProvider;
        this.inventoryProvider = inventoryProvider;
        this.fluidProvider = fluidProvider;
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

    public @Nullable EnergyStorage getEnergyProvider(T blockEntity, @Nullable Direction direction) {
        return this.energyProvider.apply(blockEntity, direction);
    }

    public @Nullable InventoryStorage getInventoryProvider(T blockEntity, @Nullable Direction direction) {
        return this.inventoryProvider.apply(blockEntity, direction);
    }

    public @Nullable Storage<FluidVariant> getFluidProvider(T blockEntity, @Nullable Direction direction) {
        return this.fluidProvider.apply(blockEntity, direction);
    }

    public static class Builder<T extends BlockEntity> {
        private final int numBlocks;
        private boolean hasDirectionProperty = true;
        private QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse = (world, player, hitResult, pos) -> {};
        private BiConsumer<World, BlockPos> onMultiblockBreak = (world, pos) -> {};
        private BiFunction<T, @Nullable Direction, EnergyStorage> energyProvider = (blockEntity, direction) -> null;
        private BiFunction<T, @Nullable Direction, InventoryStorage> inventoryProvider = (blockEntity, direction) -> null;
        private BiFunction<T, @Nullable Direction, Storage<FluidVariant>> fluidProvider = (blockEntity, direction) -> null;

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

        public Builder<T> setEnergyProvider(@NotNull BiFunction<T, @Nullable Direction, EnergyStorage> energyProvider) {
            this.energyProvider = energyProvider;
            return this;
        }

        public Builder<T> setInventoryProvider(@NotNull BiFunction<T, @Nullable Direction, InventoryStorage> inventoryProvider) {
            this.inventoryProvider = inventoryProvider;
            return this;
        }

        public Builder<T> setFluidProvider(@NotNull BiFunction<T, @Nullable Direction, Storage<FluidVariant>> fluidProvider) {
            this.fluidProvider = fluidProvider;
            return this;
        }

        public MultiblockType<T> build() {
            return new MultiblockType<>(this.hasDirectionProperty, this.numBlocks, this.onPrimaryBlockUse, this.onMultiblockBreak, this.energyProvider, this.inventoryProvider, this.fluidProvider);
        }
    }
}
