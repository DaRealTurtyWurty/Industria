package dev.turtywurty.industria.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.turtywurty.industria.component.FluidPocketsComponent;
import dev.turtywurty.industria.init.ComponentTypeInit;
import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.util.DoublePositionSource;
import dev.turtywurty.industria.util.IndustriaFluidRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SeismicScannerRendering {
    public static void renderSeismicScanner(ItemStack stack, ItemRenderer itemRenderer, BakedModel model, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (stack.isOf(ItemInit.SEISMIC_SCANNER)) {
            WorldFluidPocketsState.FluidPocket fluidPocket = null;
            if(mode.isFirstPerson() || mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND || mode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND) {
                fluidPocket = getFluidPocketFromStack(stack);
            }

            matrices.push();
            matrices.translate(0.5, 0.5, 0.5);

            if (mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND || mode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND) {
                matrices.translate(-0.5, -0.5, -0.5);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-13.0F));
                matrices.translate(-0.35, 0.5, 0.0);

                RenderLayer layer = RenderLayers.getItemLayer(stack, true);
                VertexConsumer consumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, layer, true, stack.hasGlint());

                itemRenderer.renderBakedItemModel(model, stack, light, overlay, matrices, consumer);

                if(fluidPocket != null) {
                    matrices.push();
                    matrices.scale(0.5f, 0.5f, 0.5f);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
                    matrices.translate(1.0f, 1.0625f, -0.75f);
                    renderHologram(fluidPocket, matrices, vertexConsumers, light);
                    matrices.pop();
                }
            } else {
                itemRenderer.renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, model);

                matrices.push();
                Transformation transformation = model.getTransformation().getTransformation(mode);
                transformation.apply(false, matrices);
                matrices.scale(0.5f, 0.5f, 0.5f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
                matrices.translate(0, 0.25, 0.75);

                if (mode.isFirstPerson()) {
                    createScannerSonar1stPerson();

                    if(fluidPocket != null) {
                        renderHologram(fluidPocket, matrices, vertexConsumers, light);
                    }
                }

                matrices.pop();
            }

            matrices.pop();
        }
    }

    private static @Nullable WorldFluidPocketsState.FluidPocket getFluidPocketFromStack(ItemStack stack) {
        if (!stack.contains(ComponentTypeInit.FLUID_POCKETS))
            return null;

        FluidPocketsComponent fluidPockets = stack.get(ComponentTypeInit.FLUID_POCKETS);
        if (fluidPockets == null)
            return null;

        return fluidPockets.pockets().getFirst();
    }

    // Draws a "cone"-type shape that extends outwards from the center and to the edge of the fluid pocket
    private static void renderHologram(WorldFluidPocketsState.FluidPocket fluidPocket, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        RenderSystem.disableDepthTest();

        int minX = fluidPocket.minX();
        int minY = fluidPocket.minY();
        int minZ = fluidPocket.minZ();
        int maxX = fluidPocket.maxX();
        int maxY = fluidPocket.maxY();
        int maxZ = fluidPocket.maxZ();

        // Calculate dimensions of the fluid pocket
        int width = maxX - minX;
        int height = maxY - minY;
        int depth = maxZ - minZ;

        // Calculate the minor and major radii of the oval shape
        float majorRadius = width / 2f;
        float minorRadius = depth / 2f;

        // Calculate the center of the fluid pocket
        int centerY = Math.max(1, MathHelper.ceil(height / 2f));

        // Draw the oval shape
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.LINE_STRIP);
        MatrixStack.Entry entry = matrices.peek();
        for (int i = 0; i < 360; i++) {
            float x = (float) (Math.cos(Math.toRadians(i)) * majorRadius);
            float z = (float) (Math.sin(Math.toRadians(i)) * minorRadius);

            consumer.vertex(entry, 0, 0, 0)
                    .color(0, 150, 250, 255)
                    .normal(0, 0, 0);

            consumer.vertex(entry, x / 16f, centerY / 8f, z / 16f)
                    .color(0, 200, 255, 0)
                    .normal(0, 0, 0);
        }

        renderFluidPocket(fluidPocket, matrices, vertexConsumers, light);
    }

    private static void createScannerSonar1stPerson() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null)
            return;

        if (player.getRandom().nextInt(30) == 0) {
            float playerYaw = player.headYaw;
            float playerPitch = player.prevPitch;
            float distance = 1.5f;

            Vec3d eyePos = player.getEyePos();
            double startX = eyePos.x;
            double startY = eyePos.y - 0.25f;
            double startZ = eyePos.z;

            double targetX = startX - Math.sin(Math.toRadians(playerYaw)) * distance;
            double targetY = startY - Math.tan(Math.toRadians(playerPitch)) * distance;
            double targetZ = startZ + Math.cos(Math.toRadians(playerYaw)) * distance;

            MinecraftClient.getInstance().particleManager.addParticle(
                    new VibrationParticleEffect(new DoublePositionSource(targetX, targetY, targetZ), 20),
                    startX,
                    startY,
                    startZ,
                    0.0D,
                    0.0D,
                    0.0D);
        }
    }

    private static void renderFluidPocket(WorldFluidPocketsState.FluidPocket fluidPocket, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        FluidState fluidState = fluidPocket.fluidState();
        List<BlockPos> positions = fluidPocket.fluidPositions();

        int minX = fluidPocket.minX();
        int minY = fluidPocket.minY();
        int minZ = fluidPocket.minZ();
        int maxX = fluidPocket.maxX();
        int maxY = fluidPocket.maxY();
        int maxZ = fluidPocket.maxZ();

        // Calculate dimensions of the fluid pocket
        int width = maxX - minX;
        int height = maxY - minY;
        int depth = maxZ - minZ;

        // Calculate the center of the fluid pocket
        int centerX = Math.max(1, MathHelper.ceil(width / 2f));
        int centerY = Math.max(1, MathHelper.ceil(height / 2f));
        int centerZ = Math.max(1, MathHelper.ceil(depth / 2f));

        // Translate to the center of the fluid pocket
        matrices.push();
        matrices.translate(-centerX / 16f, centerY / 32f, centerZ / 16f);
        matrices.translate(0, height / 16f, 0);

        var buffer = new IndustriaDynamicItemRenderer.DrawableVertexConsumer((VertexConsumerProvider.Immediate) vertexConsumers);
        buffer.draw(); // End the previous buffer (usually the player)

        RenderSystem.setShaderColor(1.0F, 2.0F, 5.0F, 0.5F);
        for (BlockPos position : positions) {
            int relativeX = position.getX() - minX;
            int relativeY = position.getY() - minY;
            int relativeZ = -(position.getZ() - minZ);

            matrices.push();
            matrices.translate(relativeX / 16f, relativeY / 16f, relativeZ / 16f);
            matrices.scale(0.0625F, 0.0625F, 0.0625F);

            IndustriaFluidRenderer.renderFluidBox(fluidState,
                    0.0F, 0.0F, 0.0F,
                    1.0F, 1.0F, 1.0F,
                    buffer, matrices, light, true,
                    1.0f, 1.0f, 1.0f, 1.0f, IndustriaFluidRenderer.ColorMode.MULTIPLICATION);

            matrices.pop();
        }

        buffer.draw();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        matrices.pop();
    }
}
