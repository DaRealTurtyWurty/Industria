package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.BatteryBlock;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.datagen.builder.BuiltinEntityModelBuilder;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.FluidInit;
import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.renderer.item.DrillHeadItemRenderer;
import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.client.data.*;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.client.render.model.json.MultipartModelConditionBuilder;
import net.minecraft.client.render.model.json.WeightedVariant;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.AxisRotation;

public class IndustriaModelProvider extends FabricModelProvider {
    public IndustriaModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            WoodSetDatagen.generateBlockStateAndModels(woodSet, blockStateModelGenerator);
        }

        blockStateModelGenerator.registerCooker(BlockInit.ALLOY_FURNACE, TexturedModel.ORIENTABLE);
        blockStateModelGenerator.registerCooker(BlockInit.THERMAL_GENERATOR, TexturedModel.ORIENTABLE);
        createBattery(blockStateModelGenerator, BlockInit.BASIC_BATTERY);
        createBattery(blockStateModelGenerator, BlockInit.ADVANCED_BATTERY);
        createBattery(blockStateModelGenerator, BlockInit.ELITE_BATTERY);
        createBattery(blockStateModelGenerator, BlockInit.ULTIMATE_BATTERY);
        createBattery(blockStateModelGenerator, BlockInit.CREATIVE_BATTERY);
        blockStateModelGenerator.registerCooker(BlockInit.COMBUSTION_GENERATOR, TexturedModel.ORIENTABLE);
        blockStateModelGenerator.registerNorthDefaultHorizontalRotation(BlockInit.SOLAR_PANEL);
        blockStateModelGenerator.registerParentedItemModel(BlockInit.SOLAR_PANEL, Industria.id("block/solar_panel"));
        blockStateModelGenerator.registerSimpleState(FluidInit.CRUDE_OIL.block());
        blockStateModelGenerator.registerSimpleState(FluidInit.DIRTY_SODIUM_ALUMINATE.block());
        blockStateModelGenerator.registerSimpleState(FluidInit.SODIUM_ALUMINATE.block());
        blockStateModelGenerator.registerSimpleState(FluidInit.MOLTEN_ALUMINIUM.block());
        blockStateModelGenerator.registerSimpleState(FluidInit.MOLTEN_CRYOLITE.block());
        blockStateModelGenerator.registerSimpleState(BlockInit.DRILL_TUBE);
        blockStateModelGenerator.registerParentedItemModel(BlockInit.DRILL_TUBE, Industria.id("block/drill_tube"));
        blockStateModelGenerator.registerCooker(BlockInit.ELECTRIC_FURNACE, TexturedModel.ORIENTABLE);
        blockStateModelGenerator.registerNorthDefaultHorizontalRotation(BlockInit.FRACTIONAL_DISTILLATION_CONTROLLER);
        blockStateModelGenerator.registerParentedItemModel(BlockInit.FRACTIONAL_DISTILLATION_CONTROLLER, Industria.id("block/fractional_distillation_controller"));
        blockStateModelGenerator.registerNorthDefaultHorizontalRotation(BlockInit.FRACTIONAL_DISTILLATION_TOWER);
        blockStateModelGenerator.registerParentedItemModel(BlockInit.FRACTIONAL_DISTILLATION_TOWER, Industria.id("block/fractional_distillation_tower"));
        blockStateModelGenerator.registerSimpleState(BlockInit.INDUCTION_HEATER);
        blockStateModelGenerator.registerSimpleState(BlockInit.FLUID_PUMP);
        blockStateModelGenerator.registerSimpleState(BlockInit.FLUID_TANK);

        registerPipe(blockStateModelGenerator, BlockInit.CABLE, "cable");
        registerPipe(blockStateModelGenerator, BlockInit.FLUID_PIPE, "fluid_pipe");
        registerPipe(blockStateModelGenerator, BlockInit.SLURRY_PIPE, "slurry_pipe");
        registerPipe(blockStateModelGenerator, BlockInit.HEAT_PIPE, "heat_pipe");

        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.BAUXITE_ORE);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.TIN_ORE);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.ZINC_ORE);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.DEEPSLATE_BAUXITE_ORE);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.DEEPSLATE_TIN_ORE);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.DEEPSLATE_ZINC_ORE);

        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.ALUMINIUM_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.TIN_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.ZINC_BLOCK);

        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.RAW_BAUXITE_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.RAW_TIN_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.RAW_ZINC_BLOCK);
    }

    private static void registerPipe(BlockStateModelGenerator blockStateModelGenerator, Block block, String name) {
        BlockModelDefinitionCreator pipeSupplier = createPipeBlockModelDefinitionCreator(block, name);
        blockStateModelGenerator.blockStateCollector.accept(pipeSupplier);
    }

    public static WeightedVariant createWeightedVariant(Identifier id, ModelVariant.ModelState modelState) {
        return new WeightedVariant(Pool.of(new ModelVariant(id, modelState)));
    }

    public static WeightedVariant createWeightedVariant(Identifier id) {
        return new WeightedVariant(Pool.of(new ModelVariant(id)));
    }

    private static BlockModelDefinitionCreator createPipeBlockModelDefinitionCreator(Block block, String name) {
        Identifier blockModelId = Industria.id("block/" + name);
        Identifier connectedBlockModelId = Industria.id("block/" + name + "_connected");
        return MultipartBlockModelDefinitionCreator.create(block)
                .with(createWeightedVariant(Industria.id("block/" + name + "_dot")))
                .with(new MultipartModelConditionBuilder().put(PipeBlock.NORTH, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(blockModelId))
                .with(new MultipartModelConditionBuilder().put(PipeBlock.EAST, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(blockModelId,
                                ModelVariant.ModelState.DEFAULT
                                        .setRotationY(AxisRotation.R90)))
                .with(new MultipartModelConditionBuilder().put(PipeBlock.SOUTH, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(blockModelId,
                                ModelVariant.ModelState.DEFAULT
                                        .setRotationY(AxisRotation.R180)))
                .with(new MultipartModelConditionBuilder().put(PipeBlock.WEST, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(blockModelId,
                                ModelVariant.ModelState.DEFAULT
                                        .setRotationY(AxisRotation.R270)))
                .with(new MultipartModelConditionBuilder().put(PipeBlock.UP, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(blockModelId,
                                ModelVariant.ModelState.DEFAULT
                                        .setRotationX(AxisRotation.R270)))
                .with(new MultipartModelConditionBuilder().put(PipeBlock.DOWN, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(blockModelId,
                                ModelVariant.ModelState.DEFAULT
                                        .setRotationX(AxisRotation.R90)))
                .with(new MultipartModelConditionBuilder().put(PipeBlock.NORTH, PipeBlock.ConnectorType.BLOCK),
                        createWeightedVariant(connectedBlockModelId, ModelVariant.ModelState.DEFAULT))
                .with(new MultipartModelConditionBuilder().put(PipeBlock.EAST, PipeBlock.ConnectorType.BLOCK),
                        createWeightedVariant(connectedBlockModelId, ModelVariant.ModelState.DEFAULT
                                .setRotationY(AxisRotation.R90)))
                .with(new MultipartModelConditionBuilder().put(PipeBlock.SOUTH, PipeBlock.ConnectorType.BLOCK),
                        createWeightedVariant(connectedBlockModelId, ModelVariant.ModelState.DEFAULT
                                .setRotationY(AxisRotation.R180)))
                .with(new MultipartModelConditionBuilder().put(PipeBlock.WEST, PipeBlock.ConnectorType.BLOCK),
                        createWeightedVariant(connectedBlockModelId, ModelVariant.ModelState.DEFAULT
                                .setRotationY(AxisRotation.R270)))
                .with(new MultipartModelConditionBuilder().put(PipeBlock.UP, PipeBlock.ConnectorType.BLOCK),
                        createWeightedVariant(connectedBlockModelId, ModelVariant.ModelState.DEFAULT
                                .setRotationX(AxisRotation.R270)))
                .with(new MultipartModelConditionBuilder().put(PipeBlock.DOWN, PipeBlock.ConnectorType.BLOCK),
                        createWeightedVariant(connectedBlockModelId, ModelVariant.ModelState.DEFAULT
                                .setRotationX(AxisRotation.R90)));
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            WoodSetDatagen.generateItemModels(woodSet, itemModelGenerator);
        }

        itemModelGenerator.register(ItemInit.STEEL_INGOT, Models.GENERATED);
        itemModelGenerator.register(FluidInit.CRUDE_OIL.bucket(), Models.GENERATED);
        itemModelGenerator.register(FluidInit.DIRTY_SODIUM_ALUMINATE.bucket(), Models.GENERATED);
        itemModelGenerator.register(FluidInit.SODIUM_ALUMINATE.bucket(), Models.GENERATED);
        itemModelGenerator.register(FluidInit.MOLTEN_ALUMINIUM.bucket(), Models.GENERATED);
        itemModelGenerator.register(FluidInit.MOLTEN_CRYOLITE.bucket(), Models.GENERATED);
        itemModelGenerator.output.accept(ItemInit.SIMPLE_DRILL_HEAD, ItemModels.special(ModelIds.getItemModelId(ItemInit.SIMPLE_DRILL_HEAD), new DrillHeadItemRenderer.Unbaked()));
        itemModelGenerator.output.accept(ItemInit.BLOCK_BUILDER_DRILL_HEAD, ItemModels.special(ModelIds.getItemModelId(ItemInit.BLOCK_BUILDER_DRILL_HEAD), new DrillHeadItemRenderer.Unbaked()));

        BuiltinEntityModelBuilder.write(itemModelGenerator, BlockInit.WIND_TURBINE, BuiltinEntityModelBuilder.defaultBlock()
                .copyModifyGui(displaySettings -> {
                    displaySettings.setTranslation(-2.5f, -2.5f, 0);
                    displaySettings.setScale(0.5f, 0.5f, 0.5f);
                }));

        BuiltinEntityModelBuilder.write(itemModelGenerator, BlockInit.OIL_PUMP_JACK, BuiltinEntityModelBuilder.defaultBlock()
                .copyModifyGui(displaySettings -> {
                    displaySettings.setTranslation(-1.5f, -2.75f, 0);
                    displaySettings.setScale(0.275f, 0.275f, 0.275f);
                }));

        BuiltinEntityModelBuilder.write(itemModelGenerator, BlockInit.DRILL, BuiltinEntityModelBuilder.defaultBlock()
                .copyModifyGui(displaySettings -> {
                    displaySettings.setTranslation(-2.5f, -2.5f, 0);
                    displaySettings.setScale(0.5f, 0.5f, 0.5f);
                }));

        BuiltinEntityModelBuilder.write(itemModelGenerator, BlockInit.UPGRADE_STATION, BuiltinEntityModelBuilder.defaultBlock()
                .copyModifyGui(displaySettings -> {
                    displaySettings.setTranslation(-1.5f, -2.75f, 0);
                    displaySettings.setScale(0.275f, 0.275f, 0.275f);
                }));

        BuiltinEntityModelBuilder.write(itemModelGenerator, BlockInit.MOTOR, BuiltinEntityModelBuilder.defaultBlock());

        BuiltinEntityModelBuilder.write(itemModelGenerator, ItemInit.SEISMIC_SCANNER);
        BuiltinEntityModelBuilder.write(itemModelGenerator, ItemInit.SIMPLE_DRILL_HEAD,
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyAll(displaySettings ->
                                displaySettings.rotate(180, 180, 0))
                        .copyModifyGui(displaySettings ->
                                displaySettings.rotate(0, 180, 0)));

        BuiltinEntityModelBuilder.write(itemModelGenerator, ItemInit.BLOCK_BUILDER_DRILL_HEAD,
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyAll(displaySettings ->
                                displaySettings.rotate(180, 180, 0))
                        .copyModifyGui(displaySettings ->
                                displaySettings.rotate(0, 180, 0)));

        BuiltinEntityModelBuilder.write(itemModelGenerator, BlockInit.MIXER,
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        BuiltinEntityModelBuilder.write(itemModelGenerator, BlockInit.DIGESTER,
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        BuiltinEntityModelBuilder.write(itemModelGenerator, BlockInit.CLARIFIER,
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        BuiltinEntityModelBuilder.write(itemModelGenerator, BlockInit.CRYSTALLIZER,
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        BuiltinEntityModelBuilder.write(itemModelGenerator, ItemInit.ROTARY_KILN,
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-2.5f, -2.75f, 0);
                            displaySettings.setScale(0.1375f, 0.1375f, 0.1375f);
                        }));

        BuiltinEntityModelBuilder.write(itemModelGenerator, BlockInit.ELECTROLYZER,
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        itemModelGenerator.register(ItemInit.ALUMINIUM_INGOT, Models.GENERATED);
        itemModelGenerator.register(ItemInit.TIN_INGOT, Models.GENERATED);
        itemModelGenerator.register(ItemInit.ZINC_INGOT, Models.GENERATED);
        itemModelGenerator.register(ItemInit.ALUMINIUM_NUGGET, Models.GENERATED);
        itemModelGenerator.register(ItemInit.TIN_NUGGET, Models.GENERATED);
        itemModelGenerator.register(ItemInit.ZINC_NUGGET, Models.GENERATED);
        itemModelGenerator.register(ItemInit.RAW_BAUXITE, Models.GENERATED);
        itemModelGenerator.register(ItemInit.RAW_TIN, Models.GENERATED);
        itemModelGenerator.register(ItemInit.RAW_ZINC, Models.GENERATED);
        itemModelGenerator.register(ItemInit.SODIUM_HYDROXIDE, Models.GENERATED);
        itemModelGenerator.register(ItemInit.SODIUM_ALUMINATE, Models.GENERATED);
        itemModelGenerator.register(ItemInit.RED_MUD, Models.GENERATED);
        itemModelGenerator.register(ItemInit.ALUMINIUM_HYDROXIDE, Models.GENERATED);
        itemModelGenerator.register(ItemInit.SODIUM_CARBONATE, Models.GENERATED);
        itemModelGenerator.register(ItemInit.ALUMINA, Models.GENERATED);
        itemModelGenerator.register(ItemInit.CRYOLITE, Models.GENERATED);
    }

    private void createBattery(BlockStateModelGenerator blockStateModelGenerator, BatteryBlock block) {
        blockStateModelGenerator.createLogTexturePool(block).log(block);
    }
}
