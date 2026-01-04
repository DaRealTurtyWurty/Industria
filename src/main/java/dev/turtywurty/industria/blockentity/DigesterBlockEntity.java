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
import dev.turtywurty.industria.blockentity.util.fluid.OutputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.blockentity.util.slurry.InputSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.blockentity.util.slurry.SyncingSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.WrappedSlurryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.multiblock.LocalDirection;
import dev.turtywurty.industria.multiblock.PortType;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockable;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import dev.turtywurty.industria.multiblock.old.Multiblockable;
import dev.turtywurty.industria.multiblock.old.PositionedPortRule;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.DigesterRecipe;
import dev.turtywurty.industria.recipe.input.DigesterRecipeInput;
import dev.turtywurty.industria.screenhandler.DigesterScreenHandler;
import dev.turtywurty.industria.util.TransferUtils;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO: Make this work with temperature and pressure
public class DigesterBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, AutoMultiblockable, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("digester");

    private static final List<PositionedPortRule> PORT_RULES = List.of(
            PositionedPortRule.when(p -> p.y() == 4 && p.isCenterColumn())
                    .on(LocalDirection.UP)
                    .types(PortType.input(TransferType.SLURRY))
                    .build(),

            PositionedPortRule.when(p -> p.z() == -1)
                    .on(LocalDirection.FRONT)
                    .types(PortType.input(TransferType.ENERGY))
                    .build(),

            PositionedPortRule.when(p -> p.y() == 0 && p.z() == 1)
                    .on(LocalDirection.BACK)
                    .types(PortType.output(TransferType.FLUID))
                    .build()
    );

    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final WrappedSlurryStorage<SingleSlurryStorage> wrappedSlurryStorage = new WrappedSlurryStorage<>();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();

    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    private ResourceKey<Recipe<?>> currentRecipeId;
    private int progress;
    private int maxProgress;

    private final ContainerData properties = new ContainerData() {
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
        public int getCount() {
            return 2;
        }
    };

    public DigesterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.DIGESTER, BlockEntityTypeInit.DIGESTER, pos, state);

        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1,
                PredicateSimpleInventory.createSlurryPredicate(() -> {
                    SyncingSlurryStorage inputSlurryTank = getInputSlurryStorage();
                    return new SlurryStack(inputSlurryTank.variant, inputSlurryTank.amount);
                })), Direction.UP);
        this.wrappedContainerStorage.addInventory(new PredicateSimpleInventory(this, 1,
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
        return (PredicateSimpleInventory) this.wrappedContainerStorage.getInventory(0);
    }

    public PredicateSimpleInventory getOutputFluidInventory() {
        return (PredicateSimpleInventory) this.wrappedContainerStorage.getInventory(1);
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
    public List<PositionedPortRule> getPortRules() {
        return PORT_RULES;
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        SyncingSimpleInventory bucketInputInventory = getInputSlurryInventory();
        if (!bucketInputInventory.isEmpty()) {
            ItemStack bucket = bucketInputInventory.getItem(0);
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
            ItemStack bucket = bucketOutputInventory.getItem(0);
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

        if (this.currentRecipeId == null) {
            Optional<RecipeHolder<DigesterRecipe>> recipeEntry = getCurrentRecipe();
            if (recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().processTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeHolder<DigesterRecipe>> recipeEntry = getCurrentRecipe();
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
            if (outputFluidStorage.canInsert(outputFluidStack) && hasEnergy()) {
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

    private Optional<RecipeHolder<DigesterRecipe>> getCurrentRecipe() {
        if (this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return Optional.empty();

        InputSlurryStorage slurryStorage = getInputSlurryStorage();
        return serverWorld.recipeAccess().getRecipeFor(RecipeTypeInit.DIGESTER, new DigesterRecipeInput(new SlurryStack(slurryStorage.variant, slurryStorage.amount)), this.level);
    }

    private boolean hasEnergy() {
        return getEnergyStorage().amount >= 100;
    }

    private void extractEnergy() {
        getEnergyStorage().amount -= 100;
        update();
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
        return new DigesterScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage, this.properties);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
        this.wrappedSlurryStorage.writeData(view.child("FluidTank"));
        this.wrappedFluidStorage.writeData(view.child("SlurryTank"));
        Multiblockable.write(this, view);
        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            view.store("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);
        ViewUtils.readChild(view, "SlurryTank", this.wrappedSlurryStorage);
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);
        Multiblockable.read(this, view);

        this.progress = view.getIntOr("Progress", 0);

        this.maxProgress = view.getIntOr("MaxProgress", 0);

        this.currentRecipeId = view.read("CurrentRecipe", ResourceKey.codec(Registries.RECIPE))
                .orElse(null);
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.DIGESTER;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        if (this.level == null)
            return List.of();

        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = 0; y < 5; y++) {
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
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
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