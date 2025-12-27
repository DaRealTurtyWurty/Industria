package dev.turtywurty.industria.pipe.impl.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.heatapi.api.base.NoLimitHeatStorage;
import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.init.PipeNetworkTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkType;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraPacketCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HeatPipeNetwork extends PipeNetwork<HeatStorage> {
    public static final MapCodec<HeatPipeNetwork> CODEC = PipeNetwork.createCodec(
            Codec.DOUBLE.fieldOf("storageAmount").forGetter(network -> network.storage.getAmount()),
            (storage, storageAmount) -> ((SimpleHeatStorage) storage).setAmount(storageAmount),
            HeatPipeNetwork::new);

    public static final PacketCodec<RegistryByteBuf, HeatPipeNetwork> PACKET_CODEC =
            PipeNetwork.createPacketCodec(
                    PacketCodecs.DOUBLE,
                    network -> network.storage.getAmount(),
                    (storage, storageAmount) -> ((SimpleHeatStorage) storage).setAmount(storageAmount),
                    HeatPipeNetwork::new);

    public static final Codec<Set<HeatPipeNetwork>> SET_CODEC = ExtraCodecs.setOf(CODEC);
    public static final PacketCodec<RegistryByteBuf, Set<HeatPipeNetwork>> SET_PACKET_CODEC =
            ExtraPacketCodecs.setOf(PACKET_CODEC);

    private final Map<BlockPos, HeatStorage> pipeStorages = new HashMap<>();

    private static final double PIPE_CONDUCTIVITY = 0.25D;
    private static final double BLOCK_CONDUCTIVITY = 0.15D;
    private static final double PIPE_DISSIPATION = 0.02D;

    public HeatPipeNetwork(UUID id) {
        super(id, TransferType.HEAT);
    }

    @Override
    protected SimpleHeatStorage createStorage() {
        return new NoLimitHeatStorage(true, true);
    }

    @Override
    protected PipeNetworkType<HeatStorage, ? extends PipeNetwork<HeatStorage>> getType() {
        return PipeNetworkTypeInit.HEAT;
    }

    @Override
    public HeatStorage getStorage(BlockPos pos) {
        return this.pipeStorages.computeIfAbsent(pos, p -> createStorage());
    }

    @Override
    public void addPipe(BlockPos pos) {
        super.addPipe(pos);
        if (!this.pipeStorages.containsKey(pos)) {
            this.pipeStorages.put(pos, createStorage());
        }
    }

    @Override
    public void movePipesFrom(PipeNetwork<HeatStorage> oldNetwork, Set<BlockPos> pipesToInherit) {
        if (oldNetwork instanceof HeatPipeNetwork heatOldNetwork) {
            Map<BlockPos, HeatStorage> storagesToInherit = new HashMap<>();
            for (BlockPos pipe : pipesToInherit) {
                HeatStorage storage = heatOldNetwork.pipeStorages.get(pipe);
                if (storage != null) {
                    storagesToInherit.put(pipe, storage);
                }
            }

            super.movePipesFrom(oldNetwork, pipesToInherit);
            this.pipeStorages.putAll(storagesToInherit);
        } else {
            super.movePipesFrom(oldNetwork, pipesToInherit);
        }
    }

    @Override
    public boolean hasCentralStorage() {
        return false;
    }

    @Override
    public void tick(World world) {
        super.tick(world);

        // Conduct heat between adjacent pipes only once per pair
        for (BlockPos pipePos : this.pipes) {
            HeatStorage pipeStorage = getStorage(pipePos);
            for (Direction dir : Direction.values()) {
                BlockPos neighbourPos = pipePos.offset(dir);
                if (this.pipes.contains(neighbourPos) && pipePos.asLong() < neighbourPos.asLong()) {
                    HeatStorage neighbourStorage = getStorage(neighbourPos);
                    exchange(pipeStorage, neighbourStorage, PIPE_CONDUCTIVITY);
                }
            }
        }

        // Conduct heat between pipes and connected blocks
        for (BlockPos connectedPos : this.connectedBlocks) {
            for (Direction dir : Direction.values()) {
                BlockPos pipePos = connectedPos.offset(dir);
                if (this.pipes.contains(pipePos)) {
                    HeatStorage pipeStorage = getStorage(pipePos);
                    HeatStorage connectedStorage = this.transferType.lookup(world, connectedPos, dir.getOpposite());
                    if (connectedStorage != null && connectedStorage.supportsInsertion()) {
                        exchange(pipeStorage, connectedStorage, BLOCK_CONDUCTIVITY);
                    }
                }
            }
        }

        // Ambient dissipation
        for (HeatStorage pipeStorage : this.pipeStorages.values()) {
            double loss = pipeStorage.getAmount() * PIPE_DISSIPATION;
            ((SimpleHeatStorage) pipeStorage).setAmount(Math.max(0, pipeStorage.getAmount() - loss));
        }
    }

    private static void exchange(HeatStorage from, HeatStorage to, double coefficient) {
        double transfer = (from.getAmount() - to.getAmount()) * coefficient;
        ((SimpleHeatStorage) from).setAmount(from.getAmount() - transfer);
        ((SimpleHeatStorage) to).setAmount(to.getAmount() + transfer);
    }
}
