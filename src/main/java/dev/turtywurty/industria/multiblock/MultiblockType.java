package dev.turtywurty.industria.multiblock;

import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.blockentity.OilPumpJackBlockEntity;
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
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.Locale;
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
public enum MultiblockType {
    OIL_PUMP_JACK(123, (world, player, hitResult, pos) -> {
        if (world.getBlockEntity(pos) instanceof OilPumpJackBlockEntity oilPumpJack) {
            player.openHandledScreen(oilPumpJack);
        }
    }, (world, pos) -> {
        if (world.getBlockEntity(pos) instanceof OilPumpJackBlockEntity oilPumpJack) {
            oilPumpJack.breakMultiblock(world, pos);
        }
    }),
    DRILL(false, 26, (world, player, hitResult, pos) -> {
        if (world.getBlockEntity(pos) instanceof DrillBlockEntity drill) {
            player.openHandledScreen(drill);
        }
    }, (world, pos) -> {
        if (world.getBlockEntity(pos) instanceof DrillBlockEntity drill) {
            drill.breakMultiblock(world, pos);
        }
    });

    private final QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse;
    private final BiConsumer<World, BlockPos> onMultiblockBreak;
    private final boolean hasDirectionProperty; // Default: true
    private final int numBlocks;
    private final BiFunction<BlockEntity, @Nullable Direction, EnergyStorage> energyProvider;
    private final BiFunction<BlockEntity, @Nullable Direction, InventoryStorage> inventoryProvider;
    private final BiFunction<BlockEntity, @Nullable Direction, Storage<FluidVariant>> fluidProvider;

    /**
     * @param hasDirectionProperty Whether the multiblock has a direction property
     * @param numBlocks            The number of blocks in the multiblock
     * @param onPrimaryBlockUse    The action to perform when the primary block is used
     * @param onMultiblockBreak    The action to perform when the multiblock is broken
     */
    MultiblockType(boolean hasDirectionProperty, int numBlocks, QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse, BiConsumer<World, BlockPos> onMultiblockBreak) {
        this(hasDirectionProperty, numBlocks, onPrimaryBlockUse, onMultiblockBreak, (blockEntity, direction) -> null, (blockEntity, direction) -> null, (blockEntity, direction) -> null);
    }

    /**
     * @param hasDirectionProperty Whether the multiblock has a direction property
     * @param numBlocks            The number of blocks in the multiblock
     * @param onPrimaryBlockUse    The action to perform when the primary block is used
     * @param onMultiblockBreak    The action to perform when the multiblock is broken
     * @param energyProvider       The energy provider for the multiblock
     * @param inventoryProvider    The inventory provider for the multiblock
     * @param fluidProvider        The fluid provider for the multiblock
     */
    MultiblockType(boolean hasDirectionProperty, int numBlocks, QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse, BiConsumer<World, BlockPos> onMultiblockBreak, BiFunction<BlockEntity, @Nullable Direction, EnergyStorage> energyProvider, BiFunction<BlockEntity, @Nullable Direction, InventoryStorage> inventoryProvider, BiFunction<BlockEntity, @Nullable Direction, Storage<FluidVariant>> fluidProvider) {
        this.hasDirectionProperty = hasDirectionProperty;
        this.numBlocks = numBlocks;
        this.onPrimaryBlockUse = onPrimaryBlockUse;
        this.onMultiblockBreak = onMultiblockBreak;
        this.energyProvider = energyProvider;
        this.inventoryProvider = inventoryProvider;
        this.fluidProvider = fluidProvider;
    }

    /**
     * This constructor defaults to having {@link MultiblockType#hasDirectionProperty} set to {@code true}
     *
     * @param numBlocks         The number of blocks in the multiblock
     * @param onPrimaryBlockUse The action to perform when the primary block is used
     * @param onMultiblockBreak The action to perform when the multiblock is broken
     */
    MultiblockType(int numBlocks, QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse, BiConsumer<World, BlockPos> onMultiblockBreak) {
        this(true, numBlocks, onPrimaryBlockUse, onMultiblockBreak);
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

    /**
     * Gets the {@link MultiblockType} from the given string
     *
     * @param string The string to get the {@link MultiblockType} from
     * @return The {@link MultiblockType} from the given string
     */
    public static MultiblockType fromString(String string) {
        for (MultiblockType type : MultiblockType.values()) {
            if (type.name().equalsIgnoreCase(string)) {
                return type;
            }
        }

        return null;
    }

    /**
     * Converts the given {@link MultiblockType} to a string
     *
     * @param type The {@link MultiblockType} to convert
     * @return The string representation of the given {@link MultiblockType}
     */
    public static String toString(MultiblockType type) {
        return type.name().toLowerCase(Locale.ROOT);
    }

    public @Nullable EnergyStorage getEnergyProvider(BlockEntity blockEntity, @Nullable Direction direction) {
        return this.energyProvider.apply(blockEntity, direction);
    }

    public @Nullable InventoryStorage getInventoryProvider(BlockEntity blockEntity, @Nullable Direction direction) {
        return this.inventoryProvider.apply(blockEntity, direction);
    }

    public @Nullable Storage<FluidVariant> getFluidProvider(BlockEntity blockEntity, @Nullable Direction direction) {
        return this.fluidProvider.apply(blockEntity, direction);
    }
}
