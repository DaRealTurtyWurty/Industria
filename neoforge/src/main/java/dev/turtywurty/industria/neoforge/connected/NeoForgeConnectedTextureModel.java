package dev.turtywurty.industria.neoforge.connected;

import dev.turtywurty.industria.connected.*;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NeoForgeConnectedTextureModel extends DelegateBlockStateModel {
    private final NeoForgeConnectedTextureQuadCache quadCache = new NeoForgeConnectedTextureQuadCache();
    private final Set<Block> additionalConnections;

    public NeoForgeConnectedTextureModel(BlockStateModel delegate, ConnectedTextureSettings settings) {
        super(delegate);
        this.additionalConnections = settings.resolveBlocks();
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        List<BlockStateModelPart> tempParts = new ArrayList<>();
        super.collectParts(level, pos, state, random, tempParts);

        var neighbourhood = new ConnectedTextureNeighborhood(level, pos, state, additionalConnections);
        long packedTiles = 0L;
        for (Direction direction : Direction.values()) {
            ConnectedTextureFace face = ConnectedTextureFace.fromFace(direction);
            int mask = face.getMask(neighbourhood);
            ConnectedTextureTile tile = ConnectedTextureLayout.getTile(mask);
            packedTiles |= (long) tile.index() << (direction.ordinal() * 6);
        }

        for (BlockStateModelPart part : tempParts) {
            parts.add(new NeoForgeConnectedTexturePart(part, packedTiles, quadCache));
        }
    }
}
