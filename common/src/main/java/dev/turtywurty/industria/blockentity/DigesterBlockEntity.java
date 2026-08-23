package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.fluid.OutputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.blockentity.util.slurry.InputSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.blockentity.util.slurry.SyncingSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.WrappedSlurryStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.menu.DigesterScreenHandler;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.DigesterRecipe;
import dev.turtywurty.industria.recipe.input.DigesterRecipeInput;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static dev.turtywurty.industria.blockentity.util.StorageOperations.extract;
import static dev.turtywurty.industria.blockentity.util.StorageOperations.insert;

// TODO: Make this work with temperature and pressure
public class DigesterBlockEntity extends IndustriaMultiblockControllerBlockEntity implements BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("digester");

    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedSlurryStorage<SingleSlurryStorage> wrappedSlurryStorage = new WrappedSlurryStorage<>();
    private final WrappedFluidStorage<SyncingFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();

    private ResourceKey<Recipe<?>> currentRecipeId;
    private int progress;
    private int maxProgress;

    private final ContainerData properties = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> throw new IllegalArgumentException("Invalid index: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                default -> throw new IllegalArgumentException("Invalid index: " + index);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public DigesterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.DIGESTER.get(), ModBlockEntityTypes.DIGESTER.get(), pos, state);

        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createSlurryPredicate(() -> {
                    SyncingSlurryStorage inputSlurryTank = getInputSlurryStorage();
                    return new SlurryStack(inputSlurryTank.getResource(), inputSlurryTank.getAmount());
                })), Direction.UP);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createEmptyFluidPredicate(() -> getOutputFluidStorage().getResource())));

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 100_000, 5_000, 0));

        this.wrappedSlurryStorage.addStorage(new InputSlurryStorage(this, FluidAmounts.BUCKET * 5), Direction.UP);
        this.wrappedFluidStorage.addStorage(new OutputFluidStorage(this, FluidAmounts.BUCKET * 5), Direction.SOUTH);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        PredicateSimpleInventory inputSlurryInventory = getInputSlurryInventory();
        PredicateSimpleInventory outputFluidInventory = getOutputFluidInventory();
        SyncingEnergyStorage energyStorage = getEnergyStorage();
        InputSlurryStorage inputSlurryStorage = getInputSlurryStorage();
        OutputFluidStorage outputFluidStorage = getOutputFluidStorage();

        return List.of(inputSlurryInventory, outputFluidInventory, energyStorage, inputSlurryStorage, outputFluidStorage);
    }

    public PredicateSimpleInventory getInputSlurryInventory() {
        return (PredicateSimpleInventory) this.wrappedContainerStorage.getInventory(0);
    }

    public PredicateSimpleInventory getOutputFluidInventory() {
        return (PredicateSimpleInventory) this.wrappedContainerStorage.getInventory(1);
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
    }

    public InputSlurryStorage getInputSlurryStorage() {
        return (InputSlurryStorage) this.wrappedSlurryStorage.getStorage(Direction.UP);
    }

    public OutputFluidStorage getOutputFluidStorage() {
        return (OutputFluidStorage) this.wrappedFluidStorage.getStorage(Direction.SOUTH);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        SyncingSimpleInventory bucketInputInventory = getInputSlurryInventory();
        if (!bucketInputInventory.isEmpty()) {
            ItemStack bucket = bucketInputInventory.getItem(0);
            ResourceStorage<ResourceVariant<Slurry>> storage =
                    MutableItemContext.ofContainerSlot(bucketInputInventory, 0).find(SlurryStorage.KEY);
            if (storage != null && storage.supportsExtraction()) {
                SyncingSlurryStorage inputSlurryTank = getInputSlurryStorage();
                Optional<ResourceVariant<Slurry>> optVariant = TransferUtils.findFirstVariant(storage, inputSlurryTank.getResource());
                optVariant.filter(variant -> !variant.isBlank()).ifPresent(variant -> {
                    try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                        long extracted = storage.extract(variant, FluidAmounts.BUCKET, transaction);
                        if (extracted > 0) {
                            inputSlurryTank.insertInternal(variant, extracted, transaction);
                        }

                        transaction.commit();
                    }
                });
            }
        }

        SyncingSimpleInventory bucketOutputInventory = getOutputFluidInventory();
        if (!bucketOutputInventory.isEmpty()) {
            ItemStack bucket = bucketOutputInventory.getItem(0);
            ResourceStorage<ResourceVariant<Fluid>> storage =
                    MutableItemContext.ofContainerSlot(bucketOutputInventory, 0).find(StorageKeys.FLUID);
            if (storage != null && storage.supportsInsertion()) {
                SyncingFluidStorage outputFluidTank = getOutputFluidStorage();
                if (outputFluidTank.getAmount() > 0) {
                    try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                        long inserted = storage.insert(outputFluidTank.getResource(), FluidAmounts.BUCKET, transaction);
                        if (inserted > 0) {
                            outputFluidTank.extractInternal(outputFluidTank.getResource(), inserted, transaction);
                        }

                        transaction.commit();
                    }
                }
            }
        }

        if (this.currentRecipeId == null) {
            Optional<RecipeHolder<DigesterRecipe>> recipeEntry = getCurrentRecipe();
            if (recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().processTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeHolder<DigesterRecipe>> recipeEntry = getCurrentRecipe();
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            this.currentRecipeId = null;
            this.maxProgress = 0;
            this.progress = 0;
            update();
            return;
        }

        DigesterRecipe recipe = recipeEntry.get().value();
        if (this.progress >= this.maxProgress) {
            OutputFluidStorage outputFluidStorage = getOutputFluidStorage();
            FluidStack outputFluidStack = recipe.outputFluid();
            if (outputFluidStorage.canInsert(outputFluidStack) && hasEnergy()) {
                consumeEnergy();

                InputSlurryStorage inputSlurryStorage = getInputSlurryStorage();
                extract(inputSlurryStorage, inputSlurryStorage.getResource(), recipe.inputSlurry().amount());

                insert(outputFluidStorage, outputFluidStack.variant(), outputFluidStack.amount());

                this.progress = 0;
                this.maxProgress = 0;
                this.currentRecipeId = null;

                update();
            }
        } else {
            if (hasEnergy()) {
                this.progress++;
                consumeEnergy();
                update();
            }
        }
    }

    private Optional<RecipeHolder<DigesterRecipe>> getCurrentRecipe() {
        if (this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return Optional.empty();

        InputSlurryStorage slurryStorage = getInputSlurryStorage();
        return serverWorld.recipeAccess().getRecipeFor(ModRecipeTypes.DIGESTER.get(), new DigesterRecipeInput(new SlurryStack(slurryStorage.getResource(), slurryStorage.getAmount())), this.level);
    }

    private boolean hasEnergy() {
        return getEnergyStorage().getAmount() >= 100;
    }

    private void consumeEnergy() {
        extract(getEnergyStorage(), 100);
        update();
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
        return new DigesterScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage, this.properties);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
        ViewUtils.putChild(view, "SlurryTank", this.wrappedSlurryStorage);
        ViewUtils.putChild(view, "FluidTank", this.wrappedFluidStorage);
        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            view.store("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);
        ViewUtils.readChild(view, "SlurryTank", this.wrappedSlurryStorage);
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);

        this.progress = view.getIntOr("Progress", 0);

        this.maxProgress = view.getIntOr("MaxProgress", 0);

        this.currentRecipeId = view.read("CurrentRecipe", ResourceKey.codec(Registries.RECIPE))
                .orElse(null);
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }

    public @Nullable ResourceStorage<ResourceVariant<UnitResource>> getEnergyProvider(@Nullable Direction direction) {
        return this.wrappedEnergyStorage.getStorage(direction);
    }

    public @Nullable SingleSlurryStorage getSlurryProvider(@Nullable Direction direction) {
        return this.wrappedSlurryStorage.getStorage(direction);
    }

    public @Nullable SyncingFluidStorage getFluidProvider(@Nullable Direction direction) {
        return this.wrappedFluidStorage.getStorage(direction);
    }

    @Override
    protected void definePorts(PortRegistrar ports) {
        ports.input(TransferType.ENERGY, this::getEnergyStorage)
                .wherePosition(offset -> offset.getZ() == -1);
        ports.input(TransferType.SLURRY, this::getInputSlurryStorage)
                .at(new BlockPos(0, 3, 0));
        ports.output(TransferType.FLUID, this::getOutputFluidStorage)
                .wherePosition(offset -> offset.getY() == 0 && offset.getZ() == 1);
    }
}
