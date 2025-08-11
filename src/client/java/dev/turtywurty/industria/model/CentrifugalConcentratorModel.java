package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class CentrifugalConcentratorModel extends Model {
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/centrifugal_concentrator.png");
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("centrifugal_concentrator"), "main");

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
        super(root, RenderLayer::getEntityCutout);

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

    public static TexturedModelData getTexturedModelData() {
        var modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create(), ModelTransform.origin(0.0F, 24.0F, 0.0F));

        ModelPartData legs = main.addChild("legs", ModelPartBuilder.create().uv(56, 193).cuboid(16.932F, -24.0F, 35.5949F, 4.0F, 24.0F, 2.0F, new Dilation(0.0F))
                .uv(80, 38).cuboid(-0.068F, -9.0F, -1.4051F, 38.0F, 2.0F, 2.0F, new Dilation(-0.05F)), ModelTransform.origin(-18.932F, 0.0F, -13.5949F));

        ModelPartData cube_r1 = legs.addChild("cube_r1", ModelPartBuilder.create().uv(80, 30).cuboid(-42.0F, -2.0F, -1.0F, 42.0F, 2.0F, 2.0F, new Dilation(-0.05F)), ModelTransform.of(37.932F, -7.0F, -0.4051F, 0.0F, 1.0908F, 0.0F));

        ModelPartData cube_r2 = legs.addChild("cube_r2", ModelPartBuilder.create().uv(80, 34).cuboid(0.0F, -1.999F, -1.0F, 41.0F, 2.0F, 2.0F, new Dilation(-0.05F)), ModelTransform.of(-0.068F, -7.0F, -0.4051F, 0.0F, -1.0908F, 0.0F));

        ModelPartData cube_r3 = legs.addChild("cube_r3", ModelPartBuilder.create().uv(190, 98).cuboid(-1.0F, -24.0F, -1.0F, 4.0F, 24.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        ModelPartData cube_r4 = legs.addChild("cube_r4", ModelPartBuilder.create().uv(44, 193).cuboid(-3.0F, -24.0F, -1.0F, 4.0F, 24.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(37.864F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        ModelPartData dampeners = main.addChild("dampeners", ModelPartBuilder.create().uv(218, 24).cuboid(-24.0F, -1.0F, -9.6667F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(216, 92).cuboid(22.0F, -1.0F, -9.6667F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(44, 140).cuboid(-2.0F, -1.0F, 14.3333F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -30.0F, 7.6667F));

        ModelPartData connectingPlate = main.addChild("connectingPlate", ModelPartBuilder.create().uv(68, 207).cuboid(-24.0F, -0.5F, -11.5F, 3.0F, 1.0F, 8.0F, new Dilation(0.0F))
                .uv(22, 210).cuboid(21.0F, -0.5F, -11.5F, 3.0F, 1.0F, 8.0F, new Dilation(0.0F))
                .uv(52, 90).cuboid(-2.0F, -0.5F, 13.5F, 4.0F, 1.0F, 3.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -31.5F, 7.5F));

        ModelPartData outerRing = main.addChild("outerRing", ModelPartBuilder.create().uv(198, 213).cuboid(-4.7739F, -2.4995F, -24.0F, 9.5478F, 5.0F, 2.0F, new Dilation(0.0F))
                .uv(214, 58).cuboid(-4.7739F, -2.4995F, 22.0F, 9.5478F, 5.0F, 2.0F, new Dilation(0.0F))
                .uv(194, 15).cuboid(22.0F, -2.4995F, -4.7739F, 2.0F, 5.0F, 9.5478F, new Dilation(0.0F))
                .uv(200, 139).cuboid(-24.0F, -2.4995F, -4.7739F, 2.0F, 5.0F, 9.5478F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -26.5005F, 0.0F));

        ModelPartData hexadecagon_r1 = outerRing.addChild("hexadecagon_r1", ModelPartBuilder.create().uv(154, 200).cuboid(-24.0F, -8.001F, -4.7739F, 2.0F, 5.0F, 9.5478F, new Dilation(0.0F))
                .uv(178, 195).cuboid(22.0F, -8.001F, -4.7739F, 2.0F, 5.0F, 9.5478F, new Dilation(0.0F))
                .uv(214, 65).cuboid(-4.7739F, -8.001F, 22.0F, 9.5478F, 5.0F, 2.0F, new Dilation(0.0F))
                .uv(214, 30).cuboid(-4.7739F, -8.001F, -24.0F, 9.5478F, 5.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 5.5005F, 0.0F, 0.0F, -0.3927F, 0.0F));

        ModelPartData hexadecagon_r2 = outerRing.addChild("hexadecagon_r2", ModelPartBuilder.create().uv(200, 124).cuboid(-24.0F, -8.001F, -4.7739F, 2.0F, 5.0F, 9.5478F, new Dilation(0.0F))
                .uv(194, 0).cuboid(22.0F, -8.001F, -4.7739F, 2.0F, 5.0F, 9.5478F, new Dilation(0.0F))
                .uv(214, 51).cuboid(-4.7739F, -8.001F, 22.0F, 9.5478F, 5.0F, 2.0F, new Dilation(0.0F))
                .uv(210, 185).cuboid(-4.7739F, -8.001F, -24.0F, 9.5478F, 5.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 5.5005F, 0.0F, 0.0F, 0.3927F, 0.0F));

        ModelPartData hexadecagon_r3 = outerRing.addChild("hexadecagon_r3", ModelPartBuilder.create().uv(214, 72).cuboid(-4.7739F, -8.0F, 22.0F, 9.5478F, 5.0F, 2.0F, new Dilation(0.0F))
                .uv(214, 37).cuboid(-4.7739F, -8.0F, -24.0F, 9.5478F, 5.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 5.5005F, 0.0F, 0.0F, -0.7854F, 0.0F));

        ModelPartData hexadecagon_r4 = outerRing.addChild("hexadecagon_r4", ModelPartBuilder.create().uv(214, 44).cuboid(-4.7739F, -8.0F, 22.0F, 9.5478F, 5.0F, 2.0F, new Dilation(0.0F))
                .uv(202, 116).cuboid(-4.7739F, -8.0F, -24.0F, 9.5478F, 5.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 5.5005F, 0.0F, 0.0F, 0.7854F, 0.0F));

        ModelPartData inputTube = main.addChild("inputTube", ModelPartBuilder.create().uv(214, 169).cuboid(-0.7956F, 1.0F, -4.0F, 1.5913F, 14.0F, 2.0F, new Dilation(0.0F))
                .uv(168, 215).cuboid(-0.7956F, 1.0F, 2.0F, 1.5913F, 14.0F, 2.0F, new Dilation(0.0F))
                .uv(108, 216).cuboid(2.0F, 1.0F, -0.7956F, 2.0F, 14.0F, 1.5913F, new Dilation(0.0F))
                .uv(116, 216).cuboid(-4.0F, 1.0F, -0.7956F, 2.0F, 14.0F, 1.5913F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -46.0F, 0.0F));

        ModelPartData hexadecagon_r5 = inputTube.addChild("hexadecagon_r5", ModelPartBuilder.create().uv(100, 216).cuboid(-4.0F, 0.999F, -0.7956F, 2.0F, 14.0F, 1.5913F, new Dilation(0.0F))
                .uv(84, 216).cuboid(2.0F, 0.999F, -0.7956F, 2.0F, 14.0F, 1.5913F, new Dilation(0.0F))
                .uv(136, 205).cuboid(-0.7956F, 0.999F, 2.0F, 1.5913F, 14.0F, 2.0F, new Dilation(0.0F))
                .uv(160, 92).cuboid(-0.7956F, 0.999F, -4.0F, 1.5913F, 14.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        ModelPartData hexadecagon_r6 = inputTube.addChild("hexadecagon_r6", ModelPartBuilder.create().uv(92, 216).cuboid(-4.0F, 0.999F, -0.7956F, 2.0F, 14.0F, 1.5913F, new Dilation(0.0F))
                .uv(76, 216).cuboid(2.0F, 0.999F, -0.7956F, 2.0F, 14.0F, 1.5913F, new Dilation(0.0F))
                .uv(160, 108).cuboid(-0.7956F, 0.999F, 2.0F, 1.5913F, 14.0F, 2.0F, new Dilation(0.0F))
                .uv(100, 144).cuboid(-0.7956F, 0.999F, -4.0F, 1.5913F, 14.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

        ModelPartData hexadecagon_r7 = inputTube.addChild("hexadecagon_r7", ModelPartBuilder.create().uv(68, 216).cuboid(-0.7956F, 1.0F, 2.0F, 1.5913F, 14.0F, 2.0F, new Dilation(0.0F))
                .uv(152, 215).cuboid(-0.7956F, 1.0F, -4.0F, 1.5913F, 14.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        ModelPartData hexadecagon_r8 = inputTube.addChild("hexadecagon_r8", ModelPartBuilder.create().uv(160, 215).cuboid(-0.7956F, 1.0F, 2.0F, 1.5913F, 14.0F, 2.0F, new Dilation(0.0F))
                .uv(144, 205).cuboid(-0.7956F, 1.0F, -4.0F, 1.5913F, 14.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        ModelPartData outputTube = main.addChild("outputTube", ModelPartBuilder.create().uv(202, 98).cuboid(-3.0F, 0.0F, -4.0F, 6.0F, 1.0F, 8.0F, new Dilation(0.0F))
                .uv(66, 90).cuboid(-3.0F, -3.0F, 4.0F, 6.0F, 3.0F, 1.0F, new Dilation(0.0F))
                .uv(202, 107).cuboid(-3.0F, -4.0F, -4.0F, 6.0F, 1.0F, 8.0F, new Dilation(0.0F))
                .uv(178, 210).cuboid(-4.0F, -3.0F, -4.0F, 2.0F, 3.0F, 8.0F, new Dilation(0.0F))
                .uv(194, 30).cuboid(-3.0F, -3.0F, -5.0F, 6.0F, 3.0F, 1.0F, new Dilation(0.0F)), ModelTransform.origin(15.0F, -17.0F, 0.0F));

        ModelPartData outputTube2 = main.addChild("outputTube2", ModelPartBuilder.create().uv(202, 195).cuboid(-2.4F, 1.4F, -4.0F, 6.0F, 1.0F, 8.0F, new Dilation(0.0F))
                .uv(92, 201).cuboid(-2.4F, -1.6F, 4.0F, 6.0F, 3.0F, 1.0F, new Dilation(0.0F))
                .uv(202, 204).cuboid(-2.4F, -2.6F, -4.0F, 6.0F, 1.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 211).cuboid(-3.4F, -1.6F, -4.0F, 2.0F, 3.0F, 8.0F, new Dilation(0.0F))
                .uv(48, 219).cuboid(-2.4F, -1.6F, -5.0F, 6.0F, 3.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-14.6F, -18.4F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        ModelPartData ringBody = main.addChild("ringBody", ModelPartBuilder.create().uv(66, 160).cuboid(-4.1772F, -15.0F, -21.0F, 8.3543F, 30.0F, 3.0F, new Dilation(0.0F))
                .uv(110, 172).cuboid(-4.1772F, -15.0F, 18.0F, 8.3543F, 30.0F, 3.0F, new Dilation(0.0F))
                .uv(22, 178).cuboid(18.0F, -15.0F, -4.1772F, 3.0F, 24.0F, 8.3543F, new Dilation(0.0F))
                .uv(92, 205).cuboid(18.0F, 12.0F, -4.1772F, 3.0F, 3.0F, 8.3543F, new Dilation(0.0F))
                .uv(114, 205).cuboid(-21.0F, 12.0F, -4.1772F, 3.0F, 3.0F, 8.3543F, new Dilation(0.0F))
                .uv(178, 125).cuboid(-21.0F, -15.0F, -4.1772F, 3.0F, 24.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -29.0F, 0.0F));

        ModelPartData hexadecagon_r9 = ringBody.addChild("hexadecagon_r9", ModelPartBuilder.create().uv(22, 140).cuboid(-21.0F, -15.0F, -4.1772F, 3.0F, 30.0F, 8.3543F, new Dilation(0.001F))
                .uv(134, 134).cuboid(18.0F, -15.0F, -4.1772F, 3.0F, 30.0F, 8.3543F, new Dilation(0.001F))
                .uv(132, 172).cuboid(-4.1772F, -15.0F, 18.0F, 8.3543F, 30.0F, 3.0F, new Dilation(0.001F))
                .uv(88, 160).cuboid(-4.1772F, -15.0F, -21.0F, 8.3543F, 30.0F, 3.0F, new Dilation(0.001F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        ModelPartData hexadecagon_r10 = ringBody.addChild("hexadecagon_r10", ModelPartBuilder.create().uv(0, 140).cuboid(-21.0F, -15.0F, -4.1772F, 3.0F, 30.0F, 8.3543F, new Dilation(0.001F))
                .uv(112, 134).cuboid(18.0F, -15.0F, -4.1772F, 3.0F, 30.0F, 8.3543F, new Dilation(0.001F))
                .uv(172, 0).cuboid(-4.1772F, -15.0F, 18.0F, 8.3543F, 30.0F, 3.0F, new Dilation(0.001F))
                .uv(44, 160).cuboid(-4.1772F, -15.0F, -21.0F, 8.3543F, 30.0F, 3.0F, new Dilation(0.001F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

        ModelPartData hexadecagon_r11 = ringBody.addChild("hexadecagon_r11", ModelPartBuilder.create().uv(0, 178).cuboid(-4.1772F, -15.0F, 18.0F, 8.3543F, 30.0F, 3.0F, new Dilation(0.0F))
                .uv(156, 167).cuboid(-4.1772F, -15.0F, -21.0F, 8.3543F, 30.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        ModelPartData hexadecagon_r12 = ringBody.addChild("hexadecagon_r12", ModelPartBuilder.create().uv(168, 92).cuboid(-4.1772F, -15.0F, 18.0F, 8.3543F, 30.0F, 3.0F, new Dilation(0.0F))
                .uv(156, 134).cuboid(-4.1772F, -15.0F, -21.0F, 8.3543F, 30.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        ModelPartData flywheel = main.addChild("flywheel", ModelPartBuilder.create().uv(8, 222).cuboid(-1.6569F, -1.0005F, -4.0F, 3.3137F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(222, 172).cuboid(-1.6569F, -1.0005F, 3.0F, 3.3137F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(176, 221).cuboid(3.0F, -1.0005F, -1.6569F, 1.0F, 2.0F, 3.3137F, new Dilation(0.0F))
                .uv(0, 222).cuboid(-4.0F, -1.0005F, -1.6569F, 1.0F, 2.0F, 3.3137F, new Dilation(0.0F))
                .uv(20, 219).cuboid(-0.5F, -1.0005F, -3.0F, 1.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(212, 220).cuboid(-3.0F, -0.9985F, -0.5F, 6.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -10.0005F, 0.0F));

        ModelPartData cube_r5 = flywheel.addChild("cube_r5", ModelPartBuilder.create().uv(132, 221).cuboid(-3.0F, -1.0F, -0.5F, 6.0F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(34, 219).cuboid(-0.5F, -1.002F, -3.0F, 1.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(184, 221).cuboid(-4.0F, -1.0F, -1.6569F, 1.0F, 2.0F, 3.3137F, new Dilation(0.0F))
                .uv(168, 125).cuboid(3.0F, -1.0F, -1.6569F, 1.0F, 2.0F, 3.3137F, new Dilation(0.0F))
                .uv(222, 169).cuboid(-1.6569F, -1.0F, 3.0F, 3.3137F, 2.0F, 1.0F, new Dilation(0.0F))
                .uv(168, 130).cuboid(-1.6569F, -1.0F, -4.0F, 3.3137F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0005F, 0.0F, 0.0F, -0.7854F, 0.0F));

        ModelPartData shaft = flywheel.addChild("shaft", ModelPartBuilder.create().uv(124, 216).cuboid(-1.0F, -11.8384F, -1.1402F, 2.0F, 10.0F, 2.0F, new Dilation(0.0F))
                .uv(222, 175).cuboid(-1.0F, 0.1616F, -1.1402F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(48, 116).cuboid(-0.5F, 0.1616F, -0.6402F, 1.0F, 3.0F, 1.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, 0.8389F, 0.1402F));

        ModelPartData cube_r6 = shaft.addChild("cube_r6", ModelPartBuilder.create().uv(108, 144).cuboid(-0.5F, -1.5F, -0.5F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 3.8687F, 0.774F, 0.7854F, 0.0F, 0.0F));

        ModelPartData cylinderBase = main.addChild("cylinderBase", ModelPartBuilder.create().uv(0, 0).cuboid(-15.0F, -1.0F, -14.0F, 30.0F, 2.0F, 28.0F, new Dilation(0.0F))
                .uv(80, 42).cuboid(15.0F, -1.0F, -12.0F, 2.0F, 2.0F, 24.0F, new Dilation(0.0F))
                .uv(80, 68).cuboid(-17.0F, -1.0F, -12.0F, 2.0F, 2.0F, 24.0F, new Dilation(0.0F))
                .uv(188, 42).cuboid(-18.0F, -1.0F, -6.0F, 1.0F, 2.0F, 12.0F, new Dilation(0.0F))
                .uv(188, 56).cuboid(17.0F, -1.0F, -6.0F, 1.0F, 2.0F, 12.0F, new Dilation(0.0F))
                .uv(44, 144).cuboid(-13.0F, -1.0F, -16.0F, 26.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 116).cuboid(-11.0F, -1.0F, -18.0F, 22.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(56, 120).cuboid(-11.0F, -1.0F, 16.0F, 22.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(44, 148).cuboid(-13.0F, -1.0F, 14.0F, 26.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -15.001F, 0.0F));

        ModelPartData cylinderTop = main.addChild("cylinderTop", ModelPartBuilder.create().uv(166, 34).cuboid(-11.0F, -1.999F, 20.0F, 22.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(44, 156).cuboid(-13.0F, -1.999F, 18.0F, 26.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(178, 157).cuboid(-3.0F, -1.999F, 6.001F, 6.0F, 2.0F, 12.0F, new Dilation(0.0F))
                .uv(178, 171).cuboid(-3.0F, -1.999F, -10.001F, 6.0F, 2.0F, 12.0F, new Dilation(0.0F))
                .uv(0, 30).cuboid(-15.0F, -1.999F, -10.0F, 12.0F, 2.0F, 28.0F, new Dilation(0.0F))
                .uv(0, 60).cuboid(3.0F, -1.999F, -10.0F, 12.0F, 2.0F, 28.0F, new Dilation(0.0F))
                .uv(52, 94).cuboid(15.0F, -1.999F, -8.0F, 2.0F, 2.0F, 24.0F, new Dilation(0.0F))
                .uv(190, 84).cuboid(17.0F, -1.999F, -2.0F, 1.0F, 2.0F, 12.0F, new Dilation(0.0F))
                .uv(188, 70).cuboid(-18.0F, -1.999F, -2.0F, 1.0F, 2.0F, 12.0F, new Dilation(0.0F))
                .uv(0, 90).cuboid(-17.0F, -1.999F, -8.0F, 2.0F, 2.0F, 24.0F, new Dilation(0.0F))
                .uv(44, 152).cuboid(-13.0F, -1.999F, -12.0F, 26.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(160, 38).cuboid(-11.0F, -1.999F, -14.0F, 22.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -42.0F, -4.0F));

        ModelPartData drain = main.addChild("drain", ModelPartBuilder.create().uv(218, 0).cuboid(-0.5967F, -1.0F, -3.0F, 1.1935F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(210, 192).cuboid(-3.0F, -1.0F, -0.5967F, 6.0F, 2.0F, 1.1935F, new Dilation(0.0F))
                .uv(178, 185).cuboid(-4.0F, 3.0F, -4.0F, 8.0F, 2.0F, 8.0F, new Dilation(0.0F))
                .uv(214, 154).cuboid(4.0F, 1.0F, -4.0F, 2.0F, 2.0F, 8.0F, new Dilation(0.0F))
                .uv(214, 79).cuboid(-4.0F, 2.0F, -6.0F, 8.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(214, 164).cuboid(-4.0F, 2.0F, 4.0F, 8.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(68, 193).cuboid(-6.0F, 1.0F, -5.0F, 2.0F, 4.0F, 10.0F, new Dilation(0.0F))
                .uv(146, 221).cuboid(-6.0F, 0.0F, 5.0F, 2.0F, 5.0F, 1.0F, new Dilation(0.0F))
                .uv(62, 219).cuboid(-6.0F, 0.0F, -6.0F, 2.0F, 5.0F, 1.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -22.0F, 0.0F));

        ModelPartData hexadecagon_r13 = drain.addChild("hexadecagon_r13", ModelPartBuilder.create().uv(198, 220).cuboid(-3.0F, -1.001F, -0.5967F, 6.0F, 2.0F, 1.1935F, new Dilation(0.0F))
                .uv(218, 8).cuboid(-0.5967F, -1.001F, -3.0F, 1.1935F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        ModelPartData hexadecagon_r14 = drain.addChild("hexadecagon_r14", ModelPartBuilder.create().uv(200, 154).cuboid(-3.0F, -1.001F, -0.5967F, 6.0F, 2.0F, 1.1935F, new Dilation(0.0F))
                .uv(216, 84).cuboid(-0.5967F, -1.001F, -3.0F, 1.1935F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

        ModelPartData hexadecagon_r15 = drain.addChild("hexadecagon_r15", ModelPartBuilder.create().uv(218, 16).cuboid(-0.5967F, -1.0F, -3.0F, 1.1935F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        ModelPartData hexadecagon_r16 = drain.addChild("hexadecagon_r16", ModelPartBuilder.create().uv(92, 193).cuboid(-0.5967F, -1.0F, -3.0F, 1.1935F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        ModelPartData bowl = main.addChild("bowl", ModelPartBuilder.create(), ModelTransform.origin(0.0F, -27.2218F, 0.0F));

        ModelPartData cube_r7 = bowl.addChild("cube_r7", ModelPartBuilder.create().uv(132, 82).cuboid(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(-4.3176F, 0.3536F, 10.4237F, 1.0409F, 0.7119F, 1.2053F));

        ModelPartData cube_r8 = bowl.addChild("cube_r8", ModelPartBuilder.create().uv(132, 72).cuboid(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.3536F, 11.2825F, 1.5708F, 0.7854F, 1.5708F));

        ModelPartData cube_r9 = bowl.addChild("cube_r9", ModelPartBuilder.create().uv(132, 62).cuboid(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(4.3176F, 0.3536F, 10.4237F, 2.1007F, 0.7119F, 1.9363F));

        ModelPartData cube_r10 = bowl.addChild("cube_r10", ModelPartBuilder.create().uv(132, 52).cuboid(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(7.9779F, 0.3536F, 7.9779F, 2.5261F, 0.5236F, 2.1863F));

        ModelPartData cube_r11 = bowl.addChild("cube_r11", ModelPartBuilder.create().uv(132, 42).cuboid(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(10.4237F, 0.3536F, 4.3176F, 2.8567F, 0.274F, 2.3166F));

        ModelPartData cube_r12 = bowl.addChild("cube_r12", ModelPartBuilder.create().uv(0, 130).cuboid(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(11.2825F, 0.3536F, 0.0F, -3.1416F, 0.0F, 2.3562F));

        ModelPartData cube_r13 = bowl.addChild("cube_r13", ModelPartBuilder.create().uv(112, 124).cuboid(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(10.4237F, 0.3536F, -4.3176F, -2.8567F, -0.274F, 2.3166F));

        ModelPartData cube_r14 = bowl.addChild("cube_r14", ModelPartBuilder.create().uv(56, 124).cuboid(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(7.9779F, 0.3536F, -7.9779F, -2.5261F, -0.5236F, 2.1863F));

        ModelPartData cube_r15 = bowl.addChild("cube_r15", ModelPartBuilder.create().uv(0, 120).cuboid(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(4.3176F, 0.3536F, -10.4237F, -2.1007F, -0.7119F, 1.9363F));

        ModelPartData cube_r16 = bowl.addChild("cube_r16", ModelPartBuilder.create().uv(116, 20).cuboid(-11.0991F, -38.5427F, -41.829F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(-37.6519F, 27.3239F, -37.4055F, -1.5708F, -0.7854F, 1.5708F));

        ModelPartData cube_r17 = bowl.addChild("cube_r17", ModelPartBuilder.create().uv(116, 10).cuboid(-10.5F, -1.0F, -4.1771F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(-4.3176F, 0.3536F, -10.4237F, -1.0409F, -0.7119F, 1.2053F));

        ModelPartData cube_r18 = bowl.addChild("cube_r18", ModelPartBuilder.create().uv(116, 0).cuboid(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(-7.9779F, 0.3536F, -7.9779F, -0.6155F, -0.5236F, 0.9553F));

        ModelPartData cube_r19 = bowl.addChild("cube_r19", ModelPartBuilder.create().uv(104, 114).cuboid(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(-10.4237F, 0.3536F, -4.3176F, -0.2849F, -0.274F, 0.8249F));

        ModelPartData cube_r20 = bowl.addChild("cube_r20", ModelPartBuilder.create().uv(104, 104).cuboid(-10.5F, -1.0F, -4.1771F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(-10.4237F, 0.3536F, 4.3176F, 0.2849F, 0.274F, 0.8249F));

        ModelPartData cube_r21 = bowl.addChild("cube_r21", ModelPartBuilder.create().uv(104, 94).cuboid(-5.0F, -2.0F, -4.0F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(-15.8787F, -2.8284F, -0.1772F, 0.0F, 0.0F, 0.7854F));

        ModelPartData cube_r22 = bowl.addChild("cube_r22", ModelPartBuilder.create().uv(56, 134).cuboid(-10.5F, -1.0F, -4.1772F, 20.0F, 2.0F, 8.3543F, new Dilation(0.0F)), ModelTransform.of(-7.9779F, 0.3536F, 7.9779F, 0.6155F, 0.5236F, 0.9553F));
        return TexturedModelData.of(modelData, 256, 256);
    }

    public ModelPart getCylinderTop() {
        return cylinderTop;
    }

    public ModelPart getBowl() {
        return bowl;
    }
}