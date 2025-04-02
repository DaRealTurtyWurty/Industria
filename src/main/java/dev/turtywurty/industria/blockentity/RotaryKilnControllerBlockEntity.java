package dev.turtywurty.industria.blockentity;

import com.mojang.datafixers.util.Pair;
import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.RotaryKilnBlock;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.heat.InputHeatStorage;
import dev.turtywurty.industria.blockentity.util.heat.WrappedHeatStorage;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.multiblock.MultiblockIOPort;
import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.recipe.RotaryKilnRecipe;
import dev.turtywurty.industria.recipe.input.SingleItemStackRecipeInput;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RotaryKilnControllerBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity, BlockEntityContentsDropper, Multiblockable {
    private final List<BlockPos> kilnSegments = new ArrayList<>();
    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedHeatStorage<SimpleHeatStorage> wrappedHeatStorage = new WrappedHeatStorage<>();

    private final Map<Integer, InputRecipeEntry> recipes = Util.make(new HashMap<>(), map -> {
        for (int i = 0; i < 16; i++) {
            map.put(i, null);
        }
    });

    private int ticks = 0;

    public RotaryKilnControllerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.ROTARY_KILN_CONTROLLER, pos, state);

        this.wrappedInventoryStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1),
                Direction.UP, () -> RotaryKilnControllerBlockEntity.this.kilnSegments.size() >= 8);
        this.wrappedHeatStorage.addStorage(new InputHeatStorage(this, 2000, 2000));
    }

    @Override
    public WrappedInventoryStorage<?> getWrappedInventoryStorage() {
        return this.wrappedInventoryStorage;
    }

    @Override
    public Block getBlock() {
        return getCachedState().getBlock();
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingSimpleInventory inventory = getInventory();
        InputHeatStorage heatStorage = getHeatStorage();
        return List.of(inventory, heatStorage);
    }

    private InputHeatStorage getHeatStorage() {
        return (InputHeatStorage) this.wrappedHeatStorage.getStorage(0);
    }

    private SyncingSimpleInventory getInventory() {
        return (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(0);
    }

    public boolean isProcessing() {
        boolean recipesEmpty = true;
        for (int index = 0; index < this.recipes.size(); index++) {
            InputRecipeEntry recipe = this.recipes.get(index);
            if (recipe != null) {
                recipesEmpty = false;
                break;
            }
        }

        return this.kilnSegments.size() >= 8 && !recipesEmpty;
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        if (this.ticks++ == 0)
            handleSegmentSearching();

        if (this.kilnSegments.size() < 8)
            return;

        handleInputStack();

        for (int index = this.recipes.size() - 1; index >= 0; index--) {
            InputRecipeEntry recipe = this.recipes.get(index);
            if (recipe == null)
                continue;

            Optional<RecipeEntry<?>> recipeEntry = getRecipe(recipe.registryKey());
            if (recipeEntry.isEmpty())
                continue;

            RotaryKilnRecipe kilnRecipe = (RotaryKilnRecipe) recipeEntry.get().value();
            if (kilnRecipe == null)
                continue;

            if (recipe.getProgress() % 100 == 0) { // TODO: Replace with a field in the recipe
                if (index == this.kilnSegments.size() - 1) {
                    ItemStack outputStack = kilnRecipe.output().createStack(this.world.random);

                    BlockPos endPos = this.kilnSegments.get(index);
                    Direction facing = getCachedState().get(Properties.HORIZONTAL_FACING);
                    BlockPos spawnPos = endPos.offset(facing);
                    if (!tryOutputToStorage(spawnPos, facing, outputStack)) {
                        ItemScatterer.spawn(this.world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), outputStack);
                    }

                    this.recipes.put(index, null);

                    update();
                    continue;
                }

                // Check to see if the recipe to the right is null or not. if it is, more this one over and reset the progress
                int nextIndex = index + 1;
                if (nextIndex > this.recipes.size() - 1 || this.recipes.get(nextIndex) == null) {
                    this.recipes.put(nextIndex, recipe);
                    this.recipes.put(index, null);
                    update();
                }
            }

            recipe.incrementProgress();
            update();
        }
    }

    private void handleInputStack() {
        SyncingSimpleInventory inventory = getInventory();
        ItemStack inputStack = inventory.getStack(0).copy();
        if (inputStack.isEmpty())
            return;

        Optional<RecipeEntry<RotaryKilnRecipe>> recipeEntry = getMatchingRecipe(inputStack);
        if (recipeEntry.isEmpty())
            return;

        RotaryKilnRecipe recipe = recipeEntry.get().value();
        if (recipe == null)
            return;

        InputRecipeEntry inputRecipeEntry = this.recipes.get(0);
        if (inputRecipeEntry == null) {
            this.recipes.put(0, new InputRecipeEntry(recipeEntry.get().id(), inputStack));
            inventory.removeStack(0, recipe.input().stackData().count());
            update();
        }
    }

    private boolean tryOutputToStorage(BlockPos spawnPos, Direction facing, ItemStack outputStack) {
        Storage<ItemVariant> itemStorage = ItemStorage.SIDED.find(this.world, spawnPos, facing.getOpposite());
        if (itemStorage == null || !itemStorage.supportsInsertion())
            return false;

        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = itemStorage.insert(ItemVariant.of(outputStack), outputStack.getCount(), transaction);
            if (inserted == 0)
                return false;

            transaction.commit();
            return true;
        }
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.ROTARY_KILN_CONTROLLER;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        if (this.world == null || this.world.isClient)
            return List.of();

        // 5x5x1 structure
        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();

        int widthRange = 2;  // -2 to 2 = 5 blocks
        int heightRange = 4; // 0 to 4 = 5 blocks
        int depthRange = 0;  // 0 only = 1 block

        if (facing == null)
            throw new NullPointerException("Unexpected facing direction: null");

        // Define axis-aligned directions based on facing
        Direction right = facing.rotateYClockwise();

        // The main loop structure is the same for all directions
        for (int w = -widthRange; w <= widthRange; w++) {
            for (int h = 0; h <= heightRange; h++) {
                for (int d = 0; d <= depthRange; d++) {
                    if (w == 0 && h == 0 && d == 0)
                        continue;

                    BlockPos pos = this.pos
                            .offset(right, w)
                            .offset(Direction.UP, h)
                            .offset(facing, d);

                    if (this.world.getBlockState(pos).isReplaceable()) {
                        positions.add(pos);
                    } else {
                        invalidPositions.add(pos);
                    }
                }
            }
        }

        return invalidPositions.isEmpty() ? positions : List.of();
    }

    @Override
    public List<BlockPos> getMultiblockPositions() {
        return this.multiblockPositions;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        nbt.put("Inventory", this.wrappedInventoryStorage.writeNbt(registries));
        nbt.put("Heat", this.wrappedHeatStorage.writeNbt(registries));
        nbt.put("MachinePositions", Multiblockable.writeMultiblockToNbt(this));

        var kilnSegments = new NbtList();
        for (BlockPos pos : this.kilnSegments) {
            kilnSegments.add(NbtHelper.fromBlockPos(pos));
        }

        nbt.put("KilnSegments", kilnSegments);

        var recipes = new NbtList();
        for (int index = 0; index < this.recipes.size(); index++) {
            InputRecipeEntry inputRecipeEntry = this.recipes.get(index);
            if (inputRecipeEntry == null)
                continue;

            var recipeNbt = new NbtCompound();
            recipeNbt.putInt("Index", index);

            RegistryKey<Recipe<?>> registryKey = inputRecipeEntry.registryKey();
            if (registryKey != null) {
                Identifier identifier = registryKey.getValue();
                if(identifier == null) {
                    Industria.LOGGER.error("Failed to encode recipe registry key for Rotary Kiln at {}. This specific case should never happen though. Recipe was {}.", this.pos, registryKey);
                    continue;
                }

                Identifier.CODEC.encodeStart(NbtOps.INSTANCE, identifier)
                        .resultOrPartial(Industria.LOGGER::error)
                        .ifPresent(nbtElement -> recipeNbt.put("RegistryKey", nbtElement));
            } else {
                Industria.LOGGER.error("Failed to encode recipe registry key for Rotary Kiln at {}. This is likely a bug.", this.pos);
                continue;
            }

            recipeNbt.put("InputStack", inputRecipeEntry.inputStack().toNbtAllowEmpty(registries));
            recipeNbt.putInt("Progress", inputRecipeEntry.getProgress());
            recipes.add(recipeNbt);
        }

        nbt.put("Recipes", recipes);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        if (nbt.contains("Inventory", NbtElement.LIST_TYPE))
            this.wrappedInventoryStorage.readNbt(nbt.getList("Inventory", NbtElement.COMPOUND_TYPE), registries);

        if (nbt.contains("Heat", NbtElement.LIST_TYPE))
            this.wrappedHeatStorage.readNbt(nbt.getList("Heat", NbtElement.COMPOUND_TYPE), registries);

        if (nbt.contains("MachinePositions", NbtElement.LIST_TYPE))
            Multiblockable.readMultiblockFromNbt(this, nbt.getList("MachinePositions", NbtElement.INT_ARRAY_TYPE));

        if (nbt.contains("KilnSegments", NbtElement.LIST_TYPE)) {
            this.kilnSegments.clear();

            NbtList kilnSegments = nbt.getList("KilnSegments", NbtElement.INT_ARRAY_TYPE);
            for (int i = 0; i < kilnSegments.size(); i++) {
                int[] ints = kilnSegments.getIntArray(i);
                if (ints.length != 3)
                    continue;

                this.kilnSegments.add(i, new BlockPos(ints[0], ints[1], ints[2]));
            }
        }

        if (nbt.contains("Recipes", NbtElement.LIST_TYPE)) {
            for (int i = 0; i < 16; i++) {
                this.recipes.put(i, null);
            }

            NbtList recipes = nbt.getList("Recipes", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < recipes.size(); i++) {
                NbtCompound recipeNbt = recipes.getCompound(i);
                int index = recipeNbt.getInt("Index");
                if (index < 0 || index > this.kilnSegments.size() - 1)
                    continue;

                RegistryKey<Recipe<?>> registryKey = RegistryKey.of(RegistryKeys.RECIPE,
                        Identifier.CODEC.decode(NbtOps.INSTANCE, recipeNbt.get("RegistryKey"))
                                .map(Pair::getFirst)
                                .resultOrPartial(Industria.LOGGER::error)
                                .orElse(null));
                if (registryKey == null && Thread.currentThread().getName().toLowerCase(Locale.ROOT).contains("server")) {
                    Industria.LOGGER.error("Failed to decode recipe registry key for Rotary Kiln at {}. This is likely a bug.", this.pos);
                    continue;
                }

                ItemStack inputStack = ItemStack.fromNbtOrEmpty(registries, recipeNbt.getCompound("InputStack"));

                int progress = recipeNbt.getInt("Progress");
                this.recipes.put(index, new InputRecipeEntry(registryKey, inputStack, progress));
            }
        }
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        var nbt = super.toInitialChunkDataNbt(registries);
        writeNbt(nbt, registries);
        return nbt;
    }

    public void handleSegmentSearching() {
        if (world == null || world.isClient)
            return;

        Direction facing = getCachedState().get(Properties.HORIZONTAL_FACING);

        for (int segmentIndex = 1; segmentIndex <= 15; segmentIndex++) {
            BlockPos offsetPos = pos.offset(facing);
            BlockState offsetState = world.getBlockState(offsetPos);
            if (offsetState.isOf(BlockInit.ROTARY_KILN)) {
                world.setBlockState(offsetPos, offsetState.with(RotaryKilnBlock.SEGMENT_INDEX, segmentIndex));
                addKilnSegment(offsetPos);
            } else {
                break;
            }
        }
    }

    public void addKilnSegment(BlockPos pos) {
        if (this.kilnSegments.contains(pos))
            return;

        this.kilnSegments.add(pos);
        update();
    }

    public void removeKilnSegment(BlockPos pos) {
        int segmentIndex = this.kilnSegments.indexOf(pos);
        if (segmentIndex != -1) {
            this.kilnSegments.remove(segmentIndex);
            update();

            if (segmentIndex > this.recipes.size() - 1)
                return;

            InputRecipeEntry inputRecipeEntry = this.recipes.get(segmentIndex);
            if(inputRecipeEntry == null)
                return;

            if (this.world != null && !this.world.isClient) {
                ItemScatterer.spawn(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), inputRecipeEntry.inputStack());
            }

            this.recipes.put(segmentIndex, null);
        }
    }

    public static ServerRecipeManager getRecipeManager(World world) {
        if (world == null || world.isClient)
            return null;

        return !(world instanceof ServerWorld serverWorld) ? null : serverWorld.getRecipeManager();
    }

    public Optional<RecipeEntry<?>> getRecipe(RegistryKey<Recipe<?>> recipeKey) {
        ServerRecipeManager recipeManager = getRecipeManager(this.world);
        if (recipeManager == null)
            return Optional.empty();

        return recipeManager.get(recipeKey);
    }

    public Optional<RecipeEntry<RotaryKilnRecipe>> getMatchingRecipe(ItemStack stack) {
        ServerRecipeManager recipeManager = getRecipeManager(this.world);
        if (recipeManager == null)
            return Optional.empty();

        return recipeManager.getFirstMatch(RecipeTypeInit.ROTARY_KILN, SingleItemStackRecipeInput.of(stack), this.world);
    }

    @Override
    public Map<Direction, MultiblockIOPort> getPorts(Vec3i offsetFromPrimary, Direction direction) {
        Map<Direction, List<TransferType<?, ?, ?>>> transferTypes = new EnumMap<>(Direction.class);
        if (Multiblockable.isCenterColumn(offsetFromPrimary) && offsetFromPrimary.getY() == 4 && direction == Direction.UP) {
            transferTypes.put(direction, List.of(TransferType.ITEM));
        }

        return Multiblockable.toIOPortMap(transferTypes);
    }

    public InventoryStorage getInventoryProvider(Direction side) {
        return this.wrappedInventoryStorage.getStorage(side);
    }

    public List<BlockPos> getKilnSegments() {
        return this.kilnSegments;
    }

    public Map<Integer, InputRecipeEntry> getRecipes() {
        return this.recipes;
    }

    public static final class InputRecipeEntry {
        private final RegistryKey<Recipe<?>> registryKey;
        private final ItemStack inputStack;
        private int progress;

        public InputRecipeEntry(RegistryKey<Recipe<?>> registryKey, ItemStack inputStack) {
            this.registryKey = registryKey;
            this.inputStack = inputStack;
        }

        public InputRecipeEntry(RegistryKey<Recipe<?>> registryKey, ItemStack inputStack, int progress) {
            this(registryKey, inputStack);
            this.progress = progress;
        }

        public RegistryKey<Recipe<?>> registryKey() {
            return registryKey;
        }

        public ItemStack inputStack() {
            return inputStack;
        }

        public int getProgress() {
            return this.progress;
        }

        public void incrementProgress() {
            this.progress++;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (InputRecipeEntry) obj;
            return Objects.equals(this.registryKey, that.registryKey) &&
                    Objects.equals(this.inputStack, that.inputStack) &&
                    this.progress == that.progress;
        }

        @Override
        public int hashCode() {
            return Objects.hash(registryKey, inputStack, progress);
        }

        @Override
        public String toString() {
            return "InputRecipeEntry[" +
                    "registryKey=" + registryKey + ", " +
                    "inputStack=" + inputStack + ", " +
                    "progress=" + progress + ']';
        }
    }
}
