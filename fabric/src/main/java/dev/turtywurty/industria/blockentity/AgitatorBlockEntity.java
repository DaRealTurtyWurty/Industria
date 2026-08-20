package dev.turtywurty.industria.blockentity;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SingleSlurryStorage;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.InputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.OutputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.gas.GasStack;
import dev.turtywurty.industria.blockentity.util.gas.InputGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.OutputGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.WrappedGasStorage;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.blockentity.util.slurry.InputSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.OutputSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.blockentity.util.slurry.WrappedSlurryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.AgitatorRecipe;
import dev.turtywurty.industria.recipe.input.AgitatorPortStack;
import dev.turtywurty.industria.recipe.input.AgitatorRecipeInput;
import dev.turtywurty.industria.screenhandler.AgitatorScreenHandler;
import dev.turtywurty.industria.util.AgitatorPortType;
import dev.turtywurty.industria.util.OutputItemStack;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.multiblocklib.port.PortRegistrar;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AgitatorBlockEntity extends IndustriaMultiblockControllerBlockEntity implements BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("agitator");

    private static final int INPUT_PORT_COUNT = 3;
    private static final int OUTPUT_PORT_COUNT = 2;
    private static final long PORT_CAPACITY = FluidConstants.BUCKET * 10;

    private static final Direction[] INPUT_DIRECTIONS = {Direction.NORTH, Direction.WEST, Direction.UP};
    private static final Direction[] OUTPUT_DIRECTIONS = {Direction.SOUTH, Direction.EAST};

    private static final BlockPos[] INPUT_OFFSETS = {
            new BlockPos(0, 0, -1),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 1, 0)
    };
    private static final BlockPos[] OUTPUT_OFFSETS = {
            new BlockPos(0, 0, 1),
            new BlockPos(1, 0, 0)
    };
    private static final BlockPos ENERGY_OFFSET = new BlockPos(0, -1, 0);

    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedSlurryStorage<SingleSlurryStorage> wrappedSlurryStorage = new WrappedSlurryStorage<>();
    private final WrappedGasStorage<SingleGasStorage> wrappedGasStorage = new WrappedGasStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();

    private final SyncingSimpleInventory[] inputItemStorages = new SyncingSimpleInventory[INPUT_PORT_COUNT];
    private final OutputSimpleInventory[] outputItemStorages = new OutputSimpleInventory[OUTPUT_PORT_COUNT];
    private final InputFluidStorage[] inputFluidStorages = new InputFluidStorage[INPUT_PORT_COUNT];
    private final OutputFluidStorage[] outputFluidStorages = new OutputFluidStorage[OUTPUT_PORT_COUNT];
    private final InputSlurryStorage[] inputSlurryStorages = new InputSlurryStorage[INPUT_PORT_COUNT];
    private final OutputSlurryStorage[] outputSlurryStorages = new OutputSlurryStorage[OUTPUT_PORT_COUNT];
    private final InputGasStorage[] inputGasStorages = new InputGasStorage[INPUT_PORT_COUNT];
    private final OutputGasStorage[] outputGasStorages = new OutputGasStorage[OUTPUT_PORT_COUNT];
    private final AgitatorPortType[] inputModes = new AgitatorPortType[INPUT_PORT_COUNT];
    private final AgitatorPortType[] outputModes = new AgitatorPortType[OUTPUT_PORT_COUNT];
    private final ContainerData properties = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };
    private ResourceKey<Recipe<?>> currentRecipeId;
    private int progress;
    private int maxProgress;

    public AgitatorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.AGITATOR, BlockEntityTypeInit.AGITATOR, pos, state);

        Arrays.fill(this.inputModes, AgitatorPortType.ITEM);
        Arrays.fill(this.outputModes, AgitatorPortType.ITEM);

        for (int index = 0; index < INPUT_PORT_COUNT; index++) {
            Direction direction = INPUT_DIRECTIONS[index];

            this.inputItemStorages[index] = new SyncingSimpleInventory(this, 1);
            this.wrappedContainerStorage.addInsertOnlyInventory(this.inputItemStorages[index], direction);

            this.inputFluidStorages[index] = new InputFluidStorage(this, PORT_CAPACITY);
            this.wrappedFluidStorage.addStorage(this.inputFluidStorages[index], direction);

            this.inputSlurryStorages[index] = new InputSlurryStorage(this, PORT_CAPACITY);
            this.wrappedSlurryStorage.addStorage(this.inputSlurryStorages[index], direction);

            this.inputGasStorages[index] = new InputGasStorage(this, PORT_CAPACITY);
            this.wrappedGasStorage.addStorage(this.inputGasStorages[index], direction);
        }

        for (int index = 0; index < OUTPUT_PORT_COUNT; index++) {
            Direction direction = OUTPUT_DIRECTIONS[index];

            this.outputItemStorages[index] = new OutputSimpleInventory(this, 1);
            this.wrappedContainerStorage.addExtractOnlyInventory(this.outputItemStorages[index], direction);

            this.outputFluidStorages[index] = new OutputFluidStorage(this, PORT_CAPACITY);
            this.wrappedFluidStorage.addStorage(this.outputFluidStorages[index], direction);

            this.outputSlurryStorages[index] = new OutputSlurryStorage(this, PORT_CAPACITY);
            this.wrappedSlurryStorage.addStorage(this.outputSlurryStorages[index], direction);

            this.outputGasStorages[index] = new OutputGasStorage(this, PORT_CAPACITY);
            this.wrappedGasStorage.addStorage(this.outputGasStorages[index], direction);
        }

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 100_000, 2_000, 0), Direction.DOWN);
    }

    @Override
    public Block getBlock() {
        return BlockInit.AGITATOR;
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        AgitatorRecipeInput recipeInput = createRecipeInput();
        if (this.currentRecipeId == null) {
            Optional<RecipeHolder<AgitatorRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
            if (recipeEntry.isPresent() && canOutput(recipeEntry.get().value())) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().processTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeHolder<AgitatorRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
        if (recipeEntry.isEmpty()
                || !recipeEntry.get().id().equals(this.currentRecipeId)
                || !canOutput(recipeEntry.get().value())) {
            this.currentRecipeId = null;
            this.progress = 0;
            this.maxProgress = 0;
            update();
            return;
        }

        AgitatorRecipe recipe = recipeEntry.get().value();
        SyncingEnergyStorage energyStorage = getEnergyStorage();
        if (this.progress >= this.maxProgress) {
            outputRecipe(recipe);
            consumeInputs(recipe);
            this.currentRecipeId = null;
            this.progress = 0;
            this.maxProgress = 0;
            update();
            return;
        }

        if (energyStorage.amount >= recipe.energyCost()) {
            energyStorage.amount -= recipe.energyCost();
            this.progress++;
            update();
        }
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        List<SyncableStorage> storages = new ArrayList<>(1 + (INPUT_PORT_COUNT + OUTPUT_PORT_COUNT) * 4);
        storages.addAll(Arrays.asList(this.inputItemStorages));
        storages.addAll(Arrays.asList(this.outputItemStorages));
        storages.addAll(Arrays.asList(this.inputFluidStorages));
        storages.addAll(Arrays.asList(this.outputFluidStorages));
        storages.addAll(Arrays.asList(this.inputSlurryStorages));
        storages.addAll(Arrays.asList(this.outputSlurryStorages));
        storages.addAll(Arrays.asList(this.inputGasStorages));
        storages.addAll(Arrays.asList(this.outputGasStorages));
        storages.add(getEnergyStorage());
        return storages;
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayer serverPlayer) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AgitatorScreenHandler(containerId, inventory, this, this.wrappedContainerStorage, this.properties);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);

        output.putIntArray("InputModes", Arrays.stream(this.inputModes)
                .mapToInt(AgitatorPortType::ordinal)
                .toArray());
        output.putIntArray("OutputModes", Arrays.stream(this.outputModes)
                .mapToInt(AgitatorPortType::ordinal)
                .toArray());
        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            output.store("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        ViewUtils.putChild(output, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(output, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.putChild(output, "SlurryStorage", this.wrappedSlurryStorage);
        ViewUtils.putChild(output, "GasStorage", this.wrappedGasStorage);
        ViewUtils.putChild(output, "EnergyStorage", this.wrappedEnergyStorage);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        int[] inputModes = input.getIntArray("InputModes").orElse(new int[0]);
        for (int index = 0; index < this.inputModes.length; index++) {
            this.inputModes[index] = AgitatorPortType.fromOrdinal(index < inputModes.length ? inputModes[index] : AgitatorPortType.ITEM.ordinal());
        }

        int[] outputModes = input.getIntArray("OutputModes").orElse(new int[0]);
        for (int index = 0; index < this.outputModes.length; index++) {
            this.outputModes[index] = AgitatorPortType.fromOrdinal(index < outputModes.length ? outputModes[index] : AgitatorPortType.ITEM.ordinal());
        }
        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", 0);
        this.currentRecipeId = input.read("CurrentRecipe", ResourceKey.codec(Registries.RECIPE)).orElse(null);

        ViewUtils.readChild(input, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(input, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.readChild(input, "SlurryStorage", this.wrappedSlurryStorage);
        ViewUtils.readChild(input, "GasStorage", this.wrappedGasStorage);
        ViewUtils.readChild(input, "EnergyStorage", this.wrappedEnergyStorage);
    }

    public AgitatorPortType getInputMode(int index) {
        validateInputIndex(index);
        return this.inputModes[index];
    }

    public AgitatorPortType getOutputMode(int index) {
        validateOutputIndex(index);
        return this.outputModes[index];
    }

    public boolean setInputMode(int index, AgitatorPortType portType) {
        validateInputIndex(index);
        if (portType == null || this.inputModes[index] == portType || !isInputPortEmpty(index))
            return false;

        this.inputModes[index] = portType;
        notifyPortModeChanged(INPUT_OFFSETS[index]);
        update();
        return true;
    }

    public boolean setOutputMode(int index, AgitatorPortType portType) {
        validateOutputIndex(index);
        if (portType == null || this.outputModes[index] == portType || !isOutputPortEmpty(index))
            return false;

        this.outputModes[index] = portType;
        notifyPortModeChanged(OUTPUT_OFFSETS[index]);
        update();
        return true;
    }

    public boolean isInputPortEmpty(int index) {
        validateInputIndex(index);
        return switch (this.inputModes[index]) {
            case ITEM -> this.inputItemStorages[index].isEmpty();
            case FLUID -> this.inputFluidStorages[index].isResourceBlank() || this.inputFluidStorages[index].amount <= 0;
            case GAS -> this.inputGasStorages[index].isResourceBlank() || this.inputGasStorages[index].amount <= 0;
            case SLURRY -> this.inputSlurryStorages[index].isResourceBlank() || this.inputSlurryStorages[index].amount <= 0;
        };
    }

    public boolean isOutputPortEmpty(int index) {
        validateOutputIndex(index);
        return isOutputStorageEmpty(index);
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public boolean isProcessing() {
        return this.currentRecipeId != null;
    }

    private boolean isOutputStorageEmpty(int index) {
        return switch (this.outputModes[index]) {
            case ITEM -> this.outputItemStorages[index].isEmpty();
            case FLUID -> this.outputFluidStorages[index].isResourceBlank() || this.outputFluidStorages[index].amount <= 0;
            case GAS -> this.outputGasStorages[index].isResourceBlank() || this.outputGasStorages[index].amount <= 0;
            case SLURRY -> this.outputSlurryStorages[index].isResourceBlank() || this.outputSlurryStorages[index].amount <= 0;
        };
    }

    public @Nullable ContainerStorage getInventoryProvider(Direction side) {
        Integer inputIndex = getInputPortIndex(side);
        if (inputIndex != null && this.inputModes[inputIndex] == AgitatorPortType.ITEM)
            return this.wrappedContainerStorage.getStorage(side);

        Integer outputIndex = getOutputPortIndex(side);
        if (outputIndex != null && this.outputModes[outputIndex] == AgitatorPortType.ITEM)
            return this.wrappedContainerStorage.getStorage(side);

        return null;
    }

    public @Nullable SingleFluidStorage getFluidProvider(Direction side) {
        Integer inputIndex = getInputPortIndex(side);
        if (inputIndex != null && this.inputModes[inputIndex] == AgitatorPortType.FLUID)
            return this.inputFluidStorages[inputIndex];

        Integer outputIndex = getOutputPortIndex(side);
        if (outputIndex != null && this.outputModes[outputIndex] == AgitatorPortType.FLUID)
            return this.outputFluidStorages[outputIndex];

        return null;
    }

    public @Nullable SingleSlurryStorage getSlurryProvider(Direction side) {
        Integer inputIndex = getInputPortIndex(side);
        if (inputIndex != null && this.inputModes[inputIndex] == AgitatorPortType.SLURRY)
            return this.inputSlurryStorages[inputIndex];

        Integer outputIndex = getOutputPortIndex(side);
        if (outputIndex != null && this.outputModes[outputIndex] == AgitatorPortType.SLURRY)
            return this.outputSlurryStorages[outputIndex];

        return null;
    }

    public @Nullable SingleGasStorage getGasProvider(Direction side) {
        Integer inputIndex = getInputPortIndex(side);
        if (inputIndex != null && this.inputModes[inputIndex] == AgitatorPortType.GAS)
            return this.inputGasStorages[inputIndex];

        Integer outputIndex = getOutputPortIndex(side);
        if (outputIndex != null && this.outputModes[outputIndex] == AgitatorPortType.GAS)
            return this.outputGasStorages[outputIndex];

        return null;
    }

    public EnergyStorage getEnergyProvider(Direction side) {
        return side == Direction.DOWN ? getEnergyStorage() : null;
    }

    public SyncingSimpleInventory getInputItemStorage(int index) {
        validateInputIndex(index);
        return this.inputItemStorages[index];
    }

    public OutputSimpleInventory getOutputItemStorage(int index) {
        validateOutputIndex(index);
        return this.outputItemStorages[index];
    }

    public InputFluidStorage getInputFluidStorage(int index) {
        validateInputIndex(index);
        return this.inputFluidStorages[index];
    }

    public OutputFluidStorage getOutputFluidStorage(int index) {
        validateOutputIndex(index);
        return this.outputFluidStorages[index];
    }

    public InputSlurryStorage getInputSlurryStorage(int index) {
        validateInputIndex(index);
        return this.inputSlurryStorages[index];
    }

    public OutputSlurryStorage getOutputSlurryStorage(int index) {
        validateOutputIndex(index);
        return this.outputSlurryStorages[index];
    }

    public InputGasStorage getInputGasStorage(int index) {
        validateInputIndex(index);
        return this.inputGasStorages[index];
    }

    public OutputGasStorage getOutputGasStorage(int index) {
        validateOutputIndex(index);
        return this.outputGasStorages[index];
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(Direction.DOWN);
    }

    @Override
    protected void definePorts(PortRegistrar ports) {
        for (int index = 0; index < INPUT_OFFSETS.length; index++) {
            int portIndex = index;
            ports.input(TransferType.ITEM,
                            () -> this.wrappedContainerStorage.getStorage(INPUT_DIRECTIONS[portIndex]))
                    .wherePosition(localOffset -> INPUT_OFFSETS[portIndex].equals(localOffset)
                            && this.inputModes[portIndex] == AgitatorPortType.ITEM);
            ports.input(TransferType.FLUID, () -> this.inputFluidStorages[portIndex])
                    .wherePosition(localOffset -> INPUT_OFFSETS[portIndex].equals(localOffset)
                            && this.inputModes[portIndex] == AgitatorPortType.FLUID);
            ports.input(TransferType.SLURRY, () -> this.inputSlurryStorages[portIndex])
                    .wherePosition(localOffset -> INPUT_OFFSETS[portIndex].equals(localOffset)
                            && this.inputModes[portIndex] == AgitatorPortType.SLURRY);
            ports.input(TransferType.GAS, () -> this.inputGasStorages[portIndex])
                    .wherePosition(localOffset -> INPUT_OFFSETS[portIndex].equals(localOffset)
                            && this.inputModes[portIndex] == AgitatorPortType.GAS);
        }

        for (int index = 0; index < OUTPUT_OFFSETS.length; index++) {
            int portIndex = index;
            ports.output(TransferType.ITEM,
                            () -> this.wrappedContainerStorage.getStorage(OUTPUT_DIRECTIONS[portIndex]))
                    .wherePosition(localOffset -> OUTPUT_OFFSETS[portIndex].equals(localOffset)
                            && this.outputModes[portIndex] == AgitatorPortType.ITEM);
            ports.output(TransferType.FLUID, () -> this.outputFluidStorages[portIndex])
                    .wherePosition(localOffset -> OUTPUT_OFFSETS[portIndex].equals(localOffset)
                            && this.outputModes[portIndex] == AgitatorPortType.FLUID);
            ports.output(TransferType.SLURRY, () -> this.outputSlurryStorages[portIndex])
                    .wherePosition(localOffset -> OUTPUT_OFFSETS[portIndex].equals(localOffset)
                            && this.outputModes[portIndex] == AgitatorPortType.SLURRY);
            ports.output(TransferType.GAS, () -> this.outputGasStorages[portIndex])
                    .wherePosition(localOffset -> OUTPUT_OFFSETS[portIndex].equals(localOffset)
                            && this.outputModes[portIndex] == AgitatorPortType.GAS);
        }

        ports.input(TransferType.ENERGY, this::getEnergyStorage).at(ENERGY_OFFSET);
    }

    @Override
    public @Nullable SingleGasStorage getGasStorageForExternal(BlockPos worldPos, @Nullable Direction side) {
        if (!isFormed())
            return side == null ? null : getGasProvider(side);
        return super.getGasStorageForExternal(worldPos, side);
    }

    private static Integer getInputPortIndex(Direction side) {
        for (int index = 0; index < INPUT_DIRECTIONS.length; index++) {
            if (INPUT_DIRECTIONS[index] == side)
                return index;
        }

        return null;
    }

    private static Integer getOutputPortIndex(Direction side) {
        for (int index = 0; index < OUTPUT_DIRECTIONS.length; index++) {
            if (OUTPUT_DIRECTIONS[index] == side)
                return index;
        }

        return null;
    }

    private static Integer getInputPortIndex(BlockPos localOffset) {
        for (int index = 0; index < INPUT_OFFSETS.length; index++) {
            if (INPUT_OFFSETS[index].equals(localOffset))
                return index;
        }

        return null;
    }

    private static Integer getOutputPortIndex(BlockPos localOffset) {
        for (int index = 0; index < OUTPUT_OFFSETS.length; index++) {
            if (OUTPUT_OFFSETS[index].equals(localOffset))
                return index;
        }

        return null;
    }

    private static void validateInputIndex(int index) {
        if (index < 0 || index >= INPUT_PORT_COUNT)
            throw new IndexOutOfBoundsException("Input port index out of bounds: " + index);
    }

    private static void validateOutputIndex(int index) {
        if (index < 0 || index >= OUTPUT_PORT_COUNT)
            throw new IndexOutOfBoundsException("Output port index out of bounds: " + index);
    }

    private void notifyPortModeChanged(BlockPos localOffset) {
        if (this.level == null || this.level.isClientSide())
            return;

        if (!isFormed()) {
            BlockState state = getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
            this.level.updateNeighborsAt(this.worldPosition, state.getBlock());
            return;
        }

        BlockPos worldPos = getWorldPosFromLocalOffset(localOffset);
        BlockState state = this.level.getBlockState(worldPos);
        this.level.sendBlockUpdated(worldPos, state, state, Block.UPDATE_ALL);
        this.level.updateNeighborsAt(worldPos, state.getBlock());
    }

    private BlockPos getWorldPosFromLocalOffset(BlockPos localOffset) {
        BlockPos worldOffset = switch (getControllerFacing()) {
            case NORTH -> localOffset;
            case SOUTH -> new BlockPos(-localOffset.getX(), localOffset.getY(), -localOffset.getZ());
            case WEST -> new BlockPos(-localOffset.getZ(), localOffset.getY(), localOffset.getX());
            case EAST -> new BlockPos(localOffset.getZ(), localOffset.getY(), -localOffset.getX());
            default -> localOffset;
        };

        return this.worldPosition.offset(worldOffset);
    }

    private Optional<RecipeHolder<AgitatorRecipe>> getCurrentRecipe(AgitatorRecipeInput recipeInput) {
        if (!(this.level instanceof ServerLevel serverLevel))
            return Optional.empty();

        return serverLevel.recipeAccess().getRecipeFor(RecipeTypeInit.AGITATOR, recipeInput, serverLevel);
    }

    private AgitatorRecipeInput createRecipeInput() {
        List<AgitatorPortStack> inputs = new ArrayList<>(INPUT_PORT_COUNT);
        for (int index = 0; index < INPUT_PORT_COUNT; index++) {
            inputs.add(createPortStack(index));
        }

        return new AgitatorRecipeInput(inputs);
    }

    private AgitatorPortStack createPortStack(int index) {
        return switch (this.inputModes[index]) {
            case ITEM -> new AgitatorPortStack(
                    AgitatorPortType.ITEM,
                    this.inputItemStorages[index].getItem(0).copy(),
                    FluidStack.EMPTY,
                    GasStack.EMPTY,
                    SlurryStack.EMPTY
            );
            case FLUID -> new AgitatorPortStack(
                    AgitatorPortType.FLUID,
                    ItemStack.EMPTY,
                    new FluidStack(this.inputFluidStorages[index].variant, this.inputFluidStorages[index].amount),
                    GasStack.EMPTY,
                    SlurryStack.EMPTY
            );
            case GAS -> new AgitatorPortStack(
                    AgitatorPortType.GAS,
                    ItemStack.EMPTY,
                    FluidStack.EMPTY,
                    new GasStack(this.inputGasStorages[index].variant, this.inputGasStorages[index].amount),
                    SlurryStack.EMPTY
            );
            case SLURRY -> new AgitatorPortStack(
                    AgitatorPortType.SLURRY,
                    ItemStack.EMPTY,
                    FluidStack.EMPTY,
                    GasStack.EMPTY,
                    new SlurryStack(this.inputSlurryStorages[index].variant, this.inputSlurryStorages[index].amount)
            );
        };
    }

    private boolean canOutput(AgitatorRecipe recipe) {
        for (int index = 0; index < OUTPUT_PORT_COUNT; index++) {
            if (!canOutput(index, recipe.outputs().get(index)))
                return false;
        }

        return true;
    }

    private boolean canOutput(int index, AgitatorRecipe.AgitatorOutput output) {
        if (isEmpty(output))
            return true;

        if (this.outputModes[index] != output.type())
            return false;

        return switch (output.type()) {
            case ITEM -> canInsertItemOutput(index, output.item());
            case FLUID -> this.outputFluidStorages[index].canInsert(output.fluid());
            case GAS -> this.outputGasStorages[index].canInsert(output.gas());
            case SLURRY -> this.outputSlurryStorages[index].canInsert(output.slurry());
        };
    }

    private void outputRecipe(AgitatorRecipe recipe) {
        for (int index = 0; index < OUTPUT_PORT_COUNT; index++) {
            AgitatorRecipe.AgitatorOutput output = recipe.outputs().get(index);
            if (isEmpty(output))
                continue;

            switch (output.type()) {
                case ITEM -> insertItemOutput(index, createOutputItemStack(output.item()));
                case FLUID -> insertFluidOutput(index, output.fluid());
                case GAS -> insertGasOutput(index, output.gas());
                case SLURRY -> insertSlurryOutput(index, output.slurry());
            }
        }
    }

    private void consumeInputs(AgitatorRecipe recipe) {
        for (int index = 0; index < INPUT_PORT_COUNT; index++) {
            AgitatorRecipe.AgitatorInput input = recipe.inputs().get(index);
            if (input.isEmpty())
                continue;

            switch (input.type()) {
                case ITEM -> this.inputItemStorages[index].getItem(0).shrink(input.item().stackData().count());
                case FLUID -> this.inputFluidStorages[index].amount -= input.fluid().amount();
                case GAS -> this.inputGasStorages[index].amount -= input.gas().amount();
                case SLURRY -> this.inputSlurryStorages[index].amount -= input.slurry().amount();
            }
        }
    }

    private static boolean isEmpty(AgitatorRecipe.AgitatorOutput output) {
        return switch (output.type()) {
            case ITEM -> output.item() == OutputItemStack.EMPTY;
            case FLUID -> output.fluid().isEmpty();
            case GAS -> output.gas().isEmpty();
            case SLURRY -> output.slurry().isEmpty();
        };
    }

    private boolean canInsertItemOutput(int index, OutputItemStack output) {
        if (output == OutputItemStack.EMPTY || output.item() == null || output.count().maxInclusive() <= 0)
            return true;

        return this.outputItemStorages[index].canAddItem(new ItemStack(output.item(), output.count().maxInclusive()));
    }

    private void insertItemOutput(int index, ItemStack stack) {
        if (stack.isEmpty())
            return;

        this.outputItemStorages[index].addItem(stack);
    }

    private void insertFluidOutput(int index, FluidStack stack) {
        if (stack.isEmpty())
            return;

        this.outputFluidStorages[index].variant = stack.variant();
        this.outputFluidStorages[index].amount += stack.amount();
    }

    private void insertGasOutput(int index, GasStack stack) {
        if (stack.isEmpty())
            return;

        this.outputGasStorages[index].variant = stack.variant();
        this.outputGasStorages[index].amount += stack.amount();
    }

    private void insertSlurryOutput(int index, SlurryStack stack) {
        if (stack.isEmpty())
            return;

        this.outputSlurryStorages[index].variant = stack.variant();
        this.outputSlurryStorages[index].amount += stack.amount();
    }

    private ItemStack createOutputItemStack(OutputItemStack output) {
        return this.level == null ? ItemStack.EMPTY : output.createStack(this.level.getRandom());
    }
}
