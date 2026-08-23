package dev.turtywurty.industria.init;

import com.google.common.collect.ImmutableMap;
import dev.turtywurty.industria.conveyor.block.impl.*;
import dev.turtywurty.industria.model.conveyor.anchor.*;
import dev.turtywurty.industria.renderer.world.ConveyorNetworkLevelRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.Function;

public final class ModConveyorAnchorProviders {
    private ModConveyorAnchorProviders() {
    }

    private static final Map<Block, Function<BlockState, Map<String, Model<?>>>> ANCHOR_PROVIDERS = new Object2ObjectOpenHashMap<>();

    public static void registerAnchorProvider(Block block, Function<BlockState, Map<String, Model<?>>> provider) {
        ANCHOR_PROVIDERS.put(block, provider);
    }

    public static Map<Block, Function<BlockState, Map<String, Model<?>>>> getAnchorProviders() {
        return ImmutableMap.copyOf(ANCHOR_PROVIDERS);
    }

    public static void init() {
        registerAnchorProvider(ModBlocks.CONVEYOR.get(), blockState -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE, switch (blockState.getValue(BasicConveyorBlock.SHAPE)) {
                case STRAIGHT ->
                        new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION));
                case UP ->
                        new VerticalUpConveyorAnchorPositionsModel(entityModels.bakeLayer(VerticalUpConveyorAnchorPositionsModel.LAYER_LOCATION));
                case DOWN ->
                        new VerticalDownConveyorAnchorPositionsModel(entityModels.bakeLayer(VerticalDownConveyorAnchorPositionsModel.LAYER_LOCATION));
                case TURN_LEFT, TURN_RIGHT ->
                        new CornerTurnConveyorAnchorPositionsModel(entityModels.bakeLayer(CornerTurnConveyorAnchorPositionsModel.LAYER_LOCATION));
            });
        });

        registerAnchorProvider(ModBlocks.SPLITTER_CONVEYOR.get(), _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(
                    SplitterConveyorBlock.LEFT_OUTPUT_ID,
                    new SplitterConveyorAnchorPositionsModel(entityModels.bakeLayer(SplitterConveyorAnchorPositionsModel.LEFT_LAYER_LOCATION)),
                    SplitterConveyorBlock.RIGHT_OUTPUT_ID,
                    new SplitterConveyorAnchorPositionsModel(entityModels.bakeLayer(SplitterConveyorAnchorPositionsModel.RIGHT_LAYER_LOCATION))
            );
        });

        registerAnchorProvider(ModBlocks.ALTERNATOR_CONVEYOR.get(), _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(
                    AlternatorConveyorBlock.LEFT_OUTPUT_ID,
                    new SplitterConveyorAnchorPositionsModel(entityModels.bakeLayer(SplitterConveyorAnchorPositionsModel.LEFT_LAYER_LOCATION)),
                    AlternatorConveyorBlock.RIGHT_OUTPUT_ID,
                    new SplitterConveyorAnchorPositionsModel(entityModels.bakeLayer(SplitterConveyorAnchorPositionsModel.RIGHT_LAYER_LOCATION))
            );
        });

        registerAnchorProvider(ModBlocks.MERGER_CONVEYOR.get(), _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(
                    MergerConveyorBlock.LEFT_INPUT_ID,
                    new MergerConveyorAnchorPositionsModel(entityModels.bakeLayer(MergerConveyorAnchorPositionsModel.LEFT_LAYER_LOCATION)),
                    MergerConveyorBlock.RIGHT_INPUT_ID,
                    new MergerConveyorAnchorPositionsModel(entityModels.bakeLayer(MergerConveyorAnchorPositionsModel.RIGHT_LAYER_LOCATION))
            );
        });

        registerAnchorProvider(ModBlocks.FEEDER_CONVEYOR.get(), _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE,
                    new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION)));
        });

        registerAnchorProvider(ModBlocks.HATCH_CONVEYOR.get(), _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE,
                    new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION)));
        });

        registerAnchorProvider(ModBlocks.SIDE_INJECTOR_CONVEYOR.get(), _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE,
                    new SideInjectorConveyorAnchorModel(entityModels.bakeLayer(SideInjectorConveyorAnchorModel.LAYER_LOCATION)));
        });

        registerAnchorProvider(ModBlocks.FILTER_CONVEYOR.get(), _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(
                    FilterConveyorBlock.FORWARD_OUTPUT_ID,
                    new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION)),
                    FilterConveyorBlock.RIGHT_OUTPUT_ID,
                    new SplitterConveyorAnchorPositionsModel(entityModels.bakeLayer(SplitterConveyorAnchorPositionsModel.RIGHT_LAYER_LOCATION))
            );
        });

        registerAnchorProvider(ModBlocks.MAGNETIC_CONVEYOR.get(), _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE,
                    new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION)));
        });

        registerAnchorProvider(ModBlocks.DROP_CHUTE_CONVEYOR.get(), _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE,
                    new DropChuteConveyorAnchorPositionsModel(entityModels.bakeLayer(DropChuteConveyorAnchorPositionsModel.LAYER_LOCATION)));
        });

        registerAnchorProvider(ModBlocks.DETECTOR_CONVEYOR.get(), _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE,
                    new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION)));
        });

        registerAnchorProvider(ModBlocks.COUNT_CONVEYOR.get(), _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE,
                    new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION)));
        });

        registerAnchorProvider(ModBlocks.DELAY_CONVEYOR.get(), _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE,
                    new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION)));
        });
    }
}
