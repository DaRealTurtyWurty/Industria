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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

public abstract class IndustriaBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    protected static final List<ModelPart> EMPTY_WIREFRAME = Collections.emptyList();

    protected final BlockEntityRendererFactory.Context context;

    public IndustriaBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

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
    protected abstract void postRender(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);

    /**
     * Gets the model parts to render when the player is looking at the block entity (cache if possible).
     *
     * @return The model parts to render
     */
    protected List<ModelPart> getModelParts() {
        return EMPTY_WIREFRAME;
    }

    @Override
    public final void render(T entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        setupBlockEntityTransformations(matrices, entity);
        onRender(entity, tickDelta, matrices, vertexConsumers, light, overlay);

        if(/*isPlayerLookingAt(entity.getPos())*/ true) {
            List<ModelPart> wireframe = getModelParts();
            if(!wireframe.isEmpty()) {
                boolean isHighContrast = isHighContrast();
                renderFromModelParts(wireframe, matrices, vertexConsumers, isHighContrast);
            }
        }

        matrices.pop();

        postRender(entity, tickDelta, matrices, vertexConsumers, light, overlay);
    }

    private static boolean isHighContrast() {
        return MinecraftClient.getInstance().options.getHighContrastBlockOutline().getValue();
    }

    public static VertexConsumer getHighContrastWireframeVertexConsumer(VertexConsumerProvider vertexConsumers) {
        return vertexConsumers.getBuffer(RenderLayer.getSecondaryBlockOutline());
    }

    public static VertexConsumer getWireframeVertexConsumer(VertexConsumerProvider vertexConsumers) {
        return vertexConsumers.getBuffer(RenderLayer.getLines());
    }

    public static int getWireframeColor(boolean isHighContrast) {
        return isHighContrast ? Colors.CYAN : ColorHelper.withAlpha(102, Colors.BLACK);
    }

    protected static void renderFromModelParts(List<ModelPart> modelParts, MatrixStack matrices, VertexConsumerProvider vertexConsumers, boolean isHighContrast) {
        var v0 = new Vector3f();
        var v1 = new Vector3f();
        var v2 = new Vector3f();
        var v3 = new Vector3f();
        var pos = new Vector4f();
        var normal = new Vector3f();

        VertexConsumer normalConsumer = getWireframeVertexConsumer(vertexConsumers);
        VertexConsumer highContrastConsumer = getHighContrastWireframeVertexConsumer(vertexConsumers);
        int color = getWireframeColor(isHighContrast);

        for (ModelPart modelPart : modelParts) {
            visitPart(modelPart, matrices, isHighContrast, normalConsumer, highContrastConsumer, color, v0, v1, v2, v3, pos, normal);
        }
    }

    private static void visitPart(ModelPart modelPart, MatrixStack matrices, boolean isHighContrast, VertexConsumer normalConsumer, VertexConsumer highContrastConsumer, int color, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, Vector4f pos, Vector3f normal) {
        if(!modelPart.visible || (modelPart.isEmpty() && modelPart.children.isEmpty()))
            return;

        matrices.push();
        modelPart.rotate(matrices);

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

            for (int index = 0; index < (isHighContrast ? 2 : 1); index++) {
                VertexConsumer vertexConsumer = index == 0 ? (isHighContrast ? highContrastConsumer : normalConsumer) : normalConsumer;

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
        }

        for (ModelPart part : modelPart.children.values()) {
            visitPart(part, matrices, isHighContrast, normalConsumer, highContrastConsumer, color, v0, v1, v2, v3, pos, normal);
        }

        matrices.pop();
    }

    protected void setupBlockEntityTransformations(MatrixStack matrices, T entity) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

        BlockState state = entity.getCachedState();
        if(!state.getProperties().contains(Properties.HORIZONTAL_FACING))
            return;

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 + switch (state.get(Properties.HORIZONTAL_FACING)) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        }));
    }

    public static boolean isPlayerLookingAt(BlockPos bePos) {
        if(!(MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult hitResult))
            return false;

        if (hitResult.getType() == HitResult.Type.MISS)
            return false;

        BlockPos hitPos = hitResult.getBlockPos();
        if(Objects.equals(hitPos, bePos))
            return true;

        World world = MinecraftClient.getInstance().world;
        if(world == null)
            return false;

        BlockState state = world.getBlockState(hitPos);
        if(state.isAir() || !state.isOf(BlockInit.MULTIBLOCK_BLOCK) || !world.getWorldBorder().contains(hitPos))
            return false;

        BlockPos primaryPos = MultiblockBlock.getPrimaryPos(world, hitPos);
        return Objects.equals(primaryPos, bePos);
    }
}
