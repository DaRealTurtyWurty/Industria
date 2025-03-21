package dev.turtywurty.industria.renderer.world;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

@FunctionalInterface
public interface IndustriaWorldRenderer {
    void render(WorldRenderContext context);
}
