package dev.turtywurty.industria.blockentity.abstraction;

import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.abstraction.tick.TickLogic;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public abstract class IndustriaBlockEntity<T extends IndustriaBlockEntity<T>> extends UpdatableBlockEntity implements SyncableTickableBlockEntity {
    BlockEntityFields<T> fields;

    int tickRate;
    TickLogic<T, BlockEntityFields<T>> tickLogic;

    WrappedInventoryStorage<SimpleInventory> inventoryStorage;
    WrappedFluidStorage<SingleFluidStorage> fluidStorage;
    WrappedEnergyStorage energyStorage;

    int ticks;

    IndustriaBlock blockRef;

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        BlockState newState = world.getBlockState(pos);
        if (blockRef.multiblockType != null) {
            if (!oldState.isOf(newState.getBlock())) {
                blockRef.multiblockType.onMultiblockBreak(world, pos);
            }
        } else if (blockRef.dropContentsOnBreak) {
            if (!oldState.isOf(newState.getBlock())) {
                if (this instanceof BlockEntityContentsDropper blockEntityWithInventory) { // TODO: Replace with component access maybe?
                    blockEntityWithInventory.dropContents(world, pos);
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

        this.inventoryStorage = properties.inventoryStorage;
        this.fluidStorage = properties.fluidStorage;
        this.energyStorage = properties.energyStorage;

        onFirstTick();
    }

    public void onFirstTick() {
    }

    private void registerDefaultFields(BlockEntityFields<T> fields) {
        fields.addField("world", this.world, IndustriaBlockEntity::getWorld, null);
        fields.addField("pos", this.pos, IndustriaBlockEntity::getPos, null);
        fields.addField("cachedState", getCachedState(), IndustriaBlockEntity::getCachedState, null);
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

        private final WrappedInventoryStorage<SimpleInventory> inventoryStorage = new WrappedInventoryStorage<>();
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

        public BlockEntityProperties<T> inventory(SimpleInventory inventory, Direction side) {
            this.inventoryStorage.addInventory(inventory, side);
            return this;
        }

        public BlockEntityProperties<T> inventory(SimpleInventory inventory) {
            this.inventoryStorage.addInventory(inventory);
            return this;
        }

        public BlockEntityProperties<T> tickLogic(TickLogic.TickLogicBuilder<T, BlockEntityFields<T>> builder) {
            this.tickLogic = builder.build();
            return this;
        }
    }
}
