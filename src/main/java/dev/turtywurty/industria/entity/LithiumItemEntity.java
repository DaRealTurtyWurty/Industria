package dev.turtywurty.industria.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class LithiumItemEntity extends ItemEntity {
    private static final Vector3f EMPTY_VECTOR = new Vector3f();

    private static final EntityDataAccessor<Vector3f> NEXT_POSITION =
            SynchedEntityData.defineId(LithiumItemEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Long> BY_TIME =
            SynchedEntityData.defineId(LithiumItemEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> TIME_TO_REACH =
            SynchedEntityData.defineId(LithiumItemEntity.class, EntityDataSerializers.LONG);

    public LithiumItemEntity(EntityType<LithiumItemEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(NEXT_POSITION, EMPTY_VECTOR);
        this.entityData.define(BY_TIME, 0L);
        this.entityData.define(TIME_TO_REACH, 0L);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        CompoundTag nextPosition = new CompoundTag();
        nextPosition.putFloat("X", getNextPosition().x());
        nextPosition.putFloat("Y", getNextPosition().y());
        nextPosition.putFloat("Z", getNextPosition().z());
        pCompound.put("NextPosition", nextPosition);
        pCompound.putLong("ByTime", getByTime());
        pCompound.putLong("TimeToReach", getTimeToReach());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        CompoundTag nextPosition = pCompound.getCompound("NextPosition");
        setNextPosition(new Vector3f(
                nextPosition.getFloat("X"),
                nextPosition.getFloat("Y"),
                nextPosition.getFloat("Z")
        ));

        setByTime(pCompound.getLong("ByTime"));
        setTimeToReach(pCompound.getLong("TimeToReach"));
    }

    public Vector3f getNextPosition() {
        return this.entityData.get(NEXT_POSITION);
    }

    public void setNextPosition(Vector3f nextPosition) {
        this.entityData.set(NEXT_POSITION, nextPosition);
    }

    public long getByTime() {
        return this.entityData.get(BY_TIME);
    }

    public void setByTime(long byTime) {
        this.entityData.set(BY_TIME, byTime);
    }

    public long getTimeToReach() {
        return this.entityData.get(TIME_TO_REACH);
    }

    public void setTimeToReach(long timeToReach) {
        this.entityData.set(TIME_TO_REACH, timeToReach);
    }

    @Override
    public void tick() {
        if (getItem().onEntityItemUpdate(this))
            return;

        if (getItem().isEmpty()) {
            discard();
        } else {
            super.baseTick();
            if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
                --this.pickupDelay;
            }

            this.xo = getX();
            this.yo = getY();
            this.zo = getZ();
            Vec3 movement = getDeltaMovement();
            float underwaterHeight = getEyeHeight() - 0.11111111F;
            FluidType fluidType = getMaxHeightFluidType();
            if (!fluidType.isAir() && !fluidType.isVanilla() && getFluidTypeHeight(fluidType) > (double)underwaterHeight) {
                fluidType.setItemMovement(this);
            } else if (isInWater() && getFluidHeight(FluidTags.WATER) > (double)underwaterHeight) {
                applySurfaceLogic();
                return;
            } else if (isInLava() && getFluidHeight(FluidTags.LAVA) > (double)underwaterHeight) {
                setUnderLavaMovement();
            } else if (!isNoGravity()) {
                setDeltaMovement(getDeltaMovement().add(0.0D, -0.04D, 0.0D));
            }

            if (level().isClientSide) {
                this.noPhysics = false;
            } else {
                this.noPhysics = !level().noCollision(this, getBoundingBox().deflate(1.0E-7D));
                if (this.noPhysics) {
                    moveTowardsClosestSpace(this.getX(), (getBoundingBox().minY + getBoundingBox().maxY) / 2.0D, getZ());
                }
            }

            if (!onGround() || getDeltaMovement().horizontalDistanceSqr() > (double)1.0E-5F || (this.tickCount + getId()) % 4 == 0) {
                move(MoverType.SELF, getDeltaMovement());
                float friction = 0.98F;
                if (this.onGround()) {
                    BlockPos groundPos = getBlockPosBelowThatAffectsMyMovement();
                    friction = level().getBlockState(groundPos).getFriction(level(), groundPos, this) * 0.98F;
                }

                setDeltaMovement(getDeltaMovement().multiply(friction, 0.98D, friction));
                if (onGround()) {
                    Vec3 newMovement = getDeltaMovement();
                    if (newMovement.y < 0.0D) {
                        setDeltaMovement(newMovement.multiply(1.0D, -0.5D, 1.0D));
                    }
                }
            }

            boolean hasMovedBlock = Mth.floor(this.xo) != Mth.floor(this.getX()) || Mth.floor(this.yo) != Mth.floor(this.getY()) || Mth.floor(this.zo) != Mth.floor(this.getZ());
            int i = hasMovedBlock ? 2 : 40;
            if (this.tickCount % i == 0 && !level().isClientSide && this.isMergable()) {
                mergeWithNeighbours();
            }

            if (this.age != -32768) {
                ++this.age;
            }

            this.hasImpulse |= updateInWaterStateAndDoFluidPushing();
            if (!level().isClientSide) {
                double d0 = getDeltaMovement().subtract(movement).lengthSqr();
                if (d0 > 0.01D) {
                    this.hasImpulse = true;
                }
            }

            ItemStack item = this.getItem();
            if (!level().isClientSide && this.age >= lifespan) {
                int hook = ForgeEventFactory.onItemExpire(this, item);
                if (hook < 0) {
                    discard();
                } else {
                    this.lifespan += hook;
                }
            }
            if (item.isEmpty() && !isRemoved()) {
                discard();
            }
        }
    }

    private void applySurfaceLogic() {
        Level level = level();
        RandomSource random = level.random;

        long totalTime;
        if (getTimeToReach() <= 0) {
            setTimeToReach(totalTime = 20L);
            setNextPosition(EMPTY_VECTOR);
        } else {
            totalTime = getTimeToReach();
        }

        if (getNextPosition() == EMPTY_VECTOR || getNextPosition() == null) {
            double x = getX() +
                    random.triangle(0, 5) *
                            Mth.randomBetweenInclusive(random, -1, 1) +
                    (random.nextDouble() * 10D) - 5D;

            double z = getZ() +
                    random.triangle(0, 5) *
                            Mth.randomBetweenInclusive(random, -1, 1) +
                    (random.nextDouble() * 10D) - 5D;

            double y = getY();

            setNextPosition(new Vector3f((float) x, (float) y, (float) z));
        }

        if (getByTime() == 0) {
            setByTime(level.getGameTime() + random.nextInt(100) + 100L);
        }

        Vector3f nextPosition = getNextPosition();
        double x = nextPosition.x();
        double y = nextPosition.y();
        double z = nextPosition.z();

        long byTime = getByTime();
        long gameTime = level.getGameTime();
        long timeLeft = byTime - gameTime;

        if (timeLeft <= 0 && !level.isClientSide()) {
            level.explode(this, getX(), getY(), getZ(), 1, true, Level.ExplosionInteraction.BLOCK);
            remove(Entity.RemovalReason.DISCARDED);
            return;
        }

        double currentX = getX();
        double currentY = getY();
        double currentZ = getZ();

        double deltaX = x - currentX;
        double deltaY = y - currentY;
        double deltaZ = z - currentZ;

        if (totalTime >= 0) {
            double stepX = deltaX / totalTime;
            double stepY = deltaY / totalTime;
            double stepZ = deltaZ / totalTime;

            setPos(currentX + stepX, currentY + stepY, currentZ + stepZ);
            setTimeToReach(totalTime - 1);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMALL_FLAME, currentX, currentY, currentZ, 2, 0, 0, 0, 0.1D);
            serverLevel.sendParticles(ParticleTypes.BUBBLE, currentX, currentY, currentZ, 10, 0, 0, 0, 0.1D);

            if (random.nextBoolean())
                serverLevel.sendParticles(ParticleTypes.SMOKE, currentX, currentY, currentZ, 7, 0, 0, 0, 0.1D);
        }

        if (random.nextInt(200) == 0) {
            setNextPosition(EMPTY_VECTOR);
            setByTime(0);
            setTimeToReach(0);
        }
    }

    @Override
    protected boolean isMergable() {
        return super.isMergable() && !isInWater();
    }
}
