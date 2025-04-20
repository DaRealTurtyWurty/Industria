package dev.turtywurty.industria.init;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.impl.CableNetwork;
import dev.turtywurty.industria.pipe.impl.FluidPipeNetwork;
import dev.turtywurty.industria.pipe.impl.HeatPipeNetwork;
import dev.turtywurty.industria.pipe.impl.SlurryPipeNetwork;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

import java.util.function.Function;

public class PipeNetworkTypesInit {
    public static final RegistryKey<Registry<MapCodec<? extends PipeNetwork<?>>>> PIPE_NETWORK_TYPES_KEY =
            RegistryKey.ofRegistry(Industria.id("pipe_network_types"));

    public static final Registry<MapCodec<? extends PipeNetwork<?>>> PIPE_NETWORK_TYPES =
            FabricRegistryBuilder.createSimple(PIPE_NETWORK_TYPES_KEY)
                    .attribute(RegistryAttribute.MODDED)
                    .attribute(RegistryAttribute.SYNCED)
                    .buildAndRegister();

    public static final MapCodec<PipeNetwork<?>> CODEC = PIPE_NETWORK_TYPES.getCodec().dispatchMap(PipeNetwork::getCodec, Function.identity());

    public static void register(String name, MapCodec<? extends PipeNetwork<?>> codec) {
        Registry.register(PIPE_NETWORK_TYPES, Industria.id(name), codec);
    }

    public static void init() {
        register("energy_network", CableNetwork.CODEC);
        register("fluid_network", FluidPipeNetwork.CODEC);
        register("slurry_network", SlurryPipeNetwork.CODEC);
        register("heat_network", HeatPipeNetwork.CODEC);
        // TODO: Gas, item, pressure
    }
}
