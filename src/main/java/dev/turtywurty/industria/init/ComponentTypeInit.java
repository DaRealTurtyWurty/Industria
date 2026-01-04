package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.component.FluidPocketsComponent;
import net.minecraft.core.BlockPos;
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

    public static final DataComponentType<BlockPos> MULTIBLOCK_PIECE_POS =
            register("multiblock_piece_pos", listBuilder -> listBuilder
                    .persistent(BlockPos.CODEC)
                    .networkSynchronized(BlockPos.STREAM_CODEC));

    public static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Industria.id(name), builder.apply(DataComponentType.builder()).build());
    }

    public static void init() {}
}
