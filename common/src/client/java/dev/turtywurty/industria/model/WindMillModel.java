package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class WindMillModel extends Model<WindMillModel.RotorState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("wind_mill"), "main");
    public static final Identifier TEXTURE_LOCATION =
        Identifier.fromNamespaceAndPath("minecraft", "textures/block/white_wool.png");

    private final ModelPart rotor;
    private final ModelPart[] sails = new ModelPart[4];

    public WindMillModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.rotor = root.getChild("rotor");
        for (int slot = 0; slot < this.sails.length; slot++) {
            this.sails[slot] = this.rotor.getChild("sail_" + slot);
        }
    }

    public static LayerDefinition createMainLayer() {
        var mesh = new MeshDefinition();
        var rotor = mesh.getRoot().addOrReplaceChild("rotor",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4, -4, -8, 8, 8, 16), PartPose.ZERO);
        for (int slot = 0; slot < 4; slot++) {
            rotor.addOrReplaceChild("sail_" + slot,
                CubeListBuilder.create().texOffs(0, 0).addBox(-2, -47, -1, 4, 43, 2)
                    .texOffs(16, 0).addBox(2, -47, -0.5F, 7, 29, 1),
                PartPose.offsetAndRotation(0, 0, 0, 0, 0, slot * (float) (Math.PI / 2)));
        }

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(RotorState state) {
        super.setupAnim(state);
        this.rotor.zRot = state.rotation();
        for (int slot = 0; slot < this.sails.length; slot++) {
            this.sails[slot].visible = (state.installedSails() & (1 << slot)) != 0;
        }
    }

    public record RotorState(float rotation, int installedSails) {
    }
}
