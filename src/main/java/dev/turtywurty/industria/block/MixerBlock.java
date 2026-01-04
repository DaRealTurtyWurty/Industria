package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MixerBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = Shapes.box(-0.375, 0, -0.375, 1.375, 3, 1.375);

    public MixerBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasComparatorOutput()
                .hasBlockEntityRenderer()
                .constantShape(VOXEL_SHAPE)
                .blockEntityProperties(
                        new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.MIXER)
                                .shouldTick()
                                .multiblockProperties(MultiblockTypeInit.MIXER).build()));
    }
}
