package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.fluid.OutputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.menu.CokeOvenMenu;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.CokeOvenRecipe;
import dev.turtywurty.industria.recipe.input.SingleItemStackRecipeInput;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CokeOvenBlockEntity extends IndustriaMultiblockControllerBlockEntity
    implements BlockEntityContentsDropper, BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("coke_oven");

    private final WrappedContainerStorage<SimpleContainer> wrappedInventoryStorage = new WrappedContainerStorage<>();
    private final WrappedFluidStorage<SyncingFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();

    private float temperature = 20.0f;
    private float requiredTemperature = 20.0f;
    private int progress;
    private int maxProgress;
    private int burnTime;
    private int maxBurnTime;
    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> Mth.floor(CokeOvenBlockEntity.this.temperature);
                case 1 -> CokeOvenBlockEntity.this.progress;
                case 2 -> CokeOvenBlockEntity.this.maxProgress;
                case 3 -> CokeOvenBlockEntity.this.burnTime;
                case 4 -> CokeOvenBlockEntity.this.maxBurnTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> CokeOvenBlockEntity.this.temperature = value;
                case 1 -> CokeOvenBlockEntity.this.progress = value;
                case 2 -> CokeOvenBlockEntity.this.maxProgress = value;
                case 3 -> CokeOvenBlockEntity.this.burnTime = value;
                case 4 -> CokeOvenBlockEntity.this.maxBurnTime = value;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    private ItemStack bufferedStack = ItemStack.EMPTY;
    private FluidStack bufferedFluidStack = FluidStack.EMPTY;
    private ResourceKey<Recipe<?>> currentRecipeId;

    public CokeOvenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.COKE_OVEN.get(), ModBlockEntityTypes.COKE_OVEN.get(), pos, state);
        this.wrappedInventoryStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.UP);
        this.wrappedInventoryStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.SOUTH);
        this.wrappedInventoryStorage.addExtractOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.DOWN);
        this.wrappedFluidStorage.addStorage(new OutputFluidStorage(this, FluidAmounts.BUCKET * 5), Direction.NORTH);
    }

    public boolean isFuel(ItemStack stack) {
        return FuelUtils.isFuel(stack);
    }

    public int getFuelTime(ItemStack stack) {
        return FuelUtils.burnDuration(this.level, stack);
    }

    @Override
    public Block getBlock() {
        return ModBlocks.COKE_OVEN.get();
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingSimpleInventory inputInventory = (SyncingSimpleInventory) this.wrappedInventoryStorage
            .getInventory(Direction.UP);
        SyncingSimpleInventory fuelInventory = (SyncingSimpleInventory) this.wrappedInventoryStorage
            .getInventory(Direction.SOUTH);
        SyncingSimpleInventory outputInventory = (SyncingSimpleInventory) this.wrappedInventoryStorage
            .getInventory(Direction.DOWN);
        return List.of(inputInventory, fuelInventory, outputInventory, getOutputTank());
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        if (!this.bufferedStack.isEmpty() || !this.bufferedFluidStack.isEmpty()) {
            boolean didntOutputEither = true;
            if (!this.bufferedStack.isEmpty() && canOutput(this.bufferedStack)) {
                this.wrappedInventoryStorage.getInventory(Direction.DOWN).addItem(this.bufferedStack);
                this.bufferedStack = ItemStack.EMPTY;
                update();
                didntOutputEither = false;
            }

            if (!this.bufferedFluidStack.isEmpty() && canOutput(this.bufferedFluidStack)) {
                try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                    getOutputTank().insertInternal(
                        this.bufferedFluidStack.variant(), this.bufferedFluidStack.amount(), transaction);
                    transaction.commit();
                }

                this.bufferedFluidStack = FluidStack.EMPTY;
                update();
                didntOutputEither = false;
            }

            if (didntOutputEither) {
                this.temperature = Math.clamp(this.temperature - 0.5f, 20.0f, this.requiredTemperature);
                update();
                return;
            }
        }

        if (this.burnTime > 0) {
            this.burnTime--;
            if (this.temperature < this.requiredTemperature) {
                this.temperature = Math.clamp(this.temperature + 0.5f, 20.0f, this.requiredTemperature);
            }

            this.level.setBlockAndUpdate(this.worldPosition,
                getBlockState().setValue(BlockStateProperties.LIT, this.burnTime > 0));

            update();
        }

        if (this.currentRecipeId == null) {
            Optional<RecipeHolder<CokeOvenRecipe>> recipeEntry = getCurrentRecipe();
            if (recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().processTime();
                this.requiredTemperature = recipeEntry.get().value().requiredTemperature();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeHolder<CokeOvenRecipe>> currentRecipe = getCurrentRecipe();
        if (currentRecipe.isEmpty()
            || !currentRecipe.get().id().equals(this.currentRecipeId)
            || !canOutput(currentRecipe.get().value().outputTemplate().create())
            || !canOutput(currentRecipe.get().value().outputFluid())) {
            reset();
            return;
        }

        if (this.burnTime <= 0) {
            ItemStack fuel = this.wrappedInventoryStorage.getInventory(Direction.SOUTH).getItem(0);
            if (!fuel.isEmpty() && isFuel(fuel)) {
                int burnTime = getFuelTime(fuel);
                this.maxBurnTime = burnTime;
                this.burnTime = burnTime;
                this.wrappedInventoryStorage.getInventory(Direction.SOUTH).removeItem(0, 1);
                update();
            } else {
                reset();
                return;
            }
        }

        if (this.temperature >= this.requiredTemperature) {
            this.progress++;
            if (this.progress >= this.maxProgress) {
                this.bufferedStack = currentRecipe.get().value().outputTemplate().create();
                this.bufferedFluidStack = currentRecipe.get().value().outputFluid();
                this.wrappedInventoryStorage.getInventory(Direction.UP).removeItem(0, 1);
                this.bufferedStack = this.wrappedInventoryStorage.getInventory(Direction.DOWN)
                    .addItem(this.bufferedStack);
                try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                    long inserted = getOutputTank()
                        .insertInternal(this.bufferedFluidStack.variant(), this.bufferedFluidStack.amount(),
                            transaction);
                    this.bufferedFluidStack = inserted == this.bufferedFluidStack.amount()
                        ? FluidStack.EMPTY
                        : new FluidStack(this.bufferedFluidStack.variant(),
                            this.bufferedFluidStack.amount() - inserted);
                    transaction.commit();
                }

                reset();
            }

            update();
        }
    }

    private void reset() {
        this.currentRecipeId = null;
        this.progress = 0;
        this.maxProgress = 0;
        this.requiredTemperature = 20.0f;
        update();
    }

    public boolean canOutput(ItemStack output) {
        return this.wrappedInventoryStorage.getInventory(Direction.DOWN).canAddItem(output);
    }

    public boolean canOutput(FluidStack output) {
        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            long inserted = getOutputTank()
                .insertInternal(output.variant(), output.amount(), transaction);
            return inserted == output.amount();
        }
    }

    private Optional<RecipeHolder<CokeOvenRecipe>> getCurrentRecipe() {
        if (this.level == null || !(this.level instanceof ServerLevel serverLevel))
            return Optional.empty();

        ItemStack itemStack = this.wrappedInventoryStorage.getInventory(Direction.UP).getItem(0);
        return serverLevel.recipeAccess().getRecipeFor(
            CokeOvenRecipe.Type.INSTANCE,
            SingleItemStackRecipeInput.of(itemStack),
            serverLevel);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        ViewUtils.putChild(output, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.putChild(output, "FluidInventory", this.wrappedFluidStorage);
        output.putFloat("Temperature", this.temperature);
        output.putFloat("RequiredTemperature", this.requiredTemperature);
        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
        output.putInt("BurnTime", this.burnTime);
        output.putInt("MaxBurnTime", this.maxBurnTime);

        if (this.currentRecipeId != null) {
            output.store("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        output.store("BufferedStack", ItemStack.OPTIONAL_CODEC, this.bufferedStack);
        if (!this.bufferedFluidStack.isEmpty()) {
            output.store("BufferedFluidStack", FluidStack.CODEC.codec(), this.bufferedFluidStack);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ViewUtils.readChild(input, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.readChild(input, "FluidInventory", this.wrappedFluidStorage);
        this.temperature = Math.max(20.0f, input.getFloatOr("Temperature", 20.0f));
        this.requiredTemperature = Math.max(20.0f, input.getFloatOr("RequiredTemperature", 20.0f));
        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", 0);
        this.burnTime = input.getIntOr("BurnTime", 0);
        this.maxBurnTime = input.getIntOr("MaxBurnTime", 0);

        this.currentRecipeId = input.read("CurrentRecipe", RECIPE_CODEC).orElse(null);
        this.bufferedStack = input.read("BufferedStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.bufferedFluidStack = input.read("BufferedFluidStack", FluidStack.CODEC.codec()).orElse(FluidStack.EMPTY);
    }

    @Override
    public void dropContents(Level world, BlockPos pos) {
        BlockEntityContentsDropper.super.dropContents(world, pos);
        if (world != null && !this.bufferedStack.isEmpty()) {
            Block.popResource(world, pos, this.bufferedStack);
            this.bufferedStack = ItemStack.EMPTY;
        }
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedInventoryStorage;
    }

    @Override
    public BlockPosPayload getMenuOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    public SyncingFluidStorage getOutputTank() {
        return this.wrappedFluidStorage.getStorage(Direction.NORTH);
    }

    public @Nullable SyncingFluidStorage getFluidProvider(@Nullable Direction direction) {
        return isFormed() ? null : this.wrappedFluidStorage.getStorage(direction);
    }

    public @Nullable ResourceStorage<ResourceVariant<Item>> getInventoryProvider(@Nullable Direction direction) {
        return isFormed() ? null : this.wrappedInventoryStorage.getStorage(direction);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
        return new CokeOvenMenu(syncId, inventory, this, this.wrappedInventoryStorage, this.containerData);
    }

    @Override
    protected void definePorts(PortRegistrar ports) {
        ports.input(TransferType.ITEM, () -> this.wrappedInventoryStorage.getStorage(Direction.UP))
            .atSides(new BlockPos(1, 0, -1), Direction.EAST);
        ports.input(TransferType.ITEM, () -> this.wrappedInventoryStorage.getStorage(Direction.SOUTH))
            .atSides(new BlockPos(-1, 0, -1), Direction.WEST);
        ports.output(TransferType.ITEM, () -> this.wrappedInventoryStorage.getStorage(Direction.DOWN))
            .atSides(new BlockPos(-1, 0, -3), Direction.NORTH);
        ports.output(TransferType.FLUID, this::getOutputTank)
            .atSides(new BlockPos(1, 0, -3), Direction.NORTH);
    }
}
