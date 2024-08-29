package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.EnergySpreader;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.CombustionGeneratorScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.Map;

public class CombustionGeneratorBlockEntity extends UpdatableBlockEntity implements TickableBlockEntity, ExtendedScreenHandlerFactory<BlockPosPayload>, EnergySpreader {
    private static final Map<Item, Integer> FUEL_TIMES = FurnaceBlockEntity.createFuelTimeMap();

    public static final Text TITLE = Text.translatable("container." + Industria.MOD_ID + ".combustion_generator");

    private final WrappedEnergyStorage energyStorage = new WrappedEnergyStorage();
    private final WrappedInventoryStorage<SimpleInventory> inventoryStorage = new WrappedInventoryStorage<>();

    private int burnTime = 0;
    private int fuelTime = 0;

    public CombustionGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.COMBUSTION_GENERATOR, pos, state);

        this.energyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 0, 5000));
        this.inventoryStorage.addInventory(new SyncingSimpleInventory(this, 1));
    }

    public static boolean isFuel(ItemStack stack) {
        return FUEL_TIMES.containsKey(stack.getItem());
    }

    public static int getFuelTime(ItemStack stack) {
        return FUEL_TIMES.getOrDefault(stack.getItem(), 0);
    }

    @Override
    public void tick() {
        if (this.world == null || this.world.isClient)
            return;

        SimpleEnergyStorage energyStorage = this.energyStorage.getStorage(null);

        spread(this.world, this.pos, energyStorage);

        if (energyStorage.getAmount() > energyStorage.getCapacity() - 20)
            return;

        if (this.burnTime > 0) {
            this.burnTime--;
            energyStorage.amount += 20;
            update();
        } else {
            SimpleInventory inventory = this.inventoryStorage.getInventory(0);
            ItemStack stack = inventory.getStack(0);
            if (isFuel(stack)) {
                this.fuelTime = getFuelTime(stack);
                this.burnTime = getFuelTime(stack);
                stack.decrement(1);
                update();
            }
        }
    }

    public boolean isValid(ItemStack itemStack, int slot) {
        return slot == 0 && isFuel(itemStack);
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
        return new CombustionGeneratorScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        nbt.put("EnergyStorage", this.energyStorage.writeNbt(registryLookup));
        nbt.put("Inventory", this.inventoryStorage.writeNbt(registryLookup));

        nbt.putInt("BurnTime", this.burnTime);
        nbt.putInt("FuelTime", this.fuelTime);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        this.energyStorage.readNbt(nbt.getList("EnergyStorage", NbtElement.COMPOUND_TYPE), registryLookup);
        this.inventoryStorage.readNbt(nbt.getList("Inventory", NbtElement.COMPOUND_TYPE), registryLookup);

        this.burnTime = nbt.getInt("BurnTime");
        this.fuelTime = nbt.getInt("FuelTime");
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = new NbtCompound();
        writeNbt(nbt, registryLookup);
        return nbt;
    }

    public SimpleEnergyStorage getEnergyStorage() {
        return this.energyStorage.getStorage(null);
    }

    public WrappedInventoryStorage<SimpleInventory> getWrappedInventoryStorage() {
        return this.inventoryStorage;
    }

    public EnergyStorage getEnergyProvider(Direction direction) {
        return this.energyStorage.getStorage(direction);
    }

    public InventoryStorage getInventoryProvider(Direction direction) {
        return this.inventoryStorage.getStorage(direction);
    }

    public int getBurnTime() {
        return this.burnTime;
    }

    public int getFuelTime() {
        return this.fuelTime;
    }
}
