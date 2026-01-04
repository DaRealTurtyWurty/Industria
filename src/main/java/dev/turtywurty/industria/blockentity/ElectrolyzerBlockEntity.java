package dev.turtywurty.industria.blockentity;

import dev.turtywurty.gasapi.api.GasVariant;
import dev.turtywurty.gasapi.api.storage.GasStorage;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.*;
import dev.turtywurty.industria.blockentity.util.gas.GasStack;
import dev.turtywurty.industria.blockentity.util.gas.OutputGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.SyncingGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.WrappedGasStorage;
import dev.turtywurty.industria.blockentity.util.heat.SyncingHeatStorage;
import dev.turtywurty.industria.blockentity.util.heat.WrappedHeatStorage;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.multiblock.LocalDirection;
import dev.turtywurty.industria.multiblock.PortType;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockable;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import dev.turtywurty.industria.multiblock.old.PositionedPortRule;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.ElectrolyzerRecipe;
import dev.turtywurty.industria.recipe.input.ElectrolyzerRecipeInput;
import dev.turtywurty.industria.screenhandler.ElectrolyzerScreenHandler;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ElectrolyzerBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper, AutoMultiblockable {
    public static final Component TITLE = Industria.containerTitle("electrolyzer");

    private static final List<PositionedPortRule> PORT_RULES = List.of(
            PositionedPortRule.when(p -> p.y() == 1)
                    .on(LocalDirection.UP)
                    .types(PortType.input(TransferType.ITEM), PortType.input(TransferType.ENERGY))
                    .build(),

            PositionedPortRule.when(p -> p.z() == 0)
                    .on(LocalDirection.BACK)
                    .types(PortType.input(TransferType.ITEM))
                    .build(),

            PositionedPortRule.when(p -> p.x() == -1)
                    .on(LocalDirection.LEFT)
                    .types(PortType.input(TransferType.ITEM))
                    .build(),

            PositionedPortRule.when(p -> p.x() == 1)
                    .on(LocalDirection.RIGHT)
                    .types(PortType.input(TransferType.ITEM))
                    .build(),

            PositionedPortRule.when(p -> p.z() == -1)
                    .on(LocalDirection.FRONT)
                    .types(PortType.io(TransferType.ITEM))
                    .build(),

            PositionedPortRule.when(p -> p.y() == 0)
                    .on(LocalDirection.DOWN)
                    .types(PortType.io(TransferType.ITEM))
                    .build(),

            PositionedPortRule.when(p -> p.z() == -1)
                    .on(LocalDirection.FRONT)
                    .types(PortType.input(TransferType.FLUID))
                    .build(),

            PositionedPortRule.when(p -> p.y() == 0)
                    .on(LocalDirection.DOWN)
                    .types(PortType.output(TransferType.GAS), PortType.input(TransferType.HEAT))
                    .build()
    );

    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedGasStorage<SingleGasStorage> wrappedGasStorage = new WrappedGasStorage<>();
    private final WrappedHeatStorage<SimpleHeatStorage> wrappedHeatStorage = new WrappedHeatStorage<>();

    private final List<BlockPos> multiblockPositions = new ArrayList<>();

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
                default -> throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for " + getCount());
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> electrolyteConversionProgress = value;
                case 3 -> maxElectrolyteConversionProgress = value;
                default -> throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for " + getCount());
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
        super(BlockInit.ELECTROLYZER, BlockEntityTypeInit.ELECTROLYZER, pos, state);

        this.wrappedContainerStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.UP);
        this.wrappedContainerStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.SOUTH);
        this.wrappedContainerStorage.addInsertOnlyInventory(new PredicateSimpleInventory(this, 1,
                (stack, integer) -> stack.is(TagList.Items.ELECTROLYSIS_RODS)), Direction.WEST);
        this.wrappedContainerStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.EAST);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createFluidPredicate(() -> {
                    SyncingFluidStorage outputFluidTank = getOutputFluidStorage();
                    return new FluidStack(outputFluidTank.variant, outputFluidTank.amount);
                })), Direction.NORTH);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createGasPredicate(() -> {
                    SyncingGasStorage outputGasTank = getOutputGasStorage();
                    return new GasStack(outputGasTank.variant, outputGasTank.amount);
                })), Direction.DOWN);
        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidConstants.BUCKET * 5), Direction.NORTH);
        this.wrappedFluidStorage.addStorage(new OutputFluidStorage(this, FluidConstants.BUCKET * 5), Direction.DOWN);

        this.wrappedGasStorage.addStorage(new OutputGasStorage(this, FluidConstants.BUCKET * 5), Direction.NORTH);

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 10_000_000, 100_000, 0), Direction.UP);

        this.wrappedHeatStorage.addStorage(new SyncingHeatStorage(this, 1000, 1000, 0), Direction.DOWN);
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
        SyncingHeatStorage heatStorage = getHeatStorage();
        return List.of(inputInventory, electrolyteInventory, anodeInventory, cathodeInventory, energyStorage, electrolyteFluidStorage, outputFluidStorage, outputGasStorage, heatStorage);
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
                    electrolyteFluidStorage.variant = fluidStack.variant();
                    electrolyteFluidStorage.amount += fluidStack.amount();

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
            if (energyStorage.amount >= recipe.energyCost()) {
                energyStorage.amount -= recipe.energyCost();
                this.progress++;
                update();
            }
        }
    }

    private void processOutputs() {
        SyncingFluidStorage outputFluidStorage = getOutputFluidStorage();
        if (outputFluidStorage.canInsert(this.leftoverOutputFluid)) {
            long inserted = Math.min(outputFluidStorage.getCapacity() - outputFluidStorage.amount, this.leftoverOutputFluid.amount());
            outputFluidStorage.variant = this.leftoverOutputFluid.variant();
            outputFluidStorage.amount += inserted;

            this.leftoverOutputFluid = this.leftoverOutputFluid.withAmount(this.leftoverOutputFluid.amount() - inserted);
            update();
        }

        SyncingGasStorage outputGasStorage = getOutputGasStorage();
        if (outputGasStorage.canInsert(this.leftoverOutputGas)) {
            long inserted = Math.min(outputGasStorage.getCapacity() - outputGasStorage.amount, this.leftoverOutputGas.amount());
            outputGasStorage.variant = this.leftoverOutputGas.variant();
            outputGasStorage.amount += inserted;

            this.leftoverOutputGas = this.leftoverOutputGas.withAmount(this.leftoverOutputGas.amount() - inserted);
            update();
        }
    }

    private void handleOutputSlots() {
        PredicateSimpleInventory outputFluidInv = getOutputFluidInventory();
        if (!outputFluidInv.isEmpty()) {
            ItemStack stack = outputFluidInv.getItem(0);
            Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
            if (storage != null && storage.supportsInsertion()) {
                SyncingFluidStorage outputFluid = getOutputFluidStorage();
                if (outputFluid.amount > 0) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long inserted = storage.insert(outputFluid.variant, FluidConstants.BUCKET, transaction);
                        if (inserted > 0) {
                            outputFluid.amount -= inserted;
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
            Storage<GasVariant> storage = GasStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
            if (storage != null && storage.supportsInsertion()) {
                SyncingGasStorage gasStorage = getOutputGasStorage();
                if (gasStorage.amount > 0) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long inserted = storage.insert(gasStorage.variant, FluidConstants.BUCKET, transaction);
                        if (inserted > 0) {
                            gasStorage.amount -= inserted;
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
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "EnergyStorage", this.wrappedEnergyStorage);
        ViewUtils.readChild(view, "GasStorage", this.wrappedGasStorage);
        ViewUtils.readChild(view, "HeatStorage", this.wrappedHeatStorage);

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
    public BlockPosPayload getScreenOpeningData(ServerPlayer player) {
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

    public SyncingHeatStorage getHeatStorage() {
        return (SyncingHeatStorage) this.wrappedHeatStorage.getStorage(0);
    }

    public ContainerStorage getInventoryProvider(Direction side) {
        return this.wrappedContainerStorage.getStorage(side);
    }

    public SingleFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public SingleGasStorage getGasProvider(Direction side) {
        return this.wrappedGasStorage.getStorage(side);
    }

    public EnergyStorage getEnergyProvider(Direction side) {
        return this.wrappedEnergyStorage.getStorage(side);
    }

    public SimpleHeatStorage getHeatProvider(Direction side) {
        return this.wrappedHeatStorage.getStorage(side);
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.ELECTROLYZER;
    }

    // x y z
    // 3 2 2
    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        if (this.level == null)
            return List.of();

        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 0; z++) {
                for (int y = 0; y <= 1; y++) {
                    if (x == 0 && z == 0 && y == 0)
                        continue;

                    BlockPos pos = this.worldPosition.offset(x, y, z);
                    if (this.level.getBlockState(pos).canBeReplaced()) {
                        positions.add(pos);
                    } else {
                        invalidPositions.add(pos);
                    }
                }
            }
        }

        return invalidPositions.isEmpty() ? positions : List.of();
    }

    @Override
    public List<BlockPos> getMultiblockPositions() {
        return this.multiblockPositions;
    }

    @Override
    public List<PositionedPortRule> getPortRules() {
        return PORT_RULES;
    }
}