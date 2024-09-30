package dev.turtywurty.industria.entity;

import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DrillHeadEntity extends Entity {
    private DrillBlockEntity blockEntity;
    private static final TrackedData<Boolean> IS_DRILLING = DataTracker.registerData(DrillHeadEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public DrillHeadEntity(EntityType<DrillHeadEntity> type, World world) {
        super(type, world);
    }

    public void setBlockEntity(@Nullable DrillBlockEntity blockEntity) {
        this.blockEntity = blockEntity;

        if (blockEntity != null) {
            setPos(blockEntity.getPos().getX() + 0.5, blockEntity.getPos().getY() + 0.5, blockEntity.getPos().getZ() + 0.5);
        } else {
            remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if(this.blockEntity == null) {
            remove(RemovalReason.DISCARDED);
            return;
        }

        if(this.blockEntity.getWorld() == null || this.blockEntity.getWorld().isClient)
            return;

        if(this.blockEntity.isRemoved()) {
            remove(RemovalReason.DISCARDED);
            return;
        }

        if(this.blockEntity.getWorld().getBlockEntity(this.blockEntity.getPos()) != this.blockEntity) {
            remove(RemovalReason.DISCARDED);
            return;
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(IS_DRILLING, false);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if(nbt.contains("BlockEntityPos", NbtElement.INT_ARRAY_TYPE)) {
            setBlockEntity(NbtHelper.toBlockPos(nbt, "BlockEntityPos")
                    .map(getWorld()::getBlockEntity)
                    .filter(DrillBlockEntity.class::isInstance)
                    .map(DrillBlockEntity.class::cast)
                    .orElse(null));
        }

        if (nbt.contains("IsDrilling", NbtElement.BYTE_TYPE)) {
            this.dataTracker.set(IS_DRILLING, nbt.getBoolean("IsDrilling"));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (blockEntity != null) {
            nbt.put("BlockEntityPos", NbtHelper.fromBlockPos(blockEntity.getPos()));
        }

        nbt.putBoolean("IsDrilling", this.dataTracker.get(IS_DRILLING));
    }

    public boolean isDrilling() {
        return this.dataTracker.get(IS_DRILLING);
    }

    public DrillBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
