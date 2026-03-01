package dev.turtywurty.industria.datagen;

import com.mojang.math.Quadrant;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.BatteryBlock;
import dev.turtywurty.industria.block.ConveyorBlock;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.datagen.builder.BuiltinEntityModelBuilder;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.FluidInit;
import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.renderer.item.DrillHeadItemRenderer;
import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.model.Material;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Optional;

import static net.minecraft.client.data.models.model.ModelTemplates.FLAT_ITEM;
import static net.minecraft.client.data.models.model.TexturedModel.createDefault;

public class IndustriaModelProvider extends FabricModelProvider {
    private static final TextureSlot ORE_KEY = TextureSlot.create("ore");
    private static final TextureSlot BASE_KEY = TextureSlot.create("base");
    private static final ModelTemplate ORE_MODEL = block("ore", ORE_KEY, BASE_KEY);
    private static final TexturedModel.Provider ORE = createDefault(IndustriaModelProvider::ore, ORE_MODEL);
    private static final TexturedModel.Provider STONE = createDefault(IndustriaModelProvider::stoneOre, ORE_MODEL);
    private static final TexturedModel.Provider DEEPSLATE = createDefault(IndustriaModelProvider::deepslateOre, ORE_MODEL);
    public static final TexturedModel.Provider NETHER = createDefault(IndustriaModelProvider::netherOre, ORE_MODEL);
    public static final TexturedModel.Provider END = createDefault(IndustriaModelProvider::endOre, ORE_MODEL);

    public IndustriaModelProvider(FabricPackOutput output) {
        super(output);
    }

    private static ModelTemplate block(String parent, TextureSlot... requiredTextureKeys) {
        return new ModelTemplate(Optional.of(Identifier.fromNamespaceAndPath(Industria.MOD_ID, "block/parent/" + parent)),
                Optional.empty(), requiredTextureKeys);
    }

    public static TextureMapping ore(Block block) {
        Material material = TextureMapping.getBlockTexture(block);
        return ore(material);
    }

    public static TextureMapping ore(Material material) {
        return (new TextureMapping()).put(ORE_KEY, material);
    }

    public static TextureMapping stoneOre(Block block) {
        Material material = TextureMapping.getBlockTexture(block);
        return stoneOre(material);
    }

    public static TextureMapping stoneOre(Material material) {
        return (new TextureMapping()).put(BASE_KEY, TextureMapping.getBlockTexture(Blocks.STONE)).put(ORE_KEY, material);
    }

    private static Material removeTexturePrefix(Material material, String prefix) {
        return new Material(material.sprite().withPath(path -> path.replace(prefix, "")), material.forceTranslucent());
    }

    public static TextureMapping deepslateOre(Block block) {
        return deepslateOre(removeTexturePrefix(TextureMapping.getBlockTexture(block), "deepslate_"));
    }

    public static TextureMapping deepslateOre(Material material) {
        return (new TextureMapping()).put(BASE_KEY, TextureMapping.getBlockTexture(Blocks.DEEPSLATE)).put(ORE_KEY, material);
    }

    public static TextureMapping netherOre(Block block) {
        return netherOre(removeTexturePrefix(TextureMapping.getBlockTexture(block), "nether_"));
    }

    public static TextureMapping netherOre(Material material) {
        return (new TextureMapping()).put(BASE_KEY, TextureMapping.getBlockTexture(Blocks.NETHERRACK)).put(ORE_KEY, material);
    }

    public static TextureMapping endOre(Block block) {
        return endOre(removeTexturePrefix(TextureMapping.getBlockTexture(block), "end_"));
    }

    public static TextureMapping endOre(Material material) {
        return (new TextureMapping()).put(BASE_KEY, TextureMapping.getBlockTexture(Blocks.END_STONE)).put(ORE_KEY, material);
    }

    private static void registerPipe(BlockModelGenerators blockStateModelGenerator, Block block, String name) {
        BlockModelDefinitionGenerator pipeSupplier = createPipeBlockModelDefinitionCreator(block, name);
        blockStateModelGenerator.blockStateOutput.accept(pipeSupplier);
    }

    private static void registerConveyor(BlockModelGenerators blockStateModelGenerator, ConveyorBlock block, String name) {
        blockStateModelGenerator.blockStateOutput.accept(createConveyorBlockModelDefinitionCreator(block, name));
    }

    public static MultiVariant createWeightedVariant(Identifier id, Variant.SimpleModelState modelState) {
        return new MultiVariant(WeightedList.of(new Variant(id, modelState)));
    }

    private static BlockModelDefinitionGenerator createPipeBlockModelDefinitionCreator(Block block, String name) {
        Identifier blockModelId = Industria.id("block/" + name);
        Identifier connectedBlockModelId = Industria.id("block/" + name + "_connected");
        return MultiPartGenerator.multiPart(block)
                .with(createWeightedVariant(Industria.id("block/" + name + "_dot"), Variant.SimpleModelState.DEFAULT))
                .with(new ConditionBuilder().term(PipeBlock.NORTH, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(blockModelId, Variant.SimpleModelState.DEFAULT))
                .with(new ConditionBuilder().term(PipeBlock.EAST, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(blockModelId,
                                Variant.SimpleModelState.DEFAULT
                                        .withY(Quadrant.R90)))
                .with(new ConditionBuilder().term(PipeBlock.SOUTH, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(blockModelId,
                                Variant.SimpleModelState.DEFAULT
                                        .withY(Quadrant.R180)))
                .with(new ConditionBuilder().term(PipeBlock.WEST, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(blockModelId,
                                Variant.SimpleModelState.DEFAULT
                                        .withY(Quadrant.R270)))
                .with(new ConditionBuilder().term(PipeBlock.UP, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(blockModelId,
                                Variant.SimpleModelState.DEFAULT
                                        .withX(Quadrant.R270)))
                .with(new ConditionBuilder().term(PipeBlock.DOWN, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(blockModelId,
                                Variant.SimpleModelState.DEFAULT
                                        .withX(Quadrant.R90)))
                .with(new ConditionBuilder().term(PipeBlock.NORTH, PipeBlock.ConnectorType.BLOCK),
                        createWeightedVariant(connectedBlockModelId, Variant.SimpleModelState.DEFAULT))
                .with(new ConditionBuilder().term(PipeBlock.EAST, PipeBlock.ConnectorType.BLOCK),
                        createWeightedVariant(connectedBlockModelId, Variant.SimpleModelState.DEFAULT
                                .withY(Quadrant.R90)))
                .with(new ConditionBuilder().term(PipeBlock.SOUTH, PipeBlock.ConnectorType.BLOCK),
                        createWeightedVariant(connectedBlockModelId, Variant.SimpleModelState.DEFAULT
                                .withY(Quadrant.R180)))
                .with(new ConditionBuilder().term(PipeBlock.WEST, PipeBlock.ConnectorType.BLOCK),
                        createWeightedVariant(connectedBlockModelId, Variant.SimpleModelState.DEFAULT
                                .withY(Quadrant.R270)))
                .with(new ConditionBuilder().term(PipeBlock.UP, PipeBlock.ConnectorType.BLOCK),
                        createWeightedVariant(connectedBlockModelId, Variant.SimpleModelState.DEFAULT
                                .withX(Quadrant.R270)))
                .with(new ConditionBuilder().term(PipeBlock.DOWN, PipeBlock.ConnectorType.BLOCK),
                        createWeightedVariant(connectedBlockModelId, Variant.SimpleModelState.DEFAULT
                                .withX(Quadrant.R90)));
    }

    private static BlockModelDefinitionGenerator createConveyorBlockModelDefinitionCreator(ConveyorBlock block, String name) {
        Identifier straightModelId = Industria.id("block/" + name);
        Identifier upModelId = Industria.id("block/" + name + "_up");
        Identifier downModelId = Industria.id("block/" + name + "_down");
        Identifier turnLeftModelId = Industria.id("block/" + name + "_turn_left");
        Identifier turnRightModelId = Industria.id("block/" + name + "_turn_right");

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Variant.SimpleModelState state = rotationFor(direction);
            generator.with(new ConditionBuilder()
                            .term(ConveyorBlock.FACING, direction)
                            .term(ConveyorBlock.SHAPE, ConveyorBlock.ConveyorShape.STRAIGHT),
                    createWeightedVariant(straightModelId, state));
            generator.with(new ConditionBuilder()
                            .term(ConveyorBlock.FACING, direction)
                            .term(ConveyorBlock.SHAPE, ConveyorBlock.ConveyorShape.UP),
                    createWeightedVariant(upModelId, state));
            generator.with(new ConditionBuilder()
                            .term(ConveyorBlock.FACING, direction)
                            .term(ConveyorBlock.SHAPE, ConveyorBlock.ConveyorShape.DOWN),
                    createWeightedVariant(downModelId, state));
            generator.with(new ConditionBuilder()
                            .term(ConveyorBlock.FACING, direction)
                            .term(ConveyorBlock.SHAPE, ConveyorBlock.ConveyorShape.TURN_LEFT),
                    createWeightedVariant(turnLeftModelId, state));
            generator.with(new ConditionBuilder()
                            .term(ConveyorBlock.FACING, direction)
                            .term(ConveyorBlock.SHAPE, ConveyorBlock.ConveyorShape.TURN_RIGHT),
                    createWeightedVariant(turnRightModelId, state));
        }

        return generator;
    }

    private static Variant.SimpleModelState rotationFor(Direction direction) {
        return switch (direction) {
            case EAST -> Variant.SimpleModelState.DEFAULT.withY(Quadrant.R90);
            case SOUTH -> Variant.SimpleModelState.DEFAULT.withY(Quadrant.R180);
            case WEST -> Variant.SimpleModelState.DEFAULT.withY(Quadrant.R270);
            case NORTH, UP, DOWN -> Variant.SimpleModelState.DEFAULT;
        };
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            WoodSetDatagen.generateBlockStateAndModels(woodSet, blockStateModelGenerator);
        }

        // Aluminium
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.BAUXITE_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_BAUXITE_ORE, "deepslate");
        blockStateModelGenerator.createTrivialCube(BlockInit.RAW_BAUXITE_BLOCK);
        blockStateModelGenerator.createTrivialCube(BlockInit.ALUMINIUM_BLOCK);

        // Silver
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.ARGENTITE_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_ARGENTITE_ORE, "deepslate");
        blockStateModelGenerator.createTrivialCube(BlockInit.RAW_ARGENTITE_BLOCK);
        blockStateModelGenerator.createTrivialCube(BlockInit.SILVER_BLOCK);

        // Lead
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.GALENA_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_GALENA_ORE, "deepslate");
        blockStateModelGenerator.createTrivialCube(BlockInit.RAW_GALENA_BLOCK);
        blockStateModelGenerator.createTrivialCube(BlockInit.LEAD_BLOCK);

        // Titanium
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.ILMENITE_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_ILMENITE_ORE, "deepslate");
        blockStateModelGenerator.createTrivialCube(BlockInit.RAW_ILMENITE_BLOCK);
        blockStateModelGenerator.createTrivialCube(BlockInit.TITANIUM_BLOCK);

        // Zinc
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.SPHALERITE_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_SPHALERITE_ORE, "deepslate");
        blockStateModelGenerator.createTrivialCube(BlockInit.RAW_SPHALERITE_BLOCK);
        blockStateModelGenerator.createTrivialCube(BlockInit.ZINC_BLOCK);

        // Cobalt
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.COBALTITE_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_COBALTITE_ORE, "deepslate");
        blockStateModelGenerator.createTrivialCube(BlockInit.RAW_COBALTITE_BLOCK);
        blockStateModelGenerator.createTrivialCube(BlockInit.COBALT_BLOCK);

        // Nickel
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.PENTLANDITE_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_PENTLANDITE_ORE, "deepslate");
        blockStateModelGenerator.createTrivialCube(BlockInit.RAW_PENTLANDITE_BLOCK);
        blockStateModelGenerator.createTrivialCube(BlockInit.NICKEL_BLOCK);

        // Iridium
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.IRIDIUM_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_IRIDIUM_ORE, "deepslate");
        blockStateModelGenerator.createTrivialCube(BlockInit.IRIDIUM_BLOCK);

        // Tin
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.CASSITERITE_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_CASSITERITE_ORE, "deepslate");
        blockStateModelGenerator.createTrivialCube(BlockInit.RAW_CASSITERITE_BLOCK);
        blockStateModelGenerator.createTrivialCube(BlockInit.TIN_BLOCK);

        // Pyrite
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.NETHER_PYRITE_ORE, "nether");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.END_PYRITE_ORE, "end");
        blockStateModelGenerator.createTrivialCube(BlockInit.PYRITE_BLOCK);

        // Steel
        blockStateModelGenerator.createTrivialCube(BlockInit.STEEL_BLOCK);

        // Quartz
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.QUARTZ_ORE, "stone");
        registerSimpleOreBlock(blockStateModelGenerator, BlockInit.DEEPSLATE_QUARTZ_ORE, "deepslate");

        blockStateModelGenerator.createFurnace(BlockInit.ALLOY_FURNACE, TexturedModel.ORIENTABLE_ONLY_TOP);
        blockStateModelGenerator.createFurnace(BlockInit.THERMAL_GENERATOR, TexturedModel.ORIENTABLE_ONLY_TOP);
        createBattery(blockStateModelGenerator, BlockInit.BASIC_BATTERY);
        createBattery(blockStateModelGenerator, BlockInit.ADVANCED_BATTERY);
        createBattery(blockStateModelGenerator, BlockInit.ELITE_BATTERY);
        createBattery(blockStateModelGenerator, BlockInit.ULTIMATE_BATTERY);
        createBattery(blockStateModelGenerator, BlockInit.CREATIVE_BATTERY);
        blockStateModelGenerator.createFurnace(BlockInit.COMBUSTION_GENERATOR, TexturedModel.ORIENTABLE_ONLY_TOP);
        blockStateModelGenerator.createNonTemplateHorizontalBlock(BlockInit.SOLAR_PANEL);
        blockStateModelGenerator.registerSimpleItemModel(BlockInit.SOLAR_PANEL, Industria.id("block/solar_panel"));
        blockStateModelGenerator.createNonTemplateModelBlock(FluidInit.CRUDE_OIL.block());
        blockStateModelGenerator.createNonTemplateModelBlock(FluidInit.DIRTY_SODIUM_ALUMINATE.block());
        blockStateModelGenerator.createNonTemplateModelBlock(FluidInit.SODIUM_ALUMINATE.block());
        blockStateModelGenerator.createNonTemplateModelBlock(FluidInit.MOLTEN_ALUMINIUM.block());
        blockStateModelGenerator.createNonTemplateModelBlock(FluidInit.MOLTEN_CRYOLITE.block());
        blockStateModelGenerator.createNonTemplateModelBlock(BlockInit.DRILL_TUBE);
        blockStateModelGenerator.registerSimpleItemModel(BlockInit.DRILL_TUBE, Industria.id("block/drill_tube"));
        blockStateModelGenerator.createFurnace(BlockInit.ELECTRIC_FURNACE, TexturedModel.ORIENTABLE_ONLY_TOP);
        blockStateModelGenerator.createNonTemplateHorizontalBlock(BlockInit.FRACTIONAL_DISTILLATION_CONTROLLER);
        blockStateModelGenerator.registerSimpleItemModel(BlockInit.FRACTIONAL_DISTILLATION_CONTROLLER, Industria.id("block/fractional_distillation_controller"));
        blockStateModelGenerator.createNonTemplateHorizontalBlock(BlockInit.FRACTIONAL_DISTILLATION_TOWER);
        blockStateModelGenerator.registerSimpleItemModel(BlockInit.FRACTIONAL_DISTILLATION_TOWER, Industria.id("block/fractional_distillation_tower"));
        blockStateModelGenerator.createNonTemplateModelBlock(BlockInit.INDUCTION_HEATER);
        blockStateModelGenerator.createNonTemplateModelBlock(BlockInit.FLUID_PUMP);
        blockStateModelGenerator.createNonTemplateModelBlock(BlockInit.FLUID_TANK);

        // Multiblock controllers and segments should have a visible fallback block model while unformed.
        registerCustomCube(blockStateModelGenerator, BlockInit.ARC_FURNACE, Industria.id("block/steel_block"));
        registerCustomCube(blockStateModelGenerator, BlockInit.CENTRIFUGAL_CONCENTRATOR, Industria.id("block/steel_block"));
        registerCustomCube(blockStateModelGenerator, BlockInit.CLARIFIER, Industria.id("block/clarifier"));
        registerCustomCube(blockStateModelGenerator, BlockInit.CRYSTALLIZER, Industria.id("block/crystallizer"));
        registerCustomCube(blockStateModelGenerator, BlockInit.DIGESTER, Industria.id("block/digester"));
        registerCustomCube(blockStateModelGenerator, BlockInit.DRILL, Industria.id("block/drill_frame"));
        registerCustomCube(blockStateModelGenerator, BlockInit.ELECTROLYZER, Industria.id("block/electrolyzer"));
        registerCustomCube(blockStateModelGenerator, BlockInit.MIXER, Industria.id("block/mixer"));
        registerCustomCube(blockStateModelGenerator, BlockInit.OIL_PUMP_JACK, Industria.id("block/oil_pump_jack"));
        registerCustomCube(blockStateModelGenerator, BlockInit.ROTARY_KILN, Industria.id("block/rotary_kiln"));
        registerCustomCube(blockStateModelGenerator, BlockInit.ROTARY_KILN_CONTROLLER, Industria.id("block/rotary_kiln"));
        registerCustomCube(blockStateModelGenerator, BlockInit.SHAKING_TABLE, Industria.id("block/shaking_table"));
        registerCustomCube(blockStateModelGenerator, BlockInit.UPGRADE_STATION, Industria.id("block/upgrade_station"));

        registerPipe(blockStateModelGenerator, BlockInit.CABLE, "cable");
        registerPipe(blockStateModelGenerator, BlockInit.FLUID_PIPE, "fluid_pipe");
        registerPipe(blockStateModelGenerator, BlockInit.SLURRY_PIPE, "slurry_pipe");
        registerPipe(blockStateModelGenerator, BlockInit.HEAT_PIPE, "heat_pipe");
        registerConveyor(blockStateModelGenerator, BlockInit.CONVEYOR, "conveyor");
    }

    private void registerSimpleOreBlock(BlockModelGenerators blockStateModelGenerator, Block block, String type) {
        if (!"ore".equals(type))
            type += "_ore";
        registerSimpleCubeAll(blockStateModelGenerator, block, type);
    }

    public void registerSingleton(BlockModelGenerators blockStateModelGenerator, Block block, TexturedModel.Provider modelFactory) {
        blockStateModelGenerator.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block,
                BlockModelGenerators.plainVariant(modelFactory.create(block, blockStateModelGenerator.modelOutput))));
    }

    public void registerSimpleCubeAll(BlockModelGenerators blockStateModelGenerator, Block block, String type) {
        switch (type) {
            case "stone_ore" -> registerSingleton(blockStateModelGenerator, block, STONE);
            case "deepslate_ore" -> registerSingleton(blockStateModelGenerator, block, DEEPSLATE);
            case "nether_ore" -> registerSingleton(blockStateModelGenerator, block, NETHER);
            case "end_ore" -> registerSingleton(blockStateModelGenerator, block, END);
            default -> registerSingleton(blockStateModelGenerator, block, ORE);
        }
    }

    private void registerCustomCube(final BlockModelGenerators blockStateModelGenerator, final Block block, final Identifier texture) {
        registerSingleton(blockStateModelGenerator, block, createDefault(
                ignored -> new TextureMapping().put(TextureSlot.ALL, new Material(texture, false)),
                ModelTemplates.CUBE_ALL));
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        itemModelGenerator.itemModelOutput.accept(ItemInit.SIMPLE_DRILL_HEAD, ItemModelUtils.specialModel(ModelLocationUtils.getModelLocation(ItemInit.SIMPLE_DRILL_HEAD), new DrillHeadItemRenderer.Unbaked()));
        itemModelGenerator.itemModelOutput.accept(ItemInit.BLOCK_BUILDER_DRILL_HEAD, ItemModelUtils.specialModel(ModelLocationUtils.getModelLocation(ItemInit.BLOCK_BUILDER_DRILL_HEAD), new DrillHeadItemRenderer.Unbaked()));

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

        BuiltInRegistries.ITEM.listElementIds().filter(key -> key.identifier().getNamespace().equals(Industria.MOD_ID))
                .map(BuiltInRegistries.ITEM::getValue)
                .filter(entry -> !(entry instanceof BlockItem))
                .filter(entry -> !exclusionList.contains(entry))
                .forEach(entry -> itemModelGenerator.generateFlatItem(entry, FLAT_ITEM));
    }

    private void createBattery(BlockModelGenerators blockStateModelGenerator, BatteryBlock block) {
        blockStateModelGenerator.woodProvider(block).logWithHorizontal(block);
    }
}
