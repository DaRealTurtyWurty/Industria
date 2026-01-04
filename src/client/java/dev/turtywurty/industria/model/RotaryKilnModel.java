package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class RotaryKilnModel extends Model<RotaryKilnModel.RotaryKilnModelRenderState> {
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/rotary_kiln.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("rotary_kiln"), "main");

    public RotaryKilnModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("seg0", CubeListBuilder.create().texOffs(0, 0).addBox(-40.0F, -64.0F, -8.0F, 80.0F, 80.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition seg1 = modelPartData.addOrReplaceChild("seg1", CubeListBuilder.create().texOffs(386, 329).addBox(-40.0F, 0.0F, -8.0F, 30.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 97).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(386, 264).addBox(10.0F, 0.0F, -8.0F, 30.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(195, 591).addBox(-10.0F, 45.0F, -8.0F, 20.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 394).addBox(8.0F, -16.0F, -8.0F, 32.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(162, 396).addBox(-40.0F, -16.0F, -8.0F, 32.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(73, 589).addBox(-10.0F, 0.0F, 7.0F, 20.0F, 32.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(698, 569).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 26.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(324, 396).addBox(8.0F, 0.0F, -8.0F, 2.0F, 45.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(579, 0).addBox(-10.0F, 0.0F, -8.0F, 2.0F, 45.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -16.0F));

        seg1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(694, 464).addBox(-10.0F, 0.0F, -1.0F, 20.0F, 20.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 32.0F, 8.0F, -0.8727F, 0.0F, 0.0F));

        PartDefinition seg2 = modelPartData.addOrReplaceChild("seg2", CubeListBuilder.create().texOffs(97, 394).addBox(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 130).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(386, 394).addBox(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 163).addBox(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -32.0F));

        seg2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(195, 598).addBox(-10.0F, 20.0F, -1.0F, 20.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 32.0F, 24.0F, -0.8727F, 0.0F, 0.0F));

        PartDefinition rotate_seg2 = seg2.addOrReplaceChild("rotate_seg2", CubeListBuilder.create().texOffs(579, 80).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(579, 118).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(329, 646).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(403, 646).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        rotate_seg2.addOrReplaceChild("octagon_r1", CubeListBuilder.create().texOffs(366, 646).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(292, 646).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(579, 99).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(579, 61).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition seg3 = modelPartData.addOrReplaceChild("seg3", CubeListBuilder.create().texOffs(259, 396).addBox(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(193, 0).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 427).addBox(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(193, 33).addBox(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -48.0F));

        PartDefinition rotate_seg3 = seg3.addOrReplaceChild("rotate_seg3", CubeListBuilder.create().texOffs(579, 156).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(579, 194).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(477, 646).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(551, 646).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        rotate_seg3.addOrReplaceChild("octagon_r2", CubeListBuilder.create().texOffs(514, 646).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(440, 646).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(579, 175).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(579, 137).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition seg4 = modelPartData.addOrReplaceChild("seg4", CubeListBuilder.create().texOffs(162, 429).addBox(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(193, 66).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(451, 394).addBox(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(193, 99).addBox(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -64.0F));

        PartDefinition rotate_seg4 = seg4.addOrReplaceChild("rotate_seg4", CubeListBuilder.create().texOffs(579, 232).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(581, 413).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(625, 646).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(652, 94).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        rotate_seg4.addOrReplaceChild("octagon_r3", CubeListBuilder.create().texOffs(652, 57).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(588, 646).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(581, 394).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(579, 213).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition seg5 = modelPartData.addOrReplaceChild("seg5", CubeListBuilder.create().texOffs(65, 459).addBox(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(193, 132).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(324, 459).addBox(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(193, 165).addBox(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -80.0F));

        PartDefinition rotate_seg5 = seg5.addOrReplaceChild("rotate_seg5", CubeListBuilder.create().texOffs(584, 451).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(584, 489).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(652, 168).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(654, 384).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        rotate_seg5.addOrReplaceChild("octagon_r4", CubeListBuilder.create().texOffs(652, 205).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(652, 131).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(584, 470).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(581, 432).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition seg6 = modelPartData.addOrReplaceChild("seg6", CubeListBuilder.create().texOffs(389, 459).addBox(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 196).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(454, 459).addBox(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(193, 198).addBox(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -96.0F));

        PartDefinition rotate_seg6 = seg6.addOrReplaceChild("rotate_seg6", CubeListBuilder.create().texOffs(333, 589).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(479, 589).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(657, 458).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(37, 662).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        rotate_seg6.addOrReplaceChild("octagon_r5", CubeListBuilder.create().texOffs(0, 660).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(657, 421).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(406, 589).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(260, 589).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition seg7 = modelPartData.addOrReplaceChild("seg7", CubeListBuilder.create().texOffs(227, 461).addBox(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 229).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(479, 264).addBox(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(193, 231).addBox(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -112.0F));

        PartDefinition rotate_seg7 = seg7.addOrReplaceChild("rotate_seg7", CubeListBuilder.create().texOffs(195, 608).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(341, 608).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(662, 622).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(111, 665).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        rotate_seg7.addOrReplaceChild("octagon_r6", CubeListBuilder.create().texOffs(662, 659).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(74, 662).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(268, 608).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(552, 589).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition seg8 = modelPartData.addOrReplaceChild("seg8", CubeListBuilder.create().texOffs(479, 329).addBox(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 262).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 492).addBox(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(193, 264).addBox(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -128.0F));

        PartDefinition rotate_seg8 = seg8.addOrReplaceChild("rotate_seg8", CubeListBuilder.create().texOffs(487, 608).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(609, 251).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(185, 665).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(682, 242).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        rotate_seg8.addOrReplaceChild("octagon_r7", CubeListBuilder.create().texOffs(222, 665).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(148, 665).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(560, 608).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(414, 608).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition seg9 = modelPartData.addOrReplaceChild("seg9", CubeListBuilder.create().texOffs(130, 494).addBox(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 295).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(516, 394).addBox(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(193, 297).addBox(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -144.0F));

        PartDefinition rotate_seg9 = seg9.addOrReplaceChild("rotate_seg9", CubeListBuilder.create().texOffs(609, 289).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(609, 327).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(682, 316).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(296, 683).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        rotate_seg9.addOrReplaceChild("octagon_r8", CubeListBuilder.create().texOffs(259, 683).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(682, 279).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(609, 308).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(609, 270).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition seg10 = modelPartData.addOrReplaceChild("seg10", CubeListBuilder.create().texOffs(519, 459).addBox(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 328).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(65, 524).addBox(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(193, 330).addBox(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -160.0F));

        PartDefinition rotate_seg10 = seg10.addOrReplaceChild("rotate_seg10", CubeListBuilder.create().texOffs(609, 365).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(614, 19).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(370, 683).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(444, 683).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        rotate_seg10.addOrReplaceChild("octagon_r9", CubeListBuilder.create().texOffs(407, 683).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(333, 683).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(614, 0).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(609, 346).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition seg11 = modelPartData.addOrReplaceChild("seg11", CubeListBuilder.create().texOffs(292, 524).addBox(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 361).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(357, 524).addBox(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(193, 363).addBox(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -176.0F));

        PartDefinition rotate_seg11 = seg11.addOrReplaceChild("rotate_seg11", CubeListBuilder.create().texOffs(617, 508).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(617, 546).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(518, 683).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(592, 683).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        rotate_seg11.addOrReplaceChild("octagon_r10", CubeListBuilder.create().texOffs(555, 683).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(481, 683).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(617, 527).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(614, 38).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition seg12 = modelPartData.addOrReplaceChild("seg12", CubeListBuilder.create().texOffs(422, 524).addBox(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(386, 0).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(487, 524).addBox(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(386, 33).addBox(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -192.0F));

        PartDefinition rotate_seg12 = seg12.addOrReplaceChild("rotate_seg12", CubeListBuilder.create().texOffs(0, 622).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(625, 584).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(689, 37).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(689, 111).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        rotate_seg12.addOrReplaceChild("octagon_r11", CubeListBuilder.create().texOffs(689, 74).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(687, 0).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(73, 624).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(617, 565).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition seg13 = modelPartData.addOrReplaceChild("seg13", CubeListBuilder.create().texOffs(195, 526).addBox(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(386, 66).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(544, 264).addBox(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(386, 99).addBox(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -208.0F));

        PartDefinition rotate_seg13 = seg13.addOrReplaceChild("rotate_seg13", CubeListBuilder.create().texOffs(73, 643).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(219, 646).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 697).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(74, 699).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        rotate_seg13.addOrReplaceChild("octagon_r12", CubeListBuilder.create().texOffs(37, 699).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(666, 696).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(146, 646).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 641).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition seg14 = modelPartData.addOrReplaceChild("seg14", CubeListBuilder.create().texOffs(544, 329).addBox(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(386, 132).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(552, 524).addBox(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(386, 165).addBox(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -224.0F));

        PartDefinition rotate_seg14 = seg14.addOrReplaceChild("rotate_seg14", CubeListBuilder.create().texOffs(219, 627).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(365, 627).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(689, 185).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(690, 532).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        rotate_seg14.addOrReplaceChild("octagon_r13", CubeListBuilder.create().texOffs(690, 495).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(689, 148).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(292, 627).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(146, 627).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition seg15 = modelPartData.addOrReplaceChild("seg15", CubeListBuilder.create().texOffs(0, 557).addBox(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(386, 198).addBox(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(130, 559).addBox(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(386, 231).addBox(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -40.0F, -240.0F));

        PartDefinition rotate_seg15 = seg15.addOrReplaceChild("rotate_seg15", CubeListBuilder.create().texOffs(511, 627).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(633, 603).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(694, 390).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(629, 696).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        rotate_seg15.addOrReplaceChild("octagon_r14", CubeListBuilder.create().texOffs(694, 427).addBox(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(691, 353).addBox(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(584, 627).addBox(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(438, 627).addBox(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        return LayerDefinition.create(modelData, 1024, 1024);
    }

    @Override
    public void setupAnim(RotaryKilnModelRenderState state) {
        super.setupAnim(state);
        for (int i = 1; i < 16; i++) {
            ModelPart segment = getSegment(i);
            if (i <= state.segmentCount()) {
                if (i != 1) {
                    segment.getChild("rotate_seg" + i).zRot = state.rotationAngle();
                }

                segment.skipDraw = false;
                for (ModelPart part : segment.getAllParts()) {
                    part.skipDraw = false;
                }
            } else {
                segment.skipDraw = true;
                for (ModelPart part : segment.getAllParts()) {
                    part.skipDraw = true;
                }
            }
        }
    }

    public ModelPart getSegment(int index) {
        return this.root.getChild("seg" + index);
    }

    public record RotaryKilnModelRenderState(int segmentCount, float rotationAngle) {
    }
}