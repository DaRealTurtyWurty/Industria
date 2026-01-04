package dev.turtywurty.industria.screenhandler.base;

import net.minecraft.server.level.ServerPlayer;

public interface TickableScreenHandler {
    void tick(ServerPlayer player);
}
