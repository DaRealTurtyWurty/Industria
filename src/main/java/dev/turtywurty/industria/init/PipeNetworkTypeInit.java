package dev.turtywurty.industria.init;

import com.mojang.serialization.Codec;
import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkType;
import dev.turtywurty.industria.pipe.impl.network.CableNetwork;
import dev.turtywurty.industria.pipe.impl.network.FluidPipeNetwork;
import dev.turtywurty.industria.pipe.impl.network.HeatPipeNetwork;
import dev.turtywurty.industria.pipe.impl.network.SlurryPipeNetwork;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import team.reborn.energy.api.EnergyStorage;

public class PipeNetworkTypeInit {
    public static final ResourceKey<Registry<PipeNetworkType<?, ?>>> PIPE_NETWORK_TYPE_KEY =
            ResourceKey.createRegistryKey(Industria.id("pipe_network_type"));

    public static final Registry<PipeNetworkType<?, ?>> PIPE_NETWORK_TYPES =
            FabricRegistryBuilder.create(PIPE_NETWORK_TYPE_KEY)
                    .attribute(RegistryAttribute.MODDED)
                    .attribute(RegistryAttribute.SYNCED)
                    .buildAndRegister();

    public static final Codec<PipeNetworkType<?, ?>> CODEC = PipeNetworkTypeInit.PIPE_NETWORK_TYPES.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, PipeNetworkType<?, ?>> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static final PipeNetworkType<EnergyStorage, CableNetwork> ENERGY = register("energy",
            new PipeNetworkType<>(CableNetwork.CODEC, CableNetwork.STREAM_CODEC));

    public static final PipeNetworkType<Storage<FluidVariant>, FluidPipeNetwork> FLUID = register("fluid",
            new PipeNetworkType<>(FluidPipeNetwork.CODEC, FluidPipeNetwork.STREAM_CODEC));

    public static final PipeNetworkType<HeatStorage, HeatPipeNetwork> HEAT = register("heat",
            new PipeNetworkType<>(HeatPipeNetwork.CODEC, HeatPipeNetwork.STREAM_CODEC));

    public static final PipeNetworkType<Storage<SlurryVariant>, SlurryPipeNetwork> SLURRY = register("slurry",
            new PipeNetworkType<>(SlurryPipeNetwork.CODEC, SlurryPipeNetwork.STREAM_CODEC));

    public static <S, N extends PipeNetwork<S>, T extends PipeNetworkType<S, N>> T register(String name, T type) {
        return Registry.register(PIPE_NETWORK_TYPES, Industria.id(name), type);
    }

    public static void init() {
    }
}
