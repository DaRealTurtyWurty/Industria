package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DistillationTowerBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = Shapes.block();

    public DistillationTowerBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasComparatorOutput()
                .hasBlockEntityRenderer()
                .useRotatedShapes(VOXEL_SHAPE)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(ModBlockEntityTypes.DISTILLATION_TOWER)
                        .dropContentsOnBreak()
                        .rightClickToOpenGui()
                        .shouldTick()
                ));
    }
}
