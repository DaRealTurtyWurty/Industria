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
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.AxisRotation;

import java.util.List;
import java.util.Optional;

import static net.minecraft.client.data.Models.GENERATED;
import static net.minecraft.client.data.TexturedModel.makeFactory;

public class IndustriaModelProvider extends FabricModelProvider {
    private static final TextureKey ORE_KEY = TextureKey.of("ore");
    private static final TextureKey BASE_KEY = TextureKey.of("base");
    private static final Model ORE_MODEL = block("ore", ORE_KEY, BASE_KEY);
    private static final TexturedModel.Factory ORE = makeFactory(IndustriaModelProvider::ore, ORE_MODEL);
    private static final TexturedModel.Factory STONE = makeFactory(IndustriaModelProvider::stoneOre, ORE_MODEL);
    private static final TexturedModel.Factory DEEPSLATE = makeFactory(IndustriaModelProvider::deepslateOre, ORE_MODEL);
    public static final TexturedModel.Factory NETHER = makeFactory(IndustriaModelProvider::netherOre, ORE_MODEL);
    public static final TexturedModel.Factory END = makeFactory(IndustriaModelProvider::endOre, ORE_MODEL);

    public IndustriaModelProvider(FabricDataOutput output) {
        super(output);
    }

    private static Model block(String parent, TextureKey... requiredTextureKeys) {
        return new Model(Optional.of(Identifier.of(Industria.MOD_ID, "block/parent/" + parent)),
                Optional.empty(), requiredTextureKeys);
    }

    public static TextureMap ore(Block block) {
        Identifier identifier = TextureMap.getId(block);
        return ore(identifier);
    }

    public static TextureMap ore(Identifier id) {
        return (new TextureMap()).put(ORE_KEY, id);
    }

    public static TextureMap stoneOre(Block block) {
        Identifier identifier = TextureMap.getId(block);
        return stoneOre(identifier);
    }

    public static TextureMap stoneOre(Identifier id) {
        return (new TextureMap()).put(BASE_KEY, Identifier.ofVanilla("block/stone")).put(ORE_KEY, id);
    }

    public static TextureMap deepslateOre(Block block) {
        Identifier identifier = TextureMap.getId(block);
        String namespace = identifier.getNamespace();
        String path = identifier.getPath().replace("deepslate_", "");
        return deepslateOre(Identifier.of(namespace, path));
    }

    public static TextureMap deepslateOre(Identifier id) {
        return (new TextureMap()).put(BASE_KEY, Identifier.ofVanilla("block/deepslate")).put(ORE_KEY, id);
    }

    public static TextureMap netherOre(Block block) {
        Identifier identifier = TextureMap.getId(block);
        String namespace = identifier.getNamespace();
        String path = identifier.getPath().replace("nether_", "");
        return netherOre(Identifier.of(namespace, path));
    }

    public static TextureMap netherOre(Identifier id) {
        return (new TextureMap()).put(BASE_KEY, Identifier.ofVanilla("block/netherrack")).put(ORE_KEY, id);
    }

    public static TextureMap endOre(Block block) {
        Identifier identifier = TextureMap.getId(block);
        String namespace = identifier.getNamespace();
        String path = identifier.getPath().replace("end_", "");
        return endOre(Identifier.of(namespace, path));
    }

    public static TextureMap endOre(Identifier id) {
        return (new TextureMap()).put(BASE_KEY, Identifier.ofVanilla("block/end_stone")).put(ORE_KEY, id);
    }

    private static void registerPipe(BlockStateModelGenerator blockStateModelGenerator, Block block, String name) {
        BlockModelDefinitionCreator pipeSupplier = createPipeBlockModelDefinitionCreator(block, name);
        blockStateModelGenerator.blockStateCollector.accept(pipeSupplier);
    }

    public static WeightedVariant createWeightedVariant(Identifier id, ModelVariant.ModelState modelState) {
        return new WeightedVariant(Pool.of(new ModelVariant(id, modelState)));
    }

    private static BlockModelDefinitionCreator createPipeBlockModelDefinitionCreator(Block block, String name) {
        Identifier blockModelId = Industria.id("block/" + name);
        Identifier connectedBlockModelId = Industria.id("block/" + name + "_connected");
        return MultipartBlockModelDefinitionCreator.create(block)
                .with(createWeightedVariant(Industria.id("block/" + name + "_dot"), ModelVariant.ModelState.DEFAULT))
                .with(new MultipartModelConditionBuilder().put(PipeBlock.NORTH, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(blockModelId, ModelVariant.ModelState.DEFAULT))
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
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            WoodSetDatagen.generateBlockStateAndModels(woodSet, blockStateModelGenerator);
        }

        // Aluminium
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.BAUXITE_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_BAUXITE_ORE, "deepslate");
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.RAW_BAUXITE_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.ALUMINIUM_BLOCK);

        // Silver
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.ARGENTITE_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_ARGENTITE_ORE, "deepslate");
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.RAW_ARGENTITE_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.SILVER_BLOCK);

        // Lead
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.GALENA_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_GALENA_ORE, "deepslate");
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.RAW_GALENA_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.LEAD_BLOCK);

        // Titanium
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.ILMENITE_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_ILMENITE_ORE, "deepslate");
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.RAW_ILMENITE_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.TITANIUM_BLOCK);

        // Zinc
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.SPHALERITE_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_SPHALERITE_ORE, "deepslate");
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.RAW_SPHALERITE_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.ZINC_BLOCK);

        // Cobalt
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.COBALTITE_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_COBALTITE_ORE, "deepslate");
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.RAW_COBALTITE_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.COBALT_BLOCK);

        // Nickel
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.PENTLANDITE_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_PENTLANDITE_ORE, "deepslate");
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.RAW_PENTLANDITE_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.NICKEL_BLOCK);

        // Iridium
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.IRIDIUM_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_IRIDIUM_ORE, "deepslate");
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.IRIDIUM_BLOCK);

        // Tin
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.CASSITERITE_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_CASSITERITE_ORE, "deepslate");
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.RAW_CASSITERITE_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.TIN_BLOCK);

        // Pyrite
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.NETHER_PYRITE_ORE, "nether");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.END_PYRITE_ORE, "end");
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.PYRITE_BLOCK);

        // Steel
        blockStateModelGenerator.registerSimpleCubeAll(BlockInit.STEEL_BLOCK);

        // Quartz
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.QUARTZ_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_QUARTZ_ORE, "deepslate");

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
    }

    private void registerSimpleOreBlock(BlockStateModelGenerator blockStateModelGenerator, Block block, String type) {
        if (!"ore".equals(type))
            type += "_ore";
        registerSimpleCubeAll(blockStateModelGenerator, block, type);
    }

    public void registerSingleton(BlockStateModelGenerator blockStateModelGenerator, Block block, TexturedModel.Factory modelFactory) {
        blockStateModelGenerator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(block,
                BlockStateModelGenerator.createWeightedVariant(modelFactory.upload(block, blockStateModelGenerator.modelCollector))));
    }

    public void registerSimpleCubeAll(BlockStateModelGenerator blockStateModelGenerator, Block block, String type) {
        switch (type) {
            case "stone_ore" -> registerSingleton(blockStateModelGenerator, block, STONE);
            case "deepslate_ore" -> registerSingleton(blockStateModelGenerator, block, DEEPSLATE);
            case "nether_ore" -> registerSingleton(blockStateModelGenerator, block, NETHER);
            case "end_ore" -> registerSingleton(blockStateModelGenerator, block, END);
            default -> registerSingleton(blockStateModelGenerator, block, ORE);
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
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

        BuiltinEntityModelBuilder.write(itemModelGenerator, BlockInit.SHAKING_TABLE,
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        BuiltinEntityModelBuilder.write(itemModelGenerator, BlockInit.CENTRIFUGAL_CONCENTRATOR,
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        final List<Item> exclusionList = List.of(ItemInit.SEISMIC_SCANNER, ItemInit.SIMPLE_DRILL_HEAD, ItemInit.BLOCK_BUILDER_DRILL_HEAD);

        Registries.ITEM.streamKeys().filter(key -> key.getValue().getNamespace().equals(Industria.MOD_ID))
                .map(Registries.ITEM::get)
                .filter(entry -> !(entry instanceof BlockItem))
                .filter(entry -> !exclusionList.contains(entry))
                .forEach(entry -> itemModelGenerator.register(entry, GENERATED));
    }

    private void createBattery(BlockStateModelGenerator blockStateModelGenerator, BatteryBlock block) {
        blockStateModelGenerator.createLogTexturePool(block).log(block);
    }
}
