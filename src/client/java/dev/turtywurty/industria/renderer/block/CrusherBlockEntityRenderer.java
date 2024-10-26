package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.CrusherBlockEntity;
import dev.turtywurty.industria.model.CrusherModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class CrusherBlockEntityRenderer implements BlockEntityRenderer<CrusherBlockEntity> {
    private static final Identifier TEXTURE = Industria.id("textures/block/crusher.png");

    private final CrusherModel model;

    public CrusherBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.model = new CrusherModel(context.getLayerModelPart(CrusherModel.LAYER_LOCATION));
    }

    @Override
    public void render(CrusherBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 + switch (entity.getCachedState().get(Properties.HORIZONTAL_FACING)) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        }));

        float prevBottomLeftRoll = this.model.getCrusherParts().bottomLeft().roll;
        float prevBottomRightRoll = this.model.getCrusherParts().bottomRight().roll;
        float prevTopLeftRoll = this.model.getCrusherParts().topLeft().roll;
        float prevTopRightRoll = this.model.getCrusherParts().topRight().roll;

        if (entity.getProgress() > 0) {
            this.model.getCrusherParts().bottomLeft().roll = entity.getProgress() / 100.0F;
            this.model.getCrusherParts().bottomRight().roll = entity.getProgress() / 100.0F;
            this.model.getCrusherParts().topLeft().roll = entity.getProgress() / 100.0F;
            this.model.getCrusherParts().topRight().roll = entity.getProgress() / 100.0F;
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(TEXTURE));
        this.model.render(matrices, vertexConsumer, light, overlay);

        this.model.getCrusherParts().bottomLeft().roll = prevBottomLeftRoll;
        this.model.getCrusherParts().bottomRight().roll = prevBottomRightRoll;
        this.model.getCrusherParts().topLeft().roll = prevTopLeftRoll;
        this.model.getCrusherParts().topRight().roll = prevTopRightRoll;

        matrices.pop();
    }
}
