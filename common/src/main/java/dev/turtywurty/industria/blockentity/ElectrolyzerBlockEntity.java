package dev.turtywurty.industria.blockentity;

import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.gasapi.api.storage.GasStorage;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.*;
import dev.turtywurty.industria.blockentity.util.gas.GasStack;
import dev.turtywurty.industria.blockentity.util.gas.OutputGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.SyncingGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.WrappedGasStorage;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.menu.ElectrolyzerScreenHandler;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.ElectrolyzerRecipe;
import dev.turtywurty.industria.recipe.input.ElectrolyzerRecipeInput;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.multiblocklib.port.PortRegistrar;
import dev.turtywurty.turtymultiloader.transfer.lookup.MutableItemContext;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static dev.turtywurty.industria.blockentity.util.StorageOperations.extract;
import static dev.turtywurty.industria.blockentity.util.StorageOperations.insert;

public class ElectrolyzerBlockEntity extends IndustriaMultiblockControllerBlockEntity implements BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("electrolyzer");

    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedFluidStorage<SyncingFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedGasStorage<SingleGasStorage> wrappedGasStorage = new WrappedGasStorage<>();

    private int progress, maxProgress;
    private int electrolyteConversionProgress, maxElectrolyteConversionProgress;
    private final ContainerData propertyDelegate = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> electrolyteConversionProgress;
                case 3 -> maxElectrolyteConversionProgress;
                default ->
                        throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for " + getCount());
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> electrolyteConversionProgress = value;
                case 3 -> maxElectrolyteConversionProgress = value;
                default ->
                        throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for " + getCount());
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };
    private ResourceKey<Recipe<?>> currentRecipeId;
    private FluidStack leftoverOutputFluid = FluidStack.EMPTY;
    private GasStack leftoverOutputGas = GasStack.EMPTY;

    public ElectrolyzerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ELECTROLYZER.get(), ModBlockEntityTypes.ELECTROLYZER.get(), pos, state);

        this.wrappedContainerStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.UP);
        this.wrappedContainerStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.SOUTH);
        this.wrappedContainerStorage.addInsertOnlyInventory(new PredicateSimpleInventory(this, 1,
                (stack, integer) -> stack.is(TagList.Items.ELECTROLYSIS_RODS)), Direction.WEST);
        this.wrappedContainerStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.EAST);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createFluidPredicate(() -> {
                    SyncingFluidStorage outputFluidTank = getOutputFluidStorage();
                    return new FluidStack(outputFluidTank.getResource(), outputFluidTank.getAmount());
                })), Direction.NORTH);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createGasPredicate(() -> {
                    SyncingGasStorage outputGasTank = getOutputGasStorage();
                    return new GasStack(outputGasTank.getResource(), outputGasTank.getAmount());
                })), Direction.DOWN);
        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidAmounts.BUCKET * 5), Direction.NORTH);
        this.wrappedFluidStorage.addStorage(new OutputFluidStorage(this, FluidAmounts.BUCKET * 5), Direction.DOWN);

        this.wrappedGasStorage.addStorage(new OutputGasStorage(this, FluidAmounts.BUCKET * 5), Direction.NORTH);

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 10_000_000, 100_000, 0), Direction.UP);
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingSimpleInventory inputInventory = getInputInventory();
        SyncingSimpleInventory electrolyteInventory = getElectrolyteInventory();
        PredicateSimpleInventory anodeInventory = getAnodeInventory();
        SyncingSimpleInventory cathodeInventory = getCathodeInventory();
        SyncingEnergyStorage energyStorage = getEnergyStorage();
        InputFluidStorage electrolyteFluidStorage = getElectrolyteFluidStorage();
        OutputFluidStorage outputFluidStorage = getOutputFluidStorage();
        OutputGasStorage outputGasStorage = getOutputGasStorage();
        return List.of(inputInventory, electrolyteInventory, anodeInventory, cathodeInventory, energyStorage, electrolyteFluidStorage, outputFluidStorage, outputGasStorage);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        processOutputs();
        handleOutputSlots();

        if (hasLeftover())
            return;

        SyncingSimpleInventory inputInventory = getInputInventory();
        PredicateSimpleInventory anodeInventory = getAnodeInventory();
        SyncingSimpleInventory cathodeInventory = getCathodeInventory();
        SyncingSimpleInventory electrolyteInventory = getElectrolyteInventory();
        InputFluidStorage electrolyteFluidStorage = getElectrolyteFluidStorage();
        var recipeInput = new ElectrolyzerRecipeInput(
                inputInventory,
                anodeInventory,
                cathodeInventory,
                electrolyteInventory,
                electrolyteFluidStorage
        );
        if (this.currentRecipeId == null) {
            Optional<RecipeHolder<ElectrolyzerRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
            if (recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().processTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeHolder<ElectrolyzerRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            this.currentRecipeId = null;
            this.maxProgress = 0;
            this.progress = 0;
            update();
            return;
        }

        ElectrolyzerRecipe recipe = recipeEntry.get().value();
        if (recipe.electrolyteItem().testForRecipe(electrolyteInventory.getItem(0))) {
            FluidStack fluidStack = recipe.electrolyteFluid();
            if (electrolyteFluidStorage.canInsert(fluidStack)) {
                if (this.electrolyteConversionProgress >= this.maxElectrolyteConversionProgress) {
                    insert(electrolyteFluidStorage, fluidStack.variant(), fluidStack.amount());

                    this.electrolyteConversionProgress = 0;
                    this.maxElectrolyteConversionProgress = 0;
                } else {
                    this.electrolyteConversionProgress++;
                    this.maxElectrolyteConversionProgress = 100;
                }
            } else {
                this.electrolyteConversionProgress = 0;
                this.maxElectrolyteConversionProgress = 0;
            }
        }

        if (!recipe.electrolyteFluid().testForRecipe(electrolyteFluidStorage))
            return;

        if (this.progress >= this.maxProgress) {
            if (recipe.outputFluid().amount() > 0) {
                this.leftoverOutputFluid = recipe.outputFluid();
            }

            if (recipe.outputGas().amount() > 0) {
                this.leftoverOutputGas = recipe.outputGas();
            }

            inputInventory.getItem(0).shrink(recipe.input().stackData().count());
            anodeInventory.getItem(0).hurtAndBreak(1, (ServerLevel) this.level, null, item -> {
            });

            this.progress = 0;
            update();
        } else {
            SyncingEnergyStorage energyStorage = getEnergyStorage();
            if (energyStorage.getAmount() >= recipe.energyCost()) {
                extract(energyStorage, recipe.energyCost());
                this.progress++;
                update();
            }
        }
    }

    private void processOutputs() {
        SyncingFluidStorage outputFluidStorage = getOutputFluidStorage();
        if (outputFluidStorage.canInsert(this.leftoverOutputFluid)) {
            long inserted = Math.min(outputFluidStorage.getCapacity() - outputFluidStorage.getAmount(), this.leftoverOutputFluid.amount());
            insert(outputFluidStorage, this.leftoverOutputFluid.variant(), inserted);

            this.leftoverOutputFluid = this.leftoverOutputFluid.withAmount(this.leftoverOutputFluid.amount() - inserted);
            update();
        }

        SyncingGasStorage outputGasStorage = getOutputGasStorage();
        if (outputGasStorage.canInsert(this.leftoverOutputGas)) {
            long inserted = Math.min(outputGasStorage.getCapacity() - outputGasStorage.getAmount(), this.leftoverOutputGas.amount());
            insert(outputGasStorage, this.leftoverOutputGas.variant(), inserted);

            this.leftoverOutputGas = this.leftoverOutputGas.withAmount(this.leftoverOutputGas.amount() - inserted);
            update();
        }
    }

    private void handleOutputSlots() {
        PredicateSimpleInventory outputFluidInv = getOutputFluidInventory();
        if (!outputFluidInv.isEmpty()) {
            ItemStack stack = outputFluidInv.getItem(0);
            ResourceStorage<ResourceVariant<Fluid>> storage =
                    MutableItemContext.ofContainerSlot(outputFluidInv, 0).find(StorageKeys.FLUID);
            if (storage != null && storage.supportsInsertion()) {
                SyncingFluidStorage outputFluid = getOutputFluidStorage();
                if (outputFluid.getAmount() > 0) {
                    try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                        long inserted = storage.insert(outputFluid.getResource(), FluidAmounts.BUCKET, transaction);
                        if (inserted > 0) {
                            outputFluid.extractInternal(outputFluid.getResource(), inserted, transaction);
                            transaction.commit();
                            update();
                        }
                    }
                }
            }
        }

        PredicateSimpleInventory outputGasInv = getOutputGasInventory();
        if (!outputGasInv.isEmpty()) {
            ItemStack stack = outputGasInv.getItem(0);
            ResourceStorage<ResourceVariant<Gas>> storage =
                    MutableItemContext.ofContainerSlot(outputGasInv, 0).find(GasStorage.KEY);
            if (storage != null && storage.supportsInsertion()) {
                SyncingGasStorage gasStorage = getOutputGasStorage();
                if (gasStorage.getAmount() > 0) {
                    try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                        long inserted = storage.insert(gasStorage.getResource(), FluidAmounts.BUCKET, transaction);
                        if (inserted > 0) {
                            gasStorage.extractInternal(gasStorage.getResource(), inserted, transaction);
                            transaction.commit();
                            update();
                        }
                    }
                }
            }
        }
    }

    private boolean hasLeftover() {
        return !this.leftoverOutputFluid.isEmpty() || !this.leftoverOutputGas.isEmpty();
    }

    private Optional<RecipeHolder<ElectrolyzerRecipe>> getCurrentRecipe(ElectrolyzerRecipeInput recipeInput) {
        if (this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return Optional.empty();

        return serverWorld.recipeAccess().getRecipeFor(ElectrolyzerRecipe.Type.INSTANCE, recipeInput, serverWorld);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(view, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.putChild(view, "EnergyStorage", this.wrappedEnergyStorage);
        ViewUtils.putChild(view, "GasStorage", this.wrappedGasStorage);

        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            view.store("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        if (!this.leftoverOutputFluid.isEmpty()) {
            view.store("LeftoverOutputFluid", FluidStack.CODEC.codec(), this.leftoverOutputFluid);
        }

        if (!this.leftoverOutputGas.isEmpty()) {
            view.store("LeftoverOutputGas", GasStack.CODEC.codec(), this.leftoverOutputGas);
        }
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "EnergyStorage", this.wrappedEnergyStorage);
        ViewUtils.readChild(view, "GasStorage", this.wrappedGasStorage);

        this.progress = view.getIntOr("Progress", 0);
        this.maxProgress = view.getIntOr("MaxProgress", 0);

        this.currentRecipeId = view.read("CurrentRecipe", ResourceKey.codec(Registries.RECIPE))
                .orElse(null);

        this.leftoverOutputFluid = view.read("LeftoverOutputFluid", FluidStack.CODEC.codec())
                .orElse(FluidStack.EMPTY);

        this.leftoverOutputGas = view.read("LeftoverOutputGas", GasStack.CODEC.codec())
                .orElse(GasStack.EMPTY);
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
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
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new ElectrolyzerScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage, this.propertyDelegate);
    }

    public SyncingSimpleInventory getInputInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(0);
    }

    public SyncingSimpleInventory getElectrolyteInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(1);
    }

    public PredicateSimpleInventory getAnodeInventory() {
        return (PredicateSimpleInventory) this.wrappedContainerStorage.getInventory(2);
    }

    public SyncingSimpleInventory getCathodeInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(3);
    }

    public PredicateSimpleInventory getOutputFluidInventory() {
        return (PredicateSimpleInventory) this.wrappedContainerStorage.getInventory(4);
    }

    public PredicateSimpleInventory getOutputGasInventory() {
        return (PredicateSimpleInventory) this.wrappedContainerStorage.getInventory(5);
    }

    public InputFluidStorage getElectrolyteFluidStorage() {
        return (InputFluidStorage) this.wrappedFluidStorage.getStorage(0);
    }

    public OutputFluidStorage getOutputFluidStorage() {
        return (OutputFluidStorage) this.wrappedFluidStorage.getStorage(1);
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(0);
    }

    public OutputGasStorage getOutputGasStorage() {
        return (OutputGasStorage) this.wrappedGasStorage.getStorage(0);
    }

    public ResourceStorage<ResourceVariant<Item>> getInventoryProvider(Direction side) {
        return this.wrappedContainerStorage.getStorage(side);
    }

    public SyncingFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public SingleGasStorage getGasProvider(Direction side) {
        return this.wrappedGasStorage.getStorage(side);
    }

    public ResourceStorage<ResourceVariant<UnitResource>> getEnergyProvider(Direction side) {
        return this.wrappedEnergyStorage.getStorage(side);
    }

    @Override
    protected void definePorts(PortRegistrar ports) {
        ports.both(TransferType.ITEM, this.wrappedContainerStorage::getCombinedStorage)
                .wherePosition(localOffset -> localOffset.getY() == 1
                        || localOffset.getZ() == 0
                        || localOffset.getX() == -1
                        || localOffset.getX() == 1
                        || localOffset.getZ() == -1
                        || localOffset.getY() == 0);
        ports.output(TransferType.ITEM, () -> this.wrappedContainerStorage.getStorage(Direction.NORTH))
                .wherePosition(localOffset -> localOffset.getZ() == -1);
        ports.output(TransferType.ITEM, () -> this.wrappedContainerStorage.getStorage(Direction.DOWN))
                .wherePosition(localOffset -> localOffset.getY() == 0);
        ports.input(TransferType.FLUID, this::getElectrolyteFluidStorage)
                .wherePosition(localOffset -> localOffset.getZ() == -1);
        ports.input(TransferType.ENERGY, this::getEnergyStorage)
                .wherePosition(localOffset -> localOffset.getY() == 1);
        ports.output(TransferType.GAS, this::getOutputGasStorage)
                .wherePosition(localOffset -> localOffset.getY() == 0);
    }
}
