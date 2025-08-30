package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.util.ExtraCodecs;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ExampleMultiblockControllerBlockEntity extends UpdatableBlockEntity {
    private final Set<BlockPos> positions = new HashSet<>();

    public ExampleMultiblockControllerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.EXAMPLE_MULTIBLOCK_CONTROLLER, pos, state);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.put("MultiblockPositions", ExtraCodecs.BLOCK_POS_SET_CODEC, this.positions);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.positions.clear();
        this.positions.addAll(view.read("MultiblockPositions", ExtraCodecs.BLOCK_POS_SET_CODEC).orElse(Set.of()));
    }

    public void addPositions(Collection<BlockPos> positions) {
        if (!hasWorld() || world.isClient())
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
