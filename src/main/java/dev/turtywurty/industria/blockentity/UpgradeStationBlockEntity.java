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
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.multiblock.LocalDirection;
import dev.turtywurty.industria.multiblock.PortType;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockable;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import dev.turtywurty.industria.multiblock.old.Multiblockable;
import dev.turtywurty.industria.multiblock.old.PositionedPortRule;
import dev.turtywurty.industria.network.UpgradeStationOpenPayload;
import dev.turtywurty.industria.network.UpgradeStationUpdateRecipesPayload;
import dev.turtywurty.industria.recipe.UpgradeStationRecipe;
import dev.turtywurty.industria.screenhandler.UpgradeStationScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.*;

public class UpgradeStationBlockEntity extends IndustriaBlockEntity implements BlockEntityWithGui<UpgradeStationOpenPayload>, SyncableTickableBlockEntity, AutoMultiblockable, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("upgrade_station");

    private static final List<PositionedPortRule> PORT_RULES = List.of(
            PositionedPortRule.when(p -> true)
                    .on(LocalDirection.values())
                    .types(PortType.input(TransferType.ENERGY))
                    .build()
    );

    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final List<BlockPos> multiblockPositions = new ArrayList<>();
    private final List<ResourceKey<Recipe<?>>> availableRecipes = new ArrayList<>();
    @Nullable
    private ResourceKey<Recipe<?>> selectedRecipe;
    private int selectedRecipeIndex = 0;
    private ItemStack previousCenterStack = ItemStack.EMPTY;

    private int progress = 0;
    private final ContainerData propertyDelegate = new ContainerData() {
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
        public int getCount() {
            return 1;
        }
    };
    private boolean isFirstRead = true;
    private ItemStack overflowStack = ItemStack.EMPTY;

    public UpgradeStationBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.UPGRADE_STATION, BlockEntityTypeInit.UPGRADE_STATION, pos, state);

        var inputInventory = new SyncingSimpleInventory(this, 9) {
            @Override
            public void setChanged() {
                super.setChanged();
                if (!(UpgradeStationBlockEntity.this.level instanceof ServerLevel serverWorld))
                    return;

                List<ResourceKey<Recipe<?>>> recipesForCenterStack = new ArrayList<>();
                ItemStack centerStack = getItem(4);

                if (!centerStack.isEmpty() && ItemStack.isSameItemSameComponents(centerStack, UpgradeStationBlockEntity.this.previousCenterStack))
                    return;

                if (centerStack.isEmpty()) {
                    UpgradeStationBlockEntity.this.availableRecipes.clear();
                    UpgradeStationBlockEntity.this.selectedRecipe = null;
                    UpgradeStationBlockEntity.this.selectedRecipeIndex = 0;
                    UpgradeStationBlockEntity.this.previousCenterStack = centerStack;

                    updateHandlers(serverWorld);
                    return;
                }

                RecipeManager recipeManager = serverWorld.recipeAccess();
                recipeManager.getRecipes().stream()
                        .filter(recipeEntry -> recipeEntry.value() instanceof UpgradeStationRecipe)
                        .filter(recipeEntry -> ((UpgradeStationRecipe) recipeEntry.value()).doesCenterStackMatch(centerStack))
                        .map(RecipeHolder::id)
                        .forEach(recipesForCenterStack::add);

                UpgradeStationBlockEntity.this.availableRecipes.clear();
                UpgradeStationBlockEntity.this.availableRecipes.addAll(recipesForCenterStack);
                UpgradeStationBlockEntity.this.selectedRecipe = UpgradeStationBlockEntity.this.availableRecipes.isEmpty() ? null : UpgradeStationBlockEntity.this.availableRecipes.getFirst();
                UpgradeStationBlockEntity.this.selectedRecipeIndex = 0;

                UpgradeStationBlockEntity.this.previousCenterStack = centerStack;

                updateHandlers(serverWorld);
            }
        };
        this.wrappedContainerStorage.addInventory(inputInventory, Direction.UP);

        this.wrappedContainerStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.DOWN);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 100_000, 1_000, 0));
    }

    private static ResourceKey<Recipe<?>> getRecipeKey(String string) {
        return ResourceKey.create(Registries.RECIPE, Identifier.tryParse(string));
    }

    @Override
    public UpgradeStationOpenPayload getScreenOpeningData(ServerPlayer player) {
        return new UpgradeStationOpenPayload(this.worldPosition, getRecipes());
    }

    private void updateHandlers(ServerLevel serverWorld) {
        Map<ServerPlayer, UpgradeStationScreenHandler> handlers = new HashMap<>();
        for (ServerPlayer player : serverWorld.players()) {
            if (player.containerMenu instanceof UpgradeStationScreenHandler handler) {
                handlers.put(player, handler);
            }
        }

        if (handlers.isEmpty())
            return;

        List<UpgradeStationRecipe> recipes = getRecipes();
        for (Map.Entry<ServerPlayer, UpgradeStationScreenHandler> entry : handlers.entrySet()) {
            entry.getValue().setAvailableRecipes(recipes);

            ServerPlayNetworking.send(entry.getKey(), new UpgradeStationUpdateRecipesPayload(recipes));
        }
    }

    private List<UpgradeStationRecipe> getRecipes() {
        if (this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return Collections.emptyList();

        RecipeManager recipeLookup = serverWorld.recipeAccess();
        List<UpgradeStationRecipe> recipes = new ArrayList<>();
        for (ResourceKey<Recipe<?>> recipe : this.availableRecipes) {
            Optional<RecipeHolder<?>> opt = recipeLookup.byKey(recipe);
            opt.map(RecipeHolder::value)
                    .filter(UpgradeStationRecipe.class::isInstance)
                    .map(UpgradeStationRecipe.class::cast)
                    .ifPresent(recipes::add);
        }

        return recipes;
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new UpgradeStationScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage, this.propertyDelegate, getRecipes());
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        Multiblockable.write(this, view);
        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
        if (this.selectedRecipe != null) {
            view.putString("SelectedRecipe", this.selectedRecipe.identifier().toString());
        }

        var availableRecipes = view.list("AvailableRecipes", RECIPE_CODEC);
        this.availableRecipes.forEach(availableRecipes::add);

        view.putInt("SelectedRecipeIndex", this.selectedRecipeIndex);

        view.putInt("Progress", this.progress);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        Multiblockable.read(this, view);

        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);

        this.selectedRecipe = getRecipeKey(Objects.requireNonNull(view.getStringOr("SelectedRecipe", "")));

        this.availableRecipes.clear();
        for (var recipe : view.listOrEmpty("AvailableRecipes", RECIPE_CODEC)) {
            this.availableRecipes.add(recipe);
        }

        this.selectedRecipeIndex = view.getIntOr("SelectedRecipeIndex", 0);

        this.progress = view.getIntOr("Progress", 0);

        if (this.isFirstRead) {
            this.isFirstRead = false;
            this.previousCenterStack = getInputInventory().getItem(4);
        }
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var inputInventory = (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(0);
        var outputInventory = (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(1);
        var energyStorage = (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        return List.of(inputInventory, outputInventory, energyStorage);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        if (!this.overflowStack.isEmpty()) {
            SimpleContainer outputInventory = getOutputInventory();
            if (outputInventory.canAddItem(this.overflowStack)) {
                outputInventory.addItem(this.overflowStack);
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
            if (recipe.matches(recipeInventory, this.level)) {
                ItemStack output = recipe.assemble(recipeInventory, this.level.registryAccess());
                SimpleContainer outputInventory = getOutputInventory();
                if (outputInventory.canAddItem(output)) {
                    outputInventory.addItem(output);
                    for (int index = 0; index < getInputInventory().getContainerSize(); index++) {
                        ItemStack inSlot = recipeInventory.getItem(index);
                        if (!inSlot.isEmpty()) {
                            inSlot.shrink(recipe.getIngredient(index).stackData().count());
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

        if (recipe.matches(recipeInventory, this.level)) {
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
        if (this.selectedRecipe == null || this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return null;

        RecipeManager recipeManager = serverWorld.recipeAccess();
        Optional<RecipeHolder<?>> opt = recipeManager.byKey(this.selectedRecipe);
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
        positions.add(this.worldPosition.above());

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 2; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                positions.add(this.worldPosition.offset(x, 0, z));
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
        return this.wrappedContainerStorage.getRecipeInventory();
    }

    public SimpleContainer getInputInventory() {
        return this.wrappedContainerStorage.getInventory(0);
    }

    public SimpleContainer getOutputInventory() {
        return this.wrappedContainerStorage.getInventory(1);
    }

    public List<ResourceKey<Recipe<?>>> getAvailableRecipes() {
        return this.availableRecipes;
    }

    public int getSelectedRecipeIndex() {
        return this.selectedRecipeIndex;
    }

    public void setSelectedRecipeIndex(int index) {
        if (this.level == null || this.level.isClientSide() || index < 0 || index >= this.availableRecipes.size() || this.selectedRecipeIndex == index)
            return;

        this.selectedRecipeIndex = index;
        this.selectedRecipe = this.availableRecipes.get(index);
        update();
    }

    public int getProgress() {
        return this.progress;
    }

    @Override
    public List<PositionedPortRule> getPortRules() {
        return PORT_RULES;
    }

    @Override
    public WrappedContainerStorage<SimpleContainer> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }
}