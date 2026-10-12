package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.menu.FireboxMenu;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.util.FuelUtils;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class FireboxBlockEntity extends IndustriaBlockEntity
    implements
        BlockEntityContentsDropper,
        BlockEntityWithGui<BlockPosPayload>,
        TickableBlockEntity {
    public static final Component TITLE = Industria.containerTitle("firebox");

    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();

    private static final int FUEL_UNITS_PER_BURN_TICK = 9;

    private long remainingFuelUnits;
    private int fuelUnitsPerTick = FUEL_UNITS_PER_BURN_TICK;
    private long lastBurnTick = Long.MIN_VALUE;
    private int maxBurnTime;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> {
                    if (maxBurnTime <= 0)
                        yield 0;

                    yield (int) (Mth.clamp((remainingFuelUnits / ((float) maxBurnTime * FUEL_UNITS_PER_BURN_TICK)), 0,
                        1) * 1000);
                }
                case 1 -> isBurning() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public FireboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FIREBOX.get(), ModBlockEntityTypes.FIREBOX.get(), pos, state);
        this.wrappedContainerStorage.addInventory(
            new PredicateSimpleInventory(this, 3,
                (itemStack, _) -> isFuel(itemStack)));
    }

    @Override
    public void tick() {
        if (this.level == null || this.level.isClientSide())
            return;

        updateLitState();
    }

    private void updateLitState() {
        if (this.level == null || this.level.isClientSide())
            return;

        BlockState currentState = getBlockState();
        boolean isCurrentlyLit = currentState.getValue(BlockStateProperties.LIT);
        boolean shouldBeLit = isBurning();

        if (isCurrentlyLit != shouldBeLit) {
            this.level.setBlockAndUpdate(this.worldPosition,
                currentState.setValue(BlockStateProperties.LIT, shouldBeLit));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ViewUtils.putChild(output, "Inventory", this.wrappedContainerStorage);
        output.putLong("RemainingFuelUnits", this.remainingFuelUnits);
        output.putInt("FuelUnitsPerTick", this.fuelUnitsPerTick);
        output.putInt("MaxBurnTime", this.maxBurnTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.lastBurnTick = Long.MIN_VALUE;
        ViewUtils.readChild(input, "Inventory", this.wrappedContainerStorage);
        this.remainingFuelUnits = input.getLongOr("RemainingFuelUnits", 0L);
        this.fuelUnitsPerTick = Math.max(1, input.getIntOr("FuelUnitsPerTick", FUEL_UNITS_PER_BURN_TICK));
        this.maxBurnTime = input.getIntOr("MaxBurnTime", 0);
    }

    @Override
    public Block getBlock() {
        return ModBlocks.FIREBOX.get();
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    public SimpleContainer getInventory() {
        return this.wrappedContainerStorage.getInventory(0);
    }

    public ResourceStorage<ResourceVariant<Item>> getInventoryProvider(Direction direction) {
        return this.wrappedContainerStorage.getStorage(direction);
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
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
        return new FireboxMenu(syncId, inventory, this, this.wrappedContainerStorage, this.menuData);
    }

    public boolean isBurning() {
        if (this.level == null)
            return false;

        long gameTime = this.level.getGameTime();
        return this.lastBurnTick == gameTime || this.lastBurnTick == gameTime - 1;
    }

    public boolean burnForTick() {
        if (this.level == null || this.level.isClientSide())
            return false;

        long gameTime = this.level.getGameTime();
        if (this.lastBurnTick == gameTime)
            return true;

        while (!hasFuelEnergy()) {
            if (!tryStartFuel())
                return false;
        }

        this.remainingFuelUnits -= this.fuelUnitsPerTick;
        this.lastBurnTick = gameTime;
        setChanged();
        updateLitState();
        return true;
    }

    public boolean hasFuelEnergy() {
        return this.remainingFuelUnits > 0;
    }

    public int getBurnTime() {
        return (int) ((Math.max(0L, this.remainingFuelUnits) + FUEL_UNITS_PER_BURN_TICK - 1)
            / FUEL_UNITS_PER_BURN_TICK);
    }

    public int getMaxBurnTime() {
        return this.maxBurnTime;
    }

    public void setFuelSpaceSize(int size) {
        if (size < 1)
            throw new IllegalArgumentException("Fuel space size must be at least 1");

        int previousFuelUnitsPerTick = this.fuelUnitsPerTick;
        this.fuelUnitsPerTick = size;
        if (previousFuelUnitsPerTick != this.fuelUnitsPerTick) {
            setChanged();
        }
    }

    public boolean tryStartFuel() {
        if (this.level == null || this.level.isClientSide() || hasFuelEnergy())
            return false;

        SimpleContainer inventory = getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty())
                continue;

            int fuelValue = FuelUtils.burnDuration(this.level, stack);
            if (fuelValue <= 0)
                continue;

            ItemStack consumed = inventory.removeItem(slot, 1);
            ItemStackTemplate remainderItem = consumed.getItem().getCraftingRemainder();
            ItemStack remainderStack = remainderItem == null ? ItemStack.EMPTY : remainderItem.create();
            storeFuelRemainder(remainderStack);
            this.remainingFuelUnits += (long) fuelValue * FUEL_UNITS_PER_BURN_TICK;
            this.maxBurnTime = fuelValue;
            setChanged();
            updateLitState();
            return true;
        }

        return false;
    }

    private boolean isFuel(ItemStack stack) {
        if (this.level == null)
            return false;

        return FuelUtils.isFuel(stack);
    }

    private void storeFuelRemainder(ItemStack remainder) {
        if (remainder == null || remainder.isEmpty())
            return;

        SimpleContainer inventory = getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, remainder))
                continue;

            int spaceLeft = inventory.getMaxStackSize(stack) - stack.getCount();
            if (spaceLeft <= 0)
                continue;

            int toAdd = Math.min(spaceLeft, remainder.getCount());
            stack.grow(toAdd);
            remainder.shrink(toAdd);
            inventory.setChanged();

            if (remainder.isEmpty())
                return;
        }

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty())
                continue;

            int toAdd = Math.min(inventory.getMaxStackSize(remainder), remainder.getCount());
            ItemStack newStack = remainder.split(toAdd);
            inventory.setItem(slot, newStack);
            if (remainder.isEmpty())
                return;
        }

        if (this.level != null) {
            Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(),
                this.worldPosition.getZ(), remainder);
        }
    }
}
