package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;

public class BatteryBlock extends IndustriaBlock {
    private final BatteryLevel level;

    public BatteryBlock(Properties settings, BatteryLevel level) {
        super(settings, new BlockProperties()
                .hasAxisProperty()
                .hasComparatorOutput()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.BATTERY)
                        .dropContentsOnBreak()
                        .rightClickToOpenGui()
                        .shouldTick()));
        this.level = level;
    }

    public BatteryLevel getLevel() {
        return this.level;
    }

    public enum BatteryLevel {
        BASIC(100_000, 1_000),
        ADVANCED(1_000_000, 10_000),
        ELITE(10_000_000, 100_000),
        ULTIMATE(100_000_000, 1_000_000),
        CREATIVE(Long.MAX_VALUE, Long.MAX_VALUE);

        private final long capacity;
        private final long maxTransfer;

        BatteryLevel(long capacity, long maxTransfer) {
            this.capacity = capacity;
            this.maxTransfer = maxTransfer;
        }

        public long getCapacity() {
            return this.capacity;
        }

        public long getMaxTransfer() {
            return this.maxTransfer;
        }
    }
}
