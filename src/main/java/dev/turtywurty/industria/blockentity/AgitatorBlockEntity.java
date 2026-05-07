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
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.gas.InputGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.OutputGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.WrappedGasStorage;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.blockentity.util.slurry.InputSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.OutputSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.WrappedSlurryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.util.AgitatorPortType;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
        return null;
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
        update();
        return true;
    }

    public boolean setOutputMode(int index, AgitatorPortType portType) {
        validateOutputIndex(index);
        if (portType == null || this.outputModes[index] == portType || !isOutputPortEmpty(index))
            return false;

        this.outputModes[index] = portType;
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
    protected @Nullable Storage<ItemVariant> getItemStorageForExternal(BlockPos worldPos, BlockPos localOffset) {
        Integer inputIndex = getInputPortIndex(localOffset);
        if (inputIndex != null && this.inputModes[inputIndex] == AgitatorPortType.ITEM)
            return this.wrappedContainerStorage.getStorage(INPUT_DIRECTIONS[inputIndex]);

        Integer outputIndex = getOutputPortIndex(localOffset);
        if (outputIndex != null && this.outputModes[outputIndex] == AgitatorPortType.ITEM)
            return this.wrappedContainerStorage.getStorage(OUTPUT_DIRECTIONS[outputIndex]);

        return null;
    }

    @Override
    protected @Nullable Storage<FluidVariant> getFluidStorageForExternal(BlockPos worldPos, BlockPos localOffset) {
        Integer inputIndex = getInputPortIndex(localOffset);
        if (inputIndex != null && this.inputModes[inputIndex] == AgitatorPortType.FLUID)
            return this.inputFluidStorages[inputIndex];

        Integer outputIndex = getOutputPortIndex(localOffset);
        if (outputIndex != null && this.outputModes[outputIndex] == AgitatorPortType.FLUID)
            return this.outputFluidStorages[outputIndex];

        return null;
    }

    @Override
    protected @Nullable Storage<SlurryVariant> getSlurryStorageForExternal(BlockPos worldPos, BlockPos localOffset, @Nullable Direction side) {
        Integer inputIndex = getInputPortIndex(localOffset);
        if (inputIndex != null && this.inputModes[inputIndex] == AgitatorPortType.SLURRY)
            return this.inputSlurryStorages[inputIndex];

        Integer outputIndex = getOutputPortIndex(localOffset);
        if (outputIndex != null && this.outputModes[outputIndex] == AgitatorPortType.SLURRY)
            return this.outputSlurryStorages[outputIndex];

        return null;
    }

    @Override
    public @Nullable SingleGasStorage getGasStorageForExternal(BlockPos worldPos, @Nullable Direction side) {
        if (!isFormed())
            return side == null ? null : getGasProvider(side);

        BlockPos localOffset = getLocalOffsetFromController(worldPos);

        Integer inputIndex = getInputPortIndex(localOffset);
        if (inputIndex != null && this.inputModes[inputIndex] == AgitatorPortType.GAS)
            return this.inputGasStorages[inputIndex];

        Integer outputIndex = getOutputPortIndex(localOffset);
        if (outputIndex != null && this.outputModes[outputIndex] == AgitatorPortType.GAS)
            return this.outputGasStorages[outputIndex];

        return null;
    }

    @Override
    protected @Nullable EnergyStorage getEnergyStorageForExternal(BlockPos worldPos, BlockPos localOffset) {
        return ENERGY_OFFSET.equals(localOffset) ? getEnergyStorage() : null;
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
}
