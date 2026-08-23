package dev.turtywurty.industria;

import dev.turtywurty.industria.testworld.IndustriaTestWorldLauncher;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.util.List;

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
                addFallbackButton((TitleScreen) screen, widgets);
                return;
            }

            AbstractWidget replacement = IndustriaTestWorldLauncher.replacementFor(original, screen);
            widgets.remove(original);
            widgets.add(replacement);
        });
    }

    private static void addFallbackButton(TitleScreen screen, List<AbstractWidget> widgets) {
        Button singleplayer = findButton(widgets, "menu.singleplayer");
        Button multiplayer = findButton(widgets, "menu.multiplayer");
        Button realms = findButton(widgets, "menu.online");
        Button options = findButton(widgets, "menu.options");
        if (singleplayer == null || multiplayer == null || realms == null || options == null)
            return;

        int bottomRowY = options.getY();
        widgets.stream()
                .filter(widget -> widget.getY() == bottomRowY)
                .forEach(widget -> widget.setPosition(widget.getX(), widget.getY() + 12));

        for (Button button : List.of(singleplayer, multiplayer, realms))
            button.setPosition(button.getX(), button.getY() - 12);

        widgets.add(IndustriaTestWorldLauncher.createButton(
                screen,
                realms.getX(),
                realms.getY() + 24,
                realms.getWidth(),
                realms.getHeight()));
    }

    private static Button findButton(List<AbstractWidget> widgets, String translationKey) {
        Component label = Component.translatable(translationKey);
        return widgets.stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> button.getMessage().equals(label))
                .findFirst()
                .orElse(null);
    }
}
