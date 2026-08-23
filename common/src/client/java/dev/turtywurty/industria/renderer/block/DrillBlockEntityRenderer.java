package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.model.DrillCableModel;
import dev.turtywurty.industria.model.DrillFrameModel;
import dev.turtywurty.industria.model.DrillMotorModel;
import dev.turtywurty.industria.registry.DrillHeadRegistry;
import dev.turtywurty.industria.state.DrillRenderState;
import dev.turtywurty.industria.util.DrillHeadable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

    public DrillBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new DrillFrameModel(context.bakeLayer(DrillFrameModel.LAYER_LOCATION));
        this.motorModel = new DrillMotorModel(context.bakeLayer(DrillMotorModel.LAYER_LOCATION));
        this.cableModel = new DrillCableModel(context.bakeLayer(DrillCableModel.LAYER_LOCATION));
    }

    @Override
    public DrillRenderState createRenderState() {
        return new DrillRenderState();
    }

    @Override
    public void extractRenderState(DrillBlockEntity blockEntity, DrillRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.motorInventory = blockEntity.getMotorInventory();
        state.drillHeadItemStack = blockEntity.getDrillHeadInventory().getItem(0);
        state.isDrilling = blockEntity.isDrilling();
        state.isRetracting = blockEntity.isRetracting();
        state.drillYOffset = blockEntity.getDrillYOffset();
        state.drillHeadAABB = blockEntity.getDrillHeadAABB();
        state.isPaused = blockEntity.isPaused();
        state.clientMotorRotation = blockEntity.clientMotorRotation;

        state.clockwiseRotation = blockEntity.clockwiseRotation;
        state.counterClockwiseRotation = blockEntity.counterClockwiseRotation;
    }

    @Override
    public void onRender(DrillRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        Level world = Minecraft.getInstance().level;

        { // Render motor
            if (!state.motorInventory.isEmpty()) {
                if (!state.isDrilling && !state.isRetracting) {
                    state.clientMotorRotation = 0;
                }

                state.clientMotorRotation += (state.isDrilling ? 0.03f : state.isRetracting ? -0.03f : 0);

                queue.submitModel(this.motorModel,
                        new DrillMotorModel.DrillMotorModelRenderState(state.clientMotorRotation),
                        matrices, this.motorModel.renderType(DrillMotorModel.TEXTURE_LOCATION),
                        light, overlay, 0, state.breakProgress);
            }
        }

        { // Render frame
            queue.submitModel(this.model,
                    new DrillFrameModel.DrillFrameModelRenderState(state.clientMotorRotation),
                    matrices, this.model.renderType(DrillFrameModel.TEXTURE_LOCATION),
                    light, overlay, 0, state.breakProgress);
        }

        int worldBottom = world == null ? 0 : world.getMinY();
        int startY = state.blockPos.getY() + 2;
        float currentY = state.drillYOffset - 1 + startY;

        float progress = 1 - (startY - currentY) / (startY - worldBottom);

        { // Render cable wheel
            state.cableScaleFactor = 0.5f - (progress / 2f);
            queue.submitModel(this.cableModel,
                    new DrillCableModel.DrillCableModelRenderState(state.clientMotorRotation, state.cableScaleFactor),
                    matrices, this.cableModel.renderType(DrillCableModel.TEXTURE_LOCATION),
                    light, overlay, 0, state.breakProgress);
        }

        if (state.drillHeadItemStack.isEmpty() || !(state.drillHeadItemStack.getItem() instanceof DrillHeadable drillHeadable))
            return;

        DrillHeadRegistry.DrillHeadClientData drillHeadData = DrillHeadRegistry.getClientData(drillHeadable);
        if (drillHeadData == null)
            return;

        { // Render drill cable
            RenderType renderLayer = RenderTypes.lines();
            matrices.pushPose();

            queue.submitCustomGeometry(matrices, renderLayer, (entry, vertexConsumer) -> {
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

                vertexConsumer.addVertex(entry, 0f, cableY, cableZ)
                        .setColor(70, 70, 70, 255)
                        .setNormal(1, 0, 0);

                vertexConsumer.addVertex(entry, 0, -1.54f, 0)
                        .setColor(70, 70, 70, 255)
                        .setNormal(1, 0, 0);

                vertexConsumer.addVertex(entry, 0, -1.54f, 0)
                        .setColor(70, 70, 70, 255)
                        .setNormal(0, 1, 0);

                matrices.translate(0, -state.drillYOffset, 0);
                vertexConsumer.addVertex(matrices.last(), 0, 0.5f, 0)
                        .setColor(70, 70, 70, 255)
                        .setNormal(0, 1, 0);
            });
        }

        { // Render drill head
            Model<?> drillHeadModel = this.drillHeadModels.computeIfAbsent(drillHeadable, ignored -> drillHeadData.modelResolver().apply(Either.left(this.context)));
            Identifier drillHeadTexture = this.drillHeadTextures.computeIfAbsent(drillHeadable, ignored -> drillHeadData.textureLocation());

            drillHeadData.onRender().render(state, matrices, queue, drillHeadModel, drillHeadModel.renderType(drillHeadTexture), light, overlay);
            matrices.popPose();
        }
    }

    @Override
    protected void postRender(DrillRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        if (shouldRenderHitboxes() && state.drillHeadAABB != null) {
            double minX = state.drillHeadAABB.minX - state.blockPos.getX();
            double minY = state.drillHeadAABB.minY - state.blockPos.getY();
            double minZ = state.drillHeadAABB.minZ - state.blockPos.getZ();
            double maxX = state.drillHeadAABB.maxX - state.blockPos.getX();
            double maxY = state.drillHeadAABB.maxY - state.blockPos.getY();
            double maxZ = state.drillHeadAABB.maxZ - state.blockPos.getZ();

            Gizmos.cuboid(new AABB(minX, minY, minZ, maxX, maxY, maxZ), GizmoStyle.stroke(0xFF0000FF));
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        // this has turned into a one time check
        // TODO: Look for a way to have this back to how it was.
        // return blockEntity.isDrilling() || blockEntity.isRetracting() && blockEntity.getDrillYOffset() < -1F;
        return true;
    }

    @Override
    public boolean shouldRender(DrillBlockEntity blockEntity, Vec3 pos) {
        return blockEntity.getBlockPos().closerToCenterThan(pos, blockEntity.getLevel() == null ? 64 : blockEntity.getLevel().getHeight());
    }

    @Override
    protected List<ModelPart> getModelParts() {
        return List.of(this.model.root());
    }
}
