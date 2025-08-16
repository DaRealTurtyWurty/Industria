package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.RecipeSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.multiblock.MultiblockIOPort;
import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.UpgradeStationOpenPayload;
import dev.turtywurty.industria.network.UpgradeStationUpdateRecipesPayload;
import dev.turtywurty.industria.recipe.UpgradeStationRecipe;
import dev.turtywurty.industria.screenhandler.UpgradeStationScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.*;

public class UpgradeStationBlockEntity extends IndustriaBlockEntity implements BlockEntityWithGui<UpgradeStationOpenPayload>, SyncableTickableBlockEntity, Multiblockable, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("upgrade_station");

    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final List<BlockPos> multiblockPositions = new ArrayList<>();
    private final List<RegistryKey<Recipe<?>>> availableRecipes = new ArrayList<>();
    @Nullable
    private RegistryKey<Recipe<?>> selectedRecipe;
    private int selectedRecipeIndex = 0;
    private ItemStack previousCenterStack = ItemStack.EMPTY;

    private int progress = 0;
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> UpgradeStationBlockEntity.this.progress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> UpgradeStationBlockEntity.this.progress = value;
            }
        }

        @Override
        public int size() {
            return 1;
        }
    };
    private boolean isFirstRead = true;
    private ItemStack overflowStack = ItemStack.EMPTY;

    public UpgradeStationBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.UPGRADE_STATION, BlockEntityTypeInit.UPGRADE_STATION, pos, state);

        var inputInventory = new SyncingSimpleInventory(this, 9);
        this.wrappedInventoryStorage.addInventory(inputInventory, Direction.UP);
        inputInventory.addListener(sender -> {
            if (this.world == null || !(this.world instanceof ServerWorld serverWorld))
                return;

            List<RegistryKey<Recipe<?>>> recipesForCenterStack = new ArrayList<>();
            ItemStack centerStack = sender.getStack(4);

            if (!centerStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(centerStack, this.previousCenterStack))
                return;

            if (centerStack.isEmpty()) {
                this.availableRecipes.clear();
                this.selectedRecipe = null;
                this.selectedRecipeIndex = 0;
                this.previousCenterStack = centerStack;

                updateHandlers(serverWorld);
                return;
            }

            ServerRecipeManager recipeManager = serverWorld.getRecipeManager();
            recipeManager.values().stream()
                    .filter(recipeEntry -> recipeEntry.value() instanceof UpgradeStationRecipe)
                    .filter(recipeEntry -> ((UpgradeStationRecipe) recipeEntry.value()).doesCenterStackMatch(centerStack))
                    .map(RecipeEntry::id)
                    .forEach(recipesForCenterStack::add);

            this.availableRecipes.clear();
            this.availableRecipes.addAll(recipesForCenterStack);
            this.selectedRecipe = this.availableRecipes.isEmpty() ? null : this.availableRecipes.getFirst();
            this.selectedRecipeIndex = 0;

            this.previousCenterStack = centerStack;

            updateHandlers(serverWorld);
        });

        this.wrappedInventoryStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.DOWN);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 100_000, 1_000, 0));
    }

    private static RegistryKey<Recipe<?>> getRecipeKey(String string) {
        return RegistryKey.of(RegistryKeys.RECIPE, Identifier.tryParse(string));
    }

    @Override
    public UpgradeStationOpenPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new UpgradeStationOpenPayload(this.pos, getRecipes());
    }

    private void updateHandlers(ServerWorld serverWorld) {
        Map<ServerPlayerEntity, UpgradeStationScreenHandler> handlers = new HashMap<>();
        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            if (player.currentScreenHandler instanceof UpgradeStationScreenHandler handler) {
                handlers.put(player, handler);
            }
        }

        if (handlers.isEmpty())
            return;

        List<UpgradeStationRecipe> recipes = getRecipes();
        for (Map.Entry<ServerPlayerEntity, UpgradeStationScreenHandler> entry : handlers.entrySet()) {
            entry.getValue().setAvailableRecipes(recipes);

            ServerPlayNetworking.send(entry.getKey(), new UpgradeStationUpdateRecipesPayload(recipes));
        }
    }

    private List<UpgradeStationRecipe> getRecipes() {
        if (this.world == null || !(this.world instanceof ServerWorld serverWorld))
            return Collections.emptyList();

        ServerRecipeManager recipeLookup = serverWorld.getRecipeManager();
        List<UpgradeStationRecipe> recipes = new ArrayList<>();
        for (RegistryKey<Recipe<?>> recipe : this.availableRecipes) {
            Optional<RecipeEntry<?>> opt = recipeLookup.get(recipe);
            opt.map(RecipeEntry::value)
                    .filter(UpgradeStationRecipe.class::isInstance)
                    .map(UpgradeStationRecipe.class::cast)
                    .ifPresent(recipes::add);
        }

        return recipes;
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new UpgradeStationScreenHandler(syncId, playerInventory, this, this.wrappedInventoryStorage, this.propertyDelegate, getRecipes());
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Multiblockable.write(this, view);
        ViewUtils.putChild(view, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
        if (this.selectedRecipe != null) {
            view.putString("SelectedRecipe", this.selectedRecipe.getValue().toString());
        }

        var availableRecipes = view.getListAppender("AvailableRecipes", RECIPE_CODEC);
        this.availableRecipes.forEach(availableRecipes::add);

        view.putInt("SelectedRecipeIndex", this.selectedRecipeIndex);

        view.putInt("Progress", this.progress);
    }

    @Override
    protected void readData(ReadView view) {
        Multiblockable.read(this, view);

        ViewUtils.readChild(view, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);

        this.selectedRecipe = getRecipeKey(Objects.requireNonNull(view.getString("SelectedRecipe", "")));

        this.availableRecipes.clear();
        for (var recipe : view.getTypedListView("AvailableRecipes", RECIPE_CODEC)) {
            this.availableRecipes.add(recipe);
        }

        this.selectedRecipeIndex = view.getInt("SelectedRecipeIndex", 0);

        this.progress = view.getInt("Progress", 0);

        if (this.isFirstRead) {
            this.isFirstRead = false;
            this.previousCenterStack = getInputInventory().getStack(4);
        }
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var inputInventory = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(0);
        var outputInventory = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(1);
        var energyStorage = (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        return List.of(inputInventory, outputInventory, energyStorage);
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        if (!this.overflowStack.isEmpty()) {
            SimpleInventory outputInventory = getOutputInventory();
            if (outputInventory.canInsert(this.overflowStack)) {
                outputInventory.addStack(this.overflowStack);
                this.overflowStack = ItemStack.EMPTY;
            }
        }

        if (this.selectedRecipe == null) {
            this.progress = 0;
            return;
        }

        UpgradeStationRecipe recipe = getCurrentRecipe();
        if (recipe == null) {
            this.progress = 0;
            return;
        }

        RecipeSimpleInventory recipeInventory = getRecipeInventory();
        if (this.progress >= 500) {
            if (recipe.matches(recipeInventory, this.world)) {
                ItemStack output = recipe.craft(recipeInventory, this.world.getRegistryManager());
                SimpleInventory outputInventory = getOutputInventory();
                if (outputInventory.canInsert(output)) {
                    outputInventory.addStack(output);
                    for (int index = 0; index < getInputInventory().size(); index++) {
                        ItemStack inSlot = recipeInventory.getStack(index);
                        if (!inSlot.isEmpty()) {
                            inSlot.decrement(recipe.getIngredient(index).stackData().count());
                        }
                    }
                } else {
                    this.overflowStack = output;
                }
            }

            this.progress = 0;
            update();
            return;
        }

        if (recipe.matches(recipeInventory, this.world)) {
            SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) getEnergyStorage();
            if (energyStorage.getAmount() < 200) {
                this.progress = 0;
                return;
            }

            energyStorage.amount -= 200;
            this.progress++;
        } else {
            this.progress = 0;
            return;
        }

        update();
    }

    private UpgradeStationRecipe getCurrentRecipe() {
        if (this.selectedRecipe == null || this.world == null || !(this.world instanceof ServerWorld serverWorld))
            return null;

        ServerRecipeManager recipeManager = serverWorld.getRecipeManager();
        Optional<RecipeEntry<?>> opt = recipeManager.get(this.selectedRecipe);
        if (opt.isEmpty())
            return null;

        Recipe<?> recipe = opt.get().value();
        if (!(recipe instanceof UpgradeStationRecipe upgradeStationRecipe))
            return null;

        return upgradeStationRecipe;
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.UPGRADE_STATION;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        List<BlockPos> positions = new ArrayList<>();
        positions.add(this.pos.up());

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 2; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                positions.add(this.pos.add(x, 0, z));
            }
        }

        return positions;
    }

    @Override
    public List<BlockPos> getMultiblockPositions() {
        return this.multiblockPositions;
    }

    public EnergyStorage getEnergyStorage() {
        return this.wrappedEnergyStorage.getStorage(null);
    }

    public RecipeSimpleInventory getRecipeInventory() {
        return this.wrappedInventoryStorage.getRecipeInventory();
    }

    public SimpleInventory getInputInventory() {
        return this.wrappedInventoryStorage.getInventory(0);
    }

    public SimpleInventory getOutputInventory() {
        return this.wrappedInventoryStorage.getInventory(1);
    }

    public List<RegistryKey<Recipe<?>>> getAvailableRecipes() {
        return this.availableRecipes;
    }

    public int getSelectedRecipeIndex() {
        return this.selectedRecipeIndex;
    }

    public void setSelectedRecipeIndex(int index) {
        if (this.world == null || this.world.isClient || index < 0 || index >= this.availableRecipes.size() || this.selectedRecipeIndex == index)
            return;

        this.selectedRecipeIndex = index;
        this.selectedRecipe = this.availableRecipes.get(index);
        update();
    }

    public int getProgress() {
        return this.progress;
    }

    @Override
    public Map<Direction, MultiblockIOPort> getPorts(Vec3i offsetFromPrimary, Direction direction) {
        Map<Direction, MultiblockIOPort> ports = new EnumMap<>(Direction.class);
        ports.put(direction, new MultiblockIOPort(direction, TransferType.ENERGY));

        return ports;
    }

    @Override
    public WrappedInventoryStorage<SimpleInventory> getWrappedInventoryStorage() {
        return this.wrappedInventoryStorage;
    }

    @Override
    public Block getBlock() {
        return getCachedState().getBlock();
    }
}