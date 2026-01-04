package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FluidTankBlock extends IndustriaBlock {
    private static final VoxelShape VOXEL_SHAPE = Shapes.box(0.1875, 0, 0.1875, 0.8125, 0.9375, 0.8125);

    public FluidTankBlock(Properties settings) {
        super(settings, new BlockProperties()
                .constantShape(VOXEL_SHAPE)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.FLUID_TANK)
                        .rightClickToOpenGui()
                        .shouldTick()));
    }
}
