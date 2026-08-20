package dev.turtywurty.industria.item;

import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.util.DrillHeadable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleDrillHeadItem extends Item implements DrillHeadable {
    public SimpleDrillHeadItem(Properties settings) {
        super(settings);
    }

    @Override
    public float updateDrill(DrillBlockEntity blockEntity, float drillYOffset) {
        Level world = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();

        BlockPos down = pos.above(Mth.ceil(blockEntity.getDrillYOffset()));
        BlockState state = world.getBlockState(down);
        float hardness = state.getDestroySpeed(world, pos);
        drillYOffset -= ((hardness == -1 || hardness == 0) ? 0.01F : (1F / (hardness + 5)));

        if (!state.getFluidState().isEmpty()) {
            blockEntity.setDrilling(false);
            blockEntity.setRetracting(true);
            return drillYOffset;
        } else if (state.isAir()) {
            drillYOffset -= 0.1F;
        }

        if(WorldFluidPocketsState.getServerState((ServerLevel) world).isPositionInPocket(down)) {
            blockEntity.setDrilling(false);
            blockEntity.setRetracting(true);
            return drillYOffset - 0.1F;
        }

        boolean isThis = false;
        if (state.is(BlockInit.AUTO_MULTIBLOCK_BLOCK) || state.is(BlockInit.DRILL)) {
            drillYOffset -= 0.1F;
            isThis = true;
        }

        if (world.getBlockState(down).getDestroySpeed(world, pos) == -1F || blockEntity.getDrillYOffset() < world.getMinY() - pos.getY()) {
            blockEntity.setDrilling(false);
            blockEntity.setRetracting(true);
            drillYOffset += 0.01F;
        }

        if (!blockEntity.isRetracting() && (drillYOffset - Math.floor(drillYOffset) > 0.75F) && !isThis && !state.isAir()) {
            blockEntity.handleBlockBreak(down, state);
            drillYOffset += 0.01F;
        }

        return drillYOffset;
    }

    @Override
    public float updateRetracting(DrillBlockEntity blockEntity, float drillYOffset) {
        drillYOffset += 0.05F;
        if (drillYOffset >= 1.0F) {
            blockEntity.setDrilling(false);
            blockEntity.setRetracting(false);
            drillYOffset = 1.0F;
        }

        return drillYOffset;
    }
}
