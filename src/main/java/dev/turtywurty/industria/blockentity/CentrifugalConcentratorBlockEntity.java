package dev.turtywurty.industria.blockentity;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SingleSlurryStorage;
import dev.turtywurty.fabricslurryapi.api.storage.SlurryStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
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
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.multiblock.LocalDirection;
import dev.turtywurty.industria.multiblock.PortType;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockable;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import dev.turtywurty.industria.multiblock.old.Multiblockable;
import dev.turtywurty.industria.multiblock.old.PositionedPortRule;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.CentrifugalConcentratorRecipe;
import dev.turtywurty.industria.recipe.input.CentrifugalConcentratorRecipeInput;
import dev.turtywurty.industria.screenhandler.CentrifugalConcentratorScreenHandler;
import dev.turtywurty.industria.util.TransferUtils;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CentrifugalConcentratorBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, AutoMultiblockable, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("centrifugal_concentrator");

    private static final List<PositionedPortRule> PORT_RULES = List.of(
            PositionedPortRule.when(p -> p.y() == 0)
                    .on(LocalDirection.DOWN)
                    .types(PortType.input(TransferType.ENERGY), PortType.output(TransferType.SLURRY), PortType.output(TransferType.ITEM))
                    .build(),

            PositionedPortRule.when(p -> p.y() == 2)
                    .on(LocalDirection.UP)
                    .types(PortType.input(TransferType.FLUID), PortType.output(TransferType.ITEM))
                    .build()
    );

    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedSlurryStorage<SingleSlurryStorage> wrappedSlurryStorage = new WrappedSlurryStorage<>();

    private final List<BlockPos> multiblockPositions = new ArrayList<>();
    // Render data
    public float bowlRotation = 0.0f;
    private int progress, maxProgress;
    private ResourceKey<Recipe<?>> currentRecipeId;
    private boolean isProcessing = false;
    private int recipeRPM;
    private final ContainerData propertyDelegate = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> recipeRPM;
                default -> throw new IndexOutOfBoundsException("Invalid index: " + index);
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
            return 3;
        }
    };
    private ItemStack outputItemStack = ItemStack.EMPTY;
    private SlurryStack outputSlurryStack = SlurryStack.EMPTY;

    public CentrifugalConcentratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.CENTRIFUGAL_CONCENTRATOR, BlockEntityTypeInit.CENTRIFUGAL_CONCENTRATOR, pos, state);

        this.wrappedContainerStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.UP);
        this.wrappedContainerStorage.addExtractOnlyInventory(new OutputSimpleInventory(this, 1), Direction.DOWN);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createFluidPredicate(() -> {
                    SyncingFluidStorage inputFluidTank = getInputFluidTank();
                    return new FluidStack(inputFluidTank.variant, inputFluidTank.amount);
                })), Direction.NORTH);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createEmptySlurryPredicate(() -> getOutputSlurryTank().variant)), Direction.SOUTH);

        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidConstants.BUCKET * 10, variant -> variant.isOf(Fluids.WATER)), Direction.UP);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 10_000_000, 1_000_000, 0));
        this.wrappedSlurryStorage.addStorage(new OutputSlurryStorage(this, FluidConstants.BUCKET * 10), Direction.DOWN);
    }

    @Override
    public Block getBlock() {
        return BlockInit.CENTRIFUGAL_CONCENTRATOR;
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncableStorage ContainerStorage = getInputInventory();
        SyncableStorage outputContainerStorage = getOutputInventory();
        SyncableStorage bucketInputInventory = getBucketInputInventory();
        SyncableStorage bucketOutputInventory = getBucketOutputInventory();
        SyncableStorage inputFluidTank = getInputFluidTank();
        SyncableStorage outputSlurryTank = getOutputSlurryTank();
        SyncableStorage energyStorage = getEnergyStorage();
        return List.of(ContainerStorage, outputContainerStorage, bucketInputInventory, bucketOutputInventory,
                inputFluidTank, outputSlurryTank,
                energyStorage);
    }

    @Override
    public void onTick() {
        if (this.level == null)
            return;

        SyncingSimpleInventory bucketInputInventory = getBucketInputInventory();
        if (!bucketInputInventory.isEmpty()) {
            ItemStack bucket = bucketInputInventory.getItem(0);
            Storage<FluidVariant> storage = FluidStorage.ITEM.find(bucket, ContainerItemContext.withConstant(bucket));
            if (storage != null && storage.supportsExtraction()) {
                SyncingFluidStorage inputFluidTank = getInputFluidTank();
                Optional<FluidVariant> optVariant = TransferUtils.findFirstVariant(storage, inputFluidTank.variant);
                optVariant.filter(TransferVariant::isBlank).ifPresent(variant -> {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long extracted = storage.extract(variant, FluidConstants.BUCKET, transaction);
                        if (extracted > 0) {
                            inputFluidTank.variant = variant;
                            inputFluidTank.amount += extracted;
                        }

                        transaction.commit();
                    }
                });
            }
        }

        SyncingSimpleInventory bucketOutputInventory = getBucketOutputInventory();
        if (!bucketOutputInventory.isEmpty()) {
            ItemStack bucket = bucketOutputInventory.getItem(0);
            Storage<SlurryVariant> storage = SlurryStorage.ITEM.find(bucket, ContainerItemContext.withConstant(bucket));
            if (storage != null && storage.supportsInsertion()) {
                SyncingSlurryStorage outputSlurryTank = getOutputSlurryTank();
                if (outputSlurryTank.amount > 0) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long inserted = storage.insert(outputSlurryTank.variant, FluidConstants.BUCKET, transaction);
                        if (inserted > 0) {
                            outputSlurryTank.amount -= inserted;
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

            this.isProcessing = false;
            return;
        }

        if (!this.outputSlurryStack.isEmpty()) {
            SyncingSlurryStorage outputSlurryTank = getOutputSlurryTank();
            if (Objects.equals(outputSlurryTank.variant, this.outputSlurryStack.variant()) && outputSlurryTank.getCapacity() - outputSlurryTank.amount >= 0) {
                long inserted = Math.min(outputSlurryTank.getCapacity() - outputSlurryTank.amount, this.outputSlurryStack.amount());
                outputSlurryTank.variant = this.outputSlurryStack.variant();
                outputSlurryTank.amount += inserted;
                this.outputSlurryStack = this.outputSlurryStack.withAmount(this.outputSlurryStack.amount() - inserted);
                update();
            }

            this.isProcessing = false;
            return;
        }

        CentrifugalConcentratorRecipeInput recipeInput = createRecipeInput();
        if (this.currentRecipeId == null) {
            Optional<RecipeHolder<CentrifugalConcentratorRecipe>> recipeEntryOpt = getCurrentRecipe(recipeInput);
            if (recipeEntryOpt.isPresent()) {
                RecipeHolder<CentrifugalConcentratorRecipe> recipeEntry = recipeEntryOpt.get();
                this.currentRecipeId = recipeEntry.id();

                CentrifugalConcentratorRecipe recipe = recipeEntry.value();
                this.recipeRPM = recipe.rpm();
                this.maxProgress = recipe.processTime();
                this.progress = 0;
                update();
            }

            this.isProcessing = false;
            return;
        }

        Optional<RecipeHolder<CentrifugalConcentratorRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            this.currentRecipeId = null;
            this.recipeRPM = 0;
            this.maxProgress = 0;
            this.progress = 0;
            this.isProcessing = false;
            update();
            return;
        }

        CentrifugalConcentratorRecipe recipe = recipeEntry.get().value();
        this.recipeRPM = recipe.rpm();
        if (this.progress >= this.maxProgress) {
            if (hasEnergy(recipe)) {
                extractEnergy(recipe);
                getInputInventory().getItem(0).shrink(recipe.input().stackData().count());

                ItemStack output = recipe.assemble(recipeInput, this.level.registryAccess());
                SyncingFluidStorage inputFluidTank = getInputFluidTank();
                inputFluidTank.amount -= FluidConstants.BUCKET * 2;

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
                    long inserted = Math.min(outputSlurryTank.getCapacity() - outputSlurryTank.amount, outputSlurry.amount());
                    outputSlurryTank.amount += inserted;
                    outputSlurryTank.variant = outputSlurry.variant();
                    outputSlurry = outputSlurry.withAmount(outputSlurry.amount() - inserted);
                }

                this.outputSlurryStack = outputSlurry;

                update();
            }
        } else {
            if (hasEnergy(recipe)) {
                this.progress++;
                this.isProcessing = true;
                extractEnergy(recipe);
                update();
            }
        }
    }

    @Override
    public void onClientTick() {
        if (this.level == null || !this.isProcessing)
            return;

        this.bowlRotation += (this.recipeRPM / 60f / 20f);
    }

    private Optional<RecipeHolder<CentrifugalConcentratorRecipe>> getCurrentRecipe(CentrifugalConcentratorRecipeInput recipeInput) {
        if (this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return Optional.empty();

        return serverWorld.recipeAccess().getRecipeFor(RecipeTypeInit.CENTRIFUGAL_CONCENTRATOR, recipeInput, this.level);
    }

    private CentrifugalConcentratorRecipeInput createRecipeInput() {
        return new CentrifugalConcentratorRecipeInput(getInputInventory(), getInputFluidTank().amount);
    }

    private boolean hasEnergy(CentrifugalConcentratorRecipe recipe) {
        return getEnergyStorage().amount >= recipe.rpm() * 60L;
    }

    private void extractEnergy(CentrifugalConcentratorRecipe recipe) {
        getEnergyStorage().amount -= recipe.rpm() * 60L;
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return wrappedContainerStorage;
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            view.store("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        view.putInt("RecipeRPM", this.recipeRPM);
        view.putBoolean("IsProcessing", this.isProcessing);

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

        Multiblockable.write(this, view);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        this.progress = view.getIntOr("Progress", 0);
        this.maxProgress = view.getIntOr("MaxProgress", 0);
        this.currentRecipeId = view.read("CurrentRecipe", RECIPE_CODEC).orElse(null);
        this.recipeRPM = view.getIntOr("RecipeRPM", 0);
        this.isProcessing = view.getBooleanOr("IsProcessing", false);
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "SlurryTank", this.wrappedSlurryStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);
        this.outputItemStack = view.read("OutputStack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        Multiblockable.read(this, view);
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.CENTRIFUGAL_CONCENTRATOR;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        if (this.level == null)
            return List.of();

        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = 0; y < 3; y++) {
                    if (x == 0 && y == 0 && z == 0)
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
        return new CentrifugalConcentratorScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage, this.propertyDelegate);
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

    public ContainerStorage getInventoryProvider(Direction side) {
        return this.wrappedContainerStorage.getStorage(side);
    }

    public SingleFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public SingleSlurryStorage getSlurryProvider(Direction side) {
        return this.wrappedSlurryStorage.getStorage(side);
    }

    public EnergyStorage getEnergyProvider(Direction side) {
        return this.wrappedEnergyStorage.getStorage(side);
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public int getRecipeRPM() {
        return this.recipeRPM;
    }
}