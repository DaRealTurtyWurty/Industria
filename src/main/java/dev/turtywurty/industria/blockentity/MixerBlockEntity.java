package dev.turtywurty.industria.blockentity;

import com.mojang.datafixers.util.Pair;
import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SingleSlurryStorage;
import dev.turtywurty.fabricslurryapi.api.storage.SlurryStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
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
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.multiblock.MultiblockIOPort;
import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.MixerRecipe;
import dev.turtywurty.industria.recipe.input.MixerRecipeInput;
import dev.turtywurty.industria.screenhandler.MixerScreenHandler;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
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
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.*;

// TODO: Leaving this here as an example, just in case I decide to make this system in the future
// public TickBuilder createTickBuilder() {
//        return TickBuilder.builder()
//                .progress(this.progress)
//                .maxProgress(this.maxProgress)
//                .currentRecipeId(this.currentRecipeId)
//                .validateWorld(false)
//                .fluidInputThroughBucket(getBucketInputInventory(), getInputFluidTank())
//                .fluidOutputThroughBucket(getBucketOutputInventory(), getOutputFluidTank())
//                .tryClearItemBuffer(this.outputItemStack, getOutputInventory())
//                .tryClearFluidBuffer(this.outputSlurryStack, getOutputFluidTank())
//                .checkForRecipe(RecipeTypeInit.MIXER)
//                .onComplete(new OnCompleteBuilder()
//                        .checkEnergy(10)
//                        .extractEnergy(10)
//                        .craftRecipe()
//                        .insertOutput()
//                        .resetProgress()
//                        .build())
//                .onProgress(new OnProgressBuilder()
//                        .checkEnergy(10)
//                        .extractEnergy(10)
//                        .incrementProgress()
//                        .build());
//    }
// TODO: Fix issue where items in the slots are duplicated as ghost items on the client
public class MixerBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, Multiblockable, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("mixer");

    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedSlurryStorage<SingleSlurryStorage> wrappedSlurryStorage = new WrappedSlurryStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();

    private int temperature = 175;
    private int progress, maxProgress;
    private RegistryKey<Recipe<?>> currentRecipeId;
    private ItemStack outputItemStack = ItemStack.EMPTY;
    private SlurryStack outputSlurryStack = SlurryStack.EMPTY;

    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    public float stirringRotation = 0.0F;
    public final List<Vec3d> mixingItemPositions = DefaultedList.ofSize(6, new Vec3d(0, 1, 0));

    private final PropertyDelegate properties = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> 0;
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
            return 2;
        }
    };

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.MIXER, pos, state);

        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 6), Direction.EAST);
        this.wrappedInventoryStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.WEST);
        this.wrappedInventoryStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createFluidPredicate(() -> {
                    SyncingFluidStorage inputFluidTank = getInputFluidTank();
                    return new FluidStack(inputFluidTank.variant, inputFluidTank.amount);
                })), Direction.NORTH);
        this.wrappedInventoryStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createEmptySlurryPredicate(() -> getOutputSlurryTank().variant)), Direction.SOUTH);

        this.wrappedFluidStorage.addStorage(
                new InputFluidStorage(this, FluidConstants.BUCKET * 10), Direction.UP);

        this.wrappedSlurryStorage.addStorage(
                new OutputSlurryStorage(this, FluidConstants.BUCKET * 10), Direction.DOWN);

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 10_000, 1_000, 0));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingSimpleInventory inputInventory = getInputInventory();
        SyncingSimpleInventory outputInventory = getOutputInventory();
        SyncingSimpleInventory bucketInputInventory = getBucketInputInventory();
        SyncingSimpleInventory bucketOutputInventory = getBucketOutputInventory();

        SyncingFluidStorage inputFluidTank = getInputFluidTank();
        SyncingSlurryStorage outputSlurryTank = getOutputSlurryTank();

        SyncingEnergyStorage energy = getEnergyStorage();

        return List.of(inputInventory, outputInventory, bucketInputInventory, bucketOutputInventory, inputFluidTank, outputSlurryTank, energy);
    }

    // TODO: Move to util class
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <V, T extends TransferVariant<V>> Optional<T> findFirstVariant(Storage<T> storage, @Nullable T checkFirst) {
        if (storage instanceof SingleVariantStorage singleFluidStorage) {
            return Optional.ofNullable((T) singleFluidStorage.variant);
        }

        if (checkFirst != null && !checkFirst.isBlank()) {
            try (Transaction transaction = Transaction.openOuter()) {
                if (storage.extract(checkFirst, FluidConstants.BUCKET, transaction) > 0) {
                    return Optional.of(checkFirst);
                }

                return Optional.empty();
            }
        }

        for (StorageView<T> storageView : storage.nonEmptyViews()) {
            return Optional.ofNullable(storageView.getResource());
        }

        return Optional.empty();
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
                Optional<FluidVariant> optVariant = findFirstVariant(storage, inputFluidTank.variant);
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
            SyncingSlurryStorage outputSlurryTank = getOutputSlurryTank();
            if (outputSlurryTank.canInsert(this.outputSlurryStack)) {
                long inserted = Math.min(outputSlurryTank.getCapacity() - outputSlurryTank.amount, this.outputSlurryStack.amount());
                outputSlurryTank.amount += inserted;
                this.outputSlurryStack = this.outputSlurryStack.withAmount(this.outputSlurryStack.amount() - inserted);
                update();
            }

            return;
        }

        MixerRecipeInput recipeInput = createRecipeInput();
        if (this.currentRecipeId == null) {
            Optional<RecipeEntry<MixerRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
            if (recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().processTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeEntry<MixerRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            this.currentRecipeId = null;
            this.maxProgress = 0;
            this.progress = 0;
            update();
            return;
        }

        MixerRecipe recipe = recipeEntry.get().value();
        if (this.progress >= this.maxProgress) {
            if (hasEnergy()) {
                extractEnergy(recipe);

                ItemStack output = recipe.craft(recipeInput, this.world.getRegistryManager());
                SyncingFluidStorage inputFluidTank = getInputFluidTank();
                inputFluidTank.amount -= recipe.inputFluid().amount();

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
            if (hasEnergy()) {
                this.progress++;
                extractEnergy(recipe);
                update();
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        nbt.putInt("Progress", this.progress);
        nbt.putInt("MaxProgress", this.maxProgress);
        nbt.putInt("Temperature", this.temperature);
        if (this.currentRecipeId != null) {
            Optional<NbtElement> result = RegistryKey.createCodec(RegistryKeys.RECIPE)
                    .encodeStart(NbtOps.INSTANCE, this.currentRecipeId)
                    .result();
            result.ifPresent(nbtElement -> nbt.put("CurrentRecipe", nbtElement));
        }

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

        if (nbt.contains("Progress", NbtElement.INT_TYPE)) {
            this.progress = nbt.getInt("Progress");
        }

        if (nbt.contains("MaxProgress", NbtElement.INT_TYPE)) {
            this.maxProgress = nbt.getInt("MaxProgress");
        }

        if (nbt.contains("Temperature", NbtElement.INT_TYPE)) {
            this.temperature = nbt.getInt("Temperature");
        }

        if (nbt.contains("CurrentRecipe", NbtElement.COMPOUND_TYPE)) {
            NbtCompound currentRecipe = nbt.getCompound("CurrentRecipe");
            this.currentRecipeId = currentRecipe.isEmpty() ? null :
                    RegistryKey.createCodec(RegistryKeys.RECIPE)
                            .decode(NbtOps.INSTANCE, currentRecipe)
                            .map(Pair::getFirst)
                            .result()
                            .orElse(null);
        }

        if (nbt.contains("Inventory", NbtElement.LIST_TYPE))
            this.wrappedInventoryStorage.readNbt(nbt.getList("Inventory", NbtElement.COMPOUND_TYPE), registries);

        if (nbt.contains("FluidTank", NbtElement.LIST_TYPE))
            this.wrappedFluidStorage.readNbt(nbt.getList("FluidTank", NbtElement.COMPOUND_TYPE), registries);

        if (nbt.contains("SlurryTank", NbtElement.LIST_TYPE))
            this.wrappedSlurryStorage.readNbt(nbt.getList("SlurryTank", NbtElement.COMPOUND_TYPE), registries);

        if (nbt.contains("Energy", NbtElement.LIST_TYPE))
            this.wrappedEnergyStorage.readNbt(nbt.getList("Energy", NbtElement.COMPOUND_TYPE), registries);

        if (nbt.contains("OutputStack", NbtElement.COMPOUND_TYPE)) {
            this.outputItemStack = ItemStack.fromNbtOrEmpty(registries, nbt.getCompound("OutputStack"));
        }

        if (nbt.contains("OutputSlurry", NbtElement.COMPOUND_TYPE)) {
            this.outputSlurryStack = SlurryStack.CODEC.codec()
                    .decode(NbtOps.INSTANCE, nbt.getCompound("OutputSlurry"))
                    .map(Pair::getFirst)
                    .getOrThrow();
        }

        if (nbt.contains("MachinePositions", NbtElement.LIST_TYPE)) {
            Multiblockable.readMultiblockFromNbt(this, nbt.getList("MachinePositions", NbtElement.INT_ARRAY_TYPE));
        }
    }

    private boolean hasEnergy() {
        return getEnergyStorage().amount >= 100;
    }

    private void extractEnergy(MixerRecipe recipe) {
        getEnergyStorage().amount -= recipe.maxTemp() - recipe.minTemp();
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public int getTemperature() {
        return this.temperature;
    }

    private Optional<RecipeEntry<MixerRecipe>> getCurrentRecipe(MixerRecipeInput recipeInput) {
        if (this.world == null || !(this.world instanceof ServerWorld serverWorld))
            return Optional.empty();

        return serverWorld.getRecipeManager().getFirstMatch(RecipeTypeInit.MIXER, recipeInput, this.world);
    }

    private MixerRecipeInput createRecipeInput() {
        SyncingFluidStorage inputFluidTank = getInputFluidTank();
        return new MixerRecipeInput(getInputInventory(), new FluidStack(inputFluidTank.variant, inputFluidTank.amount), this.temperature);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        var nbt = super.toInitialChunkDataNbt(registries);
        writeNbt(nbt, registries);
        return nbt;
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
        return new MixerScreenHandler(syncId, playerInventory, this, this.properties);
    }

    @Override
    public Map<Direction, MultiblockIOPort> getPorts(Vec3i offsetFromPrimary, Direction direction) {
        Map<Direction, List<TransferType<?, ?>>> transferTypes = new EnumMap<>(Direction.class);
        if (offsetFromPrimary.getY() == 2 && Multiblockable.isCenterColumn(offsetFromPrimary) && direction == Direction.UP) {
            transferTypes.computeIfAbsent(direction, k -> new ArrayList<>()).add(TransferType.FLUID);
        }

        if (offsetFromPrimary.getY() == 0 && !Multiblockable.isCenterColumn(offsetFromPrimary) && direction == Direction.DOWN) {
            transferTypes.computeIfAbsent(direction, k -> new ArrayList<>()).add(TransferType.SLURRY);
        }

        if (offsetFromPrimary.getZ() != 0 && offsetFromPrimary.getX() == 0 && offsetFromPrimary.getY() == 0) {
            if (offsetFromPrimary.getZ() == -1 && direction == Direction.EAST) {
                transferTypes.computeIfAbsent(direction, k -> new ArrayList<>()).add(TransferType.ITEM);
            } else if (offsetFromPrimary.getZ() == 1 && direction == Direction.WEST) {
                transferTypes.computeIfAbsent(direction, k -> new ArrayList<>()).add(TransferType.ITEM);
            }
        }

        if (((offsetFromPrimary.getY() == 2 && direction == Direction.UP) || (offsetFromPrimary.getY() == 0 && direction == Direction.DOWN)) && !Multiblockable.isCenterColumn(offsetFromPrimary)) {
            transferTypes.computeIfAbsent(direction, k -> new ArrayList<>()).add(TransferType.ENERGY);
        }

        return Multiblockable.toIOPortMap(transferTypes);
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

    @Override
    public WrappedInventoryStorage<SimpleInventory> getWrappedInventoryStorage() {
        return wrappedInventoryStorage;
    }

    @Override
    public Block getBlock() {
        return getCachedState().getBlock();
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.MIXER;
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

    public boolean isMixing() {
        return this.progress > 0 && this.progress < this.maxProgress;
    }
}
