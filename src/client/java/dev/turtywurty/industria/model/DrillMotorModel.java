package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class DrillMotorModel extends Model {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("drill_motor"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/drill_motor.png");

    private final DrillMotorParts parts;

    public DrillMotorModel(ModelPart root) {
        super(root, RenderLayer::getEntitySolid);

        ModelPart main = root.getChild("main");
        ModelPart spinRod = main.getChild("spinRod");
        ModelPart rodGear = spinRod.getChild("rodGear");
        ModelPart connectingGear = spinRod.getChild("connectingGear");
        this.parts = new DrillMotorParts(main, spinRod, rodGear, connectingGear);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create().uv(15, 13).cuboid(-4.9583F, -1.0455F, -4.0001F, 1.0F, 1.0F, 8.0F, new Dilation(0.0F))
                .uv(15, 23).cuboid(2.0417F, -1.0455F, -4.0001F, 1.0F, 1.0F, 8.0F, new Dilation(0.0F))
                .uv(38, 33).cuboid(-4.9583F, -2.0455F, 0.9999F, 8.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(47, 36).cuboid(-4.9583F, -5.0455F, 1.9999F, 7.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(34, 29).cuboid(-4.9583F, -7.0455F, -1.0001F, 7.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(39, 48).cuboid(2.0417F, -6.0455F, -1.0001F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(28, 48).cuboid(2.0417F, -5.0455F, -2.0001F, 1.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(17, 47).cuboid(-4.9583F, -6.0455F, -2.0001F, 1.0F, 4.0F, 4.0F, new Dilation(0.0F))
                .uv(47, 40).cuboid(-4.9583F, -5.0455F, -3.0001F, 7.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(28, 45).cuboid(-4.9583F, -2.0455F, -2.0001F, 8.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(28, 41).cuboid(-4.9583F, -2.0455F, -1.0001F, 7.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(46, 51).cuboid(2.0417F, -3.0455F, -1.0001F, 1.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(15, 0).cuboid(-5.9583F, -0.3955F, -5.0001F, 10.0F, 2.0F, 10.0F, new Dilation(0.0F))
                .uv(47, 44).cuboid(0.0417F, 28.6045F, -2.0001F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(28, 36).cuboid(0.0417F, 15.6045F, -1.0001F, 7.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-2.9583F, 1.6045F, -2.0001F, 3.0F, 41.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(18.7417F, -18.6045F, 13.0001F, 0.0F, 3.1416F, 0.0F));

        ModelPartData cube_r1 = main.addChild("cube_r1", ModelPartBuilder.create().uv(5, 52).cuboid(2.5F, -9.8919F, -11.7203F, 1.0F, 1.0F, 1.4125F, new Dilation(0.0F))
                .uv(34, 13).cuboid(-4.5F, -9.1848F, -12.4274F, 7.0F, 1.0F, 2.825F, new Dilation(0.0F))
                .uv(10, 52).cuboid(2.5F, -11.722F, -13.1345F, 1.0F, 1.4125F, 1.0F, new Dilation(0.0F))
                .uv(47, 23).cuboid(-4.5F, -12.4274F, -13.8416F, 7.0F, 2.825F, 1.0F, new Dilation(0.0F))
                .uv(0, 47).cuboid(-4.5F, -12.4274F, -9.1848F, 7.0F, 2.825F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-0.4583F, 11.5295F, -0.0001F, -0.7854F, 0.0F, 0.0F));

        ModelPartData cube_r2 = main.addChild("cube_r2", ModelPartBuilder.create().uv(0, 52).cuboid(2.5F, -9.8919F, 10.3078F, 1.0F, 1.0F, 1.4125F, new Dilation(0.0F))
                .uv(39, 52).cuboid(2.5F, -11.7203F, 12.1345F, 1.0F, 1.4125F, 1.0F, new Dilation(0.0F))
                .uv(47, 18).cuboid(-4.5F, -12.4274F, 12.8416F, 7.0F, 2.825F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-0.4583F, 11.5295F, -0.0001F, 0.7854F, 0.0F, 0.0F));

        ModelPartData spinRod = main.addChild("spinRod", ModelPartBuilder.create(), ModelTransform.pivot(8.2583F, -4.0455F, -0.0001F));

        ModelPartData rodGear = spinRod.addChild("rodGear", ModelPartBuilder.create().uv(15, 33).cuboid(-7.75F, -0.5F, -0.5F, 10.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(34, 18).cuboid(2.25F, -2.5F, -2.5F, 1.0F, 5.0F, 5.0F, new Dilation(0.0F)), ModelTransform.pivot(2.5333F, 0.0F, 0.0F));

        ModelPartData connectingGear = spinRod.addChild("connectingGear", ModelPartBuilder.create().uv(15, 36).cuboid(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 5.0F, new Dilation(0.0F)), ModelTransform.pivot(5.2833F, 0.0F, 5.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }

    public DrillMotorParts getDrillMotorParts() {
        return this.parts;
    }

    public record DrillMotorParts(ModelPart main, ModelPart spinRod, ModelPart rodGear, ModelPart connectingGear) {
    }
}