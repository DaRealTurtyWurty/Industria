package dev.turtywurty.industria.registry;

import com.mojang.datafixers.util.Either;
import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.util.DrillHeadable;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
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

    public static Stream<Pair<DrillHeadable, Function<Either<BlockEntityRendererFactory.Context, LoadedEntityModels>, Model>>> getModelResolvers() {
        return DRILL_HEADS.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue().modelResolver));
    }

    public static Stream<Pair<DrillHeadable, Identifier>> getTextureLocations() {
        return DRILL_HEADS.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue().textureLocation));
    }

    public static void init() {
    }

    public record DrillHeadClientData(
            Function<Either<BlockEntityRendererFactory.Context, LoadedEntityModels>, Model> modelResolver,
            boolean renderDynamicItem,
            RenderFunction onRender,
            Identifier textureLocation) {
        public static DrillHeadClientData create(Function<Either<BlockEntityRendererFactory.Context, LoadedEntityModels>, Model> modelResolver,
                                                 boolean renderDynamicItem,
                                                 RenderFunction onRender,
                                                 Identifier textureLocation) {
            return new DrillHeadClientData(modelResolver, renderDynamicItem, onRender, textureLocation);
        }

        public static DrillHeadClientData create(Function<Either<BlockEntityRendererFactory.Context, LoadedEntityModels>, Model> modelResolver,
                                                 RenderFunction onRender,
                                                 Identifier textureLocation) {
            return create(modelResolver, true, onRender, textureLocation);
        }

        @FunctionalInterface
        public interface RenderFunction {
            void render(DrillBlockEntity blockEntity, ItemStack headStack, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Model model, VertexConsumer vertexConsumer, int light, int overlay);
        }
    }
}
