package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.EnergySpreader;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.ThermalGeneratorScreenHandler;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.SimpleInventory;
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
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;

public class ThermalGeneratorBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload>, EnergySpreader, BlockEntityContentsDropper {
    public static final Text TITLE = Industria.containerTitle("thermal_generator");

    private static final int CONSUME_RATE = 500;

    private final WrappedEnergyStorage energyStorage = new WrappedEnergyStorage();
    private final WrappedFluidStorage<SingleFluidStorage> fluidStorage = new WrappedFluidStorage<>();
    private final WrappedInventoryStorage<SimpleInventory> inventoryStorage = new WrappedInventoryStorage<>();

    public ThermalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.THERMAL_GENERATOR, pos, state);

        this.energyStorage.addStorage(new SyncingEnergyStorage(this, 50_000, 0, 5000));
        this.fluidStorage.addStorage(new SyncingFluidStorage(this, FluidConstants.BUCKET * 10));
        this.inventoryStorage.addInventory(new SyncingSimpleInventory(this, 1));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var energy = (SyncingEnergyStorage) this.energyStorage.getStorage(null);
        var fluid = (SyncingFluidStorage) this.fluidStorage.getStorage(null);
        var inventory = (SyncingSimpleInventory) this.inventoryStorage.getInventory(0);
        return List.of(energy, fluid, inventory);
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        extractLavaFromInventory();

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) this.energyStorage.getStorage(null);
        spread(this.world, this.pos, energyStorage);

        if (energyStorage.getAmount() >= energyStorage.getCapacity())
            return;

        SingleFluidStorage fluidStorage = this.fluidStorage.getStorage(null);
        if (fluidStorage.isResourceBlank() || fluidStorage.getAmount() < CONSUME_RATE)
            return;

        long storedLava = fluidStorage.getAmount();

        fluidStorage.amount -= CONSUME_RATE;
        energyStorage.amount += MathHelper.clamp(storedLava, 0, energyStorage.getCapacity() - energyStorage.getAmount());
        if(energyStorage.getAmount() > energyStorage.getCapacity())
            energyStorage.amount = energyStorage.getCapacity();
        update();
    }

    private void extractLavaFromInventory() {
        Storage<FluidVariant> storage = ContainerItemContext.ofSingleSlot(this.inventoryStorage.getStorage(null).getSlot(0)).find(FluidStorage.ITEM);
        if (storage == null || !storage.supportsExtraction())
            return;

        SingleFluidStorage fluidStorage = this.fluidStorage.getStorage(null);
        if (fluidStorage.getAmount() >= fluidStorage.getCapacity())
            return;

        try (Transaction transaction = Transaction.openOuter()) {
            var lava = FluidVariant.of(Fluids.LAVA);

            long extracted = storage.extract(lava, FluidConstants.BUCKET, transaction);
            if (extracted > 0) {
                fluidStorage.variant = lava;
                fluidStorage.amount += extracted;

                transaction.commit();
                update();
            }
        }
    }

    public boolean isValid(ItemStack itemStack, int slot) {
        Storage<FluidVariant> storage = ContainerItemContext.withConstant(itemStack).find(FluidStorage.ITEM);
        if (storage == null || !storage.supportsExtraction())
            return false;

        try (Transaction transaction = Transaction.openOuter()) {
            return storage.extract(FluidVariant.of(Fluids.LAVA), FluidConstants.BUCKET, transaction) > 0;
        }
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
        return new ThermalGeneratorScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        nbt.put("EnergyStorage", this.energyStorage.writeNbt(registryLookup));
        nbt.put("FluidStorage", this.fluidStorage.writeNbt(registryLookup));
        nbt.put("Inventory", this.inventoryStorage.writeNbt(registryLookup));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        this.energyStorage.readNbt(nbt.getList("EnergyStorage", NbtElement.COMPOUND_TYPE), registryLookup);
        this.fluidStorage.readNbt(nbt.getList("FluidStorage", NbtElement.COMPOUND_TYPE), registryLookup);
        this.inventoryStorage.readNbt(nbt.getList("Inventory", NbtElement.COMPOUND_TYPE), registryLookup);
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

    public EnergyStorage getEnergyStorage() {
        return this.energyStorage.getStorage(null);
    }

    public SingleFluidStorage getFluidStorage() {
        return this.fluidStorage.getStorage(null);
    }

    @Override
    public WrappedInventoryStorage<SimpleInventory> getWrappedInventoryStorage() {
        return this.inventoryStorage;
    }

    @Override
    public Block getBlock() {
        return getCachedState().getBlock();
    }

    public EnergyStorage getEnergyProvider(Direction direction) {
        return this.energyStorage.getStorage(direction);
    }

    public SingleFluidStorage getFluidProvider(Direction direction) {
        return this.fluidStorage.getStorage(direction);
    }

    public InventoryStorage getInventoryProvider(Direction direction) {
        return this.inventoryStorage.getStorage(direction);
    }
}
