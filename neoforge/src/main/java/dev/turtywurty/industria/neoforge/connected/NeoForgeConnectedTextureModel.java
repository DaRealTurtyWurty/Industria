package dev.turtywurty.industria.neoforge.connected;

import dev.turtywurty.industria.connected.*;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

import java.util.ArrayList;
import java.util.List;

public class NeoForgeConnectedTextureModel extends DelegateBlockStateModel {
    private final NeoForgeConnectedTextureQuadCache quadCache = new NeoForgeConnectedTextureQuadCache();
    private final ConnectedTextureModelRules rules;

    public NeoForgeConnectedTextureModel(BlockStateModel delegate, ConnectedTextureSettings settings) {
        super(delegate);
        this.rules = new ConnectedTextureModelRules(settings);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        if (rules.isEmpty()) {
            super.collectParts(level, pos, state, random, parts);
            return;
        }

        List<BlockStateModelPart> tempParts = new ArrayList<>();
        super.collectParts(level, pos, state, random, tempParts);

        var neighbourhood = new ConnectedTextureNeighborhood(level, pos, state, rules.connections(0));
        ConnectedTextureNeighborhood[] otherNeighborhoods = rules.groupCount() > 1
                ? new ConnectedTextureNeighborhood[rules.groupCount() - 1] : null;
        long packedTiles = 0L;
        for (Direction direction : Direction.values()) {
            int group = rules.connectionGroup(direction);
            if (group < 0) {
                packedTiles |= (long) NeoForgeConnectedTexturePart.DISABLED_TILE_INDEX << (direction.ordinal() * 6);
                continue;
            }

            ConnectedTextureNeighborhood faceNeighborhood = neighbourhood;
            if (group > 0) {
                faceNeighborhood = otherNeighborhoods[group - 1];
                if (faceNeighborhood == null) {
                    faceNeighborhood = new ConnectedTextureNeighborhood(level, pos, state, rules.connections(group));
                    otherNeighborhoods[group - 1] = faceNeighborhood;
                }
            }
            ConnectedTextureFace face = ConnectedTextureFace.fromFace(direction);
            int mask = face.getMask(faceNeighborhood, rules.axis(direction));
            ConnectedTextureTile tile = ConnectedTextureLayout.getTile(mask);
            packedTiles |= (long) tile.index() << (direction.ordinal() * 6);
        }

        for (BlockStateModelPart part : tempParts) {
            parts.add(new NeoForgeConnectedTexturePart(part, packedTiles, quadCache));
        }
    }
}
