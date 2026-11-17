package dev.turtywurty.industria.blockentity;

import com.mojang.datafixers.util.Pair;
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
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.menu.CrusherScreenHandler;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.CrusherRecipe;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static dev.turtywurty.industria.blockentity.util.StorageOperations.extract;

public class CrusherBlockEntity extends IndustriaBlockEntity
    implements
        SyncableTickableBlockEntity,
        BlockEntityWithGui<BlockPosPayload>,
        BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("crusher");
    public static final int INPUT_SLOT = 0, OUTPUT_SLOT = 1;
    private static final AABB PICKUP_AREA = new AABB(0, 0, 0, 1, 0.7, 1);
    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();

    private final ItemStack[] buffer = {ItemStack.EMPTY, ItemStack.EMPTY};
    private List<ItemStack> pendingOutputs = List.of();
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
        super(ModBlocks.CRUSHER.get(), ModBlockEntityTypes.CRUSHER.get(), pos, state);

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
                Pair<ItemStack, ItemStack> outputs = recipeEntry.get().value().assemble(this.level.getRandom());
                this.pendingOutputs = List.of(outputs.getFirst(), outputs.getSecond());
                update();
            }

            return;
        }

        Optional<RecipeHolder<CrusherRecipe>> recipeEntry = getCurrentRecipe();
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            reset();
            return;
        }

        if (!canOutput())
            return;

        CrusherRecipe recipe = recipeEntry.get().value();
        if (this.progress >= this.maxProgress) {
            if (hasEnergy()) {
                consumeEnergy();
                this.wrappedContainerStorage.getInventory(INPUT_SLOT).removeItem(0, recipe.input().stackData().count());

                for (int i = 0; i < this.pendingOutputs.size(); i++) {
                    this.buffer[i] = this.wrappedContainerStorage.getInventory(OUTPUT_SLOT)
                        .addItem(this.pendingOutputs.get(i).copy());
                }

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

    private boolean canOutput() {
        if (this.pendingOutputs.isEmpty())
            return false;

        SimpleContainer output = this.wrappedContainerStorage.getInventory(OUTPUT_SLOT);
        var simulated = new SimpleContainer(output.getContainerSize());
        for (int i = 0; i < output.getContainerSize(); i++) {
            simulated.setItem(i, output.getItem(i).copy());
        }

        for (ItemStack stack : this.pendingOutputs) {
            if (!simulated.addItem(stack.copy()).isEmpty())
                return false;
        }

        return true;
    }

    private boolean canInput(ItemStack stack) {
        return this.wrappedContainerStorage.getInventory(INPUT_SLOT).canAddItem(stack);
    }

    private boolean hasEnergy() {
        return getEnergy().getAmount() >= getEnergyCost();
    }

    private void consumeEnergy() {
        extract(getEnergy(), getEnergyCost());
    }

    private void reset() {
        this.progress = 0;
        this.maxProgress = 0;
        this.currentRecipeId = null;
        this.pendingOutputs = List.of();
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

        return serverWorld.recipeAccess().getRecipeFor(ModRecipeTypes.CRUSHER.get(), getInventory(), this.level);
    }

    @Override
    public BlockPosPayload getMenuOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new CrusherScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage,
            this.propertyDelegate);
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
            view.store("PendingOutputs", ItemStack.OPTIONAL_CODEC.listOf(), this.pendingOutputs);
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

        this.pendingOutputs = view.read("PendingOutputs", ItemStack.OPTIONAL_CODEC.listOf())
            .filter(stacks -> stacks.size() == 2).orElse(List.of());
        if (this.currentRecipeId == null || this.pendingOutputs.isEmpty()) {
            this.currentRecipeId = null;
            this.pendingOutputs = List.of();
            this.progress = 0;
            this.maxProgress = 0;
        }

        Arrays.fill(this.buffer, ItemStack.EMPTY);
        List<ItemStack> savedBuffer = view.listOrEmpty("Buffer", ItemStack.CODEC).stream().toList();
        for (int i = 0; i < Math.min(savedBuffer.size(), this.buffer.length); i++) {
            this.buffer[i] = savedBuffer.get(i);
        }

        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);
    }

    public ResourceStorage<ResourceVariant<Item>> getInventoryProvider(Direction direction) {
        return this.wrappedContainerStorage.getStorage(direction);
    }

    public ResourceStorage<ResourceVariant<UnitResource>> getEnergyProvider(Direction direction) {
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
