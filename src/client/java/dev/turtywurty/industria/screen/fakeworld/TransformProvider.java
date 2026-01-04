package dev.turtywurty.industria.screen.fakeworld;

import net.minecraft.client.multiplayer.ClientLevel;

@FunctionalInterface
public interface TransformProvider {
    Transform get(float tickDelta, ClientLevel world);

    static TransformProvider constant(Transform transform) {
        return (tickDelta, world) -> transform == null ? Transform.IDENTITY : transform;
    }
}
