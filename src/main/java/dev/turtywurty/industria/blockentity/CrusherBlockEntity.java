package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.CrusherBlock;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.inventory.RecipeSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.CrusherRecipe;
import dev.turtywurty.industria.screenhandler.CrusherScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
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
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CrusherBlockEntity extends UpdatableBlockEntity implements TickableBlockEntity, ExtendedScreenHandlerFactory<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("crusher");
    public static final int INPUT_SLOT = 0, OUTPUT_SLOT = 1;

    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();

    private final ItemStack[] buffer = new ItemStack[2];
    private int progress, maxProgress;
    private RegistryKey<Recipe<?>> currentRecipeId;

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

    public CrusherBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.CRUSHER, pos, state);

        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 1), Direction.UP);
        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 2), Direction.DOWN);

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 10000, 1000, 0));
    }

    @Override
    public void tick() {
        if (this.world == null || this.world.isClient)
            return;

        listenForItemEntities();

        for (int index = 0; index < this.buffer.length; index++) {
            ItemStack stack = this.buffer[index];
            if(stack == null) {
                this.buffer[index] = ItemStack.EMPTY;
                continue;
            }

            if (!stack.isEmpty()) {
                stack = this.wrappedInventoryStorage.getInventory(OUTPUT_SLOT).addStack(stack);
                this.buffer[index] = stack;
            }
        }

        if(hasItemsInBuffer()) {
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
            if(!canOutput(outputs.getLeft()) || !canOutput(outputs.getRight())) {
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
                if(!canOutput(outputA) || !canOutput(outputB))
                    return;

                consumeEnergy();
                this.wrappedInventoryStorage.getInventory(INPUT_SLOT).removeStack(0, recipe.input().count());

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
                CrusherBlock.PICKUP_AREA.offset(this.pos),
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
        if(stack.isEmpty())
            return true;

        return this.wrappedInventoryStorage.getInventory(OUTPUT_SLOT).canInsert(stack);
    }

    private boolean canInput(ItemStack stack) {
        return this.wrappedInventoryStorage.getInventory(INPUT_SLOT).canInsert(stack);
    }

    // TODO: Create getEnergy method instead of hardcoding 10
    private boolean hasEnergy() {
        return getEnergy().getAmount() >= 10;
    }

    // TODO: Create getEnergy method instead of hardcoding 10
    private void consumeEnergy() {
        this.wrappedEnergyStorage.getStorage(null).amount -= 10;
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

    public WrappedInventoryStorage<SimpleInventory> getWrappedInventoryStorage() {
        return this.wrappedInventoryStorage;
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
        return new CrusherScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    private boolean hasItemsInBuffer() {
        return Arrays.stream(this.buffer).anyMatch(stack -> stack == null || !stack.isEmpty());
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("Progress", this.progress);
        nbt.putInt("MaxProgress", this.maxProgress);
        if (this.currentRecipeId != null) {
            Optional<NbtElement> result = RegistryKey.createCodec(RegistryKeys.RECIPE)
                    .encodeStart(NbtOps.INSTANCE, this.currentRecipeId)
                    .result();
            result.ifPresent(nbtElement -> nbt.put("CurrentRecipe", nbtElement));
        }

        if(hasItemsInBuffer()) {
            var bufferArray = new NbtList();
            for (ItemStack stack : this.buffer) {
                if(stack == null || stack.isEmpty())
                    continue;

                bufferArray.add(stack.toNbt(registryLookup));
            }

            nbt.put("Buffer", bufferArray);
        }

        nbt.put("Inventory", this.wrappedInventoryStorage.writeNbt(registryLookup));
        nbt.put("Energy", this.wrappedEnergyStorage.writeNbt(registryLookup));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (nbt.contains("Progress", NbtElement.INT_TYPE))
            this.progress = nbt.getInt("Progress");

        if (nbt.contains("MaxProgress", NbtElement.INT_TYPE))
            this.maxProgress = nbt.getInt("MaxProgress");

        if (nbt.contains("CurrentRecipe", NbtElement.COMPOUND_TYPE)) {
            NbtCompound currentRecipe = nbt.getCompound("CurrentRecipe");
            this.currentRecipeId = currentRecipe.isEmpty() ? null :
                    RegistryKey.createCodec(RegistryKeys.RECIPE)
                            .decode(NbtOps.INSTANCE, currentRecipe)
                            .map(com.mojang.datafixers.util.Pair::getFirst)
                            .result()
                            .orElse(null);
        }

        if (nbt.contains("Buffer", NbtElement.LIST_TYPE)) {
            NbtList bufferArray = nbt.getList("Buffer", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < bufferArray.size(); i++) {
                this.buffer[i] = ItemStack.fromNbt(registryLookup, bufferArray.getCompound(i)).orElse(ItemStack.EMPTY);
            }
        }

        if (nbt.contains("Inventory", NbtElement.LIST_TYPE))
            this.wrappedInventoryStorage.readNbt(nbt.getList("Inventory", NbtElement.COMPOUND_TYPE), registryLookup);

        if (nbt.contains("Energy", NbtElement.LIST_TYPE))
            this.wrappedEnergyStorage.readNbt(nbt.getList("Energy", NbtElement.COMPOUND_TYPE), registryLookup);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = new NbtCompound();
        writeNbt(nbt, registryLookup);
        return nbt;
    }

    public InventoryStorage getInventoryProvider(Direction direction) {
        return this.wrappedInventoryStorage.getStorage(direction);
    }

    public EnergyStorage getEnergyProvider(Direction direction) {
        return this.wrappedEnergyStorage.getStorage(direction);
    }

    public EnergyStorage getEnergy() {
        return this.wrappedEnergyStorage.getStorage(Direction.SOUTH);
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }
}
