package dev.turtywurty.industria.pipe.impl.manager;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.init.PipeNetworkManagerTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.pipe.impl.network.CableNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import team.reborn.energy.api.EnergyStorage;

import java.util.UUID;

public class CableNetworkManager extends PipeNetworkManager<EnergyStorage, CableNetwork> {
    public static final MapCodec<CableNetworkManager> CODEC = PipeNetworkManager.createCodec(
            CableNetwork.CODEC.codec(), CableNetworkManager::new);

    public static final PacketCodec<RegistryByteBuf, CableNetworkManager> PACKET_CODEC =
            PipeNetworkManager.createPacketCodec(CableNetwork.PACKET_CODEC, CableNetworkManager::new);

    public CableNetworkManager(RegistryKey<World> dimension) {
        super(PipeNetworkManagerTypeInit.ENERGY, TransferType.ENERGY, dimension);
    }

    @Override
    protected CableNetwork createNetwork(UUID id) {
        return new CableNetwork(id);
    }
}
