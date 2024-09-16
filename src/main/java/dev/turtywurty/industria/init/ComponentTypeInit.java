package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class ComponentTypeInit {
    public static final ComponentType<List<WorldFluidPocketsState.FluidPocket>> FLUID_POCKETS =
            register("fluid_pockets", listBuilder -> listBuilder.codec(WorldFluidPocketsState.FluidPocket.CODEC.listOf())
                    .packetCodec(PacketCodecs.collection(ArrayList::new, WorldFluidPocketsState.FluidPocket.PACKET_CODEC))
                    .cache());

    public static <T> ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builder) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Industria.id(name), builder.apply(ComponentType.builder()).build());
    }

    public static void init() {}
}
