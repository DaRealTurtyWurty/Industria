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
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;

public class SolarPanelBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, EnergySpreader, BlockEntityWithGui<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("solar_panel");

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
        if (this.world == null || this.world.isClient)
            return;

        SimpleEnergyStorage energyStorage = (SimpleEnergyStorage) getEnergyStorage();
        long currentEnergy = energyStorage.getAmount();
        if (currentEnergy < energyStorage.getCapacity()) {
            int outputSignal = getEnergyOutput();
            energyStorage.amount += MathHelper.clamp(outputSignal, 0, energyStorage.getCapacity() - currentEnergy);
            if (currentEnergy != energyStorage.getAmount())
                update();
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
        return new SolarPanelScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void readData(ReadView view) {
        ViewUtils.readChild(view, "Energy", this.energy);
    }

    @Override
    protected void writeData(WriteView view) {
        ViewUtils.putChild(view, "Energy", this.energy);
    }

    public EnergyStorage getEnergyStorage() {
        return this.energy.getStorage(null);
    }

    public WrappedEnergyStorage getWrappedEnergyStorage() {
        return this.energy;
    }

    public int getEnergyOutput() {
        if (this.world == null)
            return 0;

        long dayTime = this.world.getTimeOfDay();
        boolean isRaining = this.world.isRaining();
        boolean isThundering = this.world.isThundering();
        int skylight = this.world.getLightLevel(LightType.SKY, this.pos.up());

        return getEnergyOutput(dayTime, isRaining, isThundering, skylight);
    }

    public EnergyStorage getEnergyProvider(Direction direction) {
        return this.energy.getStorage(direction);
    }
}
