package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.BatteryBlock;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.datagen.builder.BuiltinEntityModelBuilder;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.FluidInit;
import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.*;

public class IndustriaModelProvider extends FabricModelProvider {
    public IndustriaModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            woodSet.generateBlockStateAndModels(blockStateModelGenerator);
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
        BlockStateSupplier pipeSupplier = createPipeBlockStateSupplier(block, name);
        blockStateModelGenerator.blockStateCollector.accept(pipeSupplier);
    }

    private static BlockStateSupplier createPipeBlockStateSupplier(Block block, String name) {
        return MultipartBlockStateSupplier.create(block)
                .with(BlockStateVariant.create().put(VariantSettings.MODEL, Industria.id("block/" + name + "_dot")))
                .with(When.anyOf(When.create().set(PipeBlock.NORTH, PipeBlock.ConnectorType.PIPE)),
                        BlockStateVariant.create().put(VariantSettings.MODEL, Industria.id("block/" + name)))
                .with(When.anyOf(When.create().set(PipeBlock.EAST, PipeBlock.ConnectorType.PIPE)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/" + name))
                                .put(VariantSettings.Y, VariantSettings.Rotation.R90))
                .with(When.anyOf(When.create().set(PipeBlock.SOUTH, PipeBlock.ConnectorType.PIPE)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/" + name))
                                .put(VariantSettings.Y, VariantSettings.Rotation.R180))
                .with(When.anyOf(When.create().set(PipeBlock.WEST, PipeBlock.ConnectorType.PIPE)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/" + name))
                                .put(VariantSettings.Y, VariantSettings.Rotation.R270))
                .with(When.anyOf(When.create().set(PipeBlock.UP, PipeBlock.ConnectorType.PIPE)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/" + name))
                                .put(VariantSettings.X, VariantSettings.Rotation.R270))
                .with(When.anyOf(When.create().set(PipeBlock.DOWN, PipeBlock.ConnectorType.PIPE)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/" + name))
                                .put(VariantSettings.X, VariantSettings.Rotation.R90))
                .with(When.anyOf(When.create().set(PipeBlock.NORTH, PipeBlock.ConnectorType.BLOCK)),
                        BlockStateVariant.create().put(VariantSettings.MODEL, Industria.id("block/" + name + "_connected")))
                .with(When.anyOf(When.create().set(PipeBlock.EAST, PipeBlock.ConnectorType.BLOCK)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/" + name + "_connected"))
                                .put(VariantSettings.Y, VariantSettings.Rotation.R90))
                .with(When.anyOf(When.create().set(PipeBlock.SOUTH, PipeBlock.ConnectorType.BLOCK)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/" + name + "_connected"))
                                .put(VariantSettings.Y, VariantSettings.Rotation.R180))
                .with(When.anyOf(When.create().set(PipeBlock.WEST, PipeBlock.ConnectorType.BLOCK)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/" + name + "_connected"))
                                .put(VariantSettings.Y, VariantSettings.Rotation.R270))
                .with(When.anyOf(When.create().set(PipeBlock.UP, PipeBlock.ConnectorType.BLOCK)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/" + name + "_connected"))
                                .put(VariantSettings.X, VariantSettings.Rotation.R270))
                .with(When.anyOf(When.create().set(PipeBlock.DOWN, PipeBlock.ConnectorType.BLOCK)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/" + name + "_connected"))
                                .put(VariantSettings.X, VariantSettings.Rotation.R90));
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            woodSet.generateItemModels(itemModelGenerator);
        }

        itemModelGenerator.register(ItemInit.STEEL_INGOT, Models.GENERATED);
        itemModelGenerator.register(FluidInit.CRUDE_OIL.bucket(), Models.GENERATED);
        itemModelGenerator.register(FluidInit.DIRTY_SODIUM_ALUMINATE.bucket(), Models.GENERATED);
        itemModelGenerator.register(FluidInit.SODIUM_ALUMINATE.bucket(), Models.GENERATED);
        itemModelGenerator.register(FluidInit.MOLTEN_ALUMINIUM.bucket(), Models.GENERATED);
        itemModelGenerator.register(FluidInit.MOLTEN_CRYOLITE.bucket(), Models.GENERATED);

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
        blockStateModelGenerator.registerLog(block).log(block);
    }
}
