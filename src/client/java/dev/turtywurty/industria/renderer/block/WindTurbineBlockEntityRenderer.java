package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.turtywurty.industria.blockentity.WindTurbineBlockEntity;
import dev.turtywurty.industria.model.WindTurbineModel;
import dev.turtywurty.industria.state.WindTurbineRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WindTurbineBlockEntityRenderer extends IndustriaBlockEntityRenderer<WindTurbineBlockEntity, WindTurbineRenderState> {
    private final WindTurbineModel model;

    public WindTurbineBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new WindTurbineModel(context.bakeLayer(WindTurbineModel.LAYER_LOCATION));
    }

    @Override
    public WindTurbineRenderState createRenderState() {
        return new WindTurbineRenderState();
    }

    @Override
    public void extractRenderState(WindTurbineBlockEntity blockEntity, WindTurbineRenderState state, float tickProgress, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.propellerRotation = blockEntity.getPropellerRotation();
        state.energyOutput = blockEntity.getEnergyOutput();
    }

    @Override
    public void onRender(WindTurbineRenderState state, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
        float outputPercentage = getEnergyPerTickPercent(state);
        state.propellerRotation += (outputPercentage * 0.25f) * (Minecraft.getInstance().level.getGameTime() + state.tickProgress);

        queue.submitModel(this.model,
                new WindTurbineModel.WindTurbineModelRenderState(state.propellerRotation),
                matrices, this.model.renderType(WindTurbineModel.TEXTURE_LOCATION),
                light, overlay, 0, state.breakProgress);
    }

    public long getEnergyPerTick(WindTurbineRenderState state) {
        return state.energyOutput;
    }

    public float getEnergyPerTickPercent(WindTurbineRenderState state) {
        long output = getEnergyPerTick(state);
        if (output == 0L)
            return 0.0F;

        return Mth.clamp((float) output / 500.0F, 0.0F, 1.0F);
    }
}
