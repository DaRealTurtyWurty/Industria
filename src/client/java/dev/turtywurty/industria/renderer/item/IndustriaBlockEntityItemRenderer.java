package dev.turtywurty.industria.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public record IndustriaBlockEntityItemRenderer(ModelPart modelPart, Identifier texture)
        implements SpecialModelRenderer<IndustriaBlockEntityItemRenderer.BlockEntityItemRenderData> {
    @Override
    public void submit(@Nullable IndustriaBlockEntityItemRenderer.BlockEntityItemRenderData data, ItemDisplayContext displayContext, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay, boolean glint, int i) {
        if (data == null)
            return;

        ItemStack stack = data.stack();
        if (stack.isEmpty() || this.modelPart == null)
            return;

        RenderType renderLayer = RenderTypes.entityTranslucent(this.texture);
        queue.submitCustomGeometry(matrices, renderLayer, (matricesEntry, vertexConsumer) ->
                this.modelPart.render(matrices, vertexConsumer, light, overlay));
    }

    @Override
    public void getExtents(Consumer<Vector3fc> vertices) {
        var matrices = new PoseStack();
        this.modelPart.getExtentsForGui(matrices, vertices);
    }

    @Override
    public @NotNull BlockEntityItemRenderData extractArgument(ItemStack stack) {
        return new BlockEntityItemRenderData(stack);
    }

    public record Unbaked(ModelLayerLocation modelLayer, Identifier texture) implements net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked {
        private static final Codec<ModelLayerLocation> ENTITY_MODEL_LAYER_CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        Identifier.CODEC.fieldOf("id").forGetter(ModelLayerLocation::model),
                        Codec.STRING.fieldOf("name").forGetter(ModelLayerLocation::layer)
                ).apply(instance, ModelLayerLocation::new));

        public static final MapCodec<Unbaked> CODEC =
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                        ENTITY_MODEL_LAYER_CODEC.fieldOf("model_layer").forGetter(Unbaked::modelLayer),
                        Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::texture)
                ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(BakingContext context) {
            EntityModelSet entityModels = context.entityModelSet();
            return new IndustriaBlockEntityItemRenderer(entityModels.bakeLayer(this.modelLayer), this.texture);
        }
    }

    public record BlockEntityItemRenderData(ItemStack stack) {
    }
}
