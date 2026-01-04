package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.CrusherBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrusherBlock extends IndustriaBlock {
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");

    private static final VoxelShape VOXEL_SHAPE = Shapes.box(0, 0, 0, 1, 0.625, 1);
    private static final float[][] PARTICLE_OFFSETS = {
            {0.25f, 0.25f},
            {0.75f, 0.25f},
            {0.25f, 0.75f},
            {0.75f, 0.75f}
    };

    public CrusherBlock(Properties settings) {
        super(settings, new BlockProperties()
                .hasHorizontalFacing()
                .hasComparatorOutput()
                .constantShape(VOXEL_SHAPE)
                .hasBlockEntityRenderer()
                .addStateProperty(RUNNING, false)
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.CRUSHER)
                        .rightClickToOpenGui()
                        .dropContentsOnBreak()
                        .shouldTick()));
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if(world.getBlockEntity(pos) instanceof CrusherBlockEntity blockEntity) {
            if(blockEntity.getProgress() <= 0)
                return;

            ItemStack stack = blockEntity.getWrappedContainerStorage().getInventory(CrusherBlockEntity.INPUT_SLOT).getItem(0);
            if(stack.isEmpty())
                return;

            var particle = new ItemParticleOption(ParticleTypes.ITEM, stack);
            for (float[] offset : PARTICLE_OFFSETS) {
                for (int i = 0; i < random.nextInt(2) + 1; i++) {
                    world.addParticle(particle,
                            pos.getX() + offset[0] + Mth.nextFloat(random, -0.1f, 0.1f),
                            pos.getY() + 0.55 + Mth.nextFloat(random, -0.1f, 0.1f),
                            pos.getZ() + offset[1] + Mth.nextFloat(random, -0.1f, 0.1f),
                            0, 0, 0
                    );
                }
            }
        }
    }
}
