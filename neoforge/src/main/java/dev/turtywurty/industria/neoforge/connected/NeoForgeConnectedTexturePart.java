package dev.turtywurty.industria.neoforge.connected;

import dev.turtywurty.industria.connected.ConnectedTextureLayout;
import dev.turtywurty.industria.connected.ConnectedTextureTile;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.TriState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record NeoForgeConnectedTexturePart(
        BlockStateModelPart delegate,
        long packedTiles,
        NeoForgeConnectedTextureQuadCache quadCache
) implements BlockStateModelPart {
    static final int DISABLED_TILE_INDEX = 0x3F;

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        List<BakedQuad> source = delegate.getQuads(direction);
        if (source.isEmpty())
            return source;

        if (source.size() == 1) {
            BakedQuad quad = source.getFirst();
            ConnectedTextureTile tile = getTileForDirection(quad.direction());
            return tile == null ? source : quadCache.getQuadList(quad, tile);
        }

        List<BakedQuad> quads = null;
        for (int index = 0; index < source.size(); index++) {
            BakedQuad quad = source.get(index);
            ConnectedTextureTile tile = getTileForDirection(quad.direction());
            if (tile != null) {
                if (quads == null)
                    quads = new ArrayList<>(source);
                quads.set(index, quadCache.getQuad(quad, tile));
            }
        }
        return quads == null ? source : quads;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean useAmbientOcclusion() {
        return delegate.useAmbientOcclusion();
    }

    @Override
    public TriState ambientOcclusion() {
        return delegate.ambientOcclusion();
    }

    @Override
    public Material.Baked particleMaterial() {
        return delegate.particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return delegate.materialFlags();
    }

    private @Nullable ConnectedTextureTile getTileForDirection(Direction direction) {
        int shift = direction.ordinal() * 6;
        int tileIndex = (int) ((packedTiles >>> shift) & 0x3FL);
        return tileIndex == DISABLED_TILE_INDEX ? null : ConnectedTextureLayout.getTileByIndex(tileIndex);
    }
}
