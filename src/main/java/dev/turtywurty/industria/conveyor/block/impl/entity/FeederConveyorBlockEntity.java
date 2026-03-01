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
        if (this.networkId == null) {
            ConveyorNetworkManager manager = LevelConveyorNetworks.getOrCreate((ServerLevel) this.level).getNetworkManager();
            ConveyorNetwork network = manager.getNetworkAt(this.worldPosition);
            if (network != null) {
                this.networkId = network.getId();
            } else {
                manager.traverseCreateNetwork((ServerLevel) this.level, this.worldPosition);
                network = manager.getNetworkAt(this.worldPosition);
                if (network != null) {
                    this.networkId = network.getId();
                }
            }
        }

        if (this.conveyorStorage == null && this.networkId != null) {
            ConveyorNetworkManager manager = LevelConveyorNetworks.getOrCreate((ServerLevel) this.level).getNetworkManager();
            ConveyorNetwork network = manager.getNetwork(this.networkId);
            if (network != null) {
                ConveyorNetworkStorage networkStorage = network.getStorage();
                if (networkStorage != null) {
                    ConveyorStorage conveyorStorage = networkStorage.getStorageAt(this.level, this.worldPosition);
                    this.conveyorStorage = new FeederItemStorage((ServerLevel) this.level, conveyorStorage);
                }
            }
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
                        transaction.commit();

                        ItemStack extractedStack = variantToPull.toStack((int) extracted);
                        addItemToStorage(extractedStack);
                    }
                }
            }
        }
    }

    private void addItemToStorage(ItemStack extractedStack) {
        LevelConveyorNetworks networks = LevelConveyorNetworks.getOrCreate((ServerLevel) this.level);
        ConveyorNetworkManager manager = networks.getNetworkManager();
        ConveyorNetwork network = manager.getNetwork(this.networkId);
        if (network != null) {
            network.getStorage().addItems(this.level, this.worldPosition, extractedStack);
            manager.syncNetwork((ServerLevel) this.level, network);
        }
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
        private final Storage<ItemVariant> delegate;

        private PendingInsert pendingInsert;

        private FeederItemStorage(ServerLevel level, ConveyorStorage conveyorStorage) {
            this.level = level;
            this.conveyorStorage = conveyorStorage;
            this.delegate = conveyorStorage.getItemStorage();
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0)
                return 0;

            if (this.pendingInsert != null || !this.conveyorStorage.canAcceptIncomingItem())
                return 0;

            updateSnapshots(transaction);
            this.pendingInsert = new PendingInsert(resource);
            return 1;
        }

        @Override
        public boolean supportsExtraction() {
            return this.delegate.supportsExtraction();
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return this.delegate.extract(resource, maxAmount, transaction);
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator() {
            return this.delegate.iterator();
        }

        @Override
        public Iterator<StorageView<ItemVariant>> nonEmptyIterator() {
            return this.delegate.nonEmptyIterator();
        }

        @Override
        public Iterable<StorageView<ItemVariant>> nonEmptyViews() {
            return this.delegate.nonEmptyViews();
        }

        @Override
        public long getVersion() {
            return this.delegate.getVersion();
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

            commitInsert(this.level, this.conveyorStorage, this.pendingInsert.variant());
            this.pendingInsert = null;
        }

        private static void commitInsert(ServerLevel level, ConveyorStorage conveyorStorage, ItemVariant variant) {
            LevelConveyorNetworks networks = LevelConveyorNetworks.getOrCreate(level);
            ConveyorNetworkManager manager = networks.getNetworkManager();
            ConveyorNetwork network = manager.getNetworkAt(conveyorStorage.getPos());
            if (network == null) {
                manager.traverseCreateNetwork(level, conveyorStorage.getPos());
                network = manager.getNetworkAt(conveyorStorage.getPos());
            }

            if (network == null)
                return;

            var conveyorItem = new ConveyorItem(conveyorStorage.getPos(), variant.toStack(1));
            if (!conveyorStorage.addItem(conveyorItem))
                return;

            BlockState state = level.getBlockState(conveyorStorage.getPos());
            if (state.getBlock() instanceof ConveyorLike conveyor) {
                conveyor.selectOutput(level, conveyorStorage.getPos(), state, conveyorItem, network, manager);
            }

            manager.syncNetwork(level, network);
        }

        public boolean canAcceptItem() {
            return this.conveyorStorage.canAcceptIncomingItem();
        }
    }

    private record PendingInsert(ItemVariant variant) {
        private static final PendingInsert EMPTY = new PendingInsert(ItemVariant.blank());

        private boolean isEmpty() {
            return this.variant.isBlank();
        }
    }
}
