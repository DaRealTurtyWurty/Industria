package dev.turtywurty.industria.multiblock;

import dev.turtywurty.industria.blockentity.OilPumpJackBlockEntity;
import dev.turtywurty.industria.util.QuadConsumer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Locale;
import java.util.function.BiConsumer;

public enum MultiblockType {
    OIL_PUMP_JACK(123, (world, player, hitResult, pos) -> {
        if(world.getBlockEntity(pos) instanceof OilPumpJackBlockEntity oilPumpJack) {
            player.openHandledScreen(oilPumpJack);
        }
    }, (world, pos) -> {
        if (world.getBlockEntity(pos) instanceof OilPumpJackBlockEntity oilPumpJack) {
            oilPumpJack.breakMachine(world, pos);
        }
    });

    private final QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse;
    private final BiConsumer<World, BlockPos> onMultiblockBreak;
    private final boolean hasDirectionProperty; // Default: true
    private final int numBlocks;

    MultiblockType(boolean hasDirectionProperty, int numBlocks, QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse, BiConsumer<World, BlockPos> onMultiblockBreak) {
        this.hasDirectionProperty = hasDirectionProperty;
        this.numBlocks = numBlocks;
        this.onPrimaryBlockUse = onPrimaryBlockUse;
        this.onMultiblockBreak = onMultiblockBreak;
    }

    MultiblockType(int numBlocks, QuadConsumer<World, PlayerEntity, BlockHitResult, BlockPos> onPrimaryBlockUse, BiConsumer<World, BlockPos> onMultiblockBreak) {
        this(true, numBlocks, onPrimaryBlockUse, onMultiblockBreak);
    }

    public void onPrimaryBlockUse(World world, PlayerEntity player, BlockHitResult hitResult, BlockPos pos) {
        this.onPrimaryBlockUse.accept(world, player, hitResult, pos);
    }

    public void onMultiblockBreak(World world, BlockPos pos) {
        this.onMultiblockBreak.accept(world, pos);
    }

    public boolean hasDirectionProperty() {
        return this.hasDirectionProperty;
    }

    public int numBlocks() {
        return this.numBlocks;
    }

    public static MultiblockType fromString(String string) {
        for (MultiblockType type : MultiblockType.values()) {
            if (type.name().equalsIgnoreCase(string)) {
                return type;
            }
        }

        return null;
    }

    public static String toString(MultiblockType type) {
        return type.name().toLowerCase(Locale.ROOT);
    }
}
