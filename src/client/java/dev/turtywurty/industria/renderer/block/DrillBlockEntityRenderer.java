package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.model.DrillFrameModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class DrillBlockEntityRenderer implements BlockEntityRenderer<DrillBlockEntity> {
    private static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/drill_frame.png");

    private final BlockEntityRendererFactory.Context context;
    private final DrillFrameModel model;

    public DrillBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
        this.model = new DrillFrameModel(context.getLayerModelPart(DrillFrameModel.LAYER_LOCATION));
    }

    @Override
    public void render(DrillBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 + switch (entity.getCachedState().get(Properties.HORIZONTAL_FACING)) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        }));

        this.model.render(matrices, vertexConsumers.getBuffer(this.model.getLayer(TEXTURE_LOCATION)), light, overlay);

        matrices.pop();
    }
}
