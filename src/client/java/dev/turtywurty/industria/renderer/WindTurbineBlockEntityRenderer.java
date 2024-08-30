package dev.turtywurty.industria.renderer;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.WindTurbineBlockEntity;
import dev.turtywurty.industria.model.WindTurbineModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class WindTurbineBlockEntityRenderer implements BlockEntityRenderer<WindTurbineBlockEntity> {
    private static final Identifier TEXTURE = Industria.id("textures/block/wind_turbine.png");

    private final BlockEntityRendererFactory.Context context;
    private final WindTurbineModel model;

    public WindTurbineBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;

        this.model = new WindTurbineModel(context.getLayerModelPart(WindTurbineModel.LAYER_LOCATION));
    }

    @Override
    public void render(WindTurbineBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5f, 0, 0.5f);

        float outputPercentage = getEnergyPerTickPercent(entity);
        this.model.getParts().propeller0().roll += 0.25f * outputPercentage;
        this.model.getParts().propeller1().roll += 0.25f * outputPercentage;
        this.model.getParts().propeller2().roll += 0.25f * outputPercentage;

        this.model.render(matrices, vertexConsumers.getBuffer(this.model.getLayer(TEXTURE)), light, overlay);
        matrices.pop();
    }

    public int getEnergyPerTick(WindTurbineBlockEntity blockEntity) {
        return blockEntity.getEnergyOutput();
    }

    public float getEnergyPerTickPercent(WindTurbineBlockEntity blockEntity) {
        int output = getEnergyPerTick(blockEntity);
        if (output == 0)
            return 0.0F;

        return MathHelper.clamp((float) output / 500.0F, 0.0F, 1.0F);
    }
}
