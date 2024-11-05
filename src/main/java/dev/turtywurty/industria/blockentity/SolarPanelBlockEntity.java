package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.EnergySpreader;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.SolarPanelScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
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
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;

public class SolarPanelBlockEntity extends UpdatableBlockEntity implements SyncableTickableBlockEntity, EnergySpreader, ExtendedScreenHandlerFactory<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("solar_panel");

    private final WrappedEnergyStorage energy = new WrappedEnergyStorage();

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.SOLAR_PANEL, pos, state);

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
        if(this.world == null || this.world.isClient)
            return;

        SimpleEnergyStorage energyStorage = getEnergyStorage();
        long currentEnergy = energyStorage.getAmount();
        if(currentEnergy < energyStorage.getCapacity()) {
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
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.energy.readNbt(nbt.getList("Energy", NbtElement.COMPOUND_TYPE), registryLookup);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.put("Energy", this.energy.writeNbt(registryLookup));
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
        return this.energy.getStorage(null);
    }

    public WrappedEnergyStorage getWrappedEnergyStorage() {
        return this.energy;
    }

    public int getEnergyOutput() {
        if(this.world == null)
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
