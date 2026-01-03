package dev.turtywurty.industria.renderer.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public record IndustriaBlockEntityItemRenderer(ModelPart modelPart, Identifier texture)
        implements SpecialModelRenderer<IndustriaBlockEntityItemRenderer.BlockEntityItemRenderData> {
    @Override
    public void render(@Nullable IndustriaBlockEntityItemRenderer.BlockEntityItemRenderData data, ItemDisplayContext displayContext, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, boolean glint, int i) {
        if (data == null)
            return;

        ItemStack stack = data.stack();
        if (stack.isEmpty() || this.modelPart == null)
            return;

        RenderLayer renderLayer = RenderLayers.entityTranslucent(this.texture);
        queue.submitCustom(matrices, renderLayer, (matricesEntry, vertexConsumer) ->
                this.modelPart.render(matrices, vertexConsumer, light, overlay));
    }

    @Override
    public void collectVertices(Consumer<Vector3fc> vertices) {
        var matrices = new MatrixStack();
        this.modelPart.collectVertices(matrices, vertices);
    }

    @Override
    public @NotNull BlockEntityItemRenderData getData(ItemStack stack) {
        return new BlockEntityItemRenderData(stack);
    }

    public record Unbaked(EntityModelLayer modelLayer, Identifier texture) implements SpecialModelRenderer.Unbaked {
        private static final Codec<EntityModelLayer> ENTITY_MODEL_LAYER_CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        Identifier.CODEC.fieldOf("id").forGetter(EntityModelLayer::id),
                        Codec.STRING.fieldOf("name").forGetter(EntityModelLayer::name)
                ).apply(instance, EntityModelLayer::new));

        public static final MapCodec<Unbaked> CODEC =
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                        ENTITY_MODEL_LAYER_CODEC.fieldOf("model_layer").forGetter(Unbaked::modelLayer),
                        Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::texture)
                ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(BakeContext context) {
            LoadedEntityModels entityModels = context.entityModelSet();
            return new IndustriaBlockEntityItemRenderer(entityModels.getModelPart(this.modelLayer), this.texture);
        }
    }

    public record BlockEntityItemRenderData(ItemStack stack) {
    }
}
