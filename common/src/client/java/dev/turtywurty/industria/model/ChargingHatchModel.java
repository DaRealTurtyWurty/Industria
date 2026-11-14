package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.state.ContainerDoorRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class ChargingHatchModel extends Model<ContainerDoorRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("charging_hatch"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/charging_hatch.png");

    private final ModelPart door;

    public ChargingHatchModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        this.door = root.getChild("main").getChild("door");
    }

    public ModelPart getDoor() {
        return this.door;
    }

    @Override
    public void setupAnim(ContainerDoorRenderState state) {
        super.setupAnim(state);
        float progress = 1.0F - state.doorOpenness;
        this.door.yRot = (1.0F - progress * progress * progress) * Mth.HALF_PI;
    }

    public static LayerDefinition getTexturedModelData() {
        var meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition main = root.addOrReplaceChild("main",
            CubeListBuilder.create().texOffs(2, 1).addBox(-15.0F, -16.0F, 0.0F, 16.0F, 16.0F, 15.0F),
            PartPose.offset(7.0F, 24.0F, -7.0F));

        main.addOrReplaceChild("frame",
            CubeListBuilder.create().texOffs(3, 55).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 1.0F, 1.0F)
                .texOffs(3, 57).addBox(-13.0F, -16.0F, -1.0F, 12.0F, 1.0F, 1.0F)
                .texOffs(3, 39).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 15.0F, 1.0F)
                .texOffs(9, 39).addBox(-15.0F, -16.0F, -1.0F, 2.0F, 15.0F, 1.0F),
            PartPose.ZERO);

        main.addOrReplaceChild("door",
            CubeListBuilder.create().texOffs(15, 32).addBox(0.0F, -6.5F, -0.5F, 12.0F, 14.0F, 1.0F)
                .texOffs(15, 50).addBox(10.0F, -1.5F, -1.5F, 1.0F, 4.0F, 1.0F),
            PartPose.offset(-13.0F, -8.5F, -1.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}
