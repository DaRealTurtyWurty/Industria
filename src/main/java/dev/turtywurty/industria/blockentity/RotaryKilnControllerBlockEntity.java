package dev.turtywurty.industria.blockentity;

import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.RotaryKilnBlock;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.heat.InputHeatStorage;
import dev.turtywurty.industria.blockentity.util.heat.WrappedHeatStorage;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.multiblock.LocalDirection;
import dev.turtywurty.industria.multiblock.PortType;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockable;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import dev.turtywurty.industria.multiblock.old.Multiblockable;
import dev.turtywurty.industria.multiblock.old.PositionedPortRule;
import dev.turtywurty.industria.recipe.RotaryKilnRecipe;
import dev.turtywurty.industria.recipe.input.SingleItemStackRecipeInput;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RotaryKilnControllerBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityContentsDropper, AutoMultiblockable {
    private static final List<PositionedPortRule> PORT_RULES = List.of(
            PositionedPortRule.when(p -> p.isCenterColumn() && p.y() == 4)
                    .on(LocalDirection.UP)
                    .types(PortType.input(TransferType.ITEM))
                    .build()
    );

    private final List<BlockPos> kilnSegments = new ArrayList<>();
    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedHeatStorage<SimpleHeatStorage> wrappedHeatStorage = new WrappedHeatStorage<>();

    private final List<InputRecipeEntry> recipes = new ArrayList<>();
    private int ticks = 0;

    public RotaryKilnControllerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.ROTARY_KILN_CONTROLLER, BlockEntityTypeInit.ROTARY_KILN_CONTROLLER, pos, state);

        this.wrappedInventoryStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1),
                Direction.UP, () -> RotaryKilnControllerBlockEntity.this.kilnSegments.size() >= 8);
        this.wrappedHeatStorage.addStorage(new InputHeatStorage(this, 2000, 2000));

    }

    public static ServerRecipeManager getRecipeManager(World world) {
        if (world == null || world.isClient())
            return null;

        return !(world instanceof ServerWorld serverWorld) ? null : serverWorld.getRecipeManager();
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
        for (InputRecipeEntry recipe : this.recipes) {
            if (recipe != null) {
                recipesEmpty = false;
                break;
            }
        }

        return this.kilnSegments.size() >= 8 && !recipesEmpty;
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient())
            return;

        if (this.ticks++ == 0)
            handleSegmentSearching();

        if (this.kilnSegments.size() < 8)
            return;

        handleInputStack();

        for (InputRecipeEntry recipe : this.recipes.stream().sorted(Comparator.comparingInt(InputRecipeEntry::getProgress)).toList()) {
            if (recipe == null) {
                Industria.LOGGER.warn("Found null recipe in Rotary Kiln Controller at {}. Removing it from the list.", this.pos);
                this.recipes.remove(null);
                update();
                continue;
            }

            Optional<RecipeEntry<?>> recipeEntry = getRecipe(recipe.registryKey());
            if (recipeEntry.isEmpty()) {
                Industria.LOGGER.warn("Recipe entry for {} not found in Rotary Kiln Controller at {}. Removing it from the list.", recipe.registryKey(), this.pos);
                this.recipes.remove(recipe);
                update();
                continue;
            }

            RotaryKilnRecipe kilnRecipe = (RotaryKilnRecipe) recipeEntry.get().value();
            if (kilnRecipe == null) {
                Industria.LOGGER.warn("Recipe for {} not found in Rotary Kiln Controller at {}. Removing it from the list.", recipe.registryKey(), this.pos);
                this.recipes.remove(recipe);
                update();
                continue;
            }

            if (recipe.getProgress() >= this.kilnSegments.size() * 100) {
                ItemStack outputStack = kilnRecipe.output().createStack(this.world.random);

                BlockPos endPos = this.kilnSegments.getLast();
                Direction facing = getCachedState().get(Properties.HORIZONTAL_FACING);
                BlockPos spawnPos = endPos.offset(facing);
                if (!tryOutputToStorage(spawnPos, facing, outputStack)) {
                    ItemScatterer.spawn(this.world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), outputStack);
                }

                this.recipes.remove(recipe);

                update();
                continue;
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

        if (this.recipes.stream().noneMatch(r -> r.progress <= 100)) {
            InputRecipeEntry inputRecipeEntry = new InputRecipeEntry(recipeEntry.get().id(), inputStack);

            this.recipes.add(inputRecipeEntry);

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
        if (this.world == null || this.world.isClient())
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
    protected void writeData(WriteView view) {
        ViewUtils.putChild(view, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.putChild(view, "Heat", this.wrappedHeatStorage);
        Multiblockable.write(this, view);

        view.put("KilnSegments", BlockPos.CODEC.listOf(), this.kilnSegments);

        var recipesView = view.getList("Recipes");
        for (InputRecipeEntry inputRecipeEntry : this.recipes) {
            if (inputRecipeEntry == null)
                continue;

            var recipeView = recipesView.add();

            RegistryKey<Recipe<?>> registryKey = inputRecipeEntry.registryKey;
            if (registryKey != null && registryKey.getValue() != null) {
                recipeView.put("RegistryKey", RegistryKey.createCodec(RegistryKeys.RECIPE), registryKey);
            }

            recipeView.put("InputStack", ItemStack.OPTIONAL_CODEC, inputRecipeEntry.inputStack);
            recipeView.putInt("Progress", inputRecipeEntry.progress);
            recipeView.put("UUID", Uuids.CODEC, inputRecipeEntry.uuid);
        }
    }

    @Override
    protected void readData(ReadView view) {
        ViewUtils.readChild(view, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.readChild(view, "Heat", this.wrappedHeatStorage);
        Multiblockable.read(this, view);

        this.kilnSegments.clear();
        view.read("KilnSegments", BlockPos.CODEC.listOf()).ifPresent(this.kilnSegments::addAll);

        this.recipes.clear();
        ReadView.ListReadView recipesView = view.getListReadView("Recipes");
        for (ReadView readView : recipesView) {
            RegistryKey<Recipe<?>> registryKey = readView.read(
                            "RegistryKey",
                            RegistryKey.createCodec(RegistryKeys.RECIPE))
                    .orElse(null);

            if (registryKey == null && Thread.currentThread().getName().toLowerCase(Locale.ROOT).contains("server")) {
                Industria.LOGGER.error("Failed to decode recipe registry key for Rotary Kiln at {}. This is likely a bug.", this.pos);
                continue;
            }

            ItemStack inputStack = readView.read("InputStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
            int progress = readView.getInt("Progress", 0);
            Optional<UUID> uuidOpt = readView.read("UUID", Uuids.CODEC);
            UUID uuid = uuidOpt.orElseGet(UUID::randomUUID);

            var inputRecipeEntry = new InputRecipeEntry(registryKey, inputStack, progress, uuid);
            this.recipes.add(inputRecipeEntry);
        }
    }

    public void handleSegmentSearching() {
        if (world == null || world.isClient())
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

            InputRecipeEntry inputRecipeEntry = this.recipes.stream()
                    .filter(recipe -> MathHelper.floor(recipe.getProgress() / 100f) == segmentIndex)
                    .findFirst()
                    .orElse(null);
            if (inputRecipeEntry == null)
                return;

            if (this.world != null && !this.world.isClient()) {
                ItemScatterer.spawn(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), inputRecipeEntry.inputStack());
            }

            this.recipes.remove(inputRecipeEntry);
        }
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
    public List<PositionedPortRule> getPortRules() {
        return PORT_RULES;
    }

    public InventoryStorage getInventoryProvider(Direction side) {
        return this.wrappedInventoryStorage.getStorage(side);
    }

    public List<BlockPos> getKilnSegments() {
        return this.kilnSegments;
    }

    public List<InputRecipeEntry> getRecipes() {
        return this.recipes;
    }

    public static final class InputRecipeEntry {

        private final UUID uuid;
        private final RegistryKey<Recipe<?>> registryKey;
        private final ItemStack inputStack;
        private int progress;

        public InputRecipeEntry(RegistryKey<Recipe<?>> registryKey, ItemStack inputStack) {
            this(registryKey, inputStack, 0, UUID.randomUUID());
        }

        public InputRecipeEntry(RegistryKey<Recipe<?>> registryKey, ItemStack inputStack, int progress, UUID uuid) {
            this.registryKey = registryKey;
            this.inputStack = inputStack;
            this.progress = progress;
            this.uuid = uuid;
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

        public UUID getUuid() {
            return uuid;
        }

        public void incrementProgress() {
            this.progress++;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (InputRecipeEntry) obj;

            return uuid.equals(that.uuid);
        }

        @Override
        public int hashCode() {
            return uuid.hashCode();
        }

        @Override
        public String toString() {
            return "InputRecipeEntry[" +
                    "registryKey=" + registryKey + ", " +
                    "inputStack=" + inputStack + ", " +
                    "progress=" + progress + ", " +
                    "uuid=" + uuid + ']';
        }
    }
}