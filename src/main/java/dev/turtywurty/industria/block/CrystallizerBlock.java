package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrystallizerBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = Shapes.or(
            Shapes.box(-0.75, 3.875, -0.75, 1.75, 4, 1.75),
            Shapes.box(-0.75, 0.125, -0.75, -0.625, 3.875, 1.75),
            Shapes.box(1.625, 0.125, -0.75, 1.75, 3.875, 1.75),
            Shapes.box(-0.625, 1, -0.75, 1.625, 3.875, -0.625),
            Shapes.box(-0.625, 0.125, 1.625, 1.625, 3.875, 1.75),
            Shapes.box(-0.75, 0, -0.75, 1.75, 0.125, 1.75),
            Shapes.box(-0.625, 0.125, -0.75, 0, 0.875, -0.625),
            Shapes.box(1, 0.125, -0.75, 1.625, 0.875, -0.625),
            Shapes.box(0, 0.125, -0.8125, 1, 0.875, -0.625),
            Shapes.box(-0.625, 0.875, -0.75, 1.625, 1, -0.625),
            Shapes.box(-0.625, 0.875, -0.625, 1.625, 1, 1.625)
    ).optimize();

    public CrystallizerBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasComparatorOutput()
                .hasBlockEntityRenderer()
                .useRotatedShapes(VOXEL_SHAPE)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.CRYSTALLIZER)
                        .dropContentsOnBreak()
                        .rightClickToOpenGui()
                        .shouldTick()
                        .multiblockProperties(MultiblockTypeInit.CRYSTALLIZER).build()));
    }
}
