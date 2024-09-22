package dev.turtywurty.industria.multiblock;

import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.blockentity.OilPumpJackBlockEntity;
import dev.turtywurty.industria.util.QuadConsumer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Locale;
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
    DRILL(false, 27, (world, player, hitResult, pos) -> {
        // NO-OP
    }, (world, pos) -> {
        if (world.getBlockEntity(pos) instanceof DrillBlockEntity drill) {
            drill.breakMultiblock(world, pos);
        }
    });

    private final QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse;
    private final BiConsumer<World, BlockPos> onMultiblockBreak;
    private final boolean hasDirectionProperty; // Default: true
    private final int numBlocks;

    /**
     * @param hasDirectionProperty Whether the multiblock has a direction property
     * @param numBlocks            The number of blocks in the multiblock
     * @param onPrimaryBlockUse    The action to perform when the primary block is used
     * @param onMultiblockBreak    The action to perform when the multiblock is broken
     */
    MultiblockType(boolean hasDirectionProperty, int numBlocks, QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse, BiConsumer<World, BlockPos> onMultiblockBreak) {
        this.hasDirectionProperty = hasDirectionProperty;
        this.numBlocks = numBlocks;
        this.onPrimaryBlockUse = onPrimaryBlockUse;
        this.onMultiblockBreak = onMultiblockBreak;
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
}
