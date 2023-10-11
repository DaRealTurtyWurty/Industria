package dev.turtywurty.industria.event;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class ItemEnterWaterEvent extends EntityEvent {
    public ItemEnterWaterEvent(ItemEntity entity) {
        super(entity);
    }

    public ItemEntity getItemEntity() {
        return (ItemEntity) this.getEntity();
    }
}
