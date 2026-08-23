package dev.turtywurty.industria.neoforge;

import dev.turtywurty.industria.testworld.IndustriaTestWorldLauncher;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class NeoForgeIndustriaTestWorldButton {
    private NeoForgeIndustriaTestWorldButton() {
    }

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen))
            return;

        Button original = event.getListenersList().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(IndustriaTestWorldLauncher::isVanillaTestWorldButton)
                .findFirst()
                .orElse(null);
        if (original == null)
            return;

        event.removeListener(original);
        event.addListener(IndustriaTestWorldLauncher.replacementFor(original, event.getScreen()));
    }
}
