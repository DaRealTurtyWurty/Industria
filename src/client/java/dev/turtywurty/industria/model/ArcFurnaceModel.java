package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class ArcFurnaceModel extends Model<Void> {
    public static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath("minecraft", "textures/block/iron_block.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("arc_furnace"), "main");

    private final ModelPart main;
    private final ModelPart frame;
    private final ModelPart walls;
    private final ModelPart upper_walls;
    private final ModelPart pistons;
    private final ModelPart east_piston;
    private final ModelPart east_transformer;
    private final ModelPart middle_piston;
    private final ModelPart middle_transformer;
    private final ModelPart west_piston;
    private final ModelPart west_transformer;
    private final ModelPart transformer_cabinet;
    private final ModelPart pipes;
    private final ModelPart fluid;
    private final ModelPart gas;

    public ArcFurnaceModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        this.main = root.getChild("main");
        this.frame = this.main.getChild("frame");
        this.walls = this.frame.getChild("walls");
        this.upper_walls = this.walls.getChild("upper_walls");
        this.pistons = this.main.getChild("pistons");
        this.east_piston = this.pistons.getChild("east_piston");
        this.east_transformer = this.east_piston.getChild("east_transformer");
        this.middle_piston = this.pistons.getChild("middle_piston");
        this.middle_transformer = this.middle_piston.getChild("middle_transformer");
        this.middle_transformer.getChild("middle_transformer_pont_0");
        this.west_piston = this.pistons.getChild("west_piston");
        this.west_transformer = this.west_piston.getChild("west_transformer");
        this.transformer_cabinet = this.main.getChild("transformer_cabinet");
        this.pipes = this.main.getChild("pipes");
        this.fluid = this.pipes.getChild("fluid");
        this.gas = this.pipes.getChild("gas");
    }

    public static LayerDefinition createMainLayer() {
        var meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition main = partDefinition.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(12.0667F, -1.1223F, -11.4849F));

        main.addOrReplaceChild("base", CubeListBuilder.create().texOffs(-2, -3).addBox(5.0F, -2.1F, -16.3F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(-17, -3).addBox(-10.0F, -7.1F, -16.3F, 20.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(1, -3).addBox(-10.0F, -2.1F, -16.3F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(-94, -62).addBox(-23.0F, -9.1F, -11.3F, 46.0F, 12.0F, 64.0F, new CubeDeformation(0.0F))
                .texOffs(-133, -78).addBox(-40.0F, 2.9F, -19.3F, 80.0F, 8.0F, 80.0F, new CubeDeformation(0.0F)), PartPose.offset(-12.0667F, 14.2223F, -9.2151F));

        PartDefinition frame = main.addOrReplaceChild("frame", CubeListBuilder.create().texOffs(5, -3).addBox(17.0F, -15.5385F, -5.6923F, 6.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(6, -2).addBox(17.0F, -11.5385F, -4.6923F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(7, -1).addBox(17.0F, -5.5385F, -3.6923F, 6.0F, 12.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(6, -2).addBox(17.0F, 6.4615F, -4.6923F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(-6, -2).addBox(-17.0F, -15.5385F, -4.6923F, 34.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(-34, -32).addBox(-15.0F, -16.5385F, -0.6923F, 30.0F, 5.0F, 34.0F, new CubeDeformation(0.0F))
                .texOffs(5, -3).addBox(17.0F, 12.4615F, -5.6923F, 6.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(5, -3).addBox(-23.0F, 12.4615F, -5.6923F, 6.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(6, -2).addBox(-23.0F, 6.4615F, -4.6923F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(7, -1).addBox(-23.0F, -5.5385F, -3.6923F, 6.0F, 12.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(6, -2).addBox(-23.0F, -11.5385F, -4.6923F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(5, -3).addBox(-23.0F, -15.5385F, -5.6923F, 6.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-12.0667F, -11.3392F, -12.8228F));

        PartDefinition walls = frame.addOrReplaceChild("walls", CubeListBuilder.create().texOffs(-16, -4).addBox(-17.0F, -38.0F, 21.0F, 34.0F, 32.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-32, -40).addBox(-23.0F, -38.0F, -15.0F, 6.0F, 32.0F, 42.0F, new CubeDeformation(0.0F))
                .texOffs(-32, -40).addBox(17.0F, -38.0F, -15.0F, 6.0F, 32.0F, 42.0F, new CubeDeformation(0.0F))
                .texOffs(-5, -13).addBox(-23.0F, -38.0F, 27.0F, 46.0F, 32.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(20, -6).addBox(-14.0F, -3.0F, 42.0F, 28.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.4615F, 14.3077F));

        PartDefinition upper_walls = walls.addOrReplaceChild("upper_walls", CubeListBuilder.create().texOffs(5, -3).addBox(-3.0F, -19.0F, -15.0F, 6.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(6, -2).addBox(-3.0F, -24.0F, -14.0F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-18.0F, -23.0F, 0.0F));

        upper_walls.addOrReplaceChild("upper_frame", CubeListBuilder.create().texOffs(5, -3).addBox(7.0F, -2.0F, 0.0F, 6.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(-25, -33).addBox(7.0F, -7.0F, 5.0F, 6.0F, 9.0F, 35.0F, new CubeDeformation(0.0F))
                .texOffs(-25, -33).addBox(-29.0F, -7.0F, 5.0F, 6.0F, 9.0F, 35.0F, new CubeDeformation(0.0F))
                .texOffs(-24, -12).addBox(-23.0F, -7.0F, 26.0F, 30.0F, 9.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(-30, -12).addBox(-29.0F, -13.0F, 26.0F, 42.0F, 6.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(6, -2).addBox(7.0F, -7.0F, 1.0F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(26.0F, -17.0F, -15.0F));

        main.addOrReplaceChild("furnace", CubeListBuilder.create().texOffs(-28, -28).addBox(-17.0F, -16.625F, -17.75F, 34.0F, 6.0F, 30.0F, new CubeDeformation(0.0F))
                .texOffs(-28, -28).addBox(-17.0F, 5.375F, -17.75F, 34.0F, 6.0F, 30.0F, new CubeDeformation(0.0F))
                .texOffs(-22, -28).addBox(-11.0F, 3.375F, -17.75F, 4.0F, 2.0F, 30.0F, new CubeDeformation(0.0F))
                .texOffs(-2, -8).addBox(-7.0F, 3.375F, 2.25F, 14.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(-24, -28).addBox(7.0F, 3.375F, -17.75F, 4.0F, 2.0F, 30.0F, new CubeDeformation(0.0F))
                .texOffs(0, -28).addBox(11.0F, -10.625F, -17.75F, 6.0F, 16.0F, 30.0F, new CubeDeformation(0.0F))
                .texOffs(8, -4).addBox(-11.0F, -10.625F, 6.25F, 22.0F, 16.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, -28).addBox(-17.0F, -10.625F, -17.75F, 6.0F, 16.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offset(-12.0667F, -6.2527F, 10.2349F));

        main.addOrReplaceChild("control_panel", CubeListBuilder.create().texOffs(-6, -6).addBox(-5.3333F, -12.6667F, -5.8333F, 12.0F, 24.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(-10, -10).addBox(-5.3333F, 0.3333F, 2.1667F, 10.0F, 11.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(2, 1).addBox(-4.3333F, -11.6667F, -6.8333F, 10.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(16.2667F, 5.789F, -10.6818F));

        main.addOrReplaceChild("coolant_tank", CubeListBuilder.create().texOffs(-11, -12).addBox(-7.0F, 13.5417F, -7.0F, 14.0F, 6.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(-15, -14).addBox(-8.0F, -11.4583F, -8.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(-11, -12).addBox(-7.0F, -8.4583F, -7.0F, 14.0F, 22.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(2, 1).addBox(-7.0F, 14.5417F, -8.0F, 14.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, 1).addBox(-7.0F, 14.5417F, 7.0F, 14.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(2, -12).addBox(7.0F, 14.5417F, -7.0F, 1.0F, 5.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(2, -12).addBox(-8.0F, 14.5417F, -7.0F, 1.0F, 5.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(19.9333F, -2.4194F, 22.4849F));

        main.addOrReplaceChild("coolant_tank2", CubeListBuilder.create().texOffs(-15, -14).addBox(-8.0F, 13.5417F, -8.0F, 16.0F, 6.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(-15, -14).addBox(-8.0F, -11.4583F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(-11, -12).addBox(-7.0F, -9.4583F, -7.0F, 14.0F, 23.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(-43.0667F, -2.4194F, -15.5151F));

        main.addOrReplaceChild("oxygen_tank", CubeListBuilder.create().texOffs(-23, -12).addBox(-7.0F, 24.38F, -7.0F, 14.0F, 7.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(-21, -10).addBox(-8.0F, 24.38F, -6.0F, 1.0F, 7.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(-21, -10).addBox(7.0F, 24.38F, -6.0F, 1.0F, 7.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(-9, 1).addBox(-6.0F, 24.38F, -8.0F, 12.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-9, 1).addBox(-6.0F, 24.38F, 7.0F, 12.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-20, -10).addBox(-6.0F, -10.62F, -6.0F, 12.0F, 35.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(-17, -8).addBox(-5.0F, -11.62F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(-13, -6).addBox(-4.0F, -12.62F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(-10, -4).addBox(-3.0F, -13.62F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-12, -4).addBox(-5.0F, -12.62F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-6, 1).addBox(-3.0F, -12.62F, -5.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-13, -4).addBox(4.0F, -12.62F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-6, 1).addBox(-3.0F, -12.62F, 4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-7, 1).addBox(-4.0F, -11.62F, -6.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-14, -6).addBox(-6.0F, -11.62F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(-7, 1).addBox(-4.0F, -11.62F, 5.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-14, -6).addBox(5.0F, -11.62F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(-19, -8).addBox(-7.0F, -10.62F, -5.0F, 1.0F, 35.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(-19, -8).addBox(6.0F, -10.62F, -5.0F, 1.0F, 35.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(-7, 1).addBox(-5.0F, -10.62F, -7.0F, 10.0F, 35.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-7, 1).addBox(-5.0F, -10.62F, 6.0F, 10.0F, 35.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-43.0667F, -14.2577F, 15.4849F));

        main.addOrReplaceChild("oxygen_tank2", CubeListBuilder.create().texOffs(-23, -12).addBox(-7.0F, 24.38F, -7.0F, 14.0F, 7.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(-21, -10).addBox(-8.0F, 24.38F, -6.0F, 1.0F, 7.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(-21, -10).addBox(7.0F, 24.38F, -6.0F, 1.0F, 7.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(-9, 1).addBox(-6.0F, 24.38F, -8.0F, 12.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-9, 1).addBox(-6.0F, 24.38F, 7.0F, 12.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-20, -10).addBox(-6.0F, -18.62F, -6.0F, 12.0F, 43.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(-17, -8).addBox(-5.0F, -19.62F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(-13, -6).addBox(-4.0F, -20.62F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(-10, -4).addBox(-3.0F, -21.62F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-7, 1).addBox(-4.0F, -19.62F, -6.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-7, 1).addBox(-4.0F, -19.62F, 5.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-14, -6).addBox(-6.0F, -19.62F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(-14, -6).addBox(5.0F, -19.62F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(-12, -4).addBox(-5.0F, -20.62F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-6, 1).addBox(-3.0F, -20.62F, -5.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-13, -4).addBox(4.0F, -20.62F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-6, 1).addBox(-3.0F, -20.62F, 4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-19, -8).addBox(-7.0F, -18.62F, -5.0F, 1.0F, 43.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(-19, -8).addBox(6.0F, -18.62F, -5.0F, 1.0F, 43.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(-7, 1).addBox(-5.0F, -18.62F, -7.0F, 10.0F, 43.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-7, 1).addBox(-5.0F, -18.62F, 6.0F, 10.0F, 43.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-43.0667F, -14.2577F, 35.4849F));

        PartDefinition pistons = main.addOrReplaceChild("pistons", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition east_piston = pistons.addOrReplaceChild("east_piston", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        east_piston.addOrReplaceChild("0_east_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-1.2426F, -1.0F, -3.0F, 2.4853F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.2426F, -1.0F, 1.0F, 2.4853F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -1.2426F, 2.0F, 2.0F, 2.4853F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.0F, -1.0F, -1.2426F, 2.0F, 2.0F, 2.4853F, new CubeDeformation(0.0F)), PartPose.offset(-22.0667F, -28.8777F, 4.4849F));

        east_piston.addOrReplaceChild("octagon_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -1.0F, -1.2426F, 2.0F, 2.0F, 2.4853F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -1.2426F, 2.0F, 2.0F, 2.4853F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.2426F, -1.0F, 1.0F, 2.4853F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.2426F, -1.0F, -3.0F, 2.4853F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        east_piston.addOrReplaceChild("1_east_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0355F, -1.0F, -2.5F, 2.0711F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0355F, -1.0F, 0.5F, 2.0711F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.5F, -1.0F, -1.0355F, 2.0F, 4.0F, 2.0711F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.5F, -1.0F, -1.0355F, 2.0F, 4.0F, 2.0711F, new CubeDeformation(0.0F)), PartPose.offset(-22.0667F, -32.8777F, 4.4849F));

        east_piston.addOrReplaceChild("octagon_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -1.0F, -1.0355F, 2.0F, 4.0F, 2.0711F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.5F, -1.0F, -1.0355F, 2.0F, 4.0F, 2.0711F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0355F, -1.0F, 0.5F, 2.0711F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0355F, -1.0F, -2.5F, 2.0711F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        east_piston.addOrReplaceChild("2_east_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8284F, -1.0F, -2.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, 1.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F)), PartPose.offset(-22.0667F, -34.8777F, 4.4849F));

        east_piston.addOrReplaceChild("octagon_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, 1.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, -2.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        east_piston.addOrReplaceChild("3_east_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-1.4497F, -17.0F, -3.5F, 2.8995F, 18.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4497F, -17.0F, 1.5F, 2.8995F, 18.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.5F, -17.0F, -1.4497F, 2.0F, 18.0F, 2.8995F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.5F, -17.0F, -1.4497F, 2.0F, 18.0F, 2.8995F, new CubeDeformation(0.0F)), PartPose.offset(-22.0667F, -36.8777F, 4.4849F));

        east_piston.addOrReplaceChild("octagon_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -17.0F, -1.4497F, 2.0F, 18.0F, 2.8995F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.5F, -17.0F, -1.4497F, 2.0F, 18.0F, 2.8995F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4497F, -17.0F, 1.5F, 2.8995F, 18.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4497F, -17.0F, -3.5F, 2.8995F, 18.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        east_piston.addOrReplaceChild("4_east_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8284F, -1.0F, -2.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, 1.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F)), PartPose.offset(-22.0667F, -54.8777F, 4.4849F));

        east_piston.addOrReplaceChild("octagon_r5", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, 1.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, -2.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition east_transformer = east_piston.addOrReplaceChild("east_transformer", CubeListBuilder.create().texOffs(-6, -4).addBox(-3.0F, -6.5F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(-22.0667F, -55.8777F, 4.4849F));

        east_transformer.addOrReplaceChild("east_transformer_point_0", CubeListBuilder.create().texOffs(2, 0).addBox(-2.0F, -3.5F, -1.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -2.0F, 5.0F));

        PartDefinition middle_piston = pistons.addOrReplaceChild("middle_piston", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        middle_piston.addOrReplaceChild("0_middle_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-1.2426F, -1.0F, -3.0F, 2.4853F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.2426F, -1.0F, 1.0F, 2.4853F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -1.2426F, 2.0F, 2.0F, 2.4853F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.0F, -1.0F, -1.2426F, 2.0F, 2.0F, 2.4853F, new CubeDeformation(0.0F)), PartPose.offset(-12.0667F, -28.8777F, 4.4849F));

        middle_piston.addOrReplaceChild("octagon_r6", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -1.0F, -1.2426F, 2.0F, 2.0F, 2.4853F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -1.2426F, 2.0F, 2.0F, 2.4853F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.2426F, -1.0F, 1.0F, 2.4853F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.2426F, -1.0F, -3.0F, 2.4853F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        middle_piston.addOrReplaceChild("1_middle_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0355F, -1.0F, -2.5F, 2.0711F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0355F, -1.0F, 0.5F, 2.0711F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.5F, -1.0F, -1.0355F, 2.0F, 4.0F, 2.0711F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.5F, -1.0F, -1.0355F, 2.0F, 4.0F, 2.0711F, new CubeDeformation(0.0F)), PartPose.offset(-12.0667F, -32.8777F, 4.4849F));

        middle_piston.addOrReplaceChild("octagon_r7", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -1.0F, -1.0355F, 2.0F, 4.0F, 2.0711F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.5F, -1.0F, -1.0355F, 2.0F, 4.0F, 2.0711F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0355F, -1.0F, 0.5F, 2.0711F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0355F, -1.0F, -2.5F, 2.0711F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        middle_piston.addOrReplaceChild("2_middle_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8284F, -1.0F, -2.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, 1.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F)), PartPose.offset(-12.0667F, -34.8777F, 4.4849F));

        middle_piston.addOrReplaceChild("octagon_r8", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, 1.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, -2.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        middle_piston.addOrReplaceChild("3_middle_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-1.4498F, -17.0F, -3.5F, 2.8995F, 18.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4498F, -17.0F, 1.5F, 2.8995F, 18.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.5F, -17.0F, -1.4497F, 2.0F, 18.0F, 2.8995F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.5F, -17.0F, -1.4497F, 2.0F, 18.0F, 2.8995F, new CubeDeformation(0.0F)), PartPose.offset(-12.0667F, -36.8777F, 4.4849F));

        middle_piston.addOrReplaceChild("octagon_r9", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -17.0F, -1.4497F, 2.0F, 18.0F, 2.8995F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.5F, -17.0F, -1.4497F, 2.0F, 18.0F, 2.8995F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4498F, -17.0F, 1.5F, 2.8995F, 18.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4498F, -17.0F, -3.5F, 2.8995F, 18.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        middle_piston.addOrReplaceChild("4_middle_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8284F, -1.0F, -2.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, 1.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F)), PartPose.offset(-12.0667F, -54.8777F, 4.4849F));

        middle_piston.addOrReplaceChild("octagon_r10", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, 1.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, -2.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition middle_transformer = middle_piston.addOrReplaceChild("middle_transformer", CubeListBuilder.create().texOffs(-6, -4).addBox(-3.0F, -6.5F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(-12.0667F, -55.8777F, 4.4849F));

        middle_transformer.addOrReplaceChild("middle_transformer_pont_0", CubeListBuilder.create().texOffs(2, 0).addBox(-1.0F, -3.5F, -1.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 5.0F));

        PartDefinition west_piston = pistons.addOrReplaceChild("west_piston", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        west_piston.addOrReplaceChild("0_west_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-1.2426F, -1.0F, -3.0F, 2.4853F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.2426F, -1.0F, 1.0F, 2.4853F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -1.2426F, 2.0F, 2.0F, 2.4853F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.0F, -1.0F, -1.2426F, 2.0F, 2.0F, 2.4853F, new CubeDeformation(0.0F)), PartPose.offset(-2.0667F, -28.8777F, 4.4849F));

        west_piston.addOrReplaceChild("octagon_r11", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -1.0F, -1.2426F, 2.0F, 2.0F, 2.4853F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -1.2426F, 2.0F, 2.0F, 2.4853F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.2426F, -1.0F, 1.0F, 2.4853F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.2426F, -1.0F, -3.0F, 2.4853F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        west_piston.addOrReplaceChild("1_west_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0355F, -1.0F, -2.5F, 2.0711F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0355F, -1.0F, 0.5F, 2.0711F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.5F, -1.0F, -1.0355F, 2.0F, 4.0F, 2.0711F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.5F, -1.0F, -1.0355F, 2.0F, 4.0F, 2.0711F, new CubeDeformation(0.0F)), PartPose.offset(-2.0667F, -32.8777F, 4.4849F));

        west_piston.addOrReplaceChild("octagon_r12", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -1.0F, -1.0355F, 2.0F, 4.0F, 2.0711F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.5F, -1.0F, -1.0355F, 2.0F, 4.0F, 2.0711F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0355F, -1.0F, 0.5F, 2.0711F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0355F, -1.0F, -2.5F, 2.0711F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        west_piston.addOrReplaceChild("2_west_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8284F, -1.0F, -2.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, 1.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F)), PartPose.offset(-2.0667F, -34.8777F, 4.4849F));

        west_piston.addOrReplaceChild("octagon_r13", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, 1.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, -2.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        west_piston.addOrReplaceChild("3_west_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-1.4497F, -17.0F, -3.5F, 2.8995F, 18.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4497F, -17.0F, 1.5F, 2.8995F, 18.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.5F, -17.0F, -1.4497F, 2.0F, 18.0F, 2.8995F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.5F, -17.0F, -1.4497F, 2.0F, 18.0F, 2.8995F, new CubeDeformation(0.0F)), PartPose.offset(-2.0667F, -36.8777F, 4.4849F));

        west_piston.addOrReplaceChild("octagon_r14", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -17.0F, -1.4497F, 2.0F, 18.0F, 2.8995F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.5F, -17.0F, -1.4497F, 2.0F, 18.0F, 2.8995F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4497F, -17.0F, 1.5F, 2.8995F, 18.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.4497F, -17.0F, -3.5F, 2.8995F, 18.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        west_piston.addOrReplaceChild("4_west_piston", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8284F, -1.0F, -2.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, 1.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F)), PartPose.offset(-2.0667F, -54.8777F, 4.4849F));

        west_piston.addOrReplaceChild("octagon_r15", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -1.0F, -0.8284F, 1.0F, 2.0F, 1.6569F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, 1.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.8284F, -1.0F, -2.0F, 1.6569F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition west_transformer = west_piston.addOrReplaceChild("west_transformer", CubeListBuilder.create().texOffs(-6, -4).addBox(-3.0F, -6.5F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(-2.0667F, -55.8777F, 4.4849F));

        west_transformer.addOrReplaceChild("west_transformer_point_0", CubeListBuilder.create().texOffs(2, 0).addBox(-1.0F, -3.5F, -1.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 5.0F));

        PartDefinition transformer_cabinet = main.addOrReplaceChild("transformer_cabinet", CubeListBuilder.create().texOffs(23, -5).addBox(-23.0F, 40.0F, 34.0F, 26.0F, 26.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0667F, -57.8777F, 9.4849F));

        transformer_cabinet.addOrReplaceChild("west_transformer_top", CubeListBuilder.create().texOffs(2, 0).addBox(-2.0F, 0.5F, -0.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 22.0F, 37.0F));

        transformer_cabinet.addOrReplaceChild("west_transformer_middle", CubeListBuilder.create().texOffs(-2, -2).addBox(-3.0F, -2.5F, -1.5F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 28.0F, 37.0F));

        transformer_cabinet.addOrReplaceChild("west_transformer_bottom", CubeListBuilder.create().texOffs(-6, -4).addBox(-4.0F, -8.5F, -2.5F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 40.0F, 37.0F));

        transformer_cabinet.addOrReplaceChild("middle_transformer_top", CubeListBuilder.create().texOffs(2, 0).addBox(-1.0F, 0.5F, -0.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.5F)), PartPose.offset(-10.0F, 22.0F, 37.0F));

        transformer_cabinet.addOrReplaceChild("middle_transformer_middle", CubeListBuilder.create().texOffs(-2, -2).addBox(-2.0F, -2.5F, -1.5F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(-10.0F, 28.0F, 37.0F));

        transformer_cabinet.addOrReplaceChild("middle_transformer_bottom", CubeListBuilder.create().texOffs(-6, -4).addBox(-3.0F, -8.5F, -2.5F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(-10.0F, 40.0F, 37.0F));

        transformer_cabinet.addOrReplaceChild("east_transformer_top", CubeListBuilder.create().texOffs(2, 0).addBox(-1.0F, 0.5F, -0.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.5F)), PartPose.offset(-19.0F, 22.0F, 37.0F));

        transformer_cabinet.addOrReplaceChild("east_transformer_middle", CubeListBuilder.create().texOffs(-2, -2).addBox(-2.0F, -2.5F, -1.5F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(-19.0F, 28.0F, 37.0F));

        transformer_cabinet.addOrReplaceChild("east_transformer_bottom", CubeListBuilder.create().texOffs(-6, -4).addBox(-3.0F, -8.5F, -2.5F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(-19.0F, 40.0F, 37.0F));

        PartDefinition pipes = main.addOrReplaceChild("pipes", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition fluid = pipes.addOrReplaceChild("fluid", CubeListBuilder.create().texOffs(-11, -6).addBox(-2.0667F, -8.8777F, 16.4849F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(-7, -4).addBox(-1.0667F, -16.8777F, 17.4849F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-11, -6).addBox(-65.0667F, -8.8777F, -21.5151F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(-7, -4).addBox(-64.0667F, -16.8777F, -20.5151F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-4, -2).addBox(-63.0667F, -17.8777F, -19.5151F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-62.0667F, -18.8777F, -18.5151F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-7, 0).addBox(-64.0667F, -16.8777F, -14.5151F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-5, -4).addBox(-64.0667F, -15.8777F, -12.5151F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-3, -2).addBox(-64.0667F, -10.8777F, -5.5151F, 4.0F, 35.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(-7, -4).addBox(-65.0667F, -16.8777F, -6.5151F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-8, -4).addBox(-3.0667F, -16.8777F, 17.4849F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-4, 0).addBox(-1.0667F, -16.8777F, 15.4849F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-8, -4).addBox(-1.0667F, 18.1223F, 6.4849F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-8, -4).addBox(-7.0667F, 18.1223F, 6.4849F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-8, -4).addBox(-1.0667F, -16.8777F, 6.4849F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-8, -4).addBox(3.9333F, 3.1223F, -8.5151F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-8, -4).addBox(-7.0667F, 3.1223F, -8.5151F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-8, -2).addBox(-7.0667F, -15.8777F, 18.4849F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(-7, -1).addBox(-0.0667F, -15.8777F, 12.4849F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(-8, -2).addBox(-0.0667F, -10.8777F, 7.4849F, 4.0F, 29.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(-8, -2).addBox(4.9333F, 9.1223F, -7.5151F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(-9, -2).addBox(-1.0667F, 4.1223F, -7.5151F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(-9, -3).addBox(-6.0667F, 19.1223F, 1.4849F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(-8, -2).addBox(-6.0667F, 9.1223F, -7.5151F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(18.0F, -7.0F, 2.0F));

        fluid.addOrReplaceChild("gauge", CubeListBuilder.create().texOffs(-2, 0).addBox(-2.0667F, -13.8777F, 2.4849F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-61.0F, -9.0F, -21.0F));

        PartDefinition gas = pipes.addOrReplaceChild("gas", CubeListBuilder.create().texOffs(-2, -2).addBox(-1.0F, 4.8333F, -12.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, -3.1667F, 9.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.0F, -3.1667F, 9.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-2, -2).addBox(-1.0F, -3.1667F, 8.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, 4.8333F, -11.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.0F, 4.8333F, -11.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-16, -16).addBox(-1.0F, -6.1667F, -9.0F, 2.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -4.1667F, -1.0F, 2.0F, 56.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-10, -10).addBox(-1.0F, -6.1667F, 11.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -4.1667F, 21.0F, 2.0F, 56.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, 46.8333F, 18.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.0F, 47.8333F, 18.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(11.0F, 46.8333F, 18.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(10.0F, 47.8333F, 18.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, 47.8333F, 18.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(13.0F, 47.8333F, 18.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(13.0F, 21.8333F, 18.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(11.0F, 20.8333F, 18.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(10.0F, 21.8333F, 18.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(1.0F, 21.8333F, 21.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(10, 0).addBox(11.0F, 23.8333F, 21.0F, 2.0F, 28.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(11, 1).addBox(11.0F, 21.8333F, 20.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 1).addBox(11.0F, 47.8333F, 20.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 1).addBox(-1.0F, 47.8333F, 20.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, -6.1667F, -11.0F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-43.0667F, -34.711F, 25.4849F));

        PartDefinition tank_connector = gas.addOrReplaceChild("tank_connector", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6284F, -1.0002F, -1.5F, 1.2426F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.4858F, -3.0002F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(2, 2).addBox(-0.4858F, -2.0002F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.5071F, -1.0002F, -0.6213F, 3.0F, 4.0F, 1.2426F, new CubeDeformation(0.0F)), PartPose.offset(0.0071F, -6.1665F, 10.0F));

        tank_connector.addOrReplaceChild("octagon_r16", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -2.0F, -0.6213F, 3.0F, 4.0F, 1.2426F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-0.6213F, -1.999F, -1.5F, 1.2426F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0071F, 0.9998F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition valve = gas.addOrReplaceChild("valve", CubeListBuilder.create().texOffs(-1, -1).addBox(-1.0355F, 1.5F, -0.9997F, 2.0711F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-1, -1).addBox(-1.0355F, -2.5F, -0.9997F, 2.0711F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-1, -1).addBox(1.5F, -1.0355F, -0.9997F, 1.0F, 2.0711F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-1, -1).addBox(-2.5F, -1.0355F, -0.9997F, 1.0F, 2.0711F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(1, 0).addBox(-2.0F, -0.5F, -1.0007F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(1, 0).addBox(-0.5F, -2.0F, -1.0017F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.8333F, 23.9997F));

        valve.addOrReplaceChild("octagon_r17", CubeListBuilder.create().texOffs(-1, -1).addBox(-2.5F, -1.0355F, -1.5F, 1.0F, 2.0711F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-1, -1).addBox(1.5F, -1.0355F, -1.5F, 1.0F, 2.0711F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-1, -1).addBox(-1.0355F, -2.5F, -1.5F, 2.0711F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-1, -1).addBox(-1.0355F, 1.5F, -1.5F, 2.0711F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.5003F, 0.0F, 0.0F, 0.7854F));

        return LayerDefinition.create(meshDefinition, 16, 16);
    }
}
