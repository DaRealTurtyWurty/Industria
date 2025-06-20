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
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.BatteryScreenHandler;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;

public class BatteryBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, EnergySpreader, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("battery");
    public static final Text CHARGE_MODE_BUTTON_TOOLTIP_TEXT = Text.translatable("gui." + Industria.MOD_ID + ".battery.charge_mode_button.tooltip");

    private final BatteryBlock.BatteryLevel batteryLevel;
    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();

    private ChargeMode chargeMode = ChargeMode.DISCHARGE;

    public BatteryBlockEntity(BatteryBlock block, BlockPos pos, BlockState state) {
        super(block, BlockEntityTypeInit.BATTERY, pos, state);
        this.batteryLevel = block.getLevel();

        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 1));
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, this.batteryLevel.getCapacity(), this.batteryLevel.getMaxTransfer(), this.batteryLevel.getMaxTransfer()));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var input = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(0);
        var energy = (SyncingEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        return List.of(input, energy);
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) this.wrappedEnergyStorage.getStorage(null);
        if (energyStorage == null)
            return;

        ItemStack stack = getInventory().getStack(0);
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

        spread(this.world, this.pos, energyStorage);
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new BatteryScreenHandler(syncId, playerInventory, this, this.wrappedInventoryStorage);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.chargeMode = nbt.get("ChargeMode", ChargeMode.CODEC).orElse(ChargeMode.CHARGE);
        this.wrappedInventoryStorage.readNbt(nbt.getListOrEmpty("Inventory"), registryLookup);
        this.wrappedEnergyStorage.readNbt(nbt.getListOrEmpty("Energy"), registryLookup);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putString("ChargeMode", this.chargeMode.name());
        nbt.put("Inventory", this.wrappedInventoryStorage.writeNbt(registryLookup));
        nbt.put("Energy", this.wrappedEnergyStorage.writeNbt(registryLookup));
    }

    public EnergyStorage getEnergyProvider(Direction direction) {
        return this.wrappedEnergyStorage.getStorage(direction);
    }

    public InventoryStorage getInventoryProvider(Direction direction) {
        return this.wrappedInventoryStorage.getStorage(direction);
    }

    public WrappedInventoryStorage<SimpleInventory> getWrappedInventory() {
        return this.wrappedInventoryStorage;
    }

    public EnergyStorage getEnergy() {
        return this.wrappedEnergyStorage.getStorage(null);
    }

    public SimpleInventory getInventory() {
        return this.wrappedInventoryStorage.getInventory(0);
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
    public WrappedInventoryStorage<?> getWrappedInventoryStorage() {
        return this.wrappedInventoryStorage;
    }

    @Override
    public Block getBlock() {
        return getCachedState().getBlock();
    }

    public enum ChargeMode {
        DISCHARGE,
        CHARGE;

        public static final Codec<ChargeMode> CODEC = Codec.STRING.xmap(ChargeMode::valueOf, ChargeMode::name);
        public static final PacketCodec<ByteBuf, ChargeMode> PACKET_CODEC = PacketCodec.ofStatic(
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
