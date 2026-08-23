package dev.turtywurty.industria.util;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.ModKeyBindings;
import dev.turtywurty.turtymultiloader.event.client.ClientEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class DebugRenderingRegistry {
    public static boolean debugRendering = false;

    public static void init() {
        ClientEvents.onDisconnection(_ -> DebugRenderingRegistry.debugRendering = false);

        KeyMapping toggleDebugRenderingKey = ClientEvents.registerKeyMapping(new KeyMapping(
                "key." + Industria.MOD_ID + ".toggle_debug_rendering",
                GLFW.GLFW_KEY_F6,
                ModKeyBindings.CATEGORY
        ));

        ClientEvents.onEndClientTick(client -> {
            if (toggleDebugRenderingKey.consumeClick()) {
                debugRendering = !debugRendering;
                LocalPlayer player = client.player;
                if (player == null) {
                    Industria.LOGGER.warn("Tried to send debug rendering toggle message, but player was null!");
                    return;
                }

                player.sendSystemMessage(Component.literal("Debug rendering: " + (debugRendering ? "[ENABLED]" : "[DISABLED]")));
            }
        });
    }
}
