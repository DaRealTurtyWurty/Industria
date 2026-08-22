package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.SolarPanelBlock;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.EnergySpreader;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.SimpleEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

import static dev.turtywurty.industria.blockentity.util.StorageOperations.insertEnergy;

public class SolarPanelBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, EnergySpreader {
    public static final Component TITLE = Industria.containerTitle("solar_panel");
    public static final Component ADVANCED_TITLE = Industria.containerTitle("advanced_solar_panel");
    public static final int BASIC_MAX_OUTPUT = 60;
    public static final int ADVANCED_MAX_OUTPUT = 160;

    private final WrappedEnergyStorage energy = new WrappedEnergyStorage();
    private final boolean isAdvanced;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super((SolarPanelBlock) state.getBlock(), ModBlockEntityTypes.SOLAR_PANEL.get(), pos, state);
        this.isAdvanced = state.getBlock() instanceof SolarPanelBlock solarPanelBlock && solarPanelBlock.isAdvanced();

        this.energy.addStorage(new SyncingEnergyStorage(this, 100_000, 0, 500));
    }

    public static int getEnergyOutput(long dayTime, boolean isRaining, boolean isThundering, int skylight, boolean isAdvanced) {
        dayTime = dayTime % 24000;

        if (dayTime <= 0 || dayTime >= 13000) // from 13000 to 24000 it's night
            return 0;

        int output;
        if (!isAdvanced) {
            if (dayTime < 6000) { // from 0 until 6000 it rises to the maximum and from 6000 to 13000 it falls to 0
                output = (int) (BASIC_MAX_OUTPUT * dayTime / 6000);
            } else {
                output = (int) (BASIC_MAX_OUTPUT * (13000 - dayTime) / 7000);
            }
        } else {
            output = ADVANCED_MAX_OUTPUT;
        }

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

        int outputSignal = getEnergyOutput();
        BlockState state = getBlockState();
        boolean powered = outputSignal > 0;
        if (state.getValue(SolarPanelBlock.POWERED) != powered) {
            this.level.setBlock(this.worldPosition, state.setValue(SolarPanelBlock.POWERED, powered), Block.UPDATE_CLIENTS);
        }

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) getEnergyStorage();
        long currentEnergy = energyStorage.getAmount();
        if (currentEnergy < energyStorage.getCapacity()) {
            insertEnergy(energyStorage, Mth.clamp(outputSignal, 0, energyStorage.getCapacity() - currentEnergy));
            if (currentEnergy != energyStorage.getAmount()) {
                update();
            }
        }

        spread(this.level, this.worldPosition, energyStorage);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        ViewUtils.readChild(view, "Energy", this.energy);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        ViewUtils.putChild(view, "Energy", this.energy);
    }

    public SyncingEnergyStorage getEnergyStorage() {
        return (SyncingEnergyStorage) this.energy.getStorage(null);
    }

    public WrappedEnergyStorage getWrappedEnergyStorage() {
        return this.energy;
    }

    public boolean isAdvanced() {
        return this.isAdvanced;
    }

    public int getMaximumEnergyOutput() {
        return this.isAdvanced ? ADVANCED_MAX_OUTPUT : BASIC_MAX_OUTPUT;
    }

    public int getEnergyOutput() {
        if (this.level == null)
            return 0;

        long dayTime = this.level.getOverworldClockTime();
        boolean isRaining = this.level.isRaining();
        boolean isThundering = this.level.isThundering();
        int skylight = this.level.getBrightness(LightLayer.SKY, this.worldPosition.above());

        return getEnergyOutput(dayTime, isRaining, isThundering, skylight, this.isAdvanced);
    }

    public ResourceStorage<ResourceVariant<UnitResource>> getEnergyProvider(Direction direction) {
        return this.energy.getStorage(direction);
    }
}
