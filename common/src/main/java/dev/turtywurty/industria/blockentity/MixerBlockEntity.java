package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.fluid.InputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.blockentity.util.slurry.OutputSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.blockentity.util.slurry.SyncingSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.WrappedSlurryStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.menu.MixerScreenHandler;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.MixerRecipe;
import dev.turtywurty.industria.recipe.input.MixerRecipeInput;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.TransferUtils;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.multiblocklib.port.PortRegistrar;
import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.slurryapi.api.storage.SingleSlurryStorage;
import dev.turtywurty.slurryapi.api.storage.SlurryStorage;
import dev.turtywurty.turtymultiloader.transfer.lookup.MutableItemContext;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static dev.turtywurty.industria.blockentity.util.StorageOperations.extract;
import static dev.turtywurty.industria.blockentity.util.StorageOperations.insert;

// Leaving this here as an example, just in case I decide to make this system in the future
// public TickBuilder createTickBuilder() {
//        return TickBuilder.builder()
//                .progress(this.progress)
//                .maxProgress(this.maxProgress)
//                .currentRecipeId(this.currentRecipeId)
//                .validateWorld(false)
//                .fluidInputThroughBucket(getBucketInputInventory(), getInputFluidTank())
//                .fluidOutputThroughBucket(getBucketOutputInventory(), getOutputFluidTank())
//                .tryClearItemBuffer(this.outputItemStack, getOutputInventory())
//                .tryClearFluidBuffer(this.outputSlurryStack, getOutputFluidTank())
//                .checkForRecipe(RecipeTypeInit.MIXER)
//                .onComplete(new OnCompleteBuilder()
//                        .checkEnergy(10)
//                        .extractEnergy(10)
//                        .craftRecipe()
//                        .insertOutput()
//                        .resetProgress()
//                        .build())
//                .onProgress(new OnProgressBuilder()
//                        .checkEnergy(10)
//                        .extractEnergy(10)
//                        .incrementProgress()
//                        .build());
//    }
public class MixerBlockEntity extends IndustriaMultiblockControllerBlockEntity implements BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("mixer");

    public final List<Vec3> mixingItemPositions = NonNullList.withSize(6, new Vec3(0, 1, 0));
    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedFluidStorage<SyncingFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedSlurryStorage<SingleSlurryStorage> wrappedSlurryStorage = new WrappedSlurryStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private int temperature = 175;
    private int progress, maxProgress;
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
    private ItemStack outputItemStack = ItemStack.EMPTY;
    private SlurryStack outputSlurryStack = SlurryStack.EMPTY;

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MIXER.get(), ModBlockEntityTypes.MIXER.get(), pos, state);

        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 6), Direction.EAST);
        this.wrappedContainerStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.WEST);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createFluidPredicate(() -> {
                    SyncingFluidStorage inputFluidTank = getInputFluidTank();
                    return new FluidStack(inputFluidTank.getResource(), inputFluidTank.getAmount());
                })), Direction.NORTH);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createEmptySlurryPredicate(() -> getOutputSlurryTank().getResource())), Direction.SOUTH);

        this.wrappedFluidStorage.addStorage(
                new InputFluidStorage(this, FluidAmounts.BUCKET * 10), Direction.UP);

        this.wrappedSlurryStorage.addStorage(
                new OutputSlurryStorage(this, FluidAmounts.BUCKET * 10), Direction.DOWN);

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 10_000, 1_000, 0));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingSimpleInventory inputInventory = getInputInventory();
        SyncingSimpleInventory outputInventory = getOutputInventory();
        SyncingSimpleInventory bucketInputInventory = getBucketInputInventory();
        SyncingSimpleInventory bucketOutputInventory = getBucketOutputInventory();

        SyncingFluidStorage inputFluidTank = getInputFluidTank();
        SyncingSlurryStorage outputSlurryTank = getOutputSlurryTank();

        SyncingEnergyStorage energy = getEnergyStorage();

        return List.of(inputInventory, outputInventory, bucketInputInventory, bucketOutputInventory, inputFluidTank, outputSlurryTank, energy);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        SyncingSimpleInventory bucketInputInventory = getBucketInputInventory();
        if (!bucketInputInventory.isEmpty()) {
            ItemStack bucket = bucketInputInventory.getItem(0);
            ResourceStorage<ResourceVariant<Fluid>> storage =
                    MutableItemContext.ofContainerSlot(bucketInputInventory, 0).find(StorageKeys.FLUID);
            if (storage != null && storage.supportsExtraction()) {
                SyncingFluidStorage inputFluidTank = getInputFluidTank();
                Optional<ResourceVariant<Fluid>> optVariant = TransferUtils.findFirstVariant(storage, inputFluidTank.getResource());
                optVariant.filter(variant -> !variant.isBlank()).ifPresent(variant -> {
                    try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                        long extracted = storage.extract(variant, FluidAmounts.BUCKET, transaction);
                        if (extracted > 0) {
                            inputFluidTank.insertInternal(variant, extracted, transaction);
                        }

                        transaction.commit();
                    }
                });
            }
        }

        SyncingSimpleInventory bucketOutputInventory = getBucketOutputInventory();
        if (!bucketOutputInventory.isEmpty()) {
            ItemStack bucket = bucketOutputInventory.getItem(0);
            ResourceStorage<ResourceVariant<Slurry>> storage =
                    MutableItemContext.ofContainerSlot(bucketOutputInventory, 0).find(SlurryStorage.KEY);
            if (storage != null && storage.supportsInsertion()) {
                SyncingSlurryStorage outputSlurryTank = getOutputSlurryTank();
                if (outputSlurryTank.getAmount() > 0) {
                    try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                        long inserted = storage.insert(outputSlurryTank.getResource(), FluidAmounts.BUCKET, transaction);
                        if (inserted > 0) {
                            outputSlurryTank.extractInternal(outputSlurryTank.getResource(), inserted, transaction);
                        }

                        transaction.commit();
                    }
                }
            }
        }

        if (!this.outputItemStack.isEmpty()) {
            SyncingSimpleInventory outputInventory = getOutputInventory();
            if (outputInventory.canAddItem(this.outputItemStack)) {
                this.outputItemStack = outputInventory.addItem(this.outputItemStack);
                update();
            }

            return;
        }

        if (!this.outputSlurryStack.isEmpty()) {
            SyncingSlurryStorage outputSlurryTank = getOutputSlurryTank();
            if (outputSlurryTank.canInsert(this.outputSlurryStack)) {
                long inserted = Math.min(outputSlurryTank.getCapacity() - outputSlurryTank.getAmount(), this.outputSlurryStack.amount());
                insert(outputSlurryTank, this.outputSlurryStack.variant(), inserted);
                this.outputSlurryStack = this.outputSlurryStack.withAmount(this.outputSlurryStack.amount() - inserted);
                update();
            }

            return;
        }

        MixerRecipeInput recipeInput = createRecipeInput();
        if (this.currentRecipeId == null) {
            Optional<RecipeHolder<MixerRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
            if (recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().processTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeHolder<MixerRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            this.currentRecipeId = null;
            this.maxProgress = 0;
            this.progress = 0;
            update();
            return;
        }

        MixerRecipe recipe = recipeEntry.get().value();
        if (this.progress >= this.maxProgress) {
            if (hasEnergy()) {
                consumeEnergy(recipe);

                ItemStack output = recipe.assemble(recipeInput);
                SyncingFluidStorage inputFluidTank = getInputFluidTank();
                extract(inputFluidTank, inputFluidTank.getResource(), recipe.inputFluid().amount());

                SyncingSimpleInventory outputInventory = getOutputInventory();
                SyncingSlurryStorage outputSlurryTank = getOutputSlurryTank();

                this.progress = 0;
                this.maxProgress = 0;
                this.currentRecipeId = null;

                if (outputInventory.canAddItem(output)) {
                    this.outputItemStack = outputInventory.addItem(output);
                } else {
                    this.outputItemStack = output;
                }

                SlurryStack outputSlurry = recipe.outputSlurry();
                if (outputSlurryTank.canInsert(outputSlurry)) {
                    long inserted = Math.min(outputSlurryTank.getCapacity() - outputSlurryTank.getAmount(), outputSlurry.amount());
                    insert(outputSlurryTank, outputSlurry.variant(), inserted);
                    outputSlurry = outputSlurry.withAmount(outputSlurry.amount() - inserted);
                }

                this.outputSlurryStack = outputSlurry;

                update();
            }
        } else {
            if (hasEnergy()) {
                this.progress++;
                consumeEnergy(recipe);
                update();
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);
        view.putInt("Temperature", this.temperature);
        if (this.currentRecipeId != null) {
            view.store("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.putChild(view, "SlurryTank", this.wrappedSlurryStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);

        if (!this.outputItemStack.isEmpty()) {
            view.store("OutputStack", ItemStack.CODEC, this.outputItemStack);
        }

        if (!this.outputSlurryStack.isEmpty()) {
            view.store("OutputSlurry", SlurryStack.CODEC.codec(), this.outputSlurryStack);
        }
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.progress = view.getIntOr("Progress", 0);
        this.maxProgress = view.getIntOr("MaxProgress", 0);
        this.temperature = view.getIntOr("Temperature", 0);
        this.currentRecipeId = view.read("CurrentRecipe", ResourceKey.codec(Registries.RECIPE))
                .orElse(null);

        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "SlurryTank", this.wrappedSlurryStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);
        this.outputItemStack = view.read("OutputStack", ItemStack.CODEC)
                .orElse(ItemStack.EMPTY);
        this.outputSlurryStack = view.read("OutputSlurry", SlurryStack.CODEC.codec())
                .orElse(SlurryStack.EMPTY);
    }

    private boolean hasEnergy() {
        return getEnergyStorage().getAmount() >= 100;
    }

    private void consumeEnergy(MixerRecipe recipe) {
        extract(getEnergyStorage(), recipe.maxTemp() - recipe.minTemp());
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public int getTemperature() {
        return this.temperature;
    }

    private Optional<RecipeHolder<MixerRecipe>> getCurrentRecipe(MixerRecipeInput recipeInput) {
        if (this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return Optional.empty();

        return serverWorld.recipeAccess().getRecipeFor(ModRecipeTypes.MIXER.get(), recipeInput, this.level);
    }

    private MixerRecipeInput createRecipeInput() {
        SyncingFluidStorage inputFluidTank = getInputFluidTank();
        return new MixerRecipeInput(getInputInventory(), new FluidStack(inputFluidTank.getResource(), inputFluidTank.getAmount()), this.temperature);
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
        return new MixerScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage, this.properties);
    }

    public SyncingSimpleInventory getInputInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(0);
    }

    public SyncingSimpleInventory getOutputInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(1);
    }

    public SyncingSimpleInventory getBucketInputInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(2);
    }

    public SyncingSimpleInventory getBucketOutputInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(3);
    }

    public SyncingFluidStorage getInputFluidTank() {
        return (SyncingFluidStorage) this.wrappedFluidStorage.getStorage(Direction.UP);
    }

    public SyncingSlurryStorage getOutputSlurryTank() {
        return (SyncingSlurryStorage) this.wrappedSlurryStorage.getStorage(Direction.DOWN);
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
    }

    public ResourceStorage<ResourceVariant<Item>> getInventoryProvider(Direction side) {
        return this.wrappedContainerStorage.getStorage(side);
    }

    public SyncingFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public SingleSlurryStorage getSlurryProvider(Direction side) {
        return this.wrappedSlurryStorage.getStorage(side);
    }

    public ResourceStorage<ResourceVariant<UnitResource>> getEnergyProvider(Direction side) {
        return this.wrappedEnergyStorage.getStorage(side);
    }

    @Override
    protected void definePorts(PortRegistrar ports) {
        ports.input(TransferType.ITEM, () -> this.wrappedContainerStorage.getStorage(Direction.EAST))
                .atSides(new BlockPos(1, 2, 0), Direction.UP);
        ports.output(TransferType.ITEM, () -> this.wrappedContainerStorage.getStorage(Direction.WEST))
                .at(new BlockPos(1, 0, 0));
        ports.input(TransferType.FLUID, this::getInputFluidTank)
                .where((offset, side) -> offset.equals(new BlockPos(-1, 2, 0)) && side == Direction.UP);
        ports.output(TransferType.SLURRY, this::getOutputSlurryTank)
                .at(new BlockPos(-1, 0, 0));
        ports.input(TransferType.ENERGY, this::getEnergyStorage)
                .at(new BlockPos(0, 2, 0));
    }

    @Override
    public WrappedContainerStorage<SimpleContainer> getWrappedContainerStorage() {
        return wrappedContainerStorage;
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }

    public boolean isMixing() {
        return this.progress > 0 && this.progress < this.maxProgress;
    }

    public boolean hasItemInputConnection() {
        if (this.level == null)
            return false;

        Direction connectionDirection = getWorldSideForLocalSide(Direction.EAST);
        ResourceStorage<ResourceVariant<Item>> storage = TransferType.ITEM.lookup(this.level,
                this.worldPosition.relative(connectionDirection).above(3),
                Direction.DOWN);
        return storage != null;
    }

    public boolean hasItemOutputConnection() {
        if (this.level == null)
            return false;

        Direction connectionDirection = getWorldSideForLocalSide(Direction.EAST);
        ResourceStorage<ResourceVariant<Item>> storage = TransferType.ITEM.lookup(this.level,
                this.worldPosition.relative(connectionDirection, 2),
                connectionDirection.getOpposite());
        return storage != null;
    }
}
