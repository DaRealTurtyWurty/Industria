package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.fluid.InputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.component.MoldComponent;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModDataComponentTypes;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.item.MoldItem;
import dev.turtywurty.industria.menu.CastingTableMenu;
import dev.turtywurty.industria.molding.MoldPattern;
import dev.turtywurty.industria.molding.MoldPatterns;
import dev.turtywurty.industria.molding.MoldPiece;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.CastingRecipe;
import dev.turtywurty.industria.recipe.input.CastingRecipeInput;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ContainerStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CastingTableBlockEntity extends IndustriaBlockEntity implements BlockEntityContentsDropper, BlockEntityWithGui<BlockPosPayload>, SyncableTickableBlockEntity, TappingOutletBlockEntity.TappingOutletReceiver {
    public static final Component TITLE = Industria.containerTitle("casting_table");

    private final WrappedContainerStorage<SyncingSimpleInventory> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedFluidStorage<SyncingFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();

    private int coolingProgress;
    private int coolingDuration;
    private ItemStack castingItemStack = ItemStack.EMPTY;
    private ResourceKey<Recipe<?>> currentRecipeId;

    public CastingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CASTING_TABLE.get(), ModBlockEntityTypes.CASTING_TABLE.get(), pos, state);
        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 2) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        }, Direction.NORTH);
        this.wrappedContainerStorage.addExtractOnlyInventory(new SyncingSimpleInventory(this, 1) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        }, Direction.DOWN);
        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidAmounts.BUCKET), Direction.UP);
    }

    public ItemStack getCastingItemStack() {
        return this.castingItemStack;
    }

    public float getCoolingFraction() {
        if (coolingDuration <= 0)
            return 0f;

        return Math.clamp((float) this.coolingProgress / this.coolingDuration, 0f, 1f);
    }

    @Override
    public boolean isAcceptingFluid() {
        return getAssembledMold().isPresent() && getOutputInventory().isEmpty();
    }

    public void collectOutput(Player player) {
        if (this.level == null || this.level.isClientSide())
            return;

        ItemStack outputStack = getOutputInventory().getItem(0);
        if (outputStack.isEmpty())
            return;

        ItemStack removed = getOutputInventory().removeItem(0, outputStack.getCount());
        if (!removed.isEmpty()) {
            player.getInventory().placeItemBackInInventory(removed);
            setChanged();
        }
    }

    private Optional<RecipeHolder<CastingRecipe>> getMatchingRecipe() {
        if (!(this.level instanceof ServerLevel serverLevel))
            return Optional.empty();

        SyncingFluidStorage storage = getFluidStorage();
        var fluidStack = new FluidStack(storage.resource(0), storage.amount(0));
        var input = new CastingRecipeInput(
                CastingRecipe.BlockType.TABLE,
                fluidStack,
                getAssembledMold().map(MoldPattern::id)
        );

        return serverLevel.recipeAccess().getRecipeFor(ModRecipeTypes.CASTING.get(), input, serverLevel);
    }

    private void resetCoolingProgress() {
        if (this.coolingProgress == 0
                && this.currentRecipeId == null
                && this.coolingDuration == 0
                && this.castingItemStack.isEmpty())
            return;

        this.coolingProgress = 0;
        this.coolingDuration = 0;
        this.castingItemStack = ItemStack.EMPTY;
        this.currentRecipeId = null;
        update();
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        if (!getOutputInventory().isEmpty() || getAssembledMold().isEmpty()) {
            resetCoolingProgress();
            return;
        }

        Optional<RecipeHolder<CastingRecipe>> matchingRecipe = getMatchingRecipe();
        if (matchingRecipe.isEmpty()) {
            resetCoolingProgress();
            return;
        }

        RecipeHolder<CastingRecipe> recipeHolder = matchingRecipe.get();
        CastingRecipe recipe = recipeHolder.value();
        int count = recipe.output().count().maxInclusive();
        if (recipe.inputFluid().isEmpty()
                || recipe.output().item() == Items.AIR
                || count <= 0
                || recipe.coolingTime() <= 0
                || (count > new ItemStack(recipe.output().item()).getMaxStackSize() || count > getOutputInventory().getMaxStackSize())) {
            resetCoolingProgress();
            return;
        }

        ItemStack outputStack = recipe.output().createStack(this.level.getRandom());
        if (this.coolingDuration != recipe.coolingTime() || !Objects.equals(this.castingItemStack, outputStack)) {
            this.coolingDuration = recipe.coolingTime();
            this.castingItemStack = outputStack;
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

        if (this.coolingProgress >= recipe.coolingTime()) {
            if (completeCasting(recipe)) {
                resetCoolingProgress();
            }
        }
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of(
                getFluidStorage(),
                getInputInventory(),
                getOutputInventory()
        );
    }

    @Override
    public SimpleContainer getInventory() {
        return getOutputInventory();
    }

    public Optional<MoldPattern> getAssembledMold() {
        ItemStack first = getInputInventory().getItem(0);
        ItemStack second = getInputInventory().getItem(1);
        if (first.isEmpty()) {
            first = second;
            second = ItemStack.EMPTY;
        }

        MoldComponent firstData = first.get(ModDataComponentTypes.MOLD.get());
        if (!(first.getItem() instanceof MoldItem && firstData != null))
            return Optional.empty();

        Optional<MoldPattern> pattern = MoldPatterns.find(firstData.moldId());
        if (pattern.isEmpty())
            return Optional.empty();

        if (!pattern.get().split())
            return firstData.piece() == MoldPiece.WHOLE && second.isEmpty()
                    ? pattern
                    : Optional.empty();

        MoldComponent secondData = second.get(ModDataComponentTypes.MOLD.get());
        if (secondData == null
                || first.getItem() != second.getItem()
                || !firstData.moldId().equals(secondData.moldId())
                || firstData.piece() == secondData.piece()
                || firstData.piece() == MoldPiece.WHOLE
                || secondData.piece() == MoldPiece.WHOLE)
            return Optional.empty();

        return pattern;
    }

    private boolean completeCasting(CastingRecipe recipe) {
        if (!getInventory().isEmpty()
                || recipe.inputFluid().isEmpty()
                || !recipe.inputFluid().testForRecipe(getFluidStorage()))
            return false;

        var outputStorage = ContainerStorage.of(getInventory(), null);

        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            long extracted = getFluidStorage().extractInternal(
                    recipe.inputFluid().variant(),
                    recipe.inputFluid().amount(),
                    transaction
            );
            if (extracted != recipe.inputFluid().amount())
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
    public Block getBlock() {
        return ModBlocks.CASTING_TABLE.get();
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    public BlockPosPayload getMenuOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CastingTableMenu(containerId, inventory, this, this.wrappedContainerStorage);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ViewUtils.putChild(output, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.putChild(output, "Inventory", this.wrappedContainerStorage);
        output.putInt("CoolingProgress", this.coolingProgress);
        output.putInt("CoolingDuration", this.coolingDuration);
        output.store("CastingItemStack", ItemStack.OPTIONAL_CODEC, this.castingItemStack);
        output.storeNullable("CurrentRecipe", ResourceKey.codec(Registries.RECIPE), this.currentRecipeId);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ViewUtils.readChild(input, "FluidStorage", this.wrappedFluidStorage);
        ViewUtils.readChild(input, "Inventory", this.wrappedContainerStorage);
        this.coolingProgress = Math.max(0, input.getIntOr("CoolingProgress", 0));
        this.coolingDuration = Math.max(0, input.getIntOr("CoolingDuration", 0));
        this.castingItemStack = input.read("CastingItemStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.currentRecipeId = input.read("CurrentRecipe", ResourceKey.codec(Registries.RECIPE)).orElse(null);
    }

    public SyncingSimpleInventory getInputInventory() {
        return this.wrappedContainerStorage.getInventory(0);
    }

    public SyncingSimpleInventory getOutputInventory() {
        return this.wrappedContainerStorage.getInventory(1);
    }

    public ResourceStorage<ResourceVariant<Item>> getInventoryProvider(Direction direction) {
        return this.wrappedContainerStorage.getStorage(direction);
    }

    public SyncingFluidStorage getFluidStorage() {
        return this.wrappedFluidStorage.getStorage(Direction.UP);
    }

    public ResourceStorage<ResourceVariant<Fluid>> getFluidProvider(Direction direction) {
        return this.wrappedFluidStorage.getStorage(direction);
    }
}
