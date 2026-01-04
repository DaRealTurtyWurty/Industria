package dev.turtywurty.industria.worldgen.feature;

import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.worldgen.config.FluidPocketConfig;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.FluidState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FluidPocketFeature extends Feature<FluidPocketConfig> {
    public FluidPocketFeature() {
        super(FluidPocketConfig.CODEC);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean place(FeaturePlaceContext<FluidPocketConfig> context) {
        BlockPos origin = context.origin();
        LevelAccessor world = context.level();
        FluidPocketConfig config = context.config();
        FluidState fluidState = config.fluidState();
        RandomSource random = context.random();

        BlockState originState = world.getBlockState(origin);
        if(originState.isAir() || !config.replaceable().test(originState, random))
            return false;

        if(random.nextInt(100) < 50)
            return false;

        ServerLevel serverWorld;
        if(!(world instanceof ServerLevel)) {
            if (world instanceof WorldGenRegion) {
                serverWorld = ((WorldGenRegion) world).getLevel();
            } else {
                return false;
            }
        } else {
            serverWorld = (ServerLevel) world;
        }

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
                    if (pos.closerThan(origin, currentRadius) && !state.isAir() && config.replaceable().test(state, random)) {
                        positions.add(pos);
                    }
                }
            }
        }

        Map<BlockPos, Integer> positionsWithAmount = new HashMap<>();
        for (BlockPos pos : positions) {
            positionsWithAmount.put(pos, random.nextIntBetweenInclusive((int) FluidConstants.BOTTLE, (int) (FluidConstants.BUCKET * 5)));
        }

        var pocket = new WorldFluidPocketsState.FluidPocket(fluidState, positionsWithAmount);
        WorldFluidPocketsState.getServerState(serverWorld).addFluidPocket(pocket);
        WorldFluidPocketsState.sync(serverWorld);

        System.out.println("Generated fluid pocket at " + origin);
        return true;
    }
}
