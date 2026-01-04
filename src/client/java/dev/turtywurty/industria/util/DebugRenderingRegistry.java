package dev.turtywurty.industria.util;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.KeyBindingInit;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class DebugRenderingRegistry {
    public static boolean debugRendering = false;

    public static void init() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            debugRendering = false;
        });

        KeyMapping toggleDebugRenderingKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key." + Industria.MOD_ID + ".toggle_debug_rendering",
                GLFW.GLFW_KEY_F6,
                KeyBindingInit.CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleDebugRenderingKey.consumeClick()) {
                debugRendering = !debugRendering;
                LocalPlayer player = client.player;
                if (player == null) {
                    Industria.LOGGER.warn("Tried to send debug rendering toggle message, but player was null!");
                    return;
                }

                player.displayClientMessage(Component.literal("Debug rendering: " + (debugRendering ? "[ENABLED]" : "[DISABLED]")), false);
            }
        });
    }
}
