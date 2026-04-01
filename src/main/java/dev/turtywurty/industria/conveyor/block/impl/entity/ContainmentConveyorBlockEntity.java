package dev.turtywurty.industria.conveyor.block.impl.entity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityContentsDropper;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.IndustriaBlockEntity;
import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.PredicateSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorageHolder;
import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.ConveyorStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.mixin.BeehiveBlockEntityAccessor;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import dev.turtywurty.industria.screenhandler.ContainmentConveyorScreenHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ContainmentConveyorBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, WrappedContainerStorageHolder, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("containment_conveyor");

    private final AABB bounds;
    private final WrappedContainerStorage<SimpleContainer> jarStorage = new WrappedContainerStorage<>();
    private LivingEntity containingEntity;
    private int progress;
    private final int maxProgress = 40;

    public ContainmentConveyorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.CONTAINMENT_CONVEYOR, BlockEntityTypeInit.CONTAINMENT_CONVEYOR, pos, state);
        this.bounds = new AABB(pos).inflate(0.5D);
        this.jarStorage.addInsertOnlyInventory(new PredicateSimpleInventory(this, 1,
                (stack, _) -> stack.is(ItemInit.EMPTY_MOB_JAR)), null);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var inventoryStorage = ((SyncableStorage) getInventoryStorage(null));
        return List.of(inventoryStorage);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Progress", this.progress);
        if (this.containingEntity != null) {
            writeEntityData(output, this.containingEntity);
        }
        this.jarStorage.writeData(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.progress = input.getIntOr("Progress", 0);
        this.containingEntity = readEntityData(input);
        this.jarStorage.readData(input);
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        if (this.containingEntity != null) {
            if (++this.progress >= this.maxProgress) {
                onComplete();
            }

            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level;
        LevelConveyorNetworks networks = LevelConveyorNetworks.getOrCreate(serverLevel);
        ConveyorNetwork network = networks.getNetwork(this.worldPosition);
        if (network == null)
            return;

        ConveyorStorage storage = network.getStorage().getStorageAt(serverLevel, this.worldPosition);
        if (storage == null || !storage.canAcceptIncomingItem())
            return;

        List<LivingEntity> entities = this.level.getEntitiesOfClass(LivingEntity.class, this.bounds, Entity::isAlive);
        for (LivingEntity entity : entities) {
            if (entity instanceof LivingEntity livingEntity && storage.canAcceptIncomingItem()) {
                this.containingEntity = livingEntity;
                break;
            }
        }
    }

    private void onComplete() {
        ConveyorNetwork network = LevelConveyorNetworks.getOrCreate((ServerLevel) this.level).getNetwork(this.worldPosition);
        if (network != null) {
            ConveyorStorage storage = network.getStorage().getStorageAt(this.level, this.worldPosition);
            if (storage != null && storage.canAcceptIncomingItem()) {
                Entity entity = this.containingEntity;
                this.containingEntity = null;
                this.progress = 0;

                entity.remove(Entity.RemovalReason.DISCARDED);
                storage.addItem(createItem(entity));
            }
        }
    }

    private ConveyorItem createItem(Entity entity) {
        ItemStack stack = ItemInit.FILLED_MOB_JAR.getDefaultInstance();
        try (var reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), Industria.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());

            entity.save(output);
            BeehiveBlockEntityAccessor.getIgnoredBeeTags().forEach(output::discard);

            CompoundTag entityTag = output.buildResult();
            stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(entity.getType(), entityTag));

            return new ConveyorItem(this.worldPosition, stack);
        }
    }

    private void writeEntityData(ValueOutput output, LivingEntity entity) {
        try (var reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), Industria.LOGGER)) {
            TagValueOutput tagOutput = TagValueOutput.createWithContext(reporter, entity.registryAccess());
            entity.save(tagOutput);
            BeehiveBlockEntityAccessor.getIgnoredBeeTags().forEach(tagOutput::discard);

            output.store("ContainingEntity", CompoundTag.CODEC, tagOutput.buildResult());
        }
    }

    private LivingEntity readEntityData(ValueInput input) {
        CompoundTag entityTag = input.read("ContainingEntity", CompoundTag.CODEC).orElse(null);
        if (entityTag == null)
            return null;

        if (this.level != null)
            return (LivingEntity) EntityType.loadEntityRecursive(entityTag, this.level, EntitySpawnReason.LOAD, EntityProcessor.NOP);

        return null;
    }

    public Storage<ItemVariant> getInventoryStorage(Direction side) {
        return this.jarStorage.getStorage(side);
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public @Nullable LivingEntity getContainingEntity() {
        return this.containingEntity;
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.jarStorage;
    }

    @Override
    public Block getBlock() {
        return BlockInit.CONTAINMENT_CONVEYOR;
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ContainmentConveyorScreenHandler(containerId, inventory, this, this.jarStorage);
    }
}
