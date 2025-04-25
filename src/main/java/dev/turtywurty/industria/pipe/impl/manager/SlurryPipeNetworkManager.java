package dev.turtywurty.industria.pipe.impl.manager;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.industria.init.PipeNetworkManagerTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.network.SlurryPipeNetwork;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.UUID;

public class SlurryPipeNetworkManager extends PipeNetworkManager<Storage<SlurryVariant>, SlurryPipeNetwork> {
    public static final MapCodec<SlurryPipeNetworkManager> CODEC = PipeNetworkManager.createCodec(
            SlurryPipeNetwork.CODEC.codec(), SlurryPipeNetworkManager::new);

    public static final PacketCodec<RegistryByteBuf, SlurryPipeNetworkManager> PACKET_CODEC =
            PipeNetworkManager.createPacketCodec(SlurryPipeNetwork.PACKET_CODEC, SlurryPipeNetworkManager::new);

    public SlurryPipeNetworkManager(RegistryKey<World> dimension) {
        super(PipeNetworkManagerTypeInit.SLURRY, TransferType.SLURRY, dimension);
    }

    @Override
    protected SlurryPipeNetwork createNetwork(UUID id) {
        return new SlurryPipeNetwork(id);
    }
}
