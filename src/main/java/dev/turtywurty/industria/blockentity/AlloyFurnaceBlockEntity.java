package dev.turtywurty.industria.blockentity;

import com.mojang.datafixers.util.Pair;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.behaviourtree.*;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.*;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.AlloyFurnaceRecipe;
import dev.turtywurty.industria.screenhandler.AlloyFurnaceScreenHandler;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
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
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class AlloyFurnaceBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("alloy_furnace");
    public static final int INPUT_SLOT_0 = 0, INPUT_SLOT_1 = 1, FUEL_SLOT = 2, OUTPUT_SLOT = 3;
    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final BehaviourTree<AlloyFurnaceBlockEntity> behaviourTree;

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

        this.behaviourTree = new BehaviourTree<>();
        setupBehaviourTree();
    }

    private void initializeBlackboard() {
        Blackboard blackboard = this.behaviourTree.getBlackboard();
        blackboard.set("bufferedStack", this.bufferedStack);
        blackboard.set("burnTime", this.burnTime);
        blackboard.set("maxBurnTime", this.maxBurnTime);
        blackboard.set("progress", this.progress);
        blackboard.set("maxProgress", this.maxProgress);
        blackboard.set("currentRecipeId", this.currentRecipeId);
    }

    private void setupBehaviourTree() {
        SelectorNode<AlloyFurnaceBlockEntity> root = new SelectorNode<>();

        SequenceNode<AlloyFurnaceBlockEntity> handleBufferedOutput = new SequenceNode<>();
        handleBufferedOutput.addChild(new CheckStackNotEmptyCondition("bufferedStack"));
        handleBufferedOutput.addChild(new CanInsertItemStackCondition("bufferedStack", this.wrappedInventoryStorage.getInventory(OUTPUT_SLOT)));
        handleBufferedOutput.addChild(new InsertItemStackAction("bufferedStack", this.wrappedInventoryStorage.getInventory(OUTPUT_SLOT)));
        root.addChild(handleBufferedOutput);

        SequenceNode<AlloyFurnaceBlockEntity> smeltRecipe = new SequenceNode<>();
        smeltRecipe.addChild(new HandleFuelBurningAction("burnTime", "maxBurnTime", wrappedInventoryStorage.getInventory(FUEL_SLOT)));
        smeltRecipe.addChild(new HasValidRecipeCondition<>(
                this::getCurrentRecipe,
                "currentRecipeId",
                new ResetAction()
        ));
        smeltRecipe.addChild(new CanOutputRecipeCondition(wrappedInventoryStorage.getInventory(OUTPUT_SLOT)));
        smeltRecipe.addChild(new SmeltItemAction(wrappedInventoryStorage.getRecipeInventory(), wrappedInventoryStorage.getInventory(OUTPUT_SLOT)));
        root.addChild(smeltRecipe);

        root.addChild(new ResetAction());

        behaviorTree.setRoot(root);
    }

    private static class CheckStackNotEmptyCondition extends ConditionNode<AlloyFurnaceBlockEntity> {
        private final String itemStackKey;

        public CheckStackNotEmptyCondition(String itemStackKey) {
            this.itemStackKey = itemStackKey;
        }

        @Override
        public Status tick(AlloyFurnaceBlockEntity context) {
            ItemStack stack = blackboard.get(itemStackKey, ItemStack.class);
            if (stack == null)
                return Status.FAILURE;

            return !stack.isEmpty() ? Status.SUCCESS : Status.FAILURE;
        }
    }

    private static class CanInsertItemStackCondition extends ConditionNode<AlloyFurnaceBlockEntity> {
        private final String itemStackKey;
        private final SimpleInventory targetInventory;

        public CanInsertItemStackCondition(String itemStackKey, SimpleInventory targetInventory) {
            this.itemStackKey = itemStackKey;
            this.targetInventory = targetInventory;
        }

        @Override
        public Status tick(AlloyFurnaceBlockEntity context) {
            ItemStack stack = (ItemStack) blackboard.get(itemStackKey);
            return stack != null && targetInventory.canInsert(stack) ? Status.SUCCESS : Status.FAILURE;
        }
    }

    private static class InsertItemStackAction extends ActionNode<AlloyFurnaceBlockEntity> {
        private final String itemStackKey;
        private final SimpleInventory targetInventory;

        public InsertItemStackAction(String itemStackKey, SimpleInventory targetInventory) {
            this.itemStackKey = itemStackKey;
            this.targetInventory = targetInventory;
        }

        @Override
        public Status tick(AlloyFurnaceBlockEntity context) {
            ItemStack stack = (ItemStack) blackboard.get(itemStackKey);
            if (stack != null && !stack.isEmpty()) {
                targetInventory.addStack(stack);
                blackboard.set(itemStackKey, ItemStack.EMPTY);
                context.update();
                return Status.SUCCESS;
            }

            return Status.FAILURE;
        }
    }

    private static class HandleFuelBurningAction extends ActionNode<AlloyFurnaceBlockEntity> {
        private final String burnTimeKey, maxBurnTimeKey;
        private final SimpleInventory fuelInventory;

        public HandleFuelBurningAction(String burnTimeKey, String maxBurnTimeKey, SimpleInventory fuelInventory) {
            this.burnTimeKey = burnTimeKey;
            this.maxBurnTimeKey = maxBurnTimeKey;
            this.fuelInventory = fuelInventory;
        }

        @Override
        public Status tick(AlloyFurnaceBlockEntity context) {
            int burnTime = (int) blackboard.get(burnTimeKey);
            if (burnTime > 0) {
                burnTime--;
                blackboard.set(burnTimeKey, burnTime);
                context.world.setBlockState(context.pos, context.getCachedState().with(Properties.LIT, burnTime > 0));
                context.update();
                return Status.SUCCESS;
            }

            ItemStack fuel = fuelInventory.getStack(0);
            if (context.isFuel(fuel)) {
                int newBurnTime = context.getFuelTime(fuel);
                blackboard.set(this.maxBurnTimeKey, newBurnTime);
                blackboard.set(burnTimeKey, newBurnTime);
                fuelInventory.removeStack(0, 1);
                context.world.setBlockState(context.pos, context.getCachedState().with(Properties.LIT, true));
                context.update();
                return Status.SUCCESS;
            }
            return Status.FAILURE;
        }
    }

    private static class HasValidRecipeCondition<T extends Recipe<?>> extends ConditionNode<AlloyFurnaceBlockEntity> {
        private final Supplier<Optional<RecipeEntry<T>>> recipeFunction;
        private final String currentRecipeKey, maxProgressKey, progressKey;

        public HasValidRecipeCondition(Supplier<Optional<RecipeEntry<T>>> recipeFunction, String currentRecipeKey, String maxProgressKey, String progressKey) {
            this.recipeFunction = recipeFunction;
            this.currentRecipeKey = currentRecipeKey;
            this.maxProgressKey = maxProgressKey;
            this.progressKey = progressKey;
        }

        @Override
        public Status tick(AlloyFurnaceBlockEntity context) {
            Optional<RecipeEntry<T>> recipeEntry = recipeFunction.get();
            if (recipeEntry.isEmpty()) {
                return Status.FAILURE;
            }

            @SuppressWarnings("unchecked")
            RegistryKey<Recipe<?>> currentRecipeId = blackboard.get(currentRecipeKey, RegistryKey.class);
            if (currentRecipeId == null) {
                blackboard.set(currentRecipeKey, recipeEntry.get().id());
                blackboard.set(maxProgressKey, recipeEntry.get().value().smeltTime());
                blackboard.set(progressKey, 0);
                context.update();
                return Status.SUCCESS;
            }

            return recipeEntry.get().id().equals(currentRecipeId) ? Status.SUCCESS : Status.FAILURE;
        }
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
            if(this.burnTime <= 0) {
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

        if (!nbt.contains(Industria.MOD_ID))
            return;

        NbtCompound modidData = nbt.getCompoundOrEmpty(Industria.MOD_ID);
        if (modidData.contains("Progress"))
            this.progress = modidData.getInt("Progress", 0);

        if (modidData.contains("MaxProgress"))
            this.maxProgress = modidData.getInt("MaxProgress", 0);

        if (modidData.contains("BurnTime"))
            this.burnTime = modidData.getInt("BurnTime", 0);

        if (modidData.contains("MaxBurnTime"))
            this.maxBurnTime = modidData.getInt("MaxBurnTime", 0);

        if (modidData.contains("CurrentRecipe")) {
            NbtCompound currentRecipe = modidData.getCompoundOrEmpty("CurrentRecipe");
            this.currentRecipeId = currentRecipe.isEmpty() ? null :
                    RegistryKey.createCodec(RegistryKeys.RECIPE)
                            .decode(NbtOps.INSTANCE, currentRecipe)
                            .map(Pair::getFirst)
                            .result()
                            .orElse(null);
        }

        if (modidData.contains("Inventory"))
            this.wrappedInventoryStorage.readNbt(modidData.getListOrEmpty("Inventory"), registryLookup);

        if (modidData.contains("BufferedStack"))
            this.bufferedStack = ItemStack.fromNbt(registryLookup, modidData.getCompoundOrEmpty("BufferedStack"))
                    .orElse(ItemStack.EMPTY);
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
