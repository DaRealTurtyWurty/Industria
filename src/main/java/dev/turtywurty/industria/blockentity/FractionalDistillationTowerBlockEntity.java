package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FractionalDistillationTowerBlockEntity extends IndustriaBlockEntity implements SyncableTickableBlockEntity {
    private final WrappedFluidStorage<SingleFluidStorage> tank = new WrappedFluidStorage<>();
    private BlockPos controllerPos = null;
    private int ticks = 0;

    public FractionalDistillationTowerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.FRACTIONAL_DISTILLATION_TOWER, BlockEntityTypeInit.FRACTIONAL_DISTILLATION_TOWER, pos, state);
        this.tank.addStorage(new SyncingFluidStorage(this, FluidConstants.BUCKET * 5));
    }

    @Override
    public List<SyncableStorage> getSyncableStorages() {
        return List.of((SyncableStorage) this.tank.getStorage(0));
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        super.onBlockReplaced(pos, oldState);

        if (this.world == null || pos == null || oldState == null)
            return;

        BlockState newState = world.getBlockState(pos);
        if (!oldState.isOf(newState.getBlock())) {
            if (this.controllerPos != null && world.getBlockEntity(this.controllerPos) instanceof FractionalDistillationControllerBlockEntity controller) {
                controller.removeTower(pos);
            }
        }
    }

    @Override
    public void onTick() {
        if (this.world == null || this.world.isClient())
            return;

        if (this.ticks++ == 0) {
            this.controllerPos = searchForController();
            if (this.controllerPos == null) {
                this.world.breakBlock(this.pos, true);
            }
        } else if (this.ticks > Integer.MAX_VALUE - 1) {
            this.ticks = 1;
        }
    }

    private BlockPos searchForController() {
        for (int i = 1; i <= 8; i++) {
            BlockPos pos = this.pos.down(i);
            if (this.world.getBlockEntity(pos) instanceof FractionalDistillationControllerBlockEntity blockEntity) {
                if (!blockEntity.addTower(this.pos))
                    return null;

                return pos;
            }
        }

        return null;
    }

    @Override
    protected void readData(ReadView view) {
        this.controllerPos = BlockPos.fromLong(view.getLong("ControllerPos", 0L));
        this.ticks = view.getInt("Ticks", 0);
    }

    @Override
    protected void writeData(WriteView view) {
        if (this.controllerPos != null)
            view.putLong("ControllerPos", this.controllerPos.asLong());

        view.putInt("Ticks", this.ticks);
    }

    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    public WrappedFluidStorage<SingleFluidStorage> getTank() {
        return this.tank;
    }

    public @NotNull SingleFluidStorage getFluidStorage() {
        return getFluidProvider(null);
    }

    public @NotNull SingleFluidStorage getFluidProvider(@Nullable Direction side) {
        return this.tank.getStorage(0);
    }
}