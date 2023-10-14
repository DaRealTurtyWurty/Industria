package dev.turtywurty.industria.event.listener;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.event.ItemEnterWaterEvent;
import dev.turtywurty.industria.init.ItemInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
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
        if (item == ItemInit.LITHIUM.get()) {
            event.setCanceled(true);

            Level level = entity.level();

            CompoundTag data = entity.getPersistentData();
            CompoundTag modidData = data.contains(Industria.MOD_ID, Tag.TAG_COMPOUND) ?
                    data.getCompound(Industria.MOD_ID) :
                    new CompoundTag();
            if (!modidData.contains("NextPosition", Tag.TAG_COMPOUND) || !modidData.contains("ByTime", Tag.TAG_LONG)) {
                double x = entity.getX() + (level.random.nextDouble() - 0.5D) * 5.0D;
                double y = entity.getY();
                double z = entity.getZ() + (level.random.nextDouble() - 0.5D) * 5.0D;

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

            if (isSamePosition(entity, x, y, z) || byTime < level.getGameTime() && !level.isClientSide()) {
                level.explode(entity, entity.getX(), entity.getY(), entity.getZ(), 1, true, Level.ExplosionInteraction.BLOCK);
                entity.remove(Entity.RemovalReason.DISCARDED);
                return;
            }

            data.put(Industria.MOD_ID, modidData);

            entity.setDeltaMovement(Vec3.ZERO);

            long gameTime = level.getGameTime();
            long timeLeft = byTime - gameTime;

            double currentX = entity.getX();
            double currentY = entity.getY();
            double currentZ = entity.getZ();

            double deltaX = x - currentX;
            double deltaY = y - currentY;
            double deltaZ = z - currentZ;

            double xPerTick = deltaX / timeLeft;
            double yPerTick = deltaY / timeLeft;
            double zPerTick = deltaZ / timeLeft;

            double newX = currentX + xPerTick;
            double newY = currentY + yPerTick;
            double newZ = currentZ + zPerTick;

            entity.setPos(newX, newY, newZ);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, entity.getX(), entity.getY(), entity.getZ(), 10, 0, 0, 0, 0.1D);
                if(level.getRandom().nextBoolean()) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY(), entity.getZ(), 7, 0, 0, 0, 0.1D);
                }

                serverLevel.sendParticles(ParticleTypes.BUBBLE, entity.getX(), entity.getY(), entity.getZ(), 2, 0, 0, 0, 0.1D);
            }
        }
    }

    private static boolean isSamePosition(Entity entity, double x, double y, double z) {
        return Math.abs(entity.getX() - x) < 0.1D && Math.abs(entity.getY() - y) < 0.1D && Math.abs(entity.getZ() - z) < 0.1D;
    }
}
