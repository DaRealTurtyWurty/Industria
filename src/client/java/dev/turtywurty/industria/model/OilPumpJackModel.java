package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;

public class OilPumpJackModel extends Model {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("oil_pump_jack"), "main");

    private final ModelPart main;
    private final ModelPart counterWeights;
    private final ModelPart leftCounterWeight;
    private final ModelPart bone9;
    private final ModelPart bone10;
    private final ModelPart bone11;
    private final ModelPart bone12;
    private final ModelPart bone13;
    private final ModelPart bone14;
    private final ModelPart rightCounterWeight;
    private final ModelPart bone15;
    private final ModelPart bone16;
    private final ModelPart bone17;
    private final ModelPart bone18;
    private final ModelPart bone19;
    private final ModelPart bone20;
    private final ModelPart pitmanArm;
    private final ModelPart pitmanArmLeftPivot;
    private final ModelPart pitmanArmRightPivot;
    private final ModelPart staticParts;
    private final ModelPart supports;
    private final ModelPart bone28;
    private final ModelPart bone29;
    private final ModelPart bone30;
    private final ModelPart bone31;
    private final ModelPart bone32;
    private final ModelPart bone33;
    private final ModelPart bone34;
    private final ModelPart motor;
    private final ModelPart bone42;
    private final ModelPart bone41;
    private final ModelPart bone39;
    private final ModelPart bone40;
    private final ModelPart bone38;
    private final ModelPart bone43;
    private final ModelPart bone44;
    private final ModelPart bone45;
    private final ModelPart bone46;
    private final ModelPart bone47;
    private final ModelPart frame;
    private final ModelPart bone25;
    private final ModelPart bone26;
    private final ModelPart bone27;
    private final ModelPart belt;
    private final ModelPart wheel;
    private final ModelPart bone21;
    private final ModelPart bone22;
    private final ModelPart bone5;
    private final ModelPart bone36;
    private final ModelPart bone7;
    private final ModelPart bone35;
    private final ModelPart bone23;
    private final ModelPart bone24;
    private final ModelPart bone;
    private final ModelPart bone2;
    private final ModelPart bone3;
    private final ModelPart bone4;
    private final ModelPart bone6;
    private final ModelPart bone8;
    private final ModelPart arm;
    private final ModelPart head;
    private final ModelPart beam;
    private final ModelPart bone37;
    private final ModelPart attachmentA;
    private final ModelPart attachmentB;
    private final ModelPart attachmentC;
    private final ModelPart attachmentD;
    private final ModelPart attachmentE;

    private final Parts parts;

    public OilPumpJackModel(ModelPart root) {
        super(RenderLayer::getEntitySolid);

        this.main = root.getChild("main");
        this.counterWeights = main.getChild("counterWeights");
        this.leftCounterWeight = counterWeights.getChild("leftCounterWeight");
        this.bone9 = leftCounterWeight.getChild("bone9");
        this.bone10 = leftCounterWeight.getChild("bone10");
        this.bone11 = leftCounterWeight.getChild("bone11");
        this.bone12 = leftCounterWeight.getChild("bone12");
        this.bone13 = leftCounterWeight.getChild("bone13");
        this.bone14 = leftCounterWeight.getChild("bone14");
        this.rightCounterWeight = counterWeights.getChild("rightCounterWeight");
        this.bone15 = rightCounterWeight.getChild("bone15");
        this.bone16 = rightCounterWeight.getChild("bone16");
        this.bone17 = rightCounterWeight.getChild("bone17");
        this.bone18 = rightCounterWeight.getChild("bone18");
        this.bone19 = rightCounterWeight.getChild("bone19");
        this.bone20 = rightCounterWeight.getChild("bone20");
        this.pitmanArm = counterWeights.getChild("pitmanArm");
        this.pitmanArmLeftPivot = pitmanArm.getChild("pitmanArmLeftPivot");
        this.pitmanArmRightPivot = pitmanArm.getChild("pitmanArmRightPivot");
        this.staticParts = main.getChild("staticParts");
        this.supports = staticParts.getChild("supports");
        this.bone28 = supports.getChild("bone28");
        this.bone29 = supports.getChild("bone29");
        this.bone30 = supports.getChild("bone30");
        this.bone31 = supports.getChild("bone31");
        this.bone32 = supports.getChild("bone32");
        this.bone33 = supports.getChild("bone33");
        this.bone34 = supports.getChild("bone34");
        this.motor = staticParts.getChild("motor");
        this.bone42 = motor.getChild("bone42");
        this.bone41 = bone42.getChild("bone41");
        this.bone39 = bone42.getChild("bone39");
        this.bone40 = bone42.getChild("bone40");
        this.bone38 = bone42.getChild("bone38");
        this.bone43 = motor.getChild("bone43");
        this.bone44 = bone43.getChild("bone44");
        this.bone45 = bone43.getChild("bone45");
        this.bone46 = bone43.getChild("bone46");
        this.bone47 = bone43.getChild("bone47");
        this.frame = staticParts.getChild("frame");
        this.bone25 = frame.getChild("bone25");
        this.bone26 = frame.getChild("bone26");
        this.bone27 = frame.getChild("bone27");
        this.belt = staticParts.getChild("belt");
        this.wheel = belt.getChild("wheel");
        this.bone21 = wheel.getChild("bone21");
        this.bone22 = wheel.getChild("bone22");
        this.bone5 = wheel.getChild("bone5");
        this.bone36 = wheel.getChild("bone36");
        this.bone7 = wheel.getChild("bone7");
        this.bone35 = wheel.getChild("bone35");
        this.bone23 = wheel.getChild("bone23");
        this.bone24 = wheel.getChild("bone24");
        this.bone = belt.getChild("bone");
        this.bone2 = belt.getChild("bone2");
        this.bone3 = belt.getChild("bone3");
        this.bone4 = belt.getChild("bone4");
        this.bone6 = belt.getChild("bone6");
        this.bone8 = belt.getChild("bone8");
        this.arm = main.getChild("arm");
        this.head = arm.getChild("head");
        this.beam = arm.getChild("beam");
        this.bone37 = beam.getChild("bone37");
        this.attachmentA = main.getChild("attachmentA");
        this.attachmentB = main.getChild("attachmentB");
        this.attachmentC = main.getChild("attachmentC");
        this.attachmentD = main.getChild("attachmentD");
        this.attachmentE = main.getChild("attachmentE");

        this.attachmentA.hidden = true;
        this.attachmentB.hidden = true;
        this.attachmentC.hidden = true;
        this.attachmentD.hidden = true;
        this.attachmentE.hidden = true;

        this.parts = new Parts(this.main, this.wheel, this.counterWeights, this.pitmanArm, this.arm,
                this.attachmentA, this.attachmentB, this.attachmentC, this.attachmentD, this.attachmentE);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData counterWeights = main.addChild("counterWeights", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -42.0F, 24.0F));

        ModelPartData leftCounterWeight = counterWeights.addChild("leftCounterWeight", ModelPartBuilder.create().uv(17, 230).cuboid(0.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(242, 0).cuboid(3.998F, -5.002F, -40.002F, 2.004F, 10.004F, 44.004F, new Dilation(0.002F))
                .uv(79, 198).cuboid(4.0F, -19.0F, -24.0F, 2.0F, 10.0F, 8.0F, new Dilation(0.0F))
                .uv(180, 223).cuboid(4.0F, -20.027F, -22.4F, 2.0F, 1.027F, 6.4F, new Dilation(0.0F)), ModelTransform.pivot(10.0F, 0.0F, 1.0F));

        ModelPartData bone9 = leftCounterWeight.addChild("bone9", ModelPartBuilder.create().uv(47, 97).cuboid(-1.0F, -6.0F, -8.0F, 2.0F, 12.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(5.0F, -7.2716F, -28.2614F, 0.8727F, 0.0F, 0.0F));

        ModelPartData bone10 = leftCounterWeight.addChild("bone10", ModelPartBuilder.create().uv(100, 201).cuboid(-1.002F, -6.002F, -3.002F, 2.004F, 12.004F, 6.004F, new Dilation(0.002F)), ModelTransform.of(5.0F, -13.3199F, -22.2839F, 0.48F, 0.0F, 0.0F));

        ModelPartData bone11 = leftCounterWeight.addChild("bone11", ModelPartBuilder.create(), ModelTransform.of(5.0F, 7.2365F, -28.2614F, 0.8727F, 0.0F, -3.1416F));

        ModelPartData cube_r1 = bone11.addChild("cube_r1", ModelPartBuilder.create().uv(47, 0).cuboid(-1.0F, -5.5363F, -15.6012F, 2.0F, 12.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -0.4637F, 7.6012F, 0.0F, 0.0F, 0.0F));

        ModelPartData bone12 = leftCounterWeight.addChild("bone12", ModelPartBuilder.create().uv(0, 198).cuboid(-1.0F, -5.0F, -4.0F, 2.0F, 10.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(5.0F, 13.9649F, -20.0F, 0.0F, 0.0F, -3.1416F));

        ModelPartData bone13 = leftCounterWeight.addChild("bone13", ModelPartBuilder.create(), ModelTransform.of(5.0F, 13.2848F, -22.2839F, 0.48F, 0.0F, -3.1416F));

        ModelPartData cube_r2 = bone13.addChild("cube_r2", ModelPartBuilder.create().uv(134, 201).cuboid(-1.001F, -5.2685F, -1.7512F, 2.002F, 12.002F, 6.002F, new Dilation(0.001F)), ModelTransform.of(0.0F, -0.7325F, -1.2498F, 0.0F, 0.0F, 0.0F));

        ModelPartData bone14 = leftCounterWeight.addChild("bone14", ModelPartBuilder.create().uv(197, 223).cuboid(-1.0F, -0.5135F, -3.2F, 2.0F, 1.027F, 6.4F, new Dilation(0.0F)), ModelTransform.of(5.0F, 19.4784F, -19.2F, 0.0F, 0.0F, -3.1416F));

        ModelPartData rightCounterWeight = counterWeights.addChild("rightCounterWeight", ModelPartBuilder.create().uv(0, 253).cuboid(-6.002F, -5.002F, -40.002F, 2.004F, 10.004F, 44.004F, new Dilation(0.002F))
                .uv(21, 198).cuboid(-6.0F, -19.0F, -24.0F, 2.0F, 10.0F, 8.0F, new Dilation(0.0F))
                .uv(207, 19).cuboid(-6.0F, -20.027F, -22.4F, 2.0F, 1.027F, 6.4F, new Dilation(0.0F))
                .uv(0, 230).cuboid(-4.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(-10.0F, 0.0F, 1.0F));

        ModelPartData bone15 = rightCounterWeight.addChild("bone15", ModelPartBuilder.create().uv(0, 97).cuboid(-1.0F, -6.0F, -8.0F, 2.0F, 12.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(-5.0F, -7.2716F, -28.2614F, 0.8727F, 0.0F, 0.0F));

        ModelPartData bone16 = rightCounterWeight.addChild("bone16", ModelPartBuilder.create().uv(151, 201).cuboid(-1.001F, -6.001F, -3.001F, 2.002F, 12.002F, 6.002F, new Dilation(0.001F)), ModelTransform.of(-5.0F, -13.3199F, -22.2839F, 0.48F, 0.0F, 0.0F));

        ModelPartData bone17 = rightCounterWeight.addChild("bone17", ModelPartBuilder.create(), ModelTransform.of(-5.0F, 7.2365F, -28.2614F, 0.8727F, 0.0F, -3.1416F));

        ModelPartData cube_r3 = bone17.addChild("cube_r3", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -5.5363F, -15.6012F, 2.0F, 12.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -0.4637F, 7.6012F, 0.0F, 0.0F, 0.0F));

        ModelPartData bone18 = rightCounterWeight.addChild("bone18", ModelPartBuilder.create().uv(186, 66).cuboid(-1.0F, -5.0F, -4.0F, 2.0F, 10.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(-5.0F, 13.9649F, -20.0F, 0.0F, 0.0F, -3.1416F));

        ModelPartData bone19 = rightCounterWeight.addChild("bone19", ModelPartBuilder.create(), ModelTransform.of(-5.0F, 13.2848F, -22.2839F, 0.48F, 0.0F, -3.1416F));

        ModelPartData cube_r4 = bone19.addChild("cube_r4", ModelPartBuilder.create().uv(117, 201).cuboid(-1.001F, -5.2685F, -1.7512F, 2.002F, 12.002F, 6.002F, new Dilation(0.001F)), ModelTransform.of(0.0F, -0.7325F, -1.2498F, 0.0F, 0.0F, 0.0F));

        ModelPartData bone20 = rightCounterWeight.addChild("bone20", ModelPartBuilder.create().uv(153, 221).cuboid(-1.0F, -0.5135F, -3.2F, 2.0F, 1.027F, 6.4F, new Dilation(0.0F)), ModelTransform.of(-5.0F, 19.4784F, -19.2F, 0.0F, 0.0F, -3.1416F));

        ModelPartData pitmanArm = counterWeights.addChild("pitmanArm", ModelPartBuilder.create().uv(0, 76).cuboid(-14.0F, -44.5F, -2.7778F, 28.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 47).cuboid(-18.0F, -50.5F, -2.7778F, 36.0F, 6.0F, 6.0F, new Dilation(0.0F))
                .uv(58, 158).cuboid(-6.0F, -52.5F, -4.7778F, 12.0F, 6.0F, 2.0F, new Dilation(0.0F))
                .uv(49, 170).cuboid(-6.0F, -58.5F, -2.7778F, 12.0F, 6.0F, 6.0F, new Dilation(0.0F))
                .uv(138, 220).cuboid(-4.0F, -58.5F, -4.7778F, 8.0F, 4.0F, 2.0F, new Dilation(0.0F))
                .uv(37, 170).cuboid(-20.0F, -50.5F, -2.7778F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-22.0F, -50.5F, -0.7778F, 4.0F, 6.0F, 2.0F, new Dilation(0.0F))
                .uv(208, 225).cuboid(-20.0F, -52.5F, -2.7778F, 2.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(216, 0).cuboid(-6.0F, -52.5F, -2.7778F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(58, 149).cuboid(-6.0F, -52.5F, 3.2222F, 12.0F, 6.0F, 2.0F, new Dilation(0.0F))
                .uv(190, 193).cuboid(-6.0F, -52.5F, 1.2222F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(144, 272).cuboid(18.0F, -44.5F, -2.7778F, 2.0F, 48.0F, 6.0F, new Dilation(0.0F))
                .uv(47, 0).cuboid(18.0F, -50.5F, -0.7778F, 4.0F, 6.0F, 2.0F, new Dilation(0.0F))
                .uv(97, 173).cuboid(18.0F, -50.5F, -2.7778F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F))
                .uv(132, 227).cuboid(18.0F, -52.5F, -2.7778F, 2.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(127, 272).cuboid(-20.0F, -44.5F, -2.7778F, 2.0F, 48.0F, 6.0F, new Dilation(0.0F))
                .uv(174, 69).cuboid(18.0F, -50.5F, 1.2222F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 167).cuboid(-20.0F, -50.5F, 1.2222F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -0.5F, -10.2222F));

        ModelPartData pitmanArmLeftPivot = pitmanArm.addChild("pitmanArmLeftPivot", ModelPartBuilder.create().uv(121, 220).cuboid(0.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(16.0F, 0.5F, 0.2222F));

        ModelPartData pitmanArmRightPivot = pitmanArm.addChild("pitmanArmRightPivot", ModelPartBuilder.create().uv(68, 97).cuboid(-2.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(-16.0F, 0.5F, 0.2222F));

        ModelPartData staticParts = main.addChild("staticParts", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData supports = staticParts.addChild("supports", ModelPartBuilder.create().uv(0, 167).cuboid(-6.001F, 2.099F, -6.001F, 12.002F, 4.002F, 12.002F, new Dilation(0.001F))
                .uv(37, 151).cuboid(6.0F, 2.1F, -8.0F, 2.0F, 2.0F, 16.0F, new Dilation(0.0F))
                .uv(142, 100).cuboid(-8.0F, 2.1F, -8.0F, 2.0F, 2.0F, 16.0F, new Dilation(0.0F))
                .uv(190, 183).cuboid(-6.0F, 2.1F, -8.0F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(190, 178).cuboid(-6.0F, 2.1F, 6.0F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(137, 19).cuboid(-4.0F, -4.9F, -3.0F, 8.0F, 8.0F, 6.0F, new Dilation(0.0F))
                .uv(68, 0).cuboid(4.0F, -6.9F, -3.0F, 2.0F, 6.0F, 6.0F, new Dilation(0.0F))
                .uv(224, 5).cuboid(-4.0F, -4.9F, -5.0F, 8.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(219, 193).cuboid(-4.0F, -4.9F, 3.0F, 8.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(21, 0).cuboid(-6.0F, -6.9F, -3.0F, 2.0F, 6.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -96.1F, -28.0F));

        ModelPartData bone28 = supports.addChild("bone28", ModelPartBuilder.create().uv(247, 100).cuboid(-4.0F, -31.0F, -4.0F, 8.0F, 62.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 31.9853F, 15.7679F, 0.5236F, 0.0F, 0.0F));

        ModelPartData bone29 = supports.addChild("bone29", ModelPartBuilder.create().uv(0, 126).cuboid(-6.0F, -1.0F, -2.0F, 12.0F, 2.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 5.8443F, -3.7786F, -0.3491F, 0.0F, 0.0F));

        ModelPartData bone30 = supports.addChild("bone30", ModelPartBuilder.create().uv(0, 69).cuboid(-18.0F, -2.0F, -1.0F, 36.0F, 4.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 76.0825F, -14.7497F, -0.1745F, 0.0F, 0.0F));

        ModelPartData bone31 = supports.addChild("bone31", ModelPartBuilder.create().uv(118, 167).cuboid(-10.0F, -2.0F, -1.0F, 20.0F, 4.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 48.6729F, -8.952F, -0.1745F, 0.0F, 0.0F));

        ModelPartData bone32 = supports.addChild("bone32", ModelPartBuilder.create().uv(182, 30).cuboid(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 26.0483F, -4.8104F, -0.1745F, 0.0F, 0.0F));

        ModelPartData bone33 = supports.addChild("bone33", ModelPartBuilder.create().uv(110, 268).cuboid(-2.0F, -44.0F, -2.0F, 4.0F, 88.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(-11.0476F, 47.7185F, -8.7068F, -0.1745F, 0.0F, 0.2182F));

        ModelPartData bone34 = supports.addChild("bone34", ModelPartBuilder.create().uv(93, 253).cuboid(-2.0F, -44.0F, -2.0F, 4.0F, 88.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(11.0476F, 47.7185F, -8.7068F, -0.1745F, 0.0F, -0.2182F));

        ModelPartData motor = staticParts.addChild("motor", ModelPartBuilder.create().uv(182, 0).cuboid(2.6296F, 5.1111F, -8.0F, 2.0F, 2.0F, 16.0F, new Dilation(0.0F))
                .uv(0, 29).cuboid(-11.3704F, 3.1111F, 2.0F, 16.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(97, 166).cuboid(-11.3704F, 5.1111F, -8.0F, 2.0F, 2.0F, 16.0F, new Dilation(0.0F))
                .uv(47, 29).cuboid(-11.3704F, 3.1111F, -4.0F, 16.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(97, 126).cuboid(-9.3704F, 3.1111F, -2.0F, 12.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(219, 183).cuboid(-9.3704F, 1.1111F, 2.0F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(118, 174).cuboid(-9.3704F, -2.8889F, 4.0F, 12.0F, 4.0F, 2.0F, new Dilation(0.0F))
                .uv(209, 218).cuboid(-9.3704F, -4.8889F, 2.0F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(47, 126).cuboid(-9.3704F, -6.8889F, -2.0F, 12.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(39, 149).cuboid(2.6296F, -4.8889F, -2.0F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(97, 166).cuboid(2.6296F, 1.1111F, -2.0F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(79, 217).cuboid(2.6296F, -2.8889F, -4.0F, 2.0F, 4.0F, 8.0F, new Dilation(0.0F))
                .uv(219, 178).cuboid(-9.3704F, 1.1111F, -4.0F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(163, 100).cuboid(-9.3704F, -2.8889F, -6.0F, 12.0F, 4.0F, 2.0F, new Dilation(0.0F))
                .uv(180, 218).cuboid(-9.3704F, -4.8889F, -4.0F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(21, 217).cuboid(-11.3704F, -2.8889F, -4.0F, 2.0F, 4.0F, 8.0F, new Dilation(0.0F))
                .uv(47, 106).cuboid(-11.3704F, -4.8889F, -2.0F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(142, 109).cuboid(-11.3704F, 1.1111F, -2.0F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(47, 97).cuboid(-13.3704F, -2.8889F, -2.0F, 2.0F, 4.0F, 4.0F, new Dilation(0.0F))
                .uv(0, 97).cuboid(4.6296F, -2.8889F, -2.0F, 2.0F, 4.0F, 4.0F, new Dilation(0.0F))
                .uv(142, 100).cuboid(8.6296F, -2.8889F, -2.0F, 2.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(3.3704F, -29.1111F, 56.0F));

        ModelPartData bone42 = motor.addChild("bone42", ModelPartBuilder.create().uv(0, 217).cuboid(-1.0F, -2.0F, -4.0F, 2.0F, 4.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 106).cuboid(-1.0F, -4.0F, -2.0F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(74, 77).cuboid(-1.0F, 2.0F, -2.0F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(11.6296F, -0.8889F, 0.0F));

        ModelPartData bone41 = bone42.addChild("bone41", ModelPartBuilder.create().uv(34, 217).cuboid(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new Dilation(0.0F)), ModelTransform.of(-0.01F, 2.2913F, -2.2937F, 2.3562F, 0.0F, 0.0F));

        ModelPartData bone39 = bone42.addChild("bone39", ModelPartBuilder.create().uv(111, 201).cuboid(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new Dilation(0.0F)), ModelTransform.of(-0.01F, -2.2941F, -2.2913F, 0.7854F, 0.0F, 0.0F));

        ModelPartData bone40 = bone42.addChild("bone40", ModelPartBuilder.create().uv(191, 223).cuboid(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new Dilation(0.0F)), ModelTransform.of(-0.01F, 2.2937F, 2.2917F, -2.3562F, 0.0F, 0.0F));

        ModelPartData bone38 = bone42.addChild("bone38", ModelPartBuilder.create().uv(128, 201).cuboid(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new Dilation(0.0F)), ModelTransform.of(-0.01F, -2.2917F, 2.2941F, -0.7854F, 0.0F, 0.0F));

        ModelPartData bone43 = motor.addChild("bone43", ModelPartBuilder.create().uv(203, 0).cuboid(-1.0F, -2.0F, -4.0F, 2.0F, 4.0F, 8.0F, new Dilation(0.0F))
                .uv(47, 9).cuboid(-1.0F, -4.0F, -2.0F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(0, 9).cuboid(-1.0F, 2.0F, -2.0F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(7.6296F, -0.8889F, 0.0F));

        ModelPartData bone44 = bone43.addChild("bone44", ModelPartBuilder.create().uv(13, 217).cuboid(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new Dilation(0.0F)), ModelTransform.of(-0.01F, 2.2913F, -2.2937F, 2.3562F, 0.0F, 0.0F));

        ModelPartData bone45 = bone43.addChild("bone45", ModelPartBuilder.create().uv(199, 66).cuboid(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new Dilation(0.0F)), ModelTransform.of(-0.01F, -2.2941F, -2.2913F, 0.7854F, 0.0F, 0.0F));

        ModelPartData bone46 = bone43.addChild("bone46", ModelPartBuilder.create().uv(145, 201).cuboid(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new Dilation(0.0F)), ModelTransform.of(-0.01F, 2.2937F, 2.2917F, -2.3562F, 0.0F, 0.0F));

        ModelPartData bone47 = bone43.addChild("bone47", ModelPartBuilder.create().uv(34, 198).cuboid(-1.0F, -1.0F, -1.4125F, 2.0F, 2.0F, 2.825F, new Dilation(0.0F)), ModelTransform.of(-0.01F, -2.2917F, 2.2941F, -0.7854F, 0.0F, 0.0F));

        ModelPartData frame = staticParts.addChild("frame", ModelPartBuilder.create().uv(180, 201).cuboid(-32.0004F, 15.7572F, -67.3034F, 64.0F, 8.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 60).cuboid(-18.0004F, 13.7572F, -39.3034F, 36.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-12.0004F, 15.7572F, -59.3034F, 24.0F, 8.0F, 88.0F, new Dilation(0.0F))
                .uv(0, 144).cuboid(-6.0004F, 15.7572F, 28.6966F, 12.0F, 8.0F, 14.0F, new Dilation(0.0F))
                .uv(242, 55).cuboid(-12.0004F, 1.7572F, 24.6966F, 24.0F, 2.0F, 24.0F, new Dilation(0.0F))
                .uv(180, 226).cuboid(7.9996F, -8.2428F, -21.3034F, 4.0F, 24.0F, 46.0F, new Dilation(0.0F))
                .uv(125, 201).cuboid(-12.0004F, -8.2428F, -21.3034F, 4.0F, 24.0F, 46.0F, new Dilation(0.0F))
                .uv(0, 198).cuboid(-8.0004F, -8.2428F, -21.3034F, 16.0F, 8.0F, 46.0F, new Dilation(0.0F))
                .uv(0, 97).cuboid(5.9956F, -16.2428F, -17.3034F, 4.0F, 8.0F, 38.0F, new Dilation(0.0F))
                .uv(137, 0).cuboid(5.9976F, -22.2428F, -15.3034F, 4.0F, 6.0F, 36.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-9.9964F, -16.2428F, -17.3034F, 4.0F, 8.0F, 38.0F, new Dilation(0.0F))
                .uv(137, 0).cuboid(-5.9964F, -22.2428F, 16.6966F, 12.0F, 14.0F, 4.0F, new Dilation(0.0F))
                .uv(97, 100).cuboid(-9.9944F, -22.2428F, -15.3034F, 4.0F, 6.0F, 36.0F, new Dilation(0.0F))
                .uv(155, 143).cuboid(-8.0004F, -0.2428F, 20.6966F, 8.0F, 16.0F, 4.0F, new Dilation(0.0F))
                .uv(137, 43).cuboid(-8.0004F, -0.2428F, -21.3034F, 8.0F, 16.0F, 4.0F, new Dilation(0.0F))
                .uv(182, 19).cuboid(-0.0004F, -0.2428F, -21.3034F, 8.0F, 6.0F, 4.0F, new Dilation(0.0F))
                .uv(159, 170).cuboid(-0.0004F, -0.2428F, 20.6966F, 8.0F, 6.0F, 4.0F, new Dilation(0.0F))
                .uv(235, 218).cuboid(-10.0004F, -24.8726F, -14.3417F, 20.0F, 4.0F, 35.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0004F, -23.7572F, 19.3034F));

        ModelPartData bone25 = frame.addChild("bone25", ModelPartBuilder.create().uv(167, 43).cuboid(-10.0F, -9.0F, -2.0F, 20.0F, 18.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0016F, -15.7923F, -16.0115F, -0.3927F, 0.0F, 0.0F));

        ModelPartData bone26 = frame.addChild("bone26", ModelPartBuilder.create().uv(216, 41).cuboid(-1.0F, -10.0F, -1.0F, 2.0F, 20.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-10.9984F, 9.7386F, 34.7449F, -0.829F, 0.0F, 0.0F));

        ModelPartData bone27 = frame.addChild("bone27", ModelPartBuilder.create().uv(214, 64).cuboid(-1.0F, -10.0F, -1.0F, 2.0F, 20.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(10.9976F, 9.7386F, 34.7449F, -0.829F, 0.0F, 0.0F));

        ModelPartData belt = staticParts.addChild("belt", ModelPartBuilder.create().uv(199, 79).cuboid(-1.0F, -11.014F, -13.9986F, 2.0F, 0.0F, 8.0F, new Dilation(0.0F))
                .uv(13, 198).cuboid(-1.0F, 11.006F, 7.9986F, 2.0F, 0.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(13.0F, -38.996F, 45.9986F));

        ModelPartData wheel = belt.addChild("wheel", ModelPartBuilder.create().uv(39, 144).cuboid(-18.1111F, -1.0F, -1.0F, 22.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(61, 77).cuboid(-0.1111F, 6.0F, -4.0F, 2.0F, 2.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 144).cuboid(-0.1111F, -4.0F, 6.0F, 2.0F, 8.0F, 2.0F, new Dilation(0.0F))
                .uv(139, 174).cuboid(-0.1111F, -8.0F, -4.0F, 2.0F, 2.0F, 8.0F, new Dilation(0.0F))
                .uv(136, 143).cuboid(-0.1111F, -4.0F, -8.0F, 2.0F, 8.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-0.8889F, -3.004F, -9.9986F));

        ModelPartData bone21 = wheel.addChild("bone21", ModelPartBuilder.create().uv(227, 157).cuboid(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.8869F, 4.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        ModelPartData bone22 = wheel.addChild("bone22", ModelPartBuilder.create().uv(189, 7).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.8869F, 0.0F, 4.0F, -1.5708F, 0.0F, 0.0F));

        ModelPartData bone5 = wheel.addChild("bone5", ModelPartBuilder.create().uv(79, 230).cuboid(-1.0F, -2.825F, -1.0F, 2.0F, 5.65F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.8889F, -5.2905F, 5.2953F, 0.7854F, 0.0F, 0.0F));

        ModelPartData bone36 = wheel.addChild("bone36", ModelPartBuilder.create().uv(190, 157).cuboid(-1.0F, -2.825F, -1.0F, 2.0F, 5.65F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.8889F, 5.3095F, 5.2953F, 2.3562F, 0.0F, 0.0F));

        ModelPartData bone7 = wheel.addChild("bone7", ModelPartBuilder.create().uv(34, 230).cuboid(-1.0F, -2.825F, -1.0F, 2.0F, 5.65F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.8889F, -5.2905F, -5.2737F, -0.7854F, 0.0F, 0.0F));

        ModelPartData bone35 = wheel.addChild("bone35", ModelPartBuilder.create().uv(211, 27).cuboid(-1.0F, -2.825F, -1.0F, 2.0F, 5.65F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.8889F, 5.2905F, -5.2737F, -2.3562F, 0.0F, 0.0F));

        ModelPartData bone23 = wheel.addChild("bone23", ModelPartBuilder.create().uv(149, 229).cuboid(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.8869F, -4.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        ModelPartData bone24 = wheel.addChild("bone24", ModelPartBuilder.create().uv(182, 0).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.8869F, 0.0F, -4.0F, -1.5708F, 0.0F, 0.0F));

        ModelPartData bone = belt.addChild("bone", ModelPartBuilder.create().uv(137, 43).cuboid(-1.0F, -0.025F, -12.725F, 2.0F, 0.0F, 25.45F, new Dilation(0.0F)), ModelTransform.of(0.0F, -2.0041F, 2.9994F, -0.7854F, 0.0F, 0.0F));

        ModelPartData bone2 = belt.addChild("bone2", ModelPartBuilder.create().uv(186, 101).cuboid(-1.0F, 0.0F, -2.825F, 2.0F, 0.0F, 5.65F, new Dilation(0.0F)), ModelTransform.of(0.0F, -9.009F, -15.9947F, 0.7854F, 0.0F, 0.0F));

        ModelPartData bone3 = belt.addChild("bone3", ModelPartBuilder.create().uv(71, 70).cuboid(-1.0F, 0.0F, -2.825F, 2.0F, 0.0F, 5.65F, new Dilation(0.0F)), ModelTransform.of(0.0F, 2.9847F, -16.0017F, -0.7854F, 0.0F, 0.0F));

        ModelPartData bone4 = belt.addChild("bone4", ModelPartBuilder.create().uv(163, 107).cuboid(-1.0F, -0.0012F, -4.0013F, 2.0F, 0.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -3.0128F, -17.9973F, 1.5708F, 0.0F, 0.0F));

        ModelPartData bone6 = belt.addChild("bone6", ModelPartBuilder.create().uv(127, 143).cuboid(-1.0F, 0.0F, -11.4F, 2.0F, 0.0F, 22.8F, new Dilation(0.0F)), ModelTransform.of(0.0F, 7.9966F, -3.0F, -0.2662F, 0.0F, 0.0F));

        ModelPartData bone8 = belt.addChild("bone8", ModelPartBuilder.create().uv(190, 108).cuboid(-1.0F, 0.0F, -2.0F, 2.0F, 0.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 8.9951F, 12.0095F, 1.5708F, 0.0F, 0.0F));

        ModelPartData arm = main.addChild("arm", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -101.0F, -28.0F));

        ModelPartData head = arm.addChild("head", ModelPartBuilder.create().uv(190, 100).cuboid(-6.0F, -32.0F, -7.3333F, 12.0F, 40.0F, 16.0F, new Dilation(0.0F))
                .uv(97, 143).cuboid(-6.0F, 8.0F, -5.3333F, 12.0F, 8.0F, 14.0F, new Dilation(0.0F))
                .uv(190, 157).cuboid(-6.0F, 16.0F, -3.3333F, 12.0F, 8.0F, 12.0F, new Dilation(0.0F))
                .uv(142, 119).cuboid(-6.0F, 24.0F, 0.6667F, 12.0F, 4.0F, 8.0F, new Dilation(0.0F))
                .uv(97, 97).cuboid(-6.0F, -24.0F, -9.3333F, 12.0F, 26.0F, 2.0F, new Dilation(0.0F))
                .uv(137, 69).cuboid(-6.0F, -38.0F, -7.3333F, 12.0F, 6.0F, 12.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 7.0F, -48.6667F));

        ModelPartData beam = arm.addChild("beam", ModelPartBuilder.create().uv(0, 97).cuboid(-2.2727F, -5.3636F, -72.8182F, 4.0F, 12.0F, 88.0F, new Dilation(0.0F))
                .uv(190, 100).cuboid(1.7273F, 4.6364F, -72.8182F, 2.0F, 2.0F, 90.0F, new Dilation(0.0F))
                .uv(95, 108).cuboid(-4.2727F, 4.6364F, -72.8182F, 2.0F, 2.0F, 90.0F, new Dilation(0.0F))
                .uv(139, 11).cuboid(-4.2727F, -7.3636F, -72.8182F, 8.0F, 2.0F, 86.0F, new Dilation(0.0F))
                .uv(97, 143).cuboid(1.7273F, -1.3636F, 17.1818F, 2.0F, 8.0F, 2.0F, new Dilation(0.0F))
                .uv(137, 69).cuboid(-4.2727F, -1.3636F, 17.1818F, 2.0F, 8.0F, 2.0F, new Dilation(0.0F))
                .uv(21, 97).cuboid(-2.2727F, -1.3636F, 15.1818F, 4.0F, 8.0F, 4.0F, new Dilation(0.0F))
                .uv(160, 19).cuboid(-2.2727F, -3.3636F, 15.1818F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.2727F, -6.6364F, 32.8182F));

        ModelPartData bone37 = beam.addChild("bone37", ModelPartBuilder.create().uv(100, 220).cuboid(-4.0F, -8.0F, -1.0F, 8.0F, 8.5F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-0.2727F, -1.0101F, 18.1212F, 0.7854F, 0.0F, 0.0F));

        ModelPartData attachmentA = main.addChild("attachmentA", ModelPartBuilder.create().uv(2, 1).cuboid(-16.0F, -43.0F, 24.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData attachmentB = main.addChild("attachmentB", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -103.0F, -28.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData attachmentC = main.addChild("attachmentC", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -93.0F, -87.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData attachmentD = main.addChild("attachmentD", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -2.0F, -87.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData attachmentE = main.addChild("attachmentE", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -102.0F, 13.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        return TexturedModelData.of(modelData, 512, 512);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        this.main.render(matrices, vertexConsumer, light, overlay, color);
    }

    public Parts getParts() {
        return this.parts;
    }

    public record Parts(ModelPart main, ModelPart wheel, ModelPart counterWeights, ModelPart pitmanArm, ModelPart arm,
                        ModelPart attachmentA, ModelPart attachmentB, ModelPart attachmentC, ModelPart attachmentD, ModelPart attachmentE) {
    }
}