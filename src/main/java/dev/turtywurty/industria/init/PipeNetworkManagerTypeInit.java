package dev.turtywurty.industria.init;

import com.mojang.serialization.Codec;
import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkManagerType;
import dev.turtywurty.industria.pipe.impl.manager.CableNetworkManager;
import dev.turtywurty.industria.pipe.impl.manager.FluidPipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.manager.HeatPipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.manager.SlurryPipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.network.CableNetwork;
import dev.turtywurty.industria.pipe.impl.network.FluidPipeNetwork;
import dev.turtywurty.industria.pipe.impl.network.HeatPipeNetwork;
import dev.turtywurty.industria.pipe.impl.network.SlurryPipeNetwork;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import team.reborn.energy.api.EnergyStorage;

public class PipeNetworkManagerTypeInit {
    public static final RegistryKey<Registry<PipeNetworkManagerType<?, ?>>> PIPE_NETWORK_MANAGERS_TYPE_KEY =
            RegistryKey.ofRegistry(Industria.id("pipe_network_manager_type"));

    public static final Registry<PipeNetworkManagerType<?, ?>> PIPE_NETWORK_MANAGER_TYPES =
            FabricRegistryBuilder.createSimple(PIPE_NETWORK_MANAGERS_TYPE_KEY)
                    .attribute(RegistryAttribute.MODDED)
                    .attribute(RegistryAttribute.SYNCED)
                    .buildAndRegister();

    public static final Codec<PipeNetworkManagerType<?, ?>> CODEC = PipeNetworkManagerTypeInit.PIPE_NETWORK_MANAGER_TYPES.getCodec();
    public static final PacketCodec<RegistryByteBuf, PipeNetworkManagerType<?, ?>> PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    @SuppressWarnings("unchecked")
    public static <S, N extends PipeNetwork<S>> PipeNetworkManagerType<S, N> getType(TransferType<S, ?, ?> transferType) {
        return PIPE_NETWORK_MANAGER_TYPES.stream()
                .filter(type -> type.transferType() == transferType)
                .map(type -> (PipeNetworkManagerType<S, N>) type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No PipeNetworkManagerType found for transfer type: " + transferType));
    }    public static final PipeNetworkManagerType<EnergyStorage, CableNetwork> ENERGY =
            register("energy", new PipeNetworkManagerType<>(
                    TransferType.ENERGY,
                    CableNetworkManager::new,
                    CableNetworkManager.CODEC,
                    CableNetworkManager.PACKET_CODEC));

    public static <S, N extends PipeNetwork<S>, T extends PipeNetworkManagerType<S, N>> T register(String name, T type) {
        return Registry.register(PIPE_NETWORK_MANAGER_TYPES, Industria.id(name), type);
    }    public static final PipeNetworkManagerType<Storage<FluidVariant>, FluidPipeNetwork> FLUID =
            register("fluid", new PipeNetworkManagerType<>(
                    TransferType.FLUID,
                    FluidPipeNetworkManager::new,
                    FluidPipeNetworkManager.CODEC,
                    FluidPipeNetworkManager.PACKET_CODEC));

    public static void init() {
    }    public static final PipeNetworkManagerType<Storage<SlurryVariant>, SlurryPipeNetwork> SLURRY =
            register("slurry", new PipeNetworkManagerType<>(
                    TransferType.SLURRY,
                    SlurryPipeNetworkManager::new,
                    SlurryPipeNetworkManager.CODEC,
                    SlurryPipeNetworkManager.PACKET_CODEC));

    public static final PipeNetworkManagerType<HeatStorage, HeatPipeNetwork> HEAT =
            register("heat", new PipeNetworkManagerType<>(
                    TransferType.HEAT,
                    HeatPipeNetworkManager::new,
                    HeatPipeNetworkManager.CODEC,
                    HeatPipeNetworkManager.PACKET_CODEC));






}
