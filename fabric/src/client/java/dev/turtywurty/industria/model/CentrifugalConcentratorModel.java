package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class CentrifugalConcentratorModel extends Model<Float> {
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/centrifugal_concentrator.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("centrifugal_concentrator"), "main");

    private final ModelPart main;
    private final ModelPart legs;
    private final ModelPart dampeners;
    private final ModelPart connectingPlate;
    private final ModelPart outerRing;
    private final ModelPart inputTube;
    private final ModelPart outputTube;
    private final ModelPart outputTube2;
    private final ModelPart ringBody;
    private final ModelPart flywheel;
    private final ModelPart shaft;
    private final ModelPart cylinderBase;
    private final ModelPart cylinderTop;
    private final ModelPart drain;
    private final ModelPart bowl;

    public CentrifugalConcentratorModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);

        this.main = root.getChild("main");
        this.legs = main.getChild("legs");
        this.dampeners = main.getChild("dampeners");
        this.connectingPlate = main.getChild("connectingPlate");
        this.outerRing = main.getChild("outerRing");
        this.inputTube = main.getChild("inputTube");
        this.outputTube = main.getChild("outputTube");
        this.outputTube2 = main.getChild("outputTube2");
        this.ringBody = main.getChild("ringBody");
        this.flywheel = main.getChild("flywheel");
        this.shaft = flywheel.getChild("shaft");
        this.cylinderBase = main.getChild("cylinderBase");
        this.cylinderTop = main.getChild("cylinderTop");
        this.drain = main.getChild("drain");
        this.bowl = main.getChild("bowl");
    }

    public static LayerDefinition getTexturedModelData() {
        var modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition main = modelPartData.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition legs = main.addOrReplaceChild("legs", CubeListBuilder.create().texOffs(56, 193).addBox(16.932F, -24.0F, 35.5949F, 4.0F, 24.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(80, 38).addBox(-0.068F, -9.0F, -1.4051F, 38.0F, 2.0F, 2.0F, new CubeDeformation(-0.05F)), PartPose.offset(-18.932F, 0.0F, -13.5949F));

        PartDefinition cube_r1 = legs.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(80, 30).addBox(-42.0F, -2.0F, -1.0F, 42.0F, 2.0F, 2.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(37.932F, -7.0F, -0.4051F, 0.0F, 1.0908F, 0.0F));

        PartDefinition cube_r2 = legs.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(80, 34).addBox(0.0F, -1.999F, -1.0F, 41.0F, 2.0F, 2.0F, new CubeDeformation(-0.05F)), PartPose.offsetAndRotation(-0.068F, -7.0F, -0.4051F, 0.0F, -1.0908F, 0.0F));

        PartDefinition cube_r3 = legs.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(190, 98).addBox(-1.0F, -24.0F, -1.0F, 4.0F, 24.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r4 = legs.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(44, 193).addBox(-3.0F, -24.0F, -1.0F, 4.0F, 24.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(37.864F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition dampeners = main.addOrReplaceChild("dampeners", CubeListBuilder.create().texOffs(218, 24).addBox(-24.0F, -1.0F, -9.6667F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(216, 92).addBox(22.0F, -1.0F, -9.6667F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(44, 140).addBox(-2.0F, -1.0F, 14.3333F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -30.0F, 7.6667F));

        PartDefinition connectingPlate = main.addOrReplaceChild("connectingPlate", CubeListBuilder.create().texOffs(68, 207).addBox(-24.0F, -0.5F, -11.5F, 3.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(22, 210).addBox(21.0F, -0.5F, -11.5F, 3.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(52, 90).addBox(-2.0F, -0.5F, 13.5F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -31.5F, 7.5F));

        PartDefinition outerRing = main.addOrReplaceChild("outerRing", CubeListBuilder.create().texOffs(198, 213).addBox(-4.7739F, -2.4995F, -24.0F, 9.5478F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(214, 58).addBox(-4.7739F, -2.4995F, 22.0F, 9.5478F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(194, 15).addBox(22.0F, -2.4995F, -4.7739F, 2.0F, 5.0F, 9.5478F, new CubeDeformation(0.0F))
                .texOffs(200, 139).addBox(-24.0F, -2.4995F, -4.7739F, 2.0F, 5.0F, 9.5478F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -26.5005F, 0.0F));

        PartDefinition hexadecagon_r1 = outerRing.addOrReplaceChild("hexadecagon_r1", CubeListBuilder.create().texOffs(154, 200).addBox(-24.0F, -8.001F, -4.7739F, 2.0F, 5.0F, 9.5478F, new CubeDeformation(0.0F))
                .texOffs(178, 195).addBox(22.0F, -8.001F, -4.7739F, 2.0F, 5.0F, 9.5478F, new CubeDeformation(0.0F))
                .texOffs(214, 65).addBox(-4.7739F, -8.001F, 22.0F, 9.5478F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(214, 30).addBox(-4.7739F, -8.001F, -24.0F, 9.5478F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.5005F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition hexadecagon_r2 = outerRing.addOrReplaceChild("hexadecagon_r2", CubeListBuilder.create().texOffs(200, 124).addBox(-24.0F, -8.001F, -4.7739F, 2.0F, 5.0F, 9.5478F, new CubeDeformation(0.0F))
                .texOffs(194, 0).addBox(22.0F, -8.001F, -4.7739F, 2.0F, 5.0F, 9.5478F, new CubeDeformation(0.0F))
                .texOffs(214, 51).addBox(-4.7739F, -8.001F, 22.0F, 9.5478F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(210, 185).addBox(-4.7739F, -8.001F, -24.0F, 9.5478F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.5005F, 0.0F, 0.0F, 0.3927F, 0.0F));

        PartDefinition hexadecagon_r3 = outerRing.addOrReplaceChild("hexadecagon_r3", CubeListBuilder.create().texOffs(214, 72).addBox(-4.7739F, -8.0F, 22.0F, 9.5478F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(214, 37).addBox(-4.7739F, -8.0F, -24.0F, 9.5478F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.5005F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition hexadecagon_r4 = outerRing.addOrReplaceChild("hexadecagon_r4", CubeListBuilder.create().texOffs(214, 44).addBox(-4.7739F, -8.0F, 22.0F, 9.5478F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(202, 116).addBox(-4.7739F, -8.0F, -24.0F, 9.5478F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.5005F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition inputTube = main.addOrReplaceChild("inputTube", CubeListBuilder.create().texOffs(214, 169).addBox(-0.7956F, 1.0F, -4.0F, 1.5913F, 14.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(168, 215).addBox(-0.7956F, 1.0F, 2.0F, 1.5913F, 14.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(108, 216).addBox(2.0F, 1.0F, -0.7956F, 2.0F, 14.0F, 1.5913F, new CubeDeformation(0.0F))
                .texOffs(116, 216).addBox(-4.0F, 1.0F, -0.7956F, 2.0F, 14.0F, 1.5913F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -46.0F, 0.0F));

        PartDefinition hexadecagon_r5 = inputTube.addOrReplaceChild("hexadecagon_r5", CubeListBuilder.create().texOffs(100, 216).addBox(-4.0F, 0.999F, -0.7956F, 2.0F, 14.0F, 1.5913F, new CubeDeformation(0.0F))
                .texOffs(84, 216).addBox(2.0F, 0.999F, -0.7956F, 2.0F, 14.0F, 1.5913F, new CubeDeformation(0.0F))
                .texOffs(136, 205).addBox(-0.7956F, 0.999F, 2.0F, 1.5913F, 14.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(160, 92).addBox(-0.7956F, 0.999F, -4.0F, 1.5913F, 14.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition hexadecagon_r6 = inputTube.addOrReplaceChild("hexadecagon_r6", CubeListBuilder.create().texOffs(92, 216).addBox(-4.0F, 0.999F, -0.7956F, 2.0F, 14.0F, 1.5913F, new CubeDeformation(0.0F))
                .texOffs(76, 216).addBox(2.0F, 0.999F, -0.7956F, 2.0F, 14.0F, 1.5913F, new CubeDeformation(0.0F))
                .texOffs(160, 108).addBox(-0.7956F, 0.999F, 2.0F, 1.5913F, 14.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(100, 144).addBox(-0.7956F, 0.999F, -4.0F, 1.5913F, 14.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

        PartDefinition hexadecagon_r7 = inputTube.addOrReplaceChild("hexadecagon_r7", CubeListBuilder.create().texOffs(68, 216).addBox(-0.7956F, 1.0F, 2.0F, 1.5913F, 14.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(152, 215).addBox(-0.7956F, 1.0F, -4.0F, 1.5913F, 14.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition hexadecagon_r8 = inputTube.addOrReplaceChild("hexadecagon_r8", CubeListBuilder.create().texOffs(160, 215).addBox(-0.7956F, 1.0F, 2.0F, 1.5913F, 14.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(144, 205).addBox(-0.7956F, 1.0F, -4.0F, 1.5913F, 14.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition outputTube = main.addOrReplaceChild("outputTube", CubeListBuilder.create().texOffs(202, 98).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(66, 90).addBox(-3.0F, -3.0F, 4.0F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(202, 107).addBox(-3.0F, -4.0F, -4.0F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(178, 210).addBox(-4.0F, -3.0F, -4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(194, 30).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(15.0F, -17.0F, 0.0F));

        PartDefinition outputTube2 = main.addOrReplaceChild("outputTube2", CubeListBuilder.create().texOffs(202, 195).addBox(-2.4F, 1.4F, -4.0F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(92, 201).addBox(-2.4F, -1.6F, 4.0F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(202, 204).addBox(-2.4F, -2.6F, -4.0F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 211).addBox(-3.4F, -1.6F, -4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(48, 219).addBox(-2.4F, -1.6F, -5.0F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.6F, -18.4F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition ringBody = main.addOrReplaceChild("ringBody", CubeListBuilder.create().texOffs(66, 160).addBox(-4.1772F, -15.0F, -21.0F, 8.3543F, 30.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(110, 172).addBox(-4.1772F, -15.0F, 18.0F, 8.3543F, 30.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(22, 178).addBox(18.0F, -15.0F, -4.1772F, 3.0F, 24.0F, 8.3543F, new CubeDeformation(0.0F))
                .texOffs(92, 205).addBox(18.0F, 12.0F, -4.1772F, 3.0F, 3.0F, 8.3543F, new CubeDeformation(0.0F))
                .texOffs(114, 205).addBox(-21.0F, 12.0F, -4.1772F, 3.0F, 3.0F, 8.3543F, new CubeDeformation(0.0F))
                .texOffs(178, 125).addBox(-21.0F, -15.0F, -4.1772F, 3.0F, 24.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -29.0F, 0.0F));

        PartDefinition hexadecagon_r9 = ringBody.addOrReplaceChild("hexadecagon_r9", CubeListBuilder.create().texOffs(22, 140).addBox(-21.0F, -15.0F, -4.1772F, 3.0F, 30.0F, 8.3543F, new CubeDeformation(0.001F))
                .texOffs(134, 134).addBox(18.0F, -15.0F, -4.1772F, 3.0F, 30.0F, 8.3543F, new CubeDeformation(0.001F))
                .texOffs(132, 172).addBox(-4.1772F, -15.0F, 18.0F, 8.3543F, 30.0F, 3.0F, new CubeDeformation(0.001F))
                .texOffs(88, 160).addBox(-4.1772F, -15.0F, -21.0F, 8.3543F, 30.0F, 3.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition hexadecagon_r10 = ringBody.addOrReplaceChild("hexadecagon_r10", CubeListBuilder.create().texOffs(0, 140).addBox(-21.0F, -15.0F, -4.1772F, 3.0F, 30.0F, 8.3543F, new CubeDeformation(0.001F))
                .texOffs(112, 134).addBox(18.0F, -15.0F, -4.1772F, 3.0F, 30.0F, 8.3543F, new CubeDeformation(0.001F))
                .texOffs(172, 0).addBox(-4.1772F, -15.0F, 18.0F, 8.3543F, 30.0F, 3.0F, new CubeDeformation(0.001F))
                .texOffs(44, 160).addBox(-4.1772F, -15.0F, -21.0F, 8.3543F, 30.0F, 3.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

        PartDefinition hexadecagon_r11 = ringBody.addOrReplaceChild("hexadecagon_r11", CubeListBuilder.create().texOffs(0, 178).addBox(-4.1772F, -15.0F, 18.0F, 8.3543F, 30.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(156, 167).addBox(-4.1772F, -15.0F, -21.0F, 8.3543F, 30.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition hexadecagon_r12 = ringBody.addOrReplaceChild("hexadecagon_r12", CubeListBuilder.create().texOffs(168, 92).addBox(-4.1772F, -15.0F, 18.0F, 8.3543F, 30.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(156, 134).addBox(-4.1772F, -15.0F, -21.0F, 8.3543F, 30.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition flywheel = main.addOrReplaceChild("flywheel", CubeListBuilder.create().texOffs(8, 222).addBox(-1.6569F, -1.0005F, -4.0F, 3.3137F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(222, 172).addBox(-1.6569F, -1.0005F, 3.0F, 3.3137F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(176, 221).addBox(3.0F, -1.0005F, -1.6569F, 1.0F, 2.0F, 3.3137F, new CubeDeformation(0.0F))
                .texOffs(0, 222).addBox(-4.0F, -1.0005F, -1.6569F, 1.0F, 2.0F, 3.3137F, new CubeDeformation(0.0F))
                .texOffs(20, 219).addBox(-0.5F, -1.0005F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(212, 220).addBox(-3.0F, -0.9985F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0005F, 0.0F));

        PartDefinition cube_r5 = flywheel.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(132, 221).addBox(-3.0F, -1.0F, -0.5F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(34, 219).addBox(-0.5F, -1.002F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(184, 221).addBox(-4.0F, -1.0F, -1.6569F, 1.0F, 2.0F, 3.3137F, new CubeDeformation(0.0F))
                .texOffs(168, 125).addBox(3.0F, -1.0F, -1.6569F, 1.0F, 2.0F, 3.3137F, new CubeDeformation(0.0F))
                .texOffs(222, 169).addBox(-1.6569F, -1.0F, 3.0F, 3.3137F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(168, 130).addBox(-1.6569F, -1.0F, -4.0F, 3.3137F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0005F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition shaft = flywheel.addOrReplaceChild("shaft", CubeListBuilder.create().texOffs(124, 216).addBox(-1.0F, -11.8384F, -1.1402F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(222, 175).addBox(-1.0F, 0.1616F, -1.1402F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(48, 116).addBox(-0.5F, 0.1616F, -0.6402F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.8389F, 0.1402F));

        PartDefinition cube_r6 = shaft.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(108, 144).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.8687F, 0.774F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cylinderBase = main.addOrReplaceChild("cylinderBase", CubeListBuilder.create().texOffs(0, 0).addBox(-15.0F, -1.0F, -14.0F, 30.0F, 2.0F, 28.0F, new CubeDeformation(0.0F))
                .texOffs(80, 42).addBox(15.0F, -1.0F, -12.0F, 2.0F, 2.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(80, 68).addBox(-17.0F, -1.0F, -12.0F, 2.0F, 2.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(188, 42).addBox(-18.0F, -1.0F, -6.0F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(188, 56).addBox(17.0F, -1.0F, -6.0F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(44, 144).addBox(-13.0F, -1.0F, -16.0F, 26.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 116).addBox(-11.0F, -1.0F, -18.0F, 22.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 120).addBox(-11.0F, -1.0F, 16.0F, 22.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(44, 148).addBox(-13.0F, -1.0F, 14.0F, 26.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -15.001F, 0.0F));

        PartDefinition cylinderTop = main.addOrReplaceChild("cylinderTop", CubeListBuilder.create().texOffs(166, 34).addBox(-11.0F, -1.999F, 20.0F, 22.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(44, 156).addBox(-13.0F, -1.999F, 18.0F, 26.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(178, 157).addBox(-3.0F, -1.999F, 6.001F, 6.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(178, 171).addBox(-3.0F, -1.999F, -10.001F, 6.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 30).addBox(-15.0F, -1.999F, -10.0F, 12.0F, 2.0F, 28.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).addBox(3.0F, -1.999F, -10.0F, 12.0F, 2.0F, 28.0F, new CubeDeformation(0.0F))
                .texOffs(52, 94).addBox(15.0F, -1.999F, -8.0F, 2.0F, 2.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(190, 84).addBox(17.0F, -1.999F, -2.0F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(188, 70).addBox(-18.0F, -1.999F, -2.0F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 90).addBox(-17.0F, -1.999F, -8.0F, 2.0F, 2.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(44, 152).addBox(-13.0F, -1.999F, -12.0F, 26.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(160, 38).addBox(-11.0F, -1.999F, -14.0F, 22.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -42.0F, -4.0F));

        PartDefinition drain = main.addOrReplaceChild("drain", CubeListBuilder.create().texOffs(218, 0).addBox(-0.5967F, -1.0F, -3.0F, 1.1935F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(210, 192).addBox(-3.0F, -1.0F, -0.5967F, 6.0F, 2.0F, 1.1935F, new CubeDeformation(0.0F))
                .texOffs(178, 185).addBox(-4.0F, 3.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(214, 154).addBox(4.0F, 1.0F, -4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(214, 79).addBox(-4.0F, 2.0F, -6.0F, 8.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(214, 164).addBox(-4.0F, 2.0F, 4.0F, 8.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(68, 193).addBox(-6.0F, 1.0F, -5.0F, 2.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(146, 221).addBox(-6.0F, 0.0F, 5.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(62, 219).addBox(-6.0F, 0.0F, -6.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -22.0F, 0.0F));

        PartDefinition hexadecagon_r13 = drain.addOrReplaceChild("hexadecagon_r13", CubeListBuilder.create().texOffs(198, 220).addBox(-3.0F, -1.001F, -0.5967F, 6.0F, 2.0F, 1.1935F, new CubeDeformation(0.0F))
                .texOffs(218, 8).addBox(-0.5967F, -1.001F, -3.0F, 1.1935F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition hexadecagon_r14 = drain.addOrReplaceChild("hexadecagon_r14", CubeListBuilder.create().texOffs(200, 154).addBox(-3.0F, -1.001F, -0.5967F, 6.0F, 2.0F, 1.1935F, new CubeDeformation(0.0F))
                .texOffs(216, 84).addBox(-0.5967F, -1.001F, -3.0F, 1.1935F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

        PartDefinition hexadecagon_r15 = drain.addOrReplaceChild("hexadecagon_r15", CubeListBuilder.create().texOffs(218, 16).addBox(-0.5967F, -1.0F, -3.0F, 1.1935F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition hexadecagon_r16 = drain.addOrReplaceChild("hexadecagon_r16", CubeListBuilder.create().texOffs(92, 193).addBox(-0.5967F, -1.0F, -3.0F, 1.1935F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bowl = main.addOrReplaceChild("bowl", CubeListBuilder.create(), PartPose.offset(0.0F, -27.2218F, 0.0F));

        PartDefinition cube_r7 = bowl.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(132, 82).addBox(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.3176F, 0.3536F, 10.4237F, 1.0409F, 0.7119F, 1.2053F));

        PartDefinition cube_r8 = bowl.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(132, 72).addBox(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.3536F, 11.2825F, 1.5708F, 0.7854F, 1.5708F));

        PartDefinition cube_r9 = bowl.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(132, 62).addBox(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3176F, 0.3536F, 10.4237F, 2.1007F, 0.7119F, 1.9363F));

        PartDefinition cube_r10 = bowl.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(132, 52).addBox(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9779F, 0.3536F, 7.9779F, 2.5261F, 0.5236F, 2.1863F));

        PartDefinition cube_r11 = bowl.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(132, 42).addBox(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.4237F, 0.3536F, 4.3176F, 2.8567F, 0.274F, 2.3166F));

        PartDefinition cube_r12 = bowl.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 130).addBox(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.2825F, 0.3536F, 0.0F, -3.1416F, 0.0F, 2.3562F));

        PartDefinition cube_r13 = bowl.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(112, 124).addBox(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.4237F, 0.3536F, -4.3176F, -2.8567F, -0.274F, 2.3166F));

        PartDefinition cube_r14 = bowl.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(56, 124).addBox(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.9779F, 0.3536F, -7.9779F, -2.5261F, -0.5236F, 2.1863F));

        PartDefinition cube_r15 = bowl.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 120).addBox(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3176F, 0.3536F, -10.4237F, -2.1007F, -0.7119F, 1.9363F));

        PartDefinition cube_r16 = bowl.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(116, 20).addBox(-11.0991F, -38.5427F, -41.829F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-37.6519F, 27.3239F, -37.4055F, -1.5708F, -0.7854F, 1.5708F));

        PartDefinition cube_r17 = bowl.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(116, 10).addBox(-10.5F, -1.0F, -4.1771F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.3176F, 0.3536F, -10.4237F, -1.0409F, -0.7119F, 1.2053F));

        PartDefinition cube_r18 = bowl.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(116, 0).addBox(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.9779F, 0.3536F, -7.9779F, -0.6155F, -0.5236F, 0.9553F));

        PartDefinition cube_r19 = bowl.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(104, 114).addBox(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.4237F, 0.3536F, -4.3176F, -0.2849F, -0.274F, 0.8249F));

        PartDefinition cube_r20 = bowl.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(104, 104).addBox(-10.5F, -1.0F, -4.1771F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.4237F, 0.3536F, 4.3176F, 0.2849F, 0.274F, 0.8249F));

        PartDefinition cube_r21 = bowl.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(104, 94).addBox(-5.0F, -2.0F, -4.0F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.8787F, -2.8284F, -0.1772F, 0.0F, 0.0F, 0.7854F));

        PartDefinition cube_r22 = bowl.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(56, 134).addBox(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.9779F, 0.3536F, 7.9779F, 0.6155F, 0.5236F, 0.9553F));
        return LayerDefinition.create(modelData, 256, 256);
    }

    @Override
    public void setupAnim(Float bowlRotation) {
        super.setupAnim(bowlRotation);
        this.cylinderTop.skipDraw = true;
        this.bowl.yRot = bowlRotation;
    }
}