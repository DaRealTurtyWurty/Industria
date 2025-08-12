package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class ShakingTableModel extends Model {
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/shaking_table.png");
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("shaking_table"), "main");

    private final ModelParts modelParts;

    public ShakingTableModel(ModelPart root) {
        super(root, RenderLayer::getEntityCutout);

        ModelPart main = root.getChild("main");
        ModelPart table = main.getChild("table");
        ModelPart waterChannels = table.getChild("waterChannels");
        ModelPart motorConnection = table.getChild("motorConnection");
        ModelPart supports = main.getChild("supports");
        ModelPart baseSupports = supports.getChild("baseSupports");
        ModelPart legsSupports = supports.getChild("legsSupports");
        ModelPart leg1 = legsSupports.getChild("leg1");
        ModelPart leg2 = legsSupports.getChild("leg2");
        ModelPart motor = main.getChild("motor");
        ModelPart motorLegs = motor.getChild("motorLegs");
        ModelPart springs = motor.getChild("springs");

        this.modelParts = new ModelParts(main, table, waterChannels, motorConnection, supports, baseSupports, legsSupports, leg1, leg2, motor, motorLegs, springs);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create(), ModelTransform.origin(0.0F, 24.0F, 0.0F));

        ModelPartData table = main.addChild("table", ModelPartBuilder.create().uv(0, 0).cuboid(-17.6F, -2.6F, -27.0F, 36.0F, 2.0F, 54.0F, new Dilation(0.0F))
                .uv(155, 135).cuboid(-17.6F, -6.6F, -27.0F, 36.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(155, 57).cuboid(17.4F, -6.6F, -26.0F, 1.0F, 4.0F, 53.0F, new Dilation(0.0F))
                .uv(54, 219).cuboid(9.4F, -6.6F, 26.0F, 8.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(97, 229).cuboid(6.4F, -0.6F, -21.0F, 1.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(186, 211).cuboid(-7.6F, -0.6F, -21.0F, 1.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(123, 229).cuboid(-7.6F, -0.6F, 16.0F, 1.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(110, 229).cuboid(6.4F, -0.6F, 16.0F, 1.0F, 2.0F, 5.0F, new Dilation(0.0F)), ModelTransform.origin(-0.4F, -17.4F, 8.0F));

        table.addChild("waterChannels", ModelPartBuilder.create().uv(200, 208).cuboid(31.0F, -6.0F, 38.0F, 1.0F, 2.0F, 14.0F, new Dilation(0.0F))
                .uv(216, 115).cuboid(32.0F, -4.0F, 39.0F, 2.0F, 0.0F, 12.0F, new Dilation(0.0F))
                .uv(148, 213).cuboid(32.0F, -6.0F, 51.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(231, 221).cuboid(31.0F, -8.0F, 42.0F, 4.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(170, 227).cuboid(30.0F, -8.0F, 37.0F, 1.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(231, 217).cuboid(31.0F, -8.0F, 37.0F, 4.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(67, 225).cuboid(32.0F, -6.0F, 38.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(228, 225).cuboid(35.0F, -8.0F, 37.0F, 1.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(210, 206).cuboid(32.0F, -6.0F, 41.0F, 2.0F, 0.0F, 1.0F, new Dilation(0.0F))
                .uv(97, 237).cuboid(32.0F, -6.0F, 35.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(181, 0).cuboid(31.0F, -6.0F, 16.0F, 1.0F, 2.0F, 20.0F, new Dilation(0.0F))
                .uv(236, 53).cuboid(31.0F, -8.0F, 19.0F, 4.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(212, 157).cuboid(32.0F, -6.0F, 18.0F, 2.0F, 0.0F, 1.0F, new Dilation(0.0F))
                .uv(82, 229).cuboid(30.0F, -8.0F, 14.0F, 1.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(198, 236).cuboid(31.0F, -8.0F, 14.0F, 4.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(67, 229).cuboid(35.0F, -8.0F, 14.0F, 1.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(181, 23).cuboid(32.0F, -4.0F, 16.0F, 2.0F, 0.0F, 19.0F, new Dilation(0.0F)), ModelTransform.origin(-16.6F, -0.6F, -42.0F));

        table.addChild("motorConnection", ModelPartBuilder.create().uv(186, 219).cuboid(-2.0F, -20.0F, -20.0F, 4.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(224, 37).cuboid(-4.0F, -20.0F, -22.0F, 8.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(237, 132).cuboid(-2.0F, -20.0F, -21.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(237, 136).cuboid(1.0F, -20.0F, -21.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.origin(0.4F, 17.4F, -8.0F));

        ModelPartData supports = main.addChild("supports", ModelPartBuilder.create(), ModelTransform.origin(-0.2F, -1.0F, 6.4F));

        supports.addChild("baseSupports", ModelPartBuilder.create().uv(149, 217).cuboid(-10.0F, -2.0F, -86.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(181, 53).cuboid(-14.0F, -2.0F, -85.0F, 26.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(155, 200).cuboid(-14.0F, -2.0F, -87.0F, 26.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(181, 43).cuboid(-14.0F, -2.0F, -99.0F, 26.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(181, 48).cuboid(-14.0F, -2.0F, -115.0F, 26.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(204, 163).cuboid(-14.0F, -2.0F, -108.0F, 26.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(204, 167).cuboid(-14.0F, -2.0F, -105.0F, 26.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 135).cuboid(12.0F, -2.0F, -115.0F, 2.0F, 2.0F, 75.0F, new Dilation(0.0F))
                .uv(0, 57).cuboid(-16.0F, -2.0F, -115.0F, 2.0F, 2.0F, 75.0F, new Dilation(0.0F))
                .uv(204, 159).cuboid(-14.0F, -2.0F, -50.0F, 26.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(155, 204).cuboid(-14.0F, -2.0F, -48.0F, 26.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(109, 237).cuboid(-10.0F, -2.0F, -49.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(149, 221).cuboid(6.0F, -2.0F, -86.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(104, 237).cuboid(6.0F, -2.0F, -49.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.origin(1.2F, 1.0F, 68.6F));

        ModelPartData legsSupports = supports.addChild("legsSupports", ModelPartBuilder.create(), ModelTransform.origin(-0.3F, -11.1739F, -16.9F));

        legsSupports.addChild("leg1", ModelPartBuilder.create().uv(235, 234).cuboid(5.5F, 3.1739F, -1.5F, 2.0F, 7.0F, 1.0F, new Dilation(0.0F))
                .uv(0, 230).cuboid(7.5F, 3.1739F, -1.5F, 1.0F, 7.0F, 3.0F, new Dilation(0.0F))
                .uv(212, 145).cuboid(-8.5F, -0.8261F, -1.5F, 17.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(212, 151).cuboid(-8.5F, -0.8261F, 0.5F, 17.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(74, 213).cuboid(-8.5F, 0.1739F, -0.5F, 17.0F, 3.0F, 1.0F, new Dilation(0.0F))
                .uv(230, 132).cuboid(-7.5F, 3.1739F, -1.5F, 2.0F, 7.0F, 1.0F, new Dilation(0.0F))
                .uv(18, 230).cuboid(-7.5F, 3.1739F, 0.5F, 2.0F, 7.0F, 1.0F, new Dilation(0.0F))
                .uv(136, 229).cuboid(-8.5F, 3.1739F, -1.5F, 1.0F, 7.0F, 3.0F, new Dilation(0.0F))
                .uv(228, 234).cuboid(5.5F, 3.1739F, 0.5F, 2.0F, 7.0F, 1.0F, new Dilation(0.0F))
                .uv(204, 174).cuboid(-10.5F, -0.8261F, -2.5F, 21.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(155, 184).cuboid(-10.5F, -2.8261F, -2.5F, 21.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(204, 171).cuboid(-10.5F, -0.8261F, -0.5F, 21.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(204, 177).cuboid(-10.5F, -0.8261F, 1.5F, 21.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(99, 218).cuboid(5.5F, -3.8261F, -4.5F, 3.0F, 1.0F, 9.0F, new Dilation(0.0F))
                .uv(30, 238).cuboid(6.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(224, 26).cuboid(5.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new Dilation(0.0F))
                .uv(74, 218).cuboid(-8.5F, -3.8261F, -4.5F, 3.0F, 1.0F, 9.0F, new Dilation(0.0F))
                .uv(224, 4).cuboid(-6.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new Dilation(0.0F))
                .uv(216, 132).cuboid(-7.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(224, 15).cuboid(-8.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new Dilation(0.0F))
                .uv(221, 132).cuboid(-7.5F, -4.8261F, 3.5F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(25, 238).cuboid(6.5F, -4.8261F, 3.5F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(186, 225).cuboid(7.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, 0.0F, 0.0F));

        legsSupports.addChild("leg2", ModelPartBuilder.create().uv(170, 236).cuboid(5.5F, 3.1739F, -1.5F, 2.0F, 7.0F, 1.0F, new Dilation(0.0F))
                .uv(9, 230).cuboid(7.5F, 3.1739F, -1.5F, 1.0F, 7.0F, 3.0F, new Dilation(0.0F))
                .uv(0, 213).cuboid(-8.5F, -0.8261F, -1.5F, 17.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(37, 213).cuboid(-8.5F, -0.8261F, 0.5F, 17.0F, 4.0F, 1.0F, new Dilation(0.0F))
                .uv(111, 213).cuboid(-8.5F, 0.1739F, -0.5F, 17.0F, 3.0F, 1.0F, new Dilation(0.0F))
                .uv(177, 236).cuboid(-7.5F, 3.1739F, -1.5F, 2.0F, 7.0F, 1.0F, new Dilation(0.0F))
                .uv(184, 236).cuboid(-7.5F, 3.1739F, 0.5F, 2.0F, 7.0F, 1.0F, new Dilation(0.0F))
                .uv(231, 206).cuboid(-8.5F, 3.1739F, -1.5F, 1.0F, 7.0F, 3.0F, new Dilation(0.0F))
                .uv(191, 236).cuboid(5.5F, 3.1739F, 0.5F, 2.0F, 7.0F, 1.0F, new Dilation(0.0F))
                .uv(204, 180).cuboid(-10.5F, -0.8261F, -2.5F, 21.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(155, 192).cuboid(-10.5F, -2.8261F, -2.5F, 21.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(155, 208).cuboid(-10.5F, -0.8261F, -0.5F, 21.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(208, 183).cuboid(-10.5F, -0.8261F, 1.5F, 21.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(124, 218).cuboid(5.5F, -3.8261F, -4.5F, 3.0F, 1.0F, 9.0F, new Dilation(0.0F))
                .uv(35, 238).cuboid(6.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(207, 225).cuboid(5.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new Dilation(0.0F))
                .uv(0, 219).cuboid(-8.5F, -3.8261F, -4.5F, 3.0F, 1.0F, 9.0F, new Dilation(0.0F))
                .uv(25, 227).cuboid(-6.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new Dilation(0.0F))
                .uv(40, 238).cuboid(-7.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(46, 227).cuboid(-8.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new Dilation(0.0F))
                .uv(238, 41).cuboid(-7.5F, -4.8261F, 3.5F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(238, 44).cuboid(6.5F, -4.8261F, 3.5F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(149, 227).cuboid(7.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, 0.0F, 37.0F));

        ModelPartData motor = main.addChild("motor", ModelPartBuilder.create().uv(155, 159).cuboid(-5.0F, -3.0F, -6.0F, 10.0F, 10.0F, 14.0F, new Dilation(0.0F))
                .uv(114, 237).cuboid(-5.0F, 5.0F, -7.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(119, 237).cuboid(4.0F, 5.0F, -7.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(129, 237).cuboid(4.0F, 5.0F, 8.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(124, 237).cuboid(-5.0F, 5.0F, 8.0F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(155, 115).cuboid(-6.0F, 7.0F, -8.0F, 12.0F, 1.0F, 18.0F, new Dilation(0.0F))
                .uv(155, 141).cuboid(-6.0F, -4.0F, -7.0F, 12.0F, 1.0F, 16.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -19.0F, -32.0F));

        ModelPartData motorLegs = motor.addChild("motorLegs", ModelPartBuilder.create().uv(210, 202).cuboid(-18.1923F, 0.7346F, -1.0F, 18.2066F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(212, 141).cuboid(-18.1923F, 0.7346F, 15.0F, 18.2066F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.origin(8.9858F, 11.2654F, -7.0F));

        motorLegs.addChild("cube_r1", ModelPartBuilder.create().uv(25, 223).cuboid(-5.2066F, 0.0F, -1.0F, 12.2066F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(210, 186).cuboid(1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 14.0F, new Dilation(0.0F))
                .uv(25, 219).cuboid(-5.2066F, 0.0F, 15.0F, 12.2066F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.9601F));

        motorLegs.addChild("cube_r2", ModelPartBuilder.create().uv(155, 211).cuboid(0.0F, 0.0F, -15.0F, 1.0F, 1.0F, 14.0F, new Dilation(0.0F))
                .uv(216, 128).cuboid(-5.2066F, 0.0F, -1.0F, 12.2066F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(224, 0).cuboid(-5.2066F, 0.0F, -17.0F, 12.2066F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-19.0F, 1.4692F, 16.0F, 0.0F, 0.0F, -0.9601F));

        motor.addChild("springs", ModelPartBuilder.create().uv(218, 236).cuboid(-4.0F, -20.0F, -24.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(209, 236).cuboid(2.0F, -20.0F, -24.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, 19.0F, 32.0F));
        return TexturedModelData.of(modelData, 512, 512);
    }

    public ModelParts getModelParts() {
        return modelParts;
    }

    public record ModelParts(
            ModelPart main,
            ModelPart table,
            ModelPart waterChannels,
            ModelPart motorConnection,
            ModelPart supports,
            ModelPart baseSupports,
            ModelPart legsSupports,
            ModelPart leg1,
            ModelPart leg2,
            ModelPart motor,
            ModelPart motorLegs,
            ModelPart springs
    ) {
    }
}