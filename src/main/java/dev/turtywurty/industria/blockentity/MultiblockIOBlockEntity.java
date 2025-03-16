package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.block.MultiblockBlock;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.multiblock.MultiblockIOPort;
import dev.turtywurty.industria.multiblock.Multiblockable;
import dev.turtywurty.industria.multiblock.TransferType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MultiblockIOBlockEntity extends BlockEntity implements TickableBlockEntity {
    private BlockEntity primary = null;
    private Multiblockable multiblock = null;
    private Vec3i offsetFromPrimary = null;
    private boolean waitingForPlayer = true;

    public MultiblockIOBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.MULTIBLOCK_IO, pos, state);
    }

    @Override
    public void tick() {
        if(this.world == null)
            return;

        if(this.multiblock == null) {
            BlockPos primaryPos = MultiblockBlock.getPrimaryPos(this.world, this.pos);
            if(primaryPos == null)
                return;

            BlockEntity blockEntity = this.world.getBlockEntity(primaryPos);
            if(blockEntity instanceof Multiblockable multiblockable) {
                this.primary = blockEntity;
                this.multiblock = multiblockable;
                this.offsetFromPrimary = MultiblockBlock.getOffsetFromPrimary(primaryPos, this.pos, blockEntity.getCachedState().get(Properties.HORIZONTAL_FACING));
            }
        }

        if(this.multiblock == null || this.world.isClient)
            return;

        for (Direction direction : Direction.values()) {
            Map<Direction, MultiblockIOPort> ports = getPorts(direction);
            if(ports == null)
                continue;

            MultiblockIOPort port = ports.get(direction);
            if(port == null)
                continue;

            port.tick(this.world, this.pos, this.primary.getPos());
        }
    }

    public Map<Direction, MultiblockIOPort> getPorts(Direction direction) {
        if(this.multiblock == null)
            return null;

        return this.multiblock.getPorts(offsetFromPrimary, direction);
    }

    public <T> T getProvider(TransferType<T,?> transferType, @Nullable Direction direction) {
        if(this.multiblock == null)
            return null;

        Map<Direction, MultiblockIOPort> ports = this.multiblock.getPorts(offsetFromPrimary, direction);
        if(ports == null)
            return null;

        MultiblockIOPort port = ports.get(direction);
        if(port == null)
            return null;

        return port.getProvider(transferType, this.world, this.primary.getPos(), this.primary);
    }
}
