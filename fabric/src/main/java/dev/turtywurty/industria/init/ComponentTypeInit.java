package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.component.FluidPocketsComponent;
import dev.turtywurty.industria.component.MultiblockExportSelectionComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.UnaryOperator;

public class ComponentTypeInit {
    public static final DataComponentType<FluidPocketsComponent> FLUID_POCKETS =
            register("fluid_pockets", listBuilder -> listBuilder
                    .persistent(FluidPocketsComponent.CODEC)
                    .networkSynchronized(FluidPocketsComponent.STREAM_CODEC)
                    .cacheEncoding());

    public static final DataComponentType<MultiblockExportSelectionComponent> MULTIBLOCK_EXPORT_SELECTION =
            register("multiblock_export_selection", listBuilder -> listBuilder
                    .persistent(MultiblockExportSelectionComponent.CODEC)
                    .networkSynchronized(MultiblockExportSelectionComponent.STREAM_CODEC)
                    .cacheEncoding());

    public static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Industria.id(name), builder.apply(DataComponentType.builder()).build());
    }

    public static void init() {
    }
}
