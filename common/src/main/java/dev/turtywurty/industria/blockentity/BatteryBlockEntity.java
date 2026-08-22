package dev.turtywurty.industria.blockentity;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.BatteryBlock;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.EnergySpreader;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.menu.BatteryScreenHandler;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.lookup.MutableItemContext;
import dev.turtywurty.turtymultiloader.transfer.lookup.StorageKeys;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.RestrictedStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.SimpleEnergyStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static dev.turtywurty.industria.blockentity.util.StorageOperations.setEnergy;

public class BatteryBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, EnergySpreader, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("battery");
    public static final Component CHARGE_MODE_BUTTON_TOOLTIP_TEXT = Component.translatable("gui." + Industria.MOD_ID + ".battery.charge_mode_button.tooltip");

    private final BatteryBlock.BatteryLevel batteryLevel;
    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final ResourceStorage<ResourceVariant<UnitResource>> inputEnergyStorage;
    private final ResourceStorage<ResourceVariant<UnitResource>> outputEnergyStorage;

    private ChargeMode chargeMode = ChargeMode.DISCHARGE;

    public BatteryBlockEntity(BatteryBlock block, BlockPos pos, BlockState state) {
        super(block, ModBlockEntityTypes.BATTERY.get(), pos, state);
        this.batteryLevel = block.getLevel();

        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 1));
        var energyStorage = new SyncingEnergyStorage(this, this.batteryLevel.getCapacity(),
                this.batteryLevel.getMaxTransfer(), this.batteryLevel.getMaxTransfer());
        this.wrappedEnergyStorage.addStorage(energyStorage);
        this.inputEnergyStorage = RestrictedStorage.insertionOnly(energyStorage);
        this.outputEnergyStorage = RestrictedStorage.extractionOnly(energyStorage);
        if (this.batteryLevel == BatteryBlock.BatteryLevel.CREATIVE) {
            setEnergy(energyStorage, Long.MAX_VALUE);
        }
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var input = (SyncingSimpleInventory) this.wrappedContainerStorage.getInventory(0);
        var energy = (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        List<SyncableStorage> storages = new ArrayList<>();
        storages.add(input);
        if (batteryLevel != BatteryBlock.BatteryLevel.CREATIVE) {
            storages.add(energy);
        }

        return storages;
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        if (energyStorage == null)
            return;

        ItemStack stack = getInventory().getItem(0);
        if (!stack.isEmpty()) {
            var itemEnergyStorage = MutableItemContext.ofContainerSlot(getInventory(), 0).find(StorageKeys.ENERGY);
            if (itemEnergyStorage != null) {
                try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                    if (this.chargeMode == ChargeMode.CHARGE && itemEnergyStorage.supportsExtraction()
                            && energyStorage.getAmount() < energyStorage.getCapacity()) {
                        long attemptToExtract = Math.min(Math.min(itemEnergyStorage.amount(0),
                                energyStorage.getCapacity() - energyStorage.getAmount()), energyStorage.getMaxInput());
                        if (attemptToExtract <= 0)
                            return;

                        long extracted = itemEnergyStorage.extract(SimpleEnergyStorage.ENERGY, attemptToExtract, transaction);
                        if (extracted <= 0)
                            return;

                        energyStorage.insertInternal(extracted, transaction);
                        transaction.commit();

                        update();
                    } else if (this.chargeMode == ChargeMode.DISCHARGE && itemEnergyStorage.supportsInsertion()
                            && itemEnergyStorage.amount(0) < itemEnergyStorage.capacity(0, SimpleEnergyStorage.ENERGY)) {
                        long attemptToInsert = Math.min(Math.min(energyStorage.getAmount(),
                                        itemEnergyStorage.capacity(0, SimpleEnergyStorage.ENERGY) - itemEnergyStorage.amount(0)),
                                energyStorage.getMaxOutput());
                        if (attemptToInsert <= 0)
                            return;

                        long inserted = itemEnergyStorage.insert(SimpleEnergyStorage.ENERGY, attemptToInsert, transaction);
                        if (inserted <= 0)
                            return;

                        energyStorage.extractInternal(inserted, transaction);
                        transaction.commit();

                        update();
                    }
                }
            }
        }

        if (this.chargeMode == ChargeMode.DISCHARGE) {
            spread(this.level, this.worldPosition, energyStorage);
        }
    }

    @Override
    public BlockPosPayload getMenuOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new BatteryScreenHandler(syncId, playerInventory, this, this.wrappedContainerStorage);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        this.chargeMode = view.read("ChargeMode", ChargeMode.CODEC).orElse(ChargeMode.CHARGE);
        ViewUtils.readChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.readChild(view, "Energy", this.wrappedEnergyStorage);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        view.store("ChargeMode", ChargeMode.CODEC, this.chargeMode);
        ViewUtils.putChild(view, "Inventory", this.wrappedContainerStorage);
        ViewUtils.putChild(view, "Energy", this.wrappedEnergyStorage);
    }

    public ResourceStorage<ResourceVariant<UnitResource>> getEnergyProvider(Direction direction) {
        return this.chargeMode == ChargeMode.CHARGE ? this.inputEnergyStorage : this.outputEnergyStorage;
    }

    public ResourceStorage<ResourceVariant<Item>> getInventoryProvider(Direction direction) {
        return this.wrappedContainerStorage.getStorage(direction);
    }

    public WrappedContainerStorage<SimpleContainer> getWrappedInventory() {
        return this.wrappedContainerStorage;
    }

    public SyncingEnergyStorage getEnergy() {
        return (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
    }

    public SimpleContainer getInventory() {
        return this.wrappedContainerStorage.getInventory(0);
    }

    public boolean isValid(ItemStack stack, int slot) {
        var itemEnergyStorage = MutableItemContext.withConstant(stack).find(StorageKeys.ENERGY);
        return itemEnergyStorage != null;
    }

    public BatteryBlock.BatteryLevel getBatteryLevel() {
        return batteryLevel;
    }

    public ChargeMode getChargeMode() {
        return this.chargeMode;
    }

    public void setChargeMode(ChargeMode mode) {
        this.chargeMode = mode;
        update();
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    public Block getBlock() {
        return getBlockState().getBlock();
    }

    public enum ChargeMode {
        DISCHARGE,
        CHARGE;

        public static final Codec<ChargeMode> CODEC = Codec.STRING.xmap(ChargeMode::valueOf, ChargeMode::name);
        public static final StreamCodec<ByteBuf, ChargeMode> STREAM_CODEC = StreamCodec.of(
                (buf, value) -> buf.writeByte(value.ordinal()),
                buf -> values()[buf.readByte()]);

        public ChargeMode next() {
            return switch (this) {
                case DISCHARGE -> CHARGE;
                case CHARGE -> DISCHARGE;
            };
        }
    }
}
