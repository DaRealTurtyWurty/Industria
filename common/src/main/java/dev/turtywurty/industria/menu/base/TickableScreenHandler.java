package dev.turtywurty.industria.menu.base;

import net.minecraft.server.level.ServerPlayer;

public interface TickableScreenHandler {
    void tick(ServerPlayer player);
}
