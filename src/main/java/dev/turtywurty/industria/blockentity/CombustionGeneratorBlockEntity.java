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
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.CombustionGeneratorScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;

public class CombustionGeneratorBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, EnergySpreader, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("combustion_generator");

    private final WrappedEnergyStorage energyStorage = new WrappedEnergyStorage();
    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();

    private int burnTime = 0;
    private int fuelTime = 0;

    public CombustionGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.COMBUSTION_GENERATOR, BlockEntityTypeInit.COMBUSTION_GENERATOR, pos, state);

        this.energyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 0, 5000));
        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 1));
    }

    public boolean isFuel(ItemStack stack) {
        return this.level.fuelValues().isFuel(stack);
    }

    public int getFuelTime(ItemStack stack) {
        return this.level.fuelValues().burnDuration(stack);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var energy = (SyncingEnergyStorage) this.energyStorage.getStorage(null);
        var inventory = (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(0);
        return List.of(energy, inventory);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) this.energyStorage.getStorage(null);

        spread(this.level, this.worldPosition, energyStorage);

        if (energyStorage.getAmount() > energyStorage.getCapacity() - 20)
            return;

        if (this.burnTime > 0) {
            this.burnTime--;
            energyStorage.amount += 20;
            update();
        } else {
            SimpleContainer inventory = this.wrappedContainerStorage.getInventory(0);
            ItemStack stack = inventory.getItem(0);
            if (isFuel(stack)) {
                this.fuelTime = getFuelTime(stack);
                this.burnTime = getFuelTime(stack);
                stack.shrink(1);
                update();
            }
        }
    }

    public boolean isValid(ItemStack itemStack, int slot) {
        return slot == 0 && isFuel(itemStack);
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
        return new CombustionGeneratorScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        ViewUtils.putChild(view, "EnergyStorage", this.energyStorage);
        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);

        view.putInt("BurnTime", this.burnTime);
        view.putInt("FuelTime", this.fuelTime);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        ViewUtils.readChild(view, "EnergyStorage", this.energyStorage);
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);

        this.burnTime = view.getIntOr("BurnTime", 0);
        this.fuelTime = view.getIntOr("FuelTime", 0);
    }

    public EnergyStorage getEnergyStorage() {
        return this.energyStorage.getStorage(null);
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
        return this.energyStorage.getStorage(direction);
    }

    public ContainerStorage getInventoryProvider(Direction direction) {
        return this.wrappedContainerStorage.getStorage(direction);
    }

    public int getBurnTime() {
        return this.burnTime;
    }

    public int getFuelTime() {
        return this.fuelTime;
    }
}