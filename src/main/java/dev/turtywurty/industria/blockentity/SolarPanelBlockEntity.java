package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.EnergySpreader;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.SolarPanelScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;

public class SolarPanelBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, EnergySpreader, BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("solar_panel");

    private final WrappedEnergyStorage energy = new WrappedEnergyStorage();

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.SOLAR_PANEL, BlockEntityTypeInit.SOLAR_PANEL, pos, state);

        this.energy.addStorage(new SyncingEnergyStorage(this, 100_000, 0, 500));
    }

    public static int getEnergyOutput(long dayTime, boolean isRaining, boolean isThundering, int skylight) {
        dayTime = dayTime % 24000;

        if (dayTime <= 0 || dayTime >= 13000) // from 13000 to 24000 it's night
            return 0;

        int output;
        if (dayTime < 6000) // from 0 until 6000 it goes from 0 to 35 and from 6000 to 13000 it goes from 35 to 0
            output = (int) (35 * dayTime / 6000);
        else
            output = (int) (35 * (13000 - dayTime) / 7000);

        if (isRaining) { // take off 30%
            if (isThundering) { // take off 50%
                output /= 2;
            } else {
                output = (int) (output * 0.7);
            }
        }

        if (skylight > 0) {
            output = (int) (output * (skylight / 15.0));
        } else {
            output = 0;
        }

        return output;
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of((SyncableStorage) this.energy.getStorage(null));
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) getEnergyStorage();
        long currentEnergy = energyStorage.getAmount();
        if (currentEnergy < energyStorage.getCapacity()) {
            int outputSignal = getEnergyOutput();
            energyStorage.amount += Mth.clamp(outputSignal, 0, energyStorage.getCapacity() - currentEnergy);
            if (currentEnergy != energyStorage.getAmount())
                update();
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
        return new SolarPanelScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        ViewUtils.readChild(view, "Energy", this.energy);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        ViewUtils.putChild(view, "Energy", this.energy);
    }

    public EnergyStorage getEnergyStorage() {
        return this.energy.getStorage(null);
    }

    public WrappedEnergyStorage getWrappedEnergyStorage() {
        return this.energy;
    }

    public int getEnergyOutput() {
        if (this.level == null)
            return 0;

        long dayTime = this.level.getDayTime();
        boolean isRaining = this.level.isRaining();
        boolean isThundering = this.level.isThundering();
        int skylight = this.level.getBrightness(LightLayer.SKY, this.worldPosition.above());

        return getEnergyOutput(dayTime, isRaining, isThundering, skylight);
    }

    public EnergyStorage getEnergyProvider(Direction direction) {
        return this.energy.getStorage(direction);
    }
}