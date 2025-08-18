package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.FluidTankBlockEntity;
import dev.turtywurty.industria.util.InWorldFluidRenderingComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.Items;

public class FluidTankBlockEntityRenderer extends IndustriaBlockEntityRenderer<FluidTankBlockEntity> {
    private final InWorldFluidRenderingComponent fluidRenderingComponent = new InWorldFluidRenderingComponent();

    public FluidTankBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderModel(FluidTankBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

    }

    @Override
    protected void onRender(FluidTankBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(-0.5, -1.5, -0.5);
        this.fluidRenderingComponent.renderFluidTank(entity.getFluidTank(),
                vertexConsumers, matrices,
                light, overlay,
                entity.getWorld(), entity.getPos(),
                3 / 16f, 0, 3 / 16f,
                13 / 16f, 15, 13 / 16f);
        matrices.pop();
    }
}
