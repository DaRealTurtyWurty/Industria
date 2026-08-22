package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.block.SolarPanelBlock;
import dev.turtywurty.industria.blockentity.SolarPanelBlockEntity;
import dev.turtywurty.industria.model.AdvancedSolarPanelModel;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AdvancedSolarPanelBlockEntityRenderer extends IndustriaBlockEntityRenderer<SolarPanelBlockEntity, AdvancedSolarPanelBlockEntityRenderer.RenderState> {
    private static final float MAXIMUM_TILT = 60.0F * Mth.DEG_TO_RAD;
    private static final float HALF_PI = (float) Math.PI / 2.0F;

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
        if (!state.advanced)
            return;

        state.onStair = state.blockState.getValue(SolarPanelBlock.ON_STAIR);
        Direction facing = state.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        state.yaw = getTrackingYaw(facing) + (state.onStair ? (float) Math.PI : 0.0F);

        Level level = blockEntity.getLevel();
        state.pitch = level == null ? 0.0F : getTrackingPitch(level.getOverworldClockTime(), tickProgress);
        state.powered = state.blockState.getValue(SolarPanelBlock.POWERED);
    }

    @Override
    protected void onRender(RenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        if (!state.advanced)
            return;

        AdvancedSolarPanelModel selectedModel = state.onStair ? this.stairModel : this.model;
        Identifier texture = state.powered
                ? AdvancedSolarPanelModel.TEXTURE_LOCATION
                : AdvancedSolarPanelModel.POWERLESS_TEXTURE_LOCATION;

        matrices.pushPose();
        if (state.onStair)
            matrices.mulPose(Axis.YP.rotationDegrees(180.0F));

        queue.submitModel(selectedModel, new AdvancedSolarPanelModel.RenderState(state.yaw, state.pitch),
                matrices, selectedModel.renderType(texture), light, overlay, 0, state.breakProgress);
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

    static float getTrackingPitch(long dayTime, float tickProgress) {
        float timeOfDay = Math.floorMod(dayTime, 24_000L) + tickProgress;
        if (timeOfDay <= 12_000.0F) {
            float dayProgress = timeOfDay / 12_000.0F;
            return Mth.lerp(dayProgress, MAXIMUM_TILT, -MAXIMUM_TILT);
        }

        if (timeOfDay < 13_000.0F)
            return Mth.lerp((timeOfDay - 12_000.0F) / 1_000.0F, -MAXIMUM_TILT, 0.0F);

        if (timeOfDay < 23_000.0F)
            return 0.0F;

        return Mth.lerp((timeOfDay - 23_000.0F) / 1_000.0F, 0.0F, MAXIMUM_TILT);
    }

    public static class RenderState extends IndustriaBlockEntityRenderState {
        public boolean advanced;
        public boolean onStair;
        public boolean powered;
        public float yaw = HALF_PI;
        public float pitch;

        public RenderState() {
            super(0);
        }
    }
}
