package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class DoorContainerBlockEntity extends IndustriaBlockEntity implements TickableBlockEntity, LidBlockEntity {
    private static final int EVENT_SET_OPEN_COUNT = 1;

    private final ChestLidController doorController = new ChestLidController();
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            playDoorSound(level, pos, SoundEvents.IRON_DOOR_OPEN);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            playDoorSound(level, pos, SoundEvents.IRON_DOOR_CLOSE);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int previous, int current) {
            level.blockEvent(pos, state.getBlock(), EVENT_SET_OPEN_COUNT, current);
        }

        @Override
        public boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof IndustriaScreenHandler<?, ?> menu
                && menu.getBlockEntity() == DoorContainerBlockEntity.this;
        }
    };

    protected DoorContainerBlockEntity(IndustriaBlock block, BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(block, type, pos, state);
    }

    public void startOpen(ContainerUser user) {
        if (!isRemoved() && this.level != null && !this.level.isClientSide() && !user.getLivingEntity().isSpectator()) {
            this.openersCounter.incrementOpeners(user.getLivingEntity(), this.level, this.worldPosition,
                getBlockState(), user.getContainerInteractionRange());
        }
    }

    public void stopOpen(ContainerUser user) {
        if (!isRemoved() && this.level != null && !this.level.isClientSide() && !user.getLivingEntity().isSpectator()) {
            this.openersCounter.decrementOpeners(user.getLivingEntity(), this.level, this.worldPosition, getBlockState());
        }
    }

    public void recheckOpen() {
        if (!isRemoved() && this.level != null && !this.level.isClientSide()) {
            this.openersCounter.recheckOpeners(this.level, this.worldPosition, getBlockState());
        }
    }

    @Override
    public void tick() {
        if (this.level != null && this.level.isClientSide()) {
            this.doorController.tickLid();
        }
    }

    @Override
    public boolean triggerEvent(int id, int data) {
        if (id == EVENT_SET_OPEN_COUNT) {
            this.doorController.shouldBeOpen(data > 0);
            return true;
        }

        return super.triggerEvent(id, data);
    }

    @Override
    public float getOpenNess(float partialTick) {
        return this.doorController.getOpenness(partialTick);
    }

    private static void playDoorSound(Level level, BlockPos pos, SoundEvent sound) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }
}
