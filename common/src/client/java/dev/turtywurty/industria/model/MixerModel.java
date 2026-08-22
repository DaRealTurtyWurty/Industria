package dev.turtywurty.industria.model;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.state.MixerRenderState;
import dev.turtywurty.turtymultiloader.client.registration.ClientRegistrations;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class MixerModel extends Model<MixerRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("mixer"), "main");
    public static final Identifier CLOSED_TEXTURE_LOCATION = Industria.id("textures/block/mixer_output_closed.png");
    public static final Identifier OPEN_TEXTURE_LOCATION = Industria.id("textures/block/mixer_output_open.png");
    private static final RenderPipeline GLASS_PIPELINE = ClientRegistrations.registerRenderPipeline(
            RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                    .withLocation(Industria.id("pipeline/mixer_glass"))
                    .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                    .withShaderDefine("PER_FACE_LIGHTING")
                    .withSampler("Sampler1")
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(false)
                    .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                    .build());
    private static final Function<Identifier, RenderType> GLASS_RENDER_TYPE = Util.memoize(texture ->
            ClientRegistrations.createRenderType("industria_mixer_glass",
                    RenderSetup.builder(GLASS_PIPELINE)
                            .withTexture("Sampler0", texture)
                            .useLightmap()
                            .useOverlay()
                            .affectsCrumbling()
                            .sortOnUpload()
                            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                            .createRenderSetup()));
    private final boolean glassOnly;
    private final ModelParts parts;

    public MixerModel(ModelPart root) {
        this(root, false);
    }

    public MixerModel(ModelPart root, boolean glassOnly) {
        super(root, glassOnly ? GLASS_RENDER_TYPE : RenderTypes::entityCutout);
        this.glassOnly = glassOnly;

        ModelPart main = root.getChild("main");
        ModelPart lights = main.getChild("lights");
        ModelPart blades = main.getChild("blades");
        if (glassOnly) {
            main.getChild("structure").visible = false;
            lights.visible = false;
            blades.visible = false;
            main.getChild("slurryExitPort").visible = false;
            main.getChild("itemExitPortNoPipe").visible = false;
            main.getChild("itemExitPipe").visible = false;
            main.getChild("itemInputPipe").visible = false;
            main.getChild("itemInputPortNoPipe").visible = false;
        } else {
            main.getChild("glass").visible = false;
        }

        this.parts = new ModelParts(
                main,
                blades.getChild("clockwise"),
                blades.getChild("counterClockwise"),
                lights.getChild("redLightOn"),
                lights.getChild("redLightOff"),
                lights.getChild("greenLightOn"),
                lights.getChild("greenLightOff"),
                main.getChild("itemExitPortNoPipe"),
                main.getChild("itemExitPipe"),
                main.getChild("itemInputPipe"),
                main.getChild("itemInputPortNoPipe")
        );
    }

    public static LayerDefinition getMainLayer() {
        var meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition main = root.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition structure = main.addOrReplaceChild("structure", CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -8.0F, -14.0F, 28.0F, 8.0F, 28.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36).addBox(-14.0F, -46.0F, -14.0F, 28.0F, 2.0F, 28.0F, new CubeDeformation(0.0F))
                .texOffs(32, 66).addBox(-14.0F, -44.0F, -14.0F, 2.0F, 36.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(84, 52).addBox(-4.0F, -12.0F, -4.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(40, 78).addBox(-2.0F, -41.0F, -2.0F, 4.0F, 29.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        structure.addOrReplaceChild("Center_r1", CubeListBuilder.create().texOffs(84, 40).addBox(-4.0F, -2.0F, -4.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -43.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        structure.addOrReplaceChild("EnergyPort_r1", CubeListBuilder.create().texOffs(72, 66).addBox(-2.0F, -16.25F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -31.75F, 0.0F, 0.0F, 3.1416F, 0.0F));

        structure.addOrReplaceChild("FluidPort_r1", CubeListBuilder.create().texOffs(40, 66).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, -47.0F, 0.0F, 1.5708F, 3.1416F, 1.5708F));

        structure.addOrReplaceChild("Support_r1", CubeListBuilder.create().texOffs(32, 66).addBox(-1.0F, -36.0F, -1.0F, 2.0F, 36.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -8.0F, -13.0F, 0.0F, -1.5708F, 0.0F));

        structure.addOrReplaceChild("Support_r2", CubeListBuilder.create().texOffs(32, 66).addBox(-1.0F, -36.0F, -1.0F, 2.0F, 36.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -8.0F, 13.0F, 0.0F, 3.1416F, 0.0F));

        structure.addOrReplaceChild("Support_r3", CubeListBuilder.create().texOffs(32, 66).addBox(-1.0F, -36.0F, -1.0F, 2.0F, 36.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, -8.0F, 13.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition glass = main.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(78, 68).addBox(-13.5F, -44.0F, -12.0F, 1.0F, 36.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(78, 68).addBox(12.5F, -44.0F, -12.0F, 1.0F, 36.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        glass.addOrReplaceChild("Pane_r1", CubeListBuilder.create().texOffs(78, 68).addBox(0.5F, -36.0F, -1.0F, 1.0F, 36.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -8.0F, 12.0F, 0.0F, -1.5708F, 0.0F));

        glass.addOrReplaceChild("Pane_r2", CubeListBuilder.create().texOffs(78, 68).addBox(-0.5F, -36.0F, -1.0F, 1.0F, 36.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -8.0F, -13.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition lights = main.addOrReplaceChild("lights", CubeListBuilder.create().texOffs(56, 78).addBox(-12.5F, -7.5F, -14.5F, 8.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 0.0F, 0.0F));

        lights.addOrReplaceChild("redLightOff", CubeListBuilder.create().texOffs(56, 99).addBox(-2.0F, -3.0F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, -4.0F, -14.0F));

        lights.addOrReplaceChild("greenLightOff", CubeListBuilder.create().texOffs(56, 95).addBox(-2.0F, -3.0F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -4.0F, -14.0F));

        lights.addOrReplaceChild("redLightOn", CubeListBuilder.create().texOffs(56, 107).addBox(-2.0F, -3.0F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, -4.0F, -14.0F));

        lights.addOrReplaceChild("greenLightOn", CubeListBuilder.create().texOffs(56, 103).addBox(-2.0F, -3.0F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -4.0F, -14.0F));

        PartDefinition blades = main.addOrReplaceChild("blades", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition clockwise = blades.addOrReplaceChild("clockwise", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition prop1 = clockwise.addOrReplaceChild("prop1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition oneZero = prop1.addOrReplaceChild("oneZero", CubeListBuilder.create(), PartPose.offset(0.0F, -14.5F, 0.0F));

        oneZero.addOrReplaceChild("Blade_r1", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition oneOne = prop1.addOrReplaceChild("oneOne", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        oneOne.addOrReplaceChild("Blade_r2", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.5F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition oneTwo = prop1.addOrReplaceChild("oneTwo", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        oneTwo.addOrReplaceChild("Blade_r3", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.5F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition prop2 = clockwise.addOrReplaceChild("prop2", CubeListBuilder.create(), PartPose.offset(0.0F, -8.0F, 0.0F));

        PartDefinition oneThree = prop2.addOrReplaceChild("oneThree", CubeListBuilder.create(), PartPose.offset(0.0F, -14.5F, 0.0F));

        oneThree.addOrReplaceChild("Blade_r4", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition oneFour = prop2.addOrReplaceChild("oneFour", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        oneFour.addOrReplaceChild("Blade_r5", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.5F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition oneFive = prop2.addOrReplaceChild("oneFive", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        oneFive.addOrReplaceChild("Blade_r6", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.5F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition prop5 = clockwise.addOrReplaceChild("prop5", CubeListBuilder.create(), PartPose.offset(0.0F, -16.0F, 0.0F));

        PartDefinition oneSix = prop5.addOrReplaceChild("oneSix", CubeListBuilder.create(), PartPose.offset(0.0F, -14.5F, 0.0F));

        oneSix.addOrReplaceChild("Blade_r7", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition oneSeven = prop5.addOrReplaceChild("oneSeven", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        oneSeven.addOrReplaceChild("Blade_r8", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.5F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition oneEight = prop5.addOrReplaceChild("oneEight", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        oneEight.addOrReplaceChild("Blade_r9", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.5F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition counterClockwise = blades.addOrReplaceChild("counterClockwise", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition prop3 = counterClockwise.addOrReplaceChild("prop3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition twoZero = prop3.addOrReplaceChild("twoZero", CubeListBuilder.create(), PartPose.offset(0.0F, -14.5F, 0.0F));

        twoZero.addOrReplaceChild("Blade_r10", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition twoOne = prop3.addOrReplaceChild("twoOne", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        twoOne.addOrReplaceChild("Blade_r11", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.5F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition twoTwo = prop3.addOrReplaceChild("twoTwo", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        twoTwo.addOrReplaceChild("Blade_r12", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.5F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition prop4 = counterClockwise.addOrReplaceChild("prop4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition twoThree = prop4.addOrReplaceChild("twoThree", CubeListBuilder.create(), PartPose.offset(0.0F, -14.5F, 0.0F));

        twoThree.addOrReplaceChild("Blade_r13", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition twoFour = prop4.addOrReplaceChild("twoFour", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        twoFour.addOrReplaceChild("Blade_r14", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.5F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition twoFive = prop4.addOrReplaceChild("twoFive", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        twoFive.addOrReplaceChild("Blade_r15", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.5F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition prop6 = counterClockwise.addOrReplaceChild("prop6", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -20.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition twoSix = prop6.addOrReplaceChild("twoSix", CubeListBuilder.create(), PartPose.offset(0.0F, -14.5F, 0.0F));

        twoSix.addOrReplaceChild("Blade_r16", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition twoSeven = prop6.addOrReplaceChild("twoSeven", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        twoSeven.addOrReplaceChild("Blade_r17", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.5F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition twoEight = prop6.addOrReplaceChild("twoEight", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        twoEight.addOrReplaceChild("Blade_r18", CubeListBuilder.create().texOffs(0, 106).addBox(-2.0F, -0.5F, 2.0F, 4.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.5F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition slurryExitPort = main.addOrReplaceChild("slurryExitPort", CubeListBuilder.create().texOffs(0, 45).addBox(-3.0F, -6.0F, -6.0F, 2.0F, 6.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(15.0F, -8.0F, 0.0F));

        slurryExitPort.addOrReplaceChild("Face_r1", CubeListBuilder.create().texOffs(104, 0).addBox(-7.5F, -9.5F, -2.5F, 11.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 4.0F, 2.0F, 0.0F, -1.5708F, 0.0F));

        slurryExitPort.addOrReplaceChild("bolts", CubeListBuilder.create().texOffs(109, 15).addBox(4.0F, -5.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(109, 18).addBox(1.0F, 4.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(109, 18).addBox(-2.0F, 4.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(109, 18).addBox(-5.0F, 4.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(109, 18).addBox(4.0F, 4.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(109, 15).addBox(-5.0F, 1.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(109, 15).addBox(4.0F, 1.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(109, 15).addBox(-5.0F, -2.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(109, 15).addBox(4.0F, -2.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(109, 15).addBox(-5.0F, -5.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(109, 15).addBox(-2.0F, -5.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(109, 15).addBox(1.0F, -5.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition itemExitPortNoPipe = main.addOrReplaceChild("itemExitPortNoPipe", CubeListBuilder.create().texOffs(76, 72).addBox(-14.0F, -12.0F, -4.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        itemExitPortNoPipe.addOrReplaceChild("itemPortR1", CubeListBuilder.create().texOffs(56, 66).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.0F, -8.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        main.addOrReplaceChild("itemExitPipe", CubeListBuilder.create().texOffs(0, 118).addBox(-24.0F, -10.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition itemInputPipe = main.addOrReplaceChild("itemInputPipe", CubeListBuilder.create().texOffs(108, 39).addBox(-18.0F, -48.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        itemInputPipe.addOrReplaceChild("itemInputPortR1", CubeListBuilder.create().texOffs(111, 61).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.0F, -47.0F, 0.0F, 0.0F, 3.1416F, 1.5708F));

        PartDefinition itemInputPortNoPipe = main.addOrReplaceChild("itemInputPortNoPipe", CubeListBuilder.create(), PartPose.offset(-9.0F, -47.0F, 0.0F));

        itemInputPortNoPipe.addOrReplaceChild("itemPortPipesR1", CubeListBuilder.create().texOffs(56, 66).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 1.5708F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(MixerRenderState state) {
        super.setupAnim(state);
        if (this.glassOnly)
            return;

        this.parts.clockwiseBlades().yRot = state.stirringRotation;
        this.parts.counterClockwiseBlades().yRot = -state.stirringRotation;

        this.parts.redLightOn().visible = !state.isMixing;
        this.parts.redLightOff().visible = state.isMixing;
        this.parts.greenLightOn().visible = state.isMixing;
        this.parts.greenLightOff().visible = !state.isMixing;

        this.parts.itemExitPortNoPipe().visible = !state.hasItemOutputConnection;
        this.parts.itemExitPipe().visible = state.hasItemOutputConnection;
        this.parts.itemInputPipe().visible = state.hasItemInputConnection;
        this.parts.itemInputPortNoPipe().visible = !state.hasItemInputConnection;
    }

    public record ModelParts(ModelPart main, ModelPart clockwiseBlades, ModelPart counterClockwiseBlades,
                             ModelPart redLightOn, ModelPart redLightOff, ModelPart greenLightOn,
                             ModelPart greenLightOff, ModelPart itemExitPortNoPipe, ModelPart itemExitPipe,
                             ModelPart itemInputPipe, ModelPart itemInputPortNoPipe) {
    }
}
