package dev.turtywurty.industria.pipe.impl.manager;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.init.PipeNetworkManagerTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.network.CableNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import team.reborn.energy.api.EnergyStorage;

import java.util.UUID;

public class CableNetworkManager extends PipeNetworkManager<EnergyStorage, CableNetwork> {
    public static final MapCodec<CableNetworkManager> CODEC = PipeNetworkManager.createCodec(
            CableNetwork.CODEC.codec(), CableNetworkManager::new);

    public static final PacketCodec<RegistryByteBuf, CableNetworkManager> PACKET_CODEC =
            PipeNetworkManager.createPacketCodec(CableNetwork.PACKET_CODEC, CableNetworkManager::new);

    public CableNetworkManager() {
        super(PipeNetworkManagerTypeInit.ENERGY, TransferType.ENERGY);
    }

    @Override
    protected CableNetwork createNetwork(UUID id) {
        return new CableNetwork(id);
    }
}
