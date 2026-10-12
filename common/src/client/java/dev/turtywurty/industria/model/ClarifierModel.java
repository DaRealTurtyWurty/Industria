package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.state.ClarifierRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class ClarifierModel extends Model<ClarifierRenderState> {
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/clarifier.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("clarifier"), "main");
    private final ModelPart spin;

    public ClarifierModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.spin = root.getChild("column").getChild("spin");
    }

    @Override
    public void setupAnim(ClarifierRenderState state) {
        super.setupAnim(state);
        this.spin.yRot = state.bladeRotation;
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition supports = root.addOrReplaceChild("supports", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        supports.addOrReplaceChild("cube_r", CubeListBuilder.create().texOffs(100, 127).addBox(1.0F, -16.0F, -1.0F, 0.0F, 16.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(35.0F, 18.0F, -12.0F, -0.0F, -0.0F, 0.0F));
        supports.addOrReplaceChild("cube_r_1", CubeListBuilder.create().texOffs(100, 127).addBox(36.0F, -15.0F, -13.0F, 0.0F, 16.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, -0.0F, -1.570796F, 0.0F));
        supports.addOrReplaceChild("cube_r_2", CubeListBuilder.create().texOffs(100, 127).addBox(36.0F, -15.0F, -13.0F, 0.0F, 16.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, -0.0F, 3.141593F, 0.0F));
        supports.addOrReplaceChild("cube_r_3", CubeListBuilder.create().texOffs(100, 127).addBox(36.0F, -15.0F, -13.0F, 0.0F, 16.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, -0.0F, 1.570796F, 0.0F));
        supports.addOrReplaceChild("cube_r_4", CubeListBuilder.create().texOffs(0, 13).addBox(35.0F, -15.0F, -15.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        supports.addOrReplaceChild("cube_r_5", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.0F, 10.0F, 36.0F, -0.0F, 3.141593F, 0.0F));
        supports.addOrReplaceChild("cube_r_6", CubeListBuilder.create().texOffs(0, 13).addBox(35.0F, -15.0F, -15.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, -0.0F, -1.570796F, 0.0F));
        supports.addOrReplaceChild("cube_r_7", CubeListBuilder.create().texOffs(0, 13).addBox(35.0F, -15.0F, -15.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, -0.0F, 3.141593F, 0.0F));
        supports.addOrReplaceChild("cube_r_8", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-36.0F, 10.0F, -14.0F, -0.0F, 1.570796F, 0.0F));
        supports.addOrReplaceChild("cube_r_9", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.0F, 10.0F, -36.0F, -0.0F, -0.0F, 0.0F));
        supports.addOrReplaceChild("cube_r_10", CubeListBuilder.create().texOffs(0, 13).addBox(35.0F, -15.0F, -15.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 17.0F, 0.0F, -0.0F, 1.570796F, 0.0F));
        supports.addOrReplaceChild("cube_r_11", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(36.0F, 10.0F, 14.0F, -0.0F, -1.570796F, 0.0F));
        PartDefinition column = root.addOrReplaceChild("column", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        column.addOrReplaceChild("cube_r_12", CubeListBuilder.create().texOffs(106, 115).addBox(4.0F, -62.0F, -22.0F, 4.0F, 32.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 31.0F, 20.0F, -0.0F, -0.0F, 0.0F));
        PartDefinition walls2 = column.addOrReplaceChild("walls2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        walls2.addOrReplaceChild("cube_r_13", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -7.243F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, -0.0F, -1.570796F, 0.0F));
        walls2.addOrReplaceChild("cube_r_14", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -7.243F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, -0.0F, -2.356194F, 0.0F));
        walls2.addOrReplaceChild("cube_r_15", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -7.243F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        walls2.addOrReplaceChild("cube_r_16", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -7.243F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, -0.0F, -0.785398F, 0.0F));
        walls2.addOrReplaceChild("cube_r_17", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -7.243F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, -0.0F, 1.570796F, 0.0F));
        walls2.addOrReplaceChild("cube_r_18", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -7.243F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, -0.0F, 0.785398F, 0.0F));
        walls2.addOrReplaceChild("cube_r_19", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -7.243F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, -0.0F, 3.141593F, 0.0F));
        walls2.addOrReplaceChild("cube_r_20", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -7.243F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, -0.0F, 2.356194F, 0.0F));
        PartDefinition spin = column.addOrReplaceChild("spin", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -15.16667F, 0.0F, -0.0F, -0.0F, 0.0F));
        spin.addOrReplaceChild("cube_r_21", CubeListBuilder.create().texOffs(126, 113).addBox(1.0F, -28.0F, -1.0F, 0.0F, 28.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 11.16667F, -3.0F, -0.0F, -0.0F, 0.0F));
        spin.addOrReplaceChild("cube_r_22", CubeListBuilder.create().texOffs(126, 113).addBox(0.0F, -14.0F, -4.0F, 0.0F, 28.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.83333F, -4.0F, -0.0F, -1.570796F, 0.0F));
        spin.addOrReplaceChild("cube_r_23", CubeListBuilder.create().texOffs(126, 113).addBox(-4.0F, -28.0F, -4.0F, 0.0F, 28.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.16667F, 0.0F, -0.0F, 3.141593F, 0.0F));
        spin.addOrReplaceChild("cube_r_24", CubeListBuilder.create().texOffs(126, 113).addBox(-4.0F, -14.0F, -4.0F, 0.0F, 28.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.83333F, 0.0F, -0.0F, 1.570796F, 0.0F));
        PartDefinition blades = spin.addOrReplaceChild("blades", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 11.16667F, 0.0F, -0.0F, -0.0F, 0.0F));
        PartDefinition blade0 = blades.addOrReplaceChild("blade0", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.0F, 0.0F, -3.0F, -0.0F, -0.0F, 0.0F));
        blade0.addOrReplaceChild("cube_r_25", CubeListBuilder.create().texOffs(0, 11).addBox(-7.0F, -1.0F, 0.0F, 14.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 1.0F, 32.0F, -0.0F, -0.785398F, 0.0F));
        blade0.addOrReplaceChild("cube_r_26", CubeListBuilder.create().texOffs(0, 11).addBox(-7.0F, -1.0F, 0.0F, 14.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 1.0F, 25.0F, -0.0F, -0.785398F, 0.0F));
        blade0.addOrReplaceChild("cube_r_27", CubeListBuilder.create().texOffs(0, 11).addBox(-7.0F, -1.0F, 0.0F, 14.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 1.0F, 18.0F, -0.0F, -0.785398F, 0.0F));
        blade0.addOrReplaceChild("cube_r_28", CubeListBuilder.create().texOffs(0, 11).addBox(-7.0F, -1.0F, 0.0F, 14.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 1.0F, 11.0F, -0.0F, -0.785398F, 0.0F));
        blade0.addOrReplaceChild("cube_r_29", CubeListBuilder.create().texOffs(61, 150).addBox(0.0F, -8.0F, 0.0F, 0.0F, 8.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, 7.0F, -0.0F, -0.0F, 0.523599F));
        blade0.addOrReplaceChild("cube_r_30", CubeListBuilder.create().texOffs(0, 150).addBox(0.0F, -8.0F, 0.0F, 0.0F, 8.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, 7.0F, -0.0F, -0.0F, 1.570796F));
        blade0.addOrReplaceChild("cube_r_31", CubeListBuilder.create().texOffs(61, 142).addBox(0.0F, -8.0F, 0.0F, 0.0F, 8.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, 0.0F, 7.0F, -0.0F, -0.0F, -0.523599F));
        PartDefinition blade1 = blades.addOrReplaceChild("blade1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0F, 3.141593F, 0.0F));
        blade1.addOrReplaceChild("cube_r_32", CubeListBuilder.create().texOffs(0, 11).addBox(-7.0F, -1.0F, 0.0F, 14.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 29.0F, -0.0F, -0.785398F, 0.0F));
        blade1.addOrReplaceChild("cube_r_33", CubeListBuilder.create().texOffs(0, 11).addBox(-7.0F, -1.0F, 0.0F, 14.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 22.0F, -0.0F, -0.785398F, 0.0F));
        blade1.addOrReplaceChild("cube_r_34", CubeListBuilder.create().texOffs(0, 11).addBox(-7.0F, -1.0F, 0.0F, 14.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 15.0F, -0.0F, -0.785398F, 0.0F));
        blade1.addOrReplaceChild("cube_r_35", CubeListBuilder.create().texOffs(0, 11).addBox(-7.0F, -1.0F, 0.0F, 14.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 8.0F, -0.0F, -0.785398F, 0.0F));
        blade1.addOrReplaceChild("cube_r_36", CubeListBuilder.create().texOffs(122, 142).addBox(0.0F, -8.0F, 0.0F, 0.0F, 8.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 4.0F, -0.0F, -0.0F, 0.523599F));
        blade1.addOrReplaceChild("cube_r_37", CubeListBuilder.create().texOffs(0, 142).addBox(0.0F, -8.0F, 0.0F, 0.0F, 8.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 4.0F, -0.0F, -0.0F, 1.570796F));
        blade1.addOrReplaceChild("cube_r_38", CubeListBuilder.create().texOffs(122, 150).addBox(0.0F, -8.0F, 0.0F, 0.0F, 8.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, 4.0F, -0.0F, -0.0F, -0.523599F));
        PartDefinition walls = root.addOrReplaceChild("walls", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        PartDefinition input = walls.addOrReplaceChild("input", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        input.addOrReplaceChild("fluid_port_r", CubeListBuilder.create().texOffs(100, 34).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(39.0F, -28.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        input.addOrReplaceChild("display_pipe_r", CubeListBuilder.create().texOffs(46, 46).addBox(-28.0F, -4.0F, -1.0F, 29.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(35.0F, -26.0F, -1.0F, -0.0F, -0.0F, 0.0F));
        PartDefinition output = walls.addOrReplaceChild("output", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        output.addOrReplaceChild("item_port_r", CubeListBuilder.create().texOffs(0, 34).addBox(-4.0F, -3.0F, -3.0F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, -39.0F, -0.0F, 1.570796F, 0.0F));
        walls.addOrReplaceChild("cube_r_39", CubeListBuilder.create().texOffs(116, 34).addBox(-16.0F, -33.0F, 0.0F, 32.0F, 34.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, -38.63F, -0.0F, -0.0F, 0.0F));
        walls.addOrReplaceChild("cube_r_40", CubeListBuilder.create().texOffs(116, 34).addBox(-16.0F, -33.0F, -38.63F, 32.0F, 34.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.0F, 0.785398F, 0.0F));
        walls.addOrReplaceChild("cube_r_41", CubeListBuilder.create().texOffs(116, 34).addBox(-16.0F, -33.0F, -38.63F, 32.0F, 34.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.0F, 1.570796F, 0.0F));
        walls.addOrReplaceChild("cube_r_42", CubeListBuilder.create().texOffs(116, 34).addBox(-16.0F, -33.0F, -38.63F, 32.0F, 34.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.0F, 2.356194F, 0.0F));
        walls.addOrReplaceChild("cube_r_43", CubeListBuilder.create().texOffs(116, 34).addBox(-16.0F, -33.0F, -38.63F, 32.0F, 34.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.0F, 3.141593F, 0.0F));
        walls.addOrReplaceChild("cube_r_44", CubeListBuilder.create().texOffs(116, 34).addBox(-16.0F, -33.0F, -38.63F, 32.0F, 34.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.0F, -2.356194F, 0.0F));
        walls.addOrReplaceChild("cube_r_45", CubeListBuilder.create().texOffs(116, 34).addBox(-16.0F, -33.0F, -38.63F, 32.0F, 34.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.0F, -1.570796F, 0.0F));
        walls.addOrReplaceChild("cube_r_46", CubeListBuilder.create().texOffs(116, 34).addBox(-16.0F, -33.0F, -38.63F, 32.0F, 34.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.0F, -0.785398F, 0.0F));
        walls.addOrReplaceChild("cube_r_47", CubeListBuilder.create().texOffs(72, 79).addBox(-71.0F, 0.0F, -1.0F, 72.0F, 0.0F, 72.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(35.0F, -0.1F, -35.0F, -0.0F, -0.0F, 0.0F));
        PartDefinition ladder = root.addOrReplaceChild("ladder", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 7.0F, 60.0F, -0.0F, -0.0F, 0.0F));
        ladder.addOrReplaceChild("cube_r_48", CubeListBuilder.create().texOffs(10, 65).addBox(-8.0F, -63.0F, -22.0F, 16.0F, 33.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 30.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        PartDefinition walkway = root.addOrReplaceChild("walkway", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        walkway.addOrReplaceChild("cube_r_49", CubeListBuilder.create().texOffs(0, 54).addBox(-8.0F, -32.0F, -22.0F, 16.0F, 2.0F, 46.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 14.0F, -0.0F, -0.0F, 0.0F));
        PartDefinition railing = walkway.addOrReplaceChild("railing", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -39.0F, -8.0F, -0.0F, -0.0F, 0.0F));
        railing.addOrReplaceChild("cube_r_50", CubeListBuilder.create().texOffs(0, 107).addBox(0.0F, -8.0F, -8.0F, 0.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0F, -1.570796F, 0.0F));
        railing.addOrReplaceChild("cube_r_51", CubeListBuilder.create().texOffs(0, 107).addBox(8.0F, -46.0F, -22.0F, 0.0F, 16.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 38.0F, 22.0F, -0.0F, -0.0F, 0.0F));
        railing.addOrReplaceChild("cube_r_52", CubeListBuilder.create().texOffs(0, 91).addBox(8.0F, -46.0F, -22.0F, 0.0F, 16.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-16.0F, 38.0F, 22.0F, -0.0F, -0.0F, 0.0F));
        PartDefinition floor = root.addOrReplaceChild("floor", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        floor.addOrReplaceChild("cube_r_53", CubeListBuilder.create().texOffs(0, 0).addBox(-71.0F, -2.0F, -1.0F, 72.0F, 2.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(35.0F, 3.0F, -15.0F, -0.0F, -0.0F, 0.0F));
        floor.addOrReplaceChild("cube_r_54", CubeListBuilder.create().texOffs(0, 0).addBox(-36.0F, 0.0F, -16.0F, 72.0F, 1.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.0F, -2.356194F, 0.0F));
        floor.addOrReplaceChild("cube_r_55", CubeListBuilder.create().texOffs(0, 0).addBox(-36.0F, 0.0F, -16.0F, 72.0F, 1.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.0F, -0.785398F, 0.0F));
        floor.addOrReplaceChild("cube_r_56", CubeListBuilder.create().texOffs(0, 0).addBox(-71.0F, -2.0F, -1.0F, 72.0F, 2.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.0F, 3.0F, 35.0F, -0.0F, -1.570796F, 0.0F));
        PartDefinition hopper = root.addOrReplaceChild("hopper", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 36.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        hopper.addOrReplaceChild("cube_r_57", CubeListBuilder.create().texOffs(32, 120).addBox(-8.0F, -11.75F, -19.314F, 16.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.25F, 0.0F, -0.0F, -2.356194F, 0.0F));
        hopper.addOrReplaceChild("cube_r_58", CubeListBuilder.create().texOffs(68, 120).mirror().addBox(-8.0F, -11.75F, -19.314F, 16.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -16.25F, 0.0F, -0.0F, -0.0F, 0.0F));
        hopper.addOrReplaceChild("cube_r_59", CubeListBuilder.create().texOffs(32, 120).addBox(-8.0F, -11.75F, -19.314F, 16.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.25F, 0.0F, -0.0F, -0.785398F, 0.0F));
        hopper.addOrReplaceChild("cube_r_60", CubeListBuilder.create().texOffs(32, 120).addBox(-8.0F, -11.75F, -19.314F, 16.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.25F, 0.0F, -0.0F, -1.570796F, 0.0F));
        hopper.addOrReplaceChild("cube_r_61", CubeListBuilder.create().texOffs(32, 120).addBox(-8.0F, -11.75F, -19.314F, 16.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.25F, 0.0F, -0.0F, 0.785398F, 0.0F));
        hopper.addOrReplaceChild("cube_r_62", CubeListBuilder.create().texOffs(32, 120).addBox(-8.0F, -11.75F, -19.314F, 16.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.25F, 0.0F, -0.0F, 1.570796F, 0.0F));
        hopper.addOrReplaceChild("cube_r_63", CubeListBuilder.create().texOffs(32, 120).addBox(-8.0F, -11.75F, -19.314F, 16.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.25F, 0.0F, -0.0F, 2.356194F, 0.0F));
        hopper.addOrReplaceChild("cube_r_64", CubeListBuilder.create().texOffs(32, 120).addBox(-8.0F, -11.75F, -19.314F, 16.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.25F, 0.0F, -0.0F, 3.141593F, 0.0F));
        hopper.addOrReplaceChild("cube_r_65", CubeListBuilder.create().texOffs(0, 102).addBox(-18.0F, 2.25F, -8.0F, 36.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.25F, 0.0F, -0.0F, 1.570796F, 0.0F));
        hopper.addOrReplaceChild("cube_r_66", CubeListBuilder.create().texOffs(0, 102).addBox(-18.0F, 2.25F, -8.0F, 36.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -17.25F, 0.0F, -0.0F, -2.356194F, 0.0F));
        hopper.addOrReplaceChild("cube_r_67", CubeListBuilder.create().texOffs(0, 102).addBox(-18.0F, 2.25F, -8.0F, 36.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.25F, 0.0F, -0.0F, -0.0F, 0.0F));
        hopper.addOrReplaceChild("cube_r_68", CubeListBuilder.create().texOffs(0, 102).addBox(-18.0F, 2.25F, -8.0F, 36.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -17.25F, 0.0F, -0.0F, 2.356194F, 0.0F));
        PartDefinition fluid_output = hopper.addOrReplaceChild("fluid_output", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        fluid_output.addOrReplaceChild("out_display_pipe_r", CubeListBuilder.create().texOffs(0, 46).addBox(-18.0F, -4.0F, -1.0F, 19.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -20.0F, -20.0F, -0.0F, -1.570796F, 0.0F));
        fluid_output.addOrReplaceChild("fluid_out_port_r", CubeListBuilder.create().texOffs(22, 34).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -22.0F, -39.0F, -0.0F, 1.570796F, 0.0F));
        PartDefinition energy_port = root.addOrReplaceChild("energy_port", CubeListBuilder.create(), PartPose.offsetAndRotation(40.0F, -5.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        energy_port.addOrReplaceChild("cube_r_69", CubeListBuilder.create().texOffs(32, 34).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, -0.0F, -0.0F, 1.570796F));
        energy_port.addOrReplaceChild("cube_r_70", CubeListBuilder.create().texOffs(8, 18).addBox(0.0F, -11.0F, -1.0F, 1.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -2.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        energy_port.addOrReplaceChild("cube_r_71", CubeListBuilder.create().texOffs(38, 43).addBox(-28.0F, -1.0F, -1.0F, 29.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -13.0F, 0.0F, -0.0F, -0.0F, 0.0F));
        return LayerDefinition.create(mesh, 302, 224);
    }
}
