package dev.turtywurty.industria.pipe.impl.manager;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.init.ModPipeNetworkManagerTypes;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.network.FluidPipeNetwork;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;

import java.util.UUID;

public class FluidPipeNetworkManager extends PipeNetworkManager<ResourceVariant<Fluid>, FluidPipeNetwork> {
    public static final MapCodec<FluidPipeNetworkManager> CODEC = PipeNetworkManager.createCodec(
            FluidPipeNetwork.CODEC.codec(), FluidPipeNetworkManager::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidPipeNetworkManager> STREAM_CODEC =
            PipeNetworkManager.createPacketCodec(FluidPipeNetwork.STREAM_CODEC, FluidPipeNetworkManager::new);

    public FluidPipeNetworkManager() {
        super(ModPipeNetworkManagerTypes.FLUID.get(), TransferType.FLUID);
    }

    @Override
    protected FluidPipeNetwork createNetwork(UUID id) {
        return new FluidPipeNetwork(id);
    }
}
