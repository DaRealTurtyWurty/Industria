package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.ElectricFurnaceScreenHandler;
import dev.turtywurty.industria.util.MathUtils;
import dev.turtywurty.industria.util.ViewUtils;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ElectricFurnaceBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, RecipeExperienceBlockEntity, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("electric_furnace");

    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = new Reference2IntOpenHashMap<>();
    private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> matchGetter;

    private int progress = 0, maxProgress = 0;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.ELECTRIC_FURNACE, BlockEntityTypeInit.ELECTRIC_FURNACE, pos, state);

        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 1), Direction.UP);
        this.wrappedContainerStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.DOWN);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 25_000, 1_000, 0));

        this.matchGetter = RecipeManager.createCheck(RecipeType.SMELTING);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        if (energyStorage.amount < 10)
            return;

        SimpleContainer inputInventory = this.wrappedContainerStorage.getInventory(0);
        ItemStack inputStack = inputInventory.getItem(0);
        if (inputStack.isEmpty()) {
            int currentProgress = this.progress;
            this.progress = 0;
            this.maxProgress = 0;

            if (currentProgress > 0)
                update();

            return;
        }

        var recipeInput = new SingleRecipeInput(inputStack);

        Optional<RecipeHolder<SmeltingRecipe>> recipe = this.matchGetter.getRecipeFor(recipeInput, (ServerLevel) this.level);
        if (recipe.isEmpty())
            return;

        RecipeHolder<SmeltingRecipe> entry = recipe.get();
        if (!canAcceptOutput(entry, recipeInput))
            return;

        this.maxProgress = entry.value().cookingTime() / 2;

        if (this.progress >= this.maxProgress) {
            this.progress = 0;
            this.maxProgress = 0;

            ItemStack output = entry.value().assemble(recipeInput, this.level.registryAccess());
            this.wrappedContainerStorage.getInventory(1).addItem(output);
            inputStack.shrink(1);
            setLastRecipe(entry);
            update();
            return;
        }

        this.progress++;
        energyStorage.amount -= 10;
        update();
    }

    @Override
    public void endTick() {
        super.endTick();

        if (this.level == null || this.level.isClientSide())
            return;

        boolean running = this.progress > 0;
        if (getBlockState().getValue(BlockStateProperties.LIT) != running)
            this.level.setBlock(this.worldPosition, getBlockState().setValue(BlockStateProperties.LIT, running), Block.UPDATE_ALL);
    }

    private boolean canAcceptOutput(RecipeHolder<SmeltingRecipe> recipeEntry, SingleRecipeInput recipeInput) {
        ItemStack output = recipeEntry.value().assemble(recipeInput, this.level.registryAccess());
        if (output.isEmpty())
            return false;

        SimpleContainer outputInventory = this.wrappedContainerStorage.getInventory(1);
        ItemStack outputStack = outputInventory.getItem(0);
        if (outputStack.isEmpty())
            return true;

        if (!ItemStack.isSameItemSameComponents(outputStack, output))
            return false;

        return (outputStack.getCount() + output.getCount() <= output.getMaxStackSize()) && (outputStack.getCount() + output.getCount() <= outputInventory.getMaxStackSize());
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        this.progress = view.getShortOr("Progress", (short) 0);
        this.maxProgress = view.getShortOr("MaxProgress", (short) 0);
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);

        RecipeExperienceBlockEntity.readRecipesUsed(view, "RecipesUsed", this.recipesUsed);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {

        view.putShort("Progress", (short) this.progress);
        view.putShort("MaxProgress", (short) this.maxProgress);
        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);

        RecipeExperienceBlockEntity.writeRecipesUsed(view, "RecipesUsed", this.recipesUsed);
    }

    @Override
    public void setLastRecipe(@Nullable RecipeHolder<?> recipe) {
        if (recipe == null)
            return;

        this.recipesUsed.addTo(recipe.id(), 1);
    }

    @Override
    public void dropExperienceForRecipesUsed(ServerPlayer player) {
        List<RecipeHolder<?>> list = getRecipesUsedAndDropExperience(player.level(), player.position());
        player.awardRecipes(list);

        for (RecipeHolder<?> recipeEntry : list) {
            if (recipeEntry != null) {
                player.triggerRecipeCrafted(recipeEntry, this.wrappedContainerStorage.getStacks());
            }
        }

        this.recipesUsed.clear();
    }

    @Override
    public List<RecipeHolder<?>> getRecipesUsedAndDropExperience(ServerLevel world, Vec3 pos) {
        List<RecipeHolder<?>> list = new ArrayList<>();

        for (Reference2IntMap.Entry<ResourceKey<Recipe<?>>> entry : this.recipesUsed.reference2IntEntrySet()) {
            world.recipeAccess().byKey(entry.getKey()).ifPresent(recipe -> {
                list.add(recipe);
                RecipeExperienceBlockEntity.dropExperience(
                        world,
                        pos,
                        entry.getIntValue(),
                        ((AbstractCookingRecipe) recipe.value()).experience());
            });
        }

        return list;
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new ElectricFurnaceScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingSimpleInventory input = (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(0);
        OutputSimpleInventory output = (OutputSimpleInventory) this.wrappedContainerStorage.getInventory(1);
        SyncingEnergyStorage energy = (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        return List.of(input, output, energy);
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    @Override
    public WrappedContainerStorage<SimpleContainer> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    public ContainerStorage getInventoryProvider(Direction direction) {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction relative = MathUtils.getRelativeDirection(direction, facing);
        return this.wrappedContainerStorage.getStorage(relative);
    }

    public EnergyStorage getEnergyProvider(Direction direction) {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction relative = MathUtils.getRelativeDirection(direction, facing);
        return this.wrappedEnergyStorage.getStorage(relative);
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }

    @Override
    public void dropContents(Level world, BlockPos pos) {
        BlockEntityContentsDropper.super.dropContents(world, pos);
        if (world instanceof ServerLevel serverWorld) {
            getRecipesUsedAndDropExperience(serverWorld, Vec3.atCenterOf(pos));
        }
    }
}