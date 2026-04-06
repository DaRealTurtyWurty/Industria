package dev.turtywurty.industria.conveyor.block.impl.entity;

import dev.turtywurty.industria.blockentity.IndustriaBlockEntity;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.conveyor.*;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.*;

public class DelayConveyorBlockEntity extends IndustriaBlockEntity implements TickableBlockEntity {
    public static final int MAX_THRESHOLD = 200; // 10 seconds at 20 ticks per second

    private int delayTicks = 10; // Default delay of 10 ticks (0.5 seconds)
    private final Map<UUID, Long> releaseTicksByItem = new HashMap<>();

    public DelayConveyorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.DELAY_CONVEYOR, BlockEntityTypeInit.DELAY_CONVEYOR, pos, state);
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide())
            return;

        ConveyorStorage storage = getOwnStorage();
        if (storage == null) {
            releaseTicksByItem.clear();
            return;
        }

        long now = level.getGameTime();
        Set<UUID> stillDelayedItems = new HashSet<>();
        for (ConveyorItem item : storage.getItems()) {
            UUID itemId = item.getId();
            stillDelayedItems.add(itemId);
            this.releaseTicksByItem.computeIfAbsent(itemId, _ -> now + delayTicks);
        }

        this.releaseTicksByItem.keySet().removeIf(itemId -> !stillDelayedItems.contains(itemId));
    }

    private ConveyorStorage getOwnStorage() {
        if (level == null || level.isClientSide())
            return null;

        ServerLevel serverLevel = (ServerLevel) level;
        ConveyorNetworkManager networkManager = LevelConveyorNetworks.getOrCreate(serverLevel).getNetworkManager();
        if (networkManager == null)
            return null;

        ConveyorNetwork network = networkManager.getNetworkAt(worldPosition);
        if (network == null)
            return null;

        ConveyorNetworkStorage networkStorage = network.getStorage();
        if (networkStorage == null)
            return null;

        return networkStorage.getStorageAt(serverLevel, worldPosition);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("DelayTicks", delayTicks);
        output.putInt("ReleaseCount", releaseTicksByItem.size());
        int index = 0;
        for (Map.Entry<UUID, Long> entry : releaseTicksByItem.entrySet()) {
            output.store("ItemUUID" + index, UUIDUtil.CODEC, entry.getKey());
            output.putLong("ReleaseTick" + index, entry.getValue());
            index++;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        delayTicks = input.getIntOr("DelayTicks", 10);
        int releaseCount = input.getIntOr("ReleaseCount", 0);
        releaseTicksByItem.clear();
        for (int i = 0; i < releaseCount; i++) {
            int finalI = i;
            UUID itemUUID = input.read("ItemUUID" + i, UUIDUtil.CODEC)
                    .orElseThrow(() -> new IllegalStateException("Missing ItemUUID for index " + finalI));
            long releaseTick = input.getLong("ReleaseTick" + i)
                    .orElseThrow(() -> new IllegalStateException("Missing ReleaseTick for index " + finalI));
            releaseTicksByItem.put(itemUUID, releaseTick);
        }
    }

    public boolean isItemDelayed(UUID itemUUID, long gameTime) {
        return releaseTicksByItem.getOrDefault(itemUUID, 0L) > gameTime;
    }

    public int getThreshold() {
        return delayTicks;
    }

    public void setThreshold(int delayTicks) {
        this.delayTicks = Math.clamp(delayTicks, 10, MAX_THRESHOLD);
        update();
    }
}
