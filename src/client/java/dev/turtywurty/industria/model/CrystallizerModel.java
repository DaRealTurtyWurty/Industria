package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class CrystallizerModel extends Model<Void> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("crystallizer"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/crystallizer.png");

    private final ModelPart main;

    public CrystallizerModel(ModelPart root) {
        super(root, RenderLayers::entityCutout);

        this.main = root.getChild("main");
    }

    public static TexturedModelData getTexturedModelData() {
        var modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(-28.0F, -64.0F, -12.0F, 40.0F, 2.0F, 40.0F, new Dilation(0.0F))
                .uv(0, 43).cuboid(-28.0F, -2.0F, -12.0F, 40.0F, 2.0F, 40.0F, new Dilation(0.0F))
                .uv(161, 0).cuboid(-26.0F, -16.0F, -10.0F, 36.0F, 2.0F, 36.0F, new Dilation(0.0F))
                .uv(170, 151).cuboid(-26.0F, -16.0F, -12.0F, 36.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(170, 102).cuboid(-26.0F, -62.0F, -12.0F, 36.0F, 46.0F, 2.0F, new Dilation(0.0F))
                .uv(170, 172).cuboid(0.0F, -14.0F, -12.0F, 10.0F, 12.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 187).cuboid(-26.0F, -14.0F, -12.0F, 10.0F, 12.0F, 2.0F, new Dilation(0.0F))
                .uv(170, 39).cuboid(-26.0F, -62.0F, 26.0F, 36.0F, 60.0F, 2.0F, new Dilation(0.0F))
                .uv(85, 86).cuboid(-28.0F, -62.0F, -12.0F, 2.0F, 60.0F, 40.0F, new Dilation(0.0F))
                .uv(0, 86).cuboid(10.0F, -62.0F, -12.0F, 2.0F, 60.0F, 40.0F, new Dilation(0.0F))
                .uv(170, 156).cuboid(-16.0F, -14.0F, -13.0F, 16.0F, 12.0F, 3.0F, new Dilation(0.0F)), ModelTransform.origin(8.0F, 24.0F, -8.0F));

        return TexturedModelData.of(modelData, 512, 512);
    }
}