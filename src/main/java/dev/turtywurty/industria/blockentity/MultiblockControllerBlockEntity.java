package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.util.ExtraCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MultiblockControllerBlockEntity extends UpdatableBlockEntity {
    private final Set<BlockPos> positions = new HashSet<>();

    public MultiblockControllerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.EXAMPLE_MULTIBLOCK_CONTROLLER, pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.store("MultiblockPositions", ExtraCodecs.BLOCK_POS_SET_CODEC, this.positions);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.positions.clear();
        this.positions.addAll(view.read("MultiblockPositions", ExtraCodecs.BLOCK_POS_SET_CODEC).orElse(Set.of()));
    }

    public void addPositions(Collection<BlockPos> positions) {
        if (this.level == null || this.level.isClientSide())
            return;

        this.positions.addAll(positions);
        update();
    }

    @Override
    public boolean shouldWaitForEndTick() {
        return false;
    }

    public Set<BlockPos> getPositions() {
        return Set.copyOf(this.positions);
    }
}
