package dev.turtywurty.industria.worldgen.feature;

import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.worldgen.config.FluidPocketConfig;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FluidPocketFeature extends Feature<FluidPocketConfig> {
    public FluidPocketFeature() {
        super(FluidPocketConfig.CODEC);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean generate(FeatureContext<FluidPocketConfig> context) {
        BlockPos origin = context.getOrigin();
        WorldAccess world = context.getWorld();
        FluidPocketConfig config = context.getConfig();
        FluidState fluidState = config.fluidState();
        Random random = context.getRandom();

        BlockState originState = world.getBlockState(origin);
        if(originState.isAir() || !config.replaceable().test(originState, random))
            return false;

        if(random.nextInt(100) < 50)
            return false;

        ServerWorld serverWorld;
        if(!(world instanceof ServerWorld)) {
            if (world instanceof ChunkRegion) {
                serverWorld = ((ChunkRegion) world).toServerWorld();
            } else {
                return false;
            }
        } else {
            serverWorld = (ServerWorld) world;
        }

        Set<BlockPos> positions = new HashSet<>();
        // Define the base radius and depth of the pond
        int baseRadius = config.radius().get(random);
        int baseDepth = config.depth().get(random);

        // Iterate over the area to create the pond with randomness
        for (int x = -baseRadius; x <= baseRadius; x++) {
            for (int z = -baseRadius; z <= baseRadius; z++) {
                // Introduce random variations in radius and depth
                int radiusVariation = random.nextInt(3) - 1;
                int depthVariation = random.nextInt(3) - 1;

                int currentRadius = baseRadius + radiusVariation;
                int currentDepth = baseDepth + depthVariation;

                for (int y = 0; y < currentDepth; y++) {
                    BlockPos pos = origin.add(x, -y, z);
                    BlockState state = world.getBlockState(pos);
                    if (pos.isWithinDistance(origin, currentRadius) && !state.isAir() && config.replaceable().test(state, random)) {
                        positions.add(pos);
                    }
                }
            }
        }

        var pocket = new WorldFluidPocketsState.FluidPocket(fluidState, new ArrayList<>(positions));
        WorldFluidPocketsState.getServerState(serverWorld).addFluidPocket(pocket);
        WorldFluidPocketsState.sync(serverWorld);

        System.out.println("Generated fluid pocket at " + origin);

        return true;
    }
}
