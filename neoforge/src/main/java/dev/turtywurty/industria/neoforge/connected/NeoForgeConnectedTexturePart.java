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
    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        List<BakedQuad> source = delegate.getQuads(direction);
        if (source.isEmpty())
            return source;

        if (source.size() == 1) {
            BakedQuad quad = source.getFirst();
            return quadCache.getQuadList(quad, getTileForDirection(quad.direction()));
        }

        List<BakedQuad> quads = new ArrayList<>(source);
        quads.replaceAll(this::remapQuad);
        return quads;
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

    private BakedQuad remapQuad(BakedQuad quad) {
        return quadCache.getQuad(quad, getTileForDirection(quad.direction()));
    }

    private ConnectedTextureTile getTileForDirection(Direction direction) {
        int shift = direction.ordinal() * 6;
        int tileIndex = (int) ((packedTiles >>> shift) & 0x3FL);
        return ConnectedTextureLayout.getTileByIndex(tileIndex);
    }
}
