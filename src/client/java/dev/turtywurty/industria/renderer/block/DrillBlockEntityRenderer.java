package dev.turtywurty.industria.renderer.block;

import com.mojang.datafixers.util.Either;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.model.DrillFrameModel;
import dev.turtywurty.industria.model.DrillMotorModel;
import dev.turtywurty.industria.registry.DrillHeadRegistry;
import dev.turtywurty.industria.util.DrillHeadable;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

public class DrillBlockEntityRenderer implements BlockEntityRenderer<DrillBlockEntity> {
    private static final Identifier TEXTURE_LOCATION = Industria.id("textures/block/drill_frame.png");

    private final Map<DrillHeadable, Model> drillHeadModels = new HashMap<>();
    private final Map<DrillHeadable, Identifier> drillHeadTextures = new HashMap<>();

    private final BlockEntityRendererFactory.Context context;
    private final DrillFrameModel model;
    private final DrillMotorModel motorModel;

    public DrillBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
        this.model = new DrillFrameModel(context.getLayerModelPart(DrillFrameModel.LAYER_LOCATION));
        this.motorModel = new DrillMotorModel(context.getLayerModelPart(DrillMotorModel.LAYER_LOCATION));
    }

    @Override
    public void render(DrillBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 + switch (entity.getCachedState().get(Properties.HORIZONTAL_FACING)) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        }));

        this.model.render(matrices, vertexConsumers.getBuffer(this.model.getLayer(TEXTURE_LOCATION)), light, overlay);

        if (!entity.getMotorInventory().isEmpty())
            this.motorModel.render(matrices, vertexConsumers.getBuffer(this.motorModel.getLayer(DrillMotorModel.TEXTURE_LOCATION)), light, overlay);

        ItemStack drillHeadStack = entity.getDrillStack();
        if (drillHeadStack.isEmpty() || !(drillHeadStack.getItem() instanceof DrillHeadable drillHeadable)) {
            matrices.pop();
            return;
        }

        DrillHeadRegistry.DrillHeadClientData drillHeadData = DrillHeadRegistry.getClientData(drillHeadable);
        if (drillHeadData == null) {
            matrices.pop();
            return;
        }

        VertexConsumer linesVertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
        linesVertexConsumer.vertex(matrices.peek(), 0, -1.54f, 0)
                .color(70, 70, 70, 255)
                .normal(0, 1, 0);

        Model drillHeadModel = this.drillHeadModels.computeIfAbsent(drillHeadable, ignored -> drillHeadData.modelResolver().apply(Either.left(this.context)));
        Identifier drillHeadTexture = this.drillHeadTextures.computeIfAbsent(drillHeadable, ignored -> drillHeadData.textureLocation());

        matrices.push();
        matrices.translate(0, -entity.getDrillYOffset(), 0);

        linesVertexConsumer.vertex(matrices.peek(), 0, 0.5f, 0)
                .color(70, 70, 70, 255)
                .normal(0, 1, 0);

        drillHeadData.onRender().render(entity, drillHeadStack, tickDelta, matrices, vertexConsumers, drillHeadModel, vertexConsumers.getBuffer(drillHeadModel.getLayer(drillHeadTexture)), light, overlay);

        matrices.pop();

        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(DrillBlockEntity blockEntity) {
        return blockEntity.isDrilling() || blockEntity.isRetracting() && blockEntity.getDrillYOffset() < -1F;
    }

    @Override
    public boolean isInRenderDistance(DrillBlockEntity blockEntity, Vec3d pos) {
        return blockEntity.getPos().isWithinDistance(pos, 256);
    }
}
