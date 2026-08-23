package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.util.DoublePositionSource;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;

import java.util.function.Supplier;

public class ModPositionSourceTypes {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final RegistrationHandle<PositionSourceType<?>, PositionSourceType<DoublePositionSource>> DOUBLE_POSITION_SOURCE =
            register("double_position_source", DoublePositionSource.Type::new);

    public static <T extends PositionSource> RegistrationHandle<PositionSourceType<?>, PositionSourceType<T>> register(String name, Supplier<PositionSourceType<T>> type) {
        return REGISTRIES.registerPositionSourceType(Industria.id(name), type);
    }

    public static void init() {
    }
}
