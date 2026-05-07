package dev.turtywurty.industria.pipe.impl.manager;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.gasapi.api.GasVariant;
import dev.turtywurty.industria.init.PipeNetworkManagerTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.network.GasPipeNetwork;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public class GasPipeNetworkManager extends PipeNetworkManager<Storage<GasVariant>, GasPipeNetwork> {
    public static final MapCodec<GasPipeNetworkManager> CODEC = PipeNetworkManager.createCodec(
            GasPipeNetwork.CODEC.codec(), GasPipeNetworkManager::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, GasPipeNetworkManager> STREAM_CODEC =
            PipeNetworkManager.createPacketCodec(GasPipeNetwork.STREAM_CODEC, GasPipeNetworkManager::new);

    public GasPipeNetworkManager() {
        super(PipeNetworkManagerTypeInit.GAS, TransferType.GAS);
    }

    @Override
    protected GasPipeNetwork createNetwork(UUID id) {
        return new GasPipeNetwork(id);
    }
}
