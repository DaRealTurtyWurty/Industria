package dev.turtywurty.industria.blockentity;

import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.gasapi.api.GasVariant;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.fluid.InputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.gas.OutputGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.SyncingGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.WrappedGasStorage;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModGases;
import dev.turtywurty.industria.menu.SteamBoilerMenu;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.FuelUtils;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.multiblocklib.port.PortRegistrar;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class SteamBoilerBlockEntity extends IndustriaMultiblockControllerBlockEntity
    implements BlockEntityContentsDropper,
        BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("steam_boiler");
    private static final float MAX_TEMPERATURE = 1000.0f;
    private static final float MIN_TEMPERATURE = 20.0f;
    private static final float DEFAULT_TARGET_TEMPERATURE = 100.0f;
    private static final float MAX_PRODUCTION_MULTIPLIER_MINUS_ONE = 4.0f;

    private final WrappedContainerStorage<SyncingSimpleInventory> wrappedInventoryStorage = new WrappedContainerStorage<>();
    private final WrappedFluidStorage<SyncingFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedGasStorage<SyncingGasStorage> wrappedGasStorage = new WrappedGasStorage<>();

    private float temperature = MIN_TEMPERATURE;
    private float burnTime = 0;
    private float maxBurnTime = 0;
    private float targetTemperature = DEFAULT_TARGET_TEMPERATURE;

    public SteamBoilerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.STEAM_BOILER.get(), ModBlockEntityTypes.STEAM_BOILER.get(), pos, state);
        this.wrappedInventoryStorage.addInsertOnlyInventory(
            new PredicateSimpleInventory(this, 1, (stack, _) -> FuelUtils.isFuel(stack)), Direction.SOUTH);
        this.wrappedFluidStorage.addStorage(
            new InputFluidStorage(this, FluidAmounts.BUCKET * 10, variant -> variant.value() == Fluids.WATER),
            Direction.UP);
        this.wrappedGasStorage.addStorage(new OutputGasStorage(this, FluidAmounts.BUCKET * 10), Direction.UP);
    }

    @Override
    public Block getBlock() {
        return ModBlocks.STEAM_BOILER.get();
    }

    @Override
    public void onTick() {
        if (!(this.level instanceof ServerLevel serverLevel))
            return;

        SyncingFluidStorage fluidStorage = getWaterStorage();
        if (fluidStorage.getResource().isBlank() || fluidStorage.getAmount() <= 0) {
            this.temperature = Math.max(this.temperature - 0.5f, MIN_TEMPERATURE);
            update();
            return;
        }

        SyncingGasStorage gasStorage = getSteamStorage();
        if (gasStorage.getAmount() >= gasStorage.getCapacity()) {
            this.temperature = Math.max(this.temperature - 0.5f, MIN_TEMPERATURE);
            update();
            return;
        }

        float burnRate = calculateBurnRate();
        if (this.burnTime <= 0) {
            SyncingSimpleInventory inventory = this.wrappedInventoryStorage.getInventory(Direction.SOUTH);
            if (inventory == null) {
                this.temperature = Math.max(this.temperature - 0.5f, MIN_TEMPERATURE);
                update();
                return;
            }

            ItemStack fuelStack = inventory.getItem(0);
            if (fuelStack.isEmpty()) {
                this.temperature = Math.max(this.temperature - 0.5f, MIN_TEMPERATURE);
                update();
                return;
            }

            this.maxBurnTime = FuelUtils.burnDuration(serverLevel, fuelStack);
            if (this.maxBurnTime <= 0) {
                this.temperature = Math.max(this.temperature - 0.5f, MIN_TEMPERATURE);
                update();
                return;
            }

            this.burnTime = this.maxBurnTime;
            fuelStack.shrink(1);
            inventory.setItem(0, fuelStack);
            update();
        }

        this.burnTime -= burnRate;
        this.temperature = Math.min(this.temperature + 0.5f, this.targetTemperature);

        float productionRate = calculateSteamProductionRate();
        if (productionRate <= 0.0f)
            return;

        long maxInsertable = Math.min(
            Math.min(gasStorage.getCapacity() - gasStorage.getAmount(), fluidStorage.getAmount()),
            (long) (productionRate * FluidAmounts.MILLIBUCKET));

        if (maxInsertable > 0) {
            try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                long extracted = fluidStorage.extractInternal(fluidStorage.resource(), maxInsertable, transaction);
                if (extracted > 0 && gasStorage.insertInternal(GasVariant.of(ModGases.STEAM.holder()), extracted,
                    transaction) == extracted) {
                    transaction.commit();
                }
            }
        }
    }

    public float calculateSteamProductionRate() {
        if (this.temperature < this.targetTemperature)
            return 0.0f;

        float progress = (this.temperature - this.targetTemperature) / (MAX_TEMPERATURE - this.targetTemperature);
        progress = Math.clamp(progress, 0.0f, 1.0f);

        return 1.0f + (progress * MAX_PRODUCTION_MULTIPLIER_MINUS_ONE);
    }

    public float calculateBurnRate() {
        if (this.temperature > this.targetTemperature)
            return 0.0f;

        float progress = (this.targetTemperature - this.temperature) / (this.targetTemperature - MIN_TEMPERATURE);
        progress = Math.clamp(progress, 0.0f, 1.0f);

        return 1.0f + (progress * MAX_PRODUCTION_MULTIPLIER_MINUS_ONE);
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedInventoryStorage;
    }

    public SyncingFluidStorage getWaterStorage() {
        return this.wrappedFluidStorage.getStorage(Direction.UP);
    }

    public SyncingGasStorage getSteamStorage() {
        return this.wrappedGasStorage.getStorage(Direction.UP);
    }

    public float getTemperature() {
        return this.temperature;
    }

    public float getTargetTemperature() {
        return this.targetTemperature;
    }

    public float getBurnProgress() {
        return this.maxBurnTime > 0 ? Math.clamp(this.burnTime / this.maxBurnTime, 0.0f, 1.0f) : 0.0f;
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        ViewUtils.putChild(output, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.putChild(output, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.putChild(output, "GasStorage", this.wrappedGasStorage);
        output.putFloat("BurnTime", this.burnTime);
        output.putFloat("MaxBurnTime", this.maxBurnTime);
        output.putFloat("Temperature", this.temperature);
        output.putFloat("TargetTemperature", this.targetTemperature);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ViewUtils.readChild(input, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.readChild(input, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.readChild(input, "GasStorage", this.wrappedGasStorage);
        this.maxBurnTime = Math.max(input.getFloatOr("MaxBurnTime", 0), 0);
        this.burnTime = Math.clamp(input.getFloatOr("BurnTime", 0), 0, this.maxBurnTime);
        this.temperature = Math.clamp(input.getFloatOr("Temperature", 0), MIN_TEMPERATURE, MAX_TEMPERATURE);
        this.targetTemperature = Math.clamp(input.getFloatOr("TargetTemperature", DEFAULT_TARGET_TEMPERATURE),
            MIN_TEMPERATURE, MAX_TEMPERATURE);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingSimpleInventory fuelInventory = this.wrappedInventoryStorage.getInventory(Direction.SOUTH);
        return List.of(fuelInventory, getWaterStorage(), getSteamStorage());
    }

    @Override
    public BlockPosPayload getMenuOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    protected void definePorts(PortRegistrar ports) {
        ports.input(TransferType.ITEM, () -> this.wrappedInventoryStorage.getStorage(Direction.SOUTH))
            .atSides(new BlockPos(0, 0, -1), Direction.NORTH);
        ports.input(TransferType.FLUID, () -> this.wrappedFluidStorage.getStorage(Direction.UP))
            .atSides(new BlockPos(0, 3, -1), Direction.NORTH);
        ports.output(TransferType.GAS, () -> this.wrappedGasStorage.getStorage(Direction.UP))
            .atSides(new BlockPos(0, 3, 0), Direction.UP);
    }

    public ResourceStorage<ResourceVariant<Item>> getInventoryProvider(Direction side) {
        return this.wrappedInventoryStorage.getStorage(side);
    }

    public ResourceStorage<ResourceVariant<Fluid>> getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public ResourceStorage<ResourceVariant<Gas>> getGasProvider(Direction side) {
        return this.wrappedGasStorage.getStorage(side);
    }

    @Override
    public SteamBoilerMenu createMenu(int syncId, Inventory inventory, Player player) {
        return new SteamBoilerMenu(syncId, inventory, this);
    }
}
