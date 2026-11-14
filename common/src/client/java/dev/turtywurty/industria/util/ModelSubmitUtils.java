package dev.turtywurty.industria.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jspecify.annotations.Nullable;

/** Submits a model and its separately queued block-breaking overlay. */
public final class ModelSubmitUtils {
    private ModelSubmitUtils() {
    }

    public static <S> void submitModel(
        OrderedSubmitNodeCollector collector,
        Model<? super S> model,
        S state,
        PoseStack poses,
        RenderType type,
        int light,
        int overlay,
        int outlineColor,
        @Nullable CrumblingOverlay breaking
    ) {
        collector.submitModel(model, state, poses, type, light, overlay, outlineColor);
        if (breaking != null) {
            collector.submitCrumblingOverlay(model, state, poses, type, light, overlay, -1, breaking);
        }
    }
}
