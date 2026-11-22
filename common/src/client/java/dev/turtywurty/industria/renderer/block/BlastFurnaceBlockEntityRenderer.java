package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.BlastFurnaceBlock.BlastFurnaceGeometry;
import dev.turtywurty.industria.blockentity.BlastFurnaceBlockEntity;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import dev.turtywurty.industria.util.ModelSubmitUtils;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;

public class BlastFurnaceBlockEntityRenderer
    extends IndustriaBlockEntityRenderer<BlastFurnaceBlockEntity, IndustriaBlockEntityRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Industria.id("blast_furnace"), "main");
    private static final Identifier TEXTURE = Industria.id("textures/entity/blast_furnace.png");
    private final Model.Simple shell, coldBed, hotBed;

    public BlastFurnaceBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        ModelPart root = context.bakeLayer(LAYER);
        shell = new Model.Simple(root.getChild("shell"), RenderTypes::entitySolid);
        coldBed = new Model.Simple(root.getChild("cold_bed"), RenderTypes::entitySolid);
        hotBed = new Model.Simple(root.getChild("hot_bed"), RenderTypes::entitySolid);
    }

    public static LayerDefinition createLayer() {
        var mesh = new MeshDefinition();
        var shell = CubeListBuilder.create();
        var cold = CubeListBuilder.create();
        var hot = CubeListBuilder.create();
        for (BlastFurnaceGeometry.Box box : BlastFurnaceGeometry.BOXES) {
            switch (box.material()) {
                case FIREBRICK -> addBox(shell, box, 0);
                case STEEL -> addBox(shell, box, 128);
                case CAST_IRON -> addBox(shell, box, 240);
                case SOOT -> addBox(shell, box, 352);
                case FIREBED -> {
                    addBox(cold, box, 352);
                    addBox(hot, box, 448);
                }
            }
        }

        mesh.getRoot().addOrReplaceChild("shell", shell, PartPose.ZERO);
        mesh.getRoot().addOrReplaceChild("cold_bed", cold, PartPose.ZERO);
        mesh.getRoot().addOrReplaceChild("hot_bed", hot, PartPose.ZERO);
        return LayerDefinition.create(mesh, 256, 512);
    }

    private static void addBox(CubeListBuilder cubes, BlastFurnaceGeometry.Box box, int textureY) {
        cubes.texOffs(0, textureY).addBox(box.x(), box.y(), box.z(), box.width(), box.height(), box.depth());
    }

    @Override
    protected void setupBlockEntityTransformations(PoseStack poses, IndustriaBlockEntityRenderState state) {
        poses.pushPose();
        if (state.multiblockFormed) {
            Direction facing = state.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            poses.translate(0.5, 0, 0.5);
            poses.rotate(Axis.YP.rotationDegrees(180 - facing.toYRot()));
            poses.translate(-0.5 - BlastFurnaceGeometry.CONTROLLER_X / 16.0,
                -BlastFurnaceGeometry.CONTROLLER_Y / 16.0, -0.5 - BlastFurnaceGeometry.CONTROLLER_Z / 16.0);
        }
    }

    @Override
    protected void onRender(
        IndustriaBlockEntityRenderState state,
        PoseStack poses,
        SubmitNodeCollector queue,
        int light,
        int overlay
    ) {
        RenderType renderType = shell.renderType(TEXTURE);
        ModelSubmitUtils.submitModel(queue, shell, Unit.INSTANCE, poses, renderType, light, overlay, 0,
            state.breakProgress);
        boolean lit = state.blockState.getValue(BlockStateProperties.LIT);
        ModelSubmitUtils.submitModel(queue, lit ? hotBed : coldBed, Unit.INSTANCE, poses, renderType,
            lit ? LightCoordsUtil.FULL_BRIGHT : light, overlay, 0, state.breakProgress);
    }
}
