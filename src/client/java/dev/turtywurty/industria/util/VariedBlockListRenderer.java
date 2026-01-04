package dev.turtywurty.industria.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.turtywurty.industria.multiblock.VariedBlockList;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.util.List;

public class VariedBlockListRenderer {
    public static void renderInWorld(VariedBlockList variedBlockList, @Nullable BlockPos pos, Level world, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        Minecraft client = Minecraft.getInstance();
        Camera camera = client.gameRenderer.getMainCamera();
        float tickDelta = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        CameraRenderState cameraRenderState = buildCameraRenderState(camera, tickDelta);
        MultiBufferSource vertexConsumers = client.gameRenderer.getFeatureRenderDispatcher().bufferSource;
        renderInWorld(variedBlockList, pos, world, matrices, queue, light, overlay, camera, cameraRenderState, tickDelta, vertexConsumers);
    }

    public static void renderInWorld(
            VariedBlockList variedBlockList,
            @Nullable BlockPos pos,
            Level world,
            PoseStack matrices,
            SubmitNodeCollector queue,
            int light,
            int overlay,
            Camera camera,
            CameraRenderState cameraRenderState,
            float tickDelta,
            MultiBufferSource vertexConsumers
    ) {
        Minecraft client = Minecraft.getInstance();
        BlockRenderDispatcher blockRenderer = client.getBlockRenderer();
        BlockState state = getCurrentState(variedBlockList, world);
        if (state == null)
            return;

        if (!state.getFluidState().isEmpty() && pos != null) {
            BlockAndTintGetter fluidLevel = world;
            if (light == LightCoordsUtil.UI_FULL_BRIGHT) {
                fluidLevel = new UiLightmapBlockAndTintGetter(world);
            }

            RenderType fluidLayer = mapBlockLayer(ItemBlockRenderTypes.getRenderLayer(state.getFluidState()));
            var base = vertexConsumers.getBuffer(fluidLayer);
            var transformedConsumer = new TransformedVertexConsumer(base, matrices.last());
            blockRenderer.renderLiquid(BlockPos.ZERO, fluidLevel, transformedConsumer, world.getBlockState(pos), state.getFluidState());
        }

        boolean useWorldRender = pos != null && world == client.level;
        if (!useWorldRender) {
            blockRenderer.renderSingleBlock(state, matrices, vertexConsumers, light, overlay);
        } else {
            blockRenderer.renderBlockAsEntity(state, matrices, vertexConsumers, light, overlay, world, pos);
        }

        if (!state.hasBlockEntity() || pos == null)
            return;

        if (!(state.getBlock() instanceof EntityBlock provider))
            return;

        BlockEntity blockEntity = provider.newBlockEntity(pos, state);
        if (blockEntity == null)
            return;

        blockEntity.setLevel(world);
        BlockEntityRenderDispatcher blockEntityRenderManager = client.getBlockEntityRenderDispatcher();
        blockEntityRenderManager.prepare(camera);
        BlockEntityRenderState renderState = blockEntityRenderManager.tryExtractRenderState(blockEntity, tickDelta, null);
        if (renderState != null) {
            renderState.lightCoords = light;
            blockEntityRenderManager.submit(renderState, matrices, queue, cameraRenderState);
        }
    }

    @Nullable
    private static BlockState getCurrentState(VariedBlockList variedBlockList, Level world) {
        List<BlockState> states = variedBlockList.allStates(world.registryAccess().lookupOrThrow(Registries.BLOCK));
        if (states.isEmpty())
            return null;

        long gameTime = world.getGameTime();
        int index = (int) ((gameTime / 10) % states.size());
        return states.stream().skip(index).findFirst().orElse(null);
    }

    private static CameraRenderState buildCameraRenderState(Camera camera, float tickDelta) {
        var cameraRenderState = new CameraRenderState();
        cameraRenderState.initialized = camera.isInitialized();
        cameraRenderState.pos = camera.position();
        cameraRenderState.blockPos = camera.blockPosition();
        Vec3 entityPos = cameraRenderState.pos;
        if (camera.entity() != null) {
            entityPos = camera.entity().getPosition(tickDelta);
        }
        cameraRenderState.entityPos = entityPos;
        cameraRenderState.orientation = new Quaternionf(camera.rotation());
        return cameraRenderState;
    }

    private static RenderType mapBlockLayer(ChunkSectionLayer layer) {
        return switch (layer) {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT, TRIPWIRE -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
        };
    }

    private record UiLightmapBlockAndTintGetter(BlockAndTintGetter delegate) implements BlockAndTintGetter {
        @Override
        public float getShade(Direction direction, boolean shade) {
            return this.delegate.getShade(direction, shade);
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return this.delegate.getLightEngine();
        }

        @Override
        public int getBlockTint(BlockPos pos, ColorResolver color) {
            return this.delegate.getBlockTint(pos, color);
        }

        @Override
        public int getBrightness(LightLayer layer, BlockPos pos) {
            return 0;
        }

        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return this.delegate.getBlockEntity(pos);
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return this.delegate.getBlockState(pos);
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return this.delegate.getFluidState(pos);
        }

        @Override
        public int getHeight() {
            return this.delegate.getHeight();
        }

        @Override
        public int getMinY() {
            return this.delegate.getMinY();
        }
    }

    private static final class TransformedVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final Matrix4f modelViewMatrix;
        private final Matrix3f normalMatrix;

        private TransformedVertexConsumer(VertexConsumer delegate, PoseStack.Pose matrices) {
            this.delegate = delegate;
            this.modelViewMatrix = matrices.pose();
            this.normalMatrix = matrices.normal();
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            Vector4f vector4f = this.modelViewMatrix.transform(new Vector4f(x, y, z, 1.0F));
            return this.delegate.addVertex(vector4f.x(), vector4f.y(), vector4f.z());
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this.delegate.setColor(red, green, blue, alpha);
        }

        @Override
        public VertexConsumer setColor(int argb) {
            return this.delegate.setColor(argb);
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this.delegate.setLineWidth(width);
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this.delegate.setUv(u, v);
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this.delegate.setUv1(u, v);
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this.delegate.setUv2(u, v);
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            Vector3f vector3f = this.normalMatrix.transform(new Vector3f(x, y, z));
            return this.delegate.setNormal(vector3f.x(), vector3f.y(), vector3f.z());
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
            Vector4f vector4f = this.modelViewMatrix.transform(new Vector4f(x, y, z, 1.0F));
            Vector3f vector3f = this.normalMatrix.transform(new Vector3f(normalX, normalY, normalZ));
            this.delegate.addVertex(
                    vector4f.x(),
                    vector4f.y(),
                    vector4f.z(),
                    color,
                    u,
                    v,
                    overlay,
                    light,
                    vector3f.x(),
                    vector3f.y(),
                    vector3f.z()
            );
        }
    }
}
