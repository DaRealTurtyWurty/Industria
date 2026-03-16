package dev.turtywurty.industria.conveyor.block.impl.entity;

import dev.turtywurty.industria.blockentity.IndustriaBlockEntity;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.ConveyorStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

// TODO: Maybe get rid of this and add magnetic chest or somethin
public class MagneticConveyorBlockEntity extends IndustriaBlockEntity implements TickableBlockEntity {
    private final AABB magneticBounds;

    public MagneticConveyorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.MAGNETIC_CONVEYOR, BlockEntityTypeInit.MAGNETIC_CONVEYOR, pos, state);
        this.magneticBounds = new AABB(pos).inflate(5);
    }

    @Override
    public void tick() {
        if (this.level == null || this.level.isClientSide())
            return;

        ServerLevel serverLevel = (ServerLevel) this.level;
        LevelConveyorNetworks networks = LevelConveyorNetworks.getOrCreate(serverLevel);
        ConveyorNetwork network = networks.getNetwork(this.worldPosition);
        if (network == null)
            return;

        ConveyorStorage storage = network.getStorage().getStorageAt(serverLevel, this.worldPosition);
        if (storage == null || !storage.canAcceptIncomingItem())
            return;

        List<ItemEntity> itemEntities = this.level.getEntitiesOfClass(ItemEntity.class, this.magneticBounds, Entity::isAlive);
        for (ItemEntity itemEntity : itemEntities) {
           pullItemEntity(itemEntity, storage);
        }
    }

    private void pullItemEntity(ItemEntity itemEntity, ConveyorStorage storage) {
        Vec3 conveyorPos = this.worldPosition.getCenter();
        Vec3 entityPos = itemEntity.position();

        Vec3 delta = conveyorPos.subtract(entityPos);
        Vec3 direction = delta.normalize();
        double distance = delta.length();
        double strength = Math.min((1.0D / distance) / 10D, distance);

        itemEntity.addDeltaMovement(direction.scale(strength));
        itemEntity.needsSync = true;

        if(distance < 1D) {
            addItemToStorage(storage, itemEntity);
        }
    }

    private void addItemToStorage(ConveyorStorage storage, ItemEntity itemEntity) {
        ItemStack entityStack = itemEntity.getItem();
        if (storage.addItem(new ConveyorItem(this.worldPosition, entityStack.copyWithCount(1)))) {
            ItemStack stack = entityStack.copyWithCount(entityStack.getCount() - 1);
            itemEntity.setItem(stack);
            if (stack.isEmpty()) {
                itemEntity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }
}
