package dev.turtywurty.industria.worldgen.trunkplacer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.block.LatexBlock;
import dev.turtywurty.industria.init.worldgen.TrunkPlacerTypeInit;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class RubberTreeTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<RubberTreeTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance ->
            fillTrunkPlacerFields(instance).and(
                    instance.group(IntProvider.POSITIVE_CODEC.fieldOf("branch_start_height").forGetter(placer -> placer.branchStartHeight),
                            IntProvider.NON_NEGATIVE_CODEC.fieldOf("branch_length").forGetter(placer -> placer.branchLength)))
                    .apply(instance, RubberTreeTrunkPlacer::new));
    public final IntProvider branchStartHeight;
    public final IntProvider branchLength;

    public RubberTreeTrunkPlacer(int baseHeight, int firstRandomHeight, int secondRandomHeight, IntProvider branchStartHeight, IntProvider branchLength) {
        super(baseHeight, firstRandomHeight, secondRandomHeight);
        this.branchStartHeight = branchStartHeight;
        this.branchLength = branchLength;
    }

    @Override
    protected TrunkPlacerType<?> getType() {
        return TrunkPlacerTypeInit.RUBBER;
    }

    @Override
    public List<FoliagePlacer.TreeNode> generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int freeTreeHeight, BlockPos startPos, TreeFeatureConfig config) {
        List<FoliagePlacer.TreeNode> nodes = new ArrayList<>();
        setToDirt(world, replacer, random, startPos.down(), config);
        nodes.add(new FoliagePlacer.TreeNode(startPos.up(freeTreeHeight), 0, false));

        int height = this.branchStartHeight.get(random);
        float branchingPossibility = 0.8F;
        Direction branchDirection = null;
        for (int yPos = 0; yPos < freeTreeHeight; ++yPos) {
            getAndSetState(world, replacer, random, startPos.up(yPos), config);

            if(yPos >= height - 1) {
                if(random.nextFloat() < branchingPossibility) {
                    branchingPossibility *= branchingPossibility;

                    Direction direction;
                    do {
                        direction = Direction.Type.HORIZONTAL.random(random);
                    } while (direction == branchDirection);

                    branchDirection = direction;

                    BlockPos pos = startPos.up(yPos).add(direction.getOffsetX(), 0, direction.getOffsetZ()).mutableCopy();

                    BlockPos lastPos = pos;
                    int offsetX = 0;
                    int offsetZ = 0;
                    int length = this.branchLength.get(random);
                    for (int hPos = 0; hPos < length; hPos++) {
                        BlockPos offsetPos = pos.add(offsetX, hPos, offsetZ);
                        getAndSetState(world, replacer, random, offsetPos, config);

                        if(hPos == 0 || random.nextFloat() < 0.8F) {
                            offsetX += branchDirection.getOffsetX();
                            offsetZ += branchDirection.getOffsetZ();
                        }

                        lastPos = offsetPos;
                    }

                    nodes.add(new FoliagePlacer.TreeNode(lastPos.up(), 0, false));
                }
            }
        }

        return nodes;
    }

    @Override
    protected boolean getAndSetState(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, BlockPos pos, TreeFeatureConfig config) {
        return getAndSetState(world, replacer, random, pos, config,
                state -> state.with(LatexBlock.LATEX_LEVEL, random.nextBetween(1, 5)));
    }
}
