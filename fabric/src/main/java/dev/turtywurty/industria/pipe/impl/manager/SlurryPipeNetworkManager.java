package dev.turtywurty.industria.pipe.impl.manager;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.industria.init.PipeNetworkManagerTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.network.SlurryPipeNetwork;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public class SlurryPipeNetworkManager extends PipeNetworkManager<Storage<SlurryVariant>, SlurryPipeNetwork> {
    public static final MapCodec<SlurryPipeNetworkManager> CODEC = PipeNetworkManager.createCodec(
            SlurryPipeNetwork.CODEC.codec(), SlurryPipeNetworkManager::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, SlurryPipeNetworkManager> STREAM_CODEC =
            PipeNetworkManager.createPacketCodec(SlurryPipeNetwork.STREAM_CODEC, SlurryPipeNetworkManager::new);

    public SlurryPipeNetworkManager() {
        super(PipeNetworkManagerTypeInit.SLURRY, TransferType.SLURRY);
    }

    @Override
    protected SlurryPipeNetwork createNetwork(UUID id) {
        return new SlurryPipeNetwork(id);
    }
}
