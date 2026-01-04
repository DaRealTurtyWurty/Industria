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
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.CrusherRecipe;
import dev.turtywurty.industria.screenhandler.CrusherScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CrusherBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("crusher");
    public static final int INPUT_SLOT = 0, OUTPUT_SLOT = 1;
    private static final AABB PICKUP_AREA = new AABB(0, 0, 0, 1, 0.7, 1);
    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();

    private final ItemStack[] buffer = new ItemStack[2];
    private int progress, maxProgress;
    private final ContainerData propertyDelegate = new ContainerData() {
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

    public CrusherBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.CRUSHER, BlockEntityTypeInit.CRUSHER, pos, state);

        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 1), Direction.UP);
        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 2), Direction.DOWN);

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 10_000, 1_000, 0));
    }

    public static long getEnergyCost() {
        return 10;
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var input = (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(INPUT_SLOT);
        var output = (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(OUTPUT_SLOT);
        SyncingEnergyStorage energy = getEnergy();

        return List.of(input, output, energy);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        listenForItemEntities();

        for (int index = 0; index < this.buffer.length; index++) {
            ItemStack stack = this.buffer[index];
            if (stack == null) {
                this.buffer[index] = ItemStack.EMPTY;
                continue;
            }

            if (!stack.isEmpty()) {
                stack = this.wrappedContainerStorage.getInventory(OUTPUT_SLOT).addItem(stack);
                this.buffer[index] = stack;
            }
        }

        if (hasItemsInBuffer()) {
            update();
            return;
        }

        if (this.currentRecipeId == null) {
            Optional<RecipeHolder<CrusherRecipe>> recipeEntry = getCurrentRecipe();
            if (recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().processTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeHolder<CrusherRecipe>> recipeEntry = getCurrentRecipe();
        Tuple<ItemStack, ItemStack> outputs;
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            this.currentRecipeId = null;
            this.maxProgress = 0;
            this.progress = 0;
            update();
            return;
        } else {
            outputs = recipeEntry.get().value().assemble(getInventory(), this.level.getRandom());
            if (!canOutput(outputs.getA()) || !canOutput(outputs.getB())) {
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
                ItemStack outputA = outputs.getA();
                ItemStack outputB = outputs.getB();
                if (!canOutput(outputA) || !canOutput(outputB))
                    return;

                consumeEnergy();
                this.wrappedContainerStorage.getInventory(INPUT_SLOT).removeItem(0, recipe.input().stackData().count());

                if (!outputA.isEmpty())
                    this.wrappedContainerStorage.getInventory(OUTPUT_SLOT).addItem(outputA);

                if (!outputB.isEmpty())
                    this.wrappedContainerStorage.getInventory(OUTPUT_SLOT).addItem(outputB);

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
        List<ItemEntity> entities = this.level.getEntitiesOfClass(ItemEntity.class,
                PICKUP_AREA.move(this.worldPosition),
                entity -> {
                    ItemStack stack = entity.getItem().copy();
                    stack.setCount(1);
                    return canInput(stack);
                });
        if (entities.isEmpty())
            return;

        for (ItemEntity entity : entities) {
            ItemStack stack = entity.getItem().copy();
            stack = this.wrappedContainerStorage.getInventory(INPUT_SLOT).addItem(stack);
            if (stack.isEmpty()) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            } else {
                entity.setItem(stack);
            }
        }
    }

    private boolean canOutput(ItemStack stack) {
        if (stack.isEmpty())
            return true;

        return this.wrappedContainerStorage.getInventory(OUTPUT_SLOT).canAddItem(stack);
    }

    private boolean canInput(ItemStack stack) {
        return this.wrappedContainerStorage.getInventory(INPUT_SLOT).canAddItem(stack);
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
        return this.wrappedContainerStorage.getRecipeInventory();
    }

    @Override
    public WrappedContainerStorage<SimpleContainer> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }

    private Optional<RecipeHolder<CrusherRecipe>> getCurrentRecipe() {
        if (this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return Optional.empty();

        return serverWorld.recipeAccess().getRecipeFor(RecipeTypeInit.CRUSHER, getInventory(), this.level);
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
        return new CrusherScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage, this.propertyDelegate);
    }

    private boolean hasItemsInBuffer() {
        return Arrays.stream(this.buffer).anyMatch(stack -> stack == null || !stack.isEmpty());
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);
        if (this.currentRecipeId != null) {
            view.store("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        if (hasItemsInBuffer()) {
            var bufferArray = view.list("Buffer", ItemStack.CODEC);
            for (ItemStack stack : this.buffer) {
                if (stack == null || stack.isEmpty())
                    continue;

                bufferArray.add(stack);
            }
        }

        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        this.progress = view.getIntOr("Progress", 0);
        this.maxProgress = view.getIntOr("MaxProgress", 0);

        this.currentRecipeId = view.read("CurrentRecipe", ResourceKey.codec(Registries.RECIPE))
                .orElse(null);

        for (int i = 0; i < view.listOrEmpty("Buffer", ItemStack.CODEC).stream().toList().size(); i++) {

            this.buffer[i] = view.read(ItemStack.MAP_CODEC)
                    .orElse(ItemStack.EMPTY);
        }

        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);
    }

    public ContainerStorage getInventoryProvider(Direction direction) {
        return this.wrappedContainerStorage.getStorage(direction);
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