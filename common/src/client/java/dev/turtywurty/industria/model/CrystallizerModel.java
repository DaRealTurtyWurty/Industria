package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.util.IndustriaRenderTypes;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class CrystallizerModel extends Model<Void> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("crystallizer"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/crystallizer.png");

    public CrystallizerModel(ModelPart root) {
        this(root, false);
    }

    public CrystallizerModel(ModelPart root, boolean glassOnly) {
        super(root, glassOnly ? IndustriaRenderTypes.GLASS_RENDER_TYPE : RenderTypes::entityCutout);
        ModelPart main = root.getChild("main");
        main.getChild("support").visible = !glassOnly;
        main.getChild("window").visible = !glassOnly;
        main.getChild("structure").visible = !glassOnly;
        main.getChild("coils").visible = !glassOnly;
        main.getChild("decorPipes").visible = !glassOnly;
        main.getChild("input").visible = !glassOnly;
        main.getChild("output").visible = !glassOnly;
        main.getChild("energy").visible = !glassOnly;
        main.getChild("glass").visible = glassOnly;
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition main = root.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition support = main.addOrReplaceChild("support", CubeListBuilder.create(), PartPose.offset(0.0F, -36.1F, 0.0F));

        support.addOrReplaceChild("poles", CubeListBuilder.create().texOffs(152, 39).addBox(-29.0F, 10.0F, -1.0F, 3.0F, 17.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(137, 69).addBox(-5.0F, 10.0F, 22.9F, 3.0F, 17.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(152, 69).addBox(-29.0F, 10.0F, 22.9F, 3.0F, 17.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(137, 39).addBox(-5.0F, 10.0F, -1.0F, 3.0F, 17.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(15.4F, 9.1F, -12.5F));

        PartDefinition diagonalSupports = support.addOrReplaceChild("diagonalSupports", CubeListBuilder.create().texOffs(167, 61).addBox(12.0F, -6.0F, -13.0F, 0.0F, 12.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 29.1F, 0.0F));

        diagonalSupports.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(167, 61).addBox(12.0F, -6.0F, -13.0F, 0.0F, 12.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        diagonalSupports.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(167, 61).addBox(36.0F, -14.0F, -13.0F, 0.0F, 12.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(24.0F, 8.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        main.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(185, 41)
            .addBox(-14.0F, 0.0F, -1.0F, 12.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(8.0F, -30.1F, 19.0F));

        main.addOrReplaceChild("window", CubeListBuilder.create()
            .texOffs(214, 38).addBox(-15.5F, -1.5F, -1.5F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(185, 57).addBox(-13.5F, 7.5F, -1.5F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(175, 39).addBox(-2.5F, -1.5F, -1.5F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(185, 35).addBox(-13.5F, -1.5F, -1.5F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -30.1F, 19.0F));

        PartDefinition structure = main.addOrReplaceChild("structure", CubeListBuilder.create(), PartPose.offset(0.0F, -11.1F, 0.0F));

        PartDefinition body = structure.addOrReplaceChild("body", CubeListBuilder.create().texOffs(41, 0).addBox(-8.0F, -40.0F, 16.3F, 16.0F, 20.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(84, 0).addBox(-8.0F, -20.0F, 16.3F, 1.0F, 10.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(84, 0).addBox(7.0F, -20.0F, 16.3F, 1.0F, 10.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(41, 25).addBox(-8.0F, -10.0F, 16.3F, 16.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(166, 155).addBox(-8.0F, -42.001F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(222, 142).addBox(-6.0F, -44.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(218, 121).addBox(-6.0F, -45.001F, 7.5147F, 12.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(184, 176).addBox(-9.0F, -46.0F, -9.0F, 18.0F, 1.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(184, 176).addBox(-9.0F, -2.0F, -9.0F, 18.0F, 2.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(208, 198).addBox(-6.0F, -3.0F, -6.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 74).addBox(-8.0F, -15.0F, 16.3F, 16.0F, 32.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(218, 121).addBox(-6.0F, -20.001F, 7.5147F, 12.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(222, 142).addBox(-6.0F, -19.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(166, 155).addBox(-8.0F, -17.001F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -25.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -15.0F, 16.3F, 16.0F, 32.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -23.0F, 0.0F, 3.1416F, -0.7854F, 0.0F));

        body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, 16.3F, 16.0F, 32.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, -3.1416F, 0.7854F, 0.0F));

        body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 37).addBox(-8.0F, -16.0F, 16.3F, 16.0F, 32.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        body.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 37).addBox(-8.0F, -15.0F, 16.3F, 16.0F, 32.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(218, 121).addBox(-6.0F, -20.001F, 7.5147F, 12.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(222, 142).addBox(-6.0F, -19.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(166, 155).addBox(-8.0F, -17.001F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -25.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        body.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(218, 121).addBox(-6.0F, -1.0F, 7.5147F, 12.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -44.001F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        body.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(218, 132).addBox(-6.0F, -1.0F, 7.5147F, 12.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(222, 148).addBox(-6.0F, 0.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -44.0F, 0.0F, -3.1416F, -0.7854F, 3.1416F));

        body.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(218, 132).addBox(-6.0F, -1.0F, 7.5147F, 12.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(222, 148).addBox(-6.0F, 0.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(166, 164).addBox(-8.0F, 2.0F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -44.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        body.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(218, 132).addBox(-6.0F, -1.0F, 7.5147F, 12.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(222, 148).addBox(-6.0F, 0.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(166, 164).addBox(-8.0F, 2.0F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -44.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        body.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(218, 132).addBox(-6.0F, -1.0F, 7.5147F, 12.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -44.0F, 0.0F, 3.1416F, 0.7854F, 3.1416F));

        body.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(218, 121).addBox(-6.0F, -2.0F, 7.5147F, 12.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 3.1416F));

        body.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(218, 131).addBox(-6.0F, -2.0F, 7.5147F, 12.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.001F, 0.0F, 0.0F, 0.7854F, -3.1416F));

        body.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(222, 148).addBox(-6.0F, -2.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.7854F, -3.1416F));

        body.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(218, 121).addBox(-6.0F, -2.0F, 7.5147F, 12.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 1.5708F, 3.1416F));

        body.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(222, 142).addBox(-6.0F, -2.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 1.5708F, 3.1416F));

        body.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(218, 131).addBox(-6.0F, -2.0F, 7.5147F, 12.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.001F, 0.0F, 3.1416F, 0.7854F, 0.0F));

        body.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(222, 148).addBox(-6.0F, -2.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 3.1416F, 0.7854F, 0.0F));

        body.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(222, 142).addBox(-6.0F, -2.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        body.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(222, 148).addBox(-6.0F, -2.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 3.1416F, -0.7854F, 0.0F));

        body.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(218, 131).addBox(-6.0F, -2.0F, 7.5147F, 12.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.001F, 0.0F, 0.0F, -0.7854F, -3.1416F));

        body.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(222, 148).addBox(-6.0F, -2.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, -0.7854F, 3.1416F));

        body.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(218, 121).addBox(-6.0F, -2.0F, 7.5147F, 12.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, -1.5708F, -3.1416F));

        body.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(222, 142).addBox(-6.0F, -2.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, -1.5708F, -3.1416F));

        body.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(218, 131).addBox(-6.0F, -2.0F, 7.5147F, 12.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.001F, 0.0F, 3.1416F, -0.7854F, 0.0F));

        body.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(218, 121).addBox(-6.0F, -2.0F, 7.5147F, 12.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, -3.1416F, 0.0F, 0.0F));

        body.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(222, 142).addBox(-6.0F, -2.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 3.1416F, 0.0F, 0.0F));

        body.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(222, 142).addBox(-6.0F, 0.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -44.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        body.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(222, 148).addBox(-6.0F, 0.0F, 11.5147F, 12.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -44.0F, 0.0F, -3.1416F, 0.7854F, 3.1416F));

        body.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -15.0F, 16.3F, 16.0F, 32.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -23.0F, 0.0F, 0.0F, 0.7854F, 3.1416F));

        body.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(166, 164).addBox(-8.0F, 16.0F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -58.0F, 0.0F, -3.1416F, -0.7854F, 3.1416F));

        body.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(166, 164).addBox(-8.0F, 16.0F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -58.0F, 0.0F, -3.1416F, 0.7854F, 3.1416F));

        body.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(166, 155).addBox(-8.0F, 16.0F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -58.001F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        body.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(212, 155).addBox(-8.0F, 16.0F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 3.1416F, 0.0F, 0.0F));

        body.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(212, 164).addBox(-8.0F, 16.0F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 9.999F, 0.0F, 0.0F, -0.7854F, 3.1416F));

        body.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(212, 164).addBox(-8.0F, 16.0F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 9.999F, 0.0F, 0.0F, 0.7854F, 3.1416F));

        body.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(212, 155).addBox(-8.0F, 16.0F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.0F, -1.5708F, -3.1416F));

        body.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(212, 155).addBox(-8.0F, 16.0F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.0F, 0.0F, 3.1416F));

        body.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(212, 164).addBox(-8.0F, 16.0F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 9.999F, 0.0F, -3.1416F, 0.7854F, 0.0F));

        body.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(212, 164).addBox(-8.0F, 16.0F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 9.999F, 0.0F, 3.1416F, -0.7854F, 0.0F));

        body.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(212, 155).addBox(-8.0F, 16.0F, 13.3F, 16.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.0F, 1.5708F, 3.1416F));

        body.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, 16.3F, 16.0F, 32.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, 0.0F, -0.7854F, -3.1416F));

        PartDefinition blades = structure.addOrReplaceChild("blades", CubeListBuilder.create().texOffs(99, 0).addBox(-3.0F, -33.0F, -3.0F, 6.0F, 37.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.0F, 0.0F));

        PartDefinition firstLayerBlades = blades.addOrReplaceChild("firstLayerBlades", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        firstLayerBlades.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.5261F, 0.5236F, 2.1863F));

        firstLayerBlades.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.5261F, -0.5236F, 2.1863F));

        firstLayerBlades.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6155F, 0.5236F, 0.9553F));

        firstLayerBlades.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6155F, -0.5236F, 0.9553F));

        PartDefinition thirdLayerBlades = blades.addOrReplaceChild("thirdLayerBlades", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

        thirdLayerBlades.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.5261F, 0.5236F, 2.1863F));

        thirdLayerBlades.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.5261F, -0.5236F, 2.1863F));

        thirdLayerBlades.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6155F, 0.5236F, 0.9553F));

        thirdLayerBlades.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6155F, -0.5236F, 0.9553F));

        PartDefinition fifthLayerBlades = blades.addOrReplaceChild("fifthLayerBlades", CubeListBuilder.create(), PartPose.offset(0.0F, -24.0F, 0.0F));

        fifthLayerBlades.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.5261F, 0.5236F, 2.1863F));

        fifthLayerBlades.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.5261F, -0.5236F, 2.1863F));

        fifthLayerBlades.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6155F, 0.5236F, 0.9553F));

        fifthLayerBlades.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6155F, -0.5236F, 0.9553F));

        PartDefinition secondLayerBlades = blades.addOrReplaceChild("secondLayerBlades", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, 0.0F));

        secondLayerBlades.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        secondLayerBlades.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, -0.7854F, 1.5708F));

        secondLayerBlades.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.7854F, 1.5708F));

        secondLayerBlades.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 2.3562F));

        PartDefinition fourthLayerBlades = blades.addOrReplaceChild("fourthLayerBlades", CubeListBuilder.create(), PartPose.offset(0.0F, -18.0F, 0.0F));

        fourthLayerBlades.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        fourthLayerBlades.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, -0.7854F, 1.5708F));

        fourthLayerBlades.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.7854F, 1.5708F));

        fourthLayerBlades.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(97, 48).addBox(-3.3F, -0.7F, 2.0F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 2.3562F));

        PartDefinition coils = main.addOrReplaceChild("coils", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition fourthLayerCoils = coils.addOrReplaceChild("fourthLayerCoils", CubeListBuilder.create().texOffs(224, 64).addBox(-7.0F, -5.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -34.2F, 0.0F));

        fourthLayerCoils.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(224, 100).addBox(-7.0F, -5.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        fourthLayerCoils.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(224, 44).addBox(-7.0F, -5.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        fourthLayerCoils.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(224, 40).addBox(-7.0F, -5.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        fourthLayerCoils.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(224, 4).addBox(-7.0F, -5.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.7854F, 3.1416F));

        fourthLayerCoils.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(224, 0).addBox(-7.0F, -5.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        fourthLayerCoils.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(224, 84).addBox(-7.0F, -5.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, -0.7854F, 3.1416F));

        fourthLayerCoils.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(224, 28).addBox(-7.0F, -5.1F, -1.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition sixthLayerCoils = coils.addOrReplaceChild("sixthLayerCoils", CubeListBuilder.create().texOffs(224, 64).addBox(-7.0F, -5.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -44.2F, 0.0F));

        sixthLayerCoils.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(224, 100).addBox(-7.0F, -5.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        sixthLayerCoils.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(224, 44).addBox(-7.0F, -5.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        sixthLayerCoils.addOrReplaceChild("cube_r75", CubeListBuilder.create().texOffs(224, 40).addBox(-7.0F, -5.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        sixthLayerCoils.addOrReplaceChild("cube_r76", CubeListBuilder.create().texOffs(224, 4).addBox(-7.0F, -5.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.7854F, 3.1416F));

        sixthLayerCoils.addOrReplaceChild("cube_r77", CubeListBuilder.create().texOffs(224, 0).addBox(-7.0F, -5.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        sixthLayerCoils.addOrReplaceChild("cube_r78", CubeListBuilder.create().texOffs(224, 84).addBox(-7.0F, -5.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, -0.7854F, 3.1416F));

        sixthLayerCoils.addOrReplaceChild("cube_r79", CubeListBuilder.create().texOffs(224, 28).addBox(-7.0F, -5.1F, -1.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition thirdLayerCoils = coils.addOrReplaceChild("thirdLayerCoils", CubeListBuilder.create().texOffs(224, 68).addBox(-7.0F, -2.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -32.2F, 0.0F));

        thirdLayerCoils.addOrReplaceChild("cube_r80", CubeListBuilder.create().texOffs(224, 92).addBox(-7.0F, -2.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        thirdLayerCoils.addOrReplaceChild("cube_r81", CubeListBuilder.create().texOffs(224, 48).addBox(-7.0F, -2.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        thirdLayerCoils.addOrReplaceChild("cube_r82", CubeListBuilder.create().texOffs(224, 88).addBox(-7.0F, -2.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        thirdLayerCoils.addOrReplaceChild("cube_r83", CubeListBuilder.create().texOffs(224, 8).addBox(-7.0F, -2.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.7854F, 3.1416F));

        thirdLayerCoils.addOrReplaceChild("cube_r84", CubeListBuilder.create().texOffs(224, 24).addBox(-7.0F, -2.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        thirdLayerCoils.addOrReplaceChild("cube_r85", CubeListBuilder.create().texOffs(224, 76).addBox(-7.0F, -2.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, -0.7854F, 3.1416F));

        thirdLayerCoils.addOrReplaceChild("cube_r86", CubeListBuilder.create().texOffs(224, 80).addBox(-7.0F, -2.1F, -1.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition fifthLayerCoils = coils.addOrReplaceChild("fifthLayerCoils", CubeListBuilder.create().texOffs(224, 68).addBox(-7.0F, -2.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -42.2F, 0.0F));

        fifthLayerCoils.addOrReplaceChild("cube_r87", CubeListBuilder.create().texOffs(224, 92).addBox(-7.0F, -2.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        fifthLayerCoils.addOrReplaceChild("cube_r88", CubeListBuilder.create().texOffs(224, 48).addBox(-7.0F, -2.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        fifthLayerCoils.addOrReplaceChild("cube_r89", CubeListBuilder.create().texOffs(224, 88).addBox(-7.0F, -2.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        fifthLayerCoils.addOrReplaceChild("cube_r90", CubeListBuilder.create().texOffs(224, 8).addBox(-7.0F, -2.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.7854F, 3.1416F));

        fifthLayerCoils.addOrReplaceChild("cube_r91", CubeListBuilder.create().texOffs(224, 24).addBox(-7.0F, -2.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        fifthLayerCoils.addOrReplaceChild("cube_r92", CubeListBuilder.create().texOffs(224, 76).addBox(-7.0F, -2.1F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, -0.7854F, 3.1416F));

        fifthLayerCoils.addOrReplaceChild("cube_r93", CubeListBuilder.create().texOffs(224, 80).addBox(-7.0F, -2.1F, -1.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition firstLayerCoils = coils.addOrReplaceChild("firstLayerCoils", CubeListBuilder.create(), PartPose.offset(0.0F, -23.9F, 0.0F));

        firstLayerCoils.addOrReplaceChild("cube_r94", CubeListBuilder.create().texOffs(207, 0).addBox(-7.0F, -1.6F, 15.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(207, 0).addBox(5.0F, -1.6F, 15.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 0.0F));

        firstLayerCoils.addOrReplaceChild("cube_r95", CubeListBuilder.create().texOffs(224, 56).addBox(-7.0F, -0.4F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.2F, 0.0F, 0.0F, -0.7854F, 3.1416F));

        firstLayerCoils.addOrReplaceChild("cube_r96", CubeListBuilder.create().texOffs(213, 9).addBox(-6.2131F, -3.8366F, 15.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3F, 1.1F, -0.001F, -3.1416F, 0.0F, 0.5236F));

        firstLayerCoils.addOrReplaceChild("cube_r97", CubeListBuilder.create().texOffs(200, 0).addBox(-4.0F, -1.6F, 15.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(224, 12).addBox(-7.0F, -0.4F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.2F, 0.0F, -3.1416F, 0.0F, 0.0F));

        firstLayerCoils.addOrReplaceChild("cube_r98", CubeListBuilder.create().texOffs(202, 9).addBox(3.2131F, -3.8366F, 15.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3F, 1.1F, -0.001F, 3.1416F, 0.0F, -0.5236F));

        firstLayerCoils.addOrReplaceChild("cube_r99", CubeListBuilder.create().texOffs(224, 96).addBox(-7.0F, -0.4F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.2F, 0.0F, 0.0F, 0.7854F, 3.1416F));

        firstLayerCoils.addOrReplaceChild("cube_r100", CubeListBuilder.create().texOffs(224, 108).addBox(-7.0F, -0.4F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.2F, 0.0F, 0.0F, 1.5708F, 3.1416F));

        firstLayerCoils.addOrReplaceChild("cube_r101", CubeListBuilder.create().texOffs(224, 116).addBox(-7.0F, -0.4F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.2F, 0.0F, -3.1416F, 0.7854F, 0.0F));

        firstLayerCoils.addOrReplaceChild("cube_r102", CubeListBuilder.create().texOffs(224, 104).addBox(-7.0F, -0.4F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.2F, 0.0F, 3.1416F, -0.7854F, 0.0F));

        firstLayerCoils.addOrReplaceChild("cube_r103", CubeListBuilder.create().texOffs(224, 16).addBox(-7.0F, -0.4F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.2F, 0.0F, 0.0F, -1.5708F, 3.1416F));

        PartDefinition secondLayerCoils = coils.addOrReplaceChild("secondLayerCoils", CubeListBuilder.create(), PartPose.offset(-4.2F, -28.9F, -16.0F));

        secondLayerCoils.addOrReplaceChild("cube_r104", CubeListBuilder.create().texOffs(202, 9).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 2.618F));

        secondLayerCoils.addOrReplaceChild("cube_r105", CubeListBuilder.create().texOffs(213, 9).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.4F, 0.0F, 0.0F, 0.0F, 0.0F, -2.618F));

        secondLayerCoils.addOrReplaceChild("cube_r106", CubeListBuilder.create().texOffs(207, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.2F, 0.6F, 0.001F, 0.0F, 0.0F, -3.1416F));

        secondLayerCoils.addOrReplaceChild("cube_r107", CubeListBuilder.create().texOffs(200, 0).addBox(-4.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, -0.6F, 0.001F, 0.0F, 0.0F, -3.1416F));

        secondLayerCoils.addOrReplaceChild("cube_r108", CubeListBuilder.create().texOffs(207, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8F, 0.6F, 0.001F, 0.0F, 0.0F, -3.1416F));

        secondLayerCoils.addOrReplaceChild("cube_r109", CubeListBuilder.create().texOffs(224, 60).addBox(-7.0F, 0.9F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, -1.3F, 16.0F, 0.0F, -0.7854F, 0.0F));

        secondLayerCoils.addOrReplaceChild("cube_r110", CubeListBuilder.create().texOffs(224, 52).addBox(-7.0F, 0.9F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, -1.3F, 16.0F, 0.0F, 1.5708F, 0.0F));

        secondLayerCoils.addOrReplaceChild("cube_r111", CubeListBuilder.create().texOffs(224, 36).addBox(-7.0F, 0.9F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, -1.3F, 16.0F, 0.0F, 0.7854F, 0.0F));

        secondLayerCoils.addOrReplaceChild("cube_r112", CubeListBuilder.create().texOffs(224, 32).addBox(-7.0F, 0.9F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, -1.3F, 16.0F, -3.1416F, 0.7854F, 3.1416F));

        secondLayerCoils.addOrReplaceChild("cube_r113", CubeListBuilder.create().texOffs(224, 20).addBox(-7.0F, 0.9F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, -1.3F, 16.0F, -3.1416F, 0.0F, 3.1416F));

        secondLayerCoils.addOrReplaceChild("cube_r114", CubeListBuilder.create().texOffs(224, 72).addBox(-7.0F, 0.9F, -17.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.2F, -1.3F, 16.0F, -3.1416F, -0.7854F, 3.1416F));

        secondLayerCoils.addOrReplaceChild("cube_r115", CubeListBuilder.create().texOffs(224, 112).addBox(-7.0F, 0.9F, -1.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.2F, -1.3F, 16.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition decorPipes = main.addOrReplaceChild("decorPipes", CubeListBuilder.create(), PartPose.offset(22.0F, -45.5F, -22.0F));

        PartDefinition pipes = decorPipes.addOrReplaceChild("pipes", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        pipes.addOrReplaceChild("cube_r116", CubeListBuilder.create().texOffs(49, 34).addBox(-10.5F, -2.0F, -2.0F, 17.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 9.0F, 18.0F, 1.5708F, 0.0F, -1.5708F));

        pipes.addOrReplaceChild("cube_r117", CubeListBuilder.create().texOffs(76, 66).addBox(-19.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(76, 57).addBox(-19.0F, -23.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 21.5F, 35.0F, 0.0F, -1.5708F, 0.0F));

        pipes.addOrReplaceChild("cube_r118", CubeListBuilder.create().texOffs(56, 34).addBox(-15.0F, -2.0F, -2.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.1F, 21.499F, 14.9F, 0.0F, -0.7854F, 0.0F));

        pipes.addOrReplaceChild("cube_r119", CubeListBuilder.create().texOffs(62, 34).addBox(-15.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.601F, 21.5F, 15.6F, 0.0F, -0.7854F, 0.0F));

        pipes.addOrReplaceChild("cube_r120", CubeListBuilder.create().texOffs(60, 34).addBox(-19.0F, -2.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 21.5F, 29.0F, 0.0F, -1.5708F, 0.0F));

        pipes.addOrReplaceChild("cube_r121", CubeListBuilder.create().texOffs(55, 34).addBox(-10.0F, -2.0F, -24.0F, 11.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-22.0F, 0.499F, 24.1F, 0.0F, -2.3562F, 0.0F));

        pipes.addOrReplaceChild("cube_r122", CubeListBuilder.create().texOffs(52, 34).addBox(-2.0F, -2.0F, -24.0F, 14.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-22.0F, 0.5F, 22.0F, 0.0F, -1.5708F, 0.0F));

        pipes.addOrReplaceChild("cube_r123", CubeListBuilder.create().texOffs(62, 34).addBox(-10.0F, 13.5F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.8F, -15.0F, 43.9F, 0.0F, -0.7854F, 0.0F));

        PartDefinition inputs = decorPipes.addOrReplaceChild("inputs", CubeListBuilder.create(), PartPose.offset(-22.0F, 21.5F, 22.0F));

        inputs.addOrReplaceChild("cube_r124", CubeListBuilder.create().texOffs(204, 19).addBox(19.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.7F, -21.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        inputs.addOrReplaceChild("cube_r125", CubeListBuilder.create().texOffs(204, 19).addBox(19.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, 0.0F, 0.6F, 0.0F, 0.7854F, 0.0F));

        PartDefinition input = main.addOrReplaceChild("input", CubeListBuilder.create(), PartPose.offset(0.0F, -45.0F, 0.0F));

        input.addOrReplaceChild("cube_r126", CubeListBuilder.create().texOffs(204, 19).addBox(19.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        input.addOrReplaceChild("cube_r127", CubeListBuilder.create().texOffs(204, 19).addBox(19.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition output = main.addOrReplaceChild("output", CubeListBuilder.create(), PartPose.offset(0.0F, -4.1F, 0.0F));

        output.addOrReplaceChild("itemOutputOutside_r1", CubeListBuilder.create().texOffs(185, 19).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        output.addOrReplaceChild("itemOutputOutside_r2", CubeListBuilder.create().texOffs(185, 19).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition pipe = output.addOrReplaceChild("pipe", CubeListBuilder.create().texOffs(43, 47).addBox(-19.0F, -1.0F, -2.0F, 22.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(43, 57).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -1.0F, 0.0F));

        pipe.addOrReplaceChild("itemOutputPipe_r1", CubeListBuilder.create().texOffs(185, 19).addBox(20.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 1.0F, 0.0F, 3.1416F, 0.0F, -3.1416F));

        PartDefinition energy = main.addOrReplaceChild("energy", CubeListBuilder.create().texOffs(96, 65).addBox(-4.0F, -14.75F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -48.25F, 0.0F));

        energy.addOrReplaceChild("energyInput_r1", CubeListBuilder.create().texOffs(102, 86).addBox(-2.0F, -15.75F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        return LayerDefinition.create(mesh, 256, 256);
    }
}
