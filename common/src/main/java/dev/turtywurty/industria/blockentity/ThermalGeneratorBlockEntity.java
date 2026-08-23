package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.EnergySpreader;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.menu.ThermalGeneratorScreenHandler;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.lookup.MutableItemContext;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.SimpleEnergyStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.turtywurty.industria.blockentity.util.StorageOperations.extract;
import static dev.turtywurty.industria.blockentity.util.StorageOperations.insertEnergy;

public class ThermalGeneratorBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, EnergySpreader, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("thermal_generator");

    private static final int CONSUME_RATE = 500;

    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedFluidStorage<SyncingFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();

    public ThermalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.THERMAL_GENERATOR.get(), ModBlockEntityTypes.THERMAL_GENERATOR.get(), pos, state);

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 0, 5000));
        this.wrappedFluidStorage.addStorage(new SyncingFluidStorage(this, FluidAmounts.BUCKET * 10));
        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 1));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var energy = (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        var fluid = (SyncingFluidStorage) this.wrappedFluidStorage.getStorage(null);
        var inventory = (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(0);
        return List.of(energy, fluid, inventory);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        extractLavaFromInventory();

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        spread(this.level, this.worldPosition, energyStorage);

        if (energyStorage.getAmount() >= energyStorage.getCapacity())
            return;

        SyncingFluidStorage fluidStorage = this.wrappedFluidStorage.getStorage(null);
        if (fluidStorage.getResource().isBlank() || fluidStorage.getAmount() < CONSUME_RATE)
            return;

        long storedLava = fluidStorage.getAmount();

        extract(fluidStorage, fluidStorage.getResource(), CONSUME_RATE);
        insertEnergy(energyStorage, Mth.clamp(storedLava, 0,
                energyStorage.getCapacity() - energyStorage.getAmount()));
        update();
    }

    private void extractLavaFromInventory() {
        SimpleContainer inventory = this.wrappedContainerStorage.getInventory(0);
        ResourceStorage<ResourceVariant<Fluid>> storage =
                MutableItemContext.ofContainerSlot(inventory, 0).find(StorageKeys.FLUID);
        if (storage == null || !storage.supportsExtraction())
            return;

        SyncingFluidStorage fluidStorage = this.wrappedFluidStorage.getStorage(null);
        if (fluidStorage.getAmount() >= fluidStorage.getCapacity())
            return;

        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            var lava = ResourceTypes.FLUID.of(Fluids.LAVA.builtInRegistryHolder());

            long extracted = storage.extract(lava, FluidAmounts.BUCKET, transaction);
            if (extracted > 0) {
                fluidStorage.insertInternal(lava, extracted, transaction);

                transaction.commit();
                update();
            }
        }
    }

    public boolean isValid(ItemStack itemStack, int slot) {
        ResourceStorage<ResourceVariant<Fluid>> storage = MutableItemContext.ofContainerSlot(
                new SimpleContainer(itemStack.copy()), 0).find(StorageKeys.FLUID);
        if (storage == null || !storage.supportsExtraction())
            return false;

        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            return storage.extract(ResourceTypes.FLUID.of(Fluids.LAVA.builtInRegistryHolder()), FluidAmounts.BUCKET, transaction) > 0;
        }
    }

    @Override
    public BlockPosPayload getMenuOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new ThermalGeneratorScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);

        ViewUtils.putChild(view, "EnergyStorage", this.wrappedEnergyStorage);
        ViewUtils.putChild(view, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);

        ViewUtils.readChild(view, "EnergyStorage", this.wrappedEnergyStorage);
        ViewUtils.readChild(view, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
    }

    public SyncingEnergyStorage getWrappedEnergyStorage() {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
    }

    public SyncingFluidStorage getWrappedFluidStorage() {
        return this.wrappedFluidStorage.getStorage(null);
    }

    @Override
    public WrappedContainerStorage<SimpleContainer> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }

    public ResourceStorage<ResourceVariant<UnitResource>> getEnergyProvider(Direction direction) {
        return this.wrappedEnergyStorage.getStorage(direction);
    }

    public SyncingFluidStorage getFluidProvider(Direction direction) {
        return this.wrappedFluidStorage.getStorage(direction);
    }

    public ResourceStorage<ResourceVariant<Item>> getInventoryProvider(Direction direction) {
        return this.wrappedContainerStorage.getStorage(direction);
    }
}
