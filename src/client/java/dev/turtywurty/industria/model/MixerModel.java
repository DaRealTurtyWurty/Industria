package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class MixerModel extends Model<MixerModel.MixerModelRenderState> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("mixer"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/mixer.png");

    private final MixerModelParts parts;

    public MixerModel(ModelPart root) {
        super(root, RenderLayer::getEntityCutout);

        ModelPart main = root.getChild("main");
        ModelPart glass = main.getChild("glass");
        ModelPart supports = main.getChild("supports");
        ModelPart stirringRods = main.getChild("stirring_rods");

        this.parts = new MixerModelParts(main, glass, supports, stirringRods);
    }

    public static TexturedModelData getTexturedModelData() {
        var modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(-14.0F, 12.5F, -14.0F, 28.0F, 2.0F, 28.0F, new Dilation(0.0F))
                .uv(0, 31).cuboid(-14.0F, -33.5F, -14.0F, 28.0F, 2.0F, 28.0F, new Dilation(0.0F))
                .uv(138, 48).cuboid(-14.0F, 6.5F, -3.0F, 2.0F, 6.0F, 6.0F, new Dilation(0.0F))
                .uv(159, 64).cuboid(12.0F, 6.5F, -3.0F, 2.0F, 6.0F, 6.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, 9.5F, 0.0F));

        main.addChild("glass", ModelPartBuilder.create().uv(106, 62).cuboid(-12.0F, -30.75F, -14.0F, 24.0F, 44.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 62).cuboid(-14.0F, -30.75F, -12.0F, 2.0F, 38.0F, 24.0F, new Dilation(0.0F))
                .uv(138, 0).cuboid(-14.0F, 7.25F, -12.0F, 2.0F, 6.0F, 9.0F, new Dilation(0.0F))
                .uv(138, 16).cuboid(-14.0F, 7.25F, 3.0F, 2.0F, 6.0F, 9.0F, new Dilation(0.0F))
                .uv(159, 48).cuboid(12.0F, 7.25F, -12.0F, 2.0F, 6.0F, 9.0F, new Dilation(0.0F))
                .uv(138, 32).cuboid(12.0F, 7.25F, 3.0F, 2.0F, 6.0F, 9.0F, new Dilation(0.0F))
                .uv(53, 62).cuboid(12.0F, -30.75F, -12.0F, 2.0F, 38.0F, 24.0F, new Dilation(0.0F))
                .uv(106, 111).cuboid(-12.0F, -30.75F, 12.0F, 24.0F, 44.0F, 2.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -0.75F, 0.0F));

        main.addChild("supports", ModelPartBuilder.create().uv(60, 127).cuboid(12.0F, -22.0F, -14.0F, 2.0F, 44.0F, 2.0F, new Dilation(0.0F))
                .uv(69, 127).cuboid(-14.0F, -22.0F, -14.0F, 2.0F, 44.0F, 2.0F, new Dilation(0.0F))
                .uv(87, 127).cuboid(-14.0F, -22.0F, 12.0F, 2.0F, 44.0F, 2.0F, new Dilation(0.0F))
                .uv(78, 127).cuboid(12.0F, -22.0F, 12.0F, 2.0F, 44.0F, 2.0F, new Dilation(0.0F))
                .uv(113, 0).cuboid(-3.0F, -22.0F, -3.0F, 6.0F, 44.0F, 6.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -9.5F, 0.0F));

        ModelPartData stirring_rods = main.addChild("stirring_rods", ModelPartBuilder.create().uv(45, 127).cuboid(-1.0F, -21.5F, -10.25F, 2.0F, 43.0F, 5.0F, new Dilation(0.0F))
                .uv(30, 127).cuboid(-1.0F, -21.5F, 4.75F, 2.0F, 43.0F, 5.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, -10.0F, 0.25F));

        stirring_rods.addChild("cube_r1", ModelPartBuilder.create().uv(0, 127).cuboid(0.0F, -20.5F, 4.5F, 2.0F, 43.0F, 5.0F, new Dilation(0.0F))
                .uv(15, 127).cuboid(0.0F, -20.5F, -11.5F, 2.0F, 43.0F, 5.0F, new Dilation(0.0F)), ModelTransform.of(-1.0F, -1.0F, -0.75F, 0.0F, -1.5708F, 0.0F));

        return TexturedModelData.of(modelData, 256, 256);
    }

    @Override
    public void setAngles(MixerModelRenderState state) {
        super.setAngles(state);
        this.parts.stirringRods().yaw = state.stirringRotation;
    }

    public record MixerModelRenderState(float stirringRotation) {
    }

    public record MixerModelParts(ModelPart main, ModelPart glass, ModelPart supports, ModelPart stirringRods) {
    }
}
