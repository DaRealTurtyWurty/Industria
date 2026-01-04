package dev.turtywurty.industria.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.registry.DrillHeadRegistry;
import dev.turtywurty.industria.state.DrillRenderState;
import dev.turtywurty.industria.util.DrillHeadable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DrillHeadItemRenderer implements SpecialModelRenderer<DrillRenderState> {
    public static final DrillHeadItemRenderer INSTANCE = new DrillHeadItemRenderer();

    public final Map<DrillHeadable, Model<?>> drillHeadModels = new HashMap<>();
    public final Map<DrillHeadable, Identifier> drillHeadTextures = new HashMap<>();

    @Override
    public void submit(DrillRenderState state, ItemDisplayContext displayContext, PoseStack matrices, SubmitNodeCollector queue, int light, int overlay, boolean glint, int outlineColor) {
        if (state == null)
            return;

        if (state.drillHeadItemStack.getItem() instanceof DrillHeadable drillHeadable) {
            EntityModelSet loadedEntityModels = Minecraft.getInstance().getEntityModels();
            DrillHeadRegistry.DrillHeadClientData clientData = DrillHeadRegistry.getClientData(drillHeadable);
            if (clientData != null && clientData.renderDynamicItem()) {
                Model<?> model = this.drillHeadModels.computeIfAbsent(drillHeadable, ignored -> clientData.modelResolver().apply(Either.right(loadedEntityModels)));
                Identifier textureLocation = this.drillHeadTextures.computeIfAbsent(drillHeadable, ignored -> clientData.textureLocation());
                matrices.pushPose();
                matrices.translate(0.5f, 0.75f, 0.5f);
                matrices.mulPose(Axis.XP.rotationDegrees(180));
                matrices.scale(0.5F, 0.5F, 0.5F);
                RenderType renderLayer = model.renderType(textureLocation);
                clientData.onRender().render(state, matrices, queue, model, renderLayer, light, overlay);
                matrices.popPose();
            }
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> vertices) {
        var matrices = new PoseStack();
        this.drillHeadModels.values().forEach(model -> model.allParts().forEach(modelPart -> modelPart.getExtentsForGui(matrices, vertices)));
    }

    @Override
    public @Nullable DrillRenderState extractArgument(ItemStack stack) {
        var state = new DrillRenderState();
        state.drillHeadItemStack = stack;
        return state;
    }

    public record Unbaked() implements net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new DrillHeadItemRenderer.Unbaked());

        @Override
        public SpecialModelRenderer<?> bake(BakingContext context) {
            return new DrillHeadItemRenderer();
        }

        @Override
        public MapCodec<? extends net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked> type() {
            return CODEC;
        }
    }
}
