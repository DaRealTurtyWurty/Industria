package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariantCodecs;
import dev.turtywurty.turtymultiloader.transfer.storage.*;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;
import java.util.Objects;

public class TappingOutletBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity {
    private final SingleSlotStorage<ResourceVariant<Fluid>> fluidProvider = new SingleSlotStorage<>() {
        @Override
        public ResourceVariant<Fluid> resource(int index) {
            StoragePreconditions.index(index, 1);
            SyncingFluidStorage storage = getChamberStorage();
            if (storage == null)
                return ResourceTypes.FLUID.empty();

            return storage.resource(0);
        }

        @Override
        public long amount(int index) {
            StoragePreconditions.index(index, 1);
            SyncingFluidStorage storage = getChamberStorage();
            if (storage == null)
                return 0;

            return storage.amount(0);
        }

        @Override
        public long capacity(int index, ResourceVariant<Fluid> resource) {
            StoragePreconditions.index(index, 1);
            SyncingFluidStorage storage = getChamberStorage();
            if (storage == null)
                return 0;

            return storage.capacity(0, resource);
        }

        @Override
        public boolean isValid(int index, ResourceVariant<Fluid> resource) {
            StoragePreconditions.index(index, 1);
            SyncingFluidStorage storage = getChamberStorage();
            if (storage == null)
                return false;

            return storage.isValid(0, resource);
        }

        @Override
        public TransferSupport support(int index) {
            StoragePreconditions.index(index, 1);
            SyncingFluidStorage storage = getChamberStorage();
            if (storage == null)
                return TransferSupport.NONE;

            return TransferSupport.EXTRACT_ONLY;
        }

        @Override
        public long insert(int index, ResourceVariant<Fluid> resource, long maxAmount, TransferContext transaction) {
            StoragePreconditions.index(index, 1);
            StoragePreconditions.check(resource, maxAmount);
            return 0;
        }

        @Override
        public long extract(int index, ResourceVariant<Fluid> resource, long maxAmount, TransferContext transaction) {
            StoragePreconditions.index(index, 1);
            StoragePreconditions.check(resource, maxAmount);

            SyncingFluidStorage storage = getChamberStorage();
            if (storage == null)
                return 0;

            return storage.extract(0, resource, maxAmount, transaction);
        }
    };

    private BlockPos controllerPos;
    private boolean pouring;
    private ResourceVariant<Fluid> pouringFluid = ResourceTypes.FLUID.empty();

    public TappingOutletBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.TAPPING_OUTLET.get(), ModBlockEntityTypes.TAPPING_OUTLET.get(), pos, state);
    }

    public void startPouring() {
        if (this.level == null || this.level.isClientSide())
            return;

        SyncingFluidStorage storage = getChamberStorage();
        if (storage == null)
            return;

        this.pouring = true;
        setChanged();
    }

    public void setControllerPos(BlockPos controllerPos) {
        BlockPos oldControllerPos = this.controllerPos;
        this.controllerPos = controllerPos == null ? null : controllerPos.immutable();

        if (!Objects.equals(oldControllerPos, this.controllerPos)) {
            setChanged();
        }
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public SmelteryControllerBlockEntity.SmelteryChamber getChamber() {
        if (this.controllerPos == null || this.level == null || !this.level.isLoaded(this.controllerPos))
            return null;

        if (!(this.level.getBlockEntity(this.controllerPos) instanceof SmelteryControllerBlockEntity controller))
            return null;

        return controller.getChamberForOutlet(this.worldPosition);
    }

    private SyncingFluidStorage getChamberStorage() {
        if (isRemoved())
            return null;

        SmelteryControllerBlockEntity.SmelteryChamber chamber = getChamber();
        if (chamber == null)
            return null;

        return chamber.getFluidStorage();
    }

    public ResourceStorage<ResourceVariant<Fluid>> getFluidProvider(Direction direction) {
        return this.fluidProvider;
    }

    public ResourceVariant<Fluid> getPouringFluid() {
        return pouringFluid;
    }

    private void setPouringFluid(ResourceVariant<Fluid> pouringFluid) {
        if (!Objects.equals(this.pouringFluid, pouringFluid)) {
            this.pouringFluid = pouringFluid;
            update();
        }
    }

    private void stopPouring() {
        this.pouring = false;
        setPouringFluid(ResourceTypes.FLUID.empty());
        setChanged();
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        if (!this.pouring) {
            stopPouring();
            return;
        }

        SyncingFluidStorage storage = getChamberStorage();
        if (storage == null || storage.amount(0) <= 0) {
            stopPouring();
            return;
        }

        BlockPos receiverPos = this.worldPosition.below();
        if (!this.level.isLoaded(receiverPos)) {
            stopPouring();
            return;
        }

        BlockEntity receiverBe = this.level.getBlockEntity(receiverPos);
        if (!(receiverBe instanceof TappingOutletReceiver receiver)) {
            stopPouring();
            return;
        }

        SimpleContainer inventory = receiver.getInventory();
        if (inventory == null || !receiver.isAcceptingFluid()) {
            stopPouring();
            return;
        }

        SimpleSingleSlotStorage<ResourceVariant<Fluid>> fluidStorage = receiver.getFluidStorage();
        if (fluidStorage == null) {
            stopPouring();
            return;
        }

        ResourceVariant<Fluid> resource = storage.getResource();
        if (resource.isBlank()) {
            stopPouring();
            return;
        }

        long moved = this.fluidProvider.moveTo(fluidStorage, resource, FluidAmounts.NUGGET);
        if(moved > 0) {
            setPouringFluid(resource);
        }

        if (moved == 0 || storage.getAmount() <= 0 || fluidStorage.getAmount() >= fluidStorage.capacity(0, resource)) {
            stopPouring();
        }
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("ControllerPos", BlockPos.CODEC, this.controllerPos);
        output.store("PouringFluid", ResourceVariantCodecs.FLUID, this.pouringFluid);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.controllerPos = input.read("ControllerPos", BlockPos.CODEC).orElse(null);
        this.pouringFluid = input.read("PouringFluid", ResourceVariantCodecs.FLUID).orElse(ResourceTypes.FLUID.empty());
    }

    public interface TappingOutletReceiver {
        SimpleContainer getInventory();

        SimpleSingleSlotStorage<ResourceVariant<Fluid>> getFluidStorage();

        boolean isAcceptingFluid();
    }
}
