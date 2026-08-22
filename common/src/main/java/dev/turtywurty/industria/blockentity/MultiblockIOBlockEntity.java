package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockBlock;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockable;
import dev.turtywurty.industria.multiblock.old.Port;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MultiblockIOBlockEntity extends BlockEntity implements TickableBlockEntity {
    private BlockEntity primary = null;
    private AutoMultiblockable multiblock = null;
    private Vec3i offsetFromPrimary = null;

    public MultiblockIOBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.AUTO_MULTIBLOCK_IO.get(), pos, state);
    }

    @Override
    public void tick() {
        if (this.level == null)
            return;

        if (this.multiblock == null) {
            BlockPos primaryPos = AutoMultiblockBlock.getPrimaryPos(this.level, this.worldPosition);
            if (primaryPos == null)
                return;

            BlockEntity blockEntity = this.level.getBlockEntity(primaryPos);
            if (blockEntity instanceof AutoMultiblockable multiblockable) {
                this.primary = blockEntity;
                this.multiblock = multiblockable;
                this.offsetFromPrimary = AutoMultiblockBlock.getOffsetFromPrimary(primaryPos, this.worldPosition, blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
            }
        }

        if (this.multiblock == null || this.level.isClientSide())
            return;

        for (Direction direction : Direction.values()) {
            Map<Direction, Port> ports = getPorts(direction);
            if (ports == null)
                continue;

            Port port = ports.get(direction);
            if (port == null)
                continue;

            port.tick(this.level, this.worldPosition, this.primary.getBlockPos());
        }
    }

    public Map<Direction, Port> getPorts(Direction direction) {
        if (this.multiblock == null)
            return null;

        return this.multiblock.getPorts(offsetFromPrimary, direction);
    }

    public <T extends ResourceStorage<?>> T getProvider(TransferType<T, ?, ?> transferType, @Nullable Direction direction) {
        if (this.multiblock == null)
            return null;

        Map<Direction, Port> ports = this.multiblock.getPorts(offsetFromPrimary, direction);
        if (ports == null)
            return null;

        Port port = ports.get(direction);
        if (port == null)
            return null;

        return port.getProvider(transferType, this.level, this.primary.getBlockPos(), this.primary);
    }
}
