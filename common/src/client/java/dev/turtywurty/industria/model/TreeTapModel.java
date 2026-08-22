package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.renderer.block.TreeTapBlockEntityRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class TreeTapModel extends Model<TreeTapBlockEntityRenderer.TreeTapRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("tree_tap"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/tree_tap.png");

    private final ModelPart harness;
    private final ModelPart bowl;
    private final ModelPart spout;

    public TreeTapModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        this.harness = root.getChild("harness");
        this.bowl = root.getChild("bowl");
        this.spout = root.getChild("spout");
    }

    public static LayerDefinition createMainLayer() {
        var meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        partDefinition.addOrReplaceChild("harness", CubeListBuilder.create().texOffs(36, 9).addBox(12.0F, -2.0F, -16.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(12, 38).addBox(12.0F, -4.0F, -16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(12, 38).addBox(6.0F, -4.0F, -16.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(36, 9).addBox(6.0F, 0.0F, -16.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(36, 9).addBox(0.0F, -2.0F, -16.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(0.0F, -2.0F, 2.0F, 20.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(18.0F, -2.0F, -14.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, -2.0F, -14.0F, 2.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 12.0F, 22.0F));

        partDefinition.addOrReplaceChild("bowl", CubeListBuilder.create().texOffs(0, 31).addBox(-5.0F, -1.0F, 1.999F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 33).addBox(-4.0F, 0.0F, 1.999F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 31).addBox(-4.0F, 2.0F, -6.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 22).addBox(-3.0F, 4.0F, -6.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(36, 13).addBox(-3.0F, 2.0F, -7.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 13).addBox(-3.0F, 0.0F, -8.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 16).addBox(-3.0F, -1.0F, -9.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 31).addBox(5.0F, 2.0F, -6.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(22, 33).addBox(5.0F, 0.0F, -7.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 31).addBox(6.0F, 0.0F, -6.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(36, 0).addBox(7.0F, -1.0F, -6.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(22, 36).addBox(6.0F, -1.0F, -7.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 36).addBox(5.0F, -1.0F, -8.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 33).addBox(-4.0F, 0.0F, -7.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 36).addBox(-4.0F, -1.0F, -8.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(26, 31).addBox(-5.0F, 0.0F, -6.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(22, 36).addBox(-5.0F, -1.0F, -7.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 0).addBox(-6.0F, -1.0F, -6.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 19.0F, 6.0F));

        PartDefinition spout = partDefinition.addOrReplaceChild("spout", CubeListBuilder.create(), PartPose.offset(1.0F, 12.0F, 6.0F));

        spout.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 38).addBox(1.999F, -2.0F, -6.0F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-1.999F, -2.0F, -6.0F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(32, 22).addBox(-2.0F, 0.0F, -6.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -2.0F, 2.0F, 0.7854F, 0.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}