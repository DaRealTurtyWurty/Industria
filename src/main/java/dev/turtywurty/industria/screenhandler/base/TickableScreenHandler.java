package dev.turtywurty.industria.screenhandler.base;

import net.minecraft.server.network.ServerPlayerEntity;

public interface TickableScreenHandler {
    void tick(ServerPlayerEntity player);
}
