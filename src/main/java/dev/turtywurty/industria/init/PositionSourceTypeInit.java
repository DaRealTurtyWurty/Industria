package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.util.DoublePositionSource;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;

public class PositionSourceTypeInit {
    public static final PositionSourceType<DoublePositionSource> DOUBLE_POSITION_SOURCE =
            register("double_position_source", new DoublePositionSource.Type());

    public static <T extends PositionSource> PositionSourceType<T> register(String name, PositionSourceType<T> type) {
        return Registry.register(BuiltInRegistries.POSITION_SOURCE_TYPE, Industria.id(name), type);
    }

    public static void init() {}
}
