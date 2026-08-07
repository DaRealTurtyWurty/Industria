package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class AdvancedSolarPanelModel extends Model<AdvancedSolarPanelModel.RenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Industria.id("advanced_solar_panel"), "main");
    public static final ModelLayerLocation STAIR_LAYER_LOCATION =
            new ModelLayerLocation(Industria.id("advanced_solar_panel_stair"), "main");
    public static final Identifier TEXTURE_LOCATION =
            Industria.id("textures/block/advanced_solar_panel.png");
    public static final Identifier POWERLESS_TEXTURE_LOCATION =
            Industria.id("textures/block/advanced_solar_panel_powerless.png");

    private final ModelPart top;
    private final ModelPart panel;

    public AdvancedSolarPanelModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        ModelPart main = root.getChild("main");
        this.top = main.getChild("top");
        this.panel = this.top.getChild("panel");
    }

    public static LayerDefinition createMainLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition main = root.addOrReplaceChild("main", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-6.0F, 2.0F, -6.0F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(22, 36).addBox(-8.0F, 3.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36).addBox(-5.0F, 3.0F, -7.0F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-6.5F, 5.5F, 4.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.5F, 3.5F, 4.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        main.addOrReplaceChild("wire", CubeListBuilder.create()
                .texOffs(0, 9).addBox(-1.5F, -0.5F, -0.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 6).addBox(-1.5F, -2.5F, -0.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.0F, 6.0F, -5.0F, 0.0F, (float) Math.PI, 0.0F));
        main.addOrReplaceChild("plug", CubeListBuilder.create().texOffs(22, 36)
                        .addBox(-1.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.0F, 5.0F, 0.0F, 0.0F, (float) Math.PI, 0.0F));

        addTrackingAssembly(main, PartPose.offset(0.0F, 2.0F, 0.0F), PartPose.offset(0.0F, -7.0F, 0.0F), -2.0F);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public static LayerDefinition createStairLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition main = root.addOrReplaceChild("main", CubeListBuilder.create()
                .texOffs(22, 36).addBox(-8.0F, 3.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36).addBox(-5.0F, 10.0F, -1.0F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(18, 44).addBox(-6.0F, 3.0F, -2.0F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 41).addBox(5.0F, 7.0F, 0.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 41).addBox(-7.0F, 7.0F, 0.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        main.addOrReplaceChild("wire", CubeListBuilder.create()
                .texOffs(0, 9).addBox(-1.5F, -0.5F, -0.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 6).addBox(-1.5F, -2.5F, -0.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.0F, 13.0F, 1.0F, 0.0F, (float) Math.PI, 0.0F));
        main.addOrReplaceChild("plug", CubeListBuilder.create().texOffs(22, 36)
                        .addBox(-1.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.0F, 5.0F, 0.0F, 0.0F, (float) Math.PI, 0.0F));

        addTrackingAssembly(main, PartPose.ZERO, PartPose.offset(0.0F, -5.0F, 0.0F), 0.0F);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    private static void addTrackingAssembly(PartDefinition main, PartPose topPose, PartPose panelPose, float shaftY) {
        PartDefinition top = main.addOrReplaceChild("top", CubeListBuilder.create()
                .texOffs(0, 41).addBox(-1.0F, shaftY, -1.0F, 2.0F, shaftY == 0.0F ? 3.0F : 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(22, 58).addBox(-8.0F, shaftY - 5.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(22, 58).addBox(7.0F, shaftY - 5.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(21, 52).addBox(-8.0F, shaftY - 2.0F, -1.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                topPose);

        top.addOrReplaceChild("panel", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(29, 57).addBox(6.5F, -2.5F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(39, 57).addBox(-8.5F, -2.5F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                panelPose);
    }

    @Override
    public void setupAnim(RenderState state) {
        super.setupAnim(state);
        this.top.yRot = state.yaw();
        this.panel.xRot = state.pitch();
    }

    public record RenderState(float yaw, float pitch) {
    }
}
