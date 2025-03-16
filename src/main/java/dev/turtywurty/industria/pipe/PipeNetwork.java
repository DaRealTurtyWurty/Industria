package dev.turtywurty.industria.pipe;

import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.util.NBTSerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class PipeNetwork<S> implements NBTSerializable<NbtCompound> {
    protected UUID id;
    protected final Set<BlockPos> pipes = new HashSet<>();
    protected final Set<BlockPos> connectedBlocks = new HashSet<>();
    protected final TransferType<S, ?> transferType;
    protected final S storage;

    public PipeNetwork(UUID id, TransferType<S, ?> transferType) {
        this.id = id;
        this.transferType = transferType;
        this.storage = createStorage();
    }

    @Override
    public NbtCompound writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("id", this.id);

        var pipes = new NbtList();
        for (BlockPos pipe : this.pipes) {
            NbtCompound pipeNbt = new NbtCompound();
            pipeNbt.putLong("pos", pipe.asLong());
            pipes.add(pipeNbt);
        }

        nbt.put("pipes", pipes);

        var connectedBlocks = new NbtList();
        for (BlockPos connectedBlock : this.connectedBlocks) {
            NbtCompound connectedBlockNbt = new NbtCompound();
            connectedBlockNbt.putLong("pos", connectedBlock.asLong());
            connectedBlocks.add(connectedBlockNbt);
        }

        nbt.put("connectedBlocks", connectedBlocks);

        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        this.pipes.clear();
        this.connectedBlocks.clear();

        this.id = nbt.getUuid("id");

        NbtList pipes = nbt.getList("pipes", 10);
        for (int i = 0; i < pipes.size(); i++) {
            this.pipes.add(BlockPos.fromLong(pipes.getCompound(i).getLong("pos")));
        }

        NbtList connectedBlocks = nbt.getList("connectedBlocks", 10);
        for (int i = 0; i < connectedBlocks.size(); i++) {
            this.connectedBlocks.add(BlockPos.fromLong(connectedBlocks.getCompound(i).getLong("pos")));
        }
    }

    public boolean isOfSameType(@NotNull PipeNetwork<?> storage) {
        return this.transferType == storage.transferType;
    }

    public UUID getId() {
        return this.id;
    }

    public Set<BlockPos> getPipes() {
        return this.pipes;
    }

    public Set<BlockPos> getConnectedBlocks() {
        return this.connectedBlocks;
    }

    protected abstract S createStorage();

    public S getStorage() {
        return this.storage;
    }

    public void tick(World world) {
        for (BlockPos connectedPos : this.connectedBlocks) {
            for (Direction direction : Direction.values()) {
                BlockPos pipePos = connectedPos.offset(direction);
                if (this.pipes.contains(pipePos)) {
                    this.transferType.pushTo(world, pipePos, connectedPos, direction);
                }
            }
        }
    }
}
