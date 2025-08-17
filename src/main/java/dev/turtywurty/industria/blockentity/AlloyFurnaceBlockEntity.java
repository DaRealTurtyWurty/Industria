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
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class AlloyFurnaceBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("alloy_furnace");
    public static final int INPUT_SLOT_0 = 0, INPUT_SLOT_1 = 1, FUEL_SLOT = 2, OUTPUT_SLOT = 3;
    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();

    private int progress, maxProgress, burnTime, maxBurnTime;
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
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
        public int size() {
            return 4;
        }
    };
    private ItemStack bufferedStack = ItemStack.EMPTY;
    private RegistryKey<Recipe<?>> currentRecipeId;

    public AlloyFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.ALLOY_FURNACE, BlockEntityTypeInit.ALLOY_FURNACE, pos, state);

        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 1), Direction.EAST);
        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 1), Direction.WEST);
        this.wrappedInventoryStorage.addInventory(new PredicateSimpleInventory(this, 1, (itemStack, slot) -> isFuel(itemStack)), Direction.UP);
        this.wrappedInventoryStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.DOWN);
    }

    public boolean isFuel(ItemStack stack) {
        return this.world.getFuelRegistry().isFuel(stack);
    }

    public int getFuelTime(ItemStack stack) {
        return this.world.getFuelRegistry().getFuelTicks(stack);
    }

    public InventoryStorage getInventoryProvider(Direction direction) {
        return this.wrappedInventoryStorage.getStorage(direction);
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new AlloyFurnaceScreenHandler(syncId, playerInventory, this, getWrappedInventoryStorage(), this.propertyDelegate);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var input0 = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(0);
        var input1 = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(1);
        var fuel = (PredicateSimpleInventory) this.wrappedInventoryStorage.getInventory(2);
        var output = (OutputSimpleInventory) this.wrappedInventoryStorage.getInventory(3);
        return List.of(input0, input1, fuel, output);
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        if (!this.bufferedStack.isEmpty()) {
            if (canOutput(this.bufferedStack)) {
                this.wrappedInventoryStorage.getInventory(OUTPUT_SLOT).addStack(this.bufferedStack);
                this.bufferedStack = ItemStack.EMPTY;
                update();
            } else {
                return;
            }
        }

        if (this.burnTime > 0) {
            this.burnTime--;
            if (this.burnTime <= 0) {
                this.world.setBlockState(this.pos, getCachedState().with(Properties.LIT, false));
            } else {
                this.world.setBlockState(this.pos, getCachedState().with(Properties.LIT, true));
            }

            update();
        }

        if (this.currentRecipeId == null) {
            Optional<RecipeEntry<AlloyFurnaceRecipe>> recipeEntry = getCurrentRecipe();
            if (recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().smeltTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeEntry<AlloyFurnaceRecipe>> currentRecipe = getCurrentRecipe();
        if (currentRecipe.isEmpty() || !currentRecipe.get().id().equals(this.currentRecipeId) || !canOutput(currentRecipe.get().value().output())) {
            reset();
            return;
        }

        if (this.burnTime <= 0) {
            ItemStack fuel = this.wrappedInventoryStorage.getInventory(FUEL_SLOT).getStack(0);
            if (isFuel(fuel)) {
                int burnTime = getFuelTime(fuel);
                this.maxBurnTime = burnTime;
                this.burnTime = burnTime;
                this.wrappedInventoryStorage.getInventory(FUEL_SLOT).removeStack(0, 1);
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

            ItemStack output = recipe.craft(this.wrappedInventoryStorage.getRecipeInventory(), this.world.getRegistryManager());
            if (canOutput(output)) {
                SimpleInventory outputInventory = this.wrappedInventoryStorage.getInventory(OUTPUT_SLOT);
                ItemStack outputStack = outputInventory.getStack(0);
                if (outputStack.isEmpty()) {
                    outputInventory.setStack(0, output);
                } else {
                    outputStack.increment(output.getCount());
                }
            } else {
                this.bufferedStack = output;
            }

            update();
        }
    }

    public boolean canOutput(ItemStack output) {
        return this.wrappedInventoryStorage.getInventory(OUTPUT_SLOT).canInsert(output);
    }

    private void reset() {
        this.currentRecipeId = null;
        this.progress = 0;
        this.maxProgress = 0;
        update();
    }

    public RecipeSimpleInventory getInventory() {
        return this.wrappedInventoryStorage.getRecipeInventory();
    }

    private Optional<RecipeEntry<AlloyFurnaceRecipe>> getCurrentRecipe() {
        if (this.world == null || !(this.world instanceof ServerWorld serverWorld))
            return Optional.empty();

        return serverWorld.getRecipeManager().getFirstMatch(RecipeTypeInit.ALLOY_FURNACE, getInventory(), this.world);
    }

    @Override
    protected void writeData(WriteView view) {

        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);
        view.putInt("BurnTime", this.burnTime);
        view.putInt("MaxBurnTime", this.maxBurnTime);

        if (this.currentRecipeId != null) {
            view.put("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        ViewUtils.putChild(view, "Inventory", this.wrappedInventoryStorage);

        if (!this.bufferedStack.isEmpty())
            view.put("BufferedStack", ItemStack.CODEC, this.bufferedStack);
    }

    @Override
    protected void readData(ReadView view) {
        this.progress = view.getInt("Progress", 0);
        this.maxProgress = view.getInt("MaxProgress", 0);
        this.burnTime = view.getInt("BurnTime", 0);
        this.maxBurnTime = view.getInt("MaxBurnTime", 0);
        this.currentRecipeId = view.read("CurrentRecipe", RECIPE_CODEC).orElse(null);
        ViewUtils.readChild(view, "Inventory", this.wrappedInventoryStorage);
        this.bufferedStack = view.read("BufferedStack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    public WrappedInventoryStorage<SimpleInventory> getWrappedInventoryStorage() {
        return wrappedInventoryStorage;
    }

    @Override
    public Block getBlock() {
        return getCachedState().getBlock();
    }
}