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
import dev.turtywurty.industria.blockentity.util.fluid.OutputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.InputSlurryStorage;
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
import dev.turtywurty.industria.recipe.DigesterRecipe;
import dev.turtywurty.industria.recipe.input.DigesterRecipeInput;
import dev.turtywurty.industria.screenhandler.DigesterScreenHandler;
import dev.turtywurty.industria.util.TransferUtils;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.*;

// TODO: Make this work with temperature and pressure
public class DigesterBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, Multiblockable, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("digester");

    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedSlurryStorage<SingleSlurryStorage> wrappedSlurryStorage = new WrappedSlurryStorage<>();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();

    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    private RegistryKey<Recipe<?>> currentRecipeId;
    private int progress;
    private int maxProgress;

    private final PropertyDelegate properties = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> throw new IllegalArgumentException("Invalid index: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                default -> throw new IllegalArgumentException("Invalid index: " + index);
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public DigesterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.DIGESTER, pos, state);

        this.wrappedInventoryStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createSlurryPredicate(() -> {
                    SyncingSlurryStorage inputSlurryTank = getInputSlurryStorage();
                    return new SlurryStack(inputSlurryTank.variant, inputSlurryTank.amount);
                })), Direction.UP);
        this.wrappedInventoryStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createEmptyFluidPredicate(() -> getOutputFluidStorage().variant)));

        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 100_000, 5_000, 0));

        this.wrappedSlurryStorage.addStorage(new InputSlurryStorage(this, FluidConstants.BUCKET * 5), Direction.UP);
        this.wrappedFluidStorage.addStorage(new OutputFluidStorage(this, FluidConstants.BUCKET * 5), Direction.SOUTH);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        PredicateSimpleInventory inputSlurryInventory = getInputSlurryInventory();
        PredicateSimpleInventory outputFluidInventory = getOutputFluidInventory();
        SyncingEnergyStorage energyStorage = getEnergyStorage();
        InputSlurryStorage inputSlurryStorage = getInputSlurryStorage();
        OutputFluidStorage outputFluidStorage = getOutputFluidStorage();

        return List.of(inputSlurryInventory, outputFluidInventory, energyStorage, inputSlurryStorage, outputFluidStorage);
    }

    public PredicateSimpleInventory getInputSlurryInventory() {
        return (PredicateSimpleInventory) this.wrappedInventoryStorage.getInventory(0);
    }

    public PredicateSimpleInventory getOutputFluidInventory() {
        return (PredicateSimpleInventory) this.wrappedInventoryStorage.getInventory(1);
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
    }

    public InputSlurryStorage getInputSlurryStorage() {
        return (InputSlurryStorage) this.wrappedSlurryStorage.getStorage(Direction.UP);
    }

    public OutputFluidStorage getOutputFluidStorage() {
        return (OutputFluidStorage) this.wrappedFluidStorage.getStorage(Direction.SOUTH);
    }

    @Override
    public Map<Direction, MultiblockIOPort> getPorts(Vec3i offsetFromPrimary, Direction direction) {
        Map<Direction, List<TransferType<?, ?, ?>>> transferTypes = new EnumMap<>(Direction.class);
        if (offsetFromPrimary.getY() == 4 && Multiblockable.isCenterColumn(offsetFromPrimary) && direction == Direction.UP)
            transferTypes.computeIfAbsent(direction, d -> new ArrayList<>()).add(TransferType.SLURRY);

        if (offsetFromPrimary.getZ() == -1 && direction == Direction.NORTH)
            transferTypes.computeIfAbsent(direction, d -> new ArrayList<>()).add(TransferType.ENERGY);

        if (offsetFromPrimary.getY() == 0 && offsetFromPrimary.getZ() == 1 && direction == Direction.SOUTH)
            transferTypes.computeIfAbsent(direction, d -> new ArrayList<>()).add(TransferType.FLUID);

        return Multiblockable.toIOPortMap(transferTypes);
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        SyncingSimpleInventory bucketInputInventory = getInputSlurryInventory();
        if (!bucketInputInventory.isEmpty()) {
            ItemStack bucket = bucketInputInventory.getStack(0);
            Storage<SlurryVariant> storage = SlurryStorage.ITEM.find(bucket, ContainerItemContext.withConstant(bucket));
            if (storage != null && storage.supportsExtraction()) {
                SyncingSlurryStorage inputSlurryTank = getInputSlurryStorage();
                Optional<SlurryVariant> optVariant = TransferUtils.findFirstVariant(storage, inputSlurryTank.variant);
                optVariant.filter(TransferVariant::isBlank).ifPresent(variant -> {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long extracted = storage.extract(variant, FluidConstants.BUCKET, transaction);
                        if (extracted > 0) {
                            inputSlurryTank.variant = variant;
                            inputSlurryTank.amount += extracted;
                        }

                        transaction.commit();
                    }
                });
            }
        }

        SyncingSimpleInventory bucketOutputInventory = getOutputFluidInventory();
        if (!bucketOutputInventory.isEmpty()) {
            ItemStack bucket = bucketOutputInventory.getStack(0);
            Storage<FluidVariant> storage = FluidStorage.ITEM.find(bucket, ContainerItemContext.withConstant(bucket));
            if (storage != null && storage.supportsInsertion()) {
                SyncingFluidStorage outputFluidTank = getOutputFluidStorage();
                if (outputFluidTank.amount > 0) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long inserted = storage.insert(outputFluidTank.variant, FluidConstants.BUCKET, transaction);
                        if (inserted > 0) {
                            outputFluidTank.amount -= inserted;
                        }

                        transaction.commit();
                    }
                }
            }
        }

        if(this.currentRecipeId == null) {
            Optional<RecipeEntry<DigesterRecipe>> recipeEntry = getCurrentRecipe();
            if (recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().processTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeEntry<DigesterRecipe>> recipeEntry = getCurrentRecipe();
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            this.currentRecipeId = null;
            this.maxProgress = 0;
            this.progress = 0;
            update();
            return;
        }

        DigesterRecipe recipe = recipeEntry.get().value();
        if (this.progress >= this.maxProgress) {
            OutputFluidStorage outputFluidStorage = getOutputFluidStorage();
            FluidStack outputFluidStack = recipe.outputFluid();
            if(outputFluidStorage.canInsert(outputFluidStack) && hasEnergy()) {
                extractEnergy();

                InputSlurryStorage inputSlurryStorage = getInputSlurryStorage();
                inputSlurryStorage.amount -= recipe.inputSlurry().amount();

                outputFluidStorage.variant = outputFluidStack.variant();
                outputFluidStorage.amount += outputFluidStack.amount();

                this.progress = 0;
                this.maxProgress = 0;
                this.currentRecipeId = null;

                update();
            }
        } else {
            if (hasEnergy()) {
                this.progress++;
                extractEnergy();
                update();
            }
        }
    }

    private Optional<RecipeEntry<DigesterRecipe>> getCurrentRecipe() {
        if (this.world == null || !(this.world instanceof ServerWorld serverWorld))
            return Optional.empty();

        InputSlurryStorage slurryStorage = getInputSlurryStorage();
        return serverWorld.getRecipeManager().getFirstMatch(RecipeTypeInit.DIGESTER, new DigesterRecipeInput(new SlurryStack(slurryStorage.variant, slurryStorage.amount)), this.world);
    }

    private boolean hasEnergy() {
        return getEnergyStorage().amount >= 100;
    }

    private void extractEnergy() {
        getEnergyStorage().amount -= 100;
        update();
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
        return new DigesterScreenHandler(syncId, playerInventory, this, this.properties);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        nbt.put("Inventory", this.wrappedInventoryStorage.writeNbt(registries));
        nbt.put("Energy", this.wrappedEnergyStorage.writeNbt(registries));
        nbt.put("SlurryTank", this.wrappedSlurryStorage.writeNbt(registries));
        nbt.put("FluidTank", this.wrappedFluidStorage.writeNbt(registries));
        nbt.put("MultiblockPositions", Multiblockable.writeMultiblockToNbt(this));
        nbt.putInt("Progress", this.progress);
        nbt.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            Optional<NbtElement> result = RegistryKey.createCodec(RegistryKeys.RECIPE)
                    .encodeStart(NbtOps.INSTANCE, this.currentRecipeId)
                    .result();
            result.ifPresent(nbtElement -> nbt.put("CurrentRecipe", nbtElement));
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        if(nbt.contains("Inventory", NbtElement.LIST_TYPE))
            this.wrappedInventoryStorage.readNbt(nbt.getList("Inventory", NbtElement.COMPOUND_TYPE), registries);

        if(nbt.contains("Energy", NbtElement.LIST_TYPE))
            this.wrappedEnergyStorage.readNbt(nbt.getList("Energy", NbtElement.COMPOUND_TYPE), registries);

        if(nbt.contains("SlurryTank", NbtElement.LIST_TYPE))
            this.wrappedSlurryStorage.readNbt(nbt.getList("SlurryTank", NbtElement.COMPOUND_TYPE), registries);

        if(nbt.contains("FluidTank", NbtElement.LIST_TYPE))
            this.wrappedFluidStorage.readNbt(nbt.getList("FluidTank", NbtElement.COMPOUND_TYPE), registries);

        if(nbt.contains("MultiblockPositions", NbtElement.LIST_TYPE))
            Multiblockable.readMultiblockFromNbt(this, nbt.getList("MultiblockPositions", NbtElement.INT_ARRAY_TYPE));

        if(nbt.contains("Progress", NbtElement.INT_TYPE))
            this.progress = nbt.getInt("Progress");

        if(nbt.contains("MaxProgress", NbtElement.INT_TYPE))
            this.maxProgress = nbt.getInt("MaxProgress");

        if (nbt.contains("CurrentRecipe", NbtElement.COMPOUND_TYPE)) {
            NbtCompound currentRecipe = nbt.getCompound("CurrentRecipe");
            this.currentRecipeId = currentRecipe.isEmpty() ? null :
                    RegistryKey.createCodec(RegistryKeys.RECIPE)
                            .decode(NbtOps.INSTANCE, currentRecipe)
                            .map(Pair::getFirst)
                            .result()
                            .orElse(null);
        }
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
    public MultiblockType<?> type() {
        return MultiblockTypeInit.DIGESTER;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        if (this.world == null)
            return List.of();

        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = 0; y < 5; y++) {
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
    public WrappedInventoryStorage<?> getWrappedInventoryStorage() {
        return this.wrappedInventoryStorage;
    }

    @Override
    public Block getBlock() {
        return getCachedState().getBlock();
    }

    public @Nullable EnergyStorage getEnergyProvider(@Nullable Direction direction) {
        return this.wrappedEnergyStorage.getStorage(direction);
    }

    public @Nullable SingleSlurryStorage getSlurryProvider(@Nullable Direction direction) {
        return this.wrappedSlurryStorage.getStorage(direction);
    }

    public @Nullable SingleFluidStorage getFluidProvider(@Nullable Direction direction) {
        return this.wrappedFluidStorage.getStorage(direction);
    }
}
