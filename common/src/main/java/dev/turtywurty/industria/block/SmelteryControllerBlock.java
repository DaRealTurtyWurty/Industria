package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SmelteryControllerBlock extends IndustriaBlock {
    private static final VoxelShape COLLISION_SHAPE = Shapes.box(0, 0, 0, 0.9999, 0.9999, 0.9999);

    public SmelteryControllerBlock(Properties properties) {
        super(properties, new BlockProperties()
            .blockEntityProperties(
                new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.SMELTERY_CONTROLLER)
                    .shouldTick()
                    .rightClickToOpenGui())
            .hasHorizontalFacing()
            .hasLitProperty());
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }
}
