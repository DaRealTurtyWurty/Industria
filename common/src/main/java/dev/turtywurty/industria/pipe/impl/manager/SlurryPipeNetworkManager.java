package dev.turtywurty.industria.pipe.impl.manager;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.init.ModPipeNetworkManagerTypes;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.network.SlurryPipeNetwork;
import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public class SlurryPipeNetworkManager extends PipeNetworkManager<ResourceVariant<Slurry>, SlurryPipeNetwork> {
    public static final MapCodec<SlurryPipeNetworkManager> CODEC = PipeNetworkManager.createCodec(
            SlurryPipeNetwork.CODEC.codec(), SlurryPipeNetworkManager::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, SlurryPipeNetworkManager> STREAM_CODEC =
            PipeNetworkManager.createPacketCodec(SlurryPipeNetwork.STREAM_CODEC, SlurryPipeNetworkManager::new);

    public SlurryPipeNetworkManager() {
        super(ModPipeNetworkManagerTypes.SLURRY.get(), TransferType.SLURRY);
    }

    @Override
    protected SlurryPipeNetwork createNetwork(UUID id) {
        return new SlurryPipeNetwork(id);
    }
}
