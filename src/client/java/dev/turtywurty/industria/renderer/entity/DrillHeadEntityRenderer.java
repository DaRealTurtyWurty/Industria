package dev.turtywurty.industria.renderer.entity;

import com.google.common.collect.ImmutableMap;
import dev.turtywurty.industria.entity.DrillHeadEntity;
import dev.turtywurty.industria.registry.DrillHeadRegistry;
import dev.turtywurty.industria.util.DrillHeadable;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.Map;

public class DrillHeadEntityRenderer extends EntityRenderer<DrillHeadEntity> {
    private final Map<DrillHeadable, EntityModel<DrillHeadEntity>> models;
    private final Map<DrillHeadable, Identifier> textures;

    public DrillHeadEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);

        EntityModelLoader loader = ctx.getModelLoader();

        this.models = DrillHeadRegistry.getModelResolvers().map(pair ->
                        Map.entry(pair.getLeft(), pair.getRight().apply(loader)))
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

        this.textures = DrillHeadRegistry.getTextureLocations()
                .collect(ImmutableMap.toImmutableMap(Pair::getLeft, Pair::getRight));
    }

    @Override
    public void render(DrillHeadEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        ItemStack stack = entity.getBlockEntity().getDrillStack();
        if (stack.isEmpty())
            return;

        matrices.push();

        DrillHeadable drillHeadable = (DrillHeadable) stack.getItem();
        EntityModel<DrillHeadEntity> model = this.models.get(drillHeadable);
        model.setAngles(entity, 0, 0, 0, 0, 0);
        model.render(matrices, vertexConsumers.getBuffer(model.getLayer(getTexture(entity))), light, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }

    @Override
    public Identifier getTexture(DrillHeadEntity entity) {
        ItemStack stack = entity.getBlockEntity().getDrillStack();
        if (stack.isEmpty())
            return null;

        DrillHeadable drillHeadable = (DrillHeadable) stack.getItem();
        return this.textures.get(drillHeadable);
    }
}
