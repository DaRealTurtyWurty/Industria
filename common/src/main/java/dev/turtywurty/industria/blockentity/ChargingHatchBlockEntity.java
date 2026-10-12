package dev.turtywurty.industria.blockentity;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.inventory.ResizableSyncingInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.menu.ChargingHatchMenu;
import dev.turtywurty.industria.network.ChargingHatchOpenPayload;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ChargingHatchBlockEntity extends IndustriaBlockEntity
    implements
        BlockEntityContentsDropper,
        BlockEntityWithGui<ChargingHatchOpenPayload> {
    public static final Component TITLE = Industria.containerTitle("charging_hatch");

    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final ResizableSyncingInventory inventory;
    private BlockPos controllerPos;
    private double[] meltingProgress = new double[9];
    private int[] meltingDuration = new int[9];
    private ItemStack[] meltingInputs = new ItemStack[9];
    private int processingSlotCount;

    public ChargingHatchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CHARGING_HATCH.get(), ModBlockEntityTypes.CHARGING_HATCH.get(), pos, state);
        this.inventory = new ResizableSyncingInventory(this, 9,
            (owner, size) -> new SyncingSimpleInventory(owner, size) {
                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });

        this.inventory.setInsertionPredicate((slot, _) -> slot < this.processingSlotCount);
        this.wrappedContainerStorage.addInventory(this.inventory);

        Arrays.fill(this.meltingInputs, ItemStack.EMPTY);
    }

    public void setControllerPos(BlockPos controllerPos) {
        BlockPos oldControllerPos = this.controllerPos;
        this.controllerPos = controllerPos == null ? null : controllerPos.immutable();

        if (!Objects.equals(oldControllerPos, this.controllerPos)) {
            this.processingSlotCount = 0;
            resetMeltingProgress();
            setChanged();
        }
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public SmelteryControllerBlockEntity.SmelteryChamber getChamber() {
        if (this.controllerPos == null || this.level == null || !this.level.isLoaded(this.controllerPos))
            return null;

        if (!(this.level.getBlockEntity(this.controllerPos) instanceof SmelteryControllerBlockEntity controller))
            return null;

        return controller.getChamberForHatch(this.worldPosition);
    }

    private void ensureSlotCapacity(int requiredSize) {
        if (requiredSize <= this.inventory.getContainerSize())
            return;

        int oldSize = this.meltingInputs.length;

        this.meltingProgress = Arrays.copyOf(this.meltingProgress, requiredSize);
        this.meltingDuration = Arrays.copyOf(this.meltingDuration, requiredSize);
        this.meltingInputs = Arrays.copyOf(this.meltingInputs, requiredSize);
        Arrays.fill(this.meltingInputs, oldSize, requiredSize, ItemStack.EMPTY);

        this.inventory.ensureSize(requiredSize);
    }

    public void resetMeltingProgress(int slot) {
        this.meltingProgress[slot] = 0;
        this.meltingDuration[slot] = 0;
        this.meltingInputs[slot] = ItemStack.EMPTY;
        setChanged();
    }

    public void resetMeltingProgress() {
        for (int slot = 0; slot < this.meltingProgress.length; slot++) {
            resetMeltingProgress(slot);
        }
    }

    public void prepareMeltingSlot(int slot, int duration) {
        SimpleContainer inventory = getInventory();
        if (slot < 0 || slot >= inventory.getContainerSize())
            return;

        ItemStack stack = inventory.getItem(slot);
        if (stack.isEmpty()) {
            resetMeltingProgress(slot);
            return;
        }

        if (this.meltingInputs[slot].isEmpty()
            || !ItemStack.isSameItemSameComponents(stack, this.meltingInputs[slot])
            || this.meltingDuration[slot] != duration) {
            this.meltingInputs[slot] = stack.copyWithCount(1);
            this.meltingDuration[slot] = duration;
            this.meltingProgress[slot] = 0;
            setChanged();
        }
    }

    public double getMeltingProgress(int slot) {
        return this.meltingProgress[slot];
    }

    public int getMeltingDuration(int slot) {
        return this.meltingDuration[slot];
    }

    public int getProcessingSlotCount() {
        return this.processingSlotCount;
    }

    public void setProcessingSlotCount(int count) {
        count = Math.max(0, count);
        ensureSlotCapacity(count);
        if (this.processingSlotCount == count)
            return;

        this.processingSlotCount = count;
        for (int slot = Math.min(count, this.meltingProgress.length); slot < this.meltingProgress.length; slot++) {
            resetMeltingProgress(slot);
        }

        setChanged();
    }

    public void advanceMeltingProgress(int slot, double amount) {
        this.meltingProgress[slot] = Math.min(this.meltingProgress[slot] + amount, this.meltingDuration[slot]);
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("ControllerPos", BlockPos.CODEC, this.controllerPos);
        output.putInt("InventorySize", this.inventory.getContainerSize());
        ViewUtils.putChild(output, "Inventory", this.wrappedContainerStorage);
        output.store("MeltingProgress", Codec.DOUBLE.listOf(), Arrays.stream(this.meltingProgress).boxed().toList());
        output.putIntArray("MeltingDuration", this.meltingDuration);
        output.store("MeltingInputs", ItemStack.OPTIONAL_CODEC.listOf(), Arrays.asList(this.meltingInputs));
        output.putInt("ProcessingSlotCount", this.processingSlotCount);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.controllerPos = input.read("ControllerPos", BlockPos.CODEC).orElse(null);

        int savedSize = Math.max(9, input.getInt("InventorySize").orElse(9));
        int savedProcessingCount = Math.max(0, input.getInt("ProcessingSlotCount").orElse(0));
        ensureSlotCapacity(Math.max(savedSize, savedProcessingCount));
        ViewUtils.readChild(input, "Inventory", this.wrappedContainerStorage);

        List<Double> progress = input.read("MeltingProgress", Codec.DOUBLE.listOf()).orElse(List.of());
        int[] durations = input.getIntArray("MeltingDuration").orElse(new int[0]);
        List<ItemStack> inputs = input.read("MeltingInputs", ItemStack.OPTIONAL_CODEC.listOf())
            .orElse(List.of());

        for (int i = 0; i < this.meltingProgress.length; i++) {
            this.meltingDuration[i] = i < durations.length
                ? Math.max(0, durations[i])
                : 0;

            double savedProgress = i < progress.size() ? progress.get(i) : 0.0;

            this.meltingProgress[i] = Double.isFinite(savedProgress)
                ? Math.clamp(savedProgress, 0.0, this.meltingDuration[i])
                : 0.0;

            this.meltingInputs[i] = i < inputs.size()
                ? inputs.get(i).copyWithCount(1)
                : ItemStack.EMPTY;

            if (this.meltingInputs[i].isEmpty()
                || !ItemStack.isSameItemSameComponents(this.meltingInputs[i], getInventory().getItem(i))) {
                this.meltingInputs[i] = ItemStack.EMPTY;
                this.meltingProgress[i] = 0;
                this.meltingDuration[i] = 0;
            }
        }

        this.processingSlotCount = savedProcessingCount;
    }

    @Override
    public WrappedContainerStorage<SimpleContainer> getWrappedContainerStorage() {
        return wrappedContainerStorage;
    }

    @Override
    public Block getBlock() {
        return ModBlocks.CHARGING_HATCH.get();
    }

    public ResizableSyncingInventory getInventory() {
        return this.inventory;
    }

    public ResourceStorage<ResourceVariant<Item>> getInventoryProvider(Direction direction) {
        return this.wrappedContainerStorage.getStorage(direction);
    }

    @Override
    public ChargingHatchOpenPayload getMenuOpeningData(ServerPlayer player) {
        return new ChargingHatchOpenPayload(this.worldPosition, this.inventory.getContainerSize());
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
        return new ChargingHatchMenu(syncId, inventory, this, this.wrappedContainerStorage, createMenuData());
    }

    private ContainerData createMenuData() {
        int slotCount = this.inventory.getContainerSize();

        return new ContainerData() {
            @Override
            public int get(int index) {
                if (index == 0)
                    return getProcessingSlotCount();

                int slot = index - 1;
                int duration = getMeltingDuration(slot);
                if (duration <= 0)
                    return 0;

                return (int) (Math.clamp(meltingProgress[slot] / duration, 0.0, 1.0) * 1000);
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return slotCount + 1;
            }
        };
    }
}
