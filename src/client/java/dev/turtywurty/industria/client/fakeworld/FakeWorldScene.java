package dev.turtywurty.industria.client.fakeworld;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.ProjectionMatrix2;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3x2f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders a tiny client-only world into an arbitrary rectangle.
 */
public final class FakeWorldScene {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ClientWorld world;
    private final Entity cameraEntity;
    private final Entity showcaseEntity;
    private final BlockRenderManager blockRenderManager;
    private final List<PlacedBlock> blocks = new ArrayList<>();
    private final net.minecraft.client.render.Camera camera = new net.minecraft.client.render.Camera();
    private final ProjectionMatrix2 projectionMatrix = new ProjectionMatrix2("FakeWorldScene", -1000.0F, 1000.0F, true);
    private SimpleFramebuffer framebuffer;

    public FakeWorldScene() {
        FakeWorldBuilder.Result result = FakeWorldBuilder.create(this.client);
        this.world = result.world();
        this.cameraEntity = result.player();
        this.blockRenderManager = this.client.getBlockRenderManager();

        this.showcaseEntity = createShowcaseEntity();
        this.world.addEntity(this.showcaseEntity);

        this.cameraEntity.setPos(2.5, 65.0, 4.0);
        this.cameraEntity.setYaw(200.0F);
        this.cameraEntity.setPitch(-15.0F);
        seedBlocks();
    }

    private Entity createShowcaseEntity() {
        ArmorStandEntity armorStand = new ArmorStandEntity(this.world, 1.5, 64.0, 1.5);
        armorStand.setCustomName(Text.literal("Client-only entity"));
        armorStand.setNoGravity(true);
        armorStand.setInvisible(false);
        return armorStand;
    }

    private void seedBlocks() {
        this.blocks.add(new PlacedBlock(new BlockPos(1, 63, 1), Blocks.OAK_LOG.getDefaultState()));
        this.blocks.add(new PlacedBlock(new BlockPos(1, 64, 1), Blocks.GLOWSTONE.getDefaultState()));
        this.blocks.add(new PlacedBlock(new BlockPos(2, 63, 1), Blocks.OAK_LEAVES.getDefaultState()));
        this.blocks.add(new PlacedBlock(new BlockPos(2, 64, 1), Blocks.GLASS.getDefaultState()));
        this.blocks.add(new PlacedBlock(new BlockPos(1, 63, 2), Blocks.STONE_BRICKS.getDefaultState()));
        this.blocks.add(new PlacedBlock(new BlockPos(1, 64, 2), Blocks.WATER.getDefaultState()));
    }

    public void tick() {
        this.world.tick(() -> true);
        this.world.tickEntities();

        this.showcaseEntity.setYaw(this.showcaseEntity.getYaw() + 1.5F);
        this.showcaseEntity.setHeadYaw(this.showcaseEntity.getYaw());
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

        RenderSystem.backupProjectionMatrix();
        RenderSystem.outputColorTextureOverride = this.framebuffer.getColorAttachmentView();
        RenderSystem.outputDepthTextureOverride = this.framebuffer.getDepthAttachmentView();
        RenderSystem.getDevice()
                .createCommandEncoder()
                .clearColorAndDepthTextures(this.framebuffer.getColorAttachment(), 0, this.framebuffer.getDepthAttachment(), 1.0);
        RenderSystem.setProjectionMatrix(this.projectionMatrix.set(framebufferWidth, framebufferHeight), ProjectionType.ORTHOGRAPHIC);

        MatrixStack matrices = new MatrixStack();
        float scaleFactor = Math.min(width, height) / 6.0F;
        matrices.translate(width / 2.0F, height * 0.8F, 0);
        matrices.scale(scaleFactor, -scaleFactor, scaleFactor);
        matrices.multiply(rotation);
        matrices.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        VertexConsumerProvider.Immediate consumers = this.client.getBufferBuilders().getEntityVertexConsumers();
        for (PlacedBlock placedBlock : this.blocks) {
            matrices.push();
            BlockPos pos = placedBlock.pos();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            BlockState state = placedBlock.state();
            this.blockRenderManager.renderBlockAsEntity(state, matrices, consumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }

        Vec3d entityPos = this.showcaseEntity.getLerpedPos(tickDelta);
        this.client.getEntityRenderDispatcher().render(
                this.showcaseEntity,
                entityPos.x - cameraPos.x,
                entityPos.y - cameraPos.y,
                entityPos.z - cameraPos.z,
                tickDelta,
                matrices,
                consumers,
                LightmapTextureManager.MAX_LIGHT_COORDINATE
        );
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

    private record PlacedBlock(BlockPos pos, BlockState state) {
    }
}
