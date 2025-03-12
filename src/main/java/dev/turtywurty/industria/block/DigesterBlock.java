package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class DigesterBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = VoxelShapes.cuboid(-0.75, 0, -0.75, 1.75, 5, 1.75);

    public DigesterBlock(Settings settings) {
        super(settings, new BlockProperties()
                .hasComparatorOutput()
                .hasHorizontalFacing()
                .constantShape(VOXEL_SHAPE)
                .hasBlockEntityRenderer()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.DIGESTER)
                        .shouldTick()
                        .rightClickToOpenGui()
                        .dropContentsOnBreak()
                        .multiblockProperties(MultiblockTypeInit.DIGESTER).build()));
    }
}
