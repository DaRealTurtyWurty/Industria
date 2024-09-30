// Made with Blockbench 4.11.0
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.entity.DrillHeadEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;

public class SimpleDrillHeadModel extends EntityModel<DrillHeadEntity> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("simple_drill_head"), "main");

    private final ModelPart main;
    private final ModelPart clockwise;
    private final ModelPart counterClockwise;

    public SimpleDrillHeadModel(ModelPart root) {
        this.main = root.getChild("main");
        this.clockwise = this.main.getChild("clockwise");
        this.counterClockwise = this.main.getChild("counterClockwise");
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

    @Override
    public void setAngles(DrillHeadEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        this.main.render(matrices, vertexConsumer, light, overlay, color);
    }

    public ModelPart getClockwise() {
        return this.clockwise;
    }

    public ModelPart getCounterClockwise() {
        return this.counterClockwise;
    }

    public static void onEntityRender(DrillHeadEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

    }
}