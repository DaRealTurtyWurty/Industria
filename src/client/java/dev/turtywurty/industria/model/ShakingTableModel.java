package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class ShakingTableModel extends Model<ShakingTableModel.ShakingTableModelRenderState> {
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/shaking_table.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("shaking_table"), "main");

    private final ModelParts modelParts;
    private final float tableOriginZ;

    public ShakingTableModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);

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
        this.tableOriginZ = table.z;
    }

    @Override
    public void setupAnim(ShakingTableModelRenderState state) {
        super.setupAnim(state);
        this.modelParts.table().z = this.tableOriginZ + state.shakeOffset;
    }

    public record ShakingTableModelRenderState(float shakeOffset) {
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition main = modelPartData.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition table = main.addOrReplaceChild("table", CubeListBuilder.create().texOffs(0, 0).addBox(-17.6F, -2.6F, -27.0F, 36.0F, 2.0F, 54.0F, new CubeDeformation(0.0F))
                .texOffs(155, 135).addBox(-17.6F, -6.6F, -27.0F, 36.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(155, 57).addBox(17.4F, -6.6F, -26.0F, 1.0F, 4.0F, 53.0F, new CubeDeformation(0.0F))
                .texOffs(54, 219).addBox(9.4F, -6.6F, 26.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(97, 229).addBox(6.4F, -0.6F, -21.0F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(186, 211).addBox(-7.6F, -0.6F, -21.0F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(123, 229).addBox(-7.6F, -0.6F, 16.0F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(110, 229).addBox(6.4F, -0.6F, 16.0F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.4F, -17.4F, 8.0F));

        table.addOrReplaceChild("waterChannels", CubeListBuilder.create().texOffs(200, 208).addBox(31.0F, -6.0F, 38.0F, 1.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(216, 115).addBox(32.0F, -4.0F, 39.0F, 2.0F, 0.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(148, 213).addBox(32.0F, -6.0F, 51.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(231, 221).addBox(31.0F, -8.0F, 42.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(170, 227).addBox(30.0F, -8.0F, 37.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(231, 217).addBox(31.0F, -8.0F, 37.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(67, 225).addBox(32.0F, -6.0F, 38.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(228, 225).addBox(35.0F, -8.0F, 37.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(210, 206).addBox(32.0F, -6.0F, 41.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(97, 237).addBox(32.0F, -6.0F, 35.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(181, 0).addBox(31.0F, -6.0F, 16.0F, 1.0F, 2.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(236, 53).addBox(31.0F, -8.0F, 19.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(212, 157).addBox(32.0F, -6.0F, 18.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(82, 229).addBox(30.0F, -8.0F, 14.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(198, 236).addBox(31.0F, -8.0F, 14.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(67, 229).addBox(35.0F, -8.0F, 14.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(181, 23).addBox(32.0F, -4.0F, 16.0F, 2.0F, 0.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offset(-16.6F, -0.6F, -42.0F));

        table.addOrReplaceChild("motorConnection", CubeListBuilder.create().texOffs(186, 219).addBox(-2.0F, -20.0F, -20.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(224, 37).addBox(-4.0F, -20.0F, -22.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(237, 132).addBox(-2.0F, -20.0F, -21.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(237, 136).addBox(1.0F, -20.0F, -21.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.4F, 17.4F, -8.0F));

        PartDefinition supports = main.addOrReplaceChild("supports", CubeListBuilder.create(), PartPose.offset(-0.2F, -1.0F, 6.4F));

        supports.addOrReplaceChild("baseSupports", CubeListBuilder.create().texOffs(149, 217).addBox(-10.0F, -2.0F, -86.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(181, 53).addBox(-14.0F, -2.0F, -85.0F, 26.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(155, 200).addBox(-14.0F, -2.0F, -87.0F, 26.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(181, 43).addBox(-14.0F, -2.0F, -99.0F, 26.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(181, 48).addBox(-14.0F, -2.0F, -115.0F, 26.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(204, 163).addBox(-14.0F, -2.0F, -108.0F, 26.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(204, 167).addBox(-14.0F, -2.0F, -105.0F, 26.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 135).addBox(12.0F, -2.0F, -115.0F, 2.0F, 2.0F, 75.0F, new CubeDeformation(0.0F))
                .texOffs(0, 57).addBox(-16.0F, -2.0F, -115.0F, 2.0F, 2.0F, 75.0F, new CubeDeformation(0.0F))
                .texOffs(204, 159).addBox(-14.0F, -2.0F, -50.0F, 26.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(155, 204).addBox(-14.0F, -2.0F, -48.0F, 26.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(109, 237).addBox(-10.0F, -2.0F, -49.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(149, 221).addBox(6.0F, -2.0F, -86.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(104, 237).addBox(6.0F, -2.0F, -49.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.2F, 1.0F, 68.6F));

        PartDefinition legsSupports = supports.addOrReplaceChild("legsSupports", CubeListBuilder.create(), PartPose.offset(-0.3F, -11.1739F, -16.9F));

        legsSupports.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(235, 234).addBox(5.5F, 3.1739F, -1.5F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 230).addBox(7.5F, 3.1739F, -1.5F, 1.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(212, 145).addBox(-8.5F, -0.8261F, -1.5F, 17.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(212, 151).addBox(-8.5F, -0.8261F, 0.5F, 17.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(74, 213).addBox(-8.5F, 0.1739F, -0.5F, 17.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(230, 132).addBox(-7.5F, 3.1739F, -1.5F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(18, 230).addBox(-7.5F, 3.1739F, 0.5F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(136, 229).addBox(-8.5F, 3.1739F, -1.5F, 1.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(228, 234).addBox(5.5F, 3.1739F, 0.5F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(204, 174).addBox(-10.5F, -0.8261F, -2.5F, 21.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(155, 184).addBox(-10.5F, -2.8261F, -2.5F, 21.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(204, 171).addBox(-10.5F, -0.8261F, -0.5F, 21.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(204, 177).addBox(-10.5F, -0.8261F, 1.5F, 21.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(99, 218).addBox(5.5F, -3.8261F, -4.5F, 3.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(30, 238).addBox(6.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(224, 26).addBox(5.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(74, 218).addBox(-8.5F, -3.8261F, -4.5F, 3.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(224, 4).addBox(-6.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(216, 132).addBox(-7.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(224, 15).addBox(-8.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(221, 132).addBox(-7.5F, -4.8261F, 3.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(25, 238).addBox(6.5F, -4.8261F, 3.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(186, 225).addBox(7.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        legsSupports.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(170, 236).addBox(5.5F, 3.1739F, -1.5F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(9, 230).addBox(7.5F, 3.1739F, -1.5F, 1.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 213).addBox(-8.5F, -0.8261F, -1.5F, 17.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(37, 213).addBox(-8.5F, -0.8261F, 0.5F, 17.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(111, 213).addBox(-8.5F, 0.1739F, -0.5F, 17.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(177, 236).addBox(-7.5F, 3.1739F, -1.5F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(184, 236).addBox(-7.5F, 3.1739F, 0.5F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(231, 206).addBox(-8.5F, 3.1739F, -1.5F, 1.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(191, 236).addBox(5.5F, 3.1739F, 0.5F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(204, 180).addBox(-10.5F, -0.8261F, -2.5F, 21.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(155, 192).addBox(-10.5F, -2.8261F, -2.5F, 21.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(155, 208).addBox(-10.5F, -0.8261F, -0.5F, 21.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(208, 183).addBox(-10.5F, -0.8261F, 1.5F, 21.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(124, 218).addBox(5.5F, -3.8261F, -4.5F, 3.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(35, 238).addBox(6.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(207, 225).addBox(5.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(0, 219).addBox(-8.5F, -3.8261F, -4.5F, 3.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(25, 227).addBox(-6.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(40, 238).addBox(-7.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(46, 227).addBox(-8.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(238, 41).addBox(-7.5F, -4.8261F, 3.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(238, 44).addBox(6.5F, -4.8261F, 3.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(149, 227).addBox(7.5F, -4.8261F, -4.5F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 37.0F));

        PartDefinition motor = main.addOrReplaceChild("motor", CubeListBuilder.create().texOffs(155, 159).addBox(-5.0F, -3.0F, -6.0F, 10.0F, 10.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(114, 237).addBox(-5.0F, 5.0F, -7.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(119, 237).addBox(4.0F, 5.0F, -7.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(129, 237).addBox(4.0F, 5.0F, 8.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(124, 237).addBox(-5.0F, 5.0F, 8.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(155, 115).addBox(-6.0F, 7.0F, -8.0F, 12.0F, 1.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(155, 141).addBox(-6.0F, -4.0F, -7.0F, 12.0F, 1.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -19.0F, -32.0F));

        PartDefinition motorLegs = motor.addOrReplaceChild("motorLegs", CubeListBuilder.create().texOffs(210, 202).addBox(-18.1923F, 0.7346F, -1.0F, 18.2066F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(212, 141).addBox(-18.1923F, 0.7346F, 15.0F, 18.2066F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(8.9858F, 11.2654F, -7.0F));

        motorLegs.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(25, 223).addBox(-5.2066F, 0.0F, -1.0F, 12.2066F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(210, 186).addBox(1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(25, 219).addBox(-5.2066F, 0.0F, 15.0F, 12.2066F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.9601F));

        motorLegs.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(155, 211).addBox(0.0F, 0.0F, -15.0F, 1.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(216, 128).addBox(-5.2066F, 0.0F, -1.0F, 12.2066F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(224, 0).addBox(-5.2066F, 0.0F, -17.0F, 12.2066F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-19.0F, 1.4692F, 16.0F, 0.0F, 0.0F, -0.9601F));

        motor.addOrReplaceChild("springs", CubeListBuilder.create().texOffs(218, 236).addBox(-4.0F, -20.0F, -24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(209, 236).addBox(2.0F, -20.0F, -24.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, 32.0F));
        return LayerDefinition.create(modelData, 512, 512);
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
