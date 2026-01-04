package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ElectrolyzerBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = Shapes.box(-1, 0, 0, 2, 2, 2);

    public ElectrolyzerBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasBlockEntityRenderer()
                .hasComparatorOutput()
                .hasHorizontalFacing()
                .useRotatedShapes(VOXEL_SHAPE)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.ELECTROLYZER)
                        .shouldTick()
                        .rightClickToOpenGui()
                        .dropContentsOnBreak()
                        .multiblockProperties(MultiblockTypeInit.ELECTROLYZER).build()));
    }
}
