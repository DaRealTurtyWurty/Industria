package dev.turtywurty.industria.blockentity;

import com.mojang.datafixers.util.Pair;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.*;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.AlloyFurnaceRecipe;
import dev.turtywurty.industria.screenhandler.AlloyFurnaceScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AlloyFurnaceBlockEntity extends UpdatableBlockEntity implements TickableBlockEntity, ExtendedScreenHandlerFactory<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("alloy_furnace");
    public static final int INPUT_SLOT_0 = 0, INPUT_SLOT_1 = 1, FUEL_SLOT = 2, OUTPUT_SLOT = 3;
    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();

    private int progress, maxProgress, burnTime, maxBurnTime;
    private ItemStack bufferedStack = ItemStack.EMPTY;

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

    private RegistryKey<Recipe<?>> currentRecipeId;

    public AlloyFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.ALLOY_FURNACE, pos, state);

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
        return new AlloyFurnaceScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    public void tick() {
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
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        var modidData = new NbtCompound();
        modidData.putInt("Progress", this.progress);
        modidData.putInt("MaxProgress", this.maxProgress);
        modidData.putInt("BurnTime", this.burnTime);
        modidData.putInt("MaxBurnTime", this.maxBurnTime);

        if (this.currentRecipeId != null) {
            Optional<NbtElement> result = RegistryKey.createCodec(RegistryKeys.RECIPE)
                    .encodeStart(NbtOps.INSTANCE, this.currentRecipeId)
                    .result();
            result.ifPresent(nbtElement -> modidData.put("CurrentRecipe", nbtElement));
        }

        modidData.put("Inventory", this.wrappedInventoryStorage.writeNbt(registryLookup));

        if (!this.bufferedStack.isEmpty())
            modidData.put("BufferedStack", this.bufferedStack.toNbt(registryLookup));

        nbt.put(Industria.MOD_ID, modidData);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (!nbt.contains(Industria.MOD_ID, NbtElement.COMPOUND_TYPE))
            return;

        NbtCompound modidData = nbt.getCompound(Industria.MOD_ID);
        if (modidData.contains("Progress", NbtElement.INT_TYPE))
            this.progress = modidData.getInt("Progress");

        if (modidData.contains("MaxProgress", NbtElement.INT_TYPE))
            this.maxProgress = modidData.getInt("MaxProgress");

        if (modidData.contains("BurnTime", NbtElement.INT_TYPE))
            this.burnTime = modidData.getInt("BurnTime");

        if (modidData.contains("MaxBurnTime", NbtElement.INT_TYPE))
            this.maxBurnTime = modidData.getInt("MaxBurnTime");

        if (modidData.contains("CurrentRecipe", NbtElement.COMPOUND_TYPE)) {
            NbtCompound currentRecipe = modidData.getCompound("CurrentRecipe");
            this.currentRecipeId = currentRecipe.isEmpty() ? null :
                    RegistryKey.createCodec(RegistryKeys.RECIPE)
                            .decode(NbtOps.INSTANCE, currentRecipe)
                            .map(Pair::getFirst)
                            .result()
                            .orElse(null);
        }

        if (modidData.contains("Inventory", NbtElement.LIST_TYPE))
            this.wrappedInventoryStorage.readNbt(modidData.getList("Inventory", NbtElement.COMPOUND_TYPE), registryLookup);

        if (modidData.contains("BufferedStack", NbtElement.COMPOUND_TYPE))
            this.bufferedStack = ItemStack.fromNbtOrEmpty(registryLookup, modidData.getCompound("BufferedStack"));
    }

    public WrappedInventoryStorage<SimpleInventory> getWrappedStorage() {
        return wrappedInventoryStorage;
    }
}
