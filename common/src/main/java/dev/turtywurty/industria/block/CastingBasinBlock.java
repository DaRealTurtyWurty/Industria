package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.CastingBasinBlockEntity;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CastingBasinBlock extends IndustriaBlock {
    private static final VoxelShape SHAPE = makeShape();

    public CastingBasinBlock(Properties settings) {
        super(settings, new BlockProperties()
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(
                ModBlockEntityTypes.CASTING_BASIN)
                .shouldTick()
                .dropContentsOnBreak())
            .constantShape(SHAPE));
    }

    private static VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0.125, 0, 1, 0.3125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.03125, 0, 0.03125, 0.28125, 0.125, 0.28125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.03125, 0, 0.71875, 0.28125, 0.125, 0.96875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.71875, 0, 0.71875, 0.96875, 0.125, 0.96875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.71875, 0, 0.03125, 0.96875, 0.125, 0.28125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.3125, 0, 0.9375, 1, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.3125, 0.9375, 0.9375, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.3125, 0, 0.0625, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.3125, 0, 1, 1, 1), BooleanOp.OR);
        return shape.optimize();
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hit
    ) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CastingBasinBlockEntity blockEntity) {
            blockEntity.collectOutput(player);
        }

        return InteractionResult.SUCCESS;
    }
}
