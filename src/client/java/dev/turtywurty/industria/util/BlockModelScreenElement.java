package dev.turtywurty.industria.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import org.jetbrains.annotations.Nullable;

public class BlockModelScreenElement extends DefaultScreenElement {
    protected BakedModel model;
    protected BlockState blockState;

    public BlockModelScreenElement(BakedModel model, @Nullable BlockState blockState) {
        this.model = model;
        this.blockState = blockState == null ? Blocks.AIR.getDefaultState() : blockState;
    }

    @Override
    public void render(DrawContext context, double mouseX, double mouseY, float partialTicks) {
        MatrixStack matrixStack = context.getMatrices();
        prepareMatrix(matrixStack);

        MinecraftClient client = MinecraftClient.getInstance();
        BlockRenderManager blockRenderManager = client.getBlockRenderManager();
        VertexConsumerProvider.Immediate provider = client.getBufferBuilders().getEntityVertexConsumers();
        RenderLayer renderLayer = this.blockState.getBlock() == Blocks.AIR ?
                TexturedRenderLayers.getEntityTranslucentCull() :
                RenderLayers.getEntityBlockLayer(this.blockState, true);
        VertexConsumer vertexConsumer = provider.getBuffer(renderLayer);

        transformMatrix(matrixStack);

        RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        renderModel(blockRenderManager, provider, renderLayer, vertexConsumer, matrixStack);

        cleanupMatrix(matrixStack);
    }

    protected void renderModel(BlockRenderManager blockRenderManager, VertexConsumerProvider.Immediate provider, RenderLayer renderLayer, VertexConsumer vertexConsumer, MatrixStack matrixStack) {
        int color = MinecraftClient.getInstance()
                .getBlockColors()
                .getColor(this.blockState, null, null, 0);

        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        blockRenderManager.getModelRenderer()
                .render(matrixStack.peek(), vertexConsumer, this.blockState, this.model, red, green, blue, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
        provider.draw();
    }
}
