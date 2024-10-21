package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.BatteryBlock;
import dev.turtywurty.industria.block.CableBlock;
import dev.turtywurty.industria.datagen.builder.BuiltinEntityModelBuilder;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.FluidInit;
import dev.turtywurty.industria.init.ItemInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;

public class IndustriaModelProvider extends FabricModelProvider {
    public IndustriaModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
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
        blockStateModelGenerator.registerSimpleState(FluidInit.CRUDE_OIL_BLOCK);

        BlockStateSupplier cableSupplier = MultipartBlockStateSupplier.create(BlockInit.CABLE)
                .with(BlockStateVariant.create().put(VariantSettings.MODEL, Industria.id("block/cable_dot")))
                .with(When.anyOf(When.create().set(CableBlock.NORTH, CableBlock.ConnectorType.CABLE)),
                        BlockStateVariant.create().put(VariantSettings.MODEL, Industria.id("block/cable")))
                .with(When.anyOf(When.create().set(CableBlock.EAST, CableBlock.ConnectorType.CABLE)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/cable"))
                                .put(VariantSettings.Y, VariantSettings.Rotation.R90))
                .with(When.anyOf(When.create().set(CableBlock.SOUTH, CableBlock.ConnectorType.CABLE)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/cable"))
                                .put(VariantSettings.Y, VariantSettings.Rotation.R180))
                .with(When.anyOf(When.create().set(CableBlock.WEST, CableBlock.ConnectorType.CABLE)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/cable"))
                                .put(VariantSettings.Y, VariantSettings.Rotation.R270))
                .with(When.anyOf(When.create().set(CableBlock.UP, CableBlock.ConnectorType.CABLE)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/cable"))
                                .put(VariantSettings.X, VariantSettings.Rotation.R270))
                .with(When.anyOf(When.create().set(CableBlock.DOWN, CableBlock.ConnectorType.CABLE)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/cable"))
                                .put(VariantSettings.X, VariantSettings.Rotation.R90))
                .with(When.anyOf(When.create().set(CableBlock.NORTH, CableBlock.ConnectorType.BLOCK)),
                        BlockStateVariant.create().put(VariantSettings.MODEL, Industria.id("block/cable_connected")))
                .with(When.anyOf(When.create().set(CableBlock.EAST, CableBlock.ConnectorType.BLOCK)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/cable_connected"))
                                .put(VariantSettings.Y, VariantSettings.Rotation.R90))
                .with(When.anyOf(When.create().set(CableBlock.SOUTH, CableBlock.ConnectorType.BLOCK)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/cable_connected"))
                                .put(VariantSettings.Y, VariantSettings.Rotation.R180))
                .with(When.anyOf(When.create().set(CableBlock.WEST, CableBlock.ConnectorType.BLOCK)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/cable_connected"))
                                .put(VariantSettings.Y, VariantSettings.Rotation.R270))
                .with(When.anyOf(When.create().set(CableBlock.UP, CableBlock.ConnectorType.BLOCK)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/cable_connected"))
                                .put(VariantSettings.X, VariantSettings.Rotation.R270))
                .with(When.anyOf(When.create().set(CableBlock.DOWN, CableBlock.ConnectorType.BLOCK)),
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL, Industria.id("block/cable_connected"))
                                .put(VariantSettings.X, VariantSettings.Rotation.R90));

        blockStateModelGenerator.blockStateCollector.accept(cableSupplier);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ItemInit.STEEL_INGOT, Models.GENERATED);

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

        BuiltinEntityModelBuilder.write(itemModelGenerator, ItemInit.SEISMIC_SCANNER);
        BuiltinEntityModelBuilder.write(itemModelGenerator, ItemInit.SIMPLE_DRILL_HEAD,
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyAll(displaySettings ->
                                displaySettings.rotate(180, 180, 0))
                        .copyModifyGui(displaySettings ->
                                displaySettings.rotate(0, 180, 0)));

        BuiltinEntityModelBuilder.write(itemModelGenerator, BlockInit.MOTOR, BuiltinEntityModelBuilder.defaultBlock());
    }

    private void createBattery(BlockStateModelGenerator blockStateModelGenerator, BatteryBlock block) {
        blockStateModelGenerator.registerLog(block).log(block);
    }
}
