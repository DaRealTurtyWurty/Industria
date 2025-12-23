package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockBlock;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import dev.turtywurty.industria.util.WireframeExtractor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Colors;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
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
 * @see IndustriaBlockEntityRenderer#onRender(IndustriaBlockEntityRenderState, MatrixStack, OrderedRenderCommandQueue, int, int)
 * @see IndustriaBlockEntityRenderer#postRender(IndustriaBlockEntityRenderState, MatrixStack, OrderedRenderCommandQueue, int, int)
 * @see IndustriaBlockEntityRenderer#setupBlockEntityTransformations(MatrixStack, IndustriaBlockEntityRenderState)
 */
public abstract class IndustriaBlockEntityRenderer<T extends BlockEntity, S extends IndustriaBlockEntityRenderState> implements BlockEntityRenderer<T, S> {
    protected static final List<ModelPart> EMPTY_WIREFRAME = Collections.emptyList();

    protected final BlockEntityRendererFactory.Context context;

    /**
     * Creates a new block entity renderer.
     *
     * @param context The block entity renderer factory context
     */
    public IndustriaBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    /**
     * Called to render the block entity.
     *
     * @param state    The block entity render state
     * @param matrices The matrix stack
     * @param queue    The vertex consumer provider
     */
    protected abstract void onRender(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay);

    /**
     * Called after the block entity and wireframe have been rendered.
     *
     * @param state    The block entity render state
     * @param matrices The matrix stack
     * @param queue    The vertex consumer provider
     */
    protected void postRender(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
    }

    @Override
    public void render(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        setupBlockEntityTransformations(matrices, state);
        onRender(state, matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV);

        if (isPlayerLookingAt(state.pos)) {
            List<ModelPart> wireframe = getModelParts();
            if (!wireframe.isEmpty()) {
                boolean isHighContrast = isHighContrast();
                renderWireframe(wireframe, matrices, queue, isHighContrast);
            }
        }

        matrices.pop();

        postRender(state, matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV);
    }

    public ItemModelManager getItemModelManager() {
        return this.context.itemModelManager();
    }

    @SuppressWarnings("unchecked")
    @Override
    public S createRenderState() {
        return (S) new IndustriaBlockEntityRenderState(0);
    }

    @Override
    public void updateRenderState(T blockEntity, S state, float tickProgress, Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.tickProgress = tickProgress;
    }

    public static boolean shouldRenderHitboxes() {
        return MinecraftClient.getInstance().debugHudEntryList.isEntryVisible(DebugHudEntries.ENTITY_HITBOXES);
    }

    private static boolean isHighContrast() {
        return MinecraftClient.getInstance().options.getHighContrastBlockOutline().getValue();
    }

    /**
     * Gets the vertex consumer for the high contrast wireframe.
     *
     * @param vertexConsumers The vertex consumer provider
     * @return The vertex consumer for the high contrast wireframe
     */
    public static VertexConsumer getHighContrastWireframeVertexConsumer(VertexConsumerProvider vertexConsumers) {
        return vertexConsumers.getBuffer(RenderLayer.getSecondaryBlockOutline());
    }

    /**
     * Gets the vertex consumer for the wireframe.
     *
     * @param vertexConsumers The vertex consumer provider
     * @return The vertex consumer for the wireframe
     */
    public static VertexConsumer getWireframeVertexConsumer(VertexConsumerProvider vertexConsumers) {
        return vertexConsumers.getBuffer(RenderLayer.getLines());
    }

    /**
     * Gets the color of the wireframe.
     *
     * @param isHighContrast If the wireframe should be high contrast
     * @return The color of the wireframe
     */
    public static int getWireframeColor(boolean isHighContrast) {
        return isHighContrast ? Colors.CYAN : ColorHelper.withAlpha(102, Colors.BLACK);
    }

    /**
     * Renders the wireframe of the model parts.
     *
     * @param modelParts     The model parts to render
     * @param matrices       The matrix stack
     * @param queue          The vertex consumer provider
     * @param isHighContrast If the wireframe should be high contrast
     */
    public static void renderWireframe(List<ModelPart> modelParts, MatrixStack matrices, OrderedRenderCommandQueue queue, boolean isHighContrast) {
        var v0 = new Vector3f();
        var v1 = new Vector3f();
        var v2 = new Vector3f();
        var v3 = new Vector3f();
        var pos = new Vector4f();
        var normal = new Vector3f();

        int color = getWireframeColor(isHighContrast);
        for (int iteration = 0; iteration < (isHighContrast ? 2 : 1); iteration++) {
            queue.submitCustom(matrices, isHighContrast ? RenderLayer.getSecondaryBlockOutline() : RenderLayer.getLines(), (entry, vertexConsumer) -> {
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
    private static void visitPart(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertexConsumer, int color, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, Vector4f pos, Vector3f normal) {
        if (!modelPart.visible || (modelPart.isEmpty() && modelPart.children.isEmpty()))
            return;

        matrices.push();
        modelPart.applyTransform(matrices);

        {
            MatrixStack.Entry entry = matrices.peek();
            Matrix4f pose = entry.getPositionMatrix();
            Matrix3f poseNormal = entry.getNormalMatrix();
            Set<WireframeExtractor.Line> lines = new HashSet<>();
            for (ModelPart.Cuboid cuboid : modelPart.cuboids) {
                for (ModelPart.Quad quad : cuboid.sides) {
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
                vertexConsumer.vertex(pos.x(), pos.y(), pos.z())
                        .color(color)
                        .normal(normal.x, normal.y, normal.z);

                pose.transform(line.x2(), line.y2(), line.z2(), 1F, pos);
                vertexConsumer.vertex(pos.x(), pos.y(), pos.z())
                        .color(color)
                        .normal(normal.x, normal.y, normal.z);
            }
        }

        for (ModelPart part : modelPart.children.values()) {
            visitPart(part, matrices, vertexConsumer, color, v0, v1, v2, v3, pos, normal);
        }

        matrices.pop();
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
        if (!(MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult hitResult))
            return false;

        if (hitResult.getType() == HitResult.Type.MISS)
            return false;

        BlockPos hitPos = hitResult.getBlockPos();
        if (Objects.equals(hitPos, bePos))
            return true;

        World world = MinecraftClient.getInstance().world;
        if (world == null)
            return false;

        BlockState state = world.getBlockState(hitPos);
        if (state.isAir() || !state.isOf(BlockInit.AUTO_MULTIBLOCK_BLOCK) || !world.getWorldBorder().contains(hitPos))
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

    public final void renderForItem(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        setupBlockEntityTransformations(matrices, state);
        onRender(state, matrices, queue, light, overlay);

        matrices.pop();

        postRender(state, matrices, queue, light, overlay);
    }

    protected void setupBlockEntityTransformations(MatrixStack matrices, S state) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

        if (!state.blockState.getProperties().contains(Properties.HORIZONTAL_FACING))
            return;

        Direction facing = state.blockState.get(Properties.HORIZONTAL_FACING);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 + switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        }));
    }
}
