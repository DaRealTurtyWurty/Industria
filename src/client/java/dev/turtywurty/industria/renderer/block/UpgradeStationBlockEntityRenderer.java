package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.UpgradeStationBlockEntity;
import dev.turtywurty.industria.model.UpgradeStationModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class UpgradeStationBlockEntityRenderer extends IndustriaBlockEntityRenderer<UpgradeStationBlockEntity> {

    private final UpgradeStationModel model;

    public UpgradeStationBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new UpgradeStationModel(context.getLayerModelPart(UpgradeStationModel.LAYER_LOCATION));
    }

    @Override
    protected void renderModel(UpgradeStationBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.model.render(matrices, vertexConsumers.getBuffer(this.model.getLayer(UpgradeStationModel.TEXTURE_LOCATION)), light, overlay);

    }

    @Override
    protected void onRender(UpgradeStationBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

    }
}
