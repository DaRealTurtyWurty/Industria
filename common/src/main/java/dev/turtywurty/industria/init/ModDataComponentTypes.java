package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.component.FluidPocketsComponent;
import dev.turtywurty.industria.component.MultiblockExportSelectionComponent;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.core.component.DataComponentType;

import java.util.function.UnaryOperator;

public class ModDataComponentTypes {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final RegistrationHandle<DataComponentType<?>, DataComponentType<FluidPocketsComponent>> FLUID_POCKETS =
            register("fluid_pockets", builder -> builder
                    .persistent(FluidPocketsComponent.CODEC)
                    .networkSynchronized(FluidPocketsComponent.STREAM_CODEC)
                    .cacheEncoding());

    public static final RegistrationHandle<DataComponentType<?>, DataComponentType<MultiblockExportSelectionComponent>> MULTIBLOCK_EXPORT_SELECTION =
            register("multiblock_export_selection", builder -> builder
                    .persistent(MultiblockExportSelectionComponent.CODEC)
                    .networkSynchronized(MultiblockExportSelectionComponent.STREAM_CODEC)
                    .cacheEncoding());

    public static <T> RegistrationHandle<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return REGISTRIES.registerDataComponentType(Industria.id(name), () -> builder.apply(DataComponentType.builder()).build());
    }

    public static void init() {
    }
}
