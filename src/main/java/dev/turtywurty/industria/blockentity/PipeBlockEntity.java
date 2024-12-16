package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Consumer;

public abstract class PipeBlockEntity<S, W extends WrappedStorage<S>> extends UpdatableBlockEntity implements SyncableTickableBlockEntity {
    protected final W wrappedStorage = createWrappedStorage();
    protected Set<BlockPos> connectedBlocks = null;

    public PipeBlockEntity(BlockEntityType<? extends PipeBlockEntity<S, W>> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected static Map<Direction, BlockPos> findConnectingPipes(World world, BlockPos pos) {
        Map<Direction, BlockPos> connecting = new HashMap<>();

        for (Direction direction : Direction.values()) {
            BlockPos offset = pos.offset(direction);
            BlockState state = world.getBlockState(offset);
            if (state.getBlock() instanceof PipeBlock && state.get(PipeBlock.DIRECTION_PROPERTY_MAP.get(direction.getOpposite())) == PipeBlock.ConnectorType.BLOCK) {
                connecting.put(direction, offset);
            }
        }

        return connecting;
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of((SyncableStorage) this.wrappedStorage.getStorage(null));
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        S storage = this.wrappedStorage.getStorage(null);
        if (storage == null || isEmpty(storage))
            return;

        checkOutputs();
        if(this.connectedBlocks == null || this.connectedBlocks.isEmpty())
            return;

        distribute(storage);
    }

    protected abstract W createWrappedStorage();
    protected abstract BlockApiLookup<S, Direction> getApiLookup();
    protected abstract boolean supportsInsertion(S storage);
    protected abstract boolean isEmpty(S storage);
    protected abstract void distribute(S storage);

    private void checkOutputs() {
        if (this.connectedBlocks == null && this.world != null) {
            this.connectedBlocks = new HashSet<>();
            traverse(this.pos, pipe -> {
                // Check for all receivers around this position (ignore pipes)
                for (Direction direction : Direction.values()) {
                    BlockPos pos = pipe.getPos().offset(direction);
                    S storage = getApiLookup().find(this.world, pos, direction.getOpposite());

                    if (storage != null && supportsInsertion(storage) && !getClass().isInstance(this.world.getBlockEntity(pos))) {
                        this.connectedBlocks.add(pos);
                    }
                }
            });
        }
    }

    // This is a generic function that will traverse all pipes connected to this pipe and call the given consumer for each pipe.
    private void traverse(BlockPos pos, Consumer<PipeBlockEntity<S, W>> consumer) {
        Set<BlockPos> traversed = new HashSet<>();
        traversed.add(pos);
        consumer.accept(this);
        traverse(pos, traversed, consumer);
    }

    @SuppressWarnings("unchecked")
    private void traverse(BlockPos pos, Set<BlockPos> traversed, Consumer<PipeBlockEntity<S, W>> consumer) {
        if (this.world == null)
            return;

        for (Direction direction : Direction.values()) {
            BlockPos offset = pos.offset(direction);
            if (!traversed.contains(offset)) {
                traversed.add(offset);

                BlockEntity blockEntity = this.world.getBlockEntity(offset);
                if (getClass().isInstance(blockEntity)) {
                    PipeBlockEntity<S, W> pipe = (PipeBlockEntity<S, W>) blockEntity;
                    consumer.accept(pipe);
                    pipe.traverse(offset, traversed, consumer);
                }
            }
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        traverse(this.pos, pipe -> pipe.connectedBlocks = null);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.put("Storage", this.wrappedStorage.writeNbt(registries));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("Storage", NbtElement.LIST_TYPE)) {
            this.wrappedStorage.readNbt(nbt.getList("Storage", NbtElement.COMPOUND_TYPE), registries);
        }
    }

    public S getStorageProvider(Direction side) {
        return this.wrappedStorage.getStorage(side);
    }
}
