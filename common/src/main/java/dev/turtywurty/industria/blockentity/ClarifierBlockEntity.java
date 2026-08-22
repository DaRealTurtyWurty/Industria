package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.fluid.*;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.menu.ClarifierScreenHandler;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.ClarifierRecipe;
import dev.turtywurty.industria.recipe.input.ClarifierRecipeInput;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.multiblocklib.port.PortRegistrar;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static dev.turtywurty.industria.blockentity.util.StorageOperations.extract;
import static dev.turtywurty.industria.blockentity.util.StorageOperations.set;

public class ClarifierBlockEntity extends IndustriaMultiblockControllerBlockEntity implements BlockEntityContentsDropper, BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("clarifier");

    private final WrappedFluidStorage<SyncingFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();


    private ResourceKey<Recipe<?>> currentRecipeId;
    private int progress;
    private int maxProgress;
    private final ContainerData properties = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> throw new IllegalArgumentException("Unknown property index: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                default -> throw new IllegalArgumentException("Unknown property index: " + index);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };
    private ItemStack outputItemStack = ItemStack.EMPTY;
    private FluidStack outputFluidStack = FluidStack.EMPTY;
    private ItemStack nextOutputItemStack = ItemStack.EMPTY; // Used for rendering

    public ClarifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CLARIFIER.get(), ModBlockEntityTypes.CLARIFIER.get(), pos, state);

        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidAmounts.BUCKET * 5), Direction.UP);

        this.wrappedFluidStorage.addStorage(new OutputFluidStorage(this, FluidAmounts.BUCKET * 5), Direction.NORTH);
        this.wrappedContainerStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.SOUTH);
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingFluidStorage inputFluidTank = getInputFluidTank();
        SyncingFluidStorage outputFluidTank = getOutputFluidTank();
        SyncingSimpleInventory outputInventory = getOutputInventory();

        return List.of(inputFluidTank, outputFluidTank, outputInventory);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        if (!this.outputItemStack.isEmpty()) {
            SyncingSimpleInventory outputInventory = getOutputInventory();
            if (outputInventory.canAddItem(this.outputItemStack)) {
                this.outputItemStack = outputInventory.addItem(this.outputItemStack);
                update();
            }

            return;
        }

        if (!this.outputFluidStack.isEmpty()) {
            SyncingFluidStorage outputFluidTank = getOutputFluidTank();
            if (outputFluidTank.canInsert(this.outputFluidStack)) {
                long inserted = Math.min(outputFluidTank.getCapacity() - outputFluidTank.getAmount(), this.outputFluidStack.amount());
                set(outputFluidTank, outputFluidStack.variant(), outputFluidTank.getAmount() + inserted);
                this.outputFluidStack = this.outputFluidStack.withAmount(this.outputFluidStack.amount() - inserted);
                update();
            }

            return;
        }

        SyncingFluidStorage inputFluidStorage = getInputFluidTank();
        var inputFluidStack = new FluidStack(inputFluidStorage.getResource(), inputFluidStorage.getAmount());
        var recipeInput = new ClarifierRecipeInput(inputFluidStack);
        if (this.currentRecipeId == null) {
            Optional<RecipeHolder<ClarifierRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
            if (recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().processTime();
                this.progress = 0;

                update();
            }

            if (!this.nextOutputItemStack.isEmpty()) {
                this.nextOutputItemStack = ItemStack.EMPTY;
                update();
            }

            return;
        }

        Optional<RecipeHolder<ClarifierRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            this.currentRecipeId = null;
            this.maxProgress = 0;
            this.progress = 0;
            this.nextOutputItemStack = ItemStack.EMPTY;

            update();
            return;
        }

        ClarifierRecipe recipe = recipeEntry.get().value();
        if (this.progress >= this.maxProgress) {
            this.outputItemStack = recipe.assemble(recipeInput);
            this.outputFluidStack = recipe.outputFluidStack();
            extract(inputFluidStorage, inputFluidStorage.getResource(), recipe.inputFluid().amount());

            this.currentRecipeId = null;
            this.maxProgress = 0;
            this.progress = 0;
            this.nextOutputItemStack = ItemStack.EMPTY;
            update();
        } else {
            this.progress++;
            this.nextOutputItemStack = recipe.assemble(recipeInput);
            update();
        }
    }

    private Optional<RecipeHolder<ClarifierRecipe>> getCurrentRecipe(ClarifierRecipeInput recipeInput) {
        if (this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return Optional.empty();

        return serverWorld.recipeAccess().getRecipeFor(ModRecipeTypes.CLARIFIER.get(), recipeInput, this.level);
    }

    public SyncingSimpleInventory getOutputInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(0);
    }

    public SyncingFluidStorage getInputFluidTank() {
        return (SyncingFluidStorage) this.wrappedFluidStorage.getStorage(Direction.UP);
    }

    public SyncingFluidStorage getOutputFluidTank() {
        return (SyncingFluidStorage) this.wrappedFluidStorage.getStorage(Direction.NORTH);
    }

    public ResourceStorage<ResourceVariant<Item>> getInventoryProvider(Direction side) {
        return this.wrappedContainerStorage.getStorage(side);
    }

    public SyncingFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public ItemStack getNextOutputItemStack() {
        return this.nextOutputItemStack;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(view, "FluidTank", this.wrappedFluidStorage);
        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            view.store("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        if (!this.outputItemStack.isEmpty()) {
            view.store("OutputStack", ItemStack.CODEC, this.outputItemStack);
        }

        if (!this.outputFluidStack.isEmpty()) {
            view.store("OutputFluid", FluidStack.CODEC.codec(), this.outputFluidStack);
        }

        view.store("NextOutputStack", ItemStack.OPTIONAL_CODEC, this.nextOutputItemStack);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);

        this.progress = view.getIntOr("Progress", 0);

        this.maxProgress = view.getIntOr("MaxProgress", 0);

        this.currentRecipeId = view.read("CurrentRecipe", ResourceKey.codec(Registries.RECIPE))
                .orElse(null);

        this.outputItemStack = view.read("OutputStack", ItemStack.CODEC)
                .orElse(ItemStack.EMPTY);

        this.outputFluidStack = view.read("OutputFluid", FluidStack.CODEC.codec())
                .orElse(FluidStack.EMPTY);

        this.nextOutputItemStack = view.read("NextOutputStack", ItemStack.OPTIONAL_CODEC)
                .orElse(ItemStack.EMPTY);
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
        ports.output(TransferType.ITEM, () -> this.wrappedContainerStorage.getStorage(Direction.SOUTH))
                .at(new BlockPos(0, 0, 1));
        ports.input(TransferType.FLUID, this::getInputFluidTank)
                .at(new BlockPos(0, 1, 0));
        ports.output(TransferType.FLUID, this::getOutputFluidTank)
                .at(new BlockPos(0, 0, -1));
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new ClarifierScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage, this.properties);
    }
}
