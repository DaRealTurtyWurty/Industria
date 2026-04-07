package dev.turtywurty.industria.blockentity;

import com.mojang.serialization.Codec;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.InputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.gas.InputGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.WrappedGasStorage;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.RecipeSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.AlloyFurnaceRecipe;
import dev.turtywurty.industria.recipe.RecyclingRecipe;
import dev.turtywurty.industria.screenhandler.ArcFurnaceScreenHandler;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.industria.util.enums.IndustriaEnum;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.*;

public class ArcFurnaceBlockEntity extends IndustriaMultiblockControllerBlockEntity implements BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("arc_furnace");

    private static final int INPUT_PORT_X = 2;
    private static final int OUTPUT_PORT_Z = 2;
    private static final int AUX_INPUT_PORT_X = -1;

    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedGasStorage<SingleGasStorage> wrappedGasStorage = new WrappedGasStorage<>();

    private final RecipeManager.CachedCheck<SingleRecipeInput, BlastingRecipe> blastingMatchGetter;
    private final RecipeManager.CachedCheck<RecipeSimpleInventory, AlloyFurnaceRecipe> alloyingMatchGetter;
    private final RecipeManager.CachedCheck<SingleRecipeInput, RecyclingRecipe> recyclingMatchGetter;
    private final List<ResourceKey<Recipe<?>>> currentRecipeIds = new ArrayList<>();
    private final NonNullList<ItemStack> bufferedOutputs = NonNullList.withSize(9, ItemStack.EMPTY);
    private final int[] progress = new int[9], maxProgress = new int[9];
    private Mode mode = Mode.BLASTING;
    private final ContainerData propertyDelegate = new ContainerData() {
        @Override
        public int getCount() {
            return 19;
        }

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> mode.ordinal();
                case 1, 2, 3, 4, 5, 6, 7, 8, 9 -> progress[index - 1];
                case 10, 11, 12, 13, 14, 15, 16, 17, 18 -> maxProgress[index - 10];
                default -> throw new IndexOutOfBoundsException("Invalid index: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> mode = Mode.values()[value];
                case 1, 2, 3, 4, 5, 6, 7, 8, 9 -> progress[index - 1] = value;
                case 10, 11, 12, 13, 14, 15, 16, 17, 18 -> maxProgress[index - 10] = value;
                default -> throw new IndexOutOfBoundsException("Invalid index: " + index);
            }
        }
    };

    public ArcFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.ARC_FURNACE, BlockEntityTypeInit.ARC_FURNACE, pos, state);

        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 9), Direction.WEST);
        this.wrappedContainerStorage.addInventory(new OutputSimpleInventory(this, 9), Direction.SOUTH);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 10_000_000, 1_000_000, 0), Direction.EAST);
        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidConstants.BUCKET * 10), Direction.EAST);
        this.wrappedGasStorage.addStorage(new InputGasStorage(this, FluidConstants.BUCKET * 5), Direction.EAST);

        this.blastingMatchGetter = RecipeManager.createCheck(RecipeType.BLASTING);
        this.alloyingMatchGetter = RecipeManager.createCheck(RecipeTypeInit.ALLOY_FURNACE);
        this.recyclingMatchGetter = RecipeManager.createCheck(RecipeTypeInit.RECYCLING);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of(getInputInventory(), getOutputInventory(), getEnergyStorage(), getFluidStorage(), getGasStorage());
    }

    @Override
    public void onTick() {
        if (level == null || level.isClientSide())
            return;

        if (hasBufferedOutput()) {
            attemptOutputBufferInsertion();
            return;
        }

        SimpleEnergyStorage energyStorage = getEnergyStorage();
        if (energyStorage.getAmount() < mode.getEnergyCostPerTick())
            return;

        List<ResourceKey<Recipe<?>>> matchingRecipeIds = calculateCurrentRecipeIds();
        if (matchingRecipeIds.isEmpty()) {
            this.currentRecipeIds.clear();
            for (int index = 0; index < this.progress.length; index++) {
                if (this.progress[index] > 0) {
                    this.progress[index]--;
                    if (this.progress[index] <= 0) {
                        this.maxProgress[index] = 0;
                    }

                    update();
                }
            }

            return;
        }

        for (int index = 0; index < matchingRecipeIds.size(); index++) {
            if (index > 0 && this.mode == Mode.ALLOYING)
                break;

            handleRecipeProcessing(matchingRecipeIds, index, energyStorage);
        }
    }

    @SuppressWarnings("DataFlowIssue") // We already check when this method is called if the server level is null
    private void handleRecipeProcessing(List<ResourceKey<Recipe<?>>> matchingRecipeIds, int index, SimpleEnergyStorage energyStorage) {
        ResourceKey<Recipe<?>> recipeId = matchingRecipeIds.get(index);
        if (recipeId == null)
            return;

        var serverLevel = (ServerLevel) level;
        SyncingSimpleInventory inputInventory = getInputInventory();
        var singleRecipeInput = new SingleRecipeInput(inputInventory.getItem(index));
        Optional<? extends Recipe<?>> recipeOpt = (switch (mode) {
            case BLASTING -> this.blastingMatchGetter.getRecipeFor(singleRecipeInput, serverLevel);
            case ALLOYING ->
                    this.alloyingMatchGetter.getRecipeFor(inputInventory, serverLevel).stream().filter(r -> r.id().equals(recipeId)).findFirst();
            case RECYCLING -> this.recyclingMatchGetter.getRecipeFor(singleRecipeInput, serverLevel);
        }).map(RecipeHolder::value);

        if (recipeOpt.isEmpty())
            return;

        Recipe<?> recipe = recipeOpt.get();
        if (this.currentRecipeIds.size() <= index || !this.currentRecipeIds.get(index).equals(recipeId)) {
            this.currentRecipeIds.add(index, recipeId);
            this.progress[index] = 0;
            this.maxProgress[index] = switch (mode) {
                case BLASTING -> ((BlastingRecipe) recipe).cookingTime() / 2;
                case ALLOYING -> ((AlloyFurnaceRecipe) recipe).smeltTime() / 2;
                case RECYCLING -> ((RecyclingRecipe) recipe).processTime() / 2;
            };
            update();
        } else {
            if (this.progress[index] < this.maxProgress[index]) {
                energyStorage.amount -= mode.getEnergyCostPerTick();
                this.progress[index]++;
                update();
                return;
            }

            if (this.progress[index] >= this.maxProgress[index]) {
                List<ItemStack> output = switch (mode) {
                    case BLASTING -> List.of(((BlastingRecipe) recipe).assemble(singleRecipeInput));
                    case ALLOYING -> List.of(((AlloyFurnaceRecipe) recipe).assemble(inputInventory));
                    case RECYCLING -> ((RecyclingRecipe) recipe).assemble(serverLevel.getRandom());
                };

                for (ItemStack stack : output) {
                    if (!stack.isEmpty()) {
                        if (getOutputInventory().canAddItem(stack)) {
                            getOutputInventory().addItem(stack);
                        } else {
                            this.bufferedOutputs.add(stack);
                        }
                    }
                }

                switch (mode) {
                    case BLASTING -> {
                        ItemStack inputStack = inputInventory.getItem(index);
                        inputStack.shrink(1);
                        inputInventory.setItem(index, inputStack);
                    }
                    case RECYCLING -> {
                        ItemStack inputStack = inputInventory.getItem(index);
                        inputStack.shrink(((RecyclingRecipe) recipe).input().stackData().count());
                        inputInventory.setItem(index, inputStack);
                    }
                    case ALLOYING -> {
                    } // This is a NO-OP because the recipe itself does this
                }

                this.progress[index] = 0;
                update();
            }
        }
    }

    private void attemptOutputBufferInsertion() {
        SimpleContainer inventory = getOutputInventory();

        List<ItemStack> newBufferedOutputs = new ArrayList<>();
        for (ItemStack output : this.bufferedOutputs) {
            if (inventory.canAddItem(output)) {
                output = inventory.addItem(output);
                if (!output.isEmpty()) {
                    newBufferedOutputs.add(output);
                }

                update();
            } else {
                newBufferedOutputs.add(output);
            }
        }

        for (int index = 0; index < this.bufferedOutputs.size(); index++) {
            this.bufferedOutputs.set(index, index < newBufferedOutputs.size() ? newBufferedOutputs.get(index) : ItemStack.EMPTY);
        }
    }

    private boolean hasBufferedOutput() {
        return !this.bufferedOutputs.stream().allMatch(ItemStack::isEmpty);
    }

    private List<ResourceKey<Recipe<?>>> calculateCurrentRecipeIds() {
        ServerLevel level = ((ServerLevel) this.level);
        if (level == null)
            return Collections.emptyList();

        SyncingSimpleInventory inputInventory = getInputInventory();
        return switch (this.mode) {
            case BLASTING -> {
                List<ResourceKey<Recipe<?>>> recipeIds = new ArrayList<>();

                for (int slot = 0; slot < inputInventory.getContainerSize(); slot++) {
                    var singleRecipeInput = new SingleRecipeInput(inputInventory.getItem(slot));
                    Optional<ResourceKey<Recipe<?>>> recipeKeyOpt = this.blastingMatchGetter.getRecipeFor(singleRecipeInput, level)
                            .map(RecipeHolder::id);
                    if (recipeKeyOpt.isPresent()) {
                        recipeIds.add(slot, recipeKeyOpt.get());
                    }
                }

                yield recipeIds;
            }
            case ALLOYING -> this.alloyingMatchGetter.getRecipeFor(inputInventory, level)
                    .stream()
                    .map(RecipeHolder::id)
                    .toList();
            case RECYCLING -> {
                List<ResourceKey<Recipe<?>>> recipeIds = new ArrayList<>();

                for (int slot = 0; slot < inputInventory.getContainerSize(); slot++) {
                    var singleRecipeInput = new SingleRecipeInput(inputInventory.getItem(slot));
                    Optional<ResourceKey<Recipe<?>>> recipeKeyOpt = this.recyclingMatchGetter.getRecipeFor(singleRecipeInput, level)
                            .map(RecipeHolder::id);
                    if (recipeKeyOpt.isPresent()) {
                        recipeIds.add(slot, recipeKeyOpt.get());
                    }
                }

                yield recipeIds;
            }
        };
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);

        view.putString("Mode", this.mode.getSerializedName());
        view.putIntArray("Progress", this.progress);
        view.putIntArray("MaxProgress", this.maxProgress);

        if (!this.currentRecipeIds.isEmpty()) {
            view.store("CurrentRecipeIds", ExtraCodecs.listOf(RECIPE_CODEC), this.currentRecipeIds);
        }

        if (hasBufferedOutput()) {
            view.store("BufferedOutputs", ExtraCodecs.listOf(ItemStack.CODEC),
                    this.bufferedOutputs.stream()
                            .filter(stack -> !stack.isEmpty())
                            .toList());
        }

        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
        ViewUtils.putChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.putChild(view, "GasTank", this.wrappedGasStorage);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);

        this.mode = view.read("Mode", Codec.STRING).flatMap(Mode::fromStringOptional).orElse(Mode.BLASTING);

        int[] progressArray = view.getIntArray("Progress").orElse(new int[0]);
        Arrays.setAll(this.progress, index -> index < progressArray.length ? progressArray[index] : 0);

        int[] maxProgressArray = view.getIntArray("MaxProgress").orElse(new int[0]);
        Arrays.setAll(this.maxProgress, index -> index < maxProgressArray.length ? maxProgressArray[index] : 0);

        this.currentRecipeIds.clear();
        view.read("CurrentRecipeIds", ExtraCodecs.listOf(RECIPE_CODEC)).ifPresent(this.currentRecipeIds::addAll);

        this.bufferedOutputs.clear();
        view.read("BufferedOutputs", ExtraCodecs.listOf(ItemStack.CODEC)).ifPresent(buff -> {
            for (int index = 0; index < Math.min(buff.size(), this.bufferedOutputs.size()); index++) {
                this.bufferedOutputs.set(index, buff.get(index));
            }
        });

        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "GasTank", this.wrappedGasStorage);
    }

    @Override
    protected @Nullable Storage<ItemVariant> getItemStorageForExternal(BlockPos worldPos, BlockPos localOffset) {
        if (isItemInputPort(localOffset))
            return this.wrappedContainerStorage.getStorage(Direction.WEST);

        if (isItemOutputPort(localOffset))
            return this.wrappedContainerStorage.getStorage(Direction.SOUTH);

        return null;
    }

    @Override
    protected @Nullable Storage<FluidVariant> getFluidStorageForExternal(BlockPos worldPos, BlockPos localOffset) {
        return isAuxInputPort(localOffset) ? getFluidStorage() : null;
    }

    @Override
    protected @Nullable EnergyStorage getEnergyStorageForExternal(BlockPos worldPos, BlockPos localOffset) {
        return isAuxInputPort(localOffset) ? getEnergyStorage() : null;
    }

    @Override
    public @Nullable SingleGasStorage getGasStorageForExternal(BlockPos worldPos, @Nullable Direction side) {
        if (isFormed())
            return isAuxInputPort(getLocalOffsetFromController(worldPos)) ? getGasStorage() : null;

        return side == Direction.EAST ? getGasStorage() : null;
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
        return new ArcFurnaceScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage, this.propertyDelegate);
    }

    @Override
    public Block getBlock() {
        return BlockInit.ARC_FURNACE;
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    public ContainerStorage getInventoryProvider(Direction side) {
        return this.wrappedContainerStorage.getStorage(side);
    }

    public SyncingEnergyStorage getEnergyProvider(Direction side) {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(side);
    }

    public SingleFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public SingleGasStorage getGasProvider(Direction side) {
        return this.wrappedGasStorage.getStorage(side);
    }

    public SyncingSimpleInventory getInputInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(Direction.WEST);
    }

    public OutputSimpleInventory getOutputInventory() {
        return (OutputSimpleInventory) this.wrappedContainerStorage.getInventory(Direction.SOUTH);
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return getEnergyProvider(Direction.EAST);
    }

    public InputFluidStorage getFluidStorage() {
        return (InputFluidStorage) getFluidProvider(Direction.EAST);
    }

    public InputGasStorage getGasStorage() {
        return (InputGasStorage) getGasProvider(Direction.EAST);
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setMode(Mode mode) {
        if (this.mode == mode) {
            return;
        }

        this.mode = mode;
        this.currentRecipeIds.clear();
        Arrays.fill(this.progress, 0);
        Arrays.fill(this.maxProgress, 0);
        update();
    }

    private boolean isItemInputPort(BlockPos localOffset) {
        return localOffset.getX() == INPUT_PORT_X;
    }

    private boolean isItemOutputPort(BlockPos localOffset) {
        return localOffset.getZ() == OUTPUT_PORT_Z;
    }

    private boolean isAuxInputPort(BlockPos localOffset) {
        return localOffset.getX() == AUX_INPUT_PORT_X;
    }

    public enum Mode implements IndustriaEnum<Mode> {
        BLASTING(10),
        ALLOYING(20),
        RECYCLING(30);

        private final String name;
        private final int energyCostPerTick;
        private final Component text;

        Mode(int energyCostPerTick) {
            this.name = name().toLowerCase(Locale.ROOT);
            this.energyCostPerTick = energyCostPerTick;
            this.text = Component.translatable("industria.arc_furnace.mode." + this.name);
        }

        public static Mode fromString(String name) {
            for (Mode mode : values()) {
                if (mode.name.equals(name))
                    return mode;
            }

            return null;
        }

        public static Optional<Mode> fromStringOptional(String name) {
            return Optional.ofNullable(fromString(name));
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int getEnergyCostPerTick() {
            return this.energyCostPerTick;
        }

        @Override
        public Mode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        @Override
        public Mode previous() {
            return values()[(ordinal() - 1 + values().length) % values().length];
        }

        @Override
        public Mode[] getValues() {
            return values();
        }

        @Override
        public Component getAsText() {
            return this.text;
        }
    }
}
