package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.Set;

public class WaterWheelModel extends Model<WaterWheelModel.WaterWheelModelRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("water_wheel"), "main");
	public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/water_wheel.png");

	private final ModelPart rotor;

    public WaterWheelModel(ModelPart root) {
		super(root, RenderTypes::entitySolid);
		this.rotor = root.getChild("rotor");
    }

	public static LayerDefinition createMainLayer() {
		var meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();

		PartDefinition rotor = partDefinition.addOrReplaceChild("rotor", CubeListBuilder.create(),
			PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, -Mth.HALF_PI, 0.0F));
		PartDefinition main = rotor.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition outerwoodrim = main.addOrReplaceChild("outerwoodrim", CubeListBuilder.create(), PartPose.offset(18.0F, -24.0F, 0.0F));

		PartDefinition outerwoodrimbase = outerwoodrim.addOrReplaceChild("outerwoodrimbase", CubeListBuilder.create().texOffs(23, 38).addBox(4.001F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(-18.0F, 0.0F, 0.0F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r1", CubeListBuilder.create().texOffs(0, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r2", CubeListBuilder.create().texOffs(0, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.3562F, 0.0F, 0.0F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r3", CubeListBuilder.create().texOffs(23, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.001F, 0.0F, 0.0F, -3.1416F, 0.0F, 0.0F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r4", CubeListBuilder.create().texOffs(0, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.3562F, 0.0F, 0.0F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r5", CubeListBuilder.create().texOffs(0, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r6", CubeListBuilder.create().texOffs(23, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.001F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r7", CubeListBuilder.create().texOffs(23, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.001F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r8", CubeListBuilder.create().texOffs(23, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.001F, 0.0F, 0.0F, -1.5708F, 0.0F, 3.1416F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r9", CubeListBuilder.create().texOffs(23, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.001F, 0.0F, 0.0F, 1.5708F, 0.0F, 3.1416F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r10", CubeListBuilder.create().texOffs(0, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.3562F, 0.0F, 3.1416F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r11", CubeListBuilder.create().texOffs(0, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, -3.1416F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r12", CubeListBuilder.create().texOffs(23, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.001F, 0.0F, 0.0F, 0.0F, 0.0F, 3.1416F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r13", CubeListBuilder.create().texOffs(0, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 3.1416F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r14", CubeListBuilder.create().texOffs(23, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.001F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        outerwoodrimbase.addOrReplaceChild("hexadecagon_r15", CubeListBuilder.create().texOffs(0, 38).addBox(4.0F, -8.5F, 15.0F, 2.0F, 17.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.3562F, 0.0F, -3.1416F));

        PartDefinition outerwoodrimparts = outerwoodrim.addOrReplaceChild("outerwoodrimparts", CubeListBuilder.create(), PartPose.offset(-18.0F, 0.0F, 0.0F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r16", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, 0.0F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r17", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.7489F, 0.0F, 0.0F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r18", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.7489F, 0.0F, 0.0F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r19", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.9635F, 0.0F, 0.0F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r20", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.1781F, 0.0F, 0.0F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r21", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r22", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.1781F, 0.0F, 0.0F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r23", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r24", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.7489F, 0.0F, 3.1416F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r25", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.9635F, 0.0F, 3.1416F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r26", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.7489F, 0.0F, -3.1416F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r27", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, -3.1416F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r28", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.1781F, 0.0F, -3.1416F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r29", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, -3.1416F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r30", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 3.1416F));

        outerwoodrimparts.addOrReplaceChild("hexadecagon_r31", CubeListBuilder.create().texOffs(15, 23).addBox(3.5F, -1.5F, 15.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.1781F, 0.0F, 3.1416F));

        PartDefinition corners = outerwoodrimparts.addOrReplaceChild("corners", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        corners.addOrReplaceChild("hexadecagon_r32", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        corners.addOrReplaceChild("hexadecagon_r33", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 3.1416F));

        corners.addOrReplaceChild("hexadecagon_r34", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.3562F, 0.0F, 0.0F));

        corners.addOrReplaceChild("hexadecagon_r35", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.3562F, 0.0F, 3.1416F));

        corners.addOrReplaceChild("hexadecagon_r36", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 3.1416F));

        corners.addOrReplaceChild("hexadecagon_r37", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        corners.addOrReplaceChild("hexadecagon_r38", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.3562F, 0.0F, 3.1416F));

        corners.addOrReplaceChild("hexadecagon_r39", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.3562F, 0.0F, 0.0F));

        corners.addOrReplaceChild("hexadecagon_r40", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 3.1416F, 0.0F, 0.0F));

        corners.addOrReplaceChild("hexadecagon_r41", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 3.1416F, 0.0F, 3.1416F));

        corners.addOrReplaceChild("hexadecagon_r42", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 3.1416F));

        corners.addOrReplaceChild("hexadecagon_r43", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 3.1416F));

        corners.addOrReplaceChild("hexadecagon_r44", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        corners.addOrReplaceChild("hexadecagon_r45", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 3.1416F));

        corners.addOrReplaceChild("hexadecagon_r46", CubeListBuilder.create().texOffs(0, 29).addBox(3.5F, -1.5F, 22.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition innerwalls = main.addOrReplaceChild("innerwalls", CubeListBuilder.create(), PartPose.offset(0.0F, -24.0F, 0.0F));
        CubeListBuilder board = CubeListBuilder.create()
            .texOffs(35, 39).addBox(-4, 15.5F, -6, 8, 1, 12, Set.of(Direction.DOWN))
            .texOffs(27, 52).addBox(-4, 15.5F, -6, 8, 1, 12, Set.of(Direction.UP))
            .texOffs(0, 35).addBox(-4, 15.5F, -6, 8, 1, 12, Set.of(Direction.NORTH, Direction.SOUTH));
        for (int i = 0; i < 8; i++) {
            innerwalls.addOrReplaceChild("board_" + i, board, PartPose.rotation(i * Mth.PI / 4, 0, 0));
        }

        PartDefinition support = main.addOrReplaceChild("support", CubeListBuilder.create(), PartPose.offset(0.0F, -24.0F, 0.0F));

		PartDefinition supportplanks = support.addOrReplaceChild("supportplanks", CubeListBuilder.create(), PartPose.offset(0.5F, 0.0F, 0.0F));

        supportplanks.addOrReplaceChild("octagon_r1", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, -2.3567F, 0.0308F, 3.1107F));

        supportplanks.addOrReplaceChild("octagon_r2", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.7849F, -0.0308F, -3.1107F));

        supportplanks.addOrReplaceChild("octagon_r3", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 2.3567F, -0.0308F, 3.1107F));

        supportplanks.addOrReplaceChild("octagon_r4", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, -0.7849F, 0.0308F, -3.1107F));

        supportplanks.addOrReplaceChild("octagon_r5", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 3.1416F, 0.0F, 3.098F));

        supportplanks.addOrReplaceChild("octagon_r6", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, -3.098F));

        supportplanks.addOrReplaceChild("octagon_r7", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 1.5708F, -0.0436F, 3.1416F));

        supportplanks.addOrReplaceChild("octagon_r8", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, -1.5708F, 0.0436F, 3.1416F));

        supportplanks.addOrReplaceChild("octagon_r9", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 1.5708F, -0.0436F, 0.0F));

        supportplanks.addOrReplaceChild("octagon_r10", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, -1.5708F, 0.0436F, 0.0F));

        supportplanks.addOrReplaceChild("octagon_r11", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 3.1416F, 0.0F, -0.0436F));

        supportplanks.addOrReplaceChild("octagon_r12", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0436F));

        supportplanks.addOrReplaceChild("octagon_r13", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 2.3567F, -0.0308F, -0.0309F));

        supportplanks.addOrReplaceChild("octagon_r14", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, -0.7849F, 0.0308F, 0.0309F));

        supportplanks.addOrReplaceChild("octagon_r15", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, -2.3567F, 0.0308F, -0.0309F));

        supportplanks.addOrReplaceChild("octagon_r16", CubeListBuilder.create().texOffs(15, 15).addBox(-7.6F, -18.0F, -1.5F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.7849F, -0.0308F, 0.0309F));

        PartDefinition supporthandle = support.addOrReplaceChild("supporthandle", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        supporthandle.addOrReplaceChild("hexadecagon_r47", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 3.1416F));

        supporthandle.addOrReplaceChild("hexadecagon_r48", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        supporthandle.addOrReplaceChild("hexadecagon_r49", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        supporthandle.addOrReplaceChild("hexadecagon_r50", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 3.1416F));

        PartDefinition supporthandle2 = support.addOrReplaceChild("supporthandle2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        supporthandle2.addOrReplaceChild("hexadecagon_r51", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 3.1416F));

        supporthandle2.addOrReplaceChild("hexadecagon_r52", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        supporthandle2.addOrReplaceChild("hexadecagon_r53", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        supporthandle2.addOrReplaceChild("hexadecagon_r54", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 3.1416F));

        PartDefinition supporthandle3 = support.addOrReplaceChild("supporthandle3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        supporthandle3.addOrReplaceChild("hexadecagon_r55", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 3.1416F));

        supporthandle3.addOrReplaceChild("hexadecagon_r56", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        supporthandle3.addOrReplaceChild("hexadecagon_r57", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        supporthandle3.addOrReplaceChild("hexadecagon_r58", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 3.1416F));

        PartDefinition supporthandle4 = support.addOrReplaceChild("supporthandle4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.3562F, 0.0F, 0.0F));

        supporthandle4.addOrReplaceChild("hexadecagon_r59", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 3.1416F));

        supporthandle4.addOrReplaceChild("hexadecagon_r60", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        supporthandle4.addOrReplaceChild("hexadecagon_r61", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        supporthandle4.addOrReplaceChild("hexadecagon_r62", CubeListBuilder.create().texOffs(0, 15).addBox(5.2F, -4.0F, 18.0F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 3.1416F));

        PartDefinition supportcenter = support.addOrReplaceChild("supportcenter", CubeListBuilder.create().texOffs(40, 0).addBox(7.5F, -28.5F, -4.5F, 3.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 24.0F, 0.0F));

        supportcenter.addOrReplaceChild("octagon_r17", CubeListBuilder.create().texOffs(40, 0).addBox(5.0F, -4.5F, -4.5F, 3.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -24.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition centerlink = support.addOrReplaceChild("centerlink", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        centerlink.addOrReplaceChild("octagon_r18", CubeListBuilder.create().texOffs(1, 1).addBox(-18.5F, -2.5F, -2.5F, 10.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.5F, -24.5F, 0.5F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition centermetal = support.addOrReplaceChild("centermetal", CubeListBuilder.create().texOffs(51, 26).addBox(-9.6F, -2.5F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.6F, 0.0F, 0.0F));

        centermetal.addOrReplaceChild("octagon_r19", CubeListBuilder.create().texOffs(51, 26).addBox(-9.0F, -2.5F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6F, 0.0F, 0.0F, -3.1416F, 0.0F, 3.1416F));

        PartDefinition waterfills = main.addOrReplaceChild("waterfills", CubeListBuilder.create(), PartPose.offset(0.0F, -24.0F, 0.0F));

        PartDefinition waterfillsflaps = waterfills.addOrReplaceChild("waterfillsflaps", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.3562F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.3562F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.1781F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9635F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -2.7489F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 2.7489F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.9635F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.1781F, 0.0F, 0.0F));

        waterfillsflaps.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(34, 20).addBox(-4.0F, -25.0F, -1.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
	}

	@Override
	public void setupAnim(WaterWheelModelRenderState state) {
		super.setupAnim(state);
		rotor.xRot = state.rotation();
	}

	public record WaterWheelModelRenderState(float rotation) {
	}
}
