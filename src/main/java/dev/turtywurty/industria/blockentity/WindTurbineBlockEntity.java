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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;

public class WindTurbineBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, EnergySpreader, BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("wind_turbine");

    private final WrappedEnergyStorage energy = new WrappedEnergyStorage();
    private float windSpeed = -1F;

    private ImprovedNoise windNoise;
    private boolean canReceiveWind = true;

    private float propellerRotation = 0F; // Client side only

    public WindTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.WIND_TURBINE, BlockEntityTypeInit.WIND_TURBINE, pos, state);

        this.energy.addStorage(new SyncingEnergyStorage(this, 100_000, 0, 500));
    }

    public static int getEnergyOutput(Level world, BlockPos pos, float windSpeed, boolean canReceiveWind) {
        if (!canReceiveWind || world == null || pos == null || !world.canSeeSkyFromBelowWater(pos) || pos.getY() < world.getSeaLevel())
            return 0;

        Holder<Biome> biome = world.getBiome(pos);
        float biomeModifier = 1.0F;
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_MOUNTAIN))
            biomeModifier = 1.5F;
        else if (biome.is(BiomeTags.IS_HILL))
            biomeModifier = 1.25F;

        float heightMultiplier = calculateHeightMultiplier(pos, world);
        // if the time of day is > 12000, reduce the output by 50%
        float timeOfDayModifier = world.getDayTime() > 12000 ? 0.5F : 1.0F;
        float output = biomeModifier * heightMultiplier * windSpeed * timeOfDayModifier * 1000.0F;
        return (int) output;
    }

    private static float calculateHeightMultiplier(BlockPos pos, Level world) {
        int seaLevel = world.getSeaLevel();
        int worldBottom = world.getMinY();
        int worldTop = world.getHeight() + worldBottom;

        int fullHeightRange = worldTop - worldBottom;
        float normalizedHeight = (float) (pos.getY() - seaLevel) / fullHeightRange + 0.5F;

        return Mth.clamp(normalizedHeight, 0.0F, 1.0F);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of((SyncableStorage) this.energy.getStorage(null));
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        if (this.windNoise == null) {
            this.windNoise = new ImprovedNoise(this.level.getRandom());
        }

        if (this.windSpeed == -1F || this.level.getGameTime() % 24000 == 0) {
            float offset = this.level.getGameTime() / 24000F;
            this.windSpeed = (float) this.windNoise.noise(
                    this.worldPosition.getX() + offset,
                    this.worldPosition.getY() + offset,
                    this.worldPosition.getZ() + offset) + 1.0F;
        }

        if (this.level.getGameTime() % 100 == 0) {
            this.canReceiveWind = true;

            // check 8 blocks in front. if any of them are not air, set canReceiveWind to false
            for (int i = 1; i <= 8; i++) {
                BlockPos pos = this.worldPosition.above(3).offset(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getUnitVec3i().multiply(i));
                if (!this.level.getBlockState(pos).isAir()) {
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
        storage.amount = Mth.clamp(storage.amount + output, 0, storage.capacity);
        if (currentEnergy != storage.amount) {
            update();
        }

        spread(this.level, this.worldPosition, storage);
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
        return new WindTurbineScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        ViewUtils.readChild(view, "Energy", this.energy);
        this.windSpeed = view.getFloatOr("WindSpeed", 0.0F);
        this.canReceiveWind = view.getBooleanOr("CanReceiveWind", true);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
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
        return getEnergyOutput(this.level, this.worldPosition.above(3), this.windSpeed, this.canReceiveWind);
    }

    public float getPropellerRotation() {
        return this.propellerRotation;
    }
}
