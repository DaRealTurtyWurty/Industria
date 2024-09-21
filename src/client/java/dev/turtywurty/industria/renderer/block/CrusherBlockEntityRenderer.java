package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.CrusherBlockEntity;
import dev.turtywurty.industria.model.CrusherModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CrusherBlockEntityRenderer implements BlockEntityRenderer<CrusherBlockEntity> {
    private static final Identifier TEXTURE = Industria.id("textures/block/crusher.png");

    private final CrusherModel model;

    public CrusherBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.model = new CrusherModel(context.getLayerModelPart(CrusherModel.LAYER_LOCATION));
    }

    @Override
    public void render(CrusherBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5, 0.225, 0.5);
        matrices.scale(1.0F, -1.0F, -1.0F);

        if(entity.getProgress() > 0) {
            this.model.getParts().bottomLeft().roll += 0.1F;
            this.model.getParts().bottomRight().roll -= 0.1F;
            this.model.getParts().topLeft().roll += 0.1F;
            this.model.getParts().topRight().roll -= 0.1F;
        } else {
            this.model.getParts().bottomLeft().roll = 0;
            this.model.getParts().bottomRight().roll = 0;
            this.model.getParts().topLeft().roll = 0;
            this.model.getParts().topRight().roll = 0;
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(TEXTURE));
        this.model.render(matrices, vertexConsumer, light, overlay);
        this.model.getParts().left().render(matrices, vertexConsumer, light, overlay);
        this.model.getParts().right().render(matrices, vertexConsumer, light, overlay);

        matrices.pop();
    }
}
