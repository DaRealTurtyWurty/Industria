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
import net.minecraft.util.math.Vec3d;

public class CrusherBlockEntityRenderer extends IndustriaBlockEntityRenderer<CrusherBlockEntity> {
    private static final Identifier TEXTURE = Industria.id("textures/block/crusher.png");

    private final CrusherModel model;

    public CrusherBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new CrusherModel(context.getLayerModelPart(CrusherModel.LAYER_LOCATION));
    }

    @Override
    protected void renderModel(CrusherBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
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
    }

    @Override
    protected void onRender(CrusherBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

    }
}
