package dev.turtywurty.industria.renderer.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.registry.DrillHeadRegistry;
import dev.turtywurty.industria.util.DrillHeadable;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DrillHeadItemRenderer implements SpecialModelRenderer<DrillHeadItemRenderer.DrillHeadItemRenderData>, IdentifiableResourceReloadListener {
    private final Map<DrillHeadable, Model> drillHeadModels = new HashMap<>();
    private final Map<DrillHeadable, Identifier> drillHeadTextures = new HashMap<>();

    @Override
    public void render(DrillHeadItemRenderData data, ItemDisplayContext displayContext, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, boolean glint) {
        if(data == null)
            return;

        ItemStack stack = data.stack();
        if (stack.getItem() instanceof DrillHeadable drillHeadable) {
            LoadedEntityModels loadedEntityModels = MinecraftClient.getInstance().getLoadedEntityModels();
            DrillHeadRegistry.DrillHeadClientData clientData = DrillHeadRegistry.getClientData(drillHeadable);
            if (clientData != null && clientData.renderDynamicItem()) {
                Model model = this.drillHeadModels.computeIfAbsent(drillHeadable, ignored -> clientData.modelResolver().apply(Either.right(loadedEntityModels)));
                Identifier textureLocation = this.drillHeadTextures.computeIfAbsent(drillHeadable, ignored -> clientData.textureLocation());
                matrices.push();
                matrices.translate(0.5f, 0.75f, 0.5f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                matrices.scale(0.5F, 0.5F, 0.5F);
                model.render(matrices, vertexConsumers.getBuffer(model.getLayer(textureLocation)), light, overlay);
                matrices.pop();
            }
        }
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        MatrixStack matrices = new MatrixStack();
        this.drillHeadModels.values().forEach(model -> model.getRootPart().collectVertices(matrices, vertices));
    }

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            this.drillHeadModels.clear();
            this.drillHeadTextures.clear();
        }, applyExecutor);
    }

    @Override
    public Identifier getFabricId() {
        return Industria.id("drill_head_item_renderer");
    }

    @Override
    public @Nullable DrillHeadItemRenderData getData(ItemStack stack) {
        return new DrillHeadItemRenderData(stack);
    }

    public record DrillHeadItemRenderData(ItemStack stack) {
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new DrillHeadItemRenderer.Unbaked());

        @Override
        public SpecialModelRenderer<?> bake(LoadedEntityModels entityModels) {
            return new DrillHeadItemRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> getCodec() {
            return CODEC;
        }
    }
}
