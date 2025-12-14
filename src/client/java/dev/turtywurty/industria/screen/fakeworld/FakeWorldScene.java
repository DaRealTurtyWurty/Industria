package dev.turtywurty.industria.screen.fakeworld;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.turtywurty.industria.util.BlockPredicateRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.fluid.FluidState;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.WorldChunk;
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
    protected final MinecraftClient client = MinecraftClient.getInstance();
    protected final ClientWorld world;
    protected final Entity cameraEntity;
    protected final BlockRenderManager blockRenderManager;
    protected final List<PlacedBlock> blocks;
    protected final List<PlacedFluid> fluids;
    protected final List<Entity> entities;
    protected final List<PredicatedBlock> predicates;
    protected final List<Nameplate> nameplates;
    protected final Camera camera = new Camera();
    protected final ProjectionMatrix2 projectionMatrix = new ProjectionMatrix2("FakeWorldScene", -1000.0F, 1000.0F, true);
    private final Consumer<FakeWorldSceneBuilder.SceneTickContext> tickHandler;
    private final Map<RenderStage, List<Consumer<RenderContext>>> beforeRenderCallbacks = new EnumMap<>(RenderStage.class);
    private final Map<RenderStage, List<Consumer<RenderContext>>> afterRenderCallbacks = new EnumMap<>(RenderStage.class);
    private final Map<SceneElement, TransformProvider> transforms = new HashMap<>();
    private Matrix4f lastModelView;
    private Matrix4f lastProjection;
    private int lastFramebufferWidth = -1;
    private int lastFramebufferHeight = -1;
    private int lastWidgetX = -1;
    private int lastWidgetY = -1;
    private int lastWidgetWidth = -1;
    private int lastWidgetHeight = -1;
    private float lastScaleFactor = 1.0F;
    protected SimpleFramebuffer framebuffer;
    private BlockPos anchorBlock;
    private int anchorTargetX;
    private int anchorTargetY;
    private Vec3d predicateOffset = Vec3d.ZERO;

    public FakeWorldScene(FakeWorldSceneBuilder.BuiltScene builtScene) {
        this(builtScene, builtScene.tickHandler());
    }

    public FakeWorldScene(FakeWorldSceneBuilder.BuiltScene builtScene, Consumer<FakeWorldSceneBuilder.SceneTickContext> tickHandler) {
        this.world = builtScene.result().world();
        this.cameraEntity = builtScene.result().player();
        this.blockRenderManager = this.client.getBlockRenderManager();
        this.blocks = new ArrayList<>(builtScene.blocks());
        this.fluids = new ArrayList<>(builtScene.fluids());
        this.entities = new ArrayList<>(builtScene.entities());
        this.predicates = new ArrayList<>(builtScene.predicates());
        this.nameplates = new ArrayList<>(builtScene.nameplates());
        this.tickHandler = tickHandler;

        this.cameraEntity.setPos(builtScene.cameraPos().x, builtScene.cameraPos().y, builtScene.cameraPos().z);
        this.cameraEntity.setYaw(builtScene.cameraYaw());
        this.cameraEntity.setPitch(builtScene.cameraPitch());
        this.cameraEntity.resetPosition();

        for (Entity entity : this.entities) {
            this.world.addEntity(entity);
            entity.resetPosition();
        }

        for (RenderStage stage : RenderStage.values()) {
            this.beforeRenderCallbacks.put(stage, new ArrayList<>());
            this.afterRenderCallbacks.put(stage, new ArrayList<>());
        }

        markUsedChunksForTicking();
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

    public void tick() {
        this.world.tick(() -> true);
        this.world.tickEntities();
        this.tickHandler.accept(new FakeWorldSceneBuilder.SceneTickContext(this.world, List.copyOf(this.entities)));
    }

    @Override
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
        this.lastModelView = new Matrix4f(matrices.peek().getPositionMatrix());
        this.lastProjection = new Matrix4f().setOrtho(0.0F, framebufferWidth, framebufferHeight, 0.0F, -1000.0F, 1000.0F);
        this.lastFramebufferWidth = framebufferWidth;
        this.lastFramebufferHeight = framebufferHeight;
        this.lastWidgetX = x;
        this.lastWidgetY = y;
        this.lastWidgetWidth = width;
        this.lastWidgetHeight = height;
        this.lastScaleFactor = (float) scale;

        VertexConsumerProvider.Immediate consumers = this.client.getBufferBuilders().getEntityVertexConsumers();
        RenderContext renderContext = new RenderContext(context, matrices, consumers, tickDelta, framebufferWidth, framebufferHeight, this.world);
        runCallbacks(this.beforeRenderCallbacks, RenderStage.SCENE, renderContext);

        runCallbacks(this.beforeRenderCallbacks, RenderStage.BLOCKS, renderContext);
        for (PlacedBlock placedBlock : List.copyOf(this.blocks)) {
            matrices.push();
            BlockPos pos = placedBlock.pos();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            applyTransform(matrices, placedBlock, renderContext.world(), tickDelta);
            this.blockRenderManager.renderBlockAsEntity(
                    placedBlock.state(),
                    matrices,
                    consumers,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE,
                    OverlayTexture.DEFAULT_UV,
                    this.world,
                    pos
            );
            matrices.pop();
        }

        runCallbacks(this.afterRenderCallbacks, RenderStage.BLOCKS, renderContext);

        runCallbacks(this.beforeRenderCallbacks, RenderStage.PREDICATES, renderContext);
        for (PredicatedBlock predicate : List.copyOf(this.predicates)) {
            matrices.push();
            BlockPos pos = predicate.pos();
            matrices.translate(pos.getX() + this.predicateOffset.x, pos.getY() + this.predicateOffset.y, pos.getZ() + this.predicateOffset.z);
            applyTransform(matrices, predicate, renderContext.world(), tickDelta);
            BlockPredicateRenderer.renderInWorld(
                    predicate.predicate(),
                    pos,
                    this.world,
                    matrices,
                    consumers,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE,
                    OverlayTexture.DEFAULT_UV,
                    tickDelta
            );
            matrices.pop();
        }

        runCallbacks(this.afterRenderCallbacks, RenderStage.PREDICATES, renderContext);

        runCallbacks(this.beforeRenderCallbacks, RenderStage.FLUIDS, renderContext);
        for (PlacedFluid placedFluid : List.copyOf(this.fluids)) {
            BlockPos pos = placedFluid.pos();
            // FluidRenderer writes vertices in chunk-local (0-15) coordinates, so add the
            // chunk origin back in to get world-space positions that match the other
            // geometry in this scene.
            RenderLayer fluidLayer = mapBlockLayer(RenderLayers.getFluidLayer(placedFluid.state()));
            VertexConsumer base = consumers.getBuffer(fluidLayer);
            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            applyTransform(matrices, placedFluid, renderContext.world(), tickDelta);

            VertexConsumer transformedConsumer = new TransformedVertexConsumer(
                    base,
                    matrices.peek()
            );
            this.blockRenderManager.renderFluid(BlockPos.ORIGIN, this.world, transformedConsumer, this.world.getBlockState(pos), placedFluid.state());
            matrices.pop();
        }

        runCallbacks(this.afterRenderCallbacks, RenderStage.FLUIDS, renderContext);

        BlockEntityRenderDispatcher blockEntityRenderDispatcher = this.client.getBlockEntityRenderDispatcher();
        blockEntityRenderDispatcher.configure(this.world, this.camera, null);
        runCallbacks(this.beforeRenderCallbacks, RenderStage.BLOCK_ENTITIES, renderContext);
        for (PlacedBlock placedBlock : List.copyOf(this.blocks)) {
            if (!placedBlock.state().hasBlockEntity())
                continue;

            BlockEntity blockEntity = this.world.getBlockEntity(placedBlock.pos());
            if (blockEntity == null)
                continue;

            matrices.push();
            BlockPos pos = placedBlock.pos();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            blockEntityRenderDispatcher.render(blockEntity, tickDelta, matrices, consumers);
            matrices.pop();
        }

        runCallbacks(this.afterRenderCallbacks, RenderStage.BLOCK_ENTITIES, renderContext);

        runCallbacks(this.beforeRenderCallbacks, RenderStage.ENTITIES, renderContext);
        for (Entity entity : List.copyOf(this.entities)) {
            matrices.push();
            Vec3d entityPos = entity.getLerpedPos(tickDelta);
            matrices.translate(entityPos.x, entityPos.y, entityPos.z);
            this.client.getEntityRenderDispatcher().render(
                    entity,
                    0,
                    0,
                    0,
                    1.0f,
                    matrices,
                    consumers,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE
            );
            matrices.pop();
        }

        runCallbacks(this.afterRenderCallbacks, RenderStage.ENTITIES, renderContext);

        runCallbacks(this.beforeRenderCallbacks, RenderStage.NAMEPLATES, renderContext);
        for (Nameplate nameplate : List.copyOf(this.nameplates)) {
            matrices.push();
            Vec3d pos = nameplate.position();
            matrices.translate(pos.x, pos.y + nameplate.yOffset(), pos.z);
            applyTransform(matrices, nameplate, renderContext.world(), tickDelta);
            renderBillboardedText(matrices, consumers, nameplate.text(), true, 0.025F);
            matrices.pop();
        }
        runCallbacks(this.afterRenderCallbacks, RenderStage.NAMEPLATES, renderContext);

        runCallbacks(this.beforeRenderCallbacks, RenderStage.CUSTOM, renderContext);
        runCallbacks(this.afterRenderCallbacks, RenderStage.CUSTOM, renderContext);
        consumers.draw();

        runCallbacks(this.afterRenderCallbacks, RenderStage.SCENE, renderContext);

        RenderSystem.restoreProjectionMatrix();
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;

        runCallbacks(this.beforeRenderCallbacks, RenderStage.FINALIZE, renderContext);
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
        runCallbacks(this.afterRenderCallbacks, RenderStage.FINALIZE, renderContext);
    }

    public void drawNameplate(RenderContext renderContext, Text text, Vec3d worldPos, float yOffset, boolean seeThrough, float scale) {
        MatrixStack matrices = renderContext.matrices();
        matrices.push();
        matrices.translate(worldPos.x, worldPos.y + yOffset, worldPos.z);
        renderBillboardedText(matrices, renderContext.consumers(), text, seeThrough, scale);
        matrices.pop();
    }

    public void drawNameplate(RenderContext renderContext, Text text, Vec3d worldPos) {
        drawNameplate(renderContext, text, worldPos, 0.0F, true, 0.025F);
    }

    public void drawNameplate(RenderContext renderContext, Text text, Vec3d worldPos, float yOffset) {
        drawNameplate(renderContext, text, worldPos, yOffset, true, 0.025F);
    }

    private void renderBillboardedText(MatrixStack matrices, VertexConsumerProvider consumers, Text text, boolean seeThrough, float scale) {
        matrices.push();
        matrices.multiply(this.client.getEntityRenderDispatcher().getRotation());
        matrices.scale(scale, -scale, scale);

        TextRenderer textRenderer = this.client.textRenderer;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float xOffset = -textRenderer.getWidth(text) / 2.0F;
        int background = (int) (this.client.options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24;
        TextRenderer.TextLayerType layer = seeThrough ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL;

        textRenderer.draw(
                text,
                xOffset,
                0.0F,
                -2130706433,
                false,
                matrix,
                consumers,
                layer,
                background,
                LightmapTextureManager.MAX_LIGHT_COORDINATE
        );

        if (seeThrough) {
            textRenderer.draw(
                    text,
                    xOffset,
                    0.0F,
                    -1,
                    false,
                    matrix,
                    consumers,
                    TextRenderer.TextLayerType.NORMAL,
                    0,
                    LightmapTextureManager.applyEmission(LightmapTextureManager.MAX_LIGHT_COORDINATE, 2)
            );
        }

        matrices.pop();
    }

    private <T extends SceneElement> void applyTransform(MatrixStack matrices, T element, ClientWorld world, float tickDelta) {
        TransformProvider provider = this.transforms.get(element);
        if (provider == null)
            return;

        Transform transform = provider.get(tickDelta, world);
        if (transform == null || transform.isIdentity())
            return;

        transform.apply(matrices);
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
        updateChunkCenter(BlockPos.ofFloored(this.cameraEntity.getPos()));
    }

    public void setCameraRotation(float yaw, float pitch) {
        this.cameraEntity.setYaw(yaw);
        this.cameraEntity.setPitch(MathHelper.clamp(pitch, -89.0F, 89.0F));
    }

    public void setCamera(Vec3d position, float yaw, float pitch) {
        setCameraPosition(position);
        setCameraRotation(yaw, pitch);
    }

    public void moveCamera(Vec3d delta) {
        setCameraPosition(this.cameraEntity.getPos().add(delta));
    }

    public Vec3d getCameraPosition() {
        return this.cameraEntity.getPos();
    }

    public void setCameraPosition(Vec3d position) {
        this.cameraEntity.setPos(position.x, position.y, position.z);
        this.cameraEntity.resetPosition();
        updateChunkCenter(BlockPos.ofFloored(position));
    }

    public void addBlock(BlockPos pos, BlockState state) {
        ensureChunk(pos);
        replaceBlock(pos, state);
        removeElementsAtPos(this.fluids, pos);
        this.world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
        if (state.hasBlockEntity() && state.getBlock() instanceof BlockEntityProvider provider) {
            BlockEntity blockEntity = provider.createBlockEntity(pos, state);
            if (blockEntity != null) {
                this.world.addBlockEntity(blockEntity);
            }
        } else {
            this.world.removeBlockEntity(pos);
        }

        this.world.resetChunkColor(new ChunkPos(pos));
    }

    public void removeBlock(BlockPos pos) {
        ensureChunk(pos);
        this.world.removeBlockEntity(pos);
        this.world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
        removeElementsAtPos(this.blocks, pos);
        removeElementsAtPos(this.fluids, pos);
        this.world.resetChunkColor(new ChunkPos(pos));
    }

    public void addPredicate(BlockPos pos, BlockPredicate predicate) {
        ensureChunk(pos);
        replacePredicate(pos, predicate);
    }

    public void removePredicate(BlockPos pos) {
        ensureChunk(pos);
        removeElementsAtPos(this.predicates, pos);
    }

    public void clearPredicates() {
        this.predicates.forEach(this::clearTransform);
        this.predicates.clear();
    }

    public void setPredicateOffset(Vec3d offset) {
        this.predicateOffset = offset;
    }

    public void addFluid(BlockPos pos, FluidState state) {
        ensureChunk(pos);
        replaceFluid(pos, state);
        removeElementsAtPos(this.blocks, pos);
        this.world.setBlockState(pos, state.getBlockState(), Block.NOTIFY_LISTENERS);
        this.world.resetChunkColor(new ChunkPos(pos));
    }

    public void removeFluid(BlockPos pos) {
        ensureChunk(pos);
        removeElementsAtPos(this.fluids, pos);
        this.world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
        this.world.resetChunkColor(new ChunkPos(pos));
    }

    public void addEntity(Entity entity) {
        this.entities.add(entity);
        this.world.addEntity(entity);
        entity.resetPosition();
    }

    public boolean removeEntity(Entity entity) {
        this.world.removeEntity(entity.getId(), RemovalReason.DISCARDED);
        return this.entities.remove(entity);
    }

    public Nameplate addNameplate(Vec3d position, Text text, float yOffset) {
        Nameplate nameplate = new Nameplate(position, text, yOffset);
        this.nameplates.add(nameplate);
        return nameplate;
    }

    public Nameplate addNameplate(Vec3d position, Text text) {
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
        addChunksFor(chunks, this.predicates);
        addChunksFor(chunks, this.nameplates);

        for (Entity entity : this.entities) {
            chunks.add(new ChunkPos(BlockPos.ofFloored(entity.getPos())));
        }

        chunks.add(new ChunkPos(BlockPos.ofFloored(this.cameraEntity.getPos())));

        for (ChunkPos chunkPos : chunks) {
            this.world.resetChunkColor(chunkPos);
        }
    }

    private WorldChunk ensureChunk(BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        var chunkManager = this.world.getChunkManager();
        chunkManager.setChunkMapCenter(chunkX, chunkZ);

        var chunkMap = chunkManager.chunks;
        int index = chunkMap.getIndex(chunkX, chunkZ);
        WorldChunk chunk = chunkMap.getChunk(index);
        if (chunk == null || chunk.getPos().x != chunkX || chunk.getPos().z != chunkZ) {
            chunk = new WorldChunk(this.world, new ChunkPos(chunkX, chunkZ));
            chunkMap.set(index, chunk);
            this.world.resetChunkColor(new ChunkPos(chunkX, chunkZ));
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

    private void replacePredicate(BlockPos pos, BlockPredicate predicate) {
        for (int i = 0; i < this.predicates.size(); i++) {
            if (this.predicates.get(i).pos().equals(pos)) {
                PredicatedBlock newPredicate = new PredicatedBlock(pos, predicate);
                transferTransform(this.predicates.get(i), newPredicate);
                this.predicates.set(i, newPredicate);
                return;
            }
        }

        this.predicates.add(new PredicatedBlock(pos, predicate));
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
        this.world.getChunkManager().setChunkMapCenter(focus.getX() >> 4, focus.getZ() >> 4);
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
        BlockPos anchorPos = BlockPos.ofFloored(centerX, centerY, centerZ);

        setAnchor(anchorPos, targetX, targetY);

        double spanX = maxX - minX + 1;
        double spanY = maxY - minY + 1;
        double spanZ = maxZ - minZ + 1;
        double maxSpan = Math.max(spanX, Math.max(spanY, spanZ));
        double distance = Math.max(6.0, maxSpan + 4.0);

        Vec3d center = new Vec3d(centerX + 0.5, centerY + 0.5, centerZ + 0.5);
        Vec3d cameraPos = center.add(distance, distance, distance);
        Vec3d toCenter = center.subtract(cameraPos).normalize();
        float yaw = (float) (Math.toDegrees(Math.atan2(toCenter.z, toCenter.x)) + 90.0);
        float pitch = (float) -Math.toDegrees(Math.asin(toCenter.y));
        setCamera(cameraPos, yaw, pitch);
    }

    public Optional<Vec2f> projectToWidget(Vec3d worldPos) {
        if (this.lastModelView == null || this.lastProjection == null || this.lastFramebufferWidth <= 0 || this.lastFramebufferHeight <= 0)
            return Optional.empty();

        Vec2f framebufferPos = projectToScreen(
                worldPos,
                this.lastModelView,
                this.lastProjection,
                this.lastFramebufferWidth,
                this.lastFramebufferHeight
        );

        float guiX = this.lastWidgetX + framebufferPos.x / this.lastScaleFactor;
        float guiY = this.lastWidgetY + framebufferPos.y / this.lastScaleFactor;

        boolean insideWidget = this.lastWidgetX <= guiX && guiX <= this.lastWidgetX + this.lastWidgetWidth
                && this.lastWidgetY <= guiY && guiY <= this.lastWidgetY + this.lastWidgetHeight;
        return insideWidget ? Optional.of(new Vec2f(guiX, guiY)) : Optional.empty();
    }

    public interface SceneElement {
        Vec3d position();

        default BlockPos blockPos() {
            return BlockPos.ofFloored(position());
        }

        default ChunkPos chunkPos() {
            return new ChunkPos(blockPos());
        }
    }

    protected record PlacedBlock(BlockPos pos, BlockState state) implements SceneElement {
        @Override
        public Vec3d position() {
            return new Vec3d(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        }

        @Override
        public BlockPos blockPos() {
            return this.pos;
        }
    }

    protected record PlacedFluid(BlockPos pos, FluidState state) implements SceneElement {
        @Override
        public Vec3d position() {
            return new Vec3d(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        }

        @Override
        public BlockPos blockPos() {
            return this.pos;
        }
    }

    protected record PredicatedBlock(BlockPos pos, BlockPredicate predicate) implements SceneElement {
        @Override
        public Vec3d position() {
            return new Vec3d(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        }

        @Override
        public BlockPos blockPos() {
            return this.pos;
        }
    }

    public record Nameplate(Vec3d position, Text text, float yOffset) implements SceneElement {
        public Nameplate {
            if (position == null) {
                position = Vec3d.ZERO;
            }
        }
    }
}
