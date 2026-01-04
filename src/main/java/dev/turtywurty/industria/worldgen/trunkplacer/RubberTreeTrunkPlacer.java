package dev.turtywurty.industria.worldgen.trunkplacer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.block.LatexBlock;
import dev.turtywurty.industria.init.worldgen.TrunkPlacerTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class RubberTreeTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<RubberTreeTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance ->
            trunkPlacerParts(instance).and(
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
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerTypeInit.RUBBER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader world, BiConsumer<BlockPos, BlockState> replacer, RandomSource random, int freeTreeHeight, BlockPos startPos, TreeConfiguration config) {
        List<FoliagePlacer.FoliageAttachment> nodes = new ArrayList<>();
        setDirtAt(world, replacer, random, startPos.below(), config);
        nodes.add(new FoliagePlacer.FoliageAttachment(startPos.above(freeTreeHeight), 0, false));

        int height = this.branchStartHeight.sample(random);
        float branchingPossibility = 0.8F;
        Direction branchDirection = null;
        for (int yPos = 0; yPos < freeTreeHeight; ++yPos) {
            placeLog(world, replacer, random, startPos.above(yPos), config);

            if(yPos >= height - 1) {
                if(random.nextFloat() < branchingPossibility) {
                    branchingPossibility *= branchingPossibility;

                    Direction direction;
                    do {
                        direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                    } while (direction == branchDirection);

                    branchDirection = direction;

                    BlockPos pos = startPos.above(yPos).offset(direction.getStepX(), 0, direction.getStepZ()).mutable();

                    BlockPos lastPos = pos;
                    int offsetX = 0;
                    int offsetZ = 0;
                    int length = this.branchLength.sample(random);
                    for (int hPos = 0; hPos < length; hPos++) {
                        BlockPos offsetPos = pos.offset(offsetX, hPos, offsetZ);
                        placeLog(world, replacer, random, offsetPos, config);

                        if(hPos == 0 || random.nextFloat() < 0.8F) {
                            offsetX += branchDirection.getStepX();
                            offsetZ += branchDirection.getStepZ();
                        }

                        lastPos = offsetPos;
                    }

                    nodes.add(new FoliagePlacer.FoliageAttachment(lastPos.above(), 0, false));
                }
            }
        }

        return nodes;
    }

    @Override
    protected boolean placeLog(LevelSimulatedReader world, BiConsumer<BlockPos, BlockState> replacer, RandomSource random, BlockPos pos, TreeConfiguration config) {
        return placeLog(world, replacer, random, pos, config,
                state -> state.setValue(LatexBlock.LATEX_LEVEL, random.nextIntBetweenInclusive(1, 5)));
    }
}
