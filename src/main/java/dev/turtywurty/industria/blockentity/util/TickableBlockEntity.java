package dev.turtywurty.industria.blockentity.util;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface TickableBlockEntity {
    void tick();

    static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level world) {
        return !world.isClientSide() ? (world0, blockPos, blockState, blockEntity) -> ((TickableBlockEntity) blockEntity).tick() : null;
    }

    static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level world, boolean allowClient) {
        if (allowClient) {
            return (world0, blockPos, blockState, blockEntity) -> ((TickableBlockEntity) blockEntity).tick();
        } else {
            return createTicker(world);
        }
    }
}
