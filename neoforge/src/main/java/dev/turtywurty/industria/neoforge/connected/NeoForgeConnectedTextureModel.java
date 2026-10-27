package dev.turtywurty.industria.neoforge.connected;

import dev.turtywurty.industria.connected.ConnectedTextureFace;
import dev.turtywurty.industria.connected.ConnectedTextureLayout;
import dev.turtywurty.industria.connected.ConnectedTextureTile;
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

    public NeoForgeConnectedTextureModel(BlockStateModel delegate) {
        super(delegate);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        List<BlockStateModelPart> tempParts = new ArrayList<>();
        super.collectParts(level, pos, state, random, tempParts);

        long packedTiles = 0L;
        for (Direction direction : Direction.values()) {
            ConnectedTextureFace face = ConnectedTextureFace.fromFace(direction);
            int mask = face.getMask(level, pos, state);
            ConnectedTextureTile tile = ConnectedTextureLayout.getTile(mask);
            packedTiles |= (long) tile.index() << (direction.ordinal() * 6);
        }

        for (BlockStateModelPart part : tempParts) {
            parts.add(new NeoForgeConnectedTexturePart(part, packedTiles, quadCache));
        }
    }
}
