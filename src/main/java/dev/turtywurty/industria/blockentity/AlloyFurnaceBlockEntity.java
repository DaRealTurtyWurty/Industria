package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.*;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.AlloyFurnaceRecipe;
import dev.turtywurty.industria.screenhandler.AlloyFurnaceScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class AlloyFurnaceBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("alloy_furnace");
    public static final int INPUT_SLOT_0 = 0, INPUT_SLOT_1 = 1, FUEL_SLOT = 2, OUTPUT_SLOT = 3;
    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();

    private int progress, maxProgress, burnTime, maxBurnTime;
    private final ContainerData propertyDelegate = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> burnTime;
                case 3 -> maxBurnTime;
                default -> throw new IllegalArgumentException("Invalid index: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> burnTime = value;
                case 3 -> maxBurnTime = value;
                default -> throw new IllegalArgumentException("Invalid index: " + index);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };
    private ItemStack bufferedStack = ItemStack.EMPTY;
    private ResourceKey<Recipe<?>> currentRecipeId;

    public AlloyFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.ALLOY_FURNACE, BlockEntityTypeInit.ALLOY_FURNACE, pos, state);

        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 1), Direction.EAST);
        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 1), Direction.WEST);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1, (itemStack, slot) -> isFuel(itemStack)), Direction.UP);
        this.wrappedContainerStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.DOWN);
    }

    public boolean isFuel(ItemStack stack) {
        return this.level.fuelValues().isFuel(stack);
    }

    public int getFuelTime(ItemStack stack) {
        return this.level.fuelValues().burnDuration(stack);
    }

    public ContainerStorage getInventoryProvider(Direction direction) {
        return this.wrappedContainerStorage.getStorage(direction);
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new AlloyFurnaceScreenHandler(syncId, playerInventory, this, getWrappedContainerStorage(), this.propertyDelegate);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var input0 = (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(0);
        var input1 = (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(1);
        var fuel = (PredicateSimpleInventory) this.wrappedContainerStorage.getInventory(2);
        var output = (OutputSimpleInventory) this.wrappedContainerStorage.getInventory(3);
        return List.of(input0, input1, fuel, output);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        if (!this.bufferedStack.isEmpty()) {
            if (canOutput(this.bufferedStack)) {
                this.wrappedContainerStorage.getInventory(OUTPUT_SLOT).addItem(this.bufferedStack);
                this.bufferedStack = ItemStack.EMPTY;
                update();
            } else {
                return;
            }
        }

        if (this.burnTime > 0) {
            this.burnTime--;
            if (this.burnTime <= 0) {
                this.level.setBlockAndUpdate(this.worldPosition, getBlockState().setValue(BlockStateProperties.LIT, false));
            } else {
                this.level.setBlockAndUpdate(this.worldPosition, getBlockState().setValue(BlockStateProperties.LIT, true));
            }

            update();
        }

        if (this.currentRecipeId == null) {
            Optional<RecipeHolder<AlloyFurnaceRecipe>> recipeEntry = getCurrentRecipe();
            if (recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().smeltTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeHolder<AlloyFurnaceRecipe>> currentRecipe = getCurrentRecipe();
        if (currentRecipe.isEmpty() || !currentRecipe.get().id().equals(this.currentRecipeId) || !canOutput(currentRecipe.get().value().output())) {
            reset();
            return;
        }

        if (this.burnTime <= 0) {
            ItemStack fuel = this.wrappedContainerStorage.getInventory(FUEL_SLOT).getItem(0);
            if (isFuel(fuel)) {
                int burnTime = getFuelTime(fuel);
                this.maxBurnTime = burnTime;
                this.burnTime = burnTime;
                this.wrappedContainerStorage.getInventory(FUEL_SLOT).removeItem(0, 1);
                update();
            } else {
                reset();
                return;
            }
        }

        this.progress++;

        AlloyFurnaceRecipe recipe = currentRecipe.get().value();
        if (this.progress >= this.maxProgress) {
            reset();

            ItemStack output = recipe.assemble(this.wrappedContainerStorage.getRecipeInventory(), this.level.registryAccess());
            if (canOutput(output)) {
                SimpleContainer outputInventory = this.wrappedContainerStorage.getInventory(OUTPUT_SLOT);
                ItemStack outputStack = outputInventory.getItem(0);
                if (outputStack.isEmpty()) {
                    outputInventory.setItem(0, output);
                } else {
                    outputStack.grow(output.getCount());
                }
            } else {
                this.bufferedStack = output;
            }

            update();
        }
    }

    public boolean canOutput(ItemStack output) {
        return this.wrappedContainerStorage.getInventory(OUTPUT_SLOT).canAddItem(output);
    }

    private void reset() {
        this.currentRecipeId = null;
        this.progress = 0;
        this.maxProgress = 0;
        update();
    }

    public RecipeSimpleInventory getInventory() {
        return this.wrappedContainerStorage.getRecipeInventory();
    }

    private Optional<RecipeHolder<AlloyFurnaceRecipe>> getCurrentRecipe() {
        if (this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return Optional.empty();

        return serverWorld.recipeAccess().getRecipeFor(RecipeTypeInit.ALLOY_FURNACE, getInventory(), this.level);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);
        view.putInt("BurnTime", this.burnTime);
        view.putInt("MaxBurnTime", this.maxBurnTime);

        if (this.currentRecipeId != null) {
            view.store("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);

        if (!this.bufferedStack.isEmpty())
            view.store("BufferedStack", ItemStack.CODEC, this.bufferedStack);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        this.progress = view.getIntOr("Progress", 0);
        this.maxProgress = view.getIntOr("MaxProgress", 0);
        this.burnTime = view.getIntOr("BurnTime", 0);
        this.maxBurnTime = view.getIntOr("MaxBurnTime", 0);
        this.currentRecipeId = view.read("CurrentRecipe", RECIPE_CODEC).orElse(null);
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        this.bufferedStack = view.read("BufferedStack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    public WrappedContainerStorage<SimpleContainer> getWrappedContainerStorage() {
        return wrappedContainerStorage;
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }
}