package dev.turtywurty.industria;

import dev.turtywurty.industria.testworld.IndustriaTestWorldLauncher;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;

public final class FabricIndustriaTestWorldButton {
    private FabricIndustriaTestWorldButton() {
    }

    public static void init() {
        ScreenEvents.AFTER_INIT.register((_, screen, _, _) -> {
            if (!(screen instanceof TitleScreen))
                return;

            var widgets = Screens.getWidgets(screen);
            Button original = widgets.stream()
                    .filter(Button.class::isInstance)
                    .map(Button.class::cast)
                    .filter(IndustriaTestWorldLauncher::isVanillaTestWorldButton)
                    .findFirst()
                    .orElse(null);
            if (original == null) {
                Button fallback = IndustriaTestWorldLauncher.createFallbackButton(screen, widgets);
                if (fallback != null)
                    widgets.add(fallback);
                return;
            }

            AbstractWidget replacement = IndustriaTestWorldLauncher.replacementFor(original, screen);
            widgets.remove(original);
            widgets.add(replacement);
        });
    }

}
