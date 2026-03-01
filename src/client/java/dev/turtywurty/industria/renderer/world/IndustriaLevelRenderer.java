package dev.turtywurty.industria.renderer.world;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;

@FunctionalInterface
public interface IndustriaLevelRenderer {
    void render(LevelRenderContext context);
}
