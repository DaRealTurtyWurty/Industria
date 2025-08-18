package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.block.MultiblockBlock;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.util.WireframeExtractor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Colors;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
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
 * @see IndustriaBlockEntityRenderer#onRender(BlockEntity, float, MatrixStack, VertexConsumerProvider, int, int)
 * @see IndustriaBlockEntityRenderer#postRender(BlockEntity, float, MatrixStack, VertexConsumerProvider, int, int)
 * @see IndustriaBlockEntityRenderer#setupBlockEntityTransformations(MatrixStack, BlockEntity)
 */
public abstract class IndustriaBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
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


    @Override
    public final void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
        matrices.push();
        setupBlockEntityTransformations(matrices, entity);

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        renderModel(entity, tickDelta, matrices, vertexConsumers, light, overlay);
        matrices.pop();

        onRender(entity, tickDelta, matrices, vertexConsumers, light, overlay);

        if (isPlayerLookingAt(entity.getPos())) {
            List<ModelPart> wireframe = getModelParts();
            if (!wireframe.isEmpty()) {
                boolean isHighContrast = isHighContrast();
                renderWireframe(wireframe, matrices, vertexConsumers, isHighContrast);
            }
        }

        matrices.pop();
        postRender(entity, tickDelta, matrices, vertexConsumers, light, overlay);
    }

    protected abstract void renderModel(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);

    /**
     * Called to render the block entity.
     *
     * @param entity          The block entity
     * @param tickDelta       The partial tick
     * @param matrices        The matrix stack
     * @param vertexConsumers The vertex consumer provider
     * @param light           The light level
     * @param overlay         The overlay
     */
    protected abstract void onRender(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);

    /**
     * Called after the block entity and wireframe have been rendered.
     *
     * @param entity          The block entity
     * @param tickDelta       The partial tick
     * @param matrices        The matrix stack
     * @param vertexConsumers The vertex consumer provider
     * @param light           The light level
     * @param overlay         The overlay
     */
    protected void postRender(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {}

    /**
     * Converts a MatrixStack to a world Position
     *
     * @param matrices The current MatrixStack
     * @return The world Position stored in a {@link Vector3f}
     */
    protected Vector3f matrixStackToWorldPosition(MatrixStack matrices) {
        Vector3f pos = matrices.peek().getPositionMatrix().transformPosition(0, 0, 0, new Vector3f());
        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

        return new Vector3f((float) (pos.x() + cameraPos.x), (float) (pos.y() + cameraPos.y), (float) (pos.z() + cameraPos.z));
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
     * @param modelParts      The model parts to render
     * @param matrices        The matrix stack
     * @param vertexConsumers The vertex consumer provider
     * @param isHighContrast  If the wireframe should be high contrast
     */
    public static void renderWireframe(List<ModelPart> modelParts, MatrixStack matrices, VertexConsumerProvider vertexConsumers, boolean isHighContrast) {
        var v0 = new Vector3f();
        var v1 = new Vector3f();
        var v2 = new Vector3f();
        var v3 = new Vector3f();
        var pos = new Vector4f();
        var normal = new Vector3f();

        int color = getWireframeColor(isHighContrast);
        for (int iteration = 0; iteration < (isHighContrast ? 2 : 1); iteration++) {
            VertexConsumer vertexConsumer = iteration == 0 && isHighContrast ? getHighContrastWireframeVertexConsumer(vertexConsumers) : getWireframeVertexConsumer(vertexConsumers);

            for (ModelPart modelPart : modelParts) {
                visitPart(modelPart, matrices, vertexConsumer, color, v0, v1, v2, v3, pos, normal);
            }
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
                    quad.vertices()[0].pos().div(16, v0);
                    quad.vertices()[1].pos().div(16, v1);
                    quad.vertices()[2].pos().div(16, v2);
                    quad.vertices()[3].pos().div(16, v3);
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
        if (state.isAir() || !state.isOf(BlockInit.MULTIBLOCK_BLOCK) || !world.getWorldBorder().contains(hitPos))
            return false;

        BlockPos primaryPos = MultiblockBlock.getPrimaryPos(world, hitPos);
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

    public final void renderForItem(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        setupBlockEntityTransformations(matrices, entity);
        onRender(entity, tickDelta, matrices, vertexConsumers, light, overlay);

        matrices.pop();

        postRender(entity, tickDelta, matrices, vertexConsumers, light, overlay);
    }

    protected void setupBlockEntityTransformations(MatrixStack matrices, T entity) {
        matrices.translate(0.5f, 1.5f, 0.5f);

        BlockState state = entity.getCachedState();
        if (!state.getProperties().contains(Properties.HORIZONTAL_FACING))
            return;

        Direction facing = state.get(Properties.HORIZONTAL_FACING);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 + switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        }));
    }
}
