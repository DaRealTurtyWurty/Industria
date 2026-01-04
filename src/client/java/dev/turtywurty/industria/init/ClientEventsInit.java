package dev.turtywurty.industria.init;

import com.mojang.blaze3d.platform.InputConstants;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockBlock;
import dev.turtywurty.industria.renderer.block.RotaryKilnBlockEntityRenderer;
import dev.turtywurty.industria.renderer.world.FluidPocketWorldRenderer;
import dev.turtywurty.industria.renderer.world.PipeNetworkWorldRenderer;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ClientEventsInit {
    public static void init() {
        ClientTickEvents.START_LEVEL_TICK.register(world ->
                AutoMultiblockBlock.SHAPE_CACHE.clear());

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, world) ->
                RotaryKilnBlockEntityRenderer.BLOCK_POS_RENDERER_DATA_MAP.clear());

        var fluidPocketWorldRenderer = new FluidPocketWorldRenderer();
        LevelRenderEvents.AFTER_ENTITIES.register(fluidPocketWorldRenderer::render);

        var pipeNetworkWorldRenderer = new PipeNetworkWorldRenderer();
        LevelRenderEvents.AFTER_ENTITIES.register(pipeNetworkWorldRenderer::render);

        KeyMapping fakeWorldPreview = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.industria.fake_world",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F9,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (fakeWorldPreview.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new FakeWorldScreen());
                }
            }
        });
    }
}
