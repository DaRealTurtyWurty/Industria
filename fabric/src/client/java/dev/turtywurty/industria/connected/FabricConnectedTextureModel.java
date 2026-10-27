package dev.turtywurty.industria.connected;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public class FabricConnectedTextureModel extends WrapperBlockStateModel {
    private final FabricConnectedTextureQuadCache quadCache = new FabricConnectedTextureQuadCache();

    public FabricConnectedTextureModel(BlockStateModel model) {
        super(model);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        Material.Baked material = wrapped.particleMaterial(level, pos, state);
        emitter.pushTransform(new QuadTransform() {
            private @Nullable ConnectedTextureNeighborhood neighborhood;

            @Override
            public boolean transform(MutableQuadView quad) {
                if (neighborhood == null) {
                    neighborhood = new ConnectedTextureNeighborhood(level, pos, state);
                }

                Direction direction = quad.lightFace();
                ConnectedTextureFace face = ConnectedTextureFace.fromFace(direction);
                int mask = face.getMask(neighborhood);
                ConnectedTextureTile tile = ConnectedTextureLayout.getTile(mask);
                FabricConnectedTextureModel.this.quadCache.apply(quad, material, tile);
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
