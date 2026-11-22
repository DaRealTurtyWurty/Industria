package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.industria.mechanical.MechanicalSource;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public class WindMillBlockEntity extends IndustriaBlockEntity
    implements SyncableTickableBlockEntity,
        MechanicalSource,
        BlockEntityContentsDropper {
    private static final int SAIL_LENGTH = 3;
    private static final int SAIL_COUNT = 4;
    private static final double MAX_RPM = 20.0;
    private static final double MAX_TORQUE = 16.0;

    private final WrappedContainerStorage<SimpleContainer> wrappedInventoryStorage = new WrappedContainerStorage<>();
    private PerlinNoise windNoise;
    private double preferredRPM;
    private double driveStrength = 0.0;
    private double rotation;
    private double shaftRPM;
    private long rotationGameTime;
    private boolean needsRotationSync = true;

    public WindMillBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.WIND_MILL.get(), ModBlockEntityTypes.WIND_MILL.get(), pos, state);
        this.wrappedInventoryStorage.addInventory(new PredicateSimpleInventory(this, SAIL_COUNT,
            (stack, _) -> stack.is(ModItems.WIND_MILL_SAIL.get())) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
    }

    @Override
    public void onTick() {
        if (!(level instanceof ServerLevel serverLevel))
            return;

        double drive = isReadyForUse() && !areSailsBlocked() ? sampleWind(serverLevel) : 0.0;
        this.preferredRPM = drive * MAX_RPM;
        this.driveStrength = drive * MAX_TORQUE / MAX_RPM;
    }

    private double sampleWind(ServerLevel serverLevel) {
        if (this.windNoise == null) {
            this.windNoise = new PerlinNoise(serverLevel.getRandom());
        }

        float dayOffset = serverLevel.getGameTime() / 24_000.0f;
        double noise = this.windNoise.get(
            this.worldPosition.getX() + dayOffset,
            this.worldPosition.getY() + dayOffset,
            this.worldPosition.getZ() + dayOffset);

        return Math.clamp(0.75 + 0.25 * noise, 0.0, 1.0);
    }

    public boolean areSailsBlocked() {
        if (this.level == null)
            return false;

        Direction sideways = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise();
        for (int x = -SAIL_LENGTH; x <= SAIL_LENGTH; x++) {
            for (int y = -SAIL_LENGTH; y <= SAIL_LENGTH; y++) {
                if (x == 0 && y == 0)
                    continue;

                double nearestX = Math.max(0.0, Math.abs(x) - 0.5);
                double nearestY = Math.max(0.0, Math.abs(y) - 0.5);
                if (nearestX * nearestX + nearestY * nearestY > SAIL_LENGTH * SAIL_LENGTH)
                    continue;

                BlockPos pos = this.worldPosition.offset(x * sideways.getStepX(), y, x * sideways.getStepZ());
                if (level.isOutsideBuildHeight(pos) || !level.isLoaded(pos)
                    || !level.getBlockState(pos).canBeReplaced())
                    return true;
            }
        }

        return false;
    }

    public boolean installSail(ItemStack stack) {
        SimpleContainer inventory = this.wrappedInventoryStorage.getInventory(0);
        for (int sailSlot = 0; sailSlot < SAIL_COUNT; sailSlot++) {
            if (inventory.getItem(sailSlot).isEmpty() && inventory.canPlaceItem(sailSlot, stack)) {
                inventory.setItem(sailSlot, stack.copyWithCount(1));
                return true;
            }
        }

        return false;
    }

    public int getInstalledSailMask() {
        SimpleContainer inventory = this.wrappedInventoryStorage.getInventory(0);
        int mask = 0;
        for (int slot = 0; slot < SAIL_COUNT; slot++) {
            if (inventory.getItem(slot).is(ModItems.WIND_MILL_SAIL.get())) {
                mask |= 1 << slot;
            }
        }

        return mask;
    }

    public boolean isReadyForUse() {
        return getInstalledSailMask() == (1 << SAIL_COUNT) - 1;
    }

    @Override
    public Block getBlock() {
        return ModBlocks.WIND_MILL.get();
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedInventoryStorage;
    }

    @Override
    public double getTorqueAtRPM(double shaftRPM) {
        return this.driveStrength * (this.preferredRPM - shaftRPM);
    }

    @Override
    public void onShaftUpdate(double shaftRPM, double rotationDelta) {
        if (!(level instanceof ServerLevel))
            return;

        double previousRPM = this.shaftRPM;
        this.rotation = wrapRotation(this.rotation + rotationDelta);
        this.shaftRPM = shaftRPM;
        this.rotationGameTime = level.getGameTime();
        if (rotationDelta != 0.0) {
            setChanged();
        }

        if (needsRotationSync
            || Math.abs(previousRPM - shaftRPM) > 0.01
            || (shaftRPM == 0.0) != (previousRPM == 0.0)
            || (shaftRPM != 0.0 && rotationGameTime % 10 == 0)) {
            needsRotationSync = false;
            update();
            forceUpdate();
        }
    }

    public float getRenderRotation(float partialTick) {
        double elapsed = level != null && level.isClientSide()
            ? Math.max(0.0, level.getGameTime() - rotationGameTime + partialTick)
            : 0.0;
        return (float) wrapRotation(rotation + elapsed * shaftRPM * Math.TAU / 1200.0);
    }

    private static double wrapRotation(double rotation) {
        double wrapped = rotation % Math.TAU;
        return wrapped < 0.0 ? wrapped + Math.TAU : wrapped;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ViewUtils.putChild(output, "Inventory", this.wrappedInventoryStorage);
        output.putDouble("RotorRotation", this.rotation);
        output.putDouble("ShaftRPM", this.shaftRPM);
        output.putLong("RotationGameTime", this.rotationGameTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ViewUtils.readChild(input, "Inventory", this.wrappedInventoryStorage);
        this.rotation = wrapRotation(input.getDoubleOr("RotorRotation", 0.0));
        this.shaftRPM = input.getDoubleOr("ShaftRPM", 0.0);
        this.rotationGameTime = input.getLongOr("RotationGameTime", 0L);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        SyncingSimpleInventory inventory = (SyncingSimpleInventory) this.wrappedInventoryStorage.getInventory(0);
        return List.of(inventory);
    }

    public ResourceStorage<ResourceVariant<Item>> getInventoryProvider(Direction direction) {
        return this.wrappedInventoryStorage.getStorage(direction);
    }
}
