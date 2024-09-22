package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class DrillBlockEntityRenderer implements BlockEntityRenderer<DrillBlockEntity> {
    private final BlockEntityRendererFactory.Context context;

    public DrillBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public void render(DrillBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

    }
}
