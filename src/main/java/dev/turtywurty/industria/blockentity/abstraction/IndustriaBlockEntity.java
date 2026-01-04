package dev.turtywurty.industria.blockentity.abstraction;

import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.abstraction.tick.TickLogic;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public abstract class IndustriaBlockEntity<T extends IndustriaBlockEntity<T>> extends UpdatableBlockEntity implements SyncableTickableBlockEntity {
    BlockEntityFields<T> fields;

    int tickRate;
    TickLogic<T, BlockEntityFields<T>> tickLogic;

    WrappedContainerStorage<SimpleContainer> ContainerStorage;
    WrappedFluidStorage<SingleFluidStorage> fluidStorage;
    WrappedEnergyStorage energyStorage;

    int ticks;

    IndustriaBlock blockRef;

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        BlockState newState = level.getBlockState(pos);
        if (blockRef.multiblockType != null) {
            if (!oldState.is(newState.getBlock())) {
                blockRef.multiblockType.onMultiblockBreak(level, pos);
            }
        } else if (blockRef.dropContentsOnBreak) {
            if (!oldState.is(newState.getBlock())) {
                if (this instanceof BlockEntityContentsDropper blockEntityWithInventory) { // TODO: Replace with component access maybe?
                    blockEntityWithInventory.dropContents(level, pos);
                }
            }
        }
    }

    public IndustriaBlockEntity(BlockPos pos, BlockState state, BlockEntityType<T> type) {
        super(type, pos, state);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of();
    }

    @Override
    public void onTick() {
        if (this.ticks == 0) {
            internal_onFirstTick();
        }

        if (this.tickRate == 0 || (this.ticks % this.tickRate == 0)) {
            internal_onTick();
        }

        this.ticks++;
    }

    @SuppressWarnings("unchecked")
    private void internal_onTick() {
        if (this.tickLogic != null) {
            this.tickLogic.run((T) this, this.fields);
        }
    }

    public int getTicks() {
        return this.ticks;
    }

    private void internal_onFirstTick() {
        BlockEntityProperties<T> properties = createProperties();

        registerDefaultFields(properties.fields);
        registerFields(properties.fields);
        this.fields = properties.fields;

        this.tickRate = properties.tickRate;
        this.tickLogic = properties.tickLogic;

        this.ContainerStorage = properties.ContainerStorage;
        this.fluidStorage = properties.fluidStorage;
        this.energyStorage = properties.energyStorage;

        onFirstTick();
    }

    public void onFirstTick() {
    }

    private void registerDefaultFields(BlockEntityFields<T> fields) {
        fields.addField("world", this.level, IndustriaBlockEntity::getLevel, null);
        fields.addField("pos", this.worldPosition, IndustriaBlockEntity::getBlockPos, null);
        fields.addField("cachedState", getBlockState(), IndustriaBlockEntity::getBlockState, null);
        fields.addField("ticks", this.ticks, IndustriaBlockEntity::getTicks, null);
        fields.addField("tickRate", this.tickRate, blockEntity -> blockEntity.tickRate, null);
        fields.addField("isDirty", this.isDirty, blockEntity -> blockEntity.isDirty, (blockEntity, value) -> blockEntity.isDirty = value);
    }

    public abstract void registerFields(BlockEntityFields<T> fields);

    protected abstract BlockEntityProperties<T> createProperties();

    public static class BlockEntityProperties<T extends IndustriaBlockEntity<T>> {
        private final T blockEntity;
        private int tickRate = 0;
        private TickLogic<T, BlockEntityFields<T>> tickLogic;

        private final WrappedContainerStorage<SimpleContainer> ContainerStorage = new WrappedContainerStorage<>();
        private final WrappedFluidStorage<SingleFluidStorage> fluidStorage = new WrappedFluidStorage<>();
        private final WrappedEnergyStorage energyStorage = new WrappedEnergyStorage();

        private final BlockEntityFields<T> fields = new BlockEntityFields<>();

        public BlockEntityProperties(T blockEntity) {
            this.blockEntity = blockEntity;

            this.blockEntity.registerFields(this.fields);
        }

        public BlockEntityProperties<T> tickRate(int tickRate) {
            if (tickRate < 0) {
                throw new IllegalArgumentException("Tick rate must be greater than or equal to 0");
            }

            this.tickRate = tickRate;
            return this;
        }

        public BlockEntityProperties<T> inventory(InventoryBuilder builder, Direction side) {
            return inventory(builder.build(), side);
        }

        public BlockEntityProperties<T> inventory(InventoryBuilder builder) {
            return inventory(builder.build());
        }

        public BlockEntityProperties<T> inventory(SimpleContainer inventory, Direction side) {
            this.ContainerStorage.addInventory(inventory, side);
            return this;
        }

        public BlockEntityProperties<T> inventory(SimpleContainer inventory) {
            this.ContainerStorage.addInventory(inventory);
            return this;
        }

        public BlockEntityProperties<T> tickLogic(TickLogic.TickLogicBuilder<T, BlockEntityFields<T>> builder) {
            this.tickLogic = builder.build();
            return this;
        }
    }
}
