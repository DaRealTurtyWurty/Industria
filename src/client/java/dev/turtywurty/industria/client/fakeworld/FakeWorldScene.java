package dev.turtywurty.industria.client.fakeworld;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.*;

import java.lang.Math;
import java.util.List;
import java.util.function.Consumer;

/**
 * Shared fake-world scene implementation used by builder-produced scenes.
 */
public class FakeWorldScene {
    protected final MinecraftClient client = MinecraftClient.getInstance();
    protected final ClientWorld world;
    protected final Entity cameraEntity;
    protected final BlockRenderManager blockRenderManager;
    protected final List<PlacedBlock> blocks;
    protected final List<PlacedFluid> fluids;
    protected final List<Entity> entities;
    protected final Camera camera = new Camera();
    protected final ProjectionMatrix2 projectionMatrix = new ProjectionMatrix2("FakeWorldScene", -1000.0F, 1000.0F, true);
    protected SimpleFramebuffer framebuffer;
    private BlockPos anchorBlock;
    private int anchorTargetX;
    private int anchorTargetY;
    private final Consumer<FakeWorldSceneBuilder.SceneTickContext> tickHandler;

    public FakeWorldScene(FakeWorldSceneBuilder.BuiltScene builtScene) {
        this(builtScene, builtScene.tickHandler());
    }

    public FakeWorldScene(FakeWorldSceneBuilder.BuiltScene builtScene, Consumer<FakeWorldSceneBuilder.SceneTickContext> tickHandler) {
        this.world = builtScene.result().world();
        this.cameraEntity = builtScene.result().player();
        this.blockRenderManager = this.client.getBlockRenderManager();
        this.blocks = builtScene.blocks();
        this.fluids = builtScene.fluids();
        this.entities = builtScene.entities();
        this.tickHandler = tickHandler;

        this.cameraEntity.setPos(builtScene.cameraPos().x, builtScene.cameraPos().y, builtScene.cameraPos().z);
        this.cameraEntity.setYaw(builtScene.cameraYaw());
        this.cameraEntity.setPitch(builtScene.cameraPitch());

        // Register custom entities into the fake world.
        for (Entity entity : this.entities) {
            this.world.addEntity(entity);
        }
    }

    public void tick() {
        this.world.tick(() -> true);
        this.world.tickEntities();
        this.tickHandler.accept(new FakeWorldSceneBuilder.SceneTickContext(this.world, this.entities));
    }

    public void close() {
        if (this.framebuffer != null) {
            this.framebuffer.delete();
            this.framebuffer = null;
        }
    }

    public void render(DrawContext context, int x, int y, int width, int height, float tickDelta) {
        this.camera.update(this.world, this.cameraEntity, false, false, tickDelta);
        Vec3d cameraPos = this.camera.getPos();
        Quaternionf rotation = this.camera.getRotation().conjugate(new Quaternionf());

        var window = this.client.getWindow();
        double scale = window.getScaleFactor();
        int framebufferWidth = Math.max(1, (int) Math.round(width * scale));
        int framebufferHeight = Math.max(1, (int) Math.round(height * scale));
        ensureFramebuffer(framebufferWidth, framebufferHeight);

        this.client.getEntityRenderDispatcher().configure(this.world, this.camera, this.cameraEntity);
        this.client.getEntityRenderDispatcher().setRenderShadows(false);

        float anchorOffsetX = 0.0F;
        float anchorOffsetY = 0.0F;
        if (this.anchorBlock != null) {
            MatrixStack anchorMatrices = new MatrixStack();
            float scaleFactor = Math.min(framebufferWidth, framebufferHeight) / 6.0F;
            anchorMatrices.translate(framebufferWidth / 2.0F, framebufferHeight * 0.8F, 0);
            anchorMatrices.scale(scaleFactor, -scaleFactor, scaleFactor);
            anchorMatrices.multiply(rotation);
            anchorMatrices.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

            Matrix4f projection = new Matrix4f().setOrtho(0.0F, framebufferWidth, framebufferHeight, 0.0F, -1000.0F, 1000.0F);
            Vec2f projected = projectToScreen(Vec3d.ofCenter(this.anchorBlock), anchorMatrices.peek().getPositionMatrix(), projection, framebufferWidth, framebufferHeight);
            float targetX = (float) (this.anchorTargetX * scale);
            float targetY = (float) (this.anchorTargetY * scale);
            anchorOffsetX = targetX - projected.x;
            anchorOffsetY = targetY - projected.y;
        }

        RenderSystem.backupProjectionMatrix();
        RenderSystem.outputColorTextureOverride = this.framebuffer.getColorAttachmentView();
        RenderSystem.outputDepthTextureOverride = this.framebuffer.getDepthAttachmentView();
        RenderSystem.getDevice()
                .createCommandEncoder()
                .clearColorAndDepthTextures(this.framebuffer.getColorAttachment(), 0, this.framebuffer.getDepthAttachment(), 1.0);
        RenderSystem.setProjectionMatrix(this.projectionMatrix.set(framebufferWidth, framebufferHeight), ProjectionType.ORTHOGRAPHIC);

        MatrixStack matrices = new MatrixStack();
        float scaleFactor = Math.min(framebufferWidth, framebufferHeight) / 6.0F;
        matrices.translate(framebufferWidth / 2.0F + anchorOffsetX, framebufferHeight * 0.8F + anchorOffsetY, 0);
        matrices.scale(scaleFactor, -scaleFactor, scaleFactor);
        matrices.multiply(rotation);
        matrices.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        VertexConsumerProvider.Immediate consumers = this.client.getBufferBuilders().getEntityVertexConsumers();
        for (PlacedBlock placedBlock : this.blocks) {
            matrices.push();
            BlockPos pos = placedBlock.pos();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            this.blockRenderManager.renderBlockAsEntity(
                    placedBlock.state(),
                    matrices,
                    consumers,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE,
                    OverlayTexture.DEFAULT_UV
            );
            matrices.pop();
        }

        for (PlacedFluid placedFluid : this.fluids) {
            BlockPos pos = placedFluid.pos();
            // FluidRenderer writes vertices in chunk-local (0-15) coordinates, so add the
            // chunk origin back in to get world-space positions that match the other
            // geometry in this scene.
            RenderLayer fluidLayer = mapBlockLayer(RenderLayers.getFluidLayer(placedFluid.state()));
            VertexConsumer base = consumers.getBuffer(fluidLayer);
            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());

            VertexConsumer transformedConsumer = new TransformedVertexConsumer(
                    base,
                    matrices.peek()
            );
            this.blockRenderManager.renderFluid(BlockPos.ORIGIN, this.world, transformedConsumer, this.world.getBlockState(pos), placedFluid.state());
            matrices.pop();
        }

        for (Entity entity : this.entities) {
            Vec3d entityPos = entity.getLerpedPos(tickDelta);
            this.client.getEntityRenderDispatcher().render(
                    entity,
                    entityPos.x - cameraPos.x,
                    entityPos.y - cameraPos.y,
                    entityPos.z - cameraPos.z,
                    tickDelta,
                    matrices,
                    consumers,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE
            );
        }
        consumers.draw();

        RenderSystem.restoreProjectionMatrix();
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;

        Matrix3x2f pose = new Matrix3x2f(context.getMatrices());
        context.state.addSimpleElement(
                new TexturedQuadGuiElementRenderState(
                        RenderPipelines.GUI_TEXTURED,
                        TextureSetup.withoutGlTexture(this.framebuffer.getColorAttachmentView()),
                        pose,
                        x,
                        y,
                        x + width,
                        y + height,
                        0.0F,
                        1.0F,
                        1.0F,
                        0.0F,
                        -1,
                        context.scissorStack.peekLast()
                )
        );
    }

    private void ensureFramebuffer(int width, int height) {
        if (this.framebuffer != null && this.framebuffer.textureWidth == width && this.framebuffer.textureHeight == height) {
            return;
        }

        if (this.framebuffer != null) {
            this.framebuffer.delete();
        }

        this.framebuffer = new SimpleFramebuffer("FakeWorldScene", width, height, true);
    }

    public void setAnchor(BlockPos blockPos, int screenX, int screenY) {
        this.anchorBlock = blockPos;
        this.anchorTargetX = screenX;
        this.anchorTargetY = screenY;
    }

    public void clearAnchor() {
        this.anchorBlock = null;
    }

    public void rotateCamera(float deltaYaw, float deltaPitch) {
        float yaw = this.cameraEntity.getYaw() + deltaYaw;
        float pitch = MathHelper.clamp(this.cameraEntity.getPitch() + deltaPitch, -89.0F, 89.0F);
        this.cameraEntity.setYaw(yaw);
        this.cameraEntity.setPitch(pitch);
    }

    private static Vec2f projectToScreen(Vec3d worldPos, Matrix4f modelView, Matrix4f projection, int framebufferWidth, int framebufferHeight) {
        Vector4f vec = new Vector4f((float) worldPos.x, (float) worldPos.y, (float) worldPos.z, 1.0F);
        vec.mul(modelView);
        vec.mul(projection);
        float invW = 1.0F / vec.w;
        float ndcX = vec.x * invW;
        float ndcY = vec.y * invW;
        float screenX = (ndcX * 0.5F + 0.5F) * framebufferWidth;
        float screenY = (1.0F - (ndcY * 0.5F + 0.5F)) * framebufferHeight;
        return new Vec2f(screenX, screenY);
    }

    private static RenderLayer mapBlockLayer(BlockRenderLayer layer) {
        return switch (layer) {
            case SOLID -> RenderLayer.getSolid();
            case CUTOUT_MIPPED -> RenderLayer.getCutoutMipped();
            case CUTOUT, TRIPWIRE -> RenderLayer.getCutout();
            case TRANSLUCENT -> RenderLayer.getTranslucentMovingBlock();
        };
    }

    private static final class TransformedVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final Matrix4f modelViewMatrix;
        private final Matrix3f normalMatrix;

        private TransformedVertexConsumer(VertexConsumer delegate, MatrixStack.Entry matrices) {
            this.delegate = delegate;
            this.modelViewMatrix = matrices.getPositionMatrix();
            this.normalMatrix = matrices.getNormalMatrix();
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            Vector4f vector4f = this.modelViewMatrix.transform(new Vector4f(x, y, z, 1.0F));
            return this.delegate.vertex(vector4f.x(), vector4f.y(), vector4f.z());
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return this.delegate.color(red, green, blue, alpha);
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            return this.delegate.texture(u, v);
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            return this.delegate.overlay(u, v);
        }

        @Override
        public VertexConsumer light(int u, int v) {
            return this.delegate.light(u, v);
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            Vector3f vector3f = this.normalMatrix.transform(new Vector3f(x, y, z));
            return this.delegate.normal(vector3f.x(), vector3f.y(), vector3f.z());
        }

        @Override
        public void vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
            Vector4f vector4f = this.modelViewMatrix.transform(new Vector4f(x, y, z, 1.0F));
            Vector3f vector3f = this.normalMatrix.transform(new Vector3f(normalX, normalY, normalZ));
            this.delegate.vertex(
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

    protected record PlacedBlock(BlockPos pos, BlockState state) {
    }

    protected record PlacedFluid(BlockPos pos, FluidState state) {
    }
}
