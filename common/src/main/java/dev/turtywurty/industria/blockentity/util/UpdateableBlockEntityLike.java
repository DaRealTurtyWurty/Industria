package dev.turtywurty.industria.blockentity.util;

public interface UpdateableBlockEntityLike {
    void update();

    boolean shouldWaitForEndTick();

    void endTick();

    void forceUpdate();
}
