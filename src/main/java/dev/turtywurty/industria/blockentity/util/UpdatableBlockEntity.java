package dev.turtywurty.industria.blockentity.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class UpdatableBlockEntity extends BlockEntity {
    protected boolean isDirty = false;

    public UpdatableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtWriteView view = NbtWriteView.create(ErrorReporter.EMPTY, registryLookup);
        writeData(view);
        return view.getNbt();
    }

    public void update() {
        this.isDirty = true;
        if (!shouldWaitForEndTick()) {
            markDirty();

            if (this.world != null && !this.world.isClient) {
                this.world.updateListeners(this.pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
            }
        }
    }

    public boolean shouldWaitForEndTick() {
        return true;
    }

    public void endTick() {
        if (this.isDirty) {
            this.isDirty = false;

            markDirty();

            if (this.world != null) {
                this.world.updateListeners(this.pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
            }
        }
    }
}
