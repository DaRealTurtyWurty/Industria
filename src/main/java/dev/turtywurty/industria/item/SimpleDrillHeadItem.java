package dev.turtywurty.industria.item;

import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.util.DrillHeadable;
import dev.turtywurty.industria.util.DrillRenderData;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class SimpleDrillHeadItem extends Item implements DrillHeadable {
    public SimpleDrillHeadItem(Settings settings) {
        super(settings);
    }

    @Override
    public DrillRenderData createRenderData() {
        return new DrillRenderData();
    }

    @Override
    public float updateDrill(DrillBlockEntity blockEntity, float drillYOffset) {
        World world = blockEntity.getWorld();
        BlockPos pos = blockEntity.getPos();

        BlockPos down = pos.up(MathHelper.ceil(blockEntity.getDrillYOffset()));
        BlockState state = world.getBlockState(down);
        float hardness = state.getHardness(world, pos);
        drillYOffset -= ((hardness == -1 || hardness == 0) ? 0.01F : (1F / (hardness + 5)));

        if (!state.getFluidState().isEmpty()) {
            blockEntity.setDrilling(false);
            blockEntity.setRetracting(true);
            return drillYOffset;
        } else if (state.isAir()) {
            drillYOffset -= 0.1F;
        }

        if (WorldFluidPocketsState.getServerState((ServerWorld) world).isPositionInPocket(down)) {
            blockEntity.setDrilling(false);
            blockEntity.setRetracting(true);
            return drillYOffset - 0.1F;
        }

        boolean isThis = false;
        if (state.isOf(BlockInit.MULTIBLOCK_BLOCK) || state.isOf(BlockInit.DRILL)) {
            drillYOffset -= 0.1F;
            isThis = true;
        }

        if (world.getBlockState(down).getHardness(world, pos) == -1F || blockEntity.getDrillYOffset() < world.getBottomY() - pos.getY()) {
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
