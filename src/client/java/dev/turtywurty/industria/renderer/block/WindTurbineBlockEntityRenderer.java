package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.WindTurbineBlockEntity;
import dev.turtywurty.industria.model.WindTurbineModel;
import dev.turtywurty.industria.state.WindTurbineRenderState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class WindTurbineBlockEntityRenderer extends IndustriaBlockEntityRenderer<WindTurbineBlockEntity, WindTurbineRenderState> {
    private final WindTurbineModel model;

    public WindTurbineBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new WindTurbineModel(context.getLayerModelPart(WindTurbineModel.LAYER_LOCATION));
    }

    @Override
    public WindTurbineRenderState createRenderState() {
        return new WindTurbineRenderState();
    }

    @Override
    public void updateRenderState(WindTurbineBlockEntity blockEntity, WindTurbineRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.propellerRotation = blockEntity.getPropellerRotation();
        state.energyOutput = blockEntity.getEnergyOutput();
    }

    @Override
    public void onRender(WindTurbineRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 + switch (state.blockState.get(Properties.HORIZONTAL_FACING)) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        }));

        float outputPercentage = getEnergyPerTickPercent(state);
        state.propellerRotation = state.propellerRotation + (outputPercentage * 0.25f);
        model.getWindTurbineParts().propellers().roll = state.propellerRotation;

        RenderLayer renderLayer = this.model.getLayer(WindTurbineModel.TEXTURE_LOCATION);
        queue.submitModel(this.model, state, matrices, renderLayer, light, overlay, 0, state.crumblingOverlay);
        this.model.getWindTurbineParts().propellers().roll = 0.0F;
        matrices.pop();
    }

    public long getEnergyPerTick(WindTurbineRenderState state) {
        return state.energyOutput;
    }

    public float getEnergyPerTickPercent(WindTurbineRenderState state) {
        long output = getEnergyPerTick(state);
        if (output == 0L)
            return 0.0F;

        return MathHelper.clamp((float) output / 500.0F, 0.0F, 1.0F);
    }
}
