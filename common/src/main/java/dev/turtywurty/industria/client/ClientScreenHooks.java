package dev.turtywurty.industria.client;

import dev.turtywurty.industria.Industria;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ClientScreenHooks {
    private ClientScreenHooks() {
    }

    public static void openSolarPanel(Level level, BlockPos pos) {
        open("openSolarPanel", level, pos);
    }

    public static void openWindTurbine(Level level, BlockPos pos) {
        open("openWindTurbine", level, pos);
    }

    private static void open(String methodName, Level level, BlockPos pos) {
        try {
            Class<?> opener = Class.forName("dev.turtywurty.industria.client.IndustriaClientScreens");
            Method method = opener.getMethod(methodName, Level.class, BlockPos.class);
            method.invoke(null, level, pos);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
            Industria.LOGGER.error("Unable to open client screen {}", methodName, exception);
        } catch (InvocationTargetException exception) {
            Industria.LOGGER.error("Unable to open client screen {}", methodName, exception.getCause());
        }
    }
}
