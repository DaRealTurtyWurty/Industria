package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.fluid.InputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.blockentity.util.slurry.OutputSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.blockentity.util.slurry.SyncingSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.WrappedSlurryStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.menu.ShakingTableScreenHandler;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.ShakingTableRecipe;
import dev.turtywurty.industria.recipe.input.ShakingTableRecipeInput;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.TransferUtils;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.multiblocklib.port.PortRegistrar;
import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.slurryapi.api.storage.SingleSlurryStorage;
import dev.turtywurty.slurryapi.api.storage.SlurryStorage;
import dev.turtywurty.turtymultiloader.transfer.lookup.MutableItemContext;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static dev.turtywurty.industria.blockentity.util.StorageOperations.extract;
import static dev.turtywurty.industria.blockentity.util.StorageOperations.insert;

public class ShakingTableBlockEntity extends IndustriaMultiblockControllerBlockEntity implements BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("shaking_table");

    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedFluidStorage<SyncingFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedSlurryStorage<SingleSlurryStorage> wrappedSlurryStorage = new WrappedSlurryStorage<>();

    private final AABB shakeBox;
    private int progress, maxProgress;
    private final ContainerData properties = new ContainerData() {
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
        public int getCount() {
            return 2;
        }
    };
    private ResourceKey<Recipe<?>> currentRecipeId;
    private int recipeFrequency;
    private ItemStack outputItemStack = ItemStack.EMPTY;
    private SlurryStack outputSlurryStack = SlurryStack.EMPTY;

    public ShakingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SHAKING_TABLE.get(), ModBlockEntityTypes.SHAKING_TABLE.get(), pos, state);

        this.wrappedContainerStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.UP);
        this.wrappedContainerStorage.addExtractOnlyInventory(new OutputSimpleInventory(this, 1), Direction.DOWN);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createFluidPredicate(() -> {
                    SyncingFluidStorage inputFluidTank = getInputFluidTank();
                    return new FluidStack(inputFluidTank.getResource(), inputFluidTank.getAmount());
                })), Direction.NORTH);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createEmptySlurryPredicate(() -> getOutputSlurryTank().getResource())), Direction.SOUTH);
        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidAmounts.BUCKET * 10, variant -> variant.value() == Fluids.WATER), Direction.UP);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 100_000, 10_000, 0));
        this.wrappedSlurryStorage.addStorage(new OutputSlurryStorage(this, FluidAmounts.BUCKET * 10), Direction.DOWN);

        this.shakeBox = createShakeBox();
    }

    public AABB createShakeBox() {
        Vec3 topCenter = this.worldPosition.getBottomCenter().add(0, 1, 0);
        float x1 = -18 / 16f;
        float y1 = 4 / 16f;
        float z1 = -19 / 16f;

        float x2 = 18 / 16f;
        float y2 = y1 + 7 / 16f;
        float z2 = 35 / 16f;

        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

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

        return new AABB(
                topCenter.x() + dx1,
                topCenter.y() + y1,
                topCenter.z() + dz1,
                topCenter.x() + dx2,
                topCenter.y() + y2,
                topCenter.z() + dz2
        );
    }

    @Override
    public Block getBlock() {
        return this.blockRef;
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncableStorage ContainerStorage = getInputInventory();
        SyncableStorage outputContainerStorage = getOutputInventory();
        SyncableStorage bucketInputInventory = getBucketInputInventory();
        SyncableStorage bucketOutputInventory = getBucketOutputInventory();
        SyncableStorage inputFluidTank = getInputFluidTank();
        SyncableStorage outputSlurryTank = getOutputSlurryTank();
        SyncableStorage energyStorage = getEnergyStorage();
        return List.of(ContainerStorage, outputContainerStorage, bucketInputInventory, bucketOutputInventory,
                inputFluidTank, outputSlurryTank,
                energyStorage);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        SyncingSimpleInventory bucketInputInventory = getBucketInputInventory();
        if (!bucketInputInventory.isEmpty()) {
            ItemStack bucket = bucketInputInventory.getItem(0);
            ResourceStorage<ResourceVariant<Fluid>> storage =
                    MutableItemContext.ofContainerSlot(bucketInputInventory, 0).find(StorageKeys.FLUID);
            if (storage != null && storage.supportsExtraction()) {
                SyncingFluidStorage inputFluidTank = getInputFluidTank();
                Optional<ResourceVariant<Fluid>> optVariant = TransferUtils.findFirstVariant(storage, inputFluidTank.getResource());
                optVariant.filter(variant -> !variant.isBlank()).ifPresent(variant -> {
                    try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                        long extracted = storage.extract(variant, FluidAmounts.BUCKET, transaction);
                        if (extracted > 0) {
                            inputFluidTank.insertInternal(variant, extracted, transaction);
                        }

                        transaction.commit();
                    }
                });
            }
        }

        SyncingSimpleInventory bucketOutputInventory = getBucketOutputInventory();
        if (!bucketOutputInventory.isEmpty()) {
            ItemStack bucket = bucketOutputInventory.getItem(0);
            ResourceStorage<ResourceVariant<Slurry>> storage =
                    MutableItemContext.ofContainerSlot(bucketOutputInventory, 0).find(SlurryStorage.KEY);
            if (storage != null && storage.supportsInsertion()) {
                SyncingSlurryStorage outputSlurryTank = getOutputSlurryTank();
                if (outputSlurryTank.getAmount() > 0) {
                    try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                        long inserted = storage.insert(outputSlurryTank.getResource(), FluidAmounts.BUCKET, transaction);
                        if (inserted > 0) {
                            outputSlurryTank.extractInternal(outputSlurryTank.getResource(), inserted, transaction);
                        }

                        transaction.commit();
                    }
                }
            }
        }

        if (!this.outputItemStack.isEmpty()) {
            SyncingSimpleInventory outputInventory = getOutputInventory();
            if (outputInventory.canAddItem(this.outputItemStack)) {
                this.outputItemStack = outputInventory.addItem(this.outputItemStack);
                update();
            }

            return;
        }

        if (!this.outputSlurryStack.isEmpty()) {
            SyncingSlurryStorage outputSlurryTank = getOutputSlurryTank();
            if (Objects.equals(outputSlurryTank.getResource(), this.outputSlurryStack.variant()) && outputSlurryTank.getCapacity() - outputSlurryTank.getAmount() >= 0) {
                long inserted = Math.min(outputSlurryTank.getCapacity() - outputSlurryTank.getAmount(), this.outputSlurryStack.amount());
                insert(outputSlurryTank, this.outputSlurryStack.variant(), inserted);
                this.outputSlurryStack = this.outputSlurryStack.withAmount(this.outputSlurryStack.amount() - inserted);
                update();
            }

            return;
        }

        ShakingTableRecipeInput recipeInput = createRecipeInput();
        if (this.currentRecipeId == null) {
            Optional<RecipeHolder<ShakingTableRecipe>> recipeEntryOpt = getCurrentRecipe(recipeInput);
            if (recipeEntryOpt.isPresent()) {
                RecipeHolder<ShakingTableRecipe> recipeEntry = recipeEntryOpt.get();
                this.currentRecipeId = recipeEntry.id();

                ShakingTableRecipe recipe = recipeEntry.value();
                this.recipeFrequency = recipe.frequency();
                this.maxProgress = recipe.processTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeHolder<ShakingTableRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
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
                consumeEnergy(recipe);
                getInputInventory().getItem(0).shrink(recipe.input().stackData().count());

                ItemStack output = recipe.assemble(recipeInput);
                SyncingFluidStorage inputFluidTank = getInputFluidTank();
                extract(inputFluidTank, inputFluidTank.getResource(), FluidAmounts.BUCKET * 2);

                SyncingSimpleInventory outputInventory = getOutputInventory();
                SyncingSlurryStorage outputSlurryTank = getOutputSlurryTank();

                this.progress = 0;
                this.maxProgress = 0;
                this.currentRecipeId = null;

                if (outputInventory.canAddItem(output)) {
                    this.outputItemStack = outputInventory.addItem(output);
                } else {
                    this.outputItemStack = output;
                }

                SlurryStack outputSlurry = recipe.outputSlurry();
                if (outputSlurryTank.canInsert(outputSlurry)) {
                    long inserted = Math.min(outputSlurryTank.getCapacity() - outputSlurryTank.getAmount(), outputSlurry.amount());
                    insert(outputSlurryTank, outputSlurry.variant(), inserted);
                    outputSlurry = outputSlurry.withAmount(outputSlurry.amount() - inserted);
                }

                this.outputSlurryStack = outputSlurry;

                update();
            }
        } else {
            if (hasEnergy(recipe)) {
                this.progress++;
                consumeEnergy(recipe);
                update();
            }
        }

        if (this.progress < this.maxProgress) {
            float shakesPerTick = this.recipeFrequency / 20f; // Convert frequency to shakes per tick
            int sign = (((this.progress & 1) == 0) ? 1 : -1); // Alternate shake direction every tick
            float amountToShake = shakesPerTick * sign;

            Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            Vec3 shakeDirection = facing.getAxis() == Direction.Axis.X
                    ? new Vec3(amountToShake, 0, 0)
                    : new Vec3(0, 0, amountToShake);

            for (LivingEntity livingEntity : this.level.getEntitiesOfClass(LivingEntity.class, this.shakeBox, entity -> true)) {
                livingEntity.push(shakeDirection);
                livingEntity.needsSync = true;
            }
        }
    }

    private Optional<RecipeHolder<ShakingTableRecipe>> getCurrentRecipe(ShakingTableRecipeInput recipeInput) {
        if (this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return Optional.empty();

        return serverWorld.recipeAccess().getRecipeFor(ModRecipeTypes.SHAKING_TABLE.get(), recipeInput, this.level);
    }

    private ShakingTableRecipeInput createRecipeInput() {
        return new ShakingTableRecipeInput(getInputInventory(), getInputFluidTank().getAmount());
    }

    private boolean hasEnergy(ShakingTableRecipe recipe) {
        return getEnergyStorage().getAmount() >= recipe.frequency() * 50L;
    }

    private void consumeEnergy(ShakingTableRecipe recipe) {
        extract(getEnergyStorage(), recipe.frequency() * 50L);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {

        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            view.store("CurrentRecipe", ResourceKey.codec(Registries.RECIPE), this.currentRecipeId);
        }

        view.putInt("RecipeFrequency", this.recipeFrequency);
        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.putChild(view, "SlurryTank", this.wrappedSlurryStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
        if (!this.outputItemStack.isEmpty()) {
            view.store("OutputStack", ItemStack.CODEC, this.outputItemStack);
        }

        if (!this.outputSlurryStack.isEmpty()) {
            view.store("OutputSlurry", SlurryStack.CODEC.codec(), this.outputSlurryStack);
        }

        super.saveAdditional(view);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        this.progress = view.getIntOr("Progress", 0);
        this.maxProgress = view.getIntOr("MaxProgress", 0);
        this.currentRecipeId = view.read("CurrentRecipe", ResourceKey.codec(Registries.RECIPE)).orElse(null);
        this.recipeFrequency = view.getIntOr("RecipeFrequency", 0);

        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "SlurryTank", this.wrappedSlurryStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);

        this.outputItemStack = view.read("OutputStack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.outputSlurryStack = view.read("OutputSlurry", SlurryStack.CODEC.codec()).orElse(SlurryStack.EMPTY);
        super.loadAdditional(view);
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
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new ShakingTableScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage, this.properties);
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
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(0);
    }

    public SyncingSimpleInventory getOutputInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(1);
    }

    public SyncingSimpleInventory getBucketInputInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(2);
    }

    public SyncingSimpleInventory getBucketOutputInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(3);
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

    public ResourceStorage<ResourceVariant<Item>> getInventoryProvider(Direction side) {
        return this.wrappedContainerStorage.getStorage(side);
    }

    public SyncingFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public SingleSlurryStorage getSlurryProvider(Direction side) {
        return this.wrappedSlurryStorage.getStorage(side);
    }

    public ResourceStorage<ResourceVariant<UnitResource>> getEnergyProvider(Direction side) {
        return this.wrappedEnergyStorage.getStorage(side);
    }

    @Override
    protected void definePorts(PortRegistrar ports) {
        ports.both(TransferType.ITEM, this.wrappedContainerStorage::getCombinedStorage)
                .wherePosition(offset -> offset.getY() == 0 || offset.getY() == 1);
        ports.output(TransferType.ITEM, () -> this.wrappedContainerStorage.getStorage(Direction.DOWN))
                .wherePosition(offset -> offset.getY() == 0);
        ports.input(TransferType.FLUID, this::getInputFluidTank)
                .wherePosition(offset -> offset.getY() == 1);
        ports.output(TransferType.SLURRY, this::getOutputSlurryTank)
                .wherePosition(offset -> offset.getY() == 0);
        ports.input(TransferType.ENERGY, this::getEnergyStorage)
                .wherePosition(offset -> offset.getY() == 0);
    }
}
