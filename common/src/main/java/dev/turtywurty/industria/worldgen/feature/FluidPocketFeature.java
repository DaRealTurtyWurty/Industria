package dev.turtywurty.industria.worldgen.feature;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.worldgen.config.FluidPocketConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.FluidState;

public class FluidPocketFeature implements Feature {
    public static final MapCodec<FluidPocketFeature> CODEC = FluidPocketConfig.CODEC.fieldOf("config")
        .xmap(FluidPocketFeature::new, feature -> feature.config);
    private final FluidPocketConfig config;

    public FluidPocketFeature(FluidPocketConfig config) {
        this.config = config;
    }

    @Override
    public MapCodec<FluidPocketFeature> codec() {
        return CODEC;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean place(WorldGenLevel world, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        FluidState fluidState = config.fluidState();

        BlockState originState = world.getBlockState(origin);
        if (originState.isAir() || !config.replaceable().test(originState, origin, random))
            return false;

        if (random.nextInt(100) < 50)
            return false;

        ServerLevel serverWorld = world.getLevel();

        Set<BlockPos> positions = new HashSet<>();
        // Define the base radius and depth of the pond
        int baseRadius = config.radius().sample(random);
        int baseDepth = config.depth().sample(random);

        // Iterate over the area to create the pond with randomness
        for (int x = -baseRadius; x <= baseRadius; x++) {
            for (int z = -baseRadius; z <= baseRadius; z++) {
                // Introduce random variations in radius and depth
                int radiusVariation = random.nextInt(3) - 1;
                int depthVariation = random.nextInt(3) - 1;

                int currentRadius = baseRadius + radiusVariation;
                int currentDepth = baseDepth + depthVariation;

                for (int y = 0; y < currentDepth; y++) {
                    BlockPos pos = origin.offset(x, -y, z);
                    BlockState state = world.getBlockState(pos);
                    if (pos.closerThan(origin, currentRadius) && !state.isAir()
                        && config.replaceable().test(state, pos, random)) {
                        positions.add(pos);
                    }
                }
            }
        }

        Map<BlockPos, Integer> positionsWithAmount = new HashMap<>();
        for (BlockPos pos : positions) {
            positionsWithAmount.put(pos,
                random.nextIntBetweenInclusive((int) FluidAmounts.BOTTLE, (int) (FluidAmounts.BUCKET * 5)));
        }

        var pocket = new WorldFluidPocketsState.FluidPocket(fluidState, positionsWithAmount);
        WorldFluidPocketsState.getServerState(serverWorld).addFluidPocket(pocket);
        WorldFluidPocketsState.sync(serverWorld);

        System.out.println("Generated fluid pocket at " + origin);
        return true;
    }
}
