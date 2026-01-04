package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ArcFurnaceBlock extends IndustriaBlock {
    public static final VoxelShape VOXEL_SHAPE = Shapes.block();

    public ArcFurnaceBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasLitProperty()
                .hasComparatorOutput()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.ARC_FURNACE)
                        .multiblockProperties(MultiblockTypeInit.ARC_FURNACE).build()
                        .shouldTick()
                        .rightClickToOpenGui()
                        .dropContentsOnBreak()));
    }
}
