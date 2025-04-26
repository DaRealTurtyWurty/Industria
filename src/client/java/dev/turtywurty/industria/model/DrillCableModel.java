package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class DrillCableModel extends Model {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("drill_cable"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/drill_cable.png");

    private final ModelPart main;

    public DrillCableModel(ModelPart root) {
        super(root, RenderLayer::getEntitySolid);

        this.main = root.getChild("main");
    }

    public static TexturedModelData getTexturedModelData() {
        var modelData = new ModelData();

        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("main", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-1.5F, -3.5F, -3.5F, 3.0F, 7.0F, 7.0F, new Dilation(-0.05F)),
                ModelTransform.origin(0.1223F, -22.642F, 7.8787F));

        return TexturedModelData.of(modelData, 32, 32);
    }

    public ModelPart getMain() {
        return this.main;
    }
}