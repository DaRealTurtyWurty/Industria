package dev.turtywurty.industria.renderer.world;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;

@FunctionalInterface
public interface IndustriaWorldRenderer {
    void render(LevelRenderContext context);
}
