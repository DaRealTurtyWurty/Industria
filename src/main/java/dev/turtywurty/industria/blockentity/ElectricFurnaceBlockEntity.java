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
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.ElectricFurnaceScreenHandler;
import dev.turtywurty.industria.util.MathUtils;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ElectricFurnaceBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, RecipeExperienceBlockEntity, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("electric_furnace");

    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed = new Reference2IntOpenHashMap<>();
    private final ServerRecipeManager.MatchGetter<SingleStackRecipeInput, SmeltingRecipe> matchGetter;

    private int progress = 0, maxProgress = 0;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.ELECTRIC_FURNACE, BlockEntityTypeInit.ELECTRIC_FURNACE, pos, state);

        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 1), Direction.UP);
        this.wrappedInventoryStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.DOWN);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 25_000, 1_000, 0));

        this.matchGetter = ServerRecipeManager.createCachedMatchGetter(RecipeType.SMELTING);
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        if (energyStorage.amount < 10)
            return;

        SimpleInventory inputInventory = this.wrappedInventoryStorage.getInventory(0);
        ItemStack inputStack = inputInventory.getStack(0);
        if (inputStack.isEmpty()) {
            int currentProgress = this.progress;
            this.progress = 0;
            this.maxProgress = 0;

            if (currentProgress > 0)
                update();

            return;
        }

        var recipeInput = new SingleStackRecipeInput(inputStack);

        Optional<RecipeEntry<SmeltingRecipe>> recipe = this.matchGetter.getFirstMatch(recipeInput, (ServerWorld) this.world);
        if (recipe.isEmpty())
            return;

        RecipeEntry<SmeltingRecipe> entry = recipe.get();
        if (!canAcceptOutput(entry, recipeInput))
            return;

        this.maxProgress = entry.value().getCookingTime() / 2;

        if (this.progress >= this.maxProgress) {
            this.progress = 0;
            this.maxProgress = 0;

            ItemStack output = entry.value().craft(recipeInput, this.world.getRegistryManager());
            this.wrappedInventoryStorage.getInventory(1).addStack(output);
            inputStack.decrement(1);
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

        if (this.world == null || this.world.isClient)
            return;

        boolean running = this.progress > 0;
        if (getCachedState().get(Properties.LIT) != running)
            this.world.setBlockState(this.pos, getCachedState().with(Properties.LIT, running), Block.NOTIFY_ALL);
    }

    private boolean canAcceptOutput(RecipeEntry<SmeltingRecipe> recipeEntry, SingleStackRecipeInput recipeInput) {
        ItemStack output = recipeEntry.value().craft(recipeInput, this.world.getRegistryManager());
        if (output.isEmpty())
            return false;

        SimpleInventory outputInventory = this.wrappedInventoryStorage.getInventory(1);
        ItemStack outputStack = outputInventory.getStack(0);
        if (outputStack.isEmpty())
            return true;

        if (!ItemStack.areItemsAndComponentsEqual(outputStack, output))
            return false;

        return (outputStack.getCount() + output.getCount() <= output.getMaxCount()) && (outputStack.getCount() + output.getCount() <= outputInventory.getMaxCountPerStack());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("Progress"))
            this.progress = nbt.getShort("Progress", (short) 0);

        if (nbt.contains("MaxProgress"))
            this.maxProgress = nbt.getShort("MaxProgress", (short) 0);

        if (nbt.contains("Inventory"))
            this.wrappedInventoryStorage.readNbt(nbt.getListOrEmpty("Inventory"), registries);

        if (nbt.contains("Energy"))
            this.wrappedEnergyStorage.readNbt(nbt.getListOrEmpty("Energy"), registries);

        RecipeExperienceBlockEntity.readRecipesUsed(nbt, "RecipesUsed", this.recipesUsed);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        nbt.putShort("Progress", (short) this.progress);
        nbt.putShort("MaxProgress", (short) this.maxProgress);
        nbt.put("Inventory", this.wrappedInventoryStorage.writeNbt(registries));
        nbt.put("Energy", this.wrappedEnergyStorage.writeNbt(registries));

        RecipeExperienceBlockEntity.writeRecipesUsed(this.recipesUsed, nbt, "RecipesUsed");
    }

    @Override
    public void setLastRecipe(@Nullable RecipeEntry<?> recipe) {
        if (recipe == null)
            return;

        this.recipesUsed.addTo(recipe.id(), 1);
    }

    @Override
    public void dropExperienceForRecipesUsed(ServerPlayerEntity player) {
        List<RecipeEntry<?>> list = getRecipesUsedAndDropExperience(player.getServerWorld(), player.getPos());
        player.unlockRecipes(list);

        for (RecipeEntry<?> recipeEntry : list) {
            if (recipeEntry != null) {
                player.onRecipeCrafted(recipeEntry, this.wrappedInventoryStorage.getStacks());
            }
        }

        this.recipesUsed.clear();
    }

    @Override
    public List<RecipeEntry<?>> getRecipesUsedAndDropExperience(ServerWorld world, Vec3d pos) {
        List<RecipeEntry<?>> list = new ArrayList<>();

        for (Reference2IntMap.Entry<RegistryKey<Recipe<?>>> entry : this.recipesUsed.reference2IntEntrySet()) {
            world.getRecipeManager().get(entry.getKey()).ifPresent(recipe -> {
                list.add(recipe);
                RecipeExperienceBlockEntity.dropExperience(
                        world,
                        pos,
                        entry.getIntValue(),
                        ((AbstractCookingRecipe) recipe.value()).getExperience());
            });
        }

        return list;
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ElectricFurnaceScreenHandler(syncId, playerInventory, this, this.wrappedInventoryStorage);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingSimpleInventory input = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(0);
        OutputSimpleInventory output = (OutputSimpleInventory) this.wrappedInventoryStorage.getInventory(1);
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
    public WrappedInventoryStorage<SimpleInventory> getWrappedInventoryStorage() {
        return this.wrappedInventoryStorage;
    }

    public InventoryStorage getInventoryProvider(Direction direction) {
        Direction facing = getCachedState().get(Properties.FACING);
        Direction relative = MathUtils.getRelativeDirection(direction, facing);
        return this.wrappedInventoryStorage.getStorage(relative);
    }

    public EnergyStorage getEnergyProvider(Direction direction) {
        Direction facing = getCachedState().get(Properties.FACING);
        Direction relative = MathUtils.getRelativeDirection(direction, facing);
        return this.wrappedEnergyStorage.getStorage(relative);
    }

    @Override
    public Block getBlock() {
        return getCachedState().getBlock();
    }

    @Override
    public void dropContents(World world, BlockPos pos) {
        BlockEntityContentsDropper.super.dropContents(world, pos);
        if(world instanceof ServerWorld serverWorld) {
            getRecipesUsedAndDropExperience(serverWorld, Vec3d.ofCenter(pos));
        }
    }
}
