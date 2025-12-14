package dev.turtywurty.industria.screen.fakeworld;

import net.minecraft.client.world.ClientWorld;

@FunctionalInterface
public interface TransformProvider {
    Transform get(float tickDelta, ClientWorld world);

    static TransformProvider constant(Transform transform) {
        return (tickDelta, world) -> transform == null ? Transform.IDENTITY : transform;
    }
}
