// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class ElectrolyzerModel extends Model {
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/electrolyzer.png");
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("electrolyzer"), "main");

    public ElectrolyzerModel(ModelPart root) {
        super(root, RenderLayer::getEntityCutout);
    }

    public static TexturedModelData getTexturedModelData() {
        var modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("main", ModelPartBuilder.create()
                .uv(0, 0)
                .cuboid(-23.0F, -32.0F, -1.0F, 48.0F, 32.0F, 32.0F,
                        new Dilation(0.0F)),
                ModelTransform.pivot(-1.0F, 24.0F, -7.0F));

        return TexturedModelData.of(modelData, 256, 256);
    }
}