package dev.turtywurty.industria.conveyor.block.impl.entity;

import dev.turtywurty.industria.blockentity.IndustriaBlockEntity;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.conveyor.*;
import dev.turtywurty.industria.conveyor.block.ConveyorLike;
import dev.turtywurty.industria.conveyor.block.impl.FeederConveyorBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FeederConveyorBlockEntity extends IndustriaBlockEntity implements TickableBlockEntity {
    private UUID networkId;
    private FeederItemStorage conveyorStorage;

    public FeederConveyorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.FEEDER_CONVEYOR, BlockEntityTypeInit.FEEDER_CONVEYOR, pos, state);
    }

    @Override
    public void tick() {
        if (this.level == null || this.level.isClientSide())
            return;

        tryInitialize();
        pullItems();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.networkId != null) {
            output.store("networkId", UUIDUtil.CODEC, this.networkId);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.networkId = input.read("networkId", UUIDUtil.CODEC).orElse(null);
    }

    private void tryInitialize() {
        ConveyorNetworkManager manager = LevelConveyorNetworks.getOrCreate((ServerLevel) this.level).getNetworkManager();
        ConveyorNetwork network = manager.getNetworkAt(this.worldPosition);
        if (network == null) {
            manager.traverseCreateNetwork((ServerLevel) this.level, this.worldPosition);
            network = manager.getNetworkAt(this.worldPosition);
        }

        if (network == null) {
            this.networkId = null;
            this.conveyorStorage = null;
            return;
        }

        this.networkId = network.getId();

        ConveyorStorage conveyorStorage = network.getStorage().getStorageAt(this.level, this.worldPosition);
        if (this.conveyorStorage == null || !this.conveyorStorage.isFor(conveyorStorage)) {
            this.conveyorStorage = new FeederItemStorage((ServerLevel) this.level, conveyorStorage);
        }
    }

    private void pullItems() {
        if (this.conveyorStorage == null || !this.conveyorStorage.canAcceptItem())
            return;

        Direction pullDirection = getPullDirection();
        Storage<ItemVariant> storageToPullFrom = TransferType.ITEM.lookup(this.level, this.worldPosition.relative(pullDirection), pullDirection.getOpposite());
        if (storageToPullFrom != null && storageToPullFrom.supportsExtraction()) {
            try (Transaction transaction = Transaction.openOuter()) {
                ItemVariant variantToPull = findItemVariant(storageToPullFrom);
                if (variantToPull != null) {
                    long extracted = storageToPullFrom.extract(variantToPull, 1, transaction);
                    if (extracted > 0) {
                        ItemStack extractedStack = variantToPull.toStack((int) extracted);
                        if (addItemToStorage(extractedStack)) {
                            transaction.commit();
                        }
                    }
                }
            }
        }
    }

    private boolean addItemToStorage(ItemStack extractedStack) {
        if (extractedStack.isEmpty())
            return false;

        LevelConveyorNetworks networks = LevelConveyorNetworks.getOrCreate((ServerLevel) this.level);
        ConveyorNetworkManager manager = networks.getNetworkManager();
        ConveyorNetwork network = manager.getNetworkAt(this.worldPosition);
        if (network == null) {
            manager.traverseCreateNetwork((ServerLevel) this.level, this.worldPosition);
            network = manager.getNetworkAt(this.worldPosition);
        }

        if (network == null)
            return false;

        this.networkId = network.getId();

        ConveyorStorage storage = network.getStorage().getStorageAt(this.level, this.worldPosition);
        ConveyorItem conveyorItem = new ConveyorItem(this.worldPosition, extractedStack.copy());
        if (!storage.addItem(conveyorItem))
            return false;

        BlockState state = this.level.getBlockState(this.worldPosition);
        if (state.getBlock() instanceof ConveyorLike conveyor) {
            conveyor.selectOutput(this.level, this.worldPosition, state, conveyorItem, network, manager);
        }

        if (this.conveyorStorage == null || !this.conveyorStorage.isFor(storage)) {
            this.conveyorStorage = new FeederItemStorage((ServerLevel) this.level, storage);
        }

        manager.syncNetwork((ServerLevel) this.level, network);
        return true;
    }

    private static @Nullable ItemVariant findItemVariant(Storage<ItemVariant> storageToPullFrom) {
        ItemVariant variantToPull = null;

        Iterator<StorageView<ItemVariant>> storageIterator = storageToPullFrom.nonEmptyIterator();
        while (storageIterator.hasNext()) {
            StorageView<ItemVariant> view = storageIterator.next();
            if (view.isResourceBlank() || view.getAmount() <= 0)
                continue;

            variantToPull = view.getResource();
            break;
        }
        return variantToPull;
    }

    public Storage<ItemVariant> getItemStorage(Direction direction) {
        if (direction == getPullDirection())
            return this.conveyorStorage;

        return null;
    }

    public Direction getPullDirection() {
        return getBlockState().getValue(FeederConveyorBlock.FACING).getOpposite();
    }

    private static final class FeederItemStorage extends SnapshotParticipant<PendingInsert> implements Storage<ItemVariant> {
        private final ServerLevel level;
        private final ConveyorStorage conveyorStorage;

        private PendingInsert pendingInsert;

        private FeederItemStorage(ServerLevel level, ConveyorStorage conveyorStorage) {
            this.level = level;
            this.conveyorStorage = conveyorStorage;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0)
                return 0;

            ConveyorStorage conveyorStorage = resolveConveyorStorage();
            if (this.pendingInsert != null || conveyorStorage == null || !conveyorStorage.canAcceptIncomingItem())
                return 0;

            updateSnapshots(transaction);
            this.pendingInsert = new PendingInsert(resource);
            return 1;
        }

        @Override
        public boolean supportsExtraction() {
            ConveyorStorage conveyorStorage = resolveConveyorStorage();
            return conveyorStorage != null && conveyorStorage.getItemStorage().supportsExtraction();
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            ConveyorStorage conveyorStorage = resolveConveyorStorage();
            return conveyorStorage != null
                    ? conveyorStorage.getItemStorage().extract(resource, maxAmount, transaction)
                    : 0;
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator() {
            ConveyorStorage conveyorStorage = resolveConveyorStorage();
            return conveyorStorage != null
                    ? conveyorStorage.getItemStorage().iterator()
                    : List.<StorageView<ItemVariant>>of().iterator();
        }

        @Override
        public Iterator<StorageView<ItemVariant>> nonEmptyIterator() {
            ConveyorStorage conveyorStorage = resolveConveyorStorage();
            return conveyorStorage != null
                    ? conveyorStorage.getItemStorage().nonEmptyIterator()
                    : List.<StorageView<ItemVariant>>of().iterator();
        }

        @Override
        public Iterable<StorageView<ItemVariant>> nonEmptyViews() {
            ConveyorStorage conveyorStorage = resolveConveyorStorage();
            return conveyorStorage != null
                    ? conveyorStorage.getItemStorage().nonEmptyViews()
                    : List.of();
        }

        @Override
        public long getVersion() {
            ConveyorStorage conveyorStorage = resolveConveyorStorage();
            return conveyorStorage != null ? conveyorStorage.getItemStorage().getVersion() : 0;
        }

        @Override
        protected PendingInsert createSnapshot() {
            return this.pendingInsert == null ? PendingInsert.EMPTY : this.pendingInsert;
        }

        @Override
        protected void readSnapshot(PendingInsert snapshot) {
            this.pendingInsert = snapshot.isEmpty() ? null : snapshot;
        }

        @Override
        protected void onFinalCommit() {
            super.onFinalCommit();

            if (this.pendingInsert == null)
                return;

            commitInsert(this.level, this.conveyorStorage.getPos(), this.pendingInsert.variant());
            this.pendingInsert = null;
        }

        private @Nullable ConveyorStorage resolveConveyorStorage() {
            LevelConveyorNetworks networks = LevelConveyorNetworks.getOrCreate(this.level);
            ConveyorNetworkManager manager = networks.getNetworkManager();
            ConveyorNetwork network = manager.getNetworkAt(this.conveyorStorage.getPos());
            if (network == null) {
                manager.traverseCreateNetwork(this.level, this.conveyorStorage.getPos());
                network = manager.getNetworkAt(this.conveyorStorage.getPos());
            }

            return network != null ? network.getStorage().getStorageAt(this.level, this.conveyorStorage.getPos()) : null;
        }

        private static void commitInsert(ServerLevel level, BlockPos conveyorPos, ItemVariant variant) {
            LevelConveyorNetworks networks = LevelConveyorNetworks.getOrCreate(level);
            ConveyorNetworkManager manager = networks.getNetworkManager();
            ConveyorNetwork network = manager.getNetworkAt(conveyorPos);
            if (network == null) {
                manager.traverseCreateNetwork(level, conveyorPos);
                network = manager.getNetworkAt(conveyorPos);
            }

            if (network == null)
                return;

            ConveyorStorage conveyorStorage = network.getStorage().getStorageAt(level, conveyorPos);
            var conveyorItem = new ConveyorItem(conveyorPos, variant.toStack(1));
            if (!conveyorStorage.addItem(conveyorItem))
                return;

            BlockState state = level.getBlockState(conveyorPos);
            if (state.getBlock() instanceof ConveyorLike conveyor) {
                conveyor.selectOutput(level, conveyorPos, state, conveyorItem, network, manager);
            }

            manager.syncNetwork(level, network);
        }

        public boolean canAcceptItem() {
            ConveyorStorage conveyorStorage = resolveConveyorStorage();
            return conveyorStorage != null && conveyorStorage.canAcceptIncomingItem();
        }

        public boolean isFor(ConveyorStorage conveyorStorage) {
            return Objects.equals(this.conveyorStorage, conveyorStorage);
        }
    }

    private record PendingInsert(ItemVariant variant) {
        private static final PendingInsert EMPTY = new PendingInsert(ItemVariant.blank());

        private boolean isEmpty() {
            return this.variant.isBlank();
        }
    }
}
