package dev.turtywurty.industria.connected;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public class FabricConnectedTextureModel extends WrapperBlockStateModel {
    private final FabricConnectedTextureQuadCache quadCache = new FabricConnectedTextureQuadCache();
    private final ConnectedTextureModelRules rules;
    private final SpriteFinder spriteFinder;

    public FabricConnectedTextureModel(BlockStateModel model, ConnectedTextureSettings settings, SpriteFinder spriteFinder) {
        super(model);
        this.rules = new ConnectedTextureModelRules(settings);
        this.spriteFinder = spriteFinder;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        if (rules.isEmpty()) {
            super.emitQuads(emitter, level, pos, state, random, cullTest);
            return;
        }

        emitter.pushTransform(new QuadTransform() {
            private @Nullable ConnectedTextureNeighborhood neighborhood;
            private @Nullable ConnectedTextureNeighborhood[] otherNeighborhoods;

            @Override
            public boolean transform(MutableQuadView quad) {
                Direction direction = quad.lightFace();
                int group = rules.connectionGroup(direction);
                if (group < 0)
                    return true;

                ConnectedTextureNeighborhood faceNeighborhood;
                if (group == 0) {
                    if (neighborhood == null) {
                        neighborhood = new ConnectedTextureNeighborhood(level, pos, state, rules.connections(0));
                    }

                    faceNeighborhood = neighborhood;
                } else {
                    if (otherNeighborhoods == null) {
                        otherNeighborhoods = new ConnectedTextureNeighborhood[rules.groupCount() - 1];
                    }

                    faceNeighborhood = otherNeighborhoods[group - 1];
                    if (faceNeighborhood == null) {
                        faceNeighborhood = new ConnectedTextureNeighborhood(level, pos, state, rules.connections(group));
                        otherNeighborhoods[group - 1] = faceNeighborhood;
                    }
                }

                ConnectedTextureFace face = ConnectedTextureFace.fromFace(direction);
                int mask = face.getMask(faceNeighborhood, rules.axis(direction));
                ConnectedTextureTile tile = ConnectedTextureLayout.getTile(mask);
                FabricConnectedTextureModel.this.quadCache.apply(quad, spriteFinder.find(quad),
                        quad.chunkLayer().translucent(), tile);
                return true;
            }
        });

        try {
            super.emitQuads(emitter, level, pos, state, random, cullTest);
        } finally {
            emitter.popTransform();
        }
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return null;
    }
}
