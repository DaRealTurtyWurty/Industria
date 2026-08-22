package dev.turtywurty.industria.init;

import com.mojang.serialization.Codec;
import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkType;
import dev.turtywurty.industria.pipe.impl.network.CableNetwork;
import dev.turtywurty.industria.pipe.impl.network.FluidPipeNetwork;
import dev.turtywurty.industria.pipe.impl.network.GasPipeNetwork;
import dev.turtywurty.industria.pipe.impl.network.SlurryPipeNetwork;
import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.turtymultiloader.registration.CustomRegistry;
import dev.turtywurty.turtymultiloader.registration.CustomRegistryOptions;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

public class ModPipeNetworkTypes {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final ResourceKey<Registry<PipeNetworkType<?, ?>>> PIPE_NETWORK_TYPE_KEY =
            ResourceKey.createRegistryKey(Industria.id("pipe_network_type"));

    public static final CustomRegistry<PipeNetworkType<?, ?>> PIPE_NETWORK_TYPES =
            REGISTRIES.customRegistry(PIPE_NETWORK_TYPE_KEY, new CustomRegistryOptions(true, false));

    public static final Codec<PipeNetworkType<?, ?>> CODEC =
            Codec.lazyInitialized(() -> ModPipeNetworkTypes.PIPE_NETWORK_TYPES.get().byNameCodec());
    public static final StreamCodec<RegistryFriendlyByteBuf, PipeNetworkType<?, ?>> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static final RegistrationHandle<PipeNetworkType<?, ?>, PipeNetworkType<ResourceVariant<UnitResource>, CableNetwork>> ENERGY = register("energy",
            () -> new PipeNetworkType<>(CableNetwork.CODEC, CableNetwork.STREAM_CODEC));

    public static final RegistrationHandle<PipeNetworkType<?, ?>, PipeNetworkType<ResourceVariant<Fluid>, FluidPipeNetwork>> FLUID = register("fluid",
            () -> new PipeNetworkType<>(FluidPipeNetwork.CODEC, FluidPipeNetwork.STREAM_CODEC));

    public static final RegistrationHandle<PipeNetworkType<?, ?>, PipeNetworkType<ResourceVariant<Slurry>, SlurryPipeNetwork>> SLURRY = register("slurry",
            () -> new PipeNetworkType<>(SlurryPipeNetwork.CODEC, SlurryPipeNetwork.STREAM_CODEC));

    public static final RegistrationHandle<PipeNetworkType<?, ?>, PipeNetworkType<ResourceVariant<Gas>, GasPipeNetwork>> GAS = register("gas",
            () -> new PipeNetworkType<>(GasPipeNetwork.CODEC, GasPipeNetwork.STREAM_CODEC));

    public static <V extends ResourceVariant<?>, N extends PipeNetwork<V>, T extends PipeNetworkType<V, N>> RegistrationHandle<PipeNetworkType<?, ?>, T> register(String name, Supplier<T> type) {
        return REGISTRIES.register(PIPE_NETWORK_TYPE_KEY, Industria.id(name), type);
    }

    public static void init() {
    }
}
