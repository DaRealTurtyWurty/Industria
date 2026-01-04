package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.SyncableStorage;
import dev.turtywurty.industria.blockentity.util.SyncableTickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.blockentity.util.fluid.WrappedFluidStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);

        if (this.level == null || pos == null || oldState == null)
            return;

        BlockState newState = level.getBlockState(pos);
        if (!oldState.is(newState.getBlock())) {
            if (this.controllerPos != null && level.getBlockEntity(this.controllerPos) instanceof FractionalDistillationControllerBlockEntity controller) {
                controller.removeTower(pos);
            }
        }
    }

    @Override
    public void onTick() {
        if (this.level == null || this.level.isClientSide())
            return;

        if (this.ticks++ == 0) {
            this.controllerPos = searchForController();
            if (this.controllerPos == null) {
                this.level.destroyBlock(this.worldPosition, true);
            }
        } else if (this.ticks > Integer.MAX_VALUE - 1) {
            this.ticks = 1;
        }
    }

    private BlockPos searchForController() {
        for (int i = 1; i <= 8; i++) {
            BlockPos pos = this.worldPosition.below(i);
            if (this.level.getBlockEntity(pos) instanceof FractionalDistillationControllerBlockEntity blockEntity) {
                if (!blockEntity.addTower(this.worldPosition))
                    return null;

                return pos;
            }
        }

        return null;
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        this.controllerPos = BlockPos.of(view.getLongOr("ControllerPos", 0L));
        this.ticks = view.getIntOr("Ticks", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
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