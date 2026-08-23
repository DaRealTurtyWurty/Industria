package dev.turtywurty.industria.pipe.impl.manager;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.init.ModPipeNetworkManagerTypes;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.network.CableNetwork;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public class CableNetworkManager extends PipeNetworkManager<ResourceVariant<UnitResource>, CableNetwork> {
    public static final MapCodec<CableNetworkManager> CODEC = PipeNetworkManager.createCodec(
            CableNetwork.CODEC.codec(), CableNetworkManager::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, CableNetworkManager> STREAM_CODEC =
            PipeNetworkManager.createPacketCodec(CableNetwork.STREAM_CODEC, CableNetworkManager::new);

    public CableNetworkManager() {
        super(ModPipeNetworkManagerTypes.ENERGY.get(), TransferType.ENERGY);
    }

    @Override
    protected CableNetwork createNetwork(UUID id) {
        return new CableNetwork(id);
    }
}
