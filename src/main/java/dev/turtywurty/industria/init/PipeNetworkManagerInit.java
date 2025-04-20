package dev.turtywurty.industria.init;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.CableNetwork;
import dev.turtywurty.industria.pipe.impl.FluidPipeNetwork;
import dev.turtywurty.industria.pipe.impl.HeatPipeNetwork;
import dev.turtywurty.industria.pipe.impl.SlurryPipeNetwork;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import team.reborn.energy.api.EnergyStorage;

public class PipeNetworkManagerInit {
    public static final RegistryKey<Registry<PipeNetworkManager<?, ?>>> PIPE_NETWORK_MANAGERS_KEY =
            RegistryKey.ofRegistry(Industria.id("pipe_network_managers"));

    public static final Registry<PipeNetworkManager<?, ?>> PIPE_NETWORK_MANAGERS =
            FabricRegistryBuilder.createSimple(PIPE_NETWORK_MANAGERS_KEY)
                    .attribute(RegistryAttribute.MODDED)
                    .attribute(RegistryAttribute.SYNCED)
                    .buildAndRegister();

    public static final PipeNetworkManager<EnergyStorage, CableNetwork> ENERGY =
            register("energy", new PipeNetworkManager<>(TransferType.ENERGY, CableNetwork::new));

    public static final PipeNetworkManager<Storage<FluidVariant>, FluidPipeNetwork> FLUID =
            register("fluid", new PipeNetworkManager<>(TransferType.FLUID, FluidPipeNetwork::new));

    public static final PipeNetworkManager<Storage<SlurryVariant>, SlurryPipeNetwork> SLURRY =
            register("slurry", new PipeNetworkManager<>(TransferType.SLURRY, SlurryPipeNetwork::new));

    public static final PipeNetworkManager<HeatStorage, HeatPipeNetwork> HEAT =
            register("heat", new PipeNetworkManager<>(TransferType.HEAT, HeatPipeNetwork::new));

    public static <S, N extends PipeNetwork<S>, M extends PipeNetworkManager<S, N>> M register(String name, M manager) {
        return Registry.register(PIPE_NETWORK_MANAGERS, Industria.id(name), manager);
    }

    public static void init() {}
}
