package dev.turtywurty.industria.block;

import com.mojang.math.OctahedralGroup;
import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.CokeOvenBlockEntity;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class CokeOvenBlock extends IndustriaBlock {
    private static final Map<Direction, VoxelShape> FORMED_SHAPES = Shapes.rotateHorizontal(
        createShape().move(0, 0, 1), OctahedralGroup.BLOCK_ROT_Y_180);

    public CokeOvenBlock(Properties settings) {
        super(settings.dynamicShape(), new BlockProperties()
            .hasHorizontalFacing()
            .hasBlockEntityRenderer()
            .hasLitProperty()
            .shapeFactory((state, level, pos, _) -> level.getBlockEntity(pos) instanceof CokeOvenBlockEntity oven
                && oven.isFormed()
                    ? FORMED_SHAPES.get(state.getValue(BlockStateProperties.HORIZONTAL_FACING))
                    : Shapes.block())
            .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.COKE_OVEN)
                .shouldTick()
                .dropContentsOnBreak()
                .rightClickToOpenGui()));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(BlockStateProperties.LIT)
            || !(level.getBlockEntity(pos) instanceof CokeOvenBlockEntity oven) || !oven.isFormed())
            return;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Vec3 chimney = Vec3.atBottomCenterOf(pos.relative(facing.getClockWise()))
            .relative(facing, 2.375).add(0, 2.8, 0);
        level.addParticle(ModParticleTypes.COKE_OVEN_SMOKE.get(),
            chimney.x + random.triangle(0, 0.1), chimney.y, chimney.z + random.triangle(0, 0.1),
            random.triangle(0, 0.004), 0.03 + random.nextDouble() * 0.008, random.triangle(0, 0.004));
    }

    private static VoxelShape createShape() {
        return Shapes.or(
            Shapes.box(-0.75, 0, -1, -0.25, 2, 3),
            Shapes.box(1.75, 0, 1, 1.875, 0.5, 3),
            Shapes.box(-0.5, 2, -1, 1.5, 2.125, 3),
            Shapes.box(-0.9375, 0, -0.875, -0.6875, 1.9375, -0.625),
            Shapes.box(1.6875, 0, -0.875, 1.9375, 1.9375, -0.625),
            Shapes.box(1.6875, 0, 2.625, 1.9375, 1.9375, 2.875),
            Shapes.box(1.6875, 0, 0.875, 1.9375, 1.9375, 1.125),
            Shapes.box(-1, -0.00625, -0.9375, -0.625, 0.36875, -0.5625),
            Shapes.box(-0.9375, 1.9375, -0.875, 1.9375, 2.1875, -0.625),
            Shapes.box(-0.9375, 0.0625, -0.5625, -0.6875, 0.3125, 0.8125),
            Shapes.box(1.25, 0, -1, 1.75, 2, 3),
            Shapes.box(-0.25, 1.75, -1, 1.25, 2, 3),
            Shapes.box(-0.25, 0, -1, 1.25, 0.5, 3),
            Shapes.box(-0.25, 0.5, 0.5, 1.25, 1.75, 3),
            Shapes.box(-0.8125, 0.00625, 1.5625, -0.1875, 2.50625, 2.1875),
            Shapes.box(-0.875, 2.5, 1.5, -0.125, 2.75, 2.25),
            Shapes.box(-0.5625, 0.3125, 2.875, -0.4375, 0.6875, 3.25),
            Shapes.box(1.4375, 0.3125, 2.875, 1.5625, 0.6875, 3.25),
            Shapes.box(1.5625, 0.375, 0.3125, 1.8125, 0.625, 0.6875),
            Shapes.box(1.875, 0.3125, 0.3125, 2, 0.6875, 0.6875),
            Shapes.box(1.75, 0, 0.75, 1.875, 0.5, 0.875),
            Shapes.box(1.75, 0, -1, 1.875, 0.5, 0.25),
            Shapes.box(-0.875, 0, 1, -0.75, 0.5, 3),
            Shapes.box(-0.875, 0, 0.75, -0.75, 0.5, 0.875),
            Shapes.box(-0.875, 0, -1, -0.75, 0.5, 0.25),
            Shapes.box(0.3125, 1.875, -0.9375, 0.6875, 2.25, -0.5625),
            Shapes.box(-1.125, 1.9375, 0.875, 2.125, 2.1875, 1.125),
            Shapes.box(-1, -0.00625, 0.8125, -0.625, 0.36875, 1.1875),
            Shapes.box(-1, -0.00625, 2.5625, -0.625, 0.36875, 2.9375),
            Shapes.box(1.625, -0.00625, -0.9375, 2, 0.36875, -0.5625),
            Shapes.box(1.625, -0.00625, 0.8125, 2, 0.36875, 1.1875),
            Shapes.box(1.625, -0.00625, 2.5625, 2, 0.36875, 2.9375),
            Shapes.box(0.3125, 1.875, 0.8125, 0.6875, 2.25, 1.1875),
            Shapes.box(0.3125, 1.875, 2.5625, 0.6875, 2.25, 2.9375),
            Shapes.box(-0.9375, 0.0625, 1.1875, -0.6875, 0.3125, 2.5625),
            Shapes.box(-0.9375, 0, 0.875, -0.6875, 1.9375, 1.125),
            Shapes.box(-0.9375, 0, 2.625, -0.6875, 1.9375, 2.875),
            Shapes.box(1.6875, 0.0625, -0.5625, 1.9375, 0.3125, 0.8125),
            Shapes.box(1.6875, 0.0625, 1.1875, 1.9375, 0.3125, 2.5625),
            Shapes.box(-0.9375, 1.9375, 0.875, 1.9375, 2.1875, 1.125),
            Shapes.box(-0.9375, 1.9375, 2.625, 1.9375, 2.1875, 2.875),
            Shapes.box(-0.75, 0.3125, 0.3125, -0.625, 0.6875, 0.6875),
            Shapes.box(-0.9375, 0.375, 0.1875, -0.6875, 0.625, 0.5625));
    }
}
