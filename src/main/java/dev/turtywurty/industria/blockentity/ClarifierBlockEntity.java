package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.*;
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
import dev.turtywurty.industria.multiblock.old.Multiblockable;
import dev.turtywurty.industria.multiblock.old.PositionedPortRule;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.ClarifierRecipe;
import dev.turtywurty.industria.recipe.input.ClarifierRecipeInput;
import dev.turtywurty.industria.screenhandler.ClarifierScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClarifierBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityContentsDropper, AutoMultiblockable, BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("clarifier");

    private static final List<PositionedPortRule> PORT_RULES = List.of(
            PositionedPortRule.when(p -> p.isCenterColumn() && p.y() == 1)
                    .on(LocalDirection.UP)
                    .types(PortType.input(TransferType.FLUID))
                    .build(),

            PositionedPortRule.when(p -> p.y() == 0 && p.z() == -1 && p.x() == 0)
                    .on(LocalDirection.FRONT)
                    .types(PortType.output(TransferType.FLUID))
                    .build(),

            PositionedPortRule.when(p -> p.y() == 0 && p.z() == 1 && p.x() == 0)
                    .on(LocalDirection.BACK)
                    .types(PortType.output(TransferType.ITEM))
                    .build()
    );

    private final WrappedFluidStorage<SingleFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();
    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();

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
    private ItemStack outputItemStack = ItemStack.EMPTY;
    private FluidStack outputFluidStack = FluidStack.EMPTY;
    private ItemStack nextOutputItemStack = ItemStack.EMPTY; // Used for rendering

    public ClarifierBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.CLARIFIER, BlockEntityTypeInit.CLARIFIER, pos, state);

        this.wrappedFluidStorage.addStorage(new InputFluidStorage(this, FluidConstants.BUCKET * 5), Direction.UP);

        this.wrappedFluidStorage.addStorage(new OutputFluidStorage(this, FluidConstants.BUCKET * 5), Direction.NORTH);
        this.wrappedContainerStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.SOUTH);
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
        SyncingFluidStorage inputFluidTank = getInputFluidTank();
        SyncingFluidStorage outputFluidTank = getOutputFluidTank();
        SyncingSimpleInventory outputInventory = getOutputInventory();

        return List.of(inputFluidTank, outputFluidTank, outputInventory);
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
            Optional<RecipeHolder<ClarifierRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
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

        Optional<RecipeHolder<ClarifierRecipe>> recipeEntry = getCurrentRecipe(recipeInput);
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
            this.outputItemStack = recipe.assemble(recipeInput, this.level.registryAccess());
            this.outputFluidStack = recipe.outputFluidStack();
            inputFluidStorage.amount -= recipe.inputFluid().amount();

            this.currentRecipeId = null;
            this.maxProgress = 0;
            this.progress = 0;
            this.nextOutputItemStack = ItemStack.EMPTY;
            update();
        } else {
            this.progress++;
            this.nextOutputItemStack = recipe.assemble(recipeInput, this.level.registryAccess());
            update();
        }
    }

    private Optional<RecipeHolder<ClarifierRecipe>> getCurrentRecipe(ClarifierRecipeInput recipeInput) {
        if (this.level == null || !(this.level instanceof ServerLevel serverWorld))
            return Optional.empty();

        return serverWorld.recipeAccess().getRecipeFor(RecipeTypeInit.CLARIFIER, recipeInput, this.level);
    }

    public SyncingSimpleInventory getOutputInventory() {
        return (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(0);
    }

    public SyncingFluidStorage getInputFluidTank() {
        return (SyncingFluidStorage) this.wrappedFluidStorage.getStorage(Direction.UP);
    }

    public SyncingFluidStorage getOutputFluidTank() {
        return (SyncingFluidStorage) this.wrappedFluidStorage.getStorage(Direction.NORTH);
    }

    public ContainerStorage getInventoryProvider(Direction side) {
        return this.wrappedContainerStorage.getStorage(side);
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
    public List<PositionedPortRule> getPortRules() {
        return PORT_RULES;
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(view, "FluidTank", this.wrappedFluidStorage);
        Multiblockable.write(this, view);
        view.putInt("Progress", this.progress);
        view.putInt("MaxProgress", this.maxProgress);

        if (this.currentRecipeId != null) {
            view.store("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        if (!this.outputItemStack.isEmpty()) {
            view.store("OutputStack", ItemStack.CODEC, this.outputItemStack);
        }

        if (!this.outputFluidStack.isEmpty()) {
            view.store("OutputFluid", FluidStack.CODEC.codec(), this.outputFluidStack);
        }

        view.store("NextOutputStack", ItemStack.OPTIONAL_CODEC, this.nextOutputItemStack);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "FluidTank", this.wrappedFluidStorage);
        Multiblockable.read(this, view);

        this.progress = view.getIntOr("Progress", 0);

        this.maxProgress = view.getIntOr("MaxProgress", 0);

        this.currentRecipeId = view.read("CurrentRecipe", ResourceKey.codec(Registries.RECIPE))
                .orElse(null);

        this.outputItemStack = view.read("OutputStack", ItemStack.CODEC)
                .orElse(ItemStack.EMPTY);

        this.outputFluidStack = view.read("OutputFluid", FluidStack.CODEC.codec())
                .orElse(FluidStack.EMPTY);

        this.nextOutputItemStack = view.read("NextOutputStack", ItemStack.OPTIONAL_CODEC)
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.CLARIFIER;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        // 3x3x2 (3 wide, 3 long, 2 high)
        if (this.level == null)
            return List.of();

        List<BlockPos> positions = new ArrayList<>();
        List<BlockPos> invalidPositions = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 1; y++) {
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
        return new ClarifierScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage, this.properties);
    }
}