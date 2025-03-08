package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.MixerBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.model.MixerModel;
import dev.turtywurty.industria.util.ColorMode;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class MixerBlockEntityRenderer extends IndustriaBlockEntityRenderer<MixerBlockEntity> {
    private final MixerModel model;
    private final InWorldFluidRenderingComponent fluidRenderer = new InWorldFluidRenderingComponent();

    public MixerBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new MixerModel(context.getLayerModelPart(MixerModel.LAYER_LOCATION));

        //this.fluidRenderer.setShouldDebugAmount(true);
    }

    @Override
    protected void onRender(MixerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        this.model.getMixerParts().stirring_rods().yaw = entity.stirringRotation;
        this.model.getMixerParts().main().render(matrices, vertexConsumers.getBuffer(this.model.getLayer(MixerModel.TEXTURE_LOCATION)), light, overlay);
        this.model.getMixerParts().stirring_rods().yaw = 0.0F;

        boolean isMixing = entity.isMixing();
        if (isMixing) {
            entity.stirringRotation = (entity.stirringRotation + 0.1f) % 360;
        }

        float widthReduction = 2f / 16f;
        float x1 = -1f + widthReduction;
        float y1 = -1.25f - (2f / 16f);
        float z1 = -1f + widthReduction;
        float x2 = 1f - widthReduction;
        float maxHeightPixels = 44f;
        float z2 = 1f - widthReduction;

        float width = x2 - x1;
        float depth = z2 - z1;

        float progress = (float) entity.getProgress() / entity.getMaxProgress();

        SyncingSimpleInventory inputInventory = entity.getInputInventory();
        for (int index = 0; index < inputInventory.heldStacks.size(); index++) {
            ItemStack stack = inputInventory.heldStacks.get(index);
            if (!stack.isEmpty()) {
                matrices.push();

                Vec3d position = entity.mixingItemPositions.get(index);

                if(isMixing) {
                    // Calculate angle for each item
                    float angle = (float) (2 * Math.PI * index / inputInventory.heldStacks.size()); // Evenly space items around circle
                    // Add rotation over time using world time
                    float rotationSpeed = 0.1f; // Adjust this value to change rotation speed
                    float timeAngle = (float) entity.getWorld().getTime() * rotationSpeed;

                    // Define radius of the circle (you can adjust this value)
                    float radius = Math.max(width, depth) * 0.5f - 0.375f;

                    // Calculate offsets using sine and cosine
                    double xOffset = radius * Math.sin(angle + timeAngle);
                    double yOffset = Math.sin(angle + index + timeAngle * 0.2f) - 1f;
                    double zOffset = radius * Math.cos(angle + timeAngle);

                    position = position.add(xOffset, yOffset, zOffset);
                }

                matrices.translate(position.x, position.y, position.z);
                matrices.scale(0.5f, 0.5f, 0.5f);

                if(isMixing) {
                    matrices.scale(1f - progress, 1f - progress, 1f - progress); // TODO: Make them fade away instead (maybe? :3)
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotation(entity.getWorld().getTime() * 0.25f));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotation(entity.getWorld().getTime() * 0.25f));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotation(entity.getWorld().getTime() * 0.25f));
                }

                this.context.getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
                matrices.pop();
            }
        }

        // TODO: Temperature-based color
        this.fluidRenderer.render(entity.getInputFluidTank(),
                vertexConsumers, matrices,
                light, overlay,
                entity.getWorld(), entity.getPos(),
                x1, y1, z1,
                x2, maxHeightPixels, z2,
                0xFFFFFFFF, ColorMode.MULTIPLICATION);
    }
}
