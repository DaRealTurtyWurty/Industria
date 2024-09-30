package dev.turtywurty.industria.registry;

import dev.turtywurty.industria.entity.DrillHeadEntity;
import dev.turtywurty.industria.util.DrillHeadable;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class DrillHeadRegistry {
    private static final Map<DrillHeadable, DrillHeadClientData> DRILL_HEADS = new HashMap<>();

    public static void register(DrillHeadable drillHeadable, DrillHeadClientData clientData) {
        DRILL_HEADS.put(drillHeadable, clientData);
    }

    public static DrillHeadClientData getClientData(DrillHeadable drillHeadable) {
        return DRILL_HEADS.get(drillHeadable);
    }

    public static Stream<Pair<DrillHeadable, Function<EntityModelLoader, EntityModel<DrillHeadEntity>>>> getModelResolvers() {
        return DRILL_HEADS.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue().modelResolver));
    }

    public static Stream<Pair<DrillHeadable, Identifier>> getTextureLocations() {
        return DRILL_HEADS.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue().textureLocation));
    }

    public static void init() {
    }

    public record DrillHeadClientData(Function<EntityModelLoader, EntityModel<DrillHeadEntity>> modelResolver,
                                      boolean renderDynamicItem,
                                      EntityRenderFunction onRender,
                                      Identifier textureLocation) {

        public static DrillHeadClientData create(Function<EntityModelLoader, EntityModel<DrillHeadEntity>> modelResolver,
                                                 boolean renderDynamicItem,
                                                 EntityRenderFunction onRender,
                                                 Identifier textureLocation) {
            return new DrillHeadClientData(modelResolver, renderDynamicItem, onRender, textureLocation);
        }

        public static DrillHeadClientData create(Function<EntityModelLoader, EntityModel<DrillHeadEntity>> modelResolver,
                                                 EntityRenderFunction onRender,
                                                 Identifier textureLocation) {
            return create(modelResolver, true, onRender, textureLocation);
        }

        @FunctionalInterface
        public interface EntityRenderFunction {
            void render(DrillHeadEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);
        }
    }

}
