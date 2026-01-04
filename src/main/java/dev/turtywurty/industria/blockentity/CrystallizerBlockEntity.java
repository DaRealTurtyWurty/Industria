package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.fluid.InputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.multiblock.LocalDirection;
import dev.turtywurty.industria.multiblock.PortType;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockable;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import dev.turtywurty.industria.multiblock.old.PositionedPortRule;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.CrystallizerRecipe;
import dev.turtywurty.industria.recipe.input.CrystallizerRecipeInput;
import dev.turtywurty.industria.screenhandler.CrystallizerScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// input:
// water (fluid)
// sodium aluminate (fluid)
// aluminium hydroxide (gibbsite) - catalyst (optional item)
//
// output:
// aluminium hydroxide (gibbsite) (item)
// sodium carbonate (item) - by-product
public class CrystallizerBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper, AutoMultiblockable {
    public static final Component TITLE = Industria.containerTitle("crystallizer");

    private static final List<PositionedPortRule> PORT_RULES = List.of(
            PositionedPortRule.when(p -> p.y() == 3 && p.isCenterColumn())
                    .on(LocalDirection.UP)
                    .types(PortType.input(TransferType.FLUID))
                    .build(),

            PositionedPortRule.when(p -> p.y() == 3 && p.z() == 1)
                    .on(LocalDirection.BACK)
                    .types(PortType.input(TransferType.FLUID))
                    .build(),

            PositionedPortRule.when(p -> p.y() == 0 && p.x() == -1)
                    .on(LocalDirection.LEFT)
                    .types(PortType.input(TransferType.ITEM))
                    .build(),

            PositionedPortRule.when(p -> p.y() == 0 && p.z() == -1)
                    .on(LocalDirection.FRONT)
                    .types(PortType.input(TransferType.ITEM))
                    .build(),

            PositionedPortRule.when(p -> p.y() == 0 && p.x() == 1)
                    .on(LocalDirection.RIGHT)
                    .types(PortType.input(TransferType.ITEM))
                    .build()
    );

    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();

    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    private int progress;
    private int maxProgress;
    private ResourceKey<Recipe<?>> currentRecipeId;
    private ItemStack outputItemStack = ItemStack.EMPTY;
    private ItemStack byproductItemStack = ItemStack.EMPTY;
    private int catalystUsesLeft;
    private int maxCatalystUses;
    private final ContainerData properties = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> CrystallizerBlockEntity.this.progress;
                case 1 -> CrystallizerBlockEntity.this.maxProgress;
                case 2 -> CrystallizerBlockEntity.this.catalystUsesLeft;
                case 3 -> CrystallizerBlockEntity.this.maxCatalystUses;
                default -> throw new IllegalArgumentException("Invalid index: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> CrystallizerBlockEntity.this.progress = value;
                case 1 -> CrystallizerBlockEntity.this.maxProgress = value;
                case 2 -> CrystallizerBlockEntity.this.catalystUsesLeft = value;
                case 3 -> CrystallizerBlockEntity.this.maxCatalystUses = value;
                default -> throw new IllegalArgumentException("Invalid index: " + index);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };
    private ItemStack nextOutputItemStack = ItemStack.EMPTY; // Used for rendering

    public CrystallizerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.CRYSTALLIZER, BlockEntityTypeInit.CRYSTALLIZER, pos, state);

        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidConstants.BUCKET * 5, $ -> !isRunning()), Direction.SOUTH);
        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidConstants.BUCKET, $ -> !isRunning()), Direction.UP);
        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 1), Direction.WEST);
        this.wrappedContainerStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.NORTH);
        this.wrappedContainerStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.EAST);
    }

    public boolean isRunning() {
        return this.currentRecipeId != null;
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        InputFluidStorage waterFluidStorage = getWaterFluidStorage();
        InputFluidStorage crystalFluidStorage = getCrystalFluidStorage();
        SyncingSimpleInventory catalystInventory = getCatalystInventory();
        OutputSimpleInventory outputInventory = getOutputInventory();
        OutputSimpleInventory byproductInventory = getByproductInventory();

        return List.of(waterFluidStorage, crystalFluidStorage, catalystInventory, outputInventory, byproductInventory);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        if (!this.outputItemStack.isEmpty()) {
            SyncingSimpleInventory outputInventory = getOutputInventory();
            if (outputInventory.canAddItem(this.outputItemStack)) {
                this.outputItemStack = outputInventory.addItem(this.outputItemStack);
                update();
            }

            return;
        }

        if (!this.byproductItemStack.isEmpty()) {
            SyncingSimpleInventory byproductInventory = getByproductInventory();
            if (byproductInventory.canAddItem(this.byproductItemStack)) {
                this.byproductItemStack = byproductInventory.addItem(this.byproductItemStack);
                update();
            }

            return;
        }

        InputFluidStorage waterFluidStorage = getWaterFluidStorage();
        InputFluidStorage crystalFluidStorage = getCrystalFluidStorage();
        SyncingSimpleInventory catalystInventory = getCatalystInventory();

        var waterFluidStack = new FluidStack(waterFluidStorage.variant, waterFluidStorage.amount);
        var crystalFluidStack = new FluidStack(crystalFluidStorage.variant, crystalFluidStorage.amount);
        ItemStack catalystItemStack = catalystInventory.getItem(0);
        var recipeInput = new CrystallizerRecipeInput(waterFluidStack, crystalFluidStack, catalystItemStack);
        if (this.currentRecipeId == null) {
            Optional<RecipeHolder<CrystallizerRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
            if (recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().processTime();
                this.progress = 0;

                update();
            }

            if (!this.nextOutputItemStack.isEmpty()) {
                this.nextOutputItemStack = ItemStack.EMPTY;
                update();
            }

            return;
        }

        CrystallizerRecipe recipe = getRecipeById(this.currentRecipeId);
        if (recipe == null) {
            this.currentRecipeId = null;
            this.maxProgress = 0;
            this.progress = 0;
            this.nextOutputItemStack = ItemStack.EMPTY;

            update();
            return;
        }

        if (this.catalystUsesLeft <= 0) {
            if (recipe.catalyst().testForRecipe(catalystItemStack)) {
                this.catalystUsesLeft = recipe.catalystUses();
                this.maxCatalystUses = recipe.catalystUses();
                update();
            } else if (recipe.requiresCatalyst()) {
                this.currentRecipeId = null;
                this.maxProgress = 0;
                this.progress = 0;
                update();
                return;
            }
        }

        if (this.progress >= this.maxProgress) {
            ItemStack output = recipe.createOutput(this.level.getRandom());
            ItemStack byproduct = recipe.createByProduct(this.level.getRandom());

            if (catalystItemStack.isEmpty()) {
                this.catalystUsesLeft = 0;
                this.maxCatalystUses = 0;
            } else {
                this.catalystUsesLeft--;
                if (this.catalystUsesLeft <= 0) {
                    catalystInventory.removeItem(0, 1);
                }
            }

            this.outputItemStack = output;
            this.byproductItemStack = byproduct;
            this.progress = 0;
            this.maxProgress = 0;
            this.currentRecipeId = null;
            this.nextOutputItemStack = ItemStack.EMPTY;

            update();
        } else {
            int progressModifier = (this.catalystUsesLeft > 0 && recipe.catalyst().testForRecipe(catalystItemStack)) ? 4 : 1;
            this.progress += progressModifier;

            int modifiedMaxProgress = this.maxProgress / progressModifier;
            long waterToRemove = recipe.waterFluid().amount() / modifiedMaxProgress;
            long crystalToRemove = recipe.crystalFluid().amount() / modifiedMaxProgress;
            waterFluidStorage.amount = Math.max(0, waterFluidStorage.amount - waterToRemove);
            crystalFluidStorage.amount = Math.max(0, crystalFluidStorage.amount - crystalToRemove);

            this.nextOutputItemStack = recipe.createOutput(this.level.getRandom());

            update();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ViewUtils.putChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            view.store("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        if (!this.outputItemStack.isEmpty()) {
            view.store("OutputStack", ItemStack.CODEC, this.outputItemStack);
        }

        if (!this.byproductItemStack.isEmpty()) {
            view.store("ByproductStack", ItemStack.CODEC, this.byproductItemStack);
        }

        view.putInt("CatalystUsesLeft", this.catalystUsesLeft);
        view.putInt("MaxCatalystUses", this.maxCatalystUses);

        if (!this.nextOutputItemStack.isEmpty()) {
            view.store("NextOutputStack", ItemStack.CODEC, this.nextOutputItemStack);
        }
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);

        this.progress = view.getIntOr("Progress", 0);
        this.maxProgress = view.getIntOr("MaxProgress", 0);

        this.currentRecipeId = view.read("CurrentRecipe", RECIPE_CODEC)
                .orElse(null);

        this.outputItemStack = view.read("OutputStack", ItemStack.CODEC)
                .orElse(ItemStack.EMPTY);

        this.byproductItemStack = view.read("ByproductItemStack", ItemStack.CODEC)
                .orElse(ItemStack.EMPTY);

        this.catalystUsesLeft = view.getIntOr("CatalystUsesLeft", 0);
        this.maxCatalystUses = view.getIntOr("MaxCatalystUses", 0);

        this.nextOutputItemStack = view.read("NextOutputStack", ItemStack.CODEC)
                .orElse(ItemStack.EMPTY);
    }

    private Optional<RecipeHolder<CrystallizerRecipe>> getCurrentRecipe(CrystallizerRecipeInput recipeInput) {
        if (this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return Optional.empty();

        return serverWorld.recipeAccess().getRecipeFor(RecipeTypeInit.CRYSTALLIZER, recipeInput, this.level);
    }

    private @Nullable CrystallizerRecipe getRecipeById(ResourceKey<Recipe<?>> recipeId) {
        if (this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return null;

        return (CrystallizerRecipe) serverWorld.recipeAccess()
                .byKey(recipeId)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    @Override
    public List<PositionedPortRule> getPortRules() {
        return PORT_RULES;
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.CRYSTALLIZER;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        // 3x3x4 (3 wide, 3 long, 4 high)
        if (this.level == null)
            return List.of();

        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y < 4; y++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;

                    BlockPos pos = this.worldPosition.offset(x, y, z);
                    if (this.level.getBlockState(pos).canBeReplaced()) {
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
    public BlockPosPayload getScreenOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new CrystallizerScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage, this.properties);
    }

    public SingleFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public ContainerStorage getInventoryProvider(Direction side) {
        return this.wrappedContainerStorage.getStorage(side);
    }

    public InputFluidStorage getWaterFluidStorage() {
        return (InputFluidStorage) getFluidProvider(Direction.SOUTH);
    }

    public InputFluidStorage getCrystalFluidStorage() {
        return (InputFluidStorage) getFluidProvider(Direction.UP);
    }

    public SyncingSimpleInventory getCatalystInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(0);
    }

    public OutputSimpleInventory getOutputInventory() {
        return (OutputSimpleInventory) this.wrappedContainerStorage.getInventory(1);
    }

    public OutputSimpleInventory getByproductInventory() {
        return (OutputSimpleInventory) this.wrappedContainerStorage.getInventory(2);
    }

    public ItemStack getNextOutputItemStack() {
        return this.nextOutputItemStack;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }
}