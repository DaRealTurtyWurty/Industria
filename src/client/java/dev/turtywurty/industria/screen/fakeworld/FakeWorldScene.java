package dev.turtywurty.industria.screen.fakeworld;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.turtywurty.industria.multiblock.VariedBlockList;
import dev.turtywurty.industria.util.VariedBlockListRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.util.*;
import java.util.function.Consumer;

/**
 * Shared fake-world scene implementation used by builder-produced scenes.
 */
public class FakeWorldScene implements AutoCloseable {
    protected final Minecraft client = Minecraft.getInstance();
    protected final ClientLevel world;
    protected final Entity cameraEntity;
    protected final BlockRenderDispatcher blockRenderManager;
    protected final List<PlacedBlock> blocks;
    protected final List<PlacedFluid> fluids;
    protected final List<Entity> entities;
    protected final List<PlacedVariedBlockList> variedBlockLists;
    protected final List<Nameplate> nameplates;
    protected final Camera camera = new Camera();
    protected final CachedOrthoProjectionMatrixBuffer projectionMatrix = new CachedOrthoProjectionMatrixBuffer("FakeWorldScene", -1000.0F, 1000.0F, true);
    private final Consumer<FakeWorldSceneBuilder.SceneTickContext> tickHandler;
    private final Map<RenderStage, List<Consumer<RenderContext>>> beforeRenderCallbacks = new EnumMap<>(RenderStage.class);
    private final Map<RenderStage, List<Consumer<RenderContext>>> afterRenderCallbacks = new EnumMap<>(RenderStage.class);
    private final Map<SceneElement, TransformProvider> transforms = new HashMap<>();
    private final SubmitNodeStorage commandQueue = new SubmitNodeStorage();
    private final FeatureRenderDispatcher renderDispatcher;
    private final BlockAndTintGetter uiLightmapBlockView;
    private final List<SelectionBox> selectionBoxes = new ArrayList<>();
    private Matrix4f lastModelView;
    private Matrix4f lastProjection;
    private int lastFramebufferWidth = -1;
    private int lastFramebufferHeight = -1;
    private int lastWidgetX = -1;
    private int lastWidgetY = -1;
    private int lastWidgetWidth = -1;
    private int lastWidgetHeight = -1;
    private float lastScaleFactor = 1.0F;
    private float scaleMultiplier = 1.0F;
    protected TextureTarget framebuffer;
    private BlockPos anchorBlock;
    private int anchorTargetX;
    private int anchorTargetY;
    private Vec3 variedBlockListOffset = Vec3.ZERO;

    public FakeWorldScene(FakeWorldSceneBuilder.BuiltScene builtScene) {
        this(builtScene, builtScene.tickHandler());
    }

    public FakeWorldScene(FakeWorldSceneBuilder.BuiltScene builtScene, Consumer<FakeWorldSceneBuilder.SceneTickContext> tickHandler) {
        this.world = builtScene.result().world();
        this.cameraEntity = builtScene.result().player();
        this.blockRenderManager = this.client.getBlockRenderer();
        this.blocks = new ArrayList<>(builtScene.blocks());
        this.fluids = new ArrayList<>(builtScene.fluids());
        this.entities = new ArrayList<>(builtScene.entities());
        this.variedBlockLists = new ArrayList<>(builtScene.variedBlockLists());
        this.nameplates = new ArrayList<>(builtScene.nameplates());
        this.tickHandler = tickHandler;
        this.renderDispatcher = new FeatureRenderDispatcher(
                this.commandQueue,
                this.blockRenderManager,
                this.client.renderBuffers().bufferSource(),
                this.client.getAtlasManager(),
                this.client.renderBuffers().outlineBufferSource(),
                this.client.renderBuffers().crumblingBufferSource(),
                this.client.font
        );
        this.uiLightmapBlockView = new UiLightmapBlockAndTintGetter(this.world);

        this.cameraEntity.setPosRaw(builtScene.cameraPos().x, builtScene.cameraPos().y, builtScene.cameraPos().z);
        this.cameraEntity.setYRot(builtScene.cameraYaw());
        this.cameraEntity.setXRot(builtScene.cameraPitch());
        this.cameraEntity.setOldPosAndRot();

        for (Entity entity : this.entities) {
            this.world.addEntity(entity);
            entity.setOldPosAndRot();
        }

        for (RenderStage stage : RenderStage.values()) {
            this.beforeRenderCallbacks.put(stage, new ArrayList<>());
            this.afterRenderCallbacks.put(stage, new ArrayList<>());
        }

        markUsedChunksForTicking();
    }

    private static Vec2 projectToScreen(Vec3 worldPos, Matrix4f modelView, Matrix4f projection, int framebufferWidth, int framebufferHeight) {
        Vector4f vec = new Vector4f((float) worldPos.x, (float) worldPos.y, (float) worldPos.z, 1.0F);
        vec.mul(modelView);
        vec.mul(projection);
        float invW = 1.0F / vec.w;
        float ndcX = vec.x * invW;
        float ndcY = vec.y * invW;
        float screenX = (ndcX * 0.5F + 0.5F) * framebufferWidth;
        float screenY = (1.0F - (ndcY * 0.5F + 0.5F)) * framebufferHeight;
        return new Vec2(screenX, screenY);
    }

    private static RenderType mapBlockLayer(ChunkSectionLayer layer) {
        return switch (layer) {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT, TRIPWIRE -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
        };
    }

    public void tick() {
        this.world.tick(() -> true);
        this.world.tickEntities();
        this.tickHandler.accept(new FakeWorldSceneBuilder.SceneTickContext(this.world, List.copyOf(this.entities)));
    }

    @Override
    public void close() {
        if (this.framebuffer != null) {
            this.framebuffer.destroyBuffers();
            this.framebuffer = null;
        }
        this.renderDispatcher.close();
    }

    public void render(GuiGraphics context, int x, int y, int width, int height, float tickDelta) {
        this.camera.setup(this.world, this.cameraEntity, false, false, tickDelta);
        Vec3 cameraPos = this.camera.position();
        Quaternionf rotation = this.camera.rotation().conjugate(new Quaternionf());
        var cameraRenderState = new CameraRenderState();
        cameraRenderState.initialized = this.camera.isInitialized();
        cameraRenderState.pos = cameraPos;
        cameraRenderState.blockPos = this.camera.blockPosition();
        cameraRenderState.entityPos = this.cameraEntity.getPosition(tickDelta);
        cameraRenderState.orientation = new Quaternionf(this.camera.rotation());

        var window = this.client.getWindow();
        double scale = window.getGuiScale();
        int framebufferWidth = Math.max(1, (int) Math.round(width * scale));
        int framebufferHeight = Math.max(1, (int) Math.round(height * scale));
        ensureFramebuffer(framebufferWidth, framebufferHeight);

        this.client.getEntityRenderDispatcher().prepare(this.camera, this.cameraEntity);

        float anchorOffsetX = 0.0F;
        float anchorOffsetY = 0.0F;
        if (this.anchorBlock != null) {
            var anchorMatrices = new PoseStack();
            float scaleFactor = Math.min(framebufferWidth, framebufferHeight) / 6.0F * this.scaleMultiplier;
            anchorMatrices.translate(framebufferWidth / 2.0F, framebufferHeight * 0.8F, 0);
            anchorMatrices.scale(scaleFactor, -scaleFactor, scaleFactor);
            anchorMatrices.mulPose(rotation);
            anchorMatrices.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

            var projection = new Matrix4f().setOrtho(0.0F, framebufferWidth, framebufferHeight, 0.0F, -1000.0F, 1000.0F);
            Vec2 projected = projectToScreen(Vec3.atCenterOf(this.anchorBlock), anchorMatrices.last().pose(), projection, framebufferWidth, framebufferHeight);
            float targetX = (float) (this.anchorTargetX * scale);
            float targetY = (float) (this.anchorTargetY * scale);
            anchorOffsetX = targetX - projected.x;
            anchorOffsetY = targetY - projected.y;
        }

        RenderSystem.backupProjectionMatrix();
        RenderSystem.outputColorTextureOverride = this.framebuffer.getColorTextureView();
        RenderSystem.outputDepthTextureOverride = this.framebuffer.getDepthTextureView();
        RenderSystem.getDevice()
                .createCommandEncoder()
                .clearColorAndDepthTextures(this.framebuffer.getColorTexture(), 0, this.framebuffer.getDepthTexture(), 1.0);
        RenderSystem.setProjectionMatrix(this.projectionMatrix.getBuffer(framebufferWidth, framebufferHeight), ProjectionType.ORTHOGRAPHIC);

        var matrices = new PoseStack();
        float scaleFactor = Math.min(framebufferWidth, framebufferHeight) / 6.0F * this.scaleMultiplier;
        matrices.translate(framebufferWidth / 2.0F + anchorOffsetX, framebufferHeight * 0.8F + anchorOffsetY, 0);
        matrices.scale(scaleFactor, -scaleFactor, scaleFactor);
        matrices.mulPose(rotation);
        matrices.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);
        this.lastModelView = new Matrix4f(matrices.last().pose());
        this.lastProjection = new Matrix4f().setOrtho(0.0F, framebufferWidth, framebufferHeight, 0.0F, -1000.0F, 1000.0F);
        this.lastFramebufferWidth = framebufferWidth;
        this.lastFramebufferHeight = framebufferHeight;
        this.lastWidgetX = x;
        this.lastWidgetY = y;
        this.lastWidgetWidth = width;
        this.lastWidgetHeight = height;
        this.lastScaleFactor = (float) scale;

        MultiBufferSource.BufferSource consumers = this.renderDispatcher.bufferSource;
        this.commandQueue.clear();
        RenderContext renderContext = new RenderContext(context, matrices, consumers, tickDelta, framebufferWidth, framebufferHeight, this.world);
        runCallbacks(this.beforeRenderCallbacks, RenderStage.SCENE, renderContext);

        runCallbacks(this.beforeRenderCallbacks, RenderStage.BLOCKS, renderContext);
        for (PlacedBlock placedBlock : List.copyOf(this.blocks)) {
            if (placedBlock.state().getRenderShape() != RenderShape.MODEL)
                continue;

            matrices.pushPose();
            BlockPos pos = placedBlock.pos();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            applyTransform(matrices, placedBlock, renderContext.world(), tickDelta);
            this.blockRenderManager.renderSingleBlock(
                    placedBlock.state(),
                    matrices,
                    consumers,
                    LightCoordsUtil.UI_FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY
            );
            matrices.popPose();
        }

        runCallbacks(this.afterRenderCallbacks, RenderStage.BLOCKS, renderContext);

        runCallbacks(this.beforeRenderCallbacks, RenderStage.VARIED_BLOCK_LISTS, renderContext);
        for (PlacedVariedBlockList placedVariedBlockList : List.copyOf(this.variedBlockLists)) {
            matrices.pushPose();
            BlockPos pos = placedVariedBlockList.pos();
            matrices.translate(pos.getX() + this.variedBlockListOffset.x, pos.getY() + this.variedBlockListOffset.y, pos.getZ() + this.variedBlockListOffset.z);
            applyTransform(matrices, placedVariedBlockList, renderContext.world(), tickDelta);
            VariedBlockListRenderer.renderInWorld(
                    placedVariedBlockList.variedBlockList(),
                    pos,
                    this.world,
                    matrices,
                    this.commandQueue,
                    LightCoordsUtil.UI_FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    this.camera,
                    cameraRenderState,
                    tickDelta,
                    consumers
            );
            matrices.popPose();
        }

        runCallbacks(this.afterRenderCallbacks, RenderStage.VARIED_BLOCK_LISTS, renderContext);

        runCallbacks(this.beforeRenderCallbacks, RenderStage.FLUIDS, renderContext);
        for (PlacedFluid placedFluid : List.copyOf(this.fluids)) {
            BlockPos pos = placedFluid.pos();
            // FluidRenderer writes vertices in chunk-local (0-15) coordinates, so add the
            // chunk origin back in to get world-space positions that match the other
            // geometry in this scene.
            RenderType fluidLayer = mapBlockLayer(ItemBlockRenderTypes.getRenderLayer(placedFluid.state()));
            VertexConsumer base = consumers.getBuffer(fluidLayer);
            matrices.pushPose();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            applyTransform(matrices, placedFluid, renderContext.world(), tickDelta);

            var transformedConsumer = new TransformedVertexConsumer(
                    base,
                    matrices.last(),
                    LightCoordsUtil.UI_FULL_BRIGHT
            );
            this.blockRenderManager.renderLiquid(BlockPos.ZERO, this.uiLightmapBlockView, transformedConsumer, this.world.getBlockState(pos), placedFluid.state());
            matrices.popPose();
        }

        runCallbacks(this.afterRenderCallbacks, RenderStage.FLUIDS, renderContext);

        BlockEntityRenderDispatcher blockEntityRenderManager = this.client.getBlockEntityRenderDispatcher();
        blockEntityRenderManager.prepare(this.camera);
        runCallbacks(this.beforeRenderCallbacks, RenderStage.BLOCK_ENTITIES, renderContext);
        for (PlacedBlock placedBlock : List.copyOf(this.blocks)) {
            if (!placedBlock.state().hasBlockEntity())
                continue;

            BlockEntity blockEntity = this.world.getBlockEntity(placedBlock.pos());
            if (blockEntity == null)
                continue;

            matrices.pushPose();
            BlockPos pos = placedBlock.pos();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());

            var crumblingOverlay = new ModelFeatureRenderer.CrumblingOverlay(0, matrices.last());
            BlockEntityRenderState state = getBlockEntityRenderState(
                    blockEntityRenderManager,
                    blockEntity,
                    tickDelta,
                    crumblingOverlay,
                    cameraPos
            );

            if (state != null) {
                state.lightCoords = LightCoordsUtil.UI_FULL_BRIGHT;
                blockEntityRenderManager.submit(state, matrices, this.commandQueue, cameraRenderState);
            }
            matrices.popPose();
        }

        runCallbacks(this.afterRenderCallbacks, RenderStage.BLOCK_ENTITIES, renderContext);

        runCallbacks(this.beforeRenderCallbacks, RenderStage.ENTITIES, renderContext);
        var entityRenderManager = this.client.getEntityRenderDispatcher();
        for (Entity entity : List.copyOf(this.entities)) {
            EntityRenderState renderState = entityRenderManager.extractEntity(entity, tickDelta);
            renderState.lightCoords = LightCoordsUtil.UI_FULL_BRIGHT;
            entityRenderManager.submit(
                    renderState,
                    cameraRenderState,
                    renderState.x,
                    renderState.y,
                    renderState.z,
                    matrices,
                    this.commandQueue
            );
        }

        runCallbacks(this.afterRenderCallbacks, RenderStage.ENTITIES, renderContext);

        drawSelectionBoxes(renderContext);

        runCallbacks(this.beforeRenderCallbacks, RenderStage.NAMEPLATES, renderContext);
        for (Nameplate nameplate : List.copyOf(this.nameplates)) {
            matrices.pushPose();
            Vec3 pos = nameplate.position();
            matrices.translate(pos.x, pos.y + nameplate.yOffset(), pos.z);
            applyTransform(matrices, nameplate, renderContext.world(), tickDelta);
            renderBillboardedText(matrices, consumers, nameplate.text(), true, 0.025F);
            matrices.popPose();
        }
        runCallbacks(this.afterRenderCallbacks, RenderStage.NAMEPLATES, renderContext);

        runCallbacks(this.beforeRenderCallbacks, RenderStage.CUSTOM, renderContext);
        runCallbacks(this.afterRenderCallbacks, RenderStage.CUSTOM, renderContext);
        this.renderDispatcher.renderAllFeatures();
        consumers.endBatch();
        this.commandQueue.endFrame();
        this.renderDispatcher.endFrame();

        runCallbacks(this.afterRenderCallbacks, RenderStage.SCENE, renderContext);

        RenderSystem.restoreProjectionMatrix();
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;

        runCallbacks(this.beforeRenderCallbacks, RenderStage.FINALIZE, renderContext);
        var pose = new Matrix3x2f(context.pose());
        context.guiRenderState.submitGuiElement(
                new BlitRenderState(
                        RenderPipelines.GUI_TEXTURED,
                        TextureSetup.singleTexture(
                                this.framebuffer.getColorTextureView(),
                                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)
                        ),
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
                        context.scissorStack.peek()
                )
        );
        runCallbacks(this.afterRenderCallbacks, RenderStage.FINALIZE, renderContext);
    }

    public void drawNameplate(RenderContext renderContext, Component text, Vec3 worldPos, float yOffset, boolean seeThrough, float scale) {
        PoseStack matrices = renderContext.matrices();
        matrices.pushPose();
        matrices.translate(worldPos.x, worldPos.y + yOffset, worldPos.z);
        renderBillboardedText(matrices, renderContext.consumers(), text, seeThrough, scale);
        matrices.popPose();
    }

    public void drawNameplate(RenderContext renderContext, Component text, Vec3 worldPos) {
        drawNameplate(renderContext, text, worldPos, 0.0F, true, 0.025F);
    }

    public void drawNameplate(RenderContext renderContext, Component text, Vec3 worldPos, float yOffset) {
        drawNameplate(renderContext, text, worldPos, yOffset, true, 0.025F);
    }

    private void renderBillboardedText(PoseStack matrices, MultiBufferSource consumers, Component text, boolean seeThrough, float scale) {
        matrices.pushPose();
        matrices.mulPose(this.client.getEntityRenderDispatcher().camera.rotation());
        matrices.scale(scale, -scale, scale);

        Font textRenderer = this.client.font;
        Matrix4f matrix = matrices.last().pose();
        float xOffset = -textRenderer.width(text) / 2.0F;
        int background = (int) (this.client.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        Font.DisplayMode layer = seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;

        textRenderer.drawInBatch(
                text,
                xOffset,
                0.0F,
                -2130706433,
                false,
                matrix,
                consumers,
                layer,
                background,
                LightCoordsUtil.UI_FULL_BRIGHT
        );

        if (seeThrough) {
            textRenderer.drawInBatch(
                    text,
                    xOffset,
                    0.0F,
                    -1,
                    false,
                    matrix,
                    consumers,
                    Font.DisplayMode.NORMAL,
                    0,
                    LightCoordsUtil.lightCoordsWithEmission(LightCoordsUtil.UI_FULL_BRIGHT, 2)
            );
        }

        matrices.popPose();
    }

    private <T extends SceneElement> void applyTransform(PoseStack matrices, T element, ClientLevel world, float tickDelta) {
        TransformProvider provider = this.transforms.get(element);
        if (provider == null)
            return;

        Transform transform = provider.get(tickDelta, world);
        if (transform == null || transform.isIdentity())
            return;

        transform.apply(matrices);
    }

    private void ensureFramebuffer(int width, int height) {
        if (this.framebuffer != null && this.framebuffer.width == width && this.framebuffer.height == height)
            return;

        if (this.framebuffer != null) {
            this.framebuffer.destroyBuffers();
        }

        this.framebuffer = new TextureTarget("FakeWorldScene", width, height, true);
    }

    private static BlockEntityRenderState getBlockEntityRenderState(
            BlockEntityRenderDispatcher blockEntityRenderManager,
            BlockEntity blockEntity,
            float tickDelta,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            Vec3 cameraPos
    ) {
        BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = blockEntityRenderManager.getRenderer(blockEntity);
        if (renderer == null)
            return null;

        if (!blockEntity.hasLevel() || !blockEntity.getType().isValid(blockEntity.getBlockState()))
            return null;

        BlockEntityRenderState state = renderer.createRenderState();
        renderer.extractRenderState(blockEntity, state, tickDelta, cameraPos, crumblingOverlay);
        return state;
    }

    public void setAnchor(BlockPos blockPos, int screenX, int screenY) {
        this.anchorBlock = blockPos;
        this.anchorTargetX = screenX;
        this.anchorTargetY = screenY;
    }

    public void clearAnchor() {
        this.anchorBlock = null;
    }

    public void setScaleMultiplier(float scaleMultiplier) {
        this.scaleMultiplier = Mth.clamp(scaleMultiplier, 0.1F, 20.0F);
    }

    public void rotateCamera(float deltaYaw, float deltaPitch) {
        float yaw = this.cameraEntity.getYRot() + deltaYaw;
        float pitch = Mth.clamp(this.cameraEntity.getXRot() + deltaPitch, -89.0F, 89.0F);
        this.cameraEntity.setYRot(yaw);
        this.cameraEntity.setXRot(pitch);
        updateChunkCenter(BlockPos.containing(this.cameraEntity.position()));
    }

    public void setCameraRotation(float yaw, float pitch) {
        this.cameraEntity.setYRot(yaw);
        this.cameraEntity.setXRot(Mth.clamp(pitch, -89.0F, 89.0F));
    }

    public void setCamera(Vec3 position, float yaw, float pitch) {
        setCameraPosition(position);
        setCameraRotation(yaw, pitch);
    }

    public void moveCamera(Vec3 delta) {
        setCameraPosition(this.cameraEntity.position().add(delta));
    }

    public Vec3 getCameraPosition() {
        return this.cameraEntity.position();
    }

    public void setCameraPosition(Vec3 position) {
        this.cameraEntity.setPosRaw(position.x, position.y, position.z);
        this.cameraEntity.setOldPosAndRot();
        updateChunkCenter(BlockPos.containing(position));
    }

    public void addBlock(BlockPos pos, BlockState state) {
        ensureChunk(pos);
        replaceBlock(pos, state);
        removeElementsAtPos(this.fluids, pos);
        this.world.setBlock(pos, state, Block.UPDATE_CLIENTS);
        if (state.hasBlockEntity() && state.getBlock() instanceof EntityBlock provider) {
            BlockEntity blockEntity = provider.newBlockEntity(pos, state);
            if (blockEntity != null) {
                this.world.setBlockEntity(blockEntity);
            }
        } else {
            this.world.removeBlockEntity(pos);
        }

        this.world.onChunkLoaded(new ChunkPos(pos));
    }

    public void removeBlock(BlockPos pos) {
        ensureChunk(pos);
        this.world.removeBlockEntity(pos);
        this.world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        removeElementsAtPos(this.blocks, pos);
        removeElementsAtPos(this.fluids, pos);
        this.world.onChunkLoaded(new ChunkPos(pos));
    }

    public void addVariedBlockList(BlockPos pos, VariedBlockList variedBlockList) {
        ensureChunk(pos);
        replaceVariedBlockList(pos, variedBlockList);
    }

    public void removeVariedBlockList(BlockPos pos) {
        ensureChunk(pos);
        removeElementsAtPos(this.variedBlockLists, pos);
    }

    public void clearVariedBlockLists() {
        this.variedBlockLists.forEach(this::clearTransform);
        this.variedBlockLists.clear();
    }

    public void setVariedBlockListOffset(Vec3 offset) {
        this.variedBlockListOffset = offset;
    }

    public void addFluid(BlockPos pos, FluidState state) {
        ensureChunk(pos);
        replaceFluid(pos, state);
        removeElementsAtPos(this.blocks, pos);
        this.world.setBlock(pos, state.createLegacyBlock(), Block.UPDATE_CLIENTS);
        this.world.onChunkLoaded(new ChunkPos(pos));
    }

    public void removeFluid(BlockPos pos) {
        ensureChunk(pos);
        removeElementsAtPos(this.fluids, pos);
        this.world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        this.world.onChunkLoaded(new ChunkPos(pos));
    }

    public void addEntity(Entity entity) {
        this.entities.add(entity);
        this.world.addEntity(entity);
        entity.setOldPosAndRot();
    }

    public boolean removeEntity(Entity entity) {
        this.world.removeEntity(entity.getId(), RemovalReason.DISCARDED);
        return this.entities.remove(entity);
    }

    public Nameplate addNameplate(Vec3 position, Component text, float yOffset) {
        Nameplate nameplate = new Nameplate(position, text, yOffset);
        this.nameplates.add(nameplate);
        return nameplate;
    }

    public Nameplate addNameplate(Vec3 position, Component text) {
        return addNameplate(position, text, 0.0F);
    }

    public boolean removeNameplate(Nameplate nameplate) {
        clearTransform(nameplate);
        return this.nameplates.remove(nameplate);
    }

    public void clearNameplates() {
        for (Nameplate nameplate : this.nameplates) {
            clearTransform(nameplate);
        }
        this.nameplates.clear();
    }

    public void setTransform(SceneElement element, Transform transform) {
        setTransform(element, TransformProvider.constant(transform));
    }

    public void setTransform(SceneElement element, TransformProvider provider) {
        if (provider == null) {
            this.transforms.remove(element);
        } else {
            this.transforms.put(element, provider);
        }
    }

    public void clearTransform(SceneElement element) {
        this.transforms.remove(element);
    }

    public void clearTransform() {
        this.transforms.clear();
    }

    public void clearTransforms() {
        clearTransform();
    }

    public void onBeforeRender(RenderStage stage, Consumer<RenderContext> callback) {
        this.beforeRenderCallbacks.get(stage).add(callback);
    }

    public void onAfterRender(RenderStage stage, Consumer<RenderContext> callback) {
        this.afterRenderCallbacks.get(stage).add(callback);
    }

    public void clearRenderCallbacks(RenderStage stage) {
        this.beforeRenderCallbacks.get(stage).clear();
        this.afterRenderCallbacks.get(stage).clear();
    }

    private void addChunksFor(Set<ChunkPos> chunks, Collection<? extends SceneElement> elements) {
        for (SceneElement element : elements) {
            chunks.add(element.chunkPos());
        }
    }

    private <T extends SceneElement> void removeElementsAtPos(Collection<T> elements, BlockPos pos) {
        elements.removeIf(element -> {
            if (element.blockPos().equals(pos)) {
                clearTransform(element);
                return true;
            }
            return false;
        });
    }

    private void transferTransform(SceneElement from, SceneElement to) {
        TransformProvider provider = this.transforms.remove(from);
        if (provider != null) {
            this.transforms.put(to, provider);
        }
    }

    private void markUsedChunksForTicking() {
        Set<ChunkPos> chunks = new HashSet<>();
        addChunksFor(chunks, this.blocks);
        addChunksFor(chunks, this.fluids);
        addChunksFor(chunks, this.variedBlockLists);
        addChunksFor(chunks, this.nameplates);

        for (Entity entity : this.entities) {
            chunks.add(new ChunkPos(BlockPos.containing(entity.position())));
        }

        chunks.add(new ChunkPos(BlockPos.containing(this.cameraEntity.position())));

        for (ChunkPos chunkPos : chunks) {
            this.world.onChunkLoaded(chunkPos);
        }
    }

    private LevelChunk ensureChunk(BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        var chunkManager = this.world.getChunkSource();
        chunkManager.updateViewCenter(chunkX, chunkZ);

        var chunkMap = chunkManager.storage;
        int index = chunkMap.getIndex(chunkX, chunkZ);
        LevelChunk chunk = chunkMap.getChunk(index);
        if (chunk == null || chunk.getPos().x != chunkX || chunk.getPos().z != chunkZ) {
            chunk = new LevelChunk(this.world, new ChunkPos(chunkX, chunkZ));
            chunkMap.replace(index, chunk);
            this.world.onChunkLoaded(new ChunkPos(chunkX, chunkZ));
        }

        return chunk;
    }

    private void replaceBlock(BlockPos pos, BlockState state) {
        for (int i = 0; i < this.blocks.size(); i++) {
            if (this.blocks.get(i).pos().equals(pos)) {
                PlacedBlock newBlock = new PlacedBlock(pos, state);
                transferTransform(this.blocks.get(i), newBlock);
                this.blocks.set(i, newBlock);
                return;
            }
        }

        this.blocks.add(new PlacedBlock(pos, state));
    }

    private void replaceFluid(BlockPos pos, FluidState state) {
        for (int i = 0; i < this.fluids.size(); i++) {
            if (this.fluids.get(i).pos().equals(pos)) {
                PlacedFluid newFluid = new PlacedFluid(pos, state);
                transferTransform(this.fluids.get(i), newFluid);
                this.fluids.set(i, newFluid);
                return;
            }
        }

        this.fluids.add(new PlacedFluid(pos, state));
    }

    private void replaceVariedBlockList(BlockPos pos, VariedBlockList variedBlockList) {
        for (int i = 0; i < this.variedBlockLists.size(); i++) {
            if (this.variedBlockLists.get(i).pos().equals(pos)) {
                PlacedVariedBlockList newVariedBlockList = new PlacedVariedBlockList(pos, variedBlockList);
                transferTransform(this.variedBlockLists.get(i), newVariedBlockList);
                this.variedBlockLists.set(i, newVariedBlockList);
                return;
            }
        }

        this.variedBlockLists.add(new PlacedVariedBlockList(pos, variedBlockList));
    }

    private void runCallbacks(Map<RenderStage, List<Consumer<RenderContext>>> callbacks, RenderStage stage, RenderContext context) {
        List<Consumer<RenderContext>> stageCallbacks = callbacks.get(stage);
        if (stageCallbacks == null)
            return;

        for (Consumer<RenderContext> callback : List.copyOf(stageCallbacks)) {
            callback.accept(context);
        }
    }

    private void updateChunkCenter(BlockPos focus) {
        this.world.getChunkSource().updateViewCenter(focus.getX() >> 4, focus.getZ() >> 4);
    }

    public void updateAnchor(Collection<BlockPos> positions, int targetX, int targetY) {
        if (positions.isEmpty()) {
            clearAnchor();
            return;
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : positions) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        double centerZ = (minZ + maxZ) / 2.0;
        BlockPos anchorPos = BlockPos.containing(centerX, centerY, centerZ);

        setAnchor(anchorPos, targetX, targetY);

        double spanX = maxX - minX + 1;
        double spanY = maxY - minY + 1;
        double spanZ = maxZ - minZ + 1;
        double maxSpan = Math.max(spanX, Math.max(spanY, spanZ));
        double distance = Math.max(6.0, maxSpan + 4.0);

        Vec3 center = new Vec3(centerX + 0.5, centerY + 0.5, centerZ + 0.5);
        Vec3 cameraPos = center.add(distance, distance, distance);
        Vec3 toCenter = center.subtract(cameraPos).normalize();
        float yaw = (float) (Math.toDegrees(Math.atan2(toCenter.z, toCenter.x)) + 90.0);
        float pitch = (float) -Math.toDegrees(Math.asin(toCenter.y));
        setCamera(cameraPos, yaw, pitch);
    }

    public void highlightBlock(BlockPos pos, int color, float expand) {
        ensureChunk(pos);
        this.selectionBoxes.clear();
        this.selectionBoxes.add(new SelectionBox(pos, color, expand));
    }

    public void clearHighlights() {
        this.selectionBoxes.clear();
    }

    public Optional<Vec2> projectToWidget(Vec3 worldPos) {
        return projectToWidget(worldPos, true);
    }

    public Optional<Vec2> projectToWidgetUnclamped(Vec3 worldPos) {
        return projectToWidget(worldPos, false);
    }

    private Optional<Vec2> projectToWidget(Vec3 worldPos, boolean clampToWidget) {
        if (this.lastModelView == null || this.lastProjection == null || this.lastFramebufferWidth <= 0 || this.lastFramebufferHeight <= 0)
            return Optional.empty();

        Vec2 framebufferPos = projectToScreen(
                worldPos,
                this.lastModelView,
                this.lastProjection,
                this.lastFramebufferWidth,
                this.lastFramebufferHeight
        );

        float guiX = this.lastWidgetX + framebufferPos.x / this.lastScaleFactor;
        float guiY = this.lastWidgetY + framebufferPos.y / this.lastScaleFactor;

        if (!clampToWidget)
            return Optional.of(new Vec2(guiX, guiY));

        boolean insideWidget = this.lastWidgetX <= guiX && guiX <= this.lastWidgetX + this.lastWidgetWidth
                && this.lastWidgetY <= guiY && guiY <= this.lastWidgetY + this.lastWidgetHeight;
        return insideWidget ? Optional.of(new Vec2(guiX, guiY)) : Optional.empty();
    }

    private void drawSelectionBoxes(RenderContext context) {
        if (this.selectionBoxes.isEmpty())
            return;

        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        for (SelectionBox selection : List.copyOf(this.selectionBoxes)) {
            matrices.pushPose();
            BlockPos pos = selection.pos();
            double expansion = selection.expand() + 0.0025;
            var box = new AABB(pos).inflate(expansion);

            int color = selection.color();

            Gizmos.cuboid(box, GizmoStyle.stroke(color));
            matrices.popPose();
        }
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

    public interface SceneElement {
        Vec3 position();

        default BlockPos blockPos() {
            return BlockPos.containing(position());
        }

        default ChunkPos chunkPos() {
            return new ChunkPos(blockPos());
        }
    }

    protected record PlacedBlock(BlockPos pos, BlockState state) implements SceneElement {
        @Override
        public Vec3 position() {
            return new Vec3(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        }

        @Override
        public BlockPos blockPos() {
            return this.pos;
        }
    }

    protected record PlacedFluid(BlockPos pos, FluidState state) implements SceneElement {
        @Override
        public Vec3 position() {
            return new Vec3(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        }

        @Override
        public BlockPos blockPos() {
            return this.pos;
        }
    }

    protected record PlacedVariedBlockList(BlockPos pos, VariedBlockList variedBlockList) implements SceneElement {
        @Override
        public Vec3 position() {
            return new Vec3(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        }

        @Override
        public BlockPos blockPos() {
            return this.pos;
        }
    }

    public record Nameplate(Vec3 position, Component text, float yOffset) implements SceneElement {
        public Nameplate {
            if (position == null) {
                position = Vec3.ZERO;
            }
        }
    }

    private record SelectionBox(BlockPos pos, int color, float expand) {
    }
}
