package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.*;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.multiblock.MultiblockIOPort;
import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.ClarifierRecipe;
import dev.turtywurty.industria.recipe.input.ClarifierRecipeInput;
import dev.turtywurty.industria.screenhandler.ClarifierScreenHandler;
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

import java.util.*;

public class ClarifierBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity, BlockEntityContentsDropper, Multiblockable, BlockEntityWithGui<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("clarifier");

    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();

    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    private RegistryKey<Recipe<?>> currentRecipeId;
    private int progress;
    private int maxProgress;

    private ItemStack outputItemStack = ItemStack.EMPTY;
    private FluidStack outputFluidStack = FluidStack.EMPTY;

    private ItemStack nextOutputItemStack = ItemStack.EMPTY; // Used for rendering

    private final PropertyDelegate properties = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> throw new IllegalArgumentException("Unknown property index: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                default -> throw new IllegalArgumentException("Unknown property index: " + index);
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public ClarifierBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.CLARIFIER, pos, state);

        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidConstants.BUCKET * 5), Direction.UP);

        this.wrappedFluidStorage.addStorage(new OutputFluidStorage(this, FluidConstants.BUCKET * 5), Direction.NORTH);
        this.wrappedInventoryStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.SOUTH);
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
        SyncingFluidStorage inputFluidTank = getInputFluidTank();
        SyncingFluidStorage outputFluidTank = getOutputFluidTank();
        SyncingSimpleInventory outputInventory = getOutputInventory();

        return List.of(inputFluidTank, outputFluidTank, outputInventory);
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

        if (!this.outputFluidStack.isEmpty()) {
            SyncingFluidStorage outputFluidTank = getOutputFluidTank();
            if (outputFluidTank.canInsert(this.outputFluidStack)) {
                long inserted = Math.min(outputFluidTank.getCapacity() - outputFluidTank.amount, this.outputFluidStack.amount());
                outputFluidTank.amount += inserted;
                outputFluidTank.variant = outputFluidStack.variant();
                this.outputFluidStack = this.outputFluidStack.withAmount(this.outputFluidStack.amount() - inserted);
                update();
            }

            return;
        }

        SyncingFluidStorage inputFluidStorage = getInputFluidTank();
        var inputFluidStack = new FluidStack(inputFluidStorage.variant, inputFluidStorage.amount);
        var recipeInput = new ClarifierRecipeInput(inputFluidStack);
        if (this.currentRecipeId == null) {
            Optional<RecipeEntry<ClarifierRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
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

        Optional<RecipeEntry<ClarifierRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            this.currentRecipeId = null;
            this.maxProgress = 0;
            this.progress = 0;
            this.nextOutputItemStack = ItemStack.EMPTY;

            update();
            return;
        }

        ClarifierRecipe recipe = recipeEntry.get().value();
        if (this.progress >= this.maxProgress) {
            this.outputItemStack = recipe.craft(recipeInput, this.world.getRegistryManager());
            this.outputFluidStack = recipe.outputFluidStack();
            inputFluidStorage.amount -= recipe.inputFluid().amount();

            this.currentRecipeId = null;
            this.maxProgress = 0;
            this.progress = 0;
            this.nextOutputItemStack = ItemStack.EMPTY;
            update();
        } else {
            this.progress++;
            this.nextOutputItemStack = recipe.craft(recipeInput, this.world.getRegistryManager());
            update();
        }
    }

    private Optional<RecipeEntry<ClarifierRecipe>> getCurrentRecipe(ClarifierRecipeInput recipeInput) {
        if (this.world == null || !(this.world instanceof ServerWorld serverWorld))
            return Optional.empty();

        return serverWorld.getRecipeManager().getFirstMatch(RecipeTypeInit.CLARIFIER, recipeInput, this.world);
    }

    public SyncingSimpleInventory getOutputInventory() {
        return (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(0);
    }

    public SyncingFluidStorage getInputFluidTank() {
        return (SyncingFluidStorage) this.wrappedFluidStorage.getStorage(Direction.UP);
    }

    public SyncingFluidStorage getOutputFluidTank() {
        return (SyncingFluidStorage) this.wrappedFluidStorage.getStorage(Direction.NORTH);
    }

    public InventoryStorage getInventoryProvider(Direction side) {
        return this.wrappedInventoryStorage.getStorage(side);
    }

    public SingleFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public ItemStack getNextOutputItemStack() {
        return this.nextOutputItemStack;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    @Override
    public Map<Direction, MultiblockIOPort> getPorts(Vec3i offsetFromPrimary, Direction direction) {
        Map<Direction, List<TransferType<?, ?, ?>>> transferTypes = new EnumMap<>(Direction.class);
        if (Multiblockable.isCenterColumn(offsetFromPrimary) && offsetFromPrimary.getY() == 1 && direction == Direction.UP)
            transferTypes.put(Direction.UP, List.of(TransferType.FLUID));
        else if (offsetFromPrimary.getY() == 0 && offsetFromPrimary.getZ() == -1 && offsetFromPrimary.getX() == 0 && direction == Direction.NORTH)
            transferTypes.put(Direction.NORTH, List.of(TransferType.FLUID));
        else if (offsetFromPrimary.getY() == 0 && offsetFromPrimary.getZ() == 1 && offsetFromPrimary.getX() == 0 && direction == Direction.SOUTH)
            transferTypes.put(Direction.SOUTH, List.of(TransferType.ITEM));

        return Multiblockable.toIOPortMap(transferTypes);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        nbt.put("Inventory", this.wrappedInventoryStorage.writeNbt(registries));
        nbt.put("FluidTank", this.wrappedFluidStorage.writeNbt(registries));
        nbt.put("MultiblockPositions", Multiblockable.writeMultiblockToNbt(this));
        nbt.putInt("Progress", this.progress);
        nbt.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            nbt.put("CurrentRecipe", RegistryKey.createCodec(RegistryKeys.RECIPE), this.currentRecipeId);
        }

        if (!this.outputItemStack.isEmpty()) {
            nbt.put("OutputStack", this.outputItemStack.toNbt(registries));
        }

        if (!this.outputFluidStack.isEmpty()) {
            nbt.put("OutputFluid", FluidStack.CODEC.codec(), this.outputFluidStack);
        }

        nbt.put("NextOutputStack", ItemStack.OPTIONAL_CODEC, this.nextOutputItemStack);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        if (nbt.contains("Inventory"))
            this.wrappedInventoryStorage.readNbt(nbt.getListOrEmpty("Inventory"), registries);

        if (nbt.contains("FluidTank"))
            this.wrappedFluidStorage.readNbt(nbt.getListOrEmpty("FluidTank"), registries);

        if (nbt.contains("MultiblockPositions"))
            Multiblockable.readMultiblockFromNbt(this, nbt.getListOrEmpty("MultiblockPositions"));

        if (nbt.contains("Progress"))
            this.progress = nbt.getInt("Progress", 0);

        if (nbt.contains("MaxProgress"))
            this.maxProgress = nbt.getInt("MaxProgress", 0);

        if (nbt.contains("CurrentRecipe")) {
            this.currentRecipeId = nbt.get("CurrentRecipe", RegistryKey.createCodec(RegistryKeys.RECIPE))
                    .orElse(null);
        }

        if (nbt.contains("OutputStack")) {
            this.outputItemStack = ItemStack.fromNbt(registries, nbt.get("OutputStack"))
                    .orElse(ItemStack.EMPTY);
        }

        if (nbt.contains("OutputFluid")) {
            this.outputFluidStack = nbt.get("OutputFluid", FluidStack.CODEC.codec())
                    .orElse(FluidStack.EMPTY);
        }

        if (nbt.contains("NextOutputStack"))
            this.nextOutputItemStack = nbt.get("NextOutputStack", ItemStack.OPTIONAL_CODEC)
                    .orElse(ItemStack.EMPTY);
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
        return MultiblockTypeInit.CLARIFIER;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        // 3x3x2 (3 wide, 3 long, 2 high)
        if (this.world == null)
            return List.of();

        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 1; y++) {
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
        return new ClarifierScreenHandler(syncId, playerInventory, this, this.wrappedInventoryStorage, this.properties);
    }
}
