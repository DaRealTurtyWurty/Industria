package dev.turtywurty.industria.pipe.impl.manager;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.init.PipeNetworkManagerTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.network.FluidPipeNetwork;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public class FluidPipeNetworkManager extends PipeNetworkManager<Storage<FluidVariant>, FluidPipeNetwork> {
    public static final MapCodec<FluidPipeNetworkManager> CODEC = PipeNetworkManager.createCodec(
            FluidPipeNetwork.CODEC.codec(), FluidPipeNetworkManager::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidPipeNetworkManager> STREAM_CODEC =
            PipeNetworkManager.createPacketCodec(FluidPipeNetwork.STREAM_CODEC, FluidPipeNetworkManager::new);

    public FluidPipeNetworkManager() {
        super(PipeNetworkManagerTypeInit.FLUID, TransferType.FLUID);
    }

    @Override
    protected FluidPipeNetwork createNetwork(UUID id) {
        return new FluidPipeNetwork(id);
    }
}
