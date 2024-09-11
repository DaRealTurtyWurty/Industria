package dev.turtywurty.industria.worldgen.feature;

import dev.turtywurty.industria.init.AttachmentTypeInit;
import dev.turtywurty.industria.worldgen.config.FluidPocketConfig;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class FluidPocketFeature extends Feature<FluidPocketConfig> {
    public FluidPocketFeature() {
        super(FluidPocketConfig.CODEC);
    }

    @Override
    public boolean generate(FeatureContext<FluidPocketConfig> context) {
        BlockPos origin = context.getOrigin();
        System.out.println("Generating fluid pocket at " + origin);
        WorldAccess world = context.getWorld();
        FluidPocketConfig config = context.getConfig();
        FluidState fluidState = config.fluidState();
        Random random = context.getRandom();

        if(random.nextInt(100) < 50)
            return false;

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
                        Chunk chunk = world.getChunk(pos);
                        Map<String, FluidState> fluidMap = chunk.getAttachedOrSet(AttachmentTypeInit.FLUID_MAP_ATTACHMENT, new HashMap<>());
                        if(fluidMap.containsKey(pos.toShortString()) && !fluidMap.get(pos.toShortString()).isEmpty())
                            continue;

                        Map<String, FluidState> copy = new HashMap<>(fluidMap);
                        copy.put(pos.toShortString(), fluidState);
                        chunk.setAttached(AttachmentTypeInit.FLUID_MAP_ATTACHMENT, copy);
                    }
                }
            }
        }

        return true;
    }
}
