package dev.turtywurty.industria.conveyor.block.impl.entity;

import dev.turtywurty.industria.blockentity.IndustriaBlockEntity;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.conveyor.block.impl.CountConveyorBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CountConveyorBlockEntity extends IndustriaBlockEntity implements TickableBlockEntity {
    public static final int MAX_THRESHOLD = 99;
    public static final int PULSE_DURATION_TICKS = 4;

    private int progress;
    private int threshold = 1;
    private int pulseTicks = 0;
    private boolean powered = false;
    private int lastSignalStrength = Integer.MIN_VALUE;

    public CountConveyorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.COUNT_CONVEYOR, BlockEntityTypeInit.COUNT_CONVEYOR, pos, state);
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide())
            return;

        if (this.pulseTicks > 0 && --this.pulseTicks == 0) {
            this.powered = false;
            update();
        }

        refreshRedstoneOutput();
    }

    private void refreshRedstoneOutput() {
        if (this.level == null || this.level.isClientSide())
            return;

        BlockState state = getBlockState();
        int currentSignalStrength = CountConveyorBlock.getSignalStrength(this.level, this.worldPosition);
        boolean shouldNotify = this.lastSignalStrength != currentSignalStrength;

        this.lastSignalStrength = currentSignalStrength;
        if (shouldNotify) {
            CountConveyorBlock.updateRedstoneOutput(this.level, this.worldPosition, state);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Progress", progress);
        output.putInt("Threshold", threshold);
        output.putInt("PulseTicks", pulseTicks);
        output.putBoolean("Powered", powered);
        output.putInt("LastSignalStrength", lastSignalStrength);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progress = input.getIntOr("Progress", 0);
        threshold = input.getIntOr("Threshold", 1);
        pulseTicks = input.getIntOr("PulseTicks", 0);
        powered = input.getBooleanOr("Powered", false);
        lastSignalStrength = input.getIntOr("LastSignalStrength", Integer.MIN_VALUE);
    }

    public void increaseCount() {
        progress++;

        if (progress >= threshold) {
            progress = 0;
            powered = true;
            pulseTicks = PULSE_DURATION_TICKS;
            update();
        }
    }

    public void setThreshold(int threshold) {
        this.threshold = Math.clamp(threshold, 1, MAX_THRESHOLD);
        update();

        if (progress >= threshold) {
            progress = 0;
            powered = true;
            pulseTicks = PULSE_DURATION_TICKS;
            update();
        }
    }

    public int getThreshold() {
        return threshold;
    }

    public boolean isPowered() {
        return powered;
    }
}
