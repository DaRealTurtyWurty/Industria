package dev.turtywurty.industria.event.listener;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.event.ItemEnterWaterEvent;
import dev.turtywurty.industria.init.ItemInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
        if (item == ItemInit.LITHIUM.get()) {
            event.setCanceled(true);

            Level level = entity.level();

            CompoundTag data = entity.getPersistentData();
            CompoundTag modidData = data.contains(Industria.MOD_ID, Tag.TAG_COMPOUND) ?
                    data.getCompound(Industria.MOD_ID) :
                    new CompoundTag();
            if (!modidData.contains("NextPosition", Tag.TAG_COMPOUND)) {
                double x = entity.getX() +
                        level.random.triangle(0, 5) *
                        Mth.randomBetweenInclusive(level.random, -1, 1) +
                        level.random.nextDouble() - 0.5D;

                double z = entity.getZ() +
                        level.random.triangle(0, 5) *
                                Mth.randomBetweenInclusive(level.random, -1, 1) +
                        level.random.nextDouble() - 0.5D;

                double y = entity.getY();

                var nextPosition = new CompoundTag();
                nextPosition.putDouble("X", x);
                nextPosition.putDouble("Y", y);
                nextPosition.putDouble("Z", z);

                modidData.put("NextPosition", nextPosition);
            }

            if(!modidData.contains("ByTime", Tag.TAG_LONG)) {
                modidData.putLong("ByTime", level.getGameTime() + 100L);
            }

            CompoundTag nextPosition = modidData.getCompound("NextPosition");
            double x = nextPosition.getDouble("X");
            double y = nextPosition.getDouble("Y");
            double z = nextPosition.getDouble("Z");

            long byTime = modidData.getLong("ByTime");
            long gameTime = level.getGameTime();
            long timeLeft = byTime - gameTime;

            if (timeLeft < 0 && !level.isClientSide()) {
                level.explode(entity, entity.getX(), entity.getY(), entity.getZ(), 1, true, Level.ExplosionInteraction.BLOCK);
                entity.remove(Entity.RemovalReason.DISCARDED);
                return;
            }

            double currentX = entity.getX();
            double currentY = entity.getY();
            double currentZ = entity.getZ();

            double deltaX = x - currentX;
            double deltaY = y - currentY;
            double deltaZ = z - currentZ;

            long totalTime = modidData.contains("TotalTime", Tag.TAG_LONG) ?
                    modidData.getLong("TotalTime") :
                    20L;

            if (totalTime > 0) {
                double stepX = deltaX / totalTime;
                double stepY = deltaY / totalTime;
                double stepZ = deltaZ / totalTime;

                entity.move(MoverType.SELF, new Vec3(stepX, stepY, stepZ));
                modidData.putLong("TotalTime", totalTime - 1);
            } else {
                entity.setPos(x, y, z);
                modidData.remove("NextPosition");
                modidData.remove("TotalTime");
            }

            data.put(Industria.MOD_ID, modidData);
            entity.hurtMarked = true;

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, entity.getX(), entity.getY(), entity.getZ(), 10, 0, 0, 0, 0.1D);
                if(level.getRandom().nextBoolean()) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, entity.getX(), entity.getY(), entity.getZ(), 7, 0, 0, 0, 0.1D);
                }

                serverLevel.sendParticles(ParticleTypes.BUBBLE, entity.getX(), entity.getY(), entity.getZ(), 10, 0, 0, 0, 0.1D);
            }
        }
    }

    private static boolean isSamePosition(Entity entity, double x, double y, double z) {
        return Math.abs(entity.getX() - x) < 0.1D && Math.abs(entity.getY() - y) < 0.1D && Math.abs(entity.getZ() - z) < 0.1D;
    }
}
