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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ContainmentConveyorBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity, WrappedContainerStorageHolder, BlockEntityWithGui<BlockPosPayload>, BlockEntityContentsDropper {
    public static final Component TITLE = Industria.containerTitle("containment_conveyor");

    private final AABB bounds;
    private final WrappedContainerStorage<SimpleContainer> jarStorage = new WrappedContainerStorage<>();
    private LivingEntity containingEntity;
    private TypedEntityData<EntityType<?>> containingEntityData;
    private int progress;
    private final int maxProgress = 100;

    public ContainmentConveyorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.CONTAINMENT_CONVEYOR, BlockEntityTypeInit.CONTAINMENT_CONVEYOR, pos, state);
        this.bounds = new AABB(pos).inflate(0.5D);
        this.jarStorage.addInsertOnlyInventory(new PredicateSimpleInventory(this, 1,
                (stack, _) -> stack.is(ItemInit.EMPTY_MOB_JAR)), null);
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        var inventoryStorage = Objects.requireNonNull(((SyncableStorage) this.jarStorage.getInventory(0)));
        return List.of(inventoryStorage);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Progress", this.progress);
        if (this.containingEntity != null) {
            writeEntityData(output, this.containingEntity);
        } else if (this.containingEntityData != null) {
            writeEntityData(output, this.containingEntityData);
        }
        this.jarStorage.writeData(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.progress = input.getIntOr("Progress", 0);
        this.containingEntity = null;
        this.containingEntityData = readEntityData(input);
        resolveContainingEntity();
        this.jarStorage.readData(input);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        resolveContainingEntity();
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        resolveContainingEntity();

        if (this.containingEntity != null) {
            if (++this.progress >= this.maxProgress) {
                onComplete();
            } else {
                update();
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

        List<LivingEntity> entities = this.level.getEntitiesOfClass(LivingEntity.class, this.bounds, entity -> !entity.isSpectator() && entity.isAlive() && !(entity instanceof Player));
        for (LivingEntity entity : entities) {
            if (entity instanceof LivingEntity livingEntity && storage.canAcceptIncomingItem()) {
                onEntityFound(livingEntity);
                break;
            }
        }
    }

    private void onEntityFound(LivingEntity livingEntity) {
        this.containingEntity = livingEntity;
        this.containingEntityData = null;
        this.containingEntity.remove(Entity.RemovalReason.DISCARDED);
        SimpleContainer inventory = this.jarStorage.getInventory(0);
        if (inventory != null) {
            inventory.removeItem(0, 1);
        }

        update();
    }

    private void onComplete() {
        ConveyorNetwork network = LevelConveyorNetworks.getOrCreate((ServerLevel) this.level).getNetwork(this.worldPosition);
        if (network != null) {
            ConveyorStorage storage = network.getStorage().getStorageAt(this.level, this.worldPosition);
            if (storage != null && storage.canAcceptIncomingItem()) {
                Entity entity = this.containingEntity;
                this.containingEntity = null;
                this.containingEntityData = null;
                this.progress = 0;

                storage.addItem(createItem(entity));
                update();
            }
        }
    }

    private ConveyorItem createItem(Entity entity) {
        ItemStack stack = ItemInit.FILLED_MOB_JAR.getDefaultInstance();
        try (var reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), Industria.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
            entity.saveWithoutId(output);
            discardTags(output);

            CompoundTag entityTag = output.buildResult();
            stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(entity.getType(), entityTag));

            return new ConveyorItem(this.worldPosition, stack);
        }
    }

    private static void discardTags(TagValueOutput output) {
        output.discard("Passengers");
        output.discard("Leash");
        output.discard("UUID");
        output.discard("Pos");
        output.discard("Motion");
        output.discard("Brain");
    }

    private void writeEntityData(ValueOutput output, LivingEntity entity) {
        try (var reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), Industria.LOGGER)) {
            TagValueOutput tagOutput = TagValueOutput.createWithContext(reporter, entity.registryAccess());
            entity.saveWithoutId(tagOutput);
            discardTags(tagOutput);

            writeEntityData(output, TypedEntityData.of(entity.getType(), tagOutput.buildResult()));
        }
    }

    private void writeEntityData(ValueOutput output, TypedEntityData<EntityType<?>> entityData) {
        output.store("ContainingEntityType", EntityType.CODEC, entityData.type());
        output.store("ContainingEntityData", CompoundTag.CODEC, entityData.copyTagWithoutId());
    }

    private TypedEntityData<EntityType<?>> readEntityData(ValueInput input) {
        EntityType<?> entityType = input.read("ContainingEntityType", EntityType.CODEC).orElse(null);
        CompoundTag entityTag = input.read("ContainingEntityData", CompoundTag.CODEC).orElse(null);
        if (entityType == null || entityTag == null)
            return null;

        return TypedEntityData.of(entityType, entityTag);
    }

    private void resolveContainingEntity() {
        if (this.level == null || this.containingEntity != null || this.containingEntityData == null)
            return;

        Entity entity = EntityType.loadEntityRecursive(
                this.containingEntityData.type(),
                this.containingEntityData.copyTagWithoutId(),
                this.level,
                EntitySpawnReason.LOAD,
                EntityProcessor.NOP);
        this.containingEntityData = null;
        if (entity instanceof LivingEntity livingEntity) {
            this.containingEntity = livingEntity;
        }
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
