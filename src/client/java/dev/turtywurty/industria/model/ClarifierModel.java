package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class ClarifierModel extends Model {
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/clarifier.png");
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("clarifier"), "main");

    private final ModelPart main;
    private final ModelPart fluid_ramp;
    private final ModelPart bone;
    private final ModelPart bone2;
    private final ModelPart bone3;
    private final ModelPart bone4;
    private final ModelPart bone5;
    private final ModelPart bone6;

    public ClarifierModel(ModelPart root) {
        super(root, RenderLayer::getEntityCutout);
        this.main = root.getChild("main");
        this.fluid_ramp = main.getChild("fluid_ramp");
        this.bone = main.getChild("bone");
        this.bone2 = main.getChild("bone2");
        this.bone3 = main.getChild("bone3");
        this.bone4 = main.getChild("bone4");
        this.bone5 = main.getChild("bone5");
        this.bone6 = main.getChild("bone6");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create().uv(0, 135).cuboid(-8.0F, -2.0F, -23.0F, 16.0F, 1.0F, 31.0F, new Dilation(0.0F))
                .uv(67, 224).cuboid(-8.0F, -12.0F, -23.0F, 16.0F, 2.0F, 15.0F, new Dilation(0.0F))
                .uv(220, 0).cuboid(-8.0F, -16.0F, -23.0F, 16.0F, 4.0F, 15.0F, new Dilation(0.0F))
                .uv(220, 20).cuboid(-8.0F, -16.0F, 8.0F, 16.0F, 1.0F, 16.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-10.0F, -1.0F, -23.0F, 20.0F, 1.0F, 47.0F, new Dilation(0.0F))
                .uv(142, 208).cuboid(-8.0F, -10.0F, -23.0F, 2.0F, 8.0F, 32.0F, new Dilation(0.0F))
                .uv(211, 208).cuboid(6.0F, -10.0F, -23.0F, 2.0F, 8.0F, 32.0F, new Dilation(0.0F))
                .uv(218, 105).cuboid(-8.0F, -12.0F, -8.0F, 16.0F, 2.0F, 16.0F, new Dilation(0.0F))
                .uv(0, 168).cuboid(8.0F, -16.0F, -23.0F, 2.0F, 15.0F, 33.0F, new Dilation(0.0F))
                .uv(71, 175).cuboid(-10.0F, -16.0F, -23.0F, 2.0F, 15.0F, 33.0F, new Dilation(0.0F))
                .uv(71, 168).cuboid(-8.0F, -12.0F, 8.0F, 16.0F, 2.0F, 3.0F, new Dilation(0.0F))
                .uv(247, 185).cuboid(-8.0F, -3.0F, 19.0F, 16.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(157, 249).cuboid(-8.0F, -15.0F, 8.0F, 2.0F, 12.0F, 16.0F, new Dilation(0.0F))
                .uv(120, 249).cuboid(6.0F, -15.0F, 8.0F, 2.0F, 12.0F, 16.0F, new Dilation(0.0F))
                .uv(182, 43).cuboid(-6.0F, -15.0F, 22.0F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(194, 249).cuboid(8.0F, -16.0F, 10.0F, 2.0F, 15.0F, 14.0F, new Dilation(0.0F))
                .uv(227, 249).cuboid(-10.0F, -16.0F, 10.0F, 2.0F, 15.0F, 14.0F, new Dilation(0.0F))
                .uv(74, 259).cuboid(-10.0F, -24.0F, -10.0F, 20.0F, 8.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 251).cuboid(-10.0F, -24.0F, -8.0F, 2.0F, 8.0F, 16.0F, new Dilation(0.0F))
                .uv(37, 259).cuboid(8.0F, -24.0F, -8.0F, 2.0F, 8.0F, 16.0F, new Dilation(0.0F))
                .uv(247, 174).cuboid(-10.0F, -24.0F, 8.0F, 20.0F, 8.0F, 2.0F, new Dilation(0.0F))
                .uv(259, 146).cuboid(-6.0F, -10.0F, 8.0F, 12.0F, 9.0F, 2.0F, new Dilation(0.0F))
                .uv(142, 193).cuboid(-21.0F, -28.0F, 13.0F, 42.0F, 7.0F, 7.0F, new Dilation(0.0F))
                .uv(218, 43).cuboid(14.0F, -28.0F, -13.0F, 7.0F, 7.0F, 26.0F, new Dilation(0.0F))
                .uv(198, 131).cuboid(-21.0F, -28.0F, -20.0F, 42.0F, 7.0F, 7.0F, new Dilation(0.0F))
                .uv(0, 217).cuboid(-21.0F, -28.0F, -13.0F, 7.0F, 7.0F, 26.0F, new Dilation(0.0F))
                .uv(198, 146).cuboid(-14.0F, -22.0F, -13.0F, 4.0F, 1.0F, 26.0F, new Dilation(0.0F))
                .uv(135, 43).cuboid(-10.0F, -22.0F, 10.0F, 20.0F, 1.0F, 3.0F, new Dilation(0.0F))
                .uv(218, 124).cuboid(-10.0F, -22.0F, -13.0F, 20.0F, 1.0F, 3.0F, new Dilation(0.0F))
                .uv(218, 77).cuboid(10.0F, -22.0F, -13.0F, 4.0F, 1.0F, 26.0F, new Dilation(0.0F)), ModelTransform.rotation(0.0F, 24.0F, 0.0F));

        main.addChild("fluid_ramp", ModelPartBuilder.create().uv(241, 193).cuboid(-8.0F, -0.5F, -6.0F, 16.0F, 1.0F, 12.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -2.9455F, 2.3968F, 0.2618F, 0.0F, 0.0F));

        ModelPartData bone6 = main.addChild("bone6", ModelPartBuilder.create(), ModelTransform.rotation(1.0F, -12.0F, 11.0F));

        bone6.addChild("cube_r1", ModelPartBuilder.create().uv(67, 242).cuboid(-7.0F, 0.0F, 0.0F, 12.0F, 2.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.7418F, 0.0F, 0.0F));

        ModelPartData bone = main.addChild("bone", ModelPartBuilder.create().uv(142, 175).cuboid(-24.0F, -32.0F, -23.0F, 48.0F, 4.0F, 4.0F, new Dilation(0.0F))
                .uv(135, 0).cuboid(-24.0F, -32.0F, -19.0F, 4.0F, 4.0F, 38.0F, new Dilation(0.0F))
                .uv(111, 131).cuboid(20.0F, -32.0F, -20.0F, 4.0F, 4.0F, 39.0F, new Dilation(0.0F))
                .uv(142, 184).cuboid(-24.0F, -32.0F, 19.0F, 48.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.rotation(0.0F, 0.0F, 0.0F));

        ModelPartData bone2 = main.addChild("bone2", ModelPartBuilder.create(), ModelTransform.rotation(20.0553F, -31.9453F, -0.5F));

        bone2.addChild("cube_r2", ModelPartBuilder.create().uv(111, 90).cuboid(-15.0106F, -0.077F, -18.5F, 15.0F, 2.0F, 38.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5847F));

        ModelPartData bone3 = main.addChild("bone3", ModelPartBuilder.create(), ModelTransform.rotation(-20.0513F, -31.9416F, -0.5F));

        bone3.addChild("cube_r3", ModelPartBuilder.create().uv(111, 49).cuboid(-15.0106F, -0.077F, -19.5F, 15.0F, 2.0F, 38.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 3.1416F, 0.0F, -2.5569F));

        ModelPartData bone4 = main.addChild("bone4", ModelPartBuilder.create(), ModelTransform.rotation(0.5F, -27.0247F, -1.002F));

        bone4.addChild("cube_r4", ModelPartBuilder.create().uv(0, 49).cuboid(4.4254F, 6.891F, -19.5F, 15.0F, 2.0F, 40.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, -0.9861F, -1.5708F));

        ModelPartData bone5 = main.addChild("bone5", ModelPartBuilder.create(), ModelTransform.rotation(0.5F, -27.0284F, 1.002F));

        bone5.addChild("cube_r5", ModelPartBuilder.create().uv(0, 92).cuboid(4.4234F, 6.8941F, -20.5F, 15.0F, 2.0F, 40.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -1.5708F, 0.9861F, -1.5708F));
        return TexturedModelData.of(modelData, 512, 512);
    }
}