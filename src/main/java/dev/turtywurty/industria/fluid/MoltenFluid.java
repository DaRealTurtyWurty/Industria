package dev.turtywurty.industria.fluid;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.function.Supplier;

public abstract class MoltenFluid extends IndustriaFluid {
    public MoltenFluid(Supplier<Fluid> stillSupplier, Supplier<Fluid> flowingSupplier, Supplier<Item> bucketSupplier, Supplier<Block> blockSupplier) {
        super(stillSupplier, flowingSupplier, bucketSupplier, blockSupplier);
    }

    public void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
        BlockPos blockPos = pos.up();
        if (world.getBlockState(blockPos).isAir() && !world.getBlockState(blockPos).isOpaqueFullCube()) {
            if (random.nextInt(100) == 0) {
                double x = pos.getX() + random.nextDouble();
                double y = pos.getY() + 1;
                double z = pos.getZ() + random.nextDouble();
                world.addParticleClient(new BlockStateParticleEffect(ParticleTypes.BLOCK, state.getBlockState()), x, y, z, 0.0, 0.0, 0.0);
                world.playSoundClient(x, y, z, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }

            if (random.nextInt(100) == 0) {
                world.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }
        }
    }

    public void onRandomTick(ServerWorld world, BlockPos pos, FluidState state, Random random) {
        if (world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
            int fireChance = random.nextInt(2);
            if (fireChance > 0) {
                BlockPos blockPos = pos;

                for (int j = 0; j < fireChance; ++j) {
                    blockPos = blockPos.add(random.nextInt(2) - 1, 1, random.nextInt(2) - 1);
                    if (!world.isPosLoaded(blockPos)) {
                        return;
                    }

                    BlockState blockState = world.getBlockState(blockPos);
                    if (blockState.isAir()) {
                        if (this.canLightFire(world, blockPos)) {
                            world.setBlockState(blockPos, AbstractFireBlock.getState(world, blockPos));
                            return;
                        }
                    } else if (blockState.blocksMovement()) {
                        return;
                    }
                }
            } else {
                for (int k = 0; k < 2; ++k) {
                    BlockPos blockPos2 = pos.add(random.nextInt(2) - 1, 0, random.nextInt(2) - 1);
                    if (!world.isPosLoaded(blockPos2)) {
                        return;
                    }

                    if (world.isAir(blockPos2.up()) && hasBurnableBlock(world, blockPos2)) {
                        world.setBlockState(blockPos2.up(), AbstractFireBlock.getState(world, blockPos2));
                    }
                }
            }

        }
    }

    private boolean canLightFire(WorldView world, BlockPos pos) {
        Direction[] directions = Direction.values();

        for (Direction direction : directions) {
            if (hasBurnableBlock(world, pos.offset(direction))) {
                return true;
            }
        }

        return false;
    }

    private boolean hasBurnableBlock(WorldView world, BlockPos pos) {
        return (!world.isInHeightLimit(pos.getY()) || world.isChunkLoaded(pos)) && world.getBlockState(pos).isBurnable();
    }

    public static class Still extends MoltenFluid {
        public Still(Supplier<Fluid> stillSupplier, Supplier<Fluid> flowingSupplier, Supplier<Item> bucketSupplier, Supplier<Block> blockSupplier) {
            super(stillSupplier, flowingSupplier, bucketSupplier, blockSupplier);
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }

        @Override
        public int getLevel(FluidState state) {
            return 8;
        }
    }

    public static class Flowing extends MoltenFluid {
        public Flowing(Supplier<Fluid> stillSupplier, Supplier<Fluid> flowingSupplier, Supplier<Item> bucketSupplier, Supplier<Block> blockSupplier) {
            super(stillSupplier, flowingSupplier, bucketSupplier, blockSupplier);
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }
    }
}
