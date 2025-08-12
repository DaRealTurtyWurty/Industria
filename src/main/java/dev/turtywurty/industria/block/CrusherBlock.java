package dev.turtywurty.industria.block;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import dev.turtywurty.industria.blockentity.CrusherBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

public class CrusherBlock extends IndustriaBlock {
    public static final BooleanProperty RUNNING = BooleanProperty.of("running");

    private static final VoxelShape VOXEL_SHAPE = VoxelShapes.cuboid(0, 0, 0, 1, 0.625, 1);
    private static final float[][] PARTICLE_OFFSETS = {
            {0.25f, 0.25f},
            {0.75f, 0.25f},
            {0.25f, 0.75f},
            {0.75f, 0.75f}
    };

    public CrusherBlock(Settings settings) {
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
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.getBlockEntity(pos) instanceof CrusherBlockEntity blockEntity) {
            if (blockEntity.getProgress() <= 0)
                return;

            ItemStack stack = blockEntity.getWrappedInventoryStorage().getInventory(CrusherBlockEntity.INPUT_SLOT).getStack(0);
            var particle = new ItemStackParticleEffect(ParticleTypes.ITEM, stack);
            for (float[] offset : PARTICLE_OFFSETS) {
                for (int i = 0; i < random.nextInt(2) + 1; i++) {
                    world.addParticleClient(particle,
                            pos.getX() + offset[0] + MathHelper.nextFloat(random, -0.1f, 0.1f),
                            pos.getY() + 0.55 + MathHelper.nextFloat(random, -0.1f, 0.1f),
                            pos.getZ() + offset[1] + MathHelper.nextFloat(random, -0.1f, 0.1f),
                            0, 0, 0
                    );
                }
            }
        }
    }
}
