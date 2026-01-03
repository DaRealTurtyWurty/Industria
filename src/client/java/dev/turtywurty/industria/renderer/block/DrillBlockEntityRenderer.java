package dev.turtywurty.industria.renderer.block;

import com.mojang.datafixers.util.Either;
import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.model.DrillCableModel;
import dev.turtywurty.industria.model.DrillFrameModel;
import dev.turtywurty.industria.model.DrillMotorModel;
import dev.turtywurty.industria.registry.DrillHeadRegistry;
import dev.turtywurty.industria.state.DrillRenderState;
import dev.turtywurty.industria.util.DrillHeadable;
import dev.turtywurty.industria.util.DrillRenderData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrillBlockEntityRenderer extends IndustriaBlockEntityRenderer<DrillBlockEntity, DrillRenderState> {
    private final Map<DrillHeadable, Model<?>> drillHeadModels = new HashMap<>();
    private final Map<DrillHeadable, Identifier> drillHeadTextures = new HashMap<>();

    private final DrillFrameModel model;
    private final DrillMotorModel motorModel;
    private final DrillCableModel cableModel;

    public DrillBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new DrillFrameModel(context.getLayerModelPart(DrillFrameModel.LAYER_LOCATION));
        this.motorModel = new DrillMotorModel(context.getLayerModelPart(DrillMotorModel.LAYER_LOCATION));
        this.cableModel = new DrillCableModel(context.getLayerModelPart(DrillCableModel.LAYER_LOCATION));
    }

    @Override
    public DrillRenderState createRenderState() {
        return new DrillRenderState();
    }

    @Override
    public void updateRenderState(DrillBlockEntity blockEntity, DrillRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.motorInventory = blockEntity.getMotorInventory();
        state.drillHeadItemStack = blockEntity.getDrillHeadInventory().getStack(0);
        state.isDrilling = blockEntity.isDrilling();
        state.isRetracting = blockEntity.isRetracting();
        state.drillYOffset = blockEntity.getDrillYOffset();
        state.drillHeadAABB = blockEntity.getDrillHeadAABB();
        state.isPaused = blockEntity.isPaused();
        state.clientMotorRotation = blockEntity.clientMotorRotation;

        DrillRenderData renderData = blockEntity.getRenderData();
        if (renderData == null)
            return;

        state.clockwiseRotation = renderData.clockwiseRotation;
        state.counterClockwiseRotation = renderData.counterClockwiseRotation;
    }

    @Override
    public void onRender(DrillRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        World world = MinecraftClient.getInstance().world;

        { // Render motor
            if (!state.motorInventory.isEmpty()) {
                if (!state.isDrilling && !state.isRetracting) {
                    state.clientMotorRotation = 0;
                }

                state.clientMotorRotation += (state.isDrilling ? 0.03f : state.isRetracting ? -0.03f : 0);

                queue.submitModel(this.motorModel,
                        new DrillMotorModel.DrillMotorModelRenderState(state.clientMotorRotation),
                        matrices, this.motorModel.getLayer(DrillMotorModel.TEXTURE_LOCATION),
                        light, overlay, 0, state.crumblingOverlay);
            }
        }

        { // Render frame
            queue.submitModel(this.model,
                    new DrillFrameModel.DrillFrameModelRenderState(state.clientMotorRotation),
                    matrices, this.model.getLayer(DrillFrameModel.TEXTURE_LOCATION),
                    light, overlay, 0, state.crumblingOverlay);
        }

        int worldBottom = world == null ? 0 : world.getBottomY();
        int startY = state.pos.getY() + 2;
        float currentY = state.drillYOffset - 1 + startY;

        float progress = 1 - (startY - currentY) / (startY - worldBottom);

        { // Render cable wheel
            state.cableScaleFactor = 0.5f - (progress / 2f);
            queue.submitModel(this.cableModel,
                    new DrillCableModel.DrillCableModelRenderState(state.clientMotorRotation, state.cableScaleFactor),
                    matrices, this.cableModel.getLayer(DrillCableModel.TEXTURE_LOCATION),
                    light, overlay, 0, state.crumblingOverlay);
        }

        if (state.drillHeadItemStack.isEmpty() || !(state.drillHeadItemStack.getItem() instanceof DrillHeadable drillHeadable))
            return;

        DrillHeadRegistry.DrillHeadClientData drillHeadData = DrillHeadRegistry.getClientData(drillHeadable);
        if (drillHeadData == null)
            return;

        { // Render drill cable
            RenderLayer renderLayer = RenderLayers.lines();
            matrices.push();

            queue.submitCustom(matrices, renderLayer, (entry, vertexConsumer) -> {
                float angleOffset = (float) (state.isRetracting ?
                        -state.clientMotorRotation < 0 ? -Math.PI / 4 : -Math.PI / 4 - Math.PI / 2 :
                        -state.clientMotorRotation < 0 ? -3 * Math.PI / 4 + Math.PI / 2 : -3 * Math.PI / 4);

                float angle = (float) (-state.clientMotorRotation % (Math.PI / 2f)) + angleOffset;

                float wheelRadius = (progress / 2f + 0.5f) * 3.5f / 16f;

                float wheelZ = 0.5f;
                float wheelY = -1.5f + 1.5f / 16f;

                double r = Math.sqrt(2) * wheelRadius;
                float cableZ = wheelZ + (float) (Math.cos(angle) * r);
                float cableY = wheelY + (float) (Math.sin(angle) * r);

                vertexConsumer.vertex(entry, 0f, cableY, cableZ)
                        .color(70, 70, 70, 255)
                        .normal(1, 0, 0);

                vertexConsumer.vertex(entry, 0, -1.54f, 0)
                        .color(70, 70, 70, 255)
                        .normal(1, 0, 0);

                vertexConsumer.vertex(entry, 0, -1.54f, 0)
                        .color(70, 70, 70, 255)
                        .normal(0, 1, 0);

                matrices.translate(0, -state.drillYOffset, 0);
                vertexConsumer.vertex(matrices.peek(), 0, 0.5f, 0)
                        .color(70, 70, 70, 255)
                        .normal(0, 1, 0);
            });
        }

        { // Render drill head
            Model<?> drillHeadModel = this.drillHeadModels.computeIfAbsent(drillHeadable, ignored -> drillHeadData.modelResolver().apply(Either.left(this.context)));
            Identifier drillHeadTexture = this.drillHeadTextures.computeIfAbsent(drillHeadable, ignored -> drillHeadData.textureLocation());

            drillHeadData.onRender().render(state, matrices, queue, drillHeadModel, drillHeadModel.getLayer(drillHeadTexture), light, overlay);
            matrices.pop();
        }
    }

    @Override
    protected void postRender(DrillRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        if (shouldRenderHitboxes() && state.drillHeadAABB != null) {
            double minX = state.drillHeadAABB.minX - state.pos.getX();
            double minY = state.drillHeadAABB.minY - state.pos.getY();
            double minZ = state.drillHeadAABB.minZ - state.pos.getZ();
            double maxX = state.drillHeadAABB.maxX - state.pos.getX();
            double maxY = state.drillHeadAABB.maxY - state.pos.getY();
            double maxZ = state.drillHeadAABB.maxZ - state.pos.getZ();

            GizmoDrawing.box(new Box(minX, minY, minZ, maxX, maxY, maxZ), DrawStyle.stroked(0xFF0000FF));
        }
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        // this has turned into a one time check
        // TODO: Look for a way to have this back to how it was.
        // return blockEntity.isDrilling() || blockEntity.isRetracting() && blockEntity.getDrillYOffset() < -1F;
        return true;
    }

    @Override
    public boolean isInRenderDistance(DrillBlockEntity blockEntity, Vec3d pos) {
        return blockEntity.getPos().isWithinDistance(pos, blockEntity.getWorld() == null ? 64 : blockEntity.getWorld().getHeight());
    }

    @Override
    protected List<ModelPart> getModelParts() {
        return List.of(this.model.getRootPart());
    }
}
