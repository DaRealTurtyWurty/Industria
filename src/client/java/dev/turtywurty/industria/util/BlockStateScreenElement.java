package dev.turtywurty.industria.util;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

public class BlockStateScreenElement extends BlockModelScreenElement {
    public BlockStateScreenElement(@NotNull BlockState blockState) {
        super(MinecraftClient.getInstance().getBlockRenderManager().getModel(blockState), blockState);
    }

    @Override
    protected void renderModel(BlockRenderManager blockRenderManager, VertexConsumerProvider.Immediate provider, RenderLayer renderLayer, VertexConsumer vertexConsumer, MatrixStack matrixStack) {
        if(this.blockState.getBlock() instanceof AbstractFireBlock) {
            DiffuseLighting.disableGuiDepthLighting();
            super.renderModel(blockRenderManager, provider, renderLayer, provider.getBuffer(RenderLayers.getEntityBlockLayer(this.blockState, false)), matrixStack);
            DiffuseLighting.enableGuiDepthLighting();
            return;
        }

        if(this.blockState.getFluidState().isEmpty()) {
            super.renderModel(blockRenderManager, provider, renderLayer, vertexConsumer, matrixStack);
            return;
        }

        IndustriaFluidRenderer.renderFluidBox(this.blockState.getFluidState(), 0, 0, 0, 1, 1, 1, provider, matrixStack, LightmapTextureManager.MAX_LIGHT_COORDINATE, true);
    }
}
