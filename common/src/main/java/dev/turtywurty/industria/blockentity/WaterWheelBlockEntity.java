package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.mechanical.MechanicalSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WaterWheelBlockEntity extends IndustriaBlockEntity
    implements
        SyncableTickableBlockEntity,
        MechanicalSource {
    private static final double MAX_RPM = 20.0;
    private static final double MAX_TORQUE = 16.0;
    private static final double DRY_DRAG = 0.04;

    private double preferredRPM;
    private double driveStrength = DRY_DRAG;
    private double rotation;
    private double shaftRPM;
    private long rotationGameTime;
    private boolean needsRotationSync = true;

    public WaterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.WATER_WHEEL.get(), ModBlockEntityTypes.WATER_WHEEL.get(), pos, state);
    }

    @Override
    public void onTick() {
        if (!(this.level instanceof ServerLevel serverLevel))
            return;

        var flow = WaterWheelFlowSampler.sample(serverLevel, worldPosition, getBlockState());
        double force = Math.clamp(flow.signedTorque(), -1.0, 1.0);
        this.preferredRPM = force * MAX_RPM;
        this.driveStrength = Math.max(DRY_DRAG, Math.abs(force) * MAX_TORQUE / MAX_RPM);
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
        if (rotationDelta != 0.0)
            setChanged();

        if (needsRotationSync || Math.abs(shaftRPM - previousRPM) > 0.01
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
        output.putDouble("WheelRotation", rotation);
        output.putDouble("ShaftRPM", shaftRPM);
        output.putLong("RotationGameTime", rotationGameTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        rotation = wrapRotation(input.getDoubleOr("WheelRotation", 0.0));
        shaftRPM = input.getDoubleOr("ShaftRPM", 0.0);
        rotationGameTime = input.getLongOr("RotationGameTime", 0L);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of();
    }

    public static final class WaterWheelFlowSampler {
        private static final double TORQUE_SCALE = 6.0;

        private WaterWheelFlowSampler() {
        }

        public static WaterFlowResult sample(ServerLevel level, BlockPos pos, BlockState state) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            Direction horizontal = facing.getClockWise();
            double totalTorque = 0.0;

            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    boolean onSide = Math.abs(x) == 2 && Math.abs(y) < 2;
                    boolean onTopOrBottom = Math.abs(y) == 2 && Math.abs(x) < 2;
                    if (!onSide && !onTopOrBottom)
                        continue;

                    var sampleOffset = new BlockPos(horizontal.getStepX() * x, y, horizontal.getStepZ() * x);
                    BlockPos samplePos = pos.offset(sampleOffset);
                    var fluidState = level.getFluidState(samplePos);
                    if (fluidState.is(FluidTags.WATER)) {
                        Vec3 flow = fluidState.getFlow(level, samplePos);
                        totalTorque += calculateTorque(Vec3.atLowerCornerOf(sampleOffset), flow, facing);
                    }
                }
            }

            return new WaterFlowResult(totalTorque / TORQUE_SCALE);
        }

        private static double calculateTorque(Vec3 offset, Vec3 flow, Direction facing) {
            var axle = facing.getAxis() == Direction.Axis.X
                ? new Vec3(1, 0, 0)
                : new Vec3(0, 0, 1);

            return offset.cross(flow).dot(axle);
        }

        public record WaterFlowResult(double signedTorque) {
        }
    }
}
