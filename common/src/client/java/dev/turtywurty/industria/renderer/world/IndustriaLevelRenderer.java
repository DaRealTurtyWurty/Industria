package dev.turtywurty.industria.renderer.world;

import dev.turtywurty.turtymultiloader.event.client.LevelRenderContext;

@FunctionalInterface
public interface IndustriaLevelRenderer {
    void render(LevelRenderContext context);
}
