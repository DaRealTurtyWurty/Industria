package dev.turtywurty.industria.conveyor.block.impl.entity;

import dev.turtywurty.industria.blockentity.IndustriaBlockEntity;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.ConveyorNetworkManager;
import dev.turtywurty.industria.conveyor.ConveyorStorage;
import dev.turtywurty.industria.conveyor.block.ConveyorLike;
import dev.turtywurty.industria.conveyor.block.impl.FeederConveyorBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.StoragePreconditions;
import dev.turtywurty.turtymultiloader.transfer.storage.TransferSupport;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransactionParticipant;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferContext;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class FeederConveyorBlockEntity extends IndustriaBlockEntity implements TickableBlockEntity {
    private UUID networkId;
    private FeederItemStorage conveyorStorage;

    public FeederConveyorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FEEDER_CONVEYOR.get(), ModBlockEntityTypes.FEEDER_CONVEYOR.get(), pos, state);
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
        if (conveyorStorage == null) {
            this.conveyorStorage = null;
            return;
        }
        if (this.conveyorStorage == null || !this.conveyorStorage.isFor(conveyorStorage)) {
            this.conveyorStorage = new FeederItemStorage((ServerLevel) this.level, conveyorStorage);
        }
    }

    private void pullItems() {
        if (this.conveyorStorage == null || !this.conveyorStorage.canAcceptItem())
            return;

        Direction pullDirection = getPullDirection();
        ResourceStorage<ResourceVariant<Item>> storageToPullFrom = TransferType.ITEM.lookup(
                this.level,
                this.worldPosition.relative(pullDirection),
                pullDirection.getOpposite());
        if (storageToPullFrom != null && storageToPullFrom.supportsExtraction()) {
            try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                ResourceVariant<Item> variantToPull = findItemVariant(storageToPullFrom);
                if (variantToPull != null) {
                    long extracted = storageToPullFrom.extract(variantToPull, 1, transaction);
                    if (extracted > 0) {
                        ItemStack extractedStack = toStack(variantToPull, Math.toIntExact(extracted));
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
        if (storage == null)
            return false;

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

    private static @Nullable ResourceVariant<Item> findItemVariant(
            ResourceStorage<ResourceVariant<Item>> storage) {
        if (!storage.hasStableIndices())
            return null;
        for (int index = 0; index < storage.size(); index++) {
            if (storage.amount(index) > 0 && !storage.resource(index).isBlank())
                return storage.resource(index);
        }
        return null;
    }

    public ResourceStorage<ResourceVariant<Item>> getItemStorage(Direction direction) {
        if (direction == getPullDirection())
            return this.conveyorStorage;

        return null;
    }

    public Direction getPullDirection() {
        return getBlockState().getValue(FeederConveyorBlock.FACING).getOpposite();
    }

    private static final class FeederItemStorage extends TransactionParticipant<PendingInsert>
            implements ResourceStorage<ResourceVariant<Item>> {
        private final ServerLevel level;
        private final ConveyorStorage conveyorStorage;

        private PendingInsert pendingInsert;
        private long version;

        private FeederItemStorage(ServerLevel level, ConveyorStorage conveyorStorage) {
            this.level = level;
            this.conveyorStorage = conveyorStorage;
        }

        @Override
        public int size() {
            ResourceStorage<ResourceVariant<Item>> storage = resolveItemStorage();
            return storage == null ? 0 : storage.size();
        }

        @Override
        public ResourceVariant<Item> resource(int index) {
            return requireItemStorage().resource(index);
        }

        @Override
        public long amount(int index) {
            return requireItemStorage().amount(index);
        }

        @Override
        public long capacity(int index, ResourceVariant<Item> resource) {
            StoragePreconditions.check(resource, 0);
            StoragePreconditions.index(index, size());
            return Math.max(1, amount(index));
        }

        @Override
        public boolean isValid(int index, ResourceVariant<Item> resource) {
            Objects.requireNonNull(resource, "resource");
            StoragePreconditions.index(index, size());
            return ResourceTypes.ITEM.equals(resource.type()) && !resource.isBlank();
        }

        @Override
        public TransferSupport support(int index) {
            ResourceStorage<ResourceVariant<Item>> storage = requireItemStorage();
            TransferSupport extraction = storage.support(index).supportsExtraction()
                    ? TransferSupport.EXTRACT_ONLY
                    : TransferSupport.NONE;
            if (!supportsInsertion())
                return extraction;
            return extraction == TransferSupport.EXTRACT_ONLY
                    ? TransferSupport.BOTH
                    : TransferSupport.INSERT_ONLY;
        }

        @Override
        public long insert(
                int index,
                ResourceVariant<Item> resource,
                long maxAmount,
                TransferContext transaction) {
            StoragePreconditions.index(index, size());
            return insert(resource, maxAmount, transaction);
        }

        @Override
        public long insert(ResourceVariant<Item> resource, long maxAmount, TransferContext transaction) {
            StoragePreconditions.check(resource, maxAmount);
            Objects.requireNonNull(transaction, "transaction");
            if (maxAmount == 0)
                return 0;

            ConveyorStorage conveyorStorage = resolveConveyorStorage();
            if (this.pendingInsert != null || conveyorStorage == null || !conveyorStorage.canAcceptIncomingItem())
                return 0;

            updateSnapshots(transaction);
            this.pendingInsert = new PendingInsert(resource);
            return 1;
        }

        @Override
        public boolean supportsInsertion() {
            ConveyorStorage conveyorStorage = resolveConveyorStorage();
            return this.pendingInsert == null
                    && conveyorStorage != null
                    && conveyorStorage.canAcceptIncomingItem();
        }

        @Override
        public boolean supportsExtraction() {
            ResourceStorage<ResourceVariant<Item>> storage = resolveItemStorage();
            return storage != null && storage.supportsExtraction();
        }

        @Override
        public long extract(
                int index,
                ResourceVariant<Item> resource,
                long maxAmount,
                TransferContext transaction) {
            ResourceStorage<ResourceVariant<Item>> storage = resolveItemStorage();
            return storage != null
                    ? storage.extract(index, resource, maxAmount, transaction)
                    : 0;
        }

        @Override
        public long extract(
                ResourceVariant<Item> resource,
                long maxAmount,
                TransferContext transaction) {
            ResourceStorage<ResourceVariant<Item>> storage = resolveItemStorage();
            return storage != null ? storage.extract(resource, maxAmount, transaction) : 0;
        }

        @Override
        public long version() {
            ResourceStorage<ResourceVariant<Item>> storage = resolveItemStorage();
            return 31 * this.version + (storage == null ? 0 : storage.version());
        }

        @Override
        protected PendingInsert createSnapshot() {
            return this.pendingInsert == null ? PendingInsert.EMPTY : this.pendingInsert;
        }

        @Override
        protected void restoreSnapshot(PendingInsert snapshot) {
            this.pendingInsert = snapshot.isEmpty() ? null : snapshot;
        }

        @Override
        protected void onFinalCommit(PendingInsert originalState) {
            if (this.pendingInsert == null)
                return;

            commitInsert(this.level, this.conveyorStorage.getPos(), this.pendingInsert.variant());
            this.pendingInsert = null;
            this.version++;
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

        private @Nullable ResourceStorage<ResourceVariant<Item>> resolveItemStorage() {
            ConveyorStorage conveyorStorage = resolveConveyorStorage();
            return conveyorStorage == null ? null : conveyorStorage.getItemStorage();
        }

        private ResourceStorage<ResourceVariant<Item>> requireItemStorage() {
            ResourceStorage<ResourceVariant<Item>> storage = resolveItemStorage();
            if (storage == null)
                throw new IllegalStateException("The feeder conveyor is not attached to a conveyor network");
            return storage;
        }

        private static void commitInsert(
                ServerLevel level,
                BlockPos conveyorPos,
                ResourceVariant<Item> variant) {
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
            if (conveyorStorage == null)
                return;

            var conveyorItem = new ConveyorItem(conveyorPos, toStack(variant, 1));
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

    private static ItemStack toStack(ResourceVariant<Item> variant, int amount) {
        return new ItemStack(variant.holder(), amount, variant.components());
    }

    private record PendingInsert(ResourceVariant<Item> variant) {
        private static final PendingInsert EMPTY = new PendingInsert(ResourceTypes.ITEM.empty());

        private boolean isEmpty() {
            return this.variant.isBlank();
        }
    }
}
