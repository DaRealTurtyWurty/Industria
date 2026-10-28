package dev.turtywurty.industria.connected;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class FabricConnectedTextureQuadCache {
    private static final int TILE_COUNT = 12 * 4;
    private static final int MAX_SOURCE_LAYOUTS = 64;

    private volatile SourceLayout[] sourceLayouts = new SourceLayout[0];

    private static void remapQuad(MutableQuadView quad, Material.Baked material, ConnectedTextureTile tile) {
        TextureAtlasSprite sprite = material.sprite();
        for (int vertex = 0; vertex < 4; vertex++) {
            float localU = (quad.u(vertex) - sprite.getU0()) / (sprite.getU1() - sprite.getU0());
            float localV = (quad.v(vertex) - sprite.getV0()) / (sprite.getV1() - sprite.getV0());
            float tileU = tile.minU() + localU * (tile.maxU() - tile.minU());
            float tileV = tile.minV() + localV * (tile.maxV() - tile.minV());
            quad.uv(vertex, sprite.getU(tileU), sprite.getV(tileV));
        }

        quad.postMaterialBake(material);
    }

    public void apply(MutableQuadView quad, Material.Baked material, ConnectedTextureTile tile) {
        apply(quad, material.sprite(), material.forceTranslucent(), tile);
    }

    public void apply(MutableQuadView quad, TextureAtlasSprite sprite, boolean forceTranslucent, ConnectedTextureTile tile) {
        long uv0 = UVPair.pack(quad.u(0), quad.v(0));
        long uv1 = UVPair.pack(quad.u(1), quad.v(1));
        long uv2 = UVPair.pack(quad.u(2), quad.v(2));
        long uv3 = UVPair.pack(quad.u(3), quad.v(3));

        SourceLayout layout = getOrCreateLayout(sprite, forceTranslucent, uv0, uv1, uv2, uv3);
        if (layout == null) {
            remapQuad(quad, new Material.Baked(sprite, forceTranslucent), tile);
            return;
        }

        int index = tile.index();
        PreparedQuad prepared = layout.variants().get(index);
        if (prepared != null) {
            prepared.apply(quad);
            return;
        }

        remapQuad(quad, new Material.Baked(sprite, forceTranslucent), tile);
        layout.variants().compareAndSet(index, null, PreparedQuad.capture(quad));
    }

    private @Nullable SourceLayout findLayout(
            TextureAtlasSprite sprite,
            boolean forceTranslucent,
            long uv0, long uv1, long uv2, long uv3
    ) {
        for (SourceLayout layout : this.sourceLayouts) {
            if (layout.matches(sprite, forceTranslucent, uv0, uv1, uv2, uv3))
                return layout;
        }

        return null;
    }

    private @Nullable SourceLayout getOrCreateLayout(
            TextureAtlasSprite sprite,
            boolean forceTranslucent,
            long uv0, long uv1, long uv2, long uv3
    ) {
        SourceLayout layout = findLayout(sprite, forceTranslucent, uv0, uv1, uv2, uv3);
        if (layout != null)
            return layout;

        synchronized (this) {
            layout = findLayout(sprite, forceTranslucent, uv0, uv1, uv2, uv3);
            if (layout != null)
                return layout;

            if (this.sourceLayouts.length >= MAX_SOURCE_LAYOUTS)
                return null;

            var newLayout = new SourceLayout(
                    sprite,
                    forceTranslucent,
                    uv0, uv1, uv2, uv3,
                    new AtomicReferenceArray<>(TILE_COUNT)
            );

            SourceLayout[] updated = Arrays.copyOf(this.sourceLayouts, this.sourceLayouts.length + 1);
            updated[updated.length - 1] = newLayout;
            this.sourceLayouts = updated;
            return newLayout;
        }
    }

    private record PreparedQuad(
            long uv0,
            long uv1,
            long uv2,
            long uv3,
            QuadAtlas atlas,
            boolean animated,
            ChunkSectionLayer chunkLayer,
            RenderType itemRenderType,
            RenderType itemGlintRenderType,
            RenderType itemGlintSpecialRenderType
    ) {
        public void apply(MutableQuadView quad) {
            quad.uv(0, UVPair.unpackU(uv0), UVPair.unpackV(uv0));
            quad.uv(1, UVPair.unpackU(uv1), UVPair.unpackV(uv1));
            quad.uv(2, UVPair.unpackU(uv2), UVPair.unpackV(uv2));
            quad.uv(3, UVPair.unpackU(uv3), UVPair.unpackV(uv3));
            quad.atlas(atlas);
            quad.animated(animated);
            quad.chunkLayer(chunkLayer);
            quad.itemRenderType(itemRenderType);
            quad.itemGlintRenderType(itemGlintRenderType);
            quad.itemGlintSpecialRenderType(itemGlintSpecialRenderType);
        }

        public static PreparedQuad capture(QuadView quad) {
            return new PreparedQuad(
                    UVPair.pack(quad.u(0), quad.v(0)),
                    UVPair.pack(quad.u(1), quad.v(1)),
                    UVPair.pack(quad.u(2), quad.v(2)),
                    UVPair.pack(quad.u(3), quad.v(3)),
                    quad.atlas(),
                    quad.animated(),
                    quad.chunkLayer(),
                    quad.itemRenderType(),
                    quad.itemGlintRenderType(),
                    quad.itemGlintSpecialRenderType()
            );
        }
    }

    private record SourceLayout(
            TextureAtlasSprite sprite,
            boolean forceTranslucent,
            long uv0,
            long uv1,
            long uv2,
            long uv3,
            AtomicReferenceArray<PreparedQuad> variants
    ) {
        public boolean matches(TextureAtlasSprite sprite, boolean forceTranslucent, long uv0, long uv1, long uv2, long uv3) {
            return this.sprite == sprite &&
                    this.forceTranslucent == forceTranslucent &&
                    this.uv0 == uv0 &&
                    this.uv1 == uv1 &&
                    this.uv2 == uv2 &&
                    this.uv3 == uv3;
        }
    }
}
