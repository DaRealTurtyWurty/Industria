package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.fluid.OutputFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.menu.BlastFurnaceMenu;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.BlastFurnaceRecipe;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.FuelUtils;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.multiblocklib.port.PortRegistrar;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public class BlastFurnaceBlockEntity extends IndustriaMultiblockControllerBlockEntity
    implements BlockEntityContentsDropper, BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("blast_furnace");

    private final WrappedContainerStorage<SyncingSimpleInventory> wrappedInventoryStorage = new WrappedContainerStorage<>();
    private final WrappedFluidStorage<SyncingFluidStorage> wrappedFluidStorage = new WrappedFluidStorage<>();

    private ResourceKey<Recipe<?>> currentRecipeId;
    private int progress;
    private int processTime;
    private int burnTimeRemaining;
    private int burnTimeTotal;

    public BlastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BLAST_FURNACE.get(), ModBlockEntityTypes.BLAST_FURNACE.get(), pos, state);
        this.wrappedInventoryStorage.addInsertOnlyInventory(new SyncingSimpleInventory(this, 2), Direction.UP);
        this.wrappedInventoryStorage.addInsertOnlyInventory(new PredicateSimpleInventory(this, 1,
            (stack, _) -> stack.is(ModItems.COAL_COKE.get())), Direction.NORTH);
        this.wrappedInventoryStorage.addExtractOnlyInventory(new SyncingSimpleInventory(this, 1), Direction.DOWN);
        this.wrappedFluidStorage.addStorage(new OutputFluidStorage(this, FluidAmounts.BUCKET * 4));
    }

    @Override
    public void onTick() {
        if (!(this.level instanceof ServerLevel serverLevel))
            return;

        SyncingSimpleInventory inputInventory = this.wrappedInventoryStorage.getInventory(Direction.UP);
        Optional<RecipeHolder<BlastFurnaceRecipe>> recipeMatch = serverLevel.recipeAccess().getRecipeFor(
            ModRecipeTypes.BLAST_FURNACE.get(), inputInventory, serverLevel);

        if (recipeMatch.isEmpty()) {
            clearBatch();
            setLit(false);
            return;
        }

        RecipeHolder<BlastFurnaceRecipe> holder = recipeMatch.get();
        BlastFurnaceRecipe recipe = holder.value();
        this.processTime = recipe.processTime();
        if (!holder.id().equals(this.currentRecipeId)) {
            this.currentRecipeId = holder.id();
            this.progress = 0;
            setChanged();
        }

        SyncingSimpleInventory fuelInventory = this.wrappedInventoryStorage.getInventory(Direction.NORTH);
        SyncingSimpleInventory byproductInventory = this.wrappedInventoryStorage.getInventory(Direction.DOWN);
        SyncingFluidStorage fluidStorage = this.wrappedFluidStorage.getStorage(0);

        ItemStack byproduct = recipe.assemble(inputInventory);
        ItemStack existingByproduct = byproductInventory.getItem(0);
        FluidStack molten = recipe.output();

        boolean byproductFits = (existingByproduct.isEmpty()
            || ItemStack.isSameItemSameComponents(existingByproduct, byproduct))
            && byproduct.getCount() <= byproductInventory.getMaxStackSize(byproduct) - existingByproduct.getCount();
        if (!hasConnectedBlower() || !byproductFits || molten.isEmpty() || !fluidStorage.canInsert(molten)) {
            setLit(false);
            return;
        }

        if (this.progress < recipe.processTime()) {
            if (this.burnTimeRemaining == 0) {
                ItemStack fuel = fuelInventory.getItem(0);
                int duration = fuel.is(ModItems.COAL_COKE.get())
                    ? FuelUtils.burnDuration(serverLevel, fuel)
                    : 0;

                if (duration <= 0) {
                    setLit(false);
                    return;
                }

                fuelInventory.removeItem(0, 1);
                this.burnTimeRemaining = duration;
                this.burnTimeTotal = duration;
            }

            this.burnTimeRemaining--;
            this.progress++;
            setChanged();
        }

        setLit(true);
        if (this.progress < recipe.processTime())
            return;

        int oreSlot = recipe.findOreSlot(inputInventory);
        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            long inserted = fluidStorage.insertInternal(molten.variant(), molten.amount(), transaction);
            if (inserted != molten.amount())
                return;

            transaction.commit();
        }

        inputInventory.removeItem(oreSlot, recipe.input().stackData().count());
        inputInventory.removeItem(1 - oreSlot, recipe.flux().stackData().count());
        byproductInventory.addItem(byproduct);
        clearBatch();
    }

    private void clearBatch() {
        this.processTime = 0;
        if (this.currentRecipeId != null || this.progress != 0) {
            this.currentRecipeId = null;
            this.progress = 0;
            setChanged();
        }
    }

    private void setLit(boolean lit) {
        BlockState state = getBlockState();
        if (state.getValue(BlockStateProperties.LIT) != lit) {
            this.level.setBlockAndUpdate(this.worldPosition,
                state.setValue(BlockStateProperties.LIT, lit));
        }
    }

    public boolean hasConnectedBlower() {
        if (this.level == null || !isFormed())
            return false;

        Direction facing = getControllerFacing();
        Direction blowerDirection = facing.getCounterClockWise();
        BlockPos blowerPos = this.worldPosition
            .relative(facing)
            .relative(blowerDirection, 2);
        if (!this.level.isLoaded(blowerPos))
            return false;

        BlockState blowerState = this.level.getBlockState(blowerPos);
        return blowerState.is(ModBlocks.HOT_AIR_BLOWER.get())
            && blowerState.getValue(BlockStateProperties.HORIZONTAL_FACING) == blowerDirection.getOpposite();
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        ViewUtils.putChild(output, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.putChild(output, "FluidInventory", this.wrappedFluidStorage);
        output.putInt("Progress", this.progress);
        if (this.currentRecipeId != null) {
            output.store("CurrentRecipe", RECIPE_CODEC, this.currentRecipeId);
        }

        output.putInt("BurnTimeRemaining", this.burnTimeRemaining);
        output.putInt("BurnTimeTotal", this.burnTimeTotal);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ViewUtils.readChild(input, "Inventory", this.wrappedInventoryStorage);
        ViewUtils.readChild(input, "FluidInventory", this.wrappedFluidStorage);

        this.currentRecipeId = input.read("CurrentRecipe", RECIPE_CODEC).orElse(null);
        int savedBurnTime = input.getIntOr("BurnTimeRemaining", -1);
        this.burnTimeRemaining = Math.max(0, savedBurnTime);
        this.burnTimeTotal = Math.max(this.burnTimeRemaining, input.getIntOr("BurnTimeTotal", 0));
        this.progress = this.currentRecipeId == null || savedBurnTime < 0
            ? 0
            : Math.max(0, input.getIntOr("Progress", 0));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingSimpleInventory inputInventory = this.wrappedInventoryStorage.getInventory(Direction.UP);
        SyncingSimpleInventory fuelInventory = this.wrappedInventoryStorage.getInventory(Direction.NORTH);
        SyncingSimpleInventory outputInventory = this.wrappedInventoryStorage.getInventory(Direction.DOWN);
        SyncingFluidStorage fluidStorage = this.wrappedFluidStorage.getStorage(0);
        return List.of(inputInventory, fuelInventory, outputInventory, fluidStorage);
    }

    @Override
    public Block getBlock() {
        return ModBlocks.BLAST_FURNACE.get();
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedInventoryStorage;
    }

    public SyncingFluidStorage getMoltenOutput() {
        return this.wrappedFluidStorage.getStorage(0);
    }

    public float getBatchProgress() {
        return this.processTime > 0 ? Math.clamp((float) this.progress / this.processTime, 0, 1) : 0;
    }

    public float getBurnProgress() {
        return this.burnTimeTotal > 0 ? (float) this.burnTimeRemaining / this.burnTimeTotal : 0;
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public BlockPosPayload getMenuOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public BlastFurnaceMenu createMenu(int syncId, Inventory inventory, Player player) {
        return new BlastFurnaceMenu(syncId, inventory, this);
    }

    @Override
    protected void definePorts(PortRegistrar ports) {
        ports.input(TransferType.ITEM, () -> this.wrappedInventoryStorage.getStorage(Direction.UP))
            .atSides(new BlockPos(0, 2, -1), Direction.UP);

        ports.input(TransferType.ITEM, () -> this.wrappedInventoryStorage.getStorage(Direction.NORTH))
            .atSides(new BlockPos(-1, 1, -1), Direction.WEST);

        ports.output(TransferType.ITEM, () -> this.wrappedInventoryStorage.getStorage(Direction.DOWN))
            .atSides(new BlockPos(-1, -1, -1), Direction.WEST);

        ports.output(TransferType.FLUID, this::getMoltenOutput)
            .atSides(new BlockPos(0, -1, 0), Direction.SOUTH);
    }
}
