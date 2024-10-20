package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.EnergySpreader;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.WindTurbineScreenHandler;
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
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.Random;

public class WindTurbineBlockEntity extends UpdatableBlockEntity implements TickableBlockEntity, EnergySpreader, ExtendedScreenHandlerFactory<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("wind_turbine");

    private final WrappedEnergyStorage energy = new WrappedEnergyStorage();

    private float windSpeed = -1F;
    private float propellerRotation = 0F; // Client side only

    public WindTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.WIND_TURBINE, pos, state);

        this.energy.addStorage(new SimpleEnergyStorage(100_000, 0, 500));
    }

    public static int getEnergyOutput(World world, BlockPos pos, float windSpeed) {
        if(world == null || pos == null || !world.isSkyVisible(pos) || pos.getY() < world.getSeaLevel())
            return 0;

        RegistryEntry<Biome> biome = world.getBiome(pos);
        float biomeModifier = 1.0F;
        if(biome.isIn(BiomeTags.IS_OCEAN) || biome.isIn(BiomeTags.IS_MOUNTAIN))
            biomeModifier = 1.5F;
        else if(biome.isIn(BiomeTags.IS_HILL))
            biomeModifier = 1.25F;

        float heightMultiplier = Math.min(1.0F, (float)pos.getY() / (world.getHeight() + world.getBottomY()));
        float output = biomeModifier * heightMultiplier * windSpeed * 1000.0F;
        return (int) output;
    }

    @Override
    public void tick() {
        if (this.world == null || this.world.isClient)
            return;

        if(this.windSpeed == -1F) {
            this.windSpeed = new Random(((ServerWorld)this.world).getSeed() + this.pos.asLong()).nextFloat();
        }

        SimpleEnergyStorage storage = getEnergyStorage();
        if (storage == null)
            return;

        int output = getEnergyOutput();
        long currentEnergy = storage.getAmount();
        storage.amount = MathHelper.clamp(storage.amount + output, 0, storage.capacity);
        if (currentEnergy != storage.amount) {
            update();
        }

        spread(this.world, this.pos, storage);
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
        return new WindTurbineScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.energy.readNbt(nbt.getList("Energy", NbtElement.COMPOUND_TYPE), registryLookup);
        this.windSpeed = nbt.getFloat("WindSpeed");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.put("Energy", this.energy.writeNbt(registryLookup));
        nbt.putFloat("WindSpeed", this.windSpeed);
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

    public float getWindSpeed() {
        return this.windSpeed;
    }

    public SimpleEnergyStorage getEnergyProvider(Direction direction) {
        return this.energy.getStorage(direction);
    }

    public int getEnergyOutput() {
        return getEnergyOutput(this.world, this.pos, this.windSpeed);
    }

    public float getPropellerRotation() {
        return this.propellerRotation;
    }

    public void setPropellerRotation(float propellerRotation) {
        this.propellerRotation = propellerRotation;
    }
}
