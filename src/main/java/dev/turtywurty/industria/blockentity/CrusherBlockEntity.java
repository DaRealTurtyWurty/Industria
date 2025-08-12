package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.inventory.RecipeSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.CrusherRecipe;
import dev.turtywurty.industria.screenhandler.CrusherScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CrusherBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("crusher");
    public static final int INPUT_SLOT = 0, OUTPUT_SLOT = 1;
    private static final Box PICKUP_AREA = new Box(0, 0, 0, 1, 0.7, 1);
    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();

    private final ItemStack[] buffer = new ItemStack[2];
    private int progress, maxProgress;
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
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
        public int size() {
            return 2;
        }
    };
    private RegistryKey<Recipe<?>> currentRecipeId;

    public CrusherBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.CRUSHER, BlockEntityTypeInit.CRUSHER, pos, state);

        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 1), Direction.UP);
        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 2), Direction.DOWN);

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 10_000, 1_000, 0));
    }

    public static long getEnergyCost() {
        return 10;
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var input = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(INPUT_SLOT);
        var output = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(OUTPUT_SLOT);
        SyncingEnergyStorage energy = getEnergy();

        return List.of(input, output, energy);
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        listenForItemEntities();

        for (int index = 0; index < this.buffer.length; index++) {
            ItemStack stack = this.buffer[index];
            if (stack == null) {
                this.buffer[index] = ItemStack.EMPTY;
                continue;
            }

            if (!stack.isEmpty()) {
                stack = this.wrappedInventoryStorage.getInventory(OUTPUT_SLOT).addStack(stack);
                this.buffer[index] = stack;
            }
        }

        if (hasItemsInBuffer()) {
            update();
            return;
        }

        if (this.currentRecipeId == null) {
            Optional<RecipeEntry<CrusherRecipe>> recipeEntry = getCurrentRecipe();
            if (recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().processTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeEntry<CrusherRecipe>> recipeEntry = getCurrentRecipe();
        Pair<ItemStack, ItemStack> outputs;
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            this.currentRecipeId = null;
            this.maxProgress = 0;
            this.progress = 0;
            update();
            return;
        } else {
            outputs = recipeEntry.get().value().assemble(getInventory(), this.world.random);
            if (!canOutput(outputs.getLeft()) || !canOutput(outputs.getRight())) {
                this.currentRecipeId = null;
                this.maxProgress = 0;
                this.progress = 0;
                update();
                return;
            }
        }

        CrusherRecipe recipe = recipeEntry.get().value();
        if (this.progress >= this.maxProgress) {
            if (hasEnergy()) {
                ItemStack outputA = outputs.getLeft();
                ItemStack outputB = outputs.getRight();
                if (!canOutput(outputA) || !canOutput(outputB))
                    return;

                consumeEnergy();
                this.wrappedInventoryStorage.getInventory(INPUT_SLOT).removeStack(0, recipe.input().stackData().count());

                if (!outputA.isEmpty())
                    this.wrappedInventoryStorage.getInventory(OUTPUT_SLOT).addStack(outputA);

                if (!outputB.isEmpty())
                    this.wrappedInventoryStorage.getInventory(OUTPUT_SLOT).addStack(outputB);

                reset();
            }
        } else {
            if (hasEnergy()) {
                this.progress++;
                consumeEnergy();
                update();
            }
        }
    }

    private void listenForItemEntities() {
        List<ItemEntity> entities = this.world.getEntitiesByClass(ItemEntity.class,
                PICKUP_AREA.offset(this.pos),
                entity -> {
                    ItemStack stack = entity.getStack().copy();
                    stack.setCount(1);
                    return canInput(stack);
                });
        if (entities.isEmpty())
            return;

        for (ItemEntity entity : entities) {
            ItemStack stack = entity.getStack().copy();
            stack = this.wrappedInventoryStorage.getInventory(INPUT_SLOT).addStack(stack);
            if (stack.isEmpty()) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            } else {
                entity.setStack(stack);
            }
        }
    }

    private boolean canOutput(ItemStack stack) {
        if (stack.isEmpty())
            return true;

        return this.wrappedInventoryStorage.getInventory(OUTPUT_SLOT).canInsert(stack);
    }

    private boolean canInput(ItemStack stack) {
        return this.wrappedInventoryStorage.getInventory(INPUT_SLOT).canInsert(stack);
    }

    private boolean hasEnergy() {
        return getEnergy().getAmount() >= getEnergyCost();
    }

    private void consumeEnergy() {
        getEnergy().amount -= getEnergyCost();
    }

    private void reset() {
        this.progress = 0;
        this.maxProgress = 0;
        this.currentRecipeId = null;
        update();
    }

    public RecipeSimpleInventory getInventory() {
        return this.wrappedInventoryStorage.getRecipeInventory();
    }

    @Override
    public WrappedInventoryStorage<SimpleInventory> getWrappedInventoryStorage() {
        return this.wrappedInventoryStorage;
    }

    @Override
    public Block getBlock() {
        return getCachedState().getBlock();
    }

    private Optional<RecipeEntry<CrusherRecipe>> getCurrentRecipe() {
        if (this.world == null || !(this.world instanceof ServerWorld serverWorld))
            return Optional.empty();

        return serverWorld.getRecipeManager().getFirstMatch(RecipeTypeInit.CRUSHER, getInventory(), this.world);
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
        return new CrusherScreenHandler(syncId, playerInventory, this, this.wrappedInventoryStorage, this.propertyDelegate);
    }

    private boolean hasItemsInBuffer() {
        return Arrays.stream(this.buffer).anyMatch(stack -> stack == null || !stack.isEmpty());
    }

    @Override
    protected void writeData(WriteView view) {
        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);
        if (this.currentRecipeId != null) {
            view.put("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        if (hasItemsInBuffer()) {
            var bufferArray = view.getListAppender("Buffer", ItemStack.CODEC);
            for (ItemStack stack : this.buffer) {
                if (stack == null || stack.isEmpty())
                    continue;

                bufferArray.add(stack);
            }
        }

        ViewUtils.putChild(view, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
    }

    @Override
    protected void readData(ReadView view) {
        this.progress = view.getInt("Progress", 0);
        this.maxProgress = view.getInt("MaxProgress", 0);

        this.currentRecipeId = view.read("CurrentRecipe", RegistryKey.createCodec(RegistryKeys.RECIPE))
                .orElse(null);

        for (int i = 0; i < view.getTypedListView("Buffer", ItemStack.CODEC).stream().toList().size(); i++) {

            this.buffer[i] = view.read(ItemStack.MAP_CODEC)
                    .orElse(ItemStack.EMPTY);
        }

        ViewUtils.readChild(view, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);
    }

    public InventoryStorage getInventoryProvider(Direction direction) {
        return this.wrappedInventoryStorage.getStorage(direction);
    }

    public EnergyStorage getEnergyProvider(Direction direction) {
        return this.wrappedEnergyStorage.getStorage(direction);
    }

    public SyncingEnergyStorage getEnergy() {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(Direction.SOUTH);
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }
}
