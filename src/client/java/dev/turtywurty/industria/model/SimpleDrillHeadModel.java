package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.item.SimpleDrillHeadItem;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class SimpleDrillHeadModel extends Model {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("simple_drill_head"), "main");

    private final DrillHeadParts parts;

    public SimpleDrillHeadModel(ModelPart root) {
        super(root, RenderLayer::getEntitySolid);

        ModelPart main = root.getChild("main");
        ModelPart clockwise = main.getChild("clockwise");
        ModelPart counterClockwise = main.getChild("counterClockwise");
        this.parts = new DrillHeadParts(main, clockwise, counterClockwise);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        main.addChild("clockwise", ModelPartBuilder.create()
                .uv(0, 19)
                .cuboid(-7.0F, -7.0F, -7.0F, 14.0F, 2.0F, 14.0F, new Dilation(0.0F))
                .uv(57, 19)
                .cuboid(-1.0F, 5.0F, -1.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 51)
                .cuboid(-3.0F, 1.0F, -3.0F, 6.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(49, 36)
                .cuboid(-5.0F, -3.0F, -5.0F, 10.0F, 2.0F, 10.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -7.0F, 0.0F));

        main.addChild("counterClockwise", ModelPartBuilder.create()
                .uv(25, 51)
                .cuboid(-2.0F, 5.0F, -2.0F, 4.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(49, 49)
                .cuboid(-4.0F, 1.0F, -4.0F, 8.0F, 2.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 36)
                .cuboid(-6.0F, -3.0F, -6.0F, 12.0F, 2.0F, 12.0F, new Dilation(0.0F))
                .uv(0, 0)
                .cuboid(-8.0F, -7.0F, -8.0F, 16.0F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -9.0F, 0.0F));
        return TexturedModelData.of(modelData, 128, 128);
    }

    public static void onRender(DrillBlockEntity blockEntity, ItemStack headStack, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Model pModel, VertexConsumer vertexConsumer, int light, int overlay) {
        SimpleDrillHeadModel model = (SimpleDrillHeadModel) pModel;
        Object renderData = blockEntity.getRenderData();
        if(!(renderData instanceof SimpleDrillHeadItem.SimpleDrillRenderData rotationData))
            return;

        DrillHeadParts parts = model.getDrillHeadParts();
        float previousClockwiseYaw = parts.clockwise().yaw;
        float previousCounterClockwiseYaw = parts.counterClockwise().yaw;

        if(blockEntity.isDrilling()) {
            parts.clockwise().yaw = rotationData.clockwiseRotation += 0.1F;
            parts.counterClockwise().yaw = rotationData.counterClockwiseRotation -= 0.125F;
        } else {
            parts.clockwise().yaw = (float) Math.clamp(rotationData.clockwiseRotation -= 0.1F, 0, Math.PI * 2);
            parts.counterClockwise().yaw = (float) Math.clamp(rotationData.counterClockwiseRotation += 0.125F, 0, Math.PI * 2);
        }

        matrices.push();
        matrices.scale(0.9F, 0.9F, 0.9F);
        model.render(matrices, vertexConsumer, light, overlay);
        matrices.pop();

        parts.clockwise().yaw = previousClockwiseYaw;
        parts.counterClockwise().yaw = previousCounterClockwiseYaw;

        blockEntity.update();
    }

    public DrillHeadParts getDrillHeadParts() {
        return this.parts;
    }

    public record DrillHeadParts(ModelPart main, ModelPart clockwise, ModelPart counterClockwise) {}
}