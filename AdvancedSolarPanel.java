// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class AdvancedSolarPanel_Converted<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "advancedsolarpanel_converted"), "main");
	private final ModelPart main;
	private final ModelPart top;
	private final ModelPart panel;

	public AdvancedSolarPanel_Converted(ModelPart root) {
		this.main = root.getChild("main");
		this.top = this.main.getChild("top");
		this.panel = this.top.getChild("panel");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 18).addBox(-6.0F, 2.0F, -6.0F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(22, 36).addBox(-8.0F, 3.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 36).addBox(-5.0F, 3.0F, -7.0F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 3).addBox(-6.5F, 5.5F, 4.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-6.5F, 3.5F, 4.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));

		PartDefinition Wire_r1 = main.addOrReplaceChild("Wire_r1", CubeListBuilder.create().texOffs(0, 9).addBox(-1.5F, -0.5F, -0.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 6).addBox(-1.5F, -2.5F, -0.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 6.0F, -5.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition Plug_r1 = main.addOrReplaceChild("Plug_r1", CubeListBuilder.create().texOffs(22, 36).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 5.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition top = main.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 41).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(22, 58).addBox(-8.0F, -7.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(22, 58).addBox(7.0F, -7.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(21, 52).addBox(-8.0F, -4.0F, -1.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition panel = top.addOrReplaceChild("panel", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -7.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(29, 57).addBox(6.5F, -7.5F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(39, 57).addBox(-8.5F, -7.5F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}