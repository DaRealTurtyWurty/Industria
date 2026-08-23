package dev.turtywurty.industria.init;

import com.mojang.serialization.Codec;
import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkManagerType;
import dev.turtywurty.industria.pipe.impl.manager.CableNetworkManager;
import dev.turtywurty.industria.pipe.impl.manager.FluidPipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.manager.GasPipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.manager.SlurryPipeNetworkManager;
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
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

public class ModPipeNetworkManagerTypes {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final ResourceKey<Registry<PipeNetworkManagerType<?, ?>>> PIPE_NETWORK_MANAGERS_TYPE_KEY =
            ResourceKey.createRegistryKey(Industria.id("pipe_network_manager_type"));

    public static final CustomRegistry<PipeNetworkManagerType<?, ?>> PIPE_NETWORK_MANAGER_TYPES =
            REGISTRIES.customRegistry(PIPE_NETWORK_MANAGERS_TYPE_KEY, new CustomRegistryOptions(true, false));

    public static final Codec<PipeNetworkManagerType<?, ?>> CODEC =
            Codec.lazyInitialized(() -> ModPipeNetworkManagerTypes.PIPE_NETWORK_MANAGER_TYPES.get().byNameCodec());
    public static final StreamCodec<RegistryFriendlyByteBuf, PipeNetworkManagerType<?, ?>> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static final RegistrationHandle<PipeNetworkManagerType<?, ?>, PipeNetworkManagerType<ResourceVariant<UnitResource>, CableNetwork>> ENERGY =
            register("energy", () -> new PipeNetworkManagerType<>(
                    TransferType.ENERGY,
                    CableNetworkManager::new,
                    CableNetworkManager.CODEC,
                    CableNetworkManager.STREAM_CODEC));

    public static final RegistrationHandle<PipeNetworkManagerType<?, ?>, PipeNetworkManagerType<ResourceVariant<Fluid>, FluidPipeNetwork>> FLUID =
            register("fluid", () -> new PipeNetworkManagerType<>(
                    TransferType.FLUID,
                    FluidPipeNetworkManager::new,
                    FluidPipeNetworkManager.CODEC,
                    FluidPipeNetworkManager.STREAM_CODEC));

    public static final RegistrationHandle<PipeNetworkManagerType<?, ?>, PipeNetworkManagerType<ResourceVariant<Slurry>, SlurryPipeNetwork>> SLURRY =
            register("slurry", () -> new PipeNetworkManagerType<>(
                    TransferType.SLURRY,
                    SlurryPipeNetworkManager::new,
                    SlurryPipeNetworkManager.CODEC,
                    SlurryPipeNetworkManager.STREAM_CODEC));

    public static final RegistrationHandle<PipeNetworkManagerType<?, ?>, PipeNetworkManagerType<ResourceVariant<Gas>, GasPipeNetwork>> GAS =
            register("gas", () -> new PipeNetworkManagerType<>(
                    TransferType.GAS,
                    GasPipeNetworkManager::new,
                    GasPipeNetworkManager.CODEC,
                    GasPipeNetworkManager.STREAM_CODEC));

    @SuppressWarnings("unchecked")
    public static <V extends ResourceVariant<?>, N extends PipeNetwork<V>> PipeNetworkManagerType<V, N> getType(
            TransferType<ResourceStorage<V>, V, Long> transferType) {
        return (PipeNetworkManagerType<V, N>) getTypeUnchecked(transferType);
    }

    public static PipeNetworkManagerType<?, ?> getTypeUnchecked(TransferType<?, ?, ?> transferType) {
        return PIPE_NETWORK_MANAGER_TYPES.get().stream()
                .filter(type -> type.transferType() == transferType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No PipeNetworkManagerType found for transfer type: " + transferType));
    }

    public static <V extends ResourceVariant<?>, N extends PipeNetwork<V>, T extends PipeNetworkManagerType<V, N>> RegistrationHandle<PipeNetworkManagerType<?, ?>, T> register(String name, Supplier<T> type) {
        return REGISTRIES.register(PIPE_NETWORK_MANAGERS_TYPE_KEY, Industria.id(name), type);
    }

    public static void init() {
    }
}
