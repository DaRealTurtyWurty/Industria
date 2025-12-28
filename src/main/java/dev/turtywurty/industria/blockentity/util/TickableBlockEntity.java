package dev.turtywurty.industria.blockentity.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.world.World;

public interface TickableBlockEntity {
    void tick();

    static <T extends BlockEntity> BlockEntityTicker<T> createTicker(World world) {
        return !world.isClient() ? (world0, blockPos, blockState, blockEntity) -> ((TickableBlockEntity) blockEntity).tick() : null;
    }

    static <T extends BlockEntity> BlockEntityTicker<T> createTicker(World world, boolean allowClient) {
        if (allowClient) {
            return (world0, blockPos, blockState, blockEntity) -> ((TickableBlockEntity) blockEntity).tick();
        } else {
            return createTicker(world);
        }
    }
}
