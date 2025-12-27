package dev.turtywurty.industria.renderer.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.registry.DrillHeadRegistry;
import dev.turtywurty.industria.state.DrillRenderState;
import dev.turtywurty.industria.util.DrillHeadable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DrillHeadItemRenderer implements SpecialModelRenderer<DrillRenderState> {
    public static final DrillHeadItemRenderer INSTANCE = new DrillHeadItemRenderer();

    public final Map<DrillHeadable, Model<?>> drillHeadModels = new HashMap<>();
    public final Map<DrillHeadable, Identifier> drillHeadTextures = new HashMap<>();

    @Override
    public void render(DrillRenderState state, ItemDisplayContext displayContext, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, boolean glint, int outlineColor) {
        if (state == null)
            return;

        if (state.drillHeadItemStack.getItem() instanceof DrillHeadable drillHeadable) {
            LoadedEntityModels loadedEntityModels = MinecraftClient.getInstance().getLoadedEntityModels();
            DrillHeadRegistry.DrillHeadClientData clientData = DrillHeadRegistry.getClientData(drillHeadable);
            if (clientData != null && clientData.renderDynamicItem()) {
                Model<?> model = this.drillHeadModels.computeIfAbsent(drillHeadable, ignored -> clientData.modelResolver().apply(Either.right(loadedEntityModels)));
                Identifier textureLocation = this.drillHeadTextures.computeIfAbsent(drillHeadable, ignored -> clientData.textureLocation());
                matrices.push();
                matrices.translate(0.5f, 0.75f, 0.5f);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                matrices.scale(0.5F, 0.5F, 0.5F);
                RenderLayer renderLayer = model.getLayer(textureLocation);
                clientData.onRender().render(state, matrices, queue, model, renderLayer, light, overlay);
                matrices.pop();
            }
        }
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        MatrixStack matrices = new MatrixStack();
        this.drillHeadModels.values().forEach(model -> model.getParts().forEach(modelPart -> modelPart.collectVertices(matrices, vertices)));
    }

    @Override
    public @Nullable DrillRenderState getData(ItemStack stack) {
        var state = new DrillRenderState();
        state.drillHeadItemStack = stack;
        return state;
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new DrillHeadItemRenderer.Unbaked());

        @Override
        public SpecialModelRenderer<?> bake(BakeContext context) {
            return new DrillHeadItemRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> getCodec() {
            return CODEC;
        }
    }
}
