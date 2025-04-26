// Made with Blockbench 4.12.3
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class DigesterModel extends Model {
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/digester.png");
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("digester"), "main");

    private final ModelPart main;

    public DigesterModel(ModelPart root) {
        super(root, RenderLayer::getEntitySolid);
        this.main = root.getChild("main");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(-28.0F, -80.0F, -12.0F, 40.0F, 80.0F, 40.0F, new Dilation(0.0F)), ModelTransform.rotation(8.0F, 24.0F, -8.0F));

        return TexturedModelData.of(modelData, 256, 256);
    }
}