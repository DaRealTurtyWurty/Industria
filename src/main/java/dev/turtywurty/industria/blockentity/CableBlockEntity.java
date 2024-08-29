package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class CableBlockEntity extends UpdatableBlockEntity implements TickableBlockEntity {
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private Set<BlockPos> connectedBlocks = null;

    protected CableBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public CableBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(BlockEntityTypeInit.CABLE, blockPos, blockState);

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 1000, 100, 0));
    }

    private void checkOutputs() {
        if (this.connectedBlocks == null && this.world != null) {
            this.connectedBlocks = new HashSet<>();
            traverse(this.pos, cable -> {
                // Check for all energy receivers around this position (ignore cables)
                for (Direction direction : Direction.values()) {
                    BlockPos pos = cable.getPos().offset(direction);
                    var storage = EnergyStorage.SIDED.find(this.world, pos, direction.getOpposite());
                    if (storage != null && storage.supportsInsertion() && !(this.world.getBlockEntity(pos) instanceof CableBlockEntity)) {
                        this.connectedBlocks.add(pos);
                    }
                }
            });
        }
    }

    // This is a generic function that will traverse all cables connected to this cable1 and call the given consumer for each cable1.
    private void traverse(BlockPos pos, Consumer<CableBlockEntity> consumer) {
        Set<BlockPos> traversed = new HashSet<>();
        traversed.add(pos);
        consumer.accept(this);
        traverse(pos, traversed, consumer);
    }

    private void traverse(BlockPos pos, Set<BlockPos> traversed, Consumer<CableBlockEntity> consumer) {
        if (this.world == null)
            return;

        for (Direction direction : Direction.values()) {
            BlockPos offset = pos.offset(direction);
            if (!traversed.contains(offset)) {
                traversed.add(offset);
                if (this.world.getBlockEntity(offset) instanceof CableBlockEntity cable) {
                    consumer.accept(cable);
                    cable.traverse(offset, traversed, consumer);
                }
            }
        }
    }

    @Override
    public void tick() {
        if(this.world == null || this.world.isClient)
            return;

        SimpleEnergyStorage energy = getEnergy();
        if(energy.amount > 0) {
            checkOutputs();
            if (this.connectedBlocks.isEmpty())
                return;

            long amount = energy.getAmount() / this.connectedBlocks.size();
            try (Transaction transaction = Transaction.openOuter()) {
                for (BlockPos pos : this.connectedBlocks) {
                    var direction = Direction.fromVector(this.pos.getX() - pos.getX(), this.pos.getY() - pos.getY(), this.pos.getZ() - pos.getZ());
                    var storage = EnergyStorage.SIDED.find(this.world, pos, direction);
                    if (storage != null && storage.supportsInsertion()) {
                        energy.amount -= storage.insert(amount, transaction);
                    }
                }

                transaction.commit();
            }
        }
    }

    public void markDirty() {
        traverse(this.pos, cable -> cable.connectedBlocks = null);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.put("Energy", this.wrappedEnergyStorage.writeNbt(registryLookup));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if(nbt.contains("Energy", NbtElement.LIST_TYPE))
            this.wrappedEnergyStorage.readNbt(nbt.getList("Energy", NbtElement.COMPOUND_TYPE), registryLookup);
    }

    public SimpleEnergyStorage getEnergy() {
        return this.wrappedEnergyStorage.getStorage(null);
    }

    public SimpleEnergyStorage getEnergyProvider(Direction direction) {
        return this.wrappedEnergyStorage.getStorage(direction);
    }
}
