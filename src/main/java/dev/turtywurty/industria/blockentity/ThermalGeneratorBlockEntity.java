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
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.ThermalGeneratorScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;

public class ThermalGeneratorBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, EnergySpreader, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("thermal_generator");

    private static final int CONSUME_RATE = 500;

    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();

    public ThermalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.THERMAL_GENERATOR, BlockEntityTypeInit.THERMAL_GENERATOR, pos, state);

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 0, 5000));
        this.wrappedFluidStorage.addStorage(new SyncingFluidStorage(this, FluidConstants.BUCKET * 10));
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

        SingleFluidStorage fluidStorage = this.wrappedFluidStorage.getStorage(null);
        if (fluidStorage.isResourceBlank() || fluidStorage.getAmount() < CONSUME_RATE)
            return;

        long storedLava = fluidStorage.getAmount();

        fluidStorage.amount -= CONSUME_RATE;
        energyStorage.amount += Mth.clamp(storedLava, 0, energyStorage.getCapacity() - energyStorage.getAmount());
        if (energyStorage.getAmount() > energyStorage.getCapacity())
            energyStorage.amount = energyStorage.getCapacity();
        update();
    }

    private void extractLavaFromInventory() {
        Storage<FluidVariant> storage = ContainerItemContext.ofSingleSlot(this.wrappedContainerStorage.getStorage(null).getSlot(0)).find(FluidStorage.ITEM);
        if (storage == null || !storage.supportsExtraction())
            return;

        SingleFluidStorage fluidStorage = this.wrappedFluidStorage.getStorage(null);
        if (fluidStorage.getAmount() >= fluidStorage.getCapacity())
            return;

        try (Transaction transaction = Transaction.openOuter()) {
            var lava = FluidVariant.of(Fluids.LAVA);

            long extracted = storage.extract(lava, FluidConstants.BUCKET, transaction);
            if (extracted > 0) {
                fluidStorage.variant = lava;
                fluidStorage.amount += extracted;

                transaction.commit();
                update();
            }
        }
    }

    public boolean isValid(ItemStack itemStack, int slot) {
        Storage<FluidVariant> storage = ContainerItemContext.withConstant(itemStack).find(FluidStorage.ITEM);
        if (storage == null || !storage.supportsExtraction())
            return false;

        try (Transaction transaction = Transaction.openOuter()) {
            return storage.extract(FluidVariant.of(Fluids.LAVA), FluidConstants.BUCKET, transaction) > 0;
        }
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayer player) {
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

    public EnergyStorage getWrappedEnergyStorage() {
        return this.wrappedEnergyStorage.getStorage(null);
    }

    public SingleFluidStorage getWrappedFluidStorage() {
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

    public EnergyStorage getEnergyProvider(Direction direction) {
        return this.wrappedEnergyStorage.getStorage(direction);
    }

    public SingleFluidStorage getFluidProvider(Direction direction) {
        return this.wrappedFluidStorage.getStorage(direction);
    }

    public ContainerStorage getInventoryProvider(Direction direction) {
        return this.wrappedContainerStorage.getStorage(direction);
    }
}