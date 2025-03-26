package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.CrystallizerBlockEntity;
import dev.turtywurty.industria.model.CrystallizerModel;
import dev.turtywurty.industria.util.ColorMode;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class CrystallizerBlockEntityRenderer extends IndustriaBlockEntityRenderer<CrystallizerBlockEntity> {
    private static final Vec3d[] OUTPUT_ITEM_POSITIONS = new Vec3d[64];

    static {
        var random = new Random(438489438L);

        float minX = -18f/16f + 0.125f;
        float minY = -0.25f;
        float minZ = -18f/16f + 0.125f;
        float maxX = 18f/16f - 0.125f;
        float maxY = 46f/16f - 0.625f;
        float maxZ = 18f/16f - 0.125f;

        for (int i = 0; i < OUTPUT_ITEM_POSITIONS.length; i++) {
            int attempts = 0;
            while(true) {
                Vec3d generatedPos = generatePosition(random, minX, -minY, minZ, maxX, -maxY, maxZ);
                if (!intersects(generatedPos, i, 0.35) || attempts++ > 100) {
                    OUTPUT_ITEM_POSITIONS[i] = generatedPos;
                    break;
                }
            }
        }
    }

    private static boolean intersects(Vec3d pos, int count, double radius) {
        for (int i = 0; i < count; i++) {
            if (pos.distanceTo(OUTPUT_ITEM_POSITIONS[i]) < radius) {
                return true;
            }
        }

        return false;
    }

    private static Vec3d generatePosition(Random random, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return new Vec3d(
                minX + random.nextFloat() * (maxX - minX),
                minY + random.nextFloat() * (maxY - minY),
                minZ + random.nextFloat() * (maxZ - minZ)
        );
    }

    private final CrystallizerModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public CrystallizerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new CrystallizerModel(context.getLayerModelPart(CrystallizerModel.LAYER_LOCATION));
    }

    @Override
    protected void onRender(CrystallizerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.model.render(matrices, vertexConsumers.getBuffer(this.model.getLayer(CrystallizerModel.TEXTURE_LOCATION)), light, overlay);

        renderNextOutputItem(entity, matrices, vertexConsumers, light, overlay);
        renderFluids(entity, matrices, vertexConsumers, light, overlay);
    }

    private void renderNextOutputItem(CrystallizerBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack itemStack = entity.getNextOutputItemStack();
        if (itemStack.isEmpty())
            return;

        int count = Math.min(itemStack.getCount(), OUTPUT_ITEM_POSITIONS.length);
        for (int i = 0; i < count; i++) {
            int index = (OUTPUT_ITEM_POSITIONS.length / count) * i;

            Vec3d position = OUTPUT_ITEM_POSITIONS[index];
            float progress = (float) entity.getProgress() / entity.getMaxProgress();
            float scale = 0.05f + 0.35f * progress;

            matrices.push();
            matrices.translate(position);
            matrices.scale(scale, scale, scale);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) ((i + 1) * 360) / count));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            this.context.getItemRenderer().renderItem(itemStack, ModelTransformationMode.NONE, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            this.context.getItemRenderer().renderItem(itemStack, ModelTransformationMode.NONE, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
            matrices.pop();
        }
    }

    private void renderFluids(CrystallizerBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.fluidRenderer.render(entity.getCrystalFluidStorage(),
                vertexConsumers, matrices,
                light, overlay,
                entity.getWorld(), entity.getPos(),
                -18f/16f, -0.5001f, -18f/16f,
                18f/16f, 46f, 18f/16f - 0.001f, 0x40000000, ColorMode.SUBTRACTION);

        this.fluidRenderer.render(entity.getWaterFluidStorage(),
                vertexConsumers, matrices,
                light, overlay,
                entity.getWorld(), entity.getPos(),
                -18f/16f, -0.5f, -18f/16f,
                18f/16f, 46f, 18f/16f);
    }
}
