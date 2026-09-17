package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import dev.turtywurty.industria.renderer.block.DigesterBlockEntityRenderer.DigesterRenderState;
import dev.turtywurty.industria.util.IndustriaRenderTypes;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class DigesterModel extends Model<IndustriaBlockEntityRenderState> {
    private final ModelPart propeller;
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/digester.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("digester"), "main");

    public DigesterModel(ModelPart root) {
        this(root, false);
    }

    public DigesterModel(ModelPart root, boolean glassOnly) {
        super(root, glassOnly ? IndustriaRenderTypes.GLASS_RENDER_TYPE : RenderTypes::entityCutout);
        this.propeller = root.getChild("center").getChild("propeller");
        root.getChild("body").visible = !glassOnly;
        root.getChild("coils").visible = !glassOnly;
        root.getChild("center").visible = !glassOnly;
        root.getChild("glass").visible = glassOnly;
    }

    @Override
    public void setupAnim(IndustriaBlockEntityRenderState state) {
        super.setupAnim(state);
        this.propeller.yRot = state instanceof DigesterRenderState digester ? digester.bladeRotation : 0f;
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(39.0F, 24.0F, -39.0F));

        PartDefinition energyPort_r1 = body.addOrReplaceChild("energyPort_r1", CubeListBuilder.create().texOffs(146, 196).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-40.0F, -25.0F, 80.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition fluidPort_r1 = body.addOrReplaceChild("fluidPort_r1", CubeListBuilder.create().texOffs(144, 182).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-39.0F, -8.0F, -2.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition top = body.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 150).addBox(-28.0F, 0.5F, -12.0F, 56.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
        .texOffs(80, 296).addBox(-12.0F, 0.5F, -28.0F, 24.0F, 8.0F, 16.0F, new CubeDeformation(0.0F))
        .texOffs(160, 300).addBox(-12.0F, 0.5F, 12.0F, 24.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(-39.0F, -44.5F, 39.0F));

        PartDefinition cube_r1 = top.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(304, 108).addBox(-10.75F, -3.9F, -9.6F, 23.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.0F, 4.5F, 16.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r2 = top.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(304, 60).addBox(-10.75F, -3.9F, -6.8F, 23.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.0F, 4.5F, 13.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r3 = top.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(304, 84).addBox(-10.75F, -3.9F, -6.25F, 23.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-16.0F, 4.5F, -15.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r4 = top.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(240, 300).addBox(-10.75F, -3.9F, -7.75F, 23.0F, 8.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.0F, 4.5F, -15.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition slurryInputPort = top.addOrReplaceChild("slurryInputPort", CubeListBuilder.create(), PartPose.offset(0.0F, 42.75F, 0.0F));

        PartDefinition cube_r5 = slurryInputPort.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(300, 336).addBox(-7.5F, -9.0F, -2.5F, 11.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.6667F, -43.3333F, 2.0F, 0.0F, -1.5708F, -1.5708F));

        PartDefinition cube_r6 = slurryInputPort.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(202, 336).addBox(-3.0F, -11.5F, -1.0F, 4.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6667F, -44.3333F, -5.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition bolts = slurryInputPort.addOrReplaceChild("bolts", CubeListBuilder.create().texOffs(142, 320).addBox(4.0F, -4.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(148, 320).addBox(1.0F, 4.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(154, 320).addBox(-2.0F, 4.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(62, 338).addBox(-5.0F, 4.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(68, 338).addBox(4.0F, 4.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(74, 338).addBox(-5.0F, 1.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(286, 340).addBox(4.0F, 1.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(292, 340).addBox(-5.0F, -1.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(62, 341).addBox(4.0F, -1.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(68, 341).addBox(-5.0F, -4.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(74, 341).addBox(-2.0F, -4.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(162, 341).addBox(1.0F, -4.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3333F, -45.3333F, 0.0F, 0.0F, -1.5708F, -1.5708F));

        PartDefinition vent = top.addOrReplaceChild("vent", CubeListBuilder.create().texOffs(284, 60).addBox(-36.5F, -39.0F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
        .texOffs(144, 206).addBox(-35.5F, -44.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
        .texOffs(324, 336).addBox(-37.0F, -42.0F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 44.5F, 0.0F));

        PartDefinition ceiling = body.addOrReplaceChild("ceiling", CubeListBuilder.create().texOffs(0, 40).addBox(-79.0F, -36.0F, 23.0F, 80.0F, 8.0F, 32.0F, new CubeDeformation(0.0F))
        .texOffs(192, 80).addBox(-55.0F, -36.0F, -1.0F, 32.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
        .texOffs(112, 212).addBox(-55.0F, -36.0F, 55.0F, 32.0F, 8.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r7 = ceiling.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(224, 242).addBox(-17.0F, 0.6F, 17.5F, 34.0F, 8.0F, 22.0F, new CubeDeformation(0.0F))
        .texOffs(0, 80).addBox(-17.0F, 0.6F, -39.75F, 34.0F, 8.0F, 62.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-39.0F, -36.5F, 39.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r8 = ceiling.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(224, 0).addBox(-17.0F, 0.6F, -53.0F, 34.0F, 8.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-89.0F, -36.5F, 89.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r9 = ceiling.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(160, 150).addBox(-17.0F, 0.6F, -39.7843F, 34.0F, 8.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-39.0F, -36.5F, 39.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition spokes = ceiling.addOrReplaceChild("spokes", CubeListBuilder.create().texOffs(61, 354).addBox(-35.0F, -28.0F, -1.0F, 31.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-39.0F, 0.0F, 39.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube_r10 = spokes.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(61, 354).addBox(-30.0F, -1.0F, -1.0F, 31.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -27.0F, 5.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r11 = spokes.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(61, 354).addBox(-30.0F, -1.0F, -1.0F, 31.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -27.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition cube_r12 = spokes.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(61, 354).addBox(-30.0F, -1.0F, -1.0F, 31.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -27.0F, -5.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r13 = spokes.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(61, 354).addBox(-35.0F, -0.5F, -1.0F, 31.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -27.5F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r14 = spokes.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(61, 354).addBox(-35.0F, -0.5F, -1.0F, 31.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -27.5F, 0.0F, 0.0F, -2.3562F, 0.0F));

        PartDefinition cube_r15 = spokes.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(61, 354).addBox(-35.0F, -0.5F, -1.0F, 31.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -27.5F, 0.0F, 0.0F, 2.3562F, 0.0F));

        PartDefinition cube_r16 = spokes.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(61, 354).addBox(-35.0F, -0.5F, -1.0F, 31.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -27.5F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition wall = body.addOrReplaceChild("wall", CubeListBuilder.create().texOffs(0, 244).addBox(-7.0F, -28.0F, 23.0F, 8.0F, 20.0F, 32.0F, new CubeDeformation(0.0F))
        .texOffs(80, 244).addBox(-79.0F, -28.0F, 23.0F, 8.0F, 20.0F, 32.0F, new CubeDeformation(0.0F))
        .texOffs(112, 182).addBox(-29.0F, -28.0F, -1.0F, 6.0F, 20.0F, 10.0F, new CubeDeformation(0.0F))
        .texOffs(336, 28).addBox(-55.0F, -28.0F, -1.0F, 6.0F, 20.0F, 10.0F, new CubeDeformation(0.0F))
        .texOffs(142, 324).addBox(-49.0F, -28.0F, -1.0F, 20.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
        .texOffs(224, 60).addBox(-49.0F, -14.0F, -1.0F, 20.0F, 6.0F, 10.0F, new CubeDeformation(0.0F))
        .texOffs(0, 296).addBox(-55.0F, -28.0F, 71.0F, 32.0F, 20.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r17 = wall.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(274, 172).addBox(-17.0F, -11.4F, 31.5F, 34.0F, 20.0F, 8.0F, new CubeDeformation(0.0F))
        .texOffs(244, 272).addBox(-17.0F, -11.4F, -39.75F, 34.0F, 20.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-39.0F, -16.5F, 39.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r18 = wall.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(274, 144).addBox(-17.0F, -11.4F, -39.0F, 34.0F, 20.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-89.0F, -16.5F, 89.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r19 = wall.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(160, 272).addBox(-17.0F, -11.4F, -39.7843F, 34.0F, 20.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-39.0F, -16.5F, 39.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition window = body.addOrReplaceChild("window", CubeListBuilder.create().texOffs(62, 324).addBox(-49.5F, -25.5F, -1.5F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(70, 324).addBox(-30.5F, -25.5F, -1.5F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(336, 244).addBox(-47.5F, -25.5F, -1.5F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(336, 248).addBox(-47.5F, -15.5F, -1.5F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        // Keep the glass in a separate pass so the coils and propeller remain visible behind it.
        partdefinition.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(7, 358)
                .addBox(-48.0F, -24.0F, -1.0F, 18.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(39.0F, 24.0F, -39.0F));

        PartDefinition base = body.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-40.0F, 8.5F, -16.0F, 80.0F, 8.0F, 32.0F, new CubeDeformation(0.0F))
        .texOffs(192, 112).addBox(-16.0F, 8.5F, -40.0F, 32.0F, 8.0F, 24.0F, new CubeDeformation(0.0F))
        .texOffs(0, 182).addBox(-16.0F, 8.5F, 16.0F, 32.0F, 8.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(-39.0F, -16.5F, 39.0F));

        PartDefinition cube_r20 = base.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(224, 30).addBox(-17.0F, 0.6F, -53.0F, 34.0F, 8.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-50.0F, 8.0F, 50.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r21 = base.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(224, 212).addBox(-17.0F, 0.6F, 17.5F, 34.0F, 8.0F, 22.0F, new CubeDeformation(0.0F))
        .texOffs(0, 214).addBox(-17.0F, 0.6F, -39.75F, 34.0F, 8.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r22 = base.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(160, 181).addBox(-17.0F, 0.6F, -39.7843F, 34.0F, 8.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition coils = partdefinition.addOrReplaceChild("coils", CubeListBuilder.create().texOffs(192, 144).addBox(-14.0F, -12.0F, 30.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(336, 256).addBox(-7.5F, -23.1F, -30.1F, 15.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(266, 336).addBox(-7.5F, -15.9F, -30.1F, 15.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(336, 264).addBox(-7.5F, -9.9F, -30.1F, 15.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(336, 260).addBox(-7.5F, -12.9F, -30.1F, 15.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(336, 252).addBox(-7.5F, -26.1F, -30.1F, 15.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(224, 76).addBox(-14.0F, -15.0F, 30.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(160, 268).addBox(-14.0F, -18.0F, 30.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(274, 200).addBox(-14.0F, -21.0F, 30.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(274, 204).addBox(-14.0F, -24.0F, 30.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, 0.0F));

        PartDefinition cube_r23 = coils.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(334, 200).addBox(-14.0F, -7.1F, -0.5F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(334, 204).addBox(-14.0F, -4.1F, -0.5F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(334, 208).addBox(-14.0F, -1.1F, -0.5F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(336, 0).addBox(-14.0F, 1.9F, -0.5F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(336, 4).addBox(-14.0F, 4.9F, -0.5F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(21.0F, -17.0F, 21.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r24 = coils.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(336, 24).addBox(-14.0F, 4.9F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(336, 20).addBox(-14.0F, 1.9F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(336, 16).addBox(-14.0F, -1.1F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(336, 12).addBox(-14.0F, -4.1F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(336, 8).addBox(-14.0F, -7.1F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-22.0F, -17.0F, -22.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r25 = coils.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(274, 208).addBox(-14.0F, 5.0F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(304, 132).addBox(-14.0F, 2.0F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(304, 136).addBox(-14.0F, -1.0F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(304, 140).addBox(-14.0F, -4.0F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(202, 324).addBox(-14.0F, -7.0F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-31.0F, -17.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r26 = coils.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(328, 296).addBox(-14.0F, -7.1F, -0.5F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(322, 328).addBox(-14.0F, -4.1F, -0.5F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(202, 332).addBox(-14.0F, -1.1F, -0.5F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(262, 332).addBox(-14.0F, 1.9F, -0.5F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(322, 332).addBox(-14.0F, 4.9F, -0.5F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-21.0F, -17.0F, 21.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r27 = coils.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(328, 276).addBox(-14.0F, -7.1F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(328, 280).addBox(-14.0F, -4.1F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(328, 284).addBox(-14.0F, -1.1F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(328, 288).addBox(-14.0F, 1.9F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(328, 292).addBox(-14.0F, 4.9F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(23.0F, -17.0F, -21.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r28 = coils.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(262, 324).addBox(-14.0F, -7.0F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(322, 324).addBox(-14.0F, -4.0F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(202, 328).addBox(-14.0F, -1.0F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(262, 328).addBox(-14.0F, 2.0F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(328, 272).addBox(-14.0F, 5.0F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(31.0F, -17.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r29 = coils.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(284, 68).addBox(-4.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -24.0F, -29.0F, 0.0F, 0.0F, -0.2618F));

        PartDefinition cube_r30 = coils.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(284, 72).addBox(-4.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -21.0F, -29.0F, 0.0F, 0.0F, 0.2618F));

        PartDefinition cube_r31 = coils.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(266, 340).addBox(-4.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -16.0F, -29.0F, 0.0F, 0.0F, -0.2618F));

        PartDefinition cube_r32 = coils.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(142, 341).addBox(-4.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -16.0F, -29.0F, 0.0F, 0.0F, 0.2618F));

        PartDefinition cube_r33 = coils.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(336, 268).addBox(-4.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -13.0F, -29.0F, 0.0F, 0.0F, -0.2618F));

        PartDefinition cube_r34 = coils.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(142, 337).addBox(-4.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -10.0F, -29.0F, 0.0F, 0.0F, -0.2618F));

        PartDefinition cube_r35 = coils.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(162, 337).addBox(-4.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -10.0F, -29.0F, 0.0F, 0.0F, 0.2618F));

        PartDefinition cube_r36 = coils.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(182, 337).addBox(-4.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -13.0F, -29.0F, 0.0F, 0.0F, 0.2618F));

        PartDefinition cube_r37 = coils.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(284, 76).addBox(-4.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -21.0F, -29.0F, 0.0F, 0.0F, -0.2618F));

        PartDefinition cube_r38 = coils.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(252, 144).addBox(-4.0F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -24.0F, -29.0F, 0.0F, 0.0F, 0.2618F));

        PartDefinition center = partdefinition.addOrReplaceChild("center", CubeListBuilder.create().texOffs(234, 336).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
        .texOffs(336, 234).addBox(-4.0F, -28.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
        .texOffs(336, 212).addBox(-3.0F, -26.0F, -3.0F, 6.0F, 16.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition propeller = center.addOrReplaceChild("propeller", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition blade = propeller.addOrReplaceChild("blade", CubeListBuilder.create(), PartPose.offset(1.0F, -14.0F, -14.5F));

        PartDefinition cube_r39 = blade.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(80, 320).addBox(-5.0F, 0.0F, -11.5F, 8.0F, 1.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition blade2 = propeller.addOrReplaceChild("blade2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r40 = blade2.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(80, 320).addBox(-5.0F, 0.0F, -11.5F, 8.0F, 1.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, -14.5F, 0.0F, 0.0F, 0.7854F));

        PartDefinition blade3 = propeller.addOrReplaceChild("blade3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r41 = blade3.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(80, 320).addBox(-5.0F, 0.0F, -11.5F, 8.0F, 1.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, -14.5F, 0.0F, 0.0F, 0.7854F));

        PartDefinition blade4 = propeller.addOrReplaceChild("blade4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition cube_r42 = blade4.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(80, 320).addBox(-5.0F, 0.0F, -11.5F, 8.0F, 1.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, -14.5F, 0.0F, 0.0F, 0.7854F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }

}
