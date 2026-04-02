package dev.turtywurty.industria.util;

import dev.turtywurty.industria.Industria;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

public final class StartupStateLogger {
    private static boolean initialized;
    private static Class<?> lastScreenClass;
    private static Class<?> lastOverlayClass;
    private static Boolean lastGameLoadFinished;
    private static int ticksSinceStart;
    private static int lastProgressLogTick = -200;

    private StartupStateLogger() {
    }

    public static void init() {
        if (initialized)
            return;

        initialized = true;
        ClientTickEvents.END_CLIENT_TICK.register(StartupStateLogger::onEndClientTick);
    }

    private static void onEndClientTick(Minecraft client) {
        ticksSinceStart++;

        Screen screen = client.screen;
        Overlay overlay = client.getOverlay();
        boolean gameLoadFinished = client.isGameLoadFinished();

        logIfChanged("screen", lastScreenClass, screen == null ? null : screen.getClass());
        lastScreenClass = screen == null ? null : screen.getClass();

        logIfChanged("overlay", lastOverlayClass, overlay == null ? null : overlay.getClass());
        lastOverlayClass = overlay == null ? null : overlay.getClass();

        if (lastGameLoadFinished == null || lastGameLoadFinished != gameLoadFinished) {
            Industria.LOGGER.info("Startup state changed: gameLoadFinished={}", gameLoadFinished);
            lastGameLoadFinished = gameLoadFinished;
        }

        if (screen instanceof TitleScreen) {
            Industria.LOGGER.info("Startup reached TitleScreen after {} ticks.", ticksSinceStart);
            return;
        }

        if (ticksSinceStart - lastProgressLogTick >= 200) {
            lastProgressLogTick = ticksSinceStart;
            Industria.LOGGER.info(
                    "Startup progress: ticks={}, screen={}, overlay={}, gameLoadFinished={}",
                    ticksSinceStart,
                    describe(screen),
                    describe(overlay),
                    gameLoadFinished
            );
        }
    }

    private static void logIfChanged(String label, Class<?> previous, Class<?> current) {
        if (previous == current)
            return;

        Industria.LOGGER.info("Startup state changed: {}={}", label, current == null ? "<null>" : current.getName());
    }

    private static String describe(Object object) {
        return object == null ? "<null>" : object.getClass().getName();
    }
}
