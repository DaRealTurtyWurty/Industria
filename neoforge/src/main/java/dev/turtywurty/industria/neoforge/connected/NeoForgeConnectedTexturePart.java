package dev.turtywurty.industria.neoforge.connected;

import dev.turtywurty.industria.connected.ConnectedTextureTile;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.TriState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record NeoForgeConnectedTexturePart(BlockStateModelPart delegate,
                                           Map<Direction, ConnectedTextureTile> tiles) implements BlockStateModelPart {
    public NeoForgeConnectedTexturePart(BlockStateModelPart delegate, Map<Direction, ConnectedTextureTile> tiles) {
        this.delegate = delegate;
        this.tiles = Map.copyOf(tiles);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        List<BakedQuad> quads = new ArrayList<>(delegate.getQuads(direction));
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
        Direction face = quad.direction();
        ConnectedTextureTile tile = tiles.get(face);
        if (tile == null)
            return quad;

        TextureAtlasSprite sprite = quad.materialInfo().sprite();
        long[] packedUVs = new long[4];
        for (int vertex = 0; vertex < 4; vertex++) {
            long uv = quad.packedUV(vertex);
            float u = UVPair.unpackU(uv);
            float v = UVPair.unpackV(uv);
            float localU = (u - sprite.getU0()) / (sprite.getU1() - sprite.getU0());
            float localV = (v - sprite.getV0()) / (sprite.getV1() - sprite.getV0());
            float tileU = tile.minU() + localU * (tile.maxU() - tile.minU());
            float tileV = tile.minV() + localV * (tile.maxV() - tile.minV());
            packedUVs[vertex] = UVPair.pack(sprite.getU(tileU), sprite.getV(tileV));
        }

        return new BakedQuad(
                quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                packedUVs[0], packedUVs[1], packedUVs[2], packedUVs[3],
                quad.direction(), quad.materialInfo(), quad.bakedNormals(), quad.bakedColors()
        );
    }
}
