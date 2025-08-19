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
import dev.turtywurty.industria.screenhandler.WindTurbineScreenHandler;
import dev.turtywurty.industria.util.ViewUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;

public class WindTurbineBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, EnergySpreader, BlockEntityWithGui<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("wind_turbine");

    private final WrappedEnergyStorage energy = new WrappedEnergyStorage();
    private float windSpeed = -1F;

    private PerlinNoiseSampler windNoise;
    private boolean canReceiveWind = true;

    private float propellerRotation = 0F; // Client side only

    public WindTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.WIND_TURBINE, BlockEntityTypeInit.WIND_TURBINE, pos, state);

        this.energy.addStorage(new SyncingEnergyStorage(this, 100_000, 0, 500));
    }

    public static int getEnergyOutput(World world, BlockPos pos, float windSpeed, boolean canReceiveWind) {
        if (!canReceiveWind || world == null || pos == null || !world.isSkyVisibleAllowingSea(pos) || pos.getY() < world.getSeaLevel())
            return 0;

        RegistryEntry<Biome> biome = world.getBiome(pos);
        float biomeModifier = 1.0F;
        if (biome.isIn(BiomeTags.IS_OCEAN) || biome.isIn(BiomeTags.IS_MOUNTAIN))
            biomeModifier = 1.5F;
        else if (biome.isIn(BiomeTags.IS_HILL))
            biomeModifier = 1.25F;

        float heightMultiplier = calculateHeightMultiplier(pos, world);
        // if the time of day is > 12000, reduce the output by 50%
        float timeOfDayModifier = world.getTimeOfDay() > 12000 ? 0.5F : 1.0F;
        float output = biomeModifier * heightMultiplier * windSpeed * timeOfDayModifier * 1000.0F;
        return (int) output;
    }

    private static float calculateHeightMultiplier(BlockPos pos, World world) {
        int seaLevel = world.getSeaLevel();
        int worldBottom = world.getBottomY();
        int worldTop = world.getHeight() + worldBottom;

        int fullHeightRange = worldTop - worldBottom;
        float normalizedHeight = (float) (pos.getY() - seaLevel) / fullHeightRange + 0.5F;

        return MathHelper.clamp(normalizedHeight, 0.0F, 1.0F);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of((SyncableStorage) this.energy.getStorage(null));
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient)
            return;

        if (this.windNoise == null) {
            this.windNoise = new PerlinNoiseSampler(this.world.random);
        }

        if (this.windSpeed == -1F || this.world.getTime() % 24000 == 0) {
            float offset = this.world.getTime() / 24000F;
            this.windSpeed = (float) this.windNoise.sample(
                    this.pos.getX() + offset,
                    this.pos.getY() + offset,
                    this.pos.getZ() + offset) + 1.0F;
        }

        if (this.world.getTime() % 100 == 0) {
            this.canReceiveWind = true;

            // check 8 blocks in front. if any of them are not air, set canReceiveWind to false
            for (int i = 1; i <= 8; i++) {
                BlockPos pos = this.pos.up(3).add(getCachedState().get(Properties.HORIZONTAL_FACING).getVector().multiply(i));
                if (!this.world.getBlockState(pos).isAir()) {
                    this.canReceiveWind = false;
                    break;
                }
            }

            update();
        }

        SimpleEnergyStorage storage = (SimpleEnergyStorage) getEnergyStorage();
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
    protected void readData(ReadView view) {
        super.readData(view);
        ViewUtils.readChild(view, "Energy", this.energy);
        this.windSpeed = view.getFloat("WindSpeed", 0.0F);
        this.canReceiveWind = view.getBoolean("CanReceiveWind", true);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        ViewUtils.putChild(view, "Energy", this.energy);
        view.putFloat("WindSpeed", this.windSpeed);
        view.putBoolean("CanReceiveWind", this.canReceiveWind);
    }

    public EnergyStorage getEnergyStorage() {
        return this.energy.getStorage(null);
    }

    public float getWindSpeed() {
        return this.windSpeed;
    }

    public EnergyStorage getEnergyProvider(Direction direction) {
        return this.energy.getStorage(direction);
    }

    public int getEnergyOutput() {
        return getEnergyOutput(this.world, this.pos.up(3), this.windSpeed, this.canReceiveWind);
    }

    public float getPropellerRotation() {
        return this.propellerRotation;
    }

    public void setPropellerRotation(float propellerRotation) {
        this.propellerRotation = propellerRotation;
    }
}
