package dev.turtywurty.industria.connected;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public class FabricConnectedTextureModel extends WrapperBlockStateModel {
    public FabricConnectedTextureModel(BlockStateModel model) {
        super(model);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        Material.Baked material = wrapped.particleMaterial(level, pos, state);
        TextureAtlasSprite sprite = material.sprite();
        emitter.pushTransform(quad -> {
            Direction direction = quad.lightFace();
            ConnectedTextureFace face = ConnectedTextureFace.fromFace(direction);
            int mask = face.getMask(level, pos, state);
            ConnectedTextureTile tile = ConnectedTextureLayout.getTile(mask);
            for (int vertex = 0; vertex < 4; vertex++) {
                float localU = (quad.u(vertex) - sprite.getU0()) / (sprite.getU1() - sprite.getU0());
                float localV = (quad.v(vertex) - sprite.getV0()) / (sprite.getV1() - sprite.getV0());
                float tileU = tile.minU() + localU * (tile.maxU() - tile.minU());
                float tileV = tile.minV() + localV * (tile.maxV() - tile.minV());
                quad.uv(vertex, sprite.getU(tileU), sprite.getV(tileV));
            }

            quad.postMaterialBake(material);
            return true;
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
