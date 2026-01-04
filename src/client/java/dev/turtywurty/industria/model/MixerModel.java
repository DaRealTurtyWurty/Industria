package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class MixerModel extends Model<MixerModel.MixerModelRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("mixer"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/mixer.png");

    private final MixerModelParts parts;

    public MixerModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);

        ModelPart main = root.getChild("main");
        ModelPart glass = main.getChild("glass");
        ModelPart supports = main.getChild("supports");
        ModelPart stirringRods = main.getChild("stirring_rods");

        this.parts = new MixerModelParts(main, glass, supports, stirringRods);
    }

    public static LayerDefinition getTexturedModelData() {
        var modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition main = modelPartData.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, 12.5F, -14.0F, 28.0F, 2.0F, 28.0F, new CubeDeformation(0.0F))
                .texOffs(0, 31).addBox(-14.0F, -33.5F, -14.0F, 28.0F, 2.0F, 28.0F, new CubeDeformation(0.0F))
                .texOffs(138, 48).addBox(-14.0F, 6.5F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(159, 64).addBox(12.0F, 6.5F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 9.5F, 0.0F));

        main.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(106, 62).addBox(-12.0F, -30.75F, -14.0F, 24.0F, 44.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 62).addBox(-14.0F, -30.75F, -12.0F, 2.0F, 38.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(138, 0).addBox(-14.0F, 7.25F, -12.0F, 2.0F, 6.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(138, 16).addBox(-14.0F, 7.25F, 3.0F, 2.0F, 6.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(159, 48).addBox(12.0F, 7.25F, -12.0F, 2.0F, 6.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(138, 32).addBox(12.0F, 7.25F, 3.0F, 2.0F, 6.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(53, 62).addBox(12.0F, -30.75F, -12.0F, 2.0F, 38.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(106, 111).addBox(-12.0F, -30.75F, 12.0F, 24.0F, 44.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.75F, 0.0F));

        main.addOrReplaceChild("supports", CubeListBuilder.create().texOffs(60, 127).addBox(12.0F, -22.0F, -14.0F, 2.0F, 44.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(69, 127).addBox(-14.0F, -22.0F, -14.0F, 2.0F, 44.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(87, 127).addBox(-14.0F, -22.0F, 12.0F, 2.0F, 44.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(78, 127).addBox(12.0F, -22.0F, 12.0F, 2.0F, 44.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(113, 0).addBox(-3.0F, -22.0F, -3.0F, 6.0F, 44.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.5F, 0.0F));

        PartDefinition stirring_rods = main.addOrReplaceChild("stirring_rods", CubeListBuilder.create().texOffs(45, 127).addBox(-1.0F, -21.5F, -10.25F, 2.0F, 43.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(30, 127).addBox(-1.0F, -21.5F, 4.75F, 2.0F, 43.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, 0.25F));

        stirring_rods.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 127).addBox(0.0F, -20.5F, 4.5F, 2.0F, 43.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(15, 127).addBox(0.0F, -20.5F, -11.5F, 2.0F, 43.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -1.0F, -0.75F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(modelData, 256, 256);
    }

    @Override
    public void setupAnim(MixerModelRenderState state) {
        super.setupAnim(state);
        this.parts.stirringRods().yRot = state.stirringRotation;
    }

    public record MixerModelRenderState(float stirringRotation) {
    }

    public record MixerModelParts(ModelPart main, ModelPart glass, ModelPart supports, ModelPart stirringRods) {
    }
}
