package dev.turtywurty.industria.util;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TintedBakedModel extends ForwardingBakedModel {
    public static final Map<Integer, Integer> TINT_INDEX_MAP = Map.of();

    private final int tint;

    public TintedBakedModel(BakedModel delegate, int tint) {
        this.wrapped = delegate;
        this.tint = tint;

        if(!TINT_INDEX_MAP.containsKey(tint))
            throw new IllegalArgumentException("Invalid tint index: " + tint);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState blockState, Direction face, Random rand) {
        List<BakedQuad> quads = super.getQuads(blockState, face, rand);
        List<BakedQuad> newQuads = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            newQuads.add(new TintedBakedQuad(quad, this.tint));
        }

        return newQuads;
    }

    public static class TintedBakedQuad extends BakedQuad {
        private final int tint;

        public TintedBakedQuad(BakedQuad delegate, int tint) {
            super(delegate.getVertexData(), delegate.getColorIndex(),
                    delegate.getFace(), delegate.getSprite(),
                    delegate.hasShade(), delegate.getLightEmission());
            this.tint = tint;
        }

        @Override
        public int getColorIndex() {
            return tint;
        }

        @Override
        public boolean hasColor() {
            return true;
        }
    }
}
