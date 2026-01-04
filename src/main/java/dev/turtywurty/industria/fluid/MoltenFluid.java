package dev.turtywurty.industria.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.function.Supplier;

public abstract class MoltenFluid extends IndustriaFluid {
    public void animateTick(Level world, BlockPos pos, FluidState state, RandomSource random) {
        BlockPos blockPos = pos.above();
        if (world.getBlockState(blockPos).isAir() && !world.getBlockState(blockPos).isSolidRender()) {
            if (random.nextInt(100) == 0) {
                double x = pos.getX() + random.nextDouble();
                double y = pos.getY() + 1;
                double z = pos.getZ() + random.nextDouble();
                world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state.createLegacyBlock()), x, y, z, 0.0, 0.0, 0.0);
                world.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }

            if (random.nextInt(100) == 0) {
                world.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
            }
        }
    }

    public void randomTick(ServerLevel world, BlockPos pos, FluidState state, RandomSource random) {
        if (world.getGameRules().get(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER) != -1) {
            int fireChance = random.nextInt(2);
            if (fireChance > 0) {
                BlockPos blockPos = pos;

                for (int j = 0; j < fireChance; ++j) {
                    blockPos = blockPos.offset(random.nextInt(2) - 1, 1, random.nextInt(2) - 1);
                    if (!world.isLoaded(blockPos)) {
                        return;
                    }

                    BlockState blockState = world.getBlockState(blockPos);
                    if (blockState.isAir()) {
                        if (this.canLightFire(world, blockPos)) {
                            world.setBlockAndUpdate(blockPos, BaseFireBlock.getState(world, blockPos));
                            return;
                        }
                    } else if (blockState.blocksMotion()) {
                        return;
                    }
                }
            } else {
                for (int k = 0; k < 2; ++k) {
                    BlockPos blockPos2 = pos.offset(random.nextInt(2) - 1, 0, random.nextInt(2) - 1);
                    if (!world.isLoaded(blockPos2)) {
                        return;
                    }

                    if (world.isEmptyBlock(blockPos2.above()) && hasBurnableBlock(world, blockPos2)) {
                        world.setBlockAndUpdate(blockPos2.above(), BaseFireBlock.getState(world, blockPos2));
                    }
                }
            }

        }
    }

    private boolean canLightFire(LevelReader world, BlockPos pos) {
        Direction[] directions = Direction.values();

        for (Direction direction : directions) {
            if (hasBurnableBlock(world, pos.relative(direction))) {
                return true;
            }
        }

        return false;
    }

    private boolean hasBurnableBlock(LevelReader world, BlockPos pos) {
        return (!world.isInsideBuildHeight(pos.getY()) || world.hasChunkAt(pos)) && world.getBlockState(pos).ignitedByLava();
    }

    public MoltenFluid(Supplier<Fluid> stillSupplier, Supplier<Fluid> flowingSupplier, Supplier<Item> bucketSupplier, Supplier<Block> blockSupplier) {
        super(stillSupplier, flowingSupplier, bucketSupplier, blockSupplier);
    }

    public static class Still extends MoltenFluid {
        public Still(Supplier<Fluid> stillSupplier, Supplier<Fluid> flowingSupplier, Supplier<Item> bucketSupplier, Supplier<Block> blockSupplier) {
            super(stillSupplier, flowingSupplier, bucketSupplier, blockSupplier);
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }
    }

    public static class Flowing extends MoltenFluid {
        public Flowing(Supplier<Fluid> stillSupplier, Supplier<Fluid> flowingSupplier, Supplier<Item> bucketSupplier, Supplier<Block> blockSupplier) {
            super(stillSupplier, flowingSupplier, bucketSupplier, blockSupplier);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }
    }
}
