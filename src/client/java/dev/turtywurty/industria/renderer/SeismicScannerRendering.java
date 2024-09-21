package dev.turtywurty.industria.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

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
                    renderThirdPersonFluidHologram(fluidPocket, matrices, vertexConsumers, light);
                    renderThirdPersonHologram(fluidPocket, matrices, vertexConsumers);
                }
            } else {
                itemRenderer.renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, model);

                if (mode.isFirstPerson()) {
                    drawFirstPersonScannerSonar();

                    if(fluidPocket != null) {
                        renderFirstPersonHologram(fluidPocket, matrices, vertexConsumers);
                        renderFirstPersonFluidHologram(fluidPocket, matrices, vertexConsumers, light);
                    }
                }
            }

            matrices.pop();
        }
    }

    private static @Nullable WorldFluidPocketsState.FluidPocket getFluidPocketFromStack(ItemStack stack) {
        if (!stack.contains(ComponentTypeInit.FLUID_POCKETS))
            return null;

        List<WorldFluidPocketsState.FluidPocket> fluidPockets = stack.get(ComponentTypeInit.FLUID_POCKETS);
        if (fluidPockets == null || fluidPockets.isEmpty())
            return null;

        return fluidPockets.getFirst();
    }

    // Draws a "cone"-type shape that extends outwards from the center and to the edge of the fluid pocket
    private static void renderFirstPersonHologram(WorldFluidPocketsState.FluidPocket fluidPocket, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        matrices.push();

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

        // width should always be larger than the depth
        if (width < depth) {
            int temp = width;
            width = depth;
            depth = temp;
        }

        int centerX = Math.round(width / 2f);
        int centerY = Math.round(height / 2f);
        int centerZ = Math.round(depth / 2f);

        // Calculate the minor and major radii of the oval shape
        float majorRadius = width / 2f;
        float minorRadius = depth / 2f;

        // Translate to the center of the fluid pocket
        matrices.translate(centerX / 16f, centerY / 16f, centerZ / 16f);

        //System.out.println("Center X: " + centerX + ", Center Y: " + centerY + ", Center Z: " + centerZ);

        // Translate to the center of the seismic scanner
        matrices.translate(-(1 + 2/16f), 0.25f + height / 32f, -5/16f);

        // Draw the oval shape
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.LINE_STRIP);
        MatrixStack.Entry entry = matrices.peek();
        for (int i = 0; i < 360; i+=1) {
            float x = (float) (centerX + Math.cos(Math.toRadians(i)) * majorRadius);
            float z = (float) (centerZ + Math.sin(Math.toRadians(i)) * minorRadius);

            consumer.vertex(entry, centerX / 32f, -height / 16f, centerZ / 32f)
                    .color(0, 150, 250, 255)
                    .normal(0, 0, 0);

            consumer.vertex(entry, x / 32f, 0, z / 32f)
                    .color(0, 200, 255, 0)
                    .normal(0, 0, 0);
        }

        matrices.pop();
    }

    // Draws rays outwards from the middle of the seismic scanner to the edge of the fluid pocket
    private static void renderThirdPersonHologram(WorldFluidPocketsState.FluidPocket fluidPocket, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        matrices.push();
        matrices.translate(0.125f, 0.25f, 0.125f);

        int minX = fluidPocket.minX();
        int minY = fluidPocket.minY();
        int minZ = fluidPocket.minZ();
        int maxX = fluidPocket.maxX();
        int maxY = fluidPocket.maxY();
        int maxZ = fluidPocket.maxZ();

        int width = maxX - minX;
        int height = maxY - minY;
        int depth = maxZ - minZ;

        // width should always be larger than the depth
        if (width < depth) {
            int temp = width;
            width = depth;
            depth = temp;
        }

        int radius = Math.max(width, Math.max(height, depth)) / 2;

        Vector3f center = new Vector3f(width / 2f, height / 2f, depth / 2f);

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.LINE_STRIP);
        MatrixStack.Entry entry = matrices.peek();

        for (int i = 0; i < 360; i += 10) {
            float x = (float) (center.x + Math.cos(Math.toRadians(i)) * radius);
            float y = (float) (center.y + Math.sin(Math.toRadians(i)) * radius);
            float z = (float) (center.z + Math.sin(Math.toRadians(i)) * radius);

            consumer.vertex(entry, center.x / 16f, center.y / 16f, center.z / 16f)
                    .color(0, 50, 200, 255)
                    .normal(0, 0, 0);

            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-45));
            matrices.translate(center.x / 16f, center.y / 16f - 0.375f, center.z / 16f + 0.25f);

            MatrixStack.Entry entry2 = matrices.peek();

            consumer.vertex(entry2, x / 16f - center.x / 16f, y / 16f - center.y / 16f, z / 16f - center.z / 16f)
                    .color(0, 105, 255, 10)
                    .normal(0, 0, 0);

            matrices.pop();

            //System.out.println("x: " + x + ", y: " + y + ", z: " + z);
        }

        matrices.pop();
    }

    private static void drawFirstPersonScannerSonar() {
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

    private static void renderThirdPersonFluidHologram(WorldFluidPocketsState.FluidPocket fluidPocket, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        FluidState fluidState = fluidPocket.fluidState();
        List<BlockPos> positions = fluidPocket.fluidPositions();

        int minX = fluidPocket.minX();
        int minY = fluidPocket.minY();
        int minZ = fluidPocket.minZ();
        int maxX = fluidPocket.maxX();
        int maxY = fluidPocket.maxY();
        int maxZ = fluidPocket.maxZ();

        int width = maxX - minX;
        int height = maxY - minY;
        int depth = maxZ - minZ;

        matrices.push();
        matrices.translate(width / 32f, height / 32f, depth / 32f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.translate(-0.125f, (height / 16f) + 0.125f, -0.125f);

        var buffer = new IndustriaDynamicItemRenderer.DrawableVertexConsumer((VertexConsumerProvider.Immediate) vertexConsumers);
        buffer.draw(); // End the previous buffer (usually the player)

        RenderSystem.setShaderColor(1.0F, 2.0F, 5.0F, 0.25F);
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

    private static void renderFirstPersonFluidHologram(WorldFluidPocketsState.FluidPocket fluidPocket, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
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

        // width should always be larger than the depth
        if (width < depth) {
            int temp = width;
            width = depth;
            depth = temp;
        }

        // Calculate the center of the fluid pocket
        int centerX = Math.round(width / 2f);
        int centerY = Math.round(height / 2f);
        int centerZ = Math.round(depth / 2f);

        // Translate to the center of the fluid pocket
        matrices.push();
        matrices.translate(-centerX / 16f, -centerY / 16f, -centerZ / 16f);
        matrices.translate(-(5.5f/16f), height / 16f + 6/16f, 12/16f);

        var buffer = new IndustriaDynamicItemRenderer.DrawableVertexConsumer((VertexConsumerProvider.Immediate) vertexConsumers);
        buffer.draw(); // End the previous buffer (usually the player)

        RenderSystem.setShaderColor(1.0F, 2.0F, 5.0F, 0.25F);
        for (BlockPos position : positions) {
            int relativeX = position.getX() - minX;
            int relativeY = position.getY() - minY;
            int relativeZ = -(position.getZ() - minZ);

            //System.out.println("Relative X: " + relativeX + ", Relative Y: " + relativeY + ", Relative Z: " + relativeZ);

            matrices.push();
            matrices.translate(relativeX / 32f, relativeY / 32f, relativeZ / 32f);
            matrices.scale(0.03125F, 0.03125F, 0.03125F);

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
