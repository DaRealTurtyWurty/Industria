package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class OilPumpJackModel extends Model<OilPumpJackModel.OilPumpJackModelRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("oil_pump_jack"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/oil_pump_jack.png");

    private final OilPumpJackParts parts;

    public OilPumpJackModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);

        ModelPart main = root.getChild("main");
        ModelPart counterWeights = main.getChild("counterWeights");
        ModelPart leftCounterWeight = counterWeights.getChild("leftCounterWeight");
        ModelPart bone9 = leftCounterWeight.getChild("bone9");
        ModelPart bone10 = leftCounterWeight.getChild("bone10");
        ModelPart bone11 = leftCounterWeight.getChild("bone11");
        ModelPart bone12 = leftCounterWeight.getChild("bone12");
        ModelPart bone13 = leftCounterWeight.getChild("bone13");
        ModelPart bone14 = leftCounterWeight.getChild("bone14");
        ModelPart rightCounterWeight = counterWeights.getChild("rightCounterWeight");
        ModelPart bone15 = rightCounterWeight.getChild("bone15");
        ModelPart bone16 = rightCounterWeight.getChild("bone16");
        ModelPart bone17 = rightCounterWeight.getChild("bone17");
        ModelPart bone18 = rightCounterWeight.getChild("bone18");
        ModelPart bone19 = rightCounterWeight.getChild("bone19");
        ModelPart bone20 = rightCounterWeight.getChild("bone20");
        ModelPart pitmanArm = counterWeights.getChild("pitmanArm");
        ModelPart pitmanArmLeftPivot = pitmanArm.getChild("pitmanArmLeftPivot");
        ModelPart pitmanArmRightPivot = pitmanArm.getChild("pitmanArmRightPivot");
        ModelPart staticParts = main.getChild("staticParts");
        ModelPart supports = staticParts.getChild("supports");
        ModelPart bone28 = supports.getChild("bone28");
        ModelPart bone29 = supports.getChild("bone29");
        ModelPart bone30 = supports.getChild("bone30");
        ModelPart bone31 = supports.getChild("bone31");
        ModelPart bone32 = supports.getChild("bone32");
        ModelPart bone33 = supports.getChild("bone33");
        ModelPart bone34 = supports.getChild("bone34");
        ModelPart motor = staticParts.getChild("motor");
        ModelPart bone42 = motor.getChild("bone42");
        ModelPart bone41 = bone42.getChild("bone41");
        ModelPart bone39 = bone42.getChild("bone39");
        ModelPart bone40 = bone42.getChild("bone40");
        ModelPart bone38 = bone42.getChild("bone38");
        ModelPart bone43 = motor.getChild("bone43");
        ModelPart bone44 = bone43.getChild("bone44");
        ModelPart bone45 = bone43.getChild("bone45");
        ModelPart bone46 = bone43.getChild("bone46");
        ModelPart bone47 = bone43.getChild("bone47");
        ModelPart frame = staticParts.getChild("frame");
        ModelPart bone25 = frame.getChild("bone25");
        ModelPart bone26 = frame.getChild("bone26");
        ModelPart bone27 = frame.getChild("bone27");
        ModelPart belt = staticParts.getChild("belt");
        ModelPart wheel = belt.getChild("wheel");
        ModelPart bone21 = wheel.getChild("bone21");
        ModelPart bone22 = wheel.getChild("bone22");
        ModelPart bone5 = wheel.getChild("bone5");
        ModelPart bone36 = wheel.getChild("bone36");
        ModelPart bone7 = wheel.getChild("bone7");
        ModelPart bone35 = wheel.getChild("bone35");
        ModelPart bone23 = wheel.getChild("bone23");
        ModelPart bone24 = wheel.getChild("bone24");
        ModelPart bone = belt.getChild("bone");
        ModelPart bone2 = belt.getChild("bone2");
        ModelPart bone3 = belt.getChild("bone3");
        ModelPart bone4 = belt.getChild("bone4");
        ModelPart bone6 = belt.getChild("bone6");
        ModelPart bone8 = belt.getChild("bone8");
        ModelPart arm = main.getChild("arm");
        ModelPart head = arm.getChild("head");
        ModelPart beam = arm.getChild("beam");
        ModelPart bone37 = beam.getChild("bone37");
        ModelPart attachmentA = main.getChild("attachmentA");
        ModelPart attachmentB = main.getChild("attachmentB");
        ModelPart attachmentC = main.getChild("attachmentC");
        ModelPart attachmentD = main.getChild("attachmentD");
        ModelPart attachmentE = main.getChild("attachmentE");

        attachmentA.skipDraw = true;
        attachmentB.skipDraw = true;
        attachmentC.skipDraw = true;
        attachmentD.skipDraw = true;
        attachmentE.skipDraw = true;

        this.parts = new OilPumpJackParts(main, wheel, counterWeights, pitmanArm, arm,
                attachmentA, attachmentB, attachmentC, attachmentD, attachmentE);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition main = modelPartData.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition counterWeights = main.addOrReplaceChild("counterWeights", CubeListBuilder.create(), PartPose.offset(0.0F, -42.0F, 24.0F));

        PartDefinition leftCounterWeight = counterWeights.addOrReplaceChild("leftCounterWeight", CubeListBuilder.create().texOffs(17, 230).addBox(0.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(242, 0).addBox(3.998F, -5.002F, -40.002F, 2.004F, 10.004F, 44.004F, new CubeDeformation(0.002F))
                .texOffs(79, 198).addBox(4.0F, -19.0F, -24.0F, 2.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(180, 223).addBox(4.0F, -20.027F, -22.4F, 2.0F, 1.027F, 6.4F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, 0.0F, 1.0F));

        PartDefinition bone9 = leftCounterWeight.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(47, 97).addBox(-1.0F, -6.0F, -8.0F, 2.0F, 12.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -7.2716F, -28.2614F, 0.8727F, 0.0F, 0.0F));

        PartDefinition bone10 = leftCounterWeight.addOrReplaceChild("bone10", CubeListBuilder.create().texOffs(100, 201).addBox(-1.002F, -6.002F, -3.002F, 2.004F, 12.004F, 6.004F, new CubeDeformation(0.002F)), PartPose.offsetAndRotation(5.0F, -13.3199F, -22.2839F, 0.48F, 0.0F, 0.0F));

        PartDefinition bone11 = leftCounterWeight.addOrReplaceChild("bone11", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, 7.2365F, -28.2614F, 0.8727F, 0.0F, -3.1416F));

        PartDefinition cube_r1 = bone11.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(47, 0).addBox(-1.0F, -5.5363F, -15.6012F, 2.0F, 12.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.4637F, 7.6012F, 0.0F, 0.0F, 0.0F));

        PartDefinition bone12 = leftCounterWeight.addOrReplaceChild("bone12", CubeListBuilder.create().texOffs(0, 198).addBox(-1.0F, -5.0F, -4.0F, 2.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 13.9649F, -20.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition bone13 = leftCounterWeight.addOrReplaceChild("bone13", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, 13.2848F, -22.2839F, 0.48F, 0.0F, -3.1416F));

        PartDefinition cube_r2 = bone13.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(134, 201).addBox(-1.001F, -5.2685F, -1.7512F, 2.002F, 12.002F, 6.002F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -0.7325F, -1.2498F, 0.0F, 0.0F, 0.0F));

        PartDefinition bone14 = leftCounterWeight.addOrReplaceChild("bone14", CubeListBuilder.create().texOffs(197, 223).addBox(-1.0F, -0.5135F, -3.2F, 2.0F, 1.027F, 6.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 19.4784F, -19.2F, 0.0F, 0.0F, -3.1416F));

        PartDefinition rightCounterWeight = counterWeights.addOrReplaceChild("rightCounterWeight", CubeListBuilder.create().texOffs(0, 253).addBox(-6.002F, -5.002F, -40.002F, 2.004F, 10.004F, 44.004F, new CubeDeformation(0.002F))
                .texOffs(21, 198).addBox(-6.0F, -19.0F, -24.0F, 2.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(207, 19).addBox(-6.0F, -20.027F, -22.4F, 2.0F, 1.027F, 6.4F, new CubeDeformation(0.0F))
                .texOffs(0, 230).addBox(-4.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 0.0F, 1.0F));

        PartDefinition bone15 = rightCounterWeight.addOrReplaceChild("bone15", CubeListBuilder.create().texOffs(0, 97).addBox(-1.0F, -6.0F, -8.0F, 2.0F, 12.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -7.2716F, -28.2614F, 0.8727F, 0.0F, 0.0F));

        PartDefinition bone16 = rightCounterWeight.addOrReplaceChild("bone16", CubeListBuilder.create().texOffs(151, 201).addBox(-1.001F, -6.001F, -3.001F, 2.002F, 12.002F, 6.002F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(-5.0F, -13.3199F, -22.2839F, 0.48F, 0.0F, 0.0F));

        PartDefinition bone17 = rightCounterWeight.addOrReplaceChild("bone17", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.0F, 7.2365F, -28.2614F, 0.8727F, 0.0F, -3.1416F));

        PartDefinition cube_r3 = bone17.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -5.5363F, -15.6012F, 2.0F, 12.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.4637F, 7.6012F, 0.0F, 0.0F, 0.0F));

        PartDefinition bone18 = rightCounterWeight.addOrReplaceChild("bone18", CubeListBuilder.create().texOffs(186, 66).addBox(-1.0F, -5.0F, -4.0F, 2.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 13.9649F, -20.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition bone19 = rightCounterWeight.addOrReplaceChild("bone19", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.0F, 13.2848F, -22.2839F, 0.48F, 0.0F, -3.1416F));

        PartDefinition cube_r4 = bone19.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(117, 201).addBox(-1.001F, -5.2685F, -1.7512F, 2.002F, 12.002F, 6.002F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, -0.7325F, -1.2498F, 0.0F, 0.0F, 0.0F));

        PartDefinition bone20 = rightCounterWeight.addOrReplaceChild("bone20", CubeListBuilder.create().texOffs(153, 221).addBox(-1.0F, -0.5135F, -3.2F, 2.0F, 1.027F, 6.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 19.4784F, -19.2F, 0.0F, 0.0F, -3.1416F));

        PartDefinition pitmanArm = counterWeights.addOrReplaceChild("pitmanArm", CubeListBuilder.create().texOffs(0, 76).addBox(-14.0F, -44.5F, -2.7778F, 28.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 47).addBox(-18.0F, -50.5F, -2.7778F, 36.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(58, 158).addBox(-6.0F, -52.5F, -4.7778F, 12.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(49, 170).addBox(-6.0F, -58.5F, -2.7778F, 12.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(138, 220).addBox(-4.0F, -58.5F, -4.7778F, 8.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(37, 170).addBox(-20.0F, -50.5F, -2.7778F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-22.0F, -50.5F, -0.7778F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(208, 225).addBox(-20.0F, -52.5F, -2.7778F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(216, 0).addBox(-6.0F, -52.5F, -2.7778F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(58, 149).addBox(-6.0F, -52.5F, 3.2222F, 12.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(190, 193).addBox(-6.0F, -52.5F, 1.2222F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(144, 272).addBox(18.0F, -44.5F, -2.7778F, 2.0F, 48.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(47, 0).addBox(18.0F, -50.5F, -0.7778F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(97, 173).addBox(18.0F, -50.5F, -2.7778F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(132, 227).addBox(18.0F, -52.5F, -2.7778F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(127, 272).addBox(-20.0F, -44.5F, -2.7778F, 2.0F, 48.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(174, 69).addBox(18.0F, -50.5F, 1.2222F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 167).addBox(-20.0F, -50.5F, 1.2222F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, -10.2222F));

        PartDefinition pitmanArmLeftPivot = pitmanArm.addOrReplaceChild("pitmanArmLeftPivot", CubeListBuilder.create().texOffs(121, 220).addBox(0.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(16.0F, 0.5F, 0.2222F));

        PartDefinition pitmanArmRightPivot = pitmanArm.addOrReplaceChild("pitmanArmRightPivot", CubeListBuilder.create().texOffs(68, 97).addBox(-2.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-16.0F, 0.5F, 0.2222F));

        PartDefinition staticParts = main.addOrReplaceChild("staticParts", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition supports = staticParts.addOrReplaceChild("supports", CubeListBuilder.create().texOffs(0, 167).addBox(-6.001F, 2.099F, -6.001F, 12.002F, 4.002F, 12.002F, new CubeDeformation(0.001F))
                .texOffs(37, 151).addBox(6.0F, 2.1F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(142, 100).addBox(-8.0F, 2.1F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(190, 183).addBox(-6.0F, 2.1F, -8.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(190, 178).addBox(-6.0F, 2.1F, 6.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(137, 19).addBox(-4.0F, -4.9F, -3.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(68, 0).addBox(4.0F, -6.9F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(224, 5).addBox(-4.0F, -4.9F, -5.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(219, 193).addBox(-4.0F, -4.9F, 3.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(21, 0).addBox(-6.0F, -6.9F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -96.1F, -28.0F));

        PartDefinition bone28 = supports.addOrReplaceChild("bone28", CubeListBuilder.create().texOffs(247, 100).addBox(-4.0F, -31.0F, -4.0F, 8.0F, 62.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 31.9853F, 15.7679F, 0.5236F, 0.0F, 0.0F));

        PartDefinition bone29 = supports.addOrReplaceChild("bone29", CubeListBuilder.create().texOffs(0, 126).addBox(-6.0F, -1.0F, -2.0F, 12.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.8443F, -3.7786F, -0.3491F, 0.0F, 0.0F));

        PartDefinition bone30 = supports.addOrReplaceChild("bone30", CubeListBuilder.create().texOffs(0, 69).addBox(-18.0F, -2.0F, -1.0F, 36.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 76.0825F, -14.7497F, -0.1745F, 0.0F, 0.0F));

        PartDefinition bone31 = supports.addOrReplaceChild("bone31", CubeListBuilder.create().texOffs(118, 167).addBox(-10.0F, -2.0F, -1.0F, 20.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 48.6729F, -8.952F, -0.1745F, 0.0F, 0.0F));

        PartDefinition bone32 = supports.addOrReplaceChild("bone32", CubeListBuilder.create().texOffs(182, 30).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 26.0483F, -4.8104F, -0.1745F, 0.0F, 0.0F));

        PartDefinition bone33 = supports.addOrReplaceChild("bone33", CubeListBuilder.create().texOffs(110, 268).addBox(-2.0F, -44.0F, -2.0F, 4.0F, 88.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0476F, 47.7185F, -8.7068F, -0.1745F, 0.0F, 0.2182F));

        PartDefinition bone34 = supports.addOrReplaceChild("bone34", CubeListBuilder.create().texOffs(93, 253).addBox(-2.0F, -44.0F, -2.0F, 4.0F, 88.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0476F, 47.7185F, -8.7068F, -0.1745F, 0.0F, -0.2182F));

        PartDefinition motor = staticParts.addOrReplaceChild("motor", CubeListBuilder.create().texOffs(182, 0).addBox(2.6296F, 5.1111F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 29).addBox(-11.3704F, 3.1111F, 2.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(97, 166).addBox(-11.3704F, 5.1111F, -8.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(47, 29).addBox(-11.3704F, 3.1111F, -4.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(97, 126).addBox(-9.3704F, 3.1111F, -2.0F, 12.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(219, 183).addBox(-9.3704F, 1.1111F, 2.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(118, 174).addBox(-9.3704F, -2.8889F, 4.0F, 12.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(209, 218).addBox(-9.3704F, -4.8889F, 2.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(47, 126).addBox(-9.3704F, -6.8889F, -2.0F, 12.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(39, 149).addBox(2.6296F, -4.8889F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(97, 166).addBox(2.6296F, 1.1111F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(79, 217).addBox(2.6296F, -2.8889F, -4.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(219, 178).addBox(-9.3704F, 1.1111F, -4.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(163, 100).addBox(-9.3704F, -2.8889F, -6.0F, 12.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(180, 218).addBox(-9.3704F, -4.8889F, -4.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(21, 217).addBox(-11.3704F, -2.8889F, -4.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(47, 106).addBox(-11.3704F, -4.8889F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(142, 109).addBox(-11.3704F, 1.1111F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(47, 97).addBox(-13.3704F, -2.8889F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 97).addBox(4.6296F, -2.8889F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(142, 100).addBox(8.6296F, -2.8889F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.3704F, -29.1111F, 56.0F));

        PartDefinition bone42 = motor.addOrReplaceChild("bone42", CubeListBuilder.create().texOffs(0, 217).addBox(-1.0F, -2.0F, -4.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 106).addBox(-1.0F, -4.0F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(74, 77).addBox(-1.0F, 2.0F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(11.6296F, -0.8889F, 0.0F));

        PartDefinition bone41 = bone42.addOrReplaceChild("bone41", CubeListBuilder.create().texOffs(34, 217).addBox(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.01F, 2.2913F, -2.2937F, 2.3562F, 0.0F, 0.0F));

        PartDefinition bone39 = bone42.addOrReplaceChild("bone39", CubeListBuilder.create().texOffs(111, 201).addBox(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.01F, -2.2941F, -2.2913F, 0.7854F, 0.0F, 0.0F));

        PartDefinition bone40 = bone42.addOrReplaceChild("bone40", CubeListBuilder.create().texOffs(191, 223).addBox(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.01F, 2.2937F, 2.2917F, -2.3562F, 0.0F, 0.0F));

        PartDefinition bone38 = bone42.addOrReplaceChild("bone38", CubeListBuilder.create().texOffs(128, 201).addBox(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.01F, -2.2917F, 2.2941F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone43 = motor.addOrReplaceChild("bone43", CubeListBuilder.create().texOffs(203, 0).addBox(-1.0F, -2.0F, -4.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(47, 9).addBox(-1.0F, -4.0F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 9).addBox(-1.0F, 2.0F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(7.6296F, -0.8889F, 0.0F));

        PartDefinition bone44 = bone43.addOrReplaceChild("bone44", CubeListBuilder.create().texOffs(13, 217).addBox(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.01F, 2.2913F, -2.2937F, 2.3562F, 0.0F, 0.0F));

        PartDefinition bone45 = bone43.addOrReplaceChild("bone45", CubeListBuilder.create().texOffs(199, 66).addBox(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.01F, -2.2941F, -2.2913F, 0.7854F, 0.0F, 0.0F));

        PartDefinition bone46 = bone43.addOrReplaceChild("bone46", CubeListBuilder.create().texOffs(145, 201).addBox(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.01F, 2.2937F, 2.2917F, -2.3562F, 0.0F, 0.0F));

        PartDefinition bone47 = bone43.addOrReplaceChild("bone47", CubeListBuilder.create().texOffs(34, 198).addBox(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.01F, -2.2917F, 2.2941F, -0.7854F, 0.0F, 0.0F));

        PartDefinition frame = staticParts.addOrReplaceChild("frame", CubeListBuilder.create().texOffs(180, 201).addBox(-32.0004F, 15.7572F, -67.3034F, 64.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).addBox(-18.0004F, 13.7572F, -39.3034F, 36.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-12.0004F, 15.7572F, -59.3034F, 24.0F, 8.0F, 88.0F, new CubeDeformation(0.0F))
                .texOffs(0, 144).addBox(-6.0004F, 15.7572F, 28.6966F, 12.0F, 8.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(242, 55).addBox(-12.0004F, 1.7572F, 24.6966F, 24.0F, 2.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(180, 226).addBox(7.9996F, -8.2428F, -21.3034F, 4.0F, 24.0F, 46.0F, new CubeDeformation(0.0F))
                .texOffs(125, 201).addBox(-12.0004F, -8.2428F, -21.3034F, 4.0F, 24.0F, 46.0F, new CubeDeformation(0.0F))
                .texOffs(0, 198).addBox(-8.0004F, -8.2428F, -21.3034F, 16.0F, 8.0F, 46.0F, new CubeDeformation(0.0F))
                .texOffs(0, 97).addBox(5.9956F, -16.2428F, -17.3034F, 4.0F, 8.0F, 38.0F, new CubeDeformation(0.0F))
                .texOffs(137, 0).addBox(5.9976F, -22.2428F, -15.3034F, 4.0F, 6.0F, 36.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-9.9964F, -16.2428F, -17.3034F, 4.0F, 8.0F, 38.0F, new CubeDeformation(0.0F))
                .texOffs(137, 0).addBox(-5.9964F, -22.2428F, 16.6966F, 12.0F, 14.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(97, 100).addBox(-9.9944F, -22.2428F, -15.3034F, 4.0F, 6.0F, 36.0F, new CubeDeformation(0.0F))
                .texOffs(155, 143).addBox(-8.0004F, -0.2428F, 20.6966F, 8.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(137, 43).addBox(-8.0004F, -0.2428F, -21.3034F, 8.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(182, 19).addBox(-0.0004F, -0.2428F, -21.3034F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(159, 170).addBox(-0.0004F, -0.2428F, 20.6966F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(235, 218).addBox(-10.0004F, -24.8726F, -14.3417F, 20.0F, 4.0F, 35.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0004F, -23.7572F, 19.3034F));

        PartDefinition bone25 = frame.addOrReplaceChild("bone25", CubeListBuilder.create().texOffs(167, 43).addBox(-10.0F, -9.0F, -2.0F, 20.0F, 18.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0016F, -15.7923F, -16.0115F, -0.3927F, 0.0F, 0.0F));

        PartDefinition bone26 = frame.addOrReplaceChild("bone26", CubeListBuilder.create().texOffs(216, 41).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 20.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.9984F, 9.7386F, 34.7449F, -0.829F, 0.0F, 0.0F));

        PartDefinition bone27 = frame.addOrReplaceChild("bone27", CubeListBuilder.create().texOffs(214, 64).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 20.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.9976F, 9.7386F, 34.7449F, -0.829F, 0.0F, 0.0F));

        PartDefinition belt = staticParts.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(199, 79).addBox(-1.0F, -11.014F, -13.9986F, 2.0F, 0.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(13, 198).addBox(-1.0F, 11.006F, 7.9986F, 2.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(13.0F, -38.996F, 45.9986F));

        PartDefinition wheel = belt.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(39, 144).addBox(-18.1111F, -1.0F, -1.0F, 22.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(61, 77).addBox(-0.1111F, 6.0F, -4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 144).addBox(-0.1111F, -4.0F, 6.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(139, 174).addBox(-0.1111F, -8.0F, -4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(136, 143).addBox(-0.1111F, -4.0F, -8.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.8889F, -3.004F, -9.9986F));

        PartDefinition bone21 = wheel.addOrReplaceChild("bone21", CubeListBuilder.create().texOffs(227, 157).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8869F, 4.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition bone22 = wheel.addOrReplaceChild("bone22", CubeListBuilder.create().texOffs(189, 7).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8869F, 0.0F, 4.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition bone5 = wheel.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(79, 230).addBox(-1.0F, -2.825F, -1.0F, 2.0F, 5.65F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8889F, -5.2905F, 5.2953F, 0.7854F, 0.0F, 0.0F));

        PartDefinition bone36 = wheel.addOrReplaceChild("bone36", CubeListBuilder.create().texOffs(190, 157).addBox(-1.0F, -2.825F, -1.0F, 2.0F, 5.65F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8889F, 5.3095F, 5.2953F, 2.3562F, 0.0F, 0.0F));

        PartDefinition bone7 = wheel.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(34, 230).addBox(-1.0F, -2.825F, -1.0F, 2.0F, 5.65F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8889F, -5.2905F, -5.2737F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone35 = wheel.addOrReplaceChild("bone35", CubeListBuilder.create().texOffs(211, 27).addBox(-1.0F, -2.825F, -1.0F, 2.0F, 5.65F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8889F, 5.2905F, -5.2737F, -2.3562F, 0.0F, 0.0F));

        PartDefinition bone23 = wheel.addOrReplaceChild("bone23", CubeListBuilder.create().texOffs(149, 229).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8869F, -4.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition bone24 = wheel.addOrReplaceChild("bone24", CubeListBuilder.create().texOffs(182, 0).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8869F, 0.0F, -4.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition bone = belt.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(137, 43).addBox(-1.0F, -0.025F, -12.725F, 2.0F, 0.0F, 25.45F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0041F, 2.9994F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone2 = belt.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(186, 101).addBox(-1.0F, 0.0F, -2.825F, 2.0F, 0.0F, 5.65F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.009F, -15.9947F, 0.7854F, 0.0F, 0.0F));

        PartDefinition bone3 = belt.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(71, 70).addBox(-1.0F, 0.0F, -2.825F, 2.0F, 0.0F, 5.65F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.9847F, -16.0017F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone4 = belt.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(163, 107).addBox(-1.0F, -0.0012F, -4.0013F, 2.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0128F, -17.9973F, 1.5708F, 0.0F, 0.0F));

        PartDefinition bone6 = belt.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(127, 143).addBox(-1.0F, 0.0F, -11.4F, 2.0F, 0.0F, 22.8F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.9966F, -3.0F, -0.2662F, 0.0F, 0.0F));

        PartDefinition bone8 = belt.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(190, 108).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.9951F, 12.0095F, 1.5708F, 0.0F, 0.0F));

        PartDefinition arm = main.addOrReplaceChild("arm", CubeListBuilder.create(), PartPose.offset(0.0F, -101.0F, -28.0F));

        PartDefinition head = arm.addOrReplaceChild("head", CubeListBuilder.create().texOffs(190, 100).addBox(-6.0F, -32.0F, -7.3333F, 12.0F, 40.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(97, 143).addBox(-6.0F, 8.0F, -5.3333F, 12.0F, 8.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(190, 157).addBox(-6.0F, 16.0F, -3.3333F, 12.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(142, 119).addBox(-6.0F, 24.0F, 0.6667F, 12.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(97, 97).addBox(-6.0F, -24.0F, -9.3333F, 12.0F, 26.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(137, 69).addBox(-6.0F, -38.0F, -7.3333F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 7.0F, -48.6667F));

        PartDefinition beam = arm.addOrReplaceChild("beam", CubeListBuilder.create().texOffs(0, 97).addBox(-2.2727F, -5.3636F, -72.8182F, 4.0F, 12.0F, 88.0F, new CubeDeformation(0.0F))
                .texOffs(190, 100).addBox(1.7273F, 4.6364F, -72.8182F, 2.0F, 2.0F, 90.0F, new CubeDeformation(0.0F))
                .texOffs(95, 108).addBox(-4.2727F, 4.6364F, -72.8182F, 2.0F, 2.0F, 90.0F, new CubeDeformation(0.0F))
                .texOffs(139, 11).addBox(-4.2727F, -7.3636F, -72.8182F, 8.0F, 2.0F, 86.0F, new CubeDeformation(0.0F))
                .texOffs(97, 143).addBox(1.7273F, -1.3636F, 17.1818F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(137, 69).addBox(-4.2727F, -1.3636F, 17.1818F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(21, 97).addBox(-2.2727F, -1.3636F, 15.1818F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(160, 19).addBox(-2.2727F, -3.3636F, 15.1818F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.2727F, -6.6364F, 32.8182F));

        PartDefinition bone37 = beam.addOrReplaceChild("bone37", CubeListBuilder.create().texOffs(100, 220).addBox(-4.0F, -8.0F, -1.0F, 8.0F, 8.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2727F, -1.0101F, 18.1212F, 0.7854F, 0.0F, 0.0F));

        PartDefinition attachmentA = main.addOrReplaceChild("attachmentA", CubeListBuilder.create().texOffs(2, 1).addBox(-16.0F, -43.0F, 24.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition attachmentB = main.addOrReplaceChild("attachmentB", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -103.0F, -28.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition attachmentC = main.addOrReplaceChild("attachmentC", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -93.0F, -87.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition attachmentD = main.addOrReplaceChild("attachmentD", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -87.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition attachmentE = main.addOrReplaceChild("attachmentE", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -102.0F, 13.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(modelData, 512, 512);
    }

    @Override
    public void setupAnim(OilPumpJackModelRenderState state) {
        super.setupAnim(state);
        this.parts.wheel().xRot = state.wheelPitch;
        this.parts.counterWeights().xRot = state.counterWeightsPitch;
        this.parts.pitmanArm().xRot = state.pitmanArmPitch;
        this.parts.arm().xRot = state.armPitch;
    }

    public OilPumpJackParts getOilPumpJackParts() {
        return this.parts;
    }

    public record OilPumpJackModelRenderState(float wheelPitch, float counterWeightsPitch, float pitmanArmPitch,
                                              float armPitch) {
    }

    public record OilPumpJackParts(ModelPart main, ModelPart wheel, ModelPart counterWeights, ModelPart pitmanArm, ModelPart arm,
                                   ModelPart attachmentA, ModelPart attachmentB, ModelPart attachmentC, ModelPart attachmentD, ModelPart attachmentE) {
    }
}
