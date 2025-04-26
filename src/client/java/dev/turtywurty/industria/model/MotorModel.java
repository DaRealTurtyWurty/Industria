package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class MotorModel extends Model {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("motor"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/motor.png");

    private final MotorParts parts;

    public MotorModel(ModelPart root) {
        super(root, RenderLayer::getEntitySolid);

        ModelPart main = root.getChild("main");
        ModelPart spinRod = main.getChild("spinRod");
        this.parts = new MotorParts(main, spinRod);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -12.0F, 24.0F, 1.0F, 1.0F, 8.0F, new Dilation(0.0F)).uv(0, 10).cuboid(3.0F, -12.0F, 24.0F, 1.0F, 1.0F, 8.0F, new Dilation(0.0F)).uv(19, 16).cuboid(-4.0F, -13.0F, 29.0F, 8.0F, 1.0F, 1.0F, new Dilation(0.0F)).uv(0, 25).cuboid(-4.0F, -16.0F, 30.0F, 7.0F, 2.0F, 1.0F, new Dilation(0.0F)).uv(19, 5).cuboid(-4.0F, -18.0F, 27.0F, 7.0F, 1.0F, 2.0F, new Dilation(0.0F)).uv(9, 33).cuboid(3.0F, -17.0F, 27.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F)).uv(28, 29).cuboid(3.0F, -16.0F, 26.0F, 1.0F, 2.0F, 4.0F, new Dilation(0.0F)).uv(17, 29).cuboid(-4.0F, -17.0F, 26.0F, 1.0F, 4.0F, 4.0F, new Dilation(0.0F)).uv(0, 29).cuboid(-4.0F, -16.0F, 25.0F, 7.0F, 2.0F, 1.0F, new Dilation(0.0F)).uv(19, 13).cuboid(-4.0F, -13.0F, 26.0F, 8.0F, 1.0F, 1.0F, new Dilation(0.0F)).uv(19, 9).cuboid(-4.0F, -13.0F, 27.0F, 7.0F, 1.0F, 2.0F, new Dilation(0.0F)).uv(34, 24).cuboid(3.0F, -14.0F, 27.0F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.rotation(0.0F, 35.0F, -28.0F));

        ModelPartData cube_r1 = main.addChild("cube_r1", ModelPartBuilder.create().uv(36, 19).cuboid(-3.0F, -0.5F, -0.5F, 1.0F, 1.0F, 1.4125F, new Dilation(0.0F)), ModelTransform.of(6.0F, -14.0F, 26.7071F, -0.7854F, 0.0F, 0.0F));

        ModelPartData cube_r2 = main.addChild("cube_r2", ModelPartBuilder.create().uv(19, 0).cuboid(-4.0F, -0.5F, -0.5F, 7.0F, 1.0F, 2.825F, new Dilation(0.0F)), ModelTransform.of(0.0F, -14.0F, 25.7071F, -0.7854F, 0.0F, 0.0F));

        ModelPartData cube_r3 = main.addChild("cube_r3", ModelPartBuilder.create().uv(0, 36).cuboid(-0.5F, -0.5F, -0.9125F, 1.0F, 1.0F, 1.4125F, new Dilation(0.0F)), ModelTransform.of(3.5F, -14.0F, 29.2929F, 0.7854F, 0.0F, 0.0F));

        ModelPartData cube_r4 = main.addChild("cube_r4", ModelPartBuilder.create().uv(28, 36).cuboid(-3.0F, 1.4075F, -0.005F, 1.0F, 1.4125F, 1.0F, new Dilation(0.0F)), ModelTransform.of(6.0F, -17.9929F, 28.0F, -0.7854F, 0.0F, 0.0F));

        ModelPartData cube_r5 = main.addChild("cube_r5", ModelPartBuilder.create().uv(0, 20).cuboid(-4.0F, -0.005F, -0.005F, 7.0F, 2.825F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -17.9929F, 27.0F, -0.7854F, 0.0F, 0.0F));

        ModelPartData cube_r6 = main.addChild("cube_r6", ModelPartBuilder.create().uv(33, 36).cuboid(2.0F, -0.5F, -0.5F, 1.0F, 1.4125F, 1.0F, new Dilation(0.0F)), ModelTransform.of(1.0F, -16.2929F, 29.0F, 0.7854F, 0.0F, 0.0F));

        ModelPartData cube_r7 = main.addChild("cube_r7", ModelPartBuilder.create().uv(19, 19).cuboid(-4.0F, -0.5F, -0.5F, 7.0F, 2.825F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -17.2929F, 29.0F, 0.7854F, 0.0F, 0.0F));

        ModelPartData cube_r8 = main.addChild("cube_r8", ModelPartBuilder.create().uv(17, 24).cuboid(-4.0F, -0.5F, -0.5F, 7.0F, 2.825F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -14.0F, 30.2929F, -0.7854F, 0.0F, 0.0F));

        ModelPartData spinRod = main.addChild("spinRod", ModelPartBuilder.create().uv(0, 33).cuboid(-1.5F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.rotation(5.5F, -15.0F, 28.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }

    public MotorParts getMotorParts() {
        return this.parts;
    }

    public record MotorParts(ModelPart main, ModelPart spinRod) {}
}