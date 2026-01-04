package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockBlock;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import dev.turtywurty.industria.util.WireframeExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

/**
 * A block entity renderer for Industria block entities.
 * <p>
 * This class provides utility methods for rendering wireframes and checking if the player is looking at the block entity.
 * It also provides a method for rendering the wireframe of the model parts.
 * <br>
 * This class also allows for the rendering process to be split up into multiple methods to allow for easier customization.
 * </p>
 *
 * @param <T> The block entity type
 * @see WireframeExtractor
 * @see IndustriaBlockEntityRenderer#onRender(IndustriaBlockEntityRenderState, PoseStack, SubmitNodeCollector, int, int)
 * @see IndustriaBlockEntityRenderer#postRender(IndustriaBlockEntityRenderState, PoseStack, SubmitNodeCollector, int, int)
 * @see IndustriaBlockEntityRenderer#setupBlockEntityTransformations(PoseStack, IndustriaBlockEntityRenderState)
 */
public abstract class IndustriaBlockEntityRenderer<T extends BlockEntity, S extends IndustriaBlockEntityRenderState> implements BlockEntityRenderer<T, S> {
    protected static final List<ModelPart> EMPTY_WIREFRAME = Collections.emptyList();

    protected final BlockEntityRendererProvider.Context context;

    /**
     * Creates a new block entity renderer.
     *
     * @param context The block entity renderer factory context
     */
    public IndustriaBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    /**
     * Called to render the block entity.
     *
     * @param state    The block entity render state
     * @param matrices The matrix stack
     * @param queue    The vertex consumer provider
     */
    protected abstract void onRender(S state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay);

    /**
     * Called after the block entity and wireframe have been rendered.
     *
     * @param state    The block entity render state
     * @param matrices The matrix stack
     * @param queue    The vertex consumer provider
     */
    protected void postRender(S state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
    }

    @Override
    public void submit(S state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        setupBlockEntityTransformations(matrices, state);
        onRender(state, matrices, queue, state.lightCoords, OverlayTexture.NO_OVERLAY);

        if (isPlayerLookingAt(state.blockPos)) {
            List<ModelPart> wireframe = getModelParts();
            if (!wireframe.isEmpty()) {
                boolean isHighContrast = isHighContrast();
                renderWireframe(wireframe, matrices, queue, isHighContrast);
            }
        }

        matrices.popPose();

        postRender(state, matrices, queue, state.lightCoords, OverlayTexture.NO_OVERLAY);
    }

    public ItemModelResolver getItemModelManager() {
        return this.context.itemModelResolver();
    }

    @SuppressWarnings("unchecked")
    @Override
    public S createRenderState() {
        return (S) new IndustriaBlockEntityRenderState(0);
    }

    @Override
    public void extractRenderState(T blockEntity, S state, float tickProgress, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.tickProgress = tickProgress;
    }

    public static boolean shouldRenderHitboxes() {
        return Minecraft.getInstance().debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES);
    }

    private static boolean isHighContrast() {
        return Minecraft.getInstance().options.highContrastBlockOutline().get();
    }

    /**
     * Gets the vertex consumer for the high contrast wireframe.
     *
     * @param vertexConsumers The vertex consumer provider
     * @return The vertex consumer for the high contrast wireframe
     */
    public static VertexConsumer getHighContrastWireframeVertexConsumer(MultiBufferSource vertexConsumers) {
        return vertexConsumers.getBuffer(RenderTypes.secondaryBlockOutline());
    }

    /**
     * Gets the vertex consumer for the wireframe.
     *
     * @param vertexConsumers The vertex consumer provider
     * @return The vertex consumer for the wireframe
     */
    public static VertexConsumer getWireframeVertexConsumer(MultiBufferSource vertexConsumers) {
        return vertexConsumers.getBuffer(RenderTypes.lines());
    }

    /**
     * Gets the color of the wireframe.
     *
     * @param isHighContrast If the wireframe should be high contrast
     * @return The color of the wireframe
     */
    public static int getWireframeColor(boolean isHighContrast) {
        return isHighContrast ? CommonColors.HIGH_CONTRAST_DIAMOND : ARGB.color(102, CommonColors.BLACK);
    }

    /**
     * Renders the wireframe of the model parts.
     *
     * @param modelParts     The model parts to render
     * @param matrices       The matrix stack
     * @param queue          The vertex consumer provider
     * @param isHighContrast If the wireframe should be high contrast
     */
    public static void renderWireframe(List<ModelPart> modelParts, PoseStack matrices, SubmitNodeCollector queue, boolean isHighContrast) {
        var v0 = new Vector3f();
        var v1 = new Vector3f();
        var v2 = new Vector3f();
        var v3 = new Vector3f();
        var pos = new Vector4f();
        var normal = new Vector3f();

        int color = getWireframeColor(isHighContrast);
        for (int iteration = 0; iteration < (isHighContrast ? 2 : 1); iteration++) {
            queue.submitCustomGeometry(matrices, isHighContrast ? RenderTypes.secondaryBlockOutline() : RenderTypes.lines(), (entry, vertexConsumer) -> {
                for (ModelPart modelPart : modelParts) {
                    visitPart(modelPart, matrices, vertexConsumer, color, v0, v1, v2, v3, pos, normal);
                }
            });
        }
    }

    /**
     * Visits a model part and renders its wireframe.
     *
     * @param modelPart      The model part to visit
     * @param matrices       The matrix stack
     * @param vertexConsumer The vertex consumer
     * @param color          The color of the wireframe
     * @param v0             The first vertex
     * @param v1             The second vertex
     * @param v2             The third vertex
     * @param v3             The fourth vertex
     * @param pos            The position of the vertex
     * @param normal         The normal of the vertex
     */
    private static void visitPart(ModelPart modelPart, PoseStack matrices, VertexConsumer vertexConsumer, int color, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, Vector4f pos, Vector3f normal) {
        if (!modelPart.visible || (modelPart.isEmpty() && modelPart.children.isEmpty()))
            return;

        matrices.pushPose();
        modelPart.translateAndRotate(matrices);

        {
            PoseStack.Pose entry = matrices.last();
            Matrix4f pose = entry.pose();
            Matrix3f poseNormal = entry.normal();
            Set<WireframeExtractor.Line> lines = new HashSet<>();
            for (ModelPart.Cube cuboid : modelPart.cubes) {
                for (ModelPart.Polygon quad : cuboid.polygons) {
                    ModelPart.Vertex[] vertices = quad.vertices();
                    v0.set(vertices[0].worldX(), vertices[0].worldY(), vertices[0].worldZ());
                    v1.set(vertices[1].worldX(), vertices[1].worldY(), vertices[1].worldZ());
                    v2.set(vertices[2].worldX(), vertices[2].worldY(), vertices[2].worldZ());
                    v3.set(vertices[3].worldX(), vertices[3].worldY(), vertices[3].worldZ());
                    lines.add(WireframeExtractor.Line.from(v0, v1));
                    lines.add(WireframeExtractor.Line.from(v1, v2));
                    lines.add(WireframeExtractor.Line.from(v2, v3));
                    lines.add(WireframeExtractor.Line.from(v3, v0));
                }
            }

            for (WireframeExtractor.Line line : lines) {
                poseNormal.transform(line.normalX(), line.normalY(), line.normalZ(), normal);

                pose.transform(line.x1(), line.y1(), line.z1(), 1F, pos);
                vertexConsumer.addVertex(pos.x(), pos.y(), pos.z())
                        .setColor(color)
                        .setNormal(normal.x, normal.y, normal.z);

                pose.transform(line.x2(), line.y2(), line.z2(), 1F, pos);
                vertexConsumer.addVertex(pos.x(), pos.y(), pos.z())
                        .setColor(color)
                        .setNormal(normal.x, normal.y, normal.z);
            }
        }

        for (ModelPart part : modelPart.children.values()) {
            visitPart(part, matrices, vertexConsumer, color, v0, v1, v2, v3, pos, normal);
        }

        matrices.popPose();
    }

    /**
     * Checks if the player is looking at the block entity.
     * <p>
     * This method uses the client crosshair target to determine if the player is looking at the block entity.
     * It achieves this by checking if the crosshair target is a block hit result and if the block position of the
     * hit result is equal to the block entity position. If the block position is not equal to the block entity
     * position, it will check if the block at the hit position is a multiblock block and if the primary position
     * of the multiblock is equal to the block entity position.
     * </p>
     *
     * @param bePos The block entity position
     * @return If the player is looking at the block entity or a multiblock block
     */
    public static boolean isPlayerLookingAt(BlockPos bePos) {
        if (!(Minecraft.getInstance().hitResult instanceof BlockHitResult hitResult))
            return false;

        if (hitResult.getType() == HitResult.Type.MISS)
            return false;

        BlockPos hitPos = hitResult.getBlockPos();
        if (Objects.equals(hitPos, bePos))
            return true;

        Level world = Minecraft.getInstance().level;
        if (world == null)
            return false;

        BlockState state = world.getBlockState(hitPos);
        if (state.isAir() || !state.is(BlockInit.AUTO_MULTIBLOCK_BLOCK) || !world.getWorldBorder().isWithinBounds(hitPos))
            return false;

        BlockPos primaryPos = AutoMultiblockBlock.getPrimaryPos(world, hitPos);
        return Objects.equals(primaryPos, bePos);
    }

    /**
     * Gets the model parts to render when the player is looking at the block entity (cache if possible).
     *
     * @return The model parts to render
     * @apiNote This method is experimental since currently it causes a crash if you use it
     */
    @ApiStatus.Experimental
    protected List<ModelPart> getModelParts() {
        return EMPTY_WIREFRAME;
    }

    public final void renderForItem(S state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        setupBlockEntityTransformations(matrices, state);
        onRender(state, matrices, queue, light, overlay);

        matrices.popPose();

        postRender(state, matrices, queue, light, overlay);
    }

    protected void setupBlockEntityTransformations(PoseStack matrices, S state) {
        matrices.pushPose();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.mulPose(Axis.XP.rotationDegrees(180));

        if (!state.blockState.getProperties().contains(BlockStateProperties.HORIZONTAL_FACING))
            return;

        Direction facing = state.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        matrices.mulPose(Axis.YP.rotationDegrees(180 + switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        }));
    }

    public static void drawFilledBox(
            PoseStack.Pose entry,
            VertexConsumer vertexConsumers,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        drawFilledBox(entry, vertexConsumers, (float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ, red, green, blue, alpha);
    }

    public static void drawFilledBox(
            PoseStack.Pose entry,
            VertexConsumer vertexConsumers,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        Matrix4f matrix4f = entry.pose();
        vertexConsumers.addVertex(matrix4f, minX, minY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, minX, minY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, minX, minY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, minX, minY, maxZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, minX, maxY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, minX, minY, maxZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, minY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, minY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, minX, maxY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, minX, minY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, minX, minY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, minY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, minX, minY, maxZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, minX, maxY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, minX, maxY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        vertexConsumers.addVertex(matrix4f, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
    }
}
