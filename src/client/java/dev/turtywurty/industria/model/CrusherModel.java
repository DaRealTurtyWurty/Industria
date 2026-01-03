package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class CrusherModel extends Model<Float> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("crusher"), "main");
    public static final Identifier TEXTURE = Industria.id("textures/block/crusher.png");

    private final ModelPart bottomLeft;
    private final ModelPart topLeft;
    private final ModelPart bottomRight;
    private final ModelPart topRight;

    public CrusherModel(ModelPart root) {
        super(root, RenderLayers::entityCutoutNoCull);
        ModelPart main = root.getChild("main");
        this.bottomLeft = main.getChild("left").getChild("bottomLeft");
        this.topLeft = main.getChild("left").getChild("topLeft");
        this.bottomRight = main.getChild("right").getChild("bottomRight");
        this.topRight = main.getChild("right").getChild("topRight");
    }

    public static TexturedModelData getTexturedModelData() {
        var modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create(),
                ModelTransform.origin(0.0F, 20.3889F, 0.0F));

        ModelPartData structure = main.addChild("structure", ModelPartBuilder.create().uv(0, 0)
                        .cuboid(-8.0F, 0.75F, -8.0F, 16.0F, 1.0F, 16.0F, new Dilation(0.0F)).uv(19, 26)
                        .cuboid(7.0F, -6.25F, -8.0F, 1.0F, 7.0F, 16.0F, new Dilation(0.0F)).uv(0, 18)
                        .cuboid(-8.0F, -6.25F, -8.0F, 1.0F, 7.0F, 16.0F, new Dilation(0.0F)).uv(59, 35)
                        .cuboid(-7.0F, -6.25F, -8.0F, 14.0F, 7.0F, 1.0F, new Dilation(0.0F)).uv(57, 18)
                        .cuboid(-7.0F, -6.25F, 7.0F, 14.0F, 7.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.origin(0.0F, -0.1389F, 0.0F));

        structure.addChild("bars", ModelPartBuilder.create().uv(36, 53)
                        .cuboid(4.0F, -8.0F, -7.0F, 1.0F, 1.0F, 14.0F, new Dilation(0.0F)).uv(49, 0)
                        .cuboid(-3.0F, -5.0F, -7.0F, 1.0F, 1.0F, 14.0F, new Dilation(0.0F)).uv(53, 55)
                        .cuboid(-5.0F, -8.0F, -7.0F, 1.0F, 1.0F, 14.0F, new Dilation(0.0F)).uv(19, 50)
                        .cuboid(2.0F, -5.0F, -7.0F, 1.0F, 1.0F, 14.0F, new Dilation(0.0F)),
                ModelTransform.origin(0.0F, 3.75F, 0.0F));

        ModelPartData funnel = structure.addChild("funnel", ModelPartBuilder.create().uv(0, 42)
                        .cuboid(-1.0F, -1.4616F, -7.0F, 2.0F, 2.0F, 14.0F, new Dilation(0.0F)),
                ModelTransform.origin(0.0F, -4.7884F, 0.0F));

        funnel.addChild("cube_r1", ModelPartBuilder.create().uv(40, 36)
                        .cuboid(-1.5F, -1.5F, -7.0F, 2.0F, 2.0F, 14.0F, new Dilation(0.0F)),
                ModelTransform.of(1.5F, 0.0384F, 0.0F, 0.0F, 0.0F, -0.6545F));

        funnel.addChild("cube_r2", ModelPartBuilder.create().uv(38, 18)
                        .cuboid(-0.5F, -1.5F, -7.0F, 2.0F, 2.0F, 14.0F, new Dilation(0.0F)),
                ModelTransform.of(-1.5F, 0.0384F, 0.0F, 0.0F, 0.0F, 0.6545F));

        structure.addChild("feet", ModelPartBuilder.create().uv(66, 5)
                        .cuboid(6.0F, 1.75F, -8.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)).uv(66, 0)
                        .cuboid(6.0F, 1.75F, 6.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)).uv(0, 64)
                        .cuboid(-8.0F, 1.75F, 6.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)).uv(9, 64)
                        .cuboid(-8.0F, 1.75F, -8.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.origin(0.0F, 0.0F, 0.0F));

        ModelPartData left = main.addChild("left", ModelPartBuilder.create(),
                ModelTransform.origin(-2.5F, -0.8889F, 0.0F));

        left.addChild("bottomLeft", ModelPartBuilder.create().uv(0, 47)
                        .cuboid(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(0, 42)
                        .cuboid(-1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(38, 35)
                        .cuboid(-1.5F, -1.5F, 4.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(38, 23)
                        .cuboid(-1.5F, -1.5F, 2.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(0, 6)
                        .cuboid(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 2.0F, new Dilation(0.0F)).uv(37, 18)
                        .cuboid(-1.5F, -1.5F, -7.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(19, 28)
                        .cuboid(-1.5F, -1.5F, -5.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.origin(0.0F, 0.0F, 0.0F));

        left.addChild("topLeft", ModelPartBuilder.create().uv(0, 59)
                        .cuboid(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(59, 44)
                        .cuboid(-1.5F, -1.5F, 5.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(18, 59)
                        .cuboid(-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(9, 59)
                        .cuboid(-1.5F, -1.5F, 1.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(36, 58)
                        .cuboid(-1.5F, -1.5F, -6.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(53, 58)
                        .cuboid(-1.5F, -1.5F, -4.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.origin(-2.0F, -3.0F, 0.0F));

        ModelPartData right = main.addChild("right", ModelPartBuilder.create(),
                ModelTransform.origin(4.5F, -3.8889F, 0.0F));

        right.addChild("bottomRight", ModelPartBuilder.create().uv(0, 18)
                        .cuboid(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(19, 18)
                        .cuboid(-1.5F, -1.5F, 6.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(0, 23)
                        .cuboid(-1.5F, -1.5F, 4.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(19, 23)
                        .cuboid(-1.5F, -1.5F, 2.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(0, 28)
                        .cuboid(-1.5F, -1.5F, -7.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(28, 18)
                        .cuboid(-1.5F, -1.5F, -5.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(0, 0)
                        .cuboid(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 2.0F, new Dilation(0.0F)),
                ModelTransform.origin(-2.0F, 3.0F, 0.0F));

        right.addChild("topRight", ModelPartBuilder.create().uv(49, 0)
                        .cuboid(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(49, 5)
                        .cuboid(-1.5F, -1.5F, 5.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(19, 50)
                        .cuboid(-1.5F, -1.5F, 3.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(36, 53)
                        .cuboid(-1.5F, -1.5F, 1.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(53, 53)
                        .cuboid(-1.5F, -1.5F, -6.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).uv(57, 27)
                        .cuboid(-1.5F, -1.5F, -4.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)),
                ModelTransform.origin(0.0F, 0.0F, 0.0F));

        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void setAngles(Float rotation) {
        super.setAngles(rotation);
        this.bottomLeft.roll = rotation;
        this.topLeft.roll = rotation;
        this.bottomRight.roll = -rotation;
        this.topRight.roll = -rotation;
    }
}