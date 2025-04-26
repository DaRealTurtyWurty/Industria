package dev.turtywurty.industria.util;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.KeyBindingInit;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class DebugRenderingRegistry {
    public static boolean debugRendering = false;

    public static void init() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            debugRendering = false;
        });

        KeyBinding toggleDebugRenderingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + Industria.MOD_ID + ".toggle_debug_rendering",
                GLFW.GLFW_KEY_F6,
                KeyBindingInit.INDUSTRIA_KEY_CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleDebugRenderingKey.wasPressed()) {
                debugRendering = !debugRendering;
                client.player.sendMessage(Text.literal("Debug rendering: " + (debugRendering ? "[ENABLED]" : "[DISABLED]")), false);
            }
        });
    }
}
