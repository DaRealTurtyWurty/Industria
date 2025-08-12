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
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
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
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;

public class ThermalGeneratorBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, EnergySpreader, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("thermal_generator");

    private static final int CONSUME_RATE = 500;

    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();

    public ThermalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.THERMAL_GENERATOR, BlockEntityTypeInit.THERMAL_GENERATOR, pos, state);

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 0, 5000));
        this.wrappedFluidStorage.addStorage(new SyncingFluidStorage(this, FluidConstants.BUCKET * 10));
        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 1));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var energy = (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        var fluid = (SyncingFluidStorage) this.wrappedFluidStorage.getStorage(null);
        var inventory = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(0);
        return List.of(energy, fluid, inventory);
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        extractLavaFromInventory();

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        spread(this.world, this.pos, energyStorage);

        if (energyStorage.getAmount() >= energyStorage.getCapacity())
            return;

        SingleFluidStorage fluidStorage = this.wrappedFluidStorage.getStorage(null);
        if (fluidStorage.isResourceBlank() || fluidStorage.getAmount() < CONSUME_RATE)
            return;

        long storedLava = fluidStorage.getAmount();

        fluidStorage.amount -= CONSUME_RATE;
        energyStorage.amount += MathHelper.clamp(storedLava, 0, energyStorage.getCapacity() - energyStorage.getAmount());
        if(energyStorage.getAmount() > energyStorage.getCapacity())
            energyStorage.amount = energyStorage.getCapacity();
        update();
    }

    private void extractLavaFromInventory() {
        Storage<FluidVariant> storage = ContainerItemContext.ofSingleSlot(this.wrappedInventoryStorage.getStorage(null).getSlot(0)).find(FluidStorage.ITEM);
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
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ThermalGeneratorScreenHandler(syncId, playerInventory, this, this.wrappedInventoryStorage);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);

        ViewUtils.putChild(view, "EnergyStorage", this.wrappedEnergyStorage);
        ViewUtils.putChild(view, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.putChild(view, "Inventory", this.wrappedInventoryStorage);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        ViewUtils.readChild(view, "EnergyStorage", this.wrappedEnergyStorage);
        ViewUtils.readChild(view, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "Inventory", this.wrappedInventoryStorage);
    }

    public EnergyStorage getWrappedEnergyStorage() {
        return this.wrappedEnergyStorage.getStorage(null);
    }

    public SingleFluidStorage getWrappedFluidStorage() {
        return this.wrappedFluidStorage.getStorage(null);
    }

    @Override
    public WrappedInventoryStorage<SimpleInventory> getWrappedInventoryStorage() {
        return this.wrappedInventoryStorage;
    }

    @Override
    public Block getBlock() {
        return getCachedState().getBlock();
    }

    public EnergyStorage getEnergyProvider(Direction direction) {
        return this.wrappedEnergyStorage.getStorage(direction);
    }

    public SingleFluidStorage getFluidProvider(Direction direction) {
        return this.wrappedFluidStorage.getStorage(direction);
    }

    public InventoryStorage getInventoryProvider(Direction direction) {
        return this.wrappedInventoryStorage.getStorage(direction);
    }
}
