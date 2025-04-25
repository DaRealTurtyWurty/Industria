package dev.turtywurty.industria.pipe.impl.manager;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.industria.init.PipeNetworkManagerTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.network.HeatPipeNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.UUID;

public class HeatPipeNetworkManager extends PipeNetworkManager<HeatStorage, HeatPipeNetwork> {
    public static final MapCodec<HeatPipeNetworkManager> CODEC = PipeNetworkManager.createCodec(
            HeatPipeNetwork.CODEC.codec(), HeatPipeNetworkManager::new);

    public static final PacketCodec<RegistryByteBuf, HeatPipeNetworkManager> PACKET_CODEC =
            PipeNetworkManager.createPacketCodec(HeatPipeNetwork.PACKET_CODEC, HeatPipeNetworkManager::new);

    public HeatPipeNetworkManager(RegistryKey<World> dimension) {
        super(PipeNetworkManagerTypeInit.HEAT, TransferType.HEAT, dimension);
    }

    @Override
    protected HeatPipeNetwork createNetwork(UUID id) {
        return new HeatPipeNetwork(id);
    }
}
