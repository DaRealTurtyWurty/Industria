package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;

public class KeyBindingInit {
    public static final String INDUSTRIA_KEY_CATEGORY = "key." + Industria.MOD_ID + ".category";

    public static void init() {
        KeyBindingRegistryImpl.addCategory(INDUSTRIA_KEY_CATEGORY);
    }
}
