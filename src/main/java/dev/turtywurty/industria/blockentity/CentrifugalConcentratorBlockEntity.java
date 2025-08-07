package dev.turtywurty.industria.blockentity;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SingleSlurryStorage;
import dev.turtywurty.fabricslurryapi.api.storage.SlurryStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.fluid.InputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.OutputSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.blockentity.util.slurry.SyncingSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.WrappedSlurryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.multiblock.MultiblockIOPort;
import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.CentrifugalConcentratorRecipe;
import dev.turtywurty.industria.recipe.input.CentrifugalConcentratorRecipeInput;
import dev.turtywurty.industria.screenhandler.CentrifugalConcentratorScreenHandler;
import dev.turtywurty.industria.util.TransferUtils;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.*;

public class CentrifugalConcentratorBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, Multiblockable, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("centrifugal_concentrator");

    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedSlurryStorage<SingleSlurryStorage> wrappedSlurryStorage = new WrappedSlurryStorage<>();

    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    private int progress, maxProgress;

    private RegistryKey<Recipe<?>> currentRecipeId;
    private int recipeRPM;
    private ItemStack outputItemStack = ItemStack.EMPTY;
    private SlurryStack outputSlurryStack = SlurryStack.EMPTY;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> recipeRPM;
                default -> throw new IndexOutOfBoundsException("Invalid index: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
            }
        }

        @Override
        public int size() {
            return 3;
        }
    };

    public CentrifugalConcentratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.CENTRIFUGAL_CONCENTRATOR, BlockEntityTypeInit.CENTRIFUGAL_CONCENTRATOR, pos, state);

        this.wrappedInventoryStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.UP);
        this.wrappedInventoryStorage.addExtractOnlyInventory(new OutputSimpleInventory(this, 1), Direction.DOWN);
        this.wrappedInventoryStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createFluidPredicate(() -> {
                    SyncingFluidStorage inputFluidTank = getInputFluidTank();
                    return new FluidStack(inputFluidTank.variant, inputFluidTank.amount);
                })), Direction.NORTH);
        this.wrappedInventoryStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createEmptySlurryPredicate(() -> getOutputSlurryTank().variant)), Direction.SOUTH);

        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidConstants.BUCKET * 10, variant -> variant.isOf(Fluids.WATER)), Direction.UP);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 10_000_000, 1_000_000, 0));
        this.wrappedSlurryStorage.addStorage(new OutputSlurryStorage(this, FluidConstants.BUCKET * 10), Direction.DOWN);
    }

    @Override
    public Block getBlock() {
        return BlockInit.CENTRIFUGAL_CONCENTRATOR;
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncableStorage inventoryStorage = getInputInventory();
        SyncableStorage outputInventoryStorage = getOutputInventory();
        SyncableStorage bucketInputInventory = getBucketInputInventory();
        SyncableStorage bucketOutputInventory = getBucketOutputInventory();
        SyncableStorage inputFluidTank = getInputFluidTank();
        SyncableStorage outputSlurryTank = getOutputSlurryTank();
        SyncableStorage energyStorage = getEnergyStorage();
        return List.of(inventoryStorage, outputInventoryStorage, bucketInputInventory, bucketOutputInventory,
                inputFluidTank, outputSlurryTank,
                energyStorage);
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        SyncingSimpleInventory bucketInputInventory = getBucketInputInventory();
        if (!bucketInputInventory.isEmpty()) {
            ItemStack bucket = bucketInputInventory.getStack(0);
            Storage<FluidVariant> storage = FluidStorage.ITEM.find(bucket, ContainerItemContext.withConstant(bucket));
            if (storage != null && storage.supportsExtraction()) {
                SyncingFluidStorage inputFluidTank = getInputFluidTank();
                Optional<FluidVariant> optVariant = TransferUtils.findFirstVariant(storage, inputFluidTank.variant);
                optVariant.filter(TransferVariant::isBlank).ifPresent(variant -> {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long extracted = storage.extract(variant, FluidConstants.BUCKET, transaction);
                        if (extracted > 0) {
                            inputFluidTank.variant = variant;
                            inputFluidTank.amount += extracted;
                        }

                        transaction.commit();
                    }
                });
            }
        }

        SyncingSimpleInventory bucketOutputInventory = getBucketOutputInventory();
        if (!bucketOutputInventory.isEmpty()) {
            ItemStack bucket = bucketOutputInventory.getStack(0);
            Storage<SlurryVariant> storage = SlurryStorage.ITEM.find(bucket, ContainerItemContext.withConstant(bucket));
            if (storage != null && storage.supportsInsertion()) {
                SyncingSlurryStorage outputSlurryTank = getOutputSlurryTank();
                if (outputSlurryTank.amount > 0) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long inserted = storage.insert(outputSlurryTank.variant, FluidConstants.BUCKET, transaction);
                        if (inserted > 0) {
                            outputSlurryTank.amount -= inserted;
                        }

                        transaction.commit();
                    }
                }
            }
        }

        if (!this.outputItemStack.isEmpty()) {
            SyncingSimpleInventory outputInventory = getOutputInventory();
            if (outputInventory.canInsert(this.outputItemStack)) {
                this.outputItemStack = outputInventory.addStack(this.outputItemStack);
                update();
            }

            return;
        }

        if (!this.outputSlurryStack.isEmpty()) {
//            SyncingSlurryStorage outputSlurryTank = getOutputSlurryTank();
//            if (Objects.equals(outputSlurryTank.variant, this.outputSlurryStack.variant()) && outputSlurryTank.getCapacity() - outputSlurryTank.amount >= 0) {
//                long inserted = Math.min(outputSlurryTank.getCapacity() - outputSlurryTank.amount, this.outputSlurryStack.amount());
//                outputSlurryTank.variant = this.outputSlurryStack.variant();
//                outputSlurryTank.amount += inserted;
//                this.outputSlurryStack = this.outputSlurryStack.withAmount(this.outputSlurryStack.amount() - inserted);
//                update();
//            }
//
//            return;
            this.outputSlurryStack = this.outputSlurryStack.withAmount(0); // TODO: Remove after slurry pipes are fixed
        }

        CentrifugalConcentratorRecipeInput recipeInput = createRecipeInput();
        if (this.currentRecipeId == null) {
            Optional<RecipeEntry<CentrifugalConcentratorRecipe>> recipeEntryOpt = getCurrentRecipe(recipeInput);
            if (recipeEntryOpt.isPresent()) {
                RecipeEntry<CentrifugalConcentratorRecipe> recipeEntry = recipeEntryOpt.get();
                this.currentRecipeId = recipeEntry.id();

                CentrifugalConcentratorRecipe recipe = recipeEntry.value();
                this.recipeRPM = recipe.rpm();
                this.maxProgress = recipe.processTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeEntry<CentrifugalConcentratorRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            this.currentRecipeId = null;
            this.recipeRPM = 0;
            this.maxProgress = 0;
            this.progress = 0;
            update();
            return;
        }

        CentrifugalConcentratorRecipe recipe = recipeEntry.get().value();
        this.recipeRPM = recipe.rpm();
        if (this.progress >= this.maxProgress) {
            if (hasEnergy(recipe)) {
                extractEnergy(recipe);
                getInputInventory().getStackInSlot(0).decrement(recipe.input().stackData().count());

                ItemStack output = recipe.craft(recipeInput, this.world.getRegistryManager());
                SyncingFluidStorage inputFluidTank = getInputFluidTank();
                inputFluidTank.amount -= FluidConstants.BUCKET * 2;

                SyncingSimpleInventory outputInventory = getOutputInventory();
                SyncingSlurryStorage outputSlurryTank = getOutputSlurryTank();

                this.progress = 0;
                this.maxProgress = 0;
                this.currentRecipeId = null;

                if (outputInventory.canInsert(output)) {
                    this.outputItemStack = outputInventory.addStack(output);
                } else {
                    this.outputItemStack = output;
                }

                SlurryStack outputSlurry = recipe.outputSlurry();
                if (outputSlurryTank.canInsert(outputSlurry)) {
                    long inserted = Math.min(outputSlurryTank.getCapacity() - outputSlurryTank.amount, outputSlurry.amount());
                    outputSlurryTank.amount += inserted;
                    outputSlurryTank.variant = outputSlurry.variant();
                    outputSlurry = outputSlurry.withAmount(outputSlurry.amount() - inserted);
                }

                this.outputSlurryStack = outputSlurry;

                update();
            }
        } else {
            if (hasEnergy(recipe)) {
                this.progress++;
                extractEnergy(recipe);
                update();
            }
        }
    }

    private Optional<RecipeEntry<CentrifugalConcentratorRecipe>> getCurrentRecipe(CentrifugalConcentratorRecipeInput recipeInput) {
        if (this.world == null || !(this.world instanceof ServerWorld serverWorld))
            return Optional.empty();

        return serverWorld.getRecipeManager().getFirstMatch(RecipeTypeInit.CENTRIFUGAL_CONCENTRATOR, recipeInput, this.world);
    }

    private CentrifugalConcentratorRecipeInput createRecipeInput() {
        return new CentrifugalConcentratorRecipeInput(getInputInventory(), getInputFluidTank().amount);
    }

    private boolean hasEnergy(CentrifugalConcentratorRecipe recipe) {
        return getEnergyStorage().amount >= recipe.rpm() * 60L;
    }

    private void extractEnergy(CentrifugalConcentratorRecipe recipe) {
        getEnergyStorage().amount -= recipe.rpm() * 60L;
    }

    @Override
    public WrappedInventoryStorage<?> getWrappedInventoryStorage() {
        return wrappedInventoryStorage;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        nbt.putInt("Progress", this.progress);
        nbt.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            Optional<NbtElement> result = RegistryKey.createCodec(RegistryKeys.RECIPE)
                    .encodeStart(NbtOps.INSTANCE, this.currentRecipeId)
                    .result();
            result.ifPresent(nbtElement -> nbt.put("CurrentRecipe", nbtElement));
        }

        nbt.putInt("RecipeRPM", this.recipeRPM);

        nbt.put("Inventory", this.wrappedInventoryStorage.writeNbt(registries));
        nbt.put("FluidTank", this.wrappedFluidStorage.writeNbt(registries));
        nbt.put("SlurryTank", this.wrappedSlurryStorage.writeNbt(registries));
        nbt.put("Energy", this.wrappedEnergyStorage.writeNbt(registries));

        if (!this.outputItemStack.isEmpty()) {
            nbt.put("OutputStack", this.outputItemStack.toNbt(registries));
        }

        if (!this.outputSlurryStack.isEmpty()) {
            nbt.put("OutputSlurry", SlurryStack.CODEC.codec()
                    .encodeStart(NbtOps.INSTANCE, this.outputSlurryStack)
                    .getOrThrow());
        }

        nbt.put("MachinePositions", Multiblockable.writeMultiblockToNbt(this));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        if (nbt.contains("Progress")) {
            this.progress = nbt.getInt("Progress", 0);
        }

        if (nbt.contains("MaxProgress")) {
            this.maxProgress = nbt.getInt("MaxProgress", 0);
        }

        if (nbt.contains("CurrentRecipe")) {
            this.currentRecipeId = nbt.get("CurrentRecipe", RegistryKey.createCodec(RegistryKeys.RECIPE))
                    .orElse(null);
        }

        if (nbt.contains("RecipeRPM")) {
            this.recipeRPM = nbt.getInt("RecipeRPM", 0);
        }

        if (nbt.contains("Inventory"))
            this.wrappedInventoryStorage.readNbt(nbt.getListOrEmpty("Inventory"), registries);

        if (nbt.contains("FluidTank"))
            this.wrappedFluidStorage.readNbt(nbt.getListOrEmpty("FluidTank"), registries);

        if (nbt.contains("SlurryTank"))
            this.wrappedSlurryStorage.readNbt(nbt.getListOrEmpty("SlurryTank"), registries);

        if (nbt.contains("Energy"))
            this.wrappedEnergyStorage.readNbt(nbt.getListOrEmpty("Energy"), registries);

        if (nbt.contains("OutputStack")) {
            this.outputItemStack = ItemStack.fromNbt(registries, nbt.get("OutputStack"))
                    .orElse(ItemStack.EMPTY);
        }

        if (nbt.contains("OutputSlurry")) {
            this.outputSlurryStack = nbt.get("OutputSlurry", SlurryStack.CODEC.codec())
                    .orElse(SlurryStack.EMPTY);
        }

        if (nbt.contains("MachinePositions")) {
            Multiblockable.readMultiblockFromNbt(this, nbt.getListOrEmpty("MachinePositions"));
        }
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.CENTRIFUGAL_CONCENTRATOR;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        if (this.world == null)
            return List.of();

        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = 0; y < 3; y++) {
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
    public Map<Direction, MultiblockIOPort> getPorts(Vec3i offsetFromPrimary, Direction direction) {
        Map<Direction, List<TransferType<?, ?, ?>>> transferTypes = new EnumMap<>(Direction.class);
        if (offsetFromPrimary.getY() == 0 && direction == Direction.DOWN) {
            transferTypes.computeIfAbsent(direction, k -> new ArrayList<>()).add(TransferType.ENERGY);
            transferTypes.computeIfAbsent(direction, k -> new ArrayList<>()).add(TransferType.SLURRY);
            transferTypes.computeIfAbsent(direction, k -> new ArrayList<>()).add(TransferType.ITEM);
        }

        if (offsetFromPrimary.getY() == 2 && direction == Direction.UP) {
            transferTypes.computeIfAbsent(direction, k -> new ArrayList<>()).add(TransferType.FLUID);
            transferTypes.computeIfAbsent(direction, k -> new ArrayList<>()).add(TransferType.ITEM);
        }

        return Multiblockable.toIOPortMap(transferTypes);
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
        return new CentrifugalConcentratorScreenHandler(syncId, playerInventory, this, this.wrappedInventoryStorage, this.propertyDelegate);
    }

    public SyncingSimpleInventory getInputInventory() {
        return (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(0);
    }

    public SyncingSimpleInventory getOutputInventory() {
        return (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(1);
    }

    public SyncingSimpleInventory getBucketInputInventory() {
        return (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(2);
    }

    public SyncingSimpleInventory getBucketOutputInventory() {
        return (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(3);
    }

    public SyncingFluidStorage getInputFluidTank() {
        return (SyncingFluidStorage) this.wrappedFluidStorage.getStorage(Direction.UP);
    }

    public SyncingSlurryStorage getOutputSlurryTank() {
        return (SyncingSlurryStorage) this.wrappedSlurryStorage.getStorage(Direction.DOWN);
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
    }

    public InventoryStorage getInventoryProvider(Direction side) {
        return this.wrappedInventoryStorage.getStorage(side);
    }

    public SingleFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public SingleSlurryStorage getSlurryProvider(Direction side) {
        return this.wrappedSlurryStorage.getStorage(side);
    }

    public EnergyStorage getEnergyProvider(Direction side) {
        return this.wrappedEnergyStorage.getStorage(side);
    }
}
