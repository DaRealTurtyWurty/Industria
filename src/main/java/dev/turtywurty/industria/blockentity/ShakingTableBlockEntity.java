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
import dev.turtywurty.industria.multiblock.*;
import dev.turtywurty.industria.multiblock.old.*;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.ShakingTableRecipe;
import dev.turtywurty.industria.recipe.input.ShakingTableRecipeInput;
import dev.turtywurty.industria.screenhandler.ShakingTableScreenHandler;
import dev.turtywurty.industria.util.TransferUtils;
import dev.turtywurty.industria.util.ViewUtils;
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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShakingTableBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, AutoMultiblockable, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("shaking_table");

    private static final List<PositionedPortRule> PORT_RULES = List.of(
            PositionedPortRule.when(p -> p.y() == 0)
                    .on(LocalDirection.DOWN)
                    .types(PortType.input(TransferType.ENERGY), PortType.output(TransferType.SLURRY), PortType.input(TransferType.ITEM))
                    .build(),
            PositionedPortRule.when(p -> p.y() == 1)
                    .on(LocalDirection.UP)
                    .types(PortType.input(TransferType.FLUID), PortType.input(TransferType.ITEM))
                    .build()
    );

    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedSlurryStorage<SingleSlurryStorage> wrappedSlurryStorage = new WrappedSlurryStorage<>();

    private final List<BlockPos> multiblockPositions = new ArrayList<>();
    private final Box shakeBox;
    private int progress, maxProgress;
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
    private RegistryKey<Recipe<?>> currentRecipeId;
    private int recipeFrequency;
    private ItemStack outputItemStack = ItemStack.EMPTY;
    private SlurryStack outputSlurryStack = SlurryStack.EMPTY;

    public ShakingTableBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.SHAKING_TABLE, BlockEntityTypeInit.SHAKING_TABLE, pos, state);

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
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 100_000, 10_000, 0));
        this.wrappedSlurryStorage.addStorage(new OutputSlurryStorage(this, FluidConstants.BUCKET * 10), Direction.DOWN);

        this.shakeBox = createShakeBox();
    }

    public Box createShakeBox() {
        Vec3d topCenter = this.pos.toBottomCenterPos().add(0, 1, 0);
        float x1 = -18 / 16f;
        float y1 = 4 / 16f;
        float z1 = -19 / 16f;

        float x2 = 18 / 16f;
        float y2 = y1 + 7 / 16f;
        float z2 = 35 / 16f;

        Direction facing = getCachedState().get(Properties.HORIZONTAL_FACING);

        double dx1, dz1, dx2, dz2;
        switch (facing) {
            case NORTH:
                dx1 = x1;
                dz1 = z2;
                dx2 = x2;
                dz2 = z1;
                break;
            case SOUTH:
                dx1 = x1;
                dz1 = -z1;
                dx2 = x2;
                dz2 = -z2;
                break;
            case WEST:
                dx1 = z1;
                dz1 = x1;
                dx2 = z2;
                dz2 = x2;
                break;
            case EAST:
            default:
                dx1 = -z2;
                dz1 = x1;
                dx2 = -z1;
                dz2 = x2;
                break;
        }

        return new Box(
                topCenter.getX() + dx1,
                topCenter.getY() + y1,
                topCenter.getZ() + dz1,
                topCenter.getX() + dx2,
                topCenter.getY() + y2,
                topCenter.getZ() + dz2
        );
    }

    @Override
    public Block getBlock() {
        return this.blockRef;
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

        ShakingTableRecipeInput recipeInput = createRecipeInput();
        if (this.currentRecipeId == null) {
            Optional<RecipeEntry<ShakingTableRecipe>> recipeEntryOpt = getCurrentRecipe(recipeInput);
            if (recipeEntryOpt.isPresent()) {
                RecipeEntry<ShakingTableRecipe> recipeEntry = recipeEntryOpt.get();
                this.currentRecipeId = recipeEntry.id();

                ShakingTableRecipe recipe = recipeEntry.value();
                this.recipeFrequency = recipe.frequency();
                this.maxProgress = recipe.processTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeEntry<ShakingTableRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            this.currentRecipeId = null;
            this.recipeFrequency = 0;
            this.maxProgress = 0;
            this.progress = 0;
            update();
            return;
        }

        ShakingTableRecipe recipe = recipeEntry.get().value();
        this.recipeFrequency = recipe.frequency();
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

        if (this.progress < this.maxProgress) {
            float shakesPerTick = this.recipeFrequency / 20f; // Convert frequency to shakes per tick
            int sign = (((this.progress & 1) == 0) ? 1 : -1); // Alternate shake direction every tick
            float amountToShake = shakesPerTick * sign;

            Direction facing = getCachedState().get(Properties.HORIZONTAL_FACING);
            Vec3d shakeDirection = facing.getAxis() == Direction.Axis.X
                    ? new Vec3d(amountToShake, 0, 0)
                    : new Vec3d(0, 0, amountToShake);

            for (LivingEntity livingEntity : this.world.getEntitiesByClass(LivingEntity.class, this.shakeBox, entity -> true)) {
                livingEntity.addVelocity(shakeDirection);
                livingEntity.velocityModified = true;
            }
        }
    }

    private Optional<RecipeEntry<ShakingTableRecipe>> getCurrentRecipe(ShakingTableRecipeInput recipeInput) {
        if (this.world == null || !(this.world instanceof ServerWorld serverWorld))
            return Optional.empty();

        return serverWorld.getRecipeManager().getFirstMatch(RecipeTypeInit.SHAKING_TABLE, recipeInput, this.world);
    }

    private ShakingTableRecipeInput createRecipeInput() {
        return new ShakingTableRecipeInput(getInputInventory(), getInputFluidTank().amount);
    }

    private boolean hasEnergy(ShakingTableRecipe recipe) {
        return getEnergyStorage().amount >= recipe.frequency() * 50L;
    }

    private void extractEnergy(ShakingTableRecipe recipe) {
        getEnergyStorage().amount -= recipe.frequency() * 50L;
    }

    @Override
    protected void writeData(WriteView view) {

        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            view.put("CurrentRecipe", RegistryKey.createCodec(RegistryKeys.RECIPE), this.currentRecipeId);
        }

        view.putInt("RecipeFrequency", this.recipeFrequency);
        ViewUtils.putChild(view, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.putChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.putChild(view, "SlurryTank", this.wrappedSlurryStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
        if (!this.outputItemStack.isEmpty()) {
            view.put("OutputStack", ItemStack.CODEC, this.outputItemStack);
        }

        if (!this.outputSlurryStack.isEmpty()) {
            view.put("OutputSlurry", SlurryStack.CODEC.codec(), this.outputSlurryStack);
        }

        Multiblockable.write(this, view);
    }

    @Override
    protected void readData(ReadView view) {
        this.progress = view.getInt("Progress", 0);
        this.maxProgress = view.getInt("MaxProgress", 0);
        this.currentRecipeId = view.read("CurrentRecipe", RegistryKey.createCodec(RegistryKeys.RECIPE)).orElse(null);
        this.recipeFrequency = view.getInt("RecipeFrequency", 0);

        ViewUtils.readChild(view, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "SlurryTank", this.wrappedSlurryStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);

        this.outputItemStack = view.read("OutputStack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.outputSlurryStack = view.read("OutputSlurry", SlurryStack.CODEC.codec()).orElse(SlurryStack.EMPTY);
        Multiblockable.read(this, view);
    }

    @Override
    public WrappedInventoryStorage<?> getWrappedInventoryStorage() {
        return this.wrappedInventoryStorage;
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.SHAKING_TABLE;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        if (this.world == null)
            return List.of();

        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();

        boolean isNorthSouth = facing == Direction.NORTH || facing == Direction.SOUTH;
        for (int z = (isNorthSouth ? -2 : -1); z <= (isNorthSouth ? 2 : 1); z++) {
            for (int x = (isNorthSouth ? -1 : -2); x <= (isNorthSouth ? 1 : 2); x++) {
                for (int y = 0; y <= 1; y++) {
                    if (x == 0 && z == 0 && y == 0)
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
    public List<PositionedPortRule> getPortRules() {
        return PORT_RULES;
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
        return new ShakingTableScreenHandler(syncId, playerInventory, this, this.wrappedInventoryStorage, this.properties);
    }

    public int getRecipeFrequency() {
        return this.recipeFrequency;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
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