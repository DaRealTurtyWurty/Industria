package dev.turtywurty.industria.neoforge.connected;

import com.mojang.blaze3d.platform.Transparency;
import dev.turtywurty.industria.connected.ConnectedTextureTile;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class NeoForgeConnectedTextureQuadCache {
    private static final int TILE_COUNT = 12 * 4;
    private static final int MAX_SOURCE_QUADS = 64;

    private final ConcurrentMap<BakedQuad, AtomicReferenceArray<List<BakedQuad>>> variants = new ConcurrentHashMap<>();

    public List<BakedQuad> getQuadList(BakedQuad quad, @Nullable ConnectedTextureTile tile) {
        if (tile == null)
            return List.of(quad);

        AtomicReferenceArray<List<BakedQuad>> quads = getOrCreateVariants(quad);
        if (quads == null)
            return List.of(remapQuad(quad, tile));

        int index = tile.index();
        List<BakedQuad> variant = quads.get(index);
        if (variant != null)
            return variant;

        List<BakedQuad> prepared = List.of(remapQuad(quad, tile));
        if (quads.compareAndSet(index, null, prepared))
            return prepared;

        return quads.get(index);
    }

    public BakedQuad getQuad(BakedQuad quad, @Nullable ConnectedTextureTile tile) {
        return getQuadList(quad, tile).getFirst();
    }

    private @Nullable AtomicReferenceArray<List<BakedQuad>> getOrCreateVariants(BakedQuad quad) {
        AtomicReferenceArray<List<BakedQuad>> quads = this.variants.get(quad);
        if (quads != null)
            return quads;

        synchronized (this.variants) {
            quads = this.variants.get(quad);
            if (quads != null)
                return quads;

            if (this.variants.size() >= MAX_SOURCE_QUADS)
                return null;

            quads = new AtomicReferenceArray<>(TILE_COUNT);
            this.variants.put(quad, quads);
            return quads;
        }
    }

    private static BakedQuad remapQuad(BakedQuad quad, ConnectedTextureTile tile) {
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
                quad.direction(),
                remapMaterialInfo(quad.materialInfo(), packedUVs),
                quad.bakedNormals(),
                quad.bakedColors()
        );
    }

    private static Transparency computeTransparency(TextureAtlasSprite sprite, long[] packedUVs) {
        float minU = Float.POSITIVE_INFINITY;
        float minV = Float.POSITIVE_INFINITY;
        float maxU = Float.NEGATIVE_INFINITY;
        float maxV = Float.NEGATIVE_INFINITY;
        for (long uv : packedUVs) {
            float u = UVPair.unpackU(uv);
            float v = UVPair.unpackV(uv);
            minU = Math.min(minU, u);
            minV = Math.min(minV, v);
            maxU = Math.max(maxU, u);
            maxV = Math.max(maxV, v);
        }

        float inverseWidth = 1.0f / (sprite.getU1() - sprite.getU0());
        float inverseHeight = 1.0f / (sprite.getV1() - sprite.getV0());

        minU = (minU - sprite.getU0()) * inverseWidth;
        maxU = (maxU - sprite.getU0()) * inverseWidth;
        minV = (minV - sprite.getV0()) * inverseHeight;
        maxV = (maxV - sprite.getV0()) * inverseHeight;

        return sprite.contents().computeTransparency(minU, minV, maxU, maxV);
    }


    private static BakedQuad.MaterialInfo remapMaterialInfo(BakedQuad.MaterialInfo original, long[] packedUVs) {
        if (original.layer().translucent())
            return original;

        Transparency transparency = computeTransparency(original.sprite(), packedUVs);
        return BakedQuad.MaterialInfo.of(
                new Material.Baked(original.sprite(), false),
                transparency,
                original.tintIndex(),
                original.shadeDirectionOverride(),
                original.lightEmission(),
                original.ambientOcclusion()
        );
    }
}
