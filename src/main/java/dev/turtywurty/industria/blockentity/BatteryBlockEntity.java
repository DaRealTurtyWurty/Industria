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
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.BatteryScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.ArrayList;
import java.util.List;

public class BatteryBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, EnergySpreader, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("battery");
    public static final Component CHARGE_MODE_BUTTON_TOOLTIP_TEXT = Component.translatable("gui." + Industria.MOD_ID + ".battery.charge_mode_button.tooltip");

    private final BatteryBlock.BatteryLevel batteryLevel;
    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();

    private ChargeMode chargeMode = ChargeMode.DISCHARGE;

    public BatteryBlockEntity(BatteryBlock block, BlockPos pos, BlockState state) {
        super(block, BlockEntityTypeInit.BATTERY, pos, state);
        this.batteryLevel = block.getLevel();

        this.wrappedContainerStorage.addInventory(new SyncingSimpleInventory(this, 1));
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, this.batteryLevel.getCapacity(), this.batteryLevel.getMaxTransfer(), this.batteryLevel.getMaxTransfer()));
        if (this.batteryLevel == BatteryBlock.BatteryLevel.CREATIVE)
            ((SimpleEnergyStorage) this.wrappedEnergyStorage.getStorage(null)).amount = Long.MAX_VALUE;

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
            var itemEnergyStorage = ContainerItemContext.withConstant(stack).find(EnergyStorage.ITEM);
            if (itemEnergyStorage != null) {
                try (Transaction transaction = Transaction.openOuter()) {
                    if (this.chargeMode == ChargeMode.CHARGE && itemEnergyStorage.supportsInsertion() && itemEnergyStorage.getAmount() < itemEnergyStorage.getCapacity()) {
                        long attemptToInsert = Math.min(Math.min(energyStorage.getAmount(), itemEnergyStorage.getCapacity() - itemEnergyStorage.getAmount()), energyStorage.maxExtract);
                        if (attemptToInsert <= 0)
                            return;

                        long inserted = itemEnergyStorage.insert(attemptToInsert, transaction);
                        if (inserted <= 0)
                            return;

                        energyStorage.amount -= inserted;
                        transaction.commit();

                        update();
                    } else if (this.chargeMode == ChargeMode.DISCHARGE && itemEnergyStorage.supportsExtraction() && energyStorage.getAmount() < energyStorage.getCapacity()) {
                        long attemptToExtract = Math.min(Math.min(itemEnergyStorage.getAmount(), energyStorage.getCapacity() - energyStorage.getAmount()), energyStorage.maxInsert);
                        if (attemptToExtract <= 0)
                            return;

                        long extracted = itemEnergyStorage.extract(attemptToExtract, transaction);
                        if (extracted <= 0)
                            return;

                        energyStorage.amount += extracted;
                        transaction.commit();

                        update();
                    }
                }
            }
        }

        spread(this.level, this.worldPosition, energyStorage);
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayer player) {
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

    public EnergyStorage getEnergyProvider(Direction direction) {
        return this.wrappedEnergyStorage.getStorage(direction);
    }

    public ContainerStorage getInventoryProvider(Direction direction) {
        return this.wrappedContainerStorage.getStorage(direction);
    }

    public WrappedContainerStorage<SimpleContainer> getWrappedInventory() {
        return this.wrappedContainerStorage;
    }

    public EnergyStorage getEnergy() {
        return this.wrappedEnergyStorage.getStorage(null);
    }

    public SimpleContainer getInventory() {
        return this.wrappedContainerStorage.getInventory(0);
    }

    public boolean isValid(ItemStack stack, int slot) {
        var itemEnergyStorage = ContainerItemContext.withConstant(stack).find(EnergyStorage.ITEM);
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
        System.out.println("Charge mode set to: " + mode);
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