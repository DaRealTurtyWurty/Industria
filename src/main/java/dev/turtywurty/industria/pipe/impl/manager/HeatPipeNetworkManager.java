package dev.turtywurty.industria.pipe.impl.manager;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.industria.init.PipeNetworkManagerTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.network.HeatPipeNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public class HeatPipeNetworkManager extends PipeNetworkManager<HeatStorage, HeatPipeNetwork> {
    public static final MapCodec<HeatPipeNetworkManager> CODEC = PipeNetworkManager.createCodec(
            HeatPipeNetwork.CODEC.codec(), HeatPipeNetworkManager::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, HeatPipeNetworkManager> STREAM_CODEC =
            PipeNetworkManager.createPacketCodec(HeatPipeNetwork.STREAM_CODEC, HeatPipeNetworkManager::new);

    public HeatPipeNetworkManager() {
        super(PipeNetworkManagerTypeInit.HEAT, TransferType.HEAT);
    }

    @Override
    protected HeatPipeNetwork createNetwork(UUID id) {
        return new HeatPipeNetwork(id);
    }
}
