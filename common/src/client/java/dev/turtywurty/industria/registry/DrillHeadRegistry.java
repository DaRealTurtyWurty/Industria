package dev.turtywurty.industria.registry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import dev.turtywurty.industria.state.DrillRenderState;
import dev.turtywurty.industria.util.DrillHeadable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public class DrillHeadRegistry {
    private static final Map<DrillHeadable, DrillHeadClientData> DRILL_HEADS = new HashMap<>();

    public static void register(DrillHeadable drillHeadable, DrillHeadClientData clientData) {
        DRILL_HEADS.put(drillHeadable, clientData);
    }

    public static DrillHeadClientData getClientData(DrillHeadable drillHeadable) {
        return DRILL_HEADS.get(drillHeadable);
    }

    public static Stream<Pair<DrillHeadable, Function<Either<BlockEntityRendererProvider.Context, EntityModelSet>, Model<?>>>> getModelResolvers() {
        return DRILL_HEADS.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue().modelResolver));
    }

    public static Stream<Pair<DrillHeadable, Identifier>> getTextureLocations() {
        return DRILL_HEADS.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue().textureLocation));
    }

    public static void init() {
    }

    public record DrillHeadClientData(
            Function<Either<BlockEntityRendererProvider.Context, EntityModelSet>, Model<?>> modelResolver,
            boolean renderDynamicItem,
            RenderFunction onRender,
            Identifier textureLocation) {
        public static DrillHeadClientData create(Function<Either<BlockEntityRendererProvider.Context, EntityModelSet>, Model<?>> modelResolver,
                                                 boolean renderDynamicItem,
                                                 RenderFunction onRender,
                                                 Identifier textureLocation) {
            return new DrillHeadClientData(modelResolver, renderDynamicItem, onRender, textureLocation);
        }

        public static DrillHeadClientData create(Function<Either<BlockEntityRendererProvider.Context, EntityModelSet>, Model<?>> modelResolver,
                                                 RenderFunction onRender,
                                                 Identifier textureLocation) {
            return create(modelResolver, true, onRender, textureLocation);
        }

        @FunctionalInterface
        public interface RenderFunction {
            void render(DrillRenderState state, PoseStack matrices, SubmitNodeCollector queue, Model<?> model, RenderType renderLayer, int light, int overlay);
        }
    }
}
