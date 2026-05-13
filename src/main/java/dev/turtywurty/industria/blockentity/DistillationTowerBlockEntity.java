package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.*;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.DistillationTowerRecipe;
import dev.turtywurty.industria.recipe.input.DistillationTowerRecipeInput;
import dev.turtywurty.industria.screenhandler.DistillationTowerScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;
import java.util.Optional;

public class DistillationTowerBlockEntity extends IndustriaMultiblockControllerBlockEntity implements BlockEntityContentsDropper, BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("distillation_tower");

    private final WrappedContainerStorage<?> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();

    private ResourceKey<Recipe<?>> currentRecipeId;
    private int progress;
    private int maxProgress;
    private FluidStack leftoverPrimaryOutput = FluidStack.EMPTY;
    private FluidStack leftoverSecondaryOutput = FluidStack.EMPTY;
    private final ContainerData properties = new ContainerData() {
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
        public int getCount() {
            return 2;
        }
    };

    public DistillationTowerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.DISTILLATION_TOWER, BlockEntityTypeInit.DISTILLATION_TOWER, pos, state);

        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidConstants.BUCKET * 5), Direction.UP);
        this.wrappedFluidStorage.addStorage(new OutputFluidStorage(this, FluidConstants.BUCKET * 5), Direction.NORTH);
        this.wrappedFluidStorage.addStorage(new OutputFluidStorage(this, FluidConstants.BUCKET * 5), Direction.SOUTH);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 100_000, 1_000, 0));
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of(getInputFluidTank(), getPrimaryOutputFluidTank(), getSecondaryOutputFluidTank(), getEnergyStorage());
    }

    @Override
    public void onTick() {
        if (!(this.level instanceof ServerLevel serverLevel))
            return;

        processOutputs();
        if (hasLeftovers())
            return;

        SyncingFluidStorage inputFluidTank = getInputFluidTank();
        DistillationTowerRecipeInput recipeInput = new DistillationTowerRecipeInput(new FluidStack(inputFluidTank.variant, inputFluidTank.amount));
        if (this.currentRecipeId == null) {
            Optional<RecipeHolder<DistillationTowerRecipe>> recipeEntry = getCurrentRecipe(serverLevel, recipeInput);
            if (recipeEntry.isPresent()) {
                this.currentRecipeId = recipeEntry.get().id();
                this.maxProgress = recipeEntry.get().value().processTime();
                this.progress = 0;
                update();
            }

            return;
        }

        Optional<RecipeHolder<DistillationTowerRecipe>> recipeEntry = getCurrentRecipe(serverLevel, recipeInput);
        if (recipeEntry.isEmpty() || !recipeEntry.get().id().equals(this.currentRecipeId)) {
            resetProgress();
            update();
            return;
        }

        DistillationTowerRecipe recipe = recipeEntry.get().value();
        if (this.progress >= this.maxProgress) {
            this.leftoverPrimaryOutput = recipe.primaryOutputFluid();
            this.leftoverSecondaryOutput = recipe.secondaryOutputFluid();
            inputFluidTank.amount -= recipe.inputFluid().amount();

            resetProgress();
            update();
        } else {
            SyncingEnergyStorage energyStorage = getEnergyStorage();
            if (energyStorage.amount >= recipe.energyCost()) {
                energyStorage.amount -= recipe.energyCost();
                this.progress++;
                update();
            }
        }
    }

    private void processOutputs() {
        insertLeftover(getPrimaryOutputFluidTank(), true);
        insertLeftover(getSecondaryOutputFluidTank(), false);
    }

    private void insertLeftover(SyncingFluidStorage outputTank, boolean primary) {
        FluidStack leftover = primary ? this.leftoverPrimaryOutput : this.leftoverSecondaryOutput;
        if (leftover.isEmpty() || !outputTank.canInsert(leftover))
            return;

        long inserted = Math.min(outputTank.getCapacity() - outputTank.amount, leftover.amount());
        outputTank.variant = leftover.variant();
        outputTank.amount += inserted;

        FluidStack remaining = leftover.withAmount(leftover.amount() - inserted);
        if (primary) {
            this.leftoverPrimaryOutput = remaining;
        } else {
            this.leftoverSecondaryOutput = remaining;
        }

        update();
    }

    private boolean hasLeftovers() {
        return !this.leftoverPrimaryOutput.isEmpty() || !this.leftoverSecondaryOutput.isEmpty();
    }

    private void resetProgress() {
        this.currentRecipeId = null;
        this.progress = 0;
        this.maxProgress = 0;
    }

    private Optional<RecipeHolder<DistillationTowerRecipe>> getCurrentRecipe(ServerLevel serverLevel, DistillationTowerRecipeInput recipeInput) {
        return serverLevel.recipeAccess().getRecipeFor(RecipeTypeInit.DISTILLATION_TOWER, recipeInput, serverLevel);
    }

    public SyncingFluidStorage getInputFluidTank() {
        return (SyncingFluidStorage) this.wrappedFluidStorage.getStorage(0);
    }

    public SyncingFluidStorage getPrimaryOutputFluidTank() {
        return (SyncingFluidStorage) this.wrappedFluidStorage.getStorage(1);
    }

    public SyncingFluidStorage getSecondaryOutputFluidTank() {
        return (SyncingFluidStorage) this.wrappedFluidStorage.getStorage(2);
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(0);
    }

    public SingleFluidStorage getFluidProvider(Direction side) {
        return this.wrappedFluidStorage.getStorage(side);
    }

    public EnergyStorage getEnergyProvider(Direction side) {
        return this.wrappedEnergyStorage.getStorage(side);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ViewUtils.putChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.putChild(view, "EnergyStorage", this.wrappedEnergyStorage);
        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            view.store("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        if (!this.leftoverPrimaryOutput.isEmpty()) {
            view.store("PrimaryLeftoverOutput", FluidStack.CODEC.codec(), this.leftoverPrimaryOutput);
        }

        if (!this.leftoverSecondaryOutput.isEmpty()) {
            view.store("SecondaryLeftoverOutput", FluidStack.CODEC.codec(), this.leftoverSecondaryOutput);
        }
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);
        ViewUtils.readChild(view, "EnergyStorage", this.wrappedEnergyStorage);
        this.progress = view.getIntOr("Progress", 0);
        this.maxProgress = view.getIntOr("MaxProgress", 0);
        this.currentRecipeId = view.read("CurrentRecipe", ResourceKey.codec(Registries.RECIPE)).orElse(null);
        this.leftoverPrimaryOutput = view.read("PrimaryLeftoverOutput", FluidStack.CODEC.codec()).orElse(FluidStack.EMPTY);
        this.leftoverSecondaryOutput = view.read("SecondaryLeftoverOutput", FluidStack.CODEC.codec()).orElse(FluidStack.EMPTY);
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
    protected @Nullable Storage<ItemVariant> getItemStorageForExternal(BlockPos worldPos, BlockPos localOffset) {
        return null;
    }

    @Override
    protected @Nullable Storage<FluidVariant> getFluidStorageForExternal(BlockPos worldPos, BlockPos localOffset) {
        if (localOffset.getX() == 1 && localOffset.getY() == 6 && localOffset.getZ() == 1)
            return getInputFluidTank();

        if (localOffset.getX() == 1 && localOffset.getY() == 1 && localOffset.getZ() == 0)
            return getPrimaryOutputFluidTank();

        return localOffset.getX() == 1 && localOffset.getY() == 1 && localOffset.getZ() == 2
                ? getSecondaryOutputFluidTank()
                : null;
    }

    @Override
    protected @Nullable EnergyStorage getEnergyStorageForExternal(BlockPos worldPos, BlockPos localOffset) {
        return localOffset.getX() == 1 && localOffset.getY() == 1 && localOffset.getZ() == 1
                ? getEnergyStorage()
                : null;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new DistillationTowerScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage, this.properties);
    }
}
