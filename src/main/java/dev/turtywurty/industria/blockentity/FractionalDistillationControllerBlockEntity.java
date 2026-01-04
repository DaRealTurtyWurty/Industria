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
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FractionalDistillationControllerBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("fractional_distillation_controller");

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
        if (this.level == null || this.level.isClientSide())
            return;

        //Industria.LOGGER.debug("Controller at {} has {} towers.", this.pos, getTowerCount());

        SingleFluidStorage tank = getFluidTank();
        if (tank.isResourceBlank() || tank.amount == 0)
            return;


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
        return new FractionalDistillationControllerScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput view) {

        this.towers.clear();
        int numberOfTowers = view.getIntOr("NumberOfTowers", 0);
        for (int i = 1; i <= numberOfTowers; i++) {
            this.towers.add(new BlockPos(this.worldPosition.getX(), this.worldPosition.getY() + i, this.worldPosition.getZ()));
        }

        ViewUtils.readChild(view, "FluidStorage", this.fluidStorage);
        ViewUtils.readChild(view, "HeatStorage", this.heatStorage);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {

        view.putInt("NumberOfTowers", this.towers.size());
        ViewUtils.putChild(view, "FluidStorage", this.fluidStorage);
        ViewUtils.putChild(view, "HeatStorage", this.heatStorage);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean addTower(BlockPos pos) {
        if (this.towers.contains(pos) ||
                getTowerCount() >= 8 ||
                pos.getX() != this.worldPosition.getX() || pos.getZ() != this.worldPosition.getZ() ||
                pos.getY() < this.worldPosition.getY())
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