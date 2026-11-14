package dev.turtywurty.industria.model;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.state.ContainerDoorRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class AlloyFurnaceModel extends Model<ContainerDoorRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Industria.id("alloy_furnace"), "main");
    public static final ModelLayerLocation ITEM_LAYER_LOCATION = new ModelLayerLocation(Industria.id("alloy_furnace"), "item");
    public static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/alloy_furnace.png");
    private final ModelPart main;
    private final ModelPart base;
    private final ModelPart front;
    private final ModelPart flame;
    private final ModelPart off;
    private final ModelPart on;
    private final ModelPart hatch;
    private final ModelPart inputs;
    private final ModelPart chimney;

    public AlloyFurnaceModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.main = root.getChild("main");
        this.base = this.main.getChild("base");
        this.front = this.base.getChild("front");
        this.flame = this.front.getChild("flame");
        this.off = this.flame.getChild("off");
        this.on = this.flame.getChild("on");
        this.hatch = this.main.getChild("hatch");
        this.inputs = this.main.getChild("inputs");
        this.chimney = this.main.getChild("chimney");
    }

    public static LayerDefinition createBodyLayer() {
        return createLayer(false);
    }

    public static LayerDefinition createItemLayer() {
        return createLayer(true);
    }

    @Override
    public void setupAnim(ContainerDoorRenderState state) {
        super.setupAnim(state);
        boolean lit = state.blockState.getValue(BlockStateProperties.LIT);
        this.off.visible = !lit;
        this.on.visible = lit;
        float progress = 1.0F - state.doorOpenness;
        this.hatch.zRot = -(1.0F - progress * progress * progress) * Mth.HALF_PI;
    }

    private static LayerDefinition createLayer(boolean item) {
        var meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition main = partDefinition.addOrReplaceChild("main", CubeListBuilder.create(),
            PartPose.offsetAndRotation(item ? 8.0F : 0.0F, 8.0F, item ? -17.5F : -9.5F, 0.0F, -(float) Math.PI / 2.0F, 0.0F));

        PartDefinition output_r1 = main.addOrReplaceChild("output_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(25.5F, 8.0F, -9.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition base = main.addOrReplaceChild("base", CubeListBuilder.create().texOffs(1, 64).addBox(-31.999F, -32.0F, 0.0F, 31.0F, 32.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(34.499F, 16.0F, -8.0F));

        PartDefinition front = base.addOrReplaceChild("front", CubeListBuilder.create().texOffs(104, 56).mirror().addBox(-32.999F, -32.0F, 0.0F, 1.0F, 32.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(84, 31).addBox(-32.999F, -32.0F, 7.0F, 1.0F, 6.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(112, 56).addBox(-32.999F, -32.0F, 25.0F, 1.0F, 32.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition flame = front.addOrReplaceChild("flame", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition off = flame.addOrReplaceChild("off", CubeListBuilder.create().texOffs(61, 17).addBox(-32.999F, -12.0F, 7.0F, 1.0F, 12.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(92, -18).addBox(-32.0F, -26.0F, 7.0F, 0.0F, 14.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition on = flame.addOrReplaceChild("on", item ? CubeListBuilder.create() : CubeListBuilder.create().texOffs(37, 33).addBox(-32.999F, -12.0F, 7.0F, 1.0F, 12.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(54, -18).addBox(-32.0F, -26.0F, 7.0F, 0.0F, 14.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hatch = main.addOrReplaceChild("hatch", CubeListBuilder.create().texOffs(34, 0).addBox(-2.0F, -10.999F, -4.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 3.999F, 8.0F));

        PartDefinition base_r1 = hatch.addOrReplaceChild("base_r1", CubeListBuilder.create().texOffs(24, 15).addBox(-9.0F, -13.999F, -1.0F, 18.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition inputs = main.addOrReplaceChild("inputs", CubeListBuilder.create().texOffs(17, 0).addBox(-26.0F, -27.0F, 13.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(59.5F, 16.0F, 0.0F));

        PartDefinition input1_r1 = inputs.addOrReplaceChild("input1_r1", CubeListBuilder.create().texOffs(17, 0).addBox(-1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-34.0F, -24.0F, 25.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition chimney = main.addOrReplaceChild("chimney", CubeListBuilder.create().texOffs(0, 31).addBox(-7.5F, -11.0F, -0.5F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 31).addBox(-7.5F, -12.0F, 0.5F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(32, 38).addBox(-0.5F, -12.0F, 0.5F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(32, 45).addBox(-7.5F, -12.0F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(32, 47).addBox(-7.5F, -12.0F, 6.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 52).addBox(-8.0F, -2.0F, -1.0F, 9.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(28.0F, -16.0F, -2.5F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }
}
