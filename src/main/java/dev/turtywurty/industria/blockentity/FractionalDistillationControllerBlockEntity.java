package dev.turtywurty.industria.blockentity;

import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.heat.InputHeatStorage;
import dev.turtywurty.industria.blockentity.util.heat.WrappedHeatStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.FractionalDistillationControllerScreenHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FractionalDistillationControllerBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("fractional_distillation_controller");

    private final List<BlockPos> towers = new ArrayList<>();
    private final WrappedFluidStorage<SingleFluidStorage> fluidStorage = new WrappedFluidStorage<>();
    private final WrappedHeatStorage<SimpleHeatStorage> heatStorage = new WrappedHeatStorage<>();

    public FractionalDistillationControllerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.FRACTIONAL_DISTILLATION_CONTROLLER, BlockEntityTypeInit.FRACTIONAL_DISTILLATION_CONTROLLER, pos, state);
        this.fluidStorage.addStorage(new SyncingFluidStorage(this, FluidConstants.BUCKET * 10));
        this.heatStorage.addStorage(new InputHeatStorage(this, 400, 50));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingFluidStorage fluidTank = getFluidTank();
        InputHeatStorage heatStorage = getHeatStorage();
        return List.of(fluidTank, heatStorage);
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        //Industria.LOGGER.debug("Controller at {} has {} towers.", this.pos, getTowerCount());

        SingleFluidStorage tank = getFluidTank();
        if(tank.isResourceBlank() || tank.amount == 0)
            return;

        
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new FractionalDistillationControllerScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        this.towers.clear();
        int numberOfTowers = nbt.getInt("NumberOfTowers", 0);
        for (int i = 1; i <= numberOfTowers; i++) {
            this.towers.add(new BlockPos(this.pos.getX(), this.pos.getY() + i, this.pos.getZ()));
        }

        nbt.put("FluidStorage", this.fluidStorage.writeNbt(registries));
        nbt.put("HeatStorage", this.heatStorage.writeNbt(registries));
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        nbt.putInt("NumberOfTowers", this.towers.size());

        if(nbt.contains("FluidStorage"))
            this.fluidStorage.readNbt(nbt.getListOrEmpty("FluidStorage"), registries);

        if(nbt.contains("HeatStorage"))
            this.heatStorage.readNbt(nbt.getListOrEmpty("HeatStorage"), registries);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = super.toInitialChunkDataNbt(registries);
        writeNbt(nbt, registries);
        return nbt;
    }

    public boolean addTower(BlockPos pos) {
        if (this.towers.contains(pos) ||
                getTowerCount() >= 8 ||
                pos.getX() != this.pos.getX() || pos.getZ() != this.pos.getZ() ||
                pos.getY() < this.pos.getY())
            return false;

        return this.towers.add(pos);
    }

    public void removeTower(BlockPos pos) {
        this.towers.remove(pos);
    }

    public int getTowerCount() {
        return this.towers.size();
    }

    public WrappedFluidStorage<SingleFluidStorage> getFluidStorage() {
        return this.fluidStorage;
    }

    public SyncingFluidStorage getFluidTank() {
        return (SyncingFluidStorage) getFluidProvider(null);
    }

    public InputHeatStorage getHeatStorage() {
        return (InputHeatStorage) getHeatProvider(null);
    }

    public @NotNull SingleFluidStorage getFluidProvider(Direction side) {
        return this.fluidStorage.getStorage(0);
    }

    public @NotNull SimpleHeatStorage getHeatProvider(Direction side) {
        return this.heatStorage.getStorage(0);
    }
}
