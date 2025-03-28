// Made with Blockbench 4.12.3
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package com.example.mod;
   
public class RotaryKilnModel extends EntityModel<Entity> {
	private final ModelPart seg0;
	private final ModelPart seg1;
	private final ModelPart seg2;
	private final ModelPart rotate_seg2;
	private final ModelPart seg3;
	private final ModelPart rotate_seg3;
	private final ModelPart seg4;
	private final ModelPart rotate_seg4;
	private final ModelPart seg5;
	private final ModelPart rotate_seg5;
	private final ModelPart seg6;
	private final ModelPart rotate_seg6;
	private final ModelPart seg7;
	private final ModelPart rotate_seg7;
	private final ModelPart seg8;
	private final ModelPart rotate_seg8;
	private final ModelPart seg9;
	private final ModelPart rotate_seg9;
	private final ModelPart seg10;
	private final ModelPart rotate_seg10;
	private final ModelPart seg11;
	private final ModelPart rotate_seg11;
	private final ModelPart seg12;
	private final ModelPart rotate_seg12;
	private final ModelPart seg13;
	private final ModelPart rotate_seg13;
	private final ModelPart seg14;
	private final ModelPart rotate_seg14;
	private final ModelPart seg15;
	private final ModelPart rotate_seg15;
	public RotaryKilnModel(ModelPart root) {
		this.seg0 = root.getChild("seg0");
		this.seg1 = root.getChild("seg1");
		this.seg2 = root.getChild("seg2");
		this.rotate_seg2 = root.getChild("rotate_seg2");
		this.seg3 = root.getChild("seg3");
		this.rotate_seg3 = root.getChild("rotate_seg3");
		this.seg4 = root.getChild("seg4");
		this.rotate_seg4 = root.getChild("rotate_seg4");
		this.seg5 = root.getChild("seg5");
		this.rotate_seg5 = root.getChild("rotate_seg5");
		this.seg6 = root.getChild("seg6");
		this.rotate_seg6 = root.getChild("rotate_seg6");
		this.seg7 = root.getChild("seg7");
		this.rotate_seg7 = root.getChild("rotate_seg7");
		this.seg8 = root.getChild("seg8");
		this.rotate_seg8 = root.getChild("rotate_seg8");
		this.seg9 = root.getChild("seg9");
		this.rotate_seg9 = root.getChild("rotate_seg9");
		this.seg10 = root.getChild("seg10");
		this.rotate_seg10 = root.getChild("rotate_seg10");
		this.seg11 = root.getChild("seg11");
		this.rotate_seg11 = root.getChild("rotate_seg11");
		this.seg12 = root.getChild("seg12");
		this.rotate_seg12 = root.getChild("rotate_seg12");
		this.seg13 = root.getChild("seg13");
		this.rotate_seg13 = root.getChild("rotate_seg13");
		this.seg14 = root.getChild("seg14");
		this.rotate_seg14 = root.getChild("rotate_seg14");
		this.seg15 = root.getChild("seg15");
		this.rotate_seg15 = root.getChild("rotate_seg15");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData seg0 = modelPartData.addChild("seg0", ModelPartBuilder.create().uv(0, 0).cuboid(-40.0F, -64.0F, -8.0F, 80.0F, 80.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 8.0F, 0.0F));

		ModelPartData seg1 = modelPartData.addChild("seg1", ModelPartBuilder.create().uv(386, 329).cuboid(-40.0F, 0.0F, -8.0F, 30.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(0, 97).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(386, 264).cuboid(10.0F, 0.0F, -8.0F, 30.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(195, 591).cuboid(-10.0F, 45.0F, -8.0F, 20.0F, 3.0F, 3.0F, new Dilation(0.0F))
		.uv(0, 394).cuboid(8.0F, -16.0F, -8.0F, 32.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(162, 396).cuboid(-40.0F, -16.0F, -8.0F, 32.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(73, 589).cuboid(-10.0F, 0.0F, 7.0F, 20.0F, 32.0F, 2.0F, new Dilation(0.0F))
		.uv(698, 569).cuboid(-8.0F, 0.0F, -8.0F, 16.0F, 26.0F, 2.0F, new Dilation(0.0F))
		.uv(324, 396).cuboid(8.0F, 0.0F, -8.0F, 2.0F, 45.0F, 15.0F, new Dilation(0.0F))
		.uv(579, 0).cuboid(-10.0F, 0.0F, -8.0F, 2.0F, 45.0F, 15.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -16.0F));

		ModelPartData cube_r1 = seg1.addChild("cube_r1", ModelPartBuilder.create().uv(694, 464).cuboid(-10.0F, 0.0F, -1.0F, 20.0F, 20.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 32.0F, 8.0F, -0.8727F, 0.0F, 0.0F));

		ModelPartData seg2 = modelPartData.addChild("seg2", ModelPartBuilder.create().uv(97, 394).cuboid(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(0, 130).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(386, 394).cuboid(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(0, 163).cuboid(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -32.0F));

		ModelPartData cube_r2 = seg2.addChild("cube_r2", ModelPartBuilder.create().uv(195, 598).cuboid(-10.0F, 20.0F, -1.0F, 20.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 32.0F, 24.0F, -0.8727F, 0.0F, 0.0F));

		ModelPartData rotate_seg2 = seg2.addChild("rotate_seg2", ModelPartBuilder.create().uv(579, 80).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(579, 118).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(329, 646).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(403, 646).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData octagon_r1 = rotate_seg2.addChild("octagon_r1", ModelPartBuilder.create().uv(366, 646).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(292, 646).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(579, 99).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(579, 61).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		ModelPartData seg3 = modelPartData.addChild("seg3", ModelPartBuilder.create().uv(259, 396).cuboid(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(193, 0).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(0, 427).cuboid(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(193, 33).cuboid(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -48.0F));

		ModelPartData rotate_seg3 = seg3.addChild("rotate_seg3", ModelPartBuilder.create().uv(579, 156).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(579, 194).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(477, 646).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(551, 646).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData octagon_r2 = rotate_seg3.addChild("octagon_r2", ModelPartBuilder.create().uv(514, 646).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(440, 646).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(579, 175).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(579, 137).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		ModelPartData seg4 = modelPartData.addChild("seg4", ModelPartBuilder.create().uv(162, 429).cuboid(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(193, 66).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(451, 394).cuboid(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(193, 99).cuboid(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -64.0F));

		ModelPartData rotate_seg4 = seg4.addChild("rotate_seg4", ModelPartBuilder.create().uv(579, 232).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(581, 413).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(625, 646).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(652, 94).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData octagon_r3 = rotate_seg4.addChild("octagon_r3", ModelPartBuilder.create().uv(652, 57).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(588, 646).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(581, 394).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(579, 213).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		ModelPartData seg5 = modelPartData.addChild("seg5", ModelPartBuilder.create().uv(65, 459).cuboid(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(193, 132).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(324, 459).cuboid(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(193, 165).cuboid(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -80.0F));

		ModelPartData rotate_seg5 = seg5.addChild("rotate_seg5", ModelPartBuilder.create().uv(584, 451).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(584, 489).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(652, 168).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(654, 384).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData octagon_r4 = rotate_seg5.addChild("octagon_r4", ModelPartBuilder.create().uv(652, 205).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(652, 131).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(584, 470).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(581, 432).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		ModelPartData seg6 = modelPartData.addChild("seg6", ModelPartBuilder.create().uv(389, 459).cuboid(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(0, 196).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(454, 459).cuboid(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(193, 198).cuboid(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -96.0F));

		ModelPartData rotate_seg6 = seg6.addChild("rotate_seg6", ModelPartBuilder.create().uv(333, 589).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(479, 589).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(657, 458).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(37, 662).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData octagon_r5 = rotate_seg6.addChild("octagon_r5", ModelPartBuilder.create().uv(0, 660).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(657, 421).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(406, 589).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(260, 589).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		ModelPartData seg7 = modelPartData.addChild("seg7", ModelPartBuilder.create().uv(227, 461).cuboid(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(0, 229).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(479, 264).cuboid(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(193, 231).cuboid(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -112.0F));

		ModelPartData rotate_seg7 = seg7.addChild("rotate_seg7", ModelPartBuilder.create().uv(195, 608).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(341, 608).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(662, 622).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(111, 665).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData octagon_r6 = rotate_seg7.addChild("octagon_r6", ModelPartBuilder.create().uv(662, 659).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(74, 662).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(268, 608).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(552, 589).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		ModelPartData seg8 = modelPartData.addChild("seg8", ModelPartBuilder.create().uv(479, 329).cuboid(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(0, 262).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(0, 492).cuboid(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(193, 264).cuboid(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -128.0F));

		ModelPartData rotate_seg8 = seg8.addChild("rotate_seg8", ModelPartBuilder.create().uv(487, 608).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(609, 251).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(185, 665).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(682, 242).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData octagon_r7 = rotate_seg8.addChild("octagon_r7", ModelPartBuilder.create().uv(222, 665).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(148, 665).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(560, 608).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(414, 608).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		ModelPartData seg9 = modelPartData.addChild("seg9", ModelPartBuilder.create().uv(130, 494).cuboid(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(0, 295).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(516, 394).cuboid(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(193, 297).cuboid(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -144.0F));

		ModelPartData rotate_seg9 = seg9.addChild("rotate_seg9", ModelPartBuilder.create().uv(609, 289).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(609, 327).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(682, 316).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(296, 683).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData octagon_r8 = rotate_seg9.addChild("octagon_r8", ModelPartBuilder.create().uv(259, 683).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(682, 279).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(609, 308).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(609, 270).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		ModelPartData seg10 = modelPartData.addChild("seg10", ModelPartBuilder.create().uv(519, 459).cuboid(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(0, 328).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(65, 524).cuboid(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(193, 330).cuboid(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -160.0F));

		ModelPartData rotate_seg10 = seg10.addChild("rotate_seg10", ModelPartBuilder.create().uv(609, 365).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(614, 19).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(370, 683).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(444, 683).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData octagon_r9 = rotate_seg10.addChild("octagon_r9", ModelPartBuilder.create().uv(407, 683).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(333, 683).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(614, 0).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(609, 346).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		ModelPartData seg11 = modelPartData.addChild("seg11", ModelPartBuilder.create().uv(292, 524).cuboid(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(0, 361).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(357, 524).cuboid(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(193, 363).cuboid(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -176.0F));

		ModelPartData rotate_seg11 = seg11.addChild("rotate_seg11", ModelPartBuilder.create().uv(617, 508).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(617, 546).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(518, 683).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(592, 683).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData octagon_r10 = rotate_seg11.addChild("octagon_r10", ModelPartBuilder.create().uv(555, 683).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(481, 683).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(617, 527).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(614, 38).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		ModelPartData seg12 = modelPartData.addChild("seg12", ModelPartBuilder.create().uv(422, 524).cuboid(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(386, 0).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(487, 524).cuboid(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(386, 33).cuboid(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -192.0F));

		ModelPartData rotate_seg12 = seg12.addChild("rotate_seg12", ModelPartBuilder.create().uv(0, 622).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(625, 584).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(689, 37).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(689, 111).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData octagon_r11 = rotate_seg12.addChild("octagon_r11", ModelPartBuilder.create().uv(689, 74).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(687, 0).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(73, 624).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(617, 565).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		ModelPartData seg13 = modelPartData.addChild("seg13", ModelPartBuilder.create().uv(195, 526).cuboid(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(386, 66).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(544, 264).cuboid(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(386, 99).cuboid(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -208.0F));

		ModelPartData rotate_seg13 = seg13.addChild("rotate_seg13", ModelPartBuilder.create().uv(73, 643).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(219, 646).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(0, 697).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(74, 699).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData octagon_r12 = rotate_seg13.addChild("octagon_r12", ModelPartBuilder.create().uv(37, 699).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(666, 696).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(146, 646).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(0, 641).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		ModelPartData seg14 = modelPartData.addChild("seg14", ModelPartBuilder.create().uv(544, 329).cuboid(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(386, 132).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(552, 524).cuboid(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(386, 165).cuboid(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -224.0F));

		ModelPartData rotate_seg14 = seg14.addChild("rotate_seg14", ModelPartBuilder.create().uv(219, 627).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(365, 627).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(689, 185).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(690, 532).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData octagon_r13 = rotate_seg14.addChild("octagon_r13", ModelPartBuilder.create().uv(690, 495).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(689, 148).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(292, 627).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(146, 627).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		ModelPartData seg15 = modelPartData.addChild("seg15", ModelPartBuilder.create().uv(0, 557).cuboid(-40.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(386, 198).cuboid(-40.0F, 48.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F))
		.uv(130, 559).cuboid(24.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F, new Dilation(0.0F))
		.uv(386, 231).cuboid(-40.0F, -16.0F, -8.0F, 80.0F, 16.0F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -40.0F, -240.0F));

		ModelPartData rotate_seg15 = seg15.addChild("rotate_seg15", ModelPartBuilder.create().uv(511, 627).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(633, 603).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(694, 390).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(629, 696).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData octagon_r14 = rotate_seg15.addChild("octagon_r14", ModelPartBuilder.create().uv(694, 427).cuboid(-24.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(691, 353).cuboid(22.0F, -9.9411F, -8.0F, 2.0F, 19.8823F, 16.0F, new Dilation(0.0F))
		.uv(584, 627).cuboid(-9.9411F, -24.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F))
		.uv(438, 627).cuboid(-9.9411F, 22.0F, -8.0F, 19.8823F, 2.0F, 16.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));
		return TexturedModelData.of(modelData, 1024, 1024);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		seg0.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg1.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg2.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg3.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg4.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg5.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg6.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg7.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg8.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg9.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg10.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg11.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg12.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg13.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg14.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		seg15.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}