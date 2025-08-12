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
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.multiblock.MultiblockIOPort;
import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.CrystallizerRecipe;
import dev.turtywurty.industria.recipe.input.CrystallizerRecipeInput;
import dev.turtywurty.industria.screenhandler.CrystallizerScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// input:
// water (fluid)
// sodium aluminate (fluid)
// aluminium hydroxide (gibbsite) - catalyst (optional item)
//
// output:
// aluminium hydroxide (gibbsite) (item)
// sodium carbonate (item) - by-product
public class CrystallizerBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper, Multiblockable {
    public static final Text TITLE = Industria.containerTitle("crystallizer");

    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();

    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    private int progress;
    private int maxProgress;
    private RegistryKey<Recipe<?>> currentRecipeId;
    private ItemStack outputItemStack = ItemStack.EMPTY;
    private ItemStack byproductItemStack = ItemStack.EMPTY;
    private int catalystUsesLeft;
    private int maxCatalystUses;

    private ItemStack nextOutputItemStack = ItemStack.EMPTY; // Used for rendering

    private final PropertyDelegate properties = new PropertyDelegate() {
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
        public int size() {
            return 4;
        }
    };

    public CrystallizerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.CRYSTALLIZER, BlockEntityTypeInit.CRYSTALLIZER, pos, state);

        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidConstants.BUCKET * 5, $ -> !isRunning()), Direction.SOUTH);
        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidConstants.BUCKET, $ -> !isRunning()), Direction.UP);
        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 1), Direction.WEST);
        this.wrappedInventoryStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.NORTH);
        this.wrappedInventoryStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.EAST);
    }

    public boolean isRunning() {
        return this.currentRecipeId != null;
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
        InputFluidStorage waterFluidStorage = getWaterFluidStorage();
        InputFluidStorage crystalFluidStorage = getCrystalFluidStorage();
        SyncingSimpleInventory catalystInventory = getCatalystInventory();
        OutputSimpleInventory outputInventory = getOutputInventory();
        OutputSimpleInventory byproductInventory = getByproductInventory();

        return List.of(waterFluidStorage, crystalFluidStorage, catalystInventory, outputInventory, byproductInventory);
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        if (!this.outputItemStack.isEmpty()) {
            SyncingSimpleInventory outputInventory = getOutputInventory();
            if (outputInventory.canInsert(this.outputItemStack)) {
                this.outputItemStack = outputInventory.addStack(this.outputItemStack);
                update();
            }

            return;
        }

        if (!this.byproductItemStack.isEmpty()) {
            SyncingSimpleInventory byproductInventory = getByproductInventory();
            if (byproductInventory.canInsert(this.byproductItemStack)) {
                this.byproductItemStack = byproductInventory.addStack(this.byproductItemStack);
                update();
            }

            return;
        }

        InputFluidStorage waterFluidStorage = getWaterFluidStorage();
        InputFluidStorage crystalFluidStorage = getCrystalFluidStorage();
        SyncingSimpleInventory catalystInventory = getCatalystInventory();

        var waterFluidStack = new FluidStack(waterFluidStorage.variant, waterFluidStorage.amount);
        var crystalFluidStack = new FluidStack(crystalFluidStorage.variant, crystalFluidStorage.amount);
        ItemStack catalystItemStack = catalystInventory.getStack(0);
        var recipeInput = new CrystallizerRecipeInput(waterFluidStack, crystalFluidStack, catalystItemStack);
        if (this.currentRecipeId == null) {
            Optional<RecipeEntry<CrystallizerRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
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
            ItemStack output = recipe.createOutput(this.world.random);
            ItemStack byproduct = recipe.createByProduct(this.world.random);

            if (catalystItemStack.isEmpty()) {
                this.catalystUsesLeft = 0;
                this.maxCatalystUses = 0;
            } else {
                this.catalystUsesLeft--;
                if (this.catalystUsesLeft <= 0) {
                    catalystInventory.removeStack(0, 1);
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

            this.nextOutputItemStack = recipe.createOutput(this.world.random);

            update();
        }
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        ViewUtils.putChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.putChild(view, "Inventory", this.wrappedInventoryStorage);
        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            view.put("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        if (!this.outputItemStack.isEmpty()) {
            view.put("OutputStack", ItemStack.CODEC, this.outputItemStack);
        }

        if (!this.byproductItemStack.isEmpty()) {
            view.put("ByproductStack", ItemStack.CODEC, this.byproductItemStack);
        }

        view.putInt("CatalystUsesLeft", this.catalystUsesLeft);
        view.putInt("MaxCatalystUses", this.maxCatalystUses);

        if (!this.nextOutputItemStack.isEmpty()) {
            view.put("NextOutputStack", ItemStack.CODEC, this.nextOutputItemStack);
        }
    }

    @Override
    protected void readData(ReadView view) {
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "Inventory", this.wrappedInventoryStorage);

        this.progress = view.getInt("Progress", 0);
        this.maxProgress = view.getInt("MaxProgress", 0);

        this.currentRecipeId = view.read("CurrentRecipe", RegistryKey.createCodec(RegistryKeys.RECIPE))
                .orElse(null);

        this.outputItemStack = view.read("OutputStack", ItemStack.CODEC)
                .orElse(ItemStack.EMPTY);

        this.byproductItemStack = view.read("ByproductItemStack", ItemStack.CODEC)
                .orElse(ItemStack.EMPTY);

        this.catalystUsesLeft = view.getInt("CatalystUsesLeft", 0);
        this.maxCatalystUses = view.getInt("MaxCatalystUses", 0);

        this.nextOutputItemStack = view.read("NextOutputStack", ItemStack.CODEC)
                .orElse(ItemStack.EMPTY);
    }

    private Optional<RecipeEntry<CrystallizerRecipe>> getCurrentRecipe(CrystallizerRecipeInput recipeInput) {
        if (this.world == null || !(this.world instanceof ServerWorld serverWorld))
            return Optional.empty();

        return serverWorld.getRecipeManager().getFirstMatch(RecipeTypeInit.CRYSTALLIZER, recipeInput, this.world);
    }

    private @Nullable CrystallizerRecipe getRecipeById(RegistryKey<Recipe<?>> recipeId) {
        if (this.world == null || !(this.world instanceof ServerWorld serverWorld))
            return null;

        return (CrystallizerRecipe) serverWorld.getRecipeManager()
                .get(recipeId)
                .map(RecipeEntry::value)
                .orElse(null);
    }

    @Override
    public Map<Direction, MultiblockIOPort> getPorts(Vec3i offsetFromPrimary, Direction direction) {
        Map<Direction, List<TransferType<?, ?, ?>>> transferTypes = new EnumMap<>(Direction.class);

        // south - water input
        // up - crystal input
        if (offsetFromPrimary.getY() == 3 && Multiblockable.isCenterColumn(offsetFromPrimary) && direction == Direction.UP)
            transferTypes.computeIfAbsent(direction, d -> new ArrayList<>()).add(TransferType.FLUID);

        if (offsetFromPrimary.getY() == 3 && offsetFromPrimary.getZ() == 1 && direction == Direction.SOUTH)
            transferTypes.computeIfAbsent(direction, d -> new ArrayList<>()).add(TransferType.FLUID);

        // west - catalyst input
        // north - output
        // east - byproduct
        if (offsetFromPrimary.getY() == 0 && offsetFromPrimary.getX() == -1 && direction == Direction.WEST)
            transferTypes.computeIfAbsent(direction, d -> new ArrayList<>()).add(TransferType.ITEM);

        if (offsetFromPrimary.getY() == 0 && offsetFromPrimary.getZ() == -1 && direction == Direction.NORTH)
            transferTypes.computeIfAbsent(direction, d -> new ArrayList<>()).add(TransferType.ITEM);

        if (offsetFromPrimary.getY() == 0 && offsetFromPrimary.getX() == 1 && direction == Direction.EAST)
            transferTypes.computeIfAbsent(direction, d -> new ArrayList<>()).add(TransferType.ITEM);

        return Multiblockable.toIOPortMap(transferTypes);
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.CRYSTALLIZER;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        // 3x3x4 (3 wide, 3 long, 4 high)
        if (this.world == null)
            return List.of();

        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y < 4; y++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;

                    BlockPos pos = this.pos.add(x, y, z);
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
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new CrystallizerScreenHandler(syncId, playerInventory, this, this.wrappedInventoryStorage, this.properties);
    }

    public SingleFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public InventoryStorage getInventoryProvider(Direction side) {
        return this.wrappedInventoryStorage.getStorage(side);
    }

    public InputFluidStorage getWaterFluidStorage() {
        return (InputFluidStorage) getFluidProvider(Direction.SOUTH);
    }

    public InputFluidStorage getCrystalFluidStorage() {
        return (InputFluidStorage) getFluidProvider(Direction.UP);
    }

    public SyncingSimpleInventory getCatalystInventory() {
        return (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(0);
    }

    public OutputSimpleInventory getOutputInventory() {
        return (OutputSimpleInventory) this.wrappedInventoryStorage.getInventory(1);
    }

    public OutputSimpleInventory getByproductInventory() {
        return (OutputSimpleInventory) this.wrappedInventoryStorage.getInventory(2);
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
