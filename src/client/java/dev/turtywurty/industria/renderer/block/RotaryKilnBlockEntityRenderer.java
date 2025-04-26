package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.RotaryKilnControllerBlockEntity;
import dev.turtywurty.industria.model.RotaryKilnModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.List;

public class RotaryKilnBlockEntityRenderer extends IndustriaBlockEntityRenderer<RotaryKilnControllerBlockEntity> {
    private final RotaryKilnModel model;

    public RotaryKilnBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new RotaryKilnModel(context.getLayerModelPart(RotaryKilnModel.LAYER_LOCATION));
    }

    @Override
    protected void onRender(RotaryKilnControllerBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(RotaryKilnModel.TEXTURE_LOCATION));
        this.model.renderSegment(0, matrices, vertexConsumer, light, overlay);

        if (entity.getKilnSegments().isEmpty())
            return;

        this.model.renderSegment(1, matrices, vertexConsumer, light, overlay);

        Direction facing = entity.getCachedState().get(Properties.HORIZONTAL_FACING);
        Direction left = facing.rotateYCounterclockwise();
        Vec3i facingVector = facing.getVector();
        Vec3i leftVector = left.getVector();
        if(true)
            return;

        float rotation = (entity.getWorld().getTime() % 360) * 0.1F;
        for (int index = 2; index < Math.min(entity.getKilnSegments().size(), 15) + 1; index++) {
            ModelPart rotatingSegment = this.model.getRotatingSegment(index);
            rotatingSegment.roll = rotation;
            this.model.renderSegment(index, matrices, vertexConsumer, light, overlay);
            rotatingSegment.roll = 0;

            List<RotaryKilnControllerBlockEntity.InputRecipeEntry> recipes = entity.getRecipes();
            if (index - 2 < recipes.size()) {
                RotaryKilnControllerBlockEntity.InputRecipeEntry recipe = recipes.get(index - 2);
                if (recipe == null)
                    continue;

                float progress = recipe.getProgress() / 100F;

                float baseXOffset = facingVector.getX() * (index - 1);
                if(facingVector.getX() != 0) {
                    baseXOffset += facingVector.getX() * progress;
                }

                float baseYOffset = 0.375f - leftVector.getY() * (index - 1);

                float baseZOffset = facingVector.getZ() * (index - 1);
                if(facingVector.getZ() != 0) {
                    baseZOffset += facingVector.getZ() * progress;
                }

                matrices.push();
                matrices.translate(baseXOffset, baseYOffset, baseZOffset);
                matrices.scale(0.5F, 0.5F, 0.5F);
                matrices.multiply(facing.getRotationQuaternion());

                this.context.getItemRenderer().renderItem(recipe.inputStack(), ItemDisplayContext.NONE, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
                matrices.pop();
            }
        }
    }
}