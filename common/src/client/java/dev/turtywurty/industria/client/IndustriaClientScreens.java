package dev.turtywurty.industria.client;

import dev.turtywurty.industria.blockentity.SolarPanelBlockEntity;
import dev.turtywurty.industria.blockentity.WindTurbineBlockEntity;
import dev.turtywurty.industria.screen.SolarPanelScreen;
import dev.turtywurty.industria.screen.WindTurbineScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class IndustriaClientScreens {
    private IndustriaClientScreens() {
    }

    public static void openSolarPanel(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof SolarPanelBlockEntity blockEntity) {
            Minecraft.getInstance().setScreen(new SolarPanelScreen(blockEntity));
        }
    }

    public static void openWindTurbine(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof WindTurbineBlockEntity blockEntity) {
            Minecraft.getInstance().setScreen(new WindTurbineScreen(blockEntity));
        }
    }
}
