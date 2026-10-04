package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.recipe.CastingBasinRecipe;
import dev.turtywurty.industria.recipe.input.CastingBasinRecipeInput;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ContainerStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CastingBasinBlockEntity extends IndustriaBlockEntity implements BlockEntityContentsDropper, SyncableTickableBlockEntity, TappingOutletBlockEntity.TappingOutletReceiver {
    private final WrappedFluidStorage<SyncingFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedContainerStorage<SyncingSimpleInventory> wrappedContainerStorage = new WrappedContainerStorage<>();

    private int coolingProgress;
    private int coolingDuration;
    private BlockState castingBlockState = Blocks.AIR.defaultBlockState();
    private ResourceKey<Recipe<?>> currentRecipeId;

    public CastingBasinBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CASTING_BASIN.get(), ModBlockEntityTypes.CASTING_BASIN.get(), pos, state);
        this.wrappedFluidStorage.addStorage(new SyncingFluidStorage(this, FluidAmounts.BLOCK));
        this.wrappedContainerStorage.addExtractOnlyInventory(new SyncingSimpleInventory(this, 1) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        }, null);
    }

    public BlockState getCastingBlockState() {
        return castingBlockState;
    }

    public float getCoolingFraction() {
        if (coolingDuration <= 0)
            return 0f;

        return Math.clamp((float) this.coolingProgress / this.coolingDuration, 0f, 1f);
    }

    public WrappedFluidStorage<SyncingFluidStorage> getWrappedFluidStorage() {
        return wrappedFluidStorage;
    }

    @Override
    public SyncingFluidStorage getFluidStorage() {
        return wrappedFluidStorage.getStorage(0);
    }

    @Override
    public WrappedContainerStorage<SyncingSimpleInventory> getWrappedContainerStorage() {
        return wrappedContainerStorage;
    }

    @Override
    public Block getBlock() {
        return ModBlocks.CASTING_BASIN.get();
    }

    @Override
    public SimpleContainer getInventory() {
        return wrappedContainerStorage.getInventory(0);
    }

    @Override
    public boolean isAcceptingFluid() {
        return getInventory().isEmpty();
    }

    public ResourceStorage<ResourceVariant<Fluid>> getFluidStorageProvider(Direction direction) {
        return wrappedFluidStorage.getStorage(direction);
    }

    public ResourceStorage<ResourceVariant<Item>> getInventoryProvider(Direction direction) {
        return wrappedContainerStorage.getStorage(direction);
    }

    public void collectOutput(Player player) {
        if(this.level == null || this.level.isClientSide())
            return;

        ItemStack outputStack = getInventory().getItem(0);
        if(outputStack.isEmpty())
            return;

        ItemStack removed = getInventory().removeItem(0, outputStack.getCount());
        if(!removed.isEmpty()) {
            player.getInventory().placeItemBackInInventory(removed);
            setChanged();
        }
    }

    private Optional<RecipeHolder<CastingBasinRecipe>> getMatchingRecipe() {
        if (!(this.level instanceof ServerLevel serverLevel))
            return Optional.empty();

        SyncingFluidStorage storage = getFluidStorage();
        var fluidStack = new FluidStack(storage.resource(0), storage.amount(0));
        var input = new CastingBasinRecipeInput(fluidStack);

        return serverLevel.recipeAccess().getRecipeFor(ModRecipeTypes.CASTING_BASIN.get(), input, serverLevel);
    }

    private void resetCoolingProgress() {
        if (this.coolingProgress == 0
                && this.currentRecipeId == null
                && this.coolingDuration == 0
                && this.castingBlockState.isAir())
            return;

        this.coolingProgress = 0;
        this.coolingDuration = 0;
        this.castingBlockState = Blocks.AIR.defaultBlockState();
        this.currentRecipeId = null;
        update();
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        if (!getInventory().isEmpty()) {
            resetCoolingProgress();
            return;
        }

        Optional<RecipeHolder<CastingBasinRecipe>> matchingRecipe = getMatchingRecipe();
        if (matchingRecipe.isEmpty()) {
            resetCoolingProgress();
            return;
        }

        RecipeHolder<CastingBasinRecipe> recipeHolder = matchingRecipe.get();
        CastingBasinRecipe recipe = recipeHolder.value();
        int count = recipe.output().count().maxInclusive();
        if (recipe.inputFluid().isEmpty()
                || recipe.output().item() == Items.AIR
                || count <= 0
                || recipe.coolingTime() <= 0
                || (count > new ItemStack(recipe.output().item()).getMaxStackSize() || count > getInventory().getMaxStackSize())) {
            resetCoolingProgress();
            return;
        }

        BlockState outputState = recipe.output().item() instanceof BlockItem blockItem
                ? blockItem.getBlock().defaultBlockState()
                : Blocks.AIR.defaultBlockState();
        if(this.coolingDuration != recipe.coolingTime() || !Objects.equals(this.castingBlockState, outputState)) {
            this.coolingDuration = recipe.coolingTime();
            this.castingBlockState = outputState;
            update();
        }

        if (this.currentRecipeId == null || !this.currentRecipeId.equals(recipeHolder.id())) {
            this.currentRecipeId = recipeHolder.id();
            this.coolingProgress = 0;
            update();
        }

        if (this.coolingProgress > recipe.coolingTime()) {
            this.coolingProgress = recipe.coolingTime();
            update();
        }

        if (this.coolingProgress < recipe.coolingTime()) {
            this.coolingProgress++;
            update();
        }

        if(this.coolingProgress >= recipe.coolingTime()) {
            if (completeCasting(recipe)) {
                resetCoolingProgress();
            }
        }
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of(
                getFluidStorage(),
                Objects.requireNonNull(this.wrappedContainerStorage.getInventory(0))
        );
    }

    private boolean completeCasting(CastingBasinRecipe recipe) {
        if(!getInventory().isEmpty()
                || recipe.inputFluid().isEmpty()
                || !recipe.inputFluid().testForRecipe(getFluidStorage()))
            return false;

        var outputStorage = ContainerStorage.of(getInventory(), null);

        try(TransferTransaction transaction = TransferTransaction.openRoot()) {
            long extracted = getFluidStorage().extract(
                    recipe.inputFluid().variant(),
                    recipe.inputFluid().amount(),
                    transaction
            );
            if(extracted != recipe.inputFluid().amount())
                return false;

            ItemStack output = recipe.output().createStack(this.level.getRandom());
            if (!output.isEmpty()) {
                long inserted = outputStorage.insert(
                        0,
                        ResourceVariant.ofItem(output),
                        output.getCount(),
                        transaction
                );
                if (inserted != output.getCount())
                    return false;
            }

            transaction.commit();
            return true;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ViewUtils.putChild(output, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.putChild(output, "Inventory", this.wrappedContainerStorage);
        output.putInt("CoolingProgress", this.coolingProgress);
        output.putInt("CoolingDuration", this.coolingDuration);
        output.store("CastingBlockState", BlockState.CODEC, this.castingBlockState);
        output.storeNullable("CurrentRecipe", ResourceKey.codec(Registries.RECIPE), this.currentRecipeId);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ViewUtils.readChild(input, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.readChild(input, "Inventory", this.wrappedContainerStorage);
        this.coolingProgress = Math.max(0, input.getIntOr("CoolingProgress", 0));
        this.coolingDuration = Math.max(0, input.getIntOr("CoolingDuration", 0));
        this.castingBlockState = input.read("CastingBlockState", BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState());
        this.currentRecipeId = input.read("CurrentRecipe", ResourceKey.codec(Registries.RECIPE)).orElse(null);
    }
}
