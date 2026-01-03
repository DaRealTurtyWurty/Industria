package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class DrillFrameModel extends Model<DrillFrameModel.DrillFrameModelRenderState> {
    public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Industria.id("drill_frame"), "main");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/drill_frame.png");

    private final ModelPart cableWheel;
    private final ModelPart cableWheelRod;

    public DrillFrameModel(ModelPart root) {
        super(root, RenderLayers::entityCutout);
        ModelPart main = root.getChild("main");
        this.cableWheel = main.getChild("cableWheel");
        this.cableWheelRod = main.getChild("cableWheelRod");
    }

    public static TexturedModelData getTexturedModelData() {
        var modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create().uv(33, 85).cuboid(15.9135F, 36.3298F, 15.1928F, 8.0F, 2.0F, 8.0F, new Dilation(0.0F))
                .uv(66, 85).cuboid(15.9135F, 36.3298F, -24.8072F, 8.0F, 2.0F, 8.0F, new Dilation(0.0F))
                .uv(86, 72).cuboid(-24.0865F, 36.3298F, -24.8072F, 8.0F, 2.0F, 8.0F, new Dilation(0.0F))
                .uv(86, 61).cuboid(-24.0865F, 36.3298F, 15.1928F, 8.0F, 2.0F, 8.0F, new Dilation(0.0F))
                .uv(68, 31).cuboid(-11.0865F, -3.3146F, -6.8072F, 5.0F, 2.0F, 12.0F, new Dilation(0.0F))
                .uv(67, 0).cuboid(-11.0865F, -3.3146F, -11.8072F, 22.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(67, 8).cuboid(-11.0865F, -3.3146F, 5.1928F, 22.0F, 2.0F, 5.0F, new Dilation(0.0F))
                .uv(68, 46).cuboid(5.9135F, -3.3146F, -6.8072F, 5.0F, 2.0F, 12.0F, new Dilation(0.0F))
                .uv(23, 100).cuboid(6.9135F, -15.3146F, 6.1928F, 2.0F, 12.0F, 2.0F, new Dilation(0.0F))
                .uv(103, 31).cuboid(-9.0865F, -15.3146F, 6.1928F, 2.0F, 12.0F, 2.0F, new Dilation(0.0F))
                .uv(103, 46).cuboid(-9.0865F, -15.3146F, -9.8072F, 2.0F, 12.0F, 2.0F, new Dilation(0.0F))
                .uv(103, 101).cuboid(6.9135F, -15.3146F, -9.8072F, 2.0F, 12.0F, 2.0F, new Dilation(0.0F))
                .uv(53, 68).cuboid(6.9135F, -15.3146F, -7.8072F, 2.0F, 2.0F, 14.0F, new Dilation(0.0F))
                .uv(0, 73).cuboid(-9.0865F, -15.3146F, -7.8072F, 2.0F, 2.0F, 14.0F, new Dilation(0.0F))
                .uv(0, 90).cuboid(-7.0865F, -15.3146F, -9.8072F, 14.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 95).cuboid(-7.0865F, -15.3146F, 6.1928F, 14.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(86, 101).cuboid(-1.9652F, -19.3122F, -2.9285F, 4.0F, 6.0F, 4.0F, new Dilation(0.0F))
                .uv(0, 105).cuboid(2.0348F, -19.3122F, -1.9285F, 7.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(33, 73).cuboid(-0.9652F, -19.3122F, -9.9285F, 2.0F, 2.0F, 7.0F, new Dilation(0.0F))
                .uv(67, 96).cuboid(-0.9652F, -19.3122F, 1.0715F, 2.0F, 2.0F, 7.0F, new Dilation(0.0F))
                .uv(32, 111).cuboid(-0.9652F, -17.3122F, -9.9285F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(41, 111).cuboid(-0.9652F, -17.3122F, 6.0715F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(67, 106).cuboid(-8.9652F, -19.3122F, -1.9285F, 7.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 110).cuboid(7.0348F, -17.3122F, -1.9285F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(9, 110).cuboid(-8.9652F, -17.3122F, -1.9285F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(18, 110).cuboid(0.0358F, -13.3122F, -1.9285F, 0.0F, 3.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-15.0865F, 24.3298F, -17.8072F, 31.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 68).cuboid(-12.0865F, 11.3298F, -14.8072F, 24.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 5).cuboid(-15.0865F, 24.3298F, 14.1928F, 31.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(68, 16).cuboid(-12.0865F, 11.3298F, 11.1928F, 24.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.origin(0.0865F, -14.3298F, 0.8072F));

        ModelPartData cube_r1 = main.addChild("cube_r1", ModelPartBuilder.create().uv(68, 21).cuboid(-12.25F, -7.5F, 12.0F, 24.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 10).cuboid(-15.25F, 5.5F, 15.0F, 31.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(68, 26).cuboid(-12.25F, -7.5F, -14.0F, 24.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 15).cuboid(-15.25F, 5.5F, -17.0F, 31.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.1635F, 18.8298F, -0.8072F, 0.0F, -1.5708F, 0.0F));

        ModelPartData cube_r2 = main.addChild("cube_r2", ModelPartBuilder.create().uv(0, 100).cuboid(-10.0F, -0.9986F, -1.0F, 9.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(99, 83).cuboid(1.0F, -0.9986F, -1.0F, 9.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0348F, -14.3146F, -0.9285F, 0.0F, -0.7854F, 0.0F));

        ModelPartData cube_r3 = main.addChild("cube_r3", ModelPartBuilder.create().uv(99, 88).cuboid(-7.0F, -0.9986F, -1.0F, 9.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(86, 96).cuboid(4.0F, -0.9986F, -1.0F, 9.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-2.0865F, -14.3146F, 1.1928F, 0.0F, 0.7854F, 0.0F));

        ModelPartData cube_r4 = main.addChild("cube_r4", ModelPartBuilder.create().uv(34, 20).cuboid(-2.0F, -21.5F, -2.0F, 4.0F, 43.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(-13.8099F, 17.3142F, -14.5305F, -2.7489F, -0.7854F, -3.1416F));

        ModelPartData cube_r5 = main.addChild("cube_r5", ModelPartBuilder.create().uv(51, 20).cuboid(-2.0F, -21.5F, -2.0F, 4.0F, 43.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(13.6368F, 17.3142F, 12.9162F, 2.7489F, -0.7854F, 3.1416F));

        ModelPartData cube_r6 = main.addChild("cube_r6", ModelPartBuilder.create().uv(17, 20).cuboid(-2.0F, -21.5F, -2.0F, 4.0F, 43.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(-13.8099F, 17.3142F, 12.9162F, 0.3927F, -0.7854F, 0.0F));

        ModelPartData cube_r7 = main.addChild("cube_r7", ModelPartBuilder.create().uv(0, 20).cuboid(-2.0F, -21.5F, -2.0F, 4.0F, 43.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(13.6368F, 17.3142F, -14.5305F, -0.3927F, -0.7854F, 0.0F));

        ModelPartData cableWheelRod = main.addChild("cableWheelRod", ModelPartBuilder.create().uv(68, 64).cuboid(1.0F, -0.5F, -0.5F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(68, 61).cuboid(-7.0F, -0.5F, -0.5F, 6.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.origin(-0.0865F, -8.3146F, 7.1928F));

        ModelPartData cableWheel = main.addChild("cableWheel", ModelPartBuilder.create().uv(109, 93).cuboid(0.0F, -2.0F, -2.0F, 1.0F, 3.0F, 3.0F, new Dilation(0.0F))
                .uv(33, 96).cuboid(1.0F, -4.0F, -4.0F, 1.0F, 7.0F, 7.0F, new Dilation(0.0F))
                .uv(50, 96).cuboid(-1.0F, -4.0F, -4.0F, 1.0F, 7.0F, 7.0F, new Dilation(0.0F)), ModelTransform.origin(-0.4642F, -7.8122F, 7.5715F));
        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void setAngles(DrillFrameModelRenderState state) {
        super.setAngles(state);
        this.cableWheel.pitch = state.clientMotorRotation;
        this.cableWheelRod.pitch = state.clientMotorRotation;
    }

    public record DrillFrameModelRenderState(float clientMotorRotation) {
    }
}
