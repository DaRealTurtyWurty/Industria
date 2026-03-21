package dev.turtywurty.industria.init;

import com.google.common.collect.ImmutableMap;
import dev.turtywurty.industria.conveyor.block.impl.BasicConveyorBlock;
import dev.turtywurty.industria.conveyor.block.impl.FilterConveyorBlock;
import dev.turtywurty.industria.conveyor.block.impl.MergerConveyorBlock;
import dev.turtywurty.industria.conveyor.block.impl.SplitterConveyorBlock;
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

public final class ConveyorAnchorProviderInit {
    private ConveyorAnchorProviderInit() {
    }

    private static final Map<Block, Function<BlockState, Map<String, Model<?>>>> ANCHOR_PROVIDERS = new Object2ObjectOpenHashMap<>();

    public static void registerAnchorProvider(Block block, Function<BlockState, Map<String, Model<?>>> provider) {
        ANCHOR_PROVIDERS.put(block, provider);
    }

    public static Map<Block, Function<BlockState, Map<String, Model<?>>>> getAnchorProviders() {
        return ImmutableMap.copyOf(ANCHOR_PROVIDERS);
    }

    public static void init() {
        registerAnchorProvider(BlockInit.CONVEYOR, blockState -> {
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

        registerAnchorProvider(BlockInit.SPLITTER_CONVEYOR, _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(
                    SplitterConveyorBlock.LEFT_OUTPUT_ID,
                    new SplitterConveyorAnchorPositionsModel(entityModels.bakeLayer(SplitterConveyorAnchorPositionsModel.LEFT_LAYER_LOCATION)),
                    SplitterConveyorBlock.RIGHT_OUTPUT_ID,
                    new SplitterConveyorAnchorPositionsModel(entityModels.bakeLayer(SplitterConveyorAnchorPositionsModel.RIGHT_LAYER_LOCATION))
            );
        });

        registerAnchorProvider(BlockInit.MERGER_CONVEYOR, _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(
                    MergerConveyorBlock.LEFT_INPUT_ID,
                    new MergerConveyorAnchorPositionsModel(entityModels.bakeLayer(MergerConveyorAnchorPositionsModel.LEFT_LAYER_LOCATION)),
                    MergerConveyorBlock.RIGHT_INPUT_ID,
                    new MergerConveyorAnchorPositionsModel(entityModels.bakeLayer(MergerConveyorAnchorPositionsModel.RIGHT_LAYER_LOCATION))
            );
        });

        registerAnchorProvider(BlockInit.FEEDER_CONVEYOR, _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE,
                    new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION)));
        });

        registerAnchorProvider(BlockInit.HATCH_CONVEYOR, _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE,
                    new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION)));
        });

        registerAnchorProvider(BlockInit.SIDE_INJECTOR_CONVEYOR, _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE,
                    new SideInjectorConveyorAnchorModel(entityModels.bakeLayer(SideInjectorConveyorAnchorModel.LAYER_LOCATION)));
        });

        registerAnchorProvider(BlockInit.FILTER_CONVEYOR, _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(
                    FilterConveyorBlock.FORWARD_OUTPUT_ID,
                    new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION)),
                    FilterConveyorBlock.RIGHT_OUTPUT_ID,
                    new SplitterConveyorAnchorPositionsModel(entityModels.bakeLayer(SplitterConveyorAnchorPositionsModel.RIGHT_LAYER_LOCATION))
            );
        });

        registerAnchorProvider(BlockInit.MAGNETIC_CONVEYOR, _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE,
                    new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION)));
        });

        registerAnchorProvider(BlockInit.DROP_CHUTE_CONVEYOR, _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE,
                    new DropChuteConveyorAnchorPositionsModel(entityModels.bakeLayer(DropChuteConveyorAnchorPositionsModel.LAYER_LOCATION)));
        });

        registerAnchorProvider(BlockInit.DETECTOR_CONVEYOR, _ -> {
            EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
            return Map.of(ConveyorNetworkLevelRenderer.DEFAULT_ANCHOR_ROUTE,
                    new StraightConveyorAnchorPositionsModel(entityModels.bakeLayer(StraightConveyorAnchorPositionsModel.LAYER_LOCATION)));
        });
    }
}
