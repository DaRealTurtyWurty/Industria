package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.SolarPanelBlock;
import dev.turtywurty.industria.blockentity.SolarPanelBlockEntity;
import dev.turtywurty.industria.model.AdvancedSolarPanelModel;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AdvancedSolarPanelBlockEntityRenderer extends IndustriaBlockEntityRenderer<SolarPanelBlockEntity, AdvancedSolarPanelBlockEntityRenderer.RenderState> {
    private static final float MAXIMUM_TILT = 60.0F * Mth.DEG_TO_RAD;
    private static final float HALF_PI = (float) Math.PI / 2.0F;
    private static final Identifier ENERGY_BAR_TEXTURE = Industria.id("textures/block/energy_bar.png");
    private static final float ENERGY_BAR_MIN_X = -4.0F / 16.0F;
    private static final float ENERGY_BAR_MAX_X = 4.0F / 16.0F;
    private static final float ENERGY_BAR_NORMAL_MIN_Y = 20.0F / 16.0F;
    private static final float ENERGY_BAR_NORMAL_MAX_Y = 22.0F / 16.0F;
    private static final float ENERGY_BAR_NORMAL_Z = -7.01F / 16.0F;
    private static final float ENERGY_BAR_STAIR_MIN_Y = 27.0F / 16.0F;
    private static final float ENERGY_BAR_STAIR_MAX_Y = 29.0F / 16.0F;
    private static final float ENERGY_BAR_STAIR_Z = -1.01F / 16.0F;

    private final AdvancedSolarPanelModel model;
    private final AdvancedSolarPanelModel stairModel;

    public AdvancedSolarPanelBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new AdvancedSolarPanelModel(context.bakeLayer(AdvancedSolarPanelModel.LAYER_LOCATION));
        this.stairModel = new AdvancedSolarPanelModel(context.bakeLayer(AdvancedSolarPanelModel.STAIR_LAYER_LOCATION));
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(SolarPanelBlockEntity blockEntity, RenderState state, float tickProgress,
                                   Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.advanced = blockEntity.isAdvanced();
        state.onStair = state.blockState.getValue(SolarPanelBlock.ON_STAIR);
        state.energy = blockEntity.getEnergyStorage().getAmount();
        state.energyCapacity = blockEntity.getEnergyStorage().getCapacity();
        if (!state.advanced)
            return;

        Direction facing = state.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        state.yaw = getTrackingYaw(facing) + (state.onStair ? (float) Math.PI : 0.0F);

        state.powered = state.blockState.getValue(SolarPanelBlock.POWERED);
        float sunAngle = Minecraft.getInstance().gameRenderer.getMainCamera().attributeProbe()
                .getValue(EnvironmentAttributes.SUN_ANGLE, tickProgress);
        state.pitch = getTrackingPitch(sunAngle);
    }

    @Override
    protected void onRender(RenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        if (state.advanced) {
            AdvancedSolarPanelModel selectedModel = state.onStair ? this.stairModel : this.model;
            Identifier texture = state.powered
                    ? AdvancedSolarPanelModel.TEXTURE_LOCATION
                    : AdvancedSolarPanelModel.POWERLESS_TEXTURE_LOCATION;

            matrices.pushPose();
            if (state.onStair) {
                matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
            }

            queue.submitModel(selectedModel, new AdvancedSolarPanelModel.RenderState(state.yaw, state.pitch),
                    matrices, selectedModel.renderType(texture), light, overlay, 0, state.breakProgress);
            matrices.popPose();
        }

        renderEnergyBuffer(state, matrices, queue, light, overlay);
    }

    private static void renderEnergyBuffer(RenderState state, PoseStack matrices, SubmitNodeCollector queue,
                                           int light, int overlay) {
        if (state.energy <= 0L || state.energyCapacity <= 0L)
            return;

        float fillPercentage = Mth.clamp((float) state.energy / state.energyCapacity, 0.0F, 1.0F);
        float maxX = Mth.lerp(fillPercentage, ENERGY_BAR_MIN_X, ENERGY_BAR_MAX_X);
        float minY = state.onStair ? ENERGY_BAR_STAIR_MIN_Y : ENERGY_BAR_NORMAL_MIN_Y;
        float maxY = state.onStair ? ENERGY_BAR_STAIR_MAX_Y : ENERGY_BAR_NORMAL_MAX_Y;
        float z = state.onStair ? ENERGY_BAR_STAIR_Z : ENERGY_BAR_NORMAL_Z;

        matrices.pushPose();
        if (state.onStair)
            matrices.mulPose(Axis.YP.rotationDegrees(180.0F));

        queue.submitCustomGeometry(matrices, RenderTypes.entityTranslucent(ENERGY_BAR_TEXTURE), (entry, vertexConsumer) -> {
            vertexConsumer.addVertex(entry, ENERGY_BAR_MIN_X, minY, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(0.0F, 0.0F).setOverlay(overlay).setLight(light).setNormal(0.0F, 0.0F, -1.0F);
            vertexConsumer.addVertex(entry, ENERGY_BAR_MIN_X, maxY, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(0.0F, 0.25F).setOverlay(overlay).setLight(light).setNormal(0.0F, 0.0F, -1.0F);
            vertexConsumer.addVertex(entry, maxX, maxY, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(fillPercentage, 0.25F).setOverlay(overlay).setLight(light).setNormal(0.0F, 0.0F, -1.0F);
            vertexConsumer.addVertex(entry, maxX, minY, z).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(fillPercentage, 0.0F).setOverlay(overlay).setLight(light).setNormal(0.0F, 0.0F, -1.0F);
        });
        matrices.popPose();
    }

    private static float getTrackingYaw(Direction facing) {
        int blockRotation = switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
        return (90.0F - blockRotation) * Mth.DEG_TO_RAD;
    }

    static float getTrackingPitch(float sunAngleDegrees) {
        float sunAngle = Mth.wrapDegrees(sunAngleDegrees);
        if (sunAngle >= -90.0F && sunAngle <= 90.0F)
            return -sunAngle / 90.0F * MAXIMUM_TILT;

        if (sunAngle > 90.0F) {
            float resetProgress = Mth.clamp((sunAngle - 90.0F) / 15.0F, 0.0F, 1.0F);
            return Mth.lerp(resetProgress, -MAXIMUM_TILT, 0.0F);
        }

        float resetProgress = Mth.clamp((sunAngle + 105.0F) / 15.0F, 0.0F, 1.0F);
        return Mth.lerp(resetProgress, 0.0F, MAXIMUM_TILT);
    }

    public static class RenderState extends IndustriaBlockEntityRenderState {
        public boolean advanced;
        public boolean onStair;
        public boolean powered;
        public float yaw = HALF_PI;
        public float pitch;
        public long energy;
        public long energyCapacity;

        public RenderState() {
            super(0);
        }
    }
}
