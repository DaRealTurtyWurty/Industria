package dev.turtywurty.industria.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public record IndustriaBlockEntityItemRenderer(ModelPart modelPart, Identifier texture)
        implements SpecialModelRenderer<IndustriaBlockEntityItemRenderer.BlockEntityItemRenderData> {
    @Override
    public void submit(@Nullable IndustriaBlockEntityItemRenderer.BlockEntityItemRenderData data, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay, boolean glint, int i) {
        if (data == null)
            return;

        ItemStack stack = data.stack();
        if (stack.isEmpty() || this.modelPart == null)
            return;

        RenderType renderLayer = RenderTypes.entityTranslucent(this.texture);
        matrices.pushPose();
        setupTransformations(matrices);
        queue.submitModelPart(this.modelPart, matrices, renderLayer, light, overlay,
                null, false, glint, -1, null, i);
        matrices.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> vertices) {
        var matrices = new PoseStack();
        setupTransformations(matrices);
        this.modelPart.getExtentsForGui(matrices, vertices);
    }

    private static void setupTransformations(PoseStack matrices) {
        matrices.translate(0.5F, 1.5F, 0.5F);
        matrices.mulPose(Axis.XP.rotationDegrees(180.0F));
        matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
    }

    @Override
    public @NotNull BlockEntityItemRenderData extractArgument(ItemStack stack) {
        return new BlockEntityItemRenderData(stack);
    }

    public record Unbaked(ModelLayerLocation modelLayer, Identifier texture)
            implements SpecialModelRenderer.Unbaked<IndustriaBlockEntityItemRenderer.BlockEntityItemRenderData> {
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
        public SpecialModelRenderer<IndustriaBlockEntityItemRenderer.BlockEntityItemRenderData> bake(BakingContext context) {
            EntityModelSet entityModels = context.entityModelSet();
            return new IndustriaBlockEntityItemRenderer(entityModels.bakeLayer(this.modelLayer), this.texture);
        }
    }

    public record BlockEntityItemRenderData(ItemStack stack) {
    }
}
