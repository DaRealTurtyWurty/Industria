package dev.turtywurty.industria.event.listener;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.event.ItemEnterWaterEvent;
import dev.turtywurty.industria.init.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Industria.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventListener {
    @SubscribeEvent
    public static void itemEnterWater(ItemEnterWaterEvent event) {
        ItemEntity entity = event.getItemEntity();
        ItemStack stack = event.getItemEntity().getItem();
        Item item = stack.getItem();
        if(item == ItemInit.LITHIUM.get()) {
            Level level = entity.level();
//            level.explode(entity, entity.getX(), entity.getY(), entity.getZ(), 1, true, Level.ExplosionInteraction.BLOCK);
//            entity.remove(Entity.RemovalReason.DISCARDED);

            CompoundTag data = entity.getPersistentData();
            CompoundTag modidData = data.contains(Industria.MOD_ID, Tag.TAG_COMPOUND) ?
                    data.getCompound(Industria.MOD_ID) :
                    new CompoundTag();
            if(!modidData.contains("NextPosition", Tag.TAG_COMPOUND) || !modidData.contains("ByTime", Tag.TAG_LONG)) {
                double x = entity.getX() + (level.random.nextDouble() - 0.5D) * 10.0D;
                double y = entity.getY();
                double z = entity.getZ() + (level.random.nextDouble() - 0.5D) * 10.0D;

                var nextPosition = new CompoundTag();
                nextPosition.putDouble("X", x);
                nextPosition.putDouble("Y", y);
                nextPosition.putDouble("Z", z);

                modidData.put("NextPosition", nextPosition);
                modidData.putLong("ByTime", level.getGameTime() + 20L);
            }

            CompoundTag nextPosition = modidData.getCompound("NextPosition");
            double x = nextPosition.getDouble("X");
            double y = nextPosition.getDouble("Y");
            double z = nextPosition.getDouble("Z");

            long byTime = modidData.getLong("ByTime");

            if (entity.getX() == x && entity.getY() == y && entity.getZ() == z) {
                level.explode(entity, entity.getX(), entity.getY(), entity.getZ(), 1, true, Level.ExplosionInteraction.BLOCK);
                entity.remove(Entity.RemovalReason.DISCARDED);
                event.setCanceled(true);
                return;
            }

            entity.setDeltaMovement(Vec3.ZERO);

            long gameTime = level.getGameTime();
            long timeLeft = byTime - gameTime;

            // set position between current position and next position over the time left
            // we dont modify the motion, we have to directly modify the position because we have to cancel the event

            double xDiff = x - entity.getX();
            double yDiff = y - entity.getY();
            double zDiff = z - entity.getZ();

            double xMove = xDiff / timeLeft;
            double yMove = yDiff / timeLeft;
            double zMove = zDiff / timeLeft;

            entity.setPos(entity.getX() + xMove, entity.getY() + yMove, entity.getZ() + zMove);
            event.setCanceled(true);
        }
    }
}
