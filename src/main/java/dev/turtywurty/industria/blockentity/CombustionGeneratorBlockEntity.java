package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.EnergySpreader;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.CombustionGeneratorScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;

public class CombustionGeneratorBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, EnergySpreader, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("combustion_generator");

    private final WrappedEnergyStorage energyStorage = new WrappedEnergyStorage();
    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();

    private int burnTime = 0;
    private int fuelTime = 0;

    public CombustionGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.COMBUSTION_GENERATOR, BlockEntityTypeInit.COMBUSTION_GENERATOR, pos, state);

        this.energyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 0, 5000));
        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 1));
    }

    public boolean isFuel(ItemStack stack) {
        return this.world.getFuelRegistry().isFuel(stack);
    }

    public int getFuelTime(ItemStack stack) {
        return this.world.getFuelRegistry().getFuelTicks(stack);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var energy = (SyncingEnergyStorage) this.energyStorage.getStorage(null);
        var inventory = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(0);
        return List.of(energy, inventory);
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient())
            return;

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) this.energyStorage.getStorage(null);

        spread(this.world, this.pos, energyStorage);

        if (energyStorage.getAmount() > energyStorage.getCapacity() - 20)
            return;

        if (this.burnTime > 0) {
            this.burnTime--;
            energyStorage.amount += 20;
            update();
        } else {
            SimpleInventory inventory = this.wrappedInventoryStorage.getInventory(0);
            ItemStack stack = inventory.getStack(0);
            if (isFuel(stack)) {
                this.fuelTime = getFuelTime(stack);
                this.burnTime = getFuelTime(stack);
                stack.decrement(1);
                update();
            }
        }
    }

    public boolean isValid(ItemStack itemStack, int slot) {
        return slot == 0 && isFuel(itemStack);
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
        return new CombustionGeneratorScreenHandler(syncId, playerInventory, this, this.wrappedInventoryStorage);
    }

    @Override
    protected void writeData(WriteView view) {
        ViewUtils.putChild(view, "EnergyStorage", this.energyStorage);
        ViewUtils.putChild(view, "Inventory", this.wrappedInventoryStorage);

        view.putInt("BurnTime", this.burnTime);
        view.putInt("FuelTime", this.fuelTime);
    }

    @Override
    protected void readData(ReadView view) {
        ViewUtils.readChild(view, "EnergyStorage", this.energyStorage);
        ViewUtils.readChild(view, "Inventory", this.wrappedInventoryStorage);

        this.burnTime = view.getInt("BurnTime", 0);
        this.fuelTime = view.getInt("FuelTime", 0);
    }

    public EnergyStorage getEnergyStorage() {
        return this.energyStorage.getStorage(null);
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
        return this.energyStorage.getStorage(direction);
    }

    public InventoryStorage getInventoryProvider(Direction direction) {
        return this.wrappedInventoryStorage.getStorage(direction);
    }

    public int getBurnTime() {
        return this.burnTime;
    }

    public int getFuelTime() {
        return this.fuelTime;
    }
}