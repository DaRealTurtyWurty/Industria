package dev.turtywurty.industria.blockentity.util;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface TickableBlockEntity {
    static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level) {
        return !level.isClientSide() ? (_, _, _, blockEntity) -> ((TickableBlockEntity) blockEntity).tick() : null;
    }

    static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level level, boolean allowClient) {
        if (allowClient) {
            return (_, _, _, blockEntity) -> ((TickableBlockEntity) blockEntity).tick();
        } else {
            return createTicker(level);
        }
    }

    void tick();
}
