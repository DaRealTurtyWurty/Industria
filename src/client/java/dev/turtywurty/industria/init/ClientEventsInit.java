package dev.turtywurty.industria.init;

import dev.turtywurty.industria.multiblock.old.AutoMultiblockBlock;
import dev.turtywurty.industria.renderer.block.RotaryKilnBlockEntityRenderer;
import dev.turtywurty.industria.renderer.world.FluidPocketWorldRenderer;
import dev.turtywurty.industria.renderer.world.PipeNetworkWorldRenderer;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldScreen;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ClientEventsInit {
    public static void init() {
        ClientTickEvents.START_WORLD_TICK.register(world ->
                AutoMultiblockBlock.SHAPE_CACHE.clear());

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) ->
                RotaryKilnBlockEntityRenderer.BLOCK_POS_RENDERER_DATA_MAP.clear());

        var fluidPocketWorldRenderer = new FluidPocketWorldRenderer();
        WorldRenderEvents.AFTER_ENTITIES.register(fluidPocketWorldRenderer::render);

        var pipeNetworkWorldRenderer = new PipeNetworkWorldRenderer();
        WorldRenderEvents.AFTER_ENTITIES.register(pipeNetworkWorldRenderer::render);

        KeyBinding fakeWorldPreview = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.industria.fake_world",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F9,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (fakeWorldPreview.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new FakeWorldScreen());
                }
            }
        });
    }
}
