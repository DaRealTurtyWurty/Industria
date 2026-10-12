package dev.turtywurty.industria.testworld;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.molding.MoldRenderPipelines;
import dev.turtywurty.turtymultiloader.event.client.ClientEvents;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;

/** Opt-in smoke test that exits after resources, custom pipelines, and the title screen load. */
public final class ClientStartupValidation {
    private ClientStartupValidation() {
    }

    public static void initialize() {
        if (!Boolean.getBoolean("industria.test.client.startup"))
            return;
        // Register both GUI pipelines before the resource reload so their shaders are validated too.
        Industria.LOGGER.info("Validating custom pipelines {} and {}", MoldRenderPipelines.MOLD_LINE.getLocation(),
            MoldRenderPipelines.MOLD_CAST.getLocation());
        int[] readyTicks = {0};
        ClientEvents.onEndClientTick(client -> {
            if (client.options.onboardAccessibility) {
                client.options.onboardAccessibility = false;
                if (client.gui.screen() instanceof AccessibilityOnboardingScreen) {
                    client.gui.setScreen(new TitleScreen());
                }
            }
            if (client.gui.overlay() == null && client.gui.screen() instanceof TitleScreen) {
                if (++readyTicks[0] >= 20) {
                    Industria.LOGGER
                        .info("[ClientStartupValidation] PASS Minecraft 26.3 resources and title screen loaded");
                    client.stop();
                }
            } else {
                readyTicks[0] = 0;
            }
        });
    }
}
