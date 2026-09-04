package dev.turtywurty.industria.datagen;

import com.mojang.math.Quadrant;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.BatteryBlock;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.block.SolarPanelBlock;
import dev.turtywurty.industria.conveyor.block.impl.*;
import dev.turtywurty.industria.datagen.builder.BuiltinEntityModelBuilder;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModFluids;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.industria.init.ModWoodSets;
import dev.turtywurty.industria.model.*;
import dev.turtywurty.industria.renderer.item.IndustriaBlockEntityItemRenderer;
import dev.turtywurty.industria.renderer.item.DrillHeadItemRenderer;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static net.minecraft.client.data.models.model.ModelTemplates.FLAT_ITEM;
import static net.minecraft.client.data.models.model.TexturedModel.createDefault;

public final class IndustriaModels {
    private static final TextureSlot ORE_KEY = TextureSlot.create("ore");
    private static final TextureSlot BASE_KEY = TextureSlot.create("base");
    private static final ModelTemplate ORE_MODEL = block("ore", ORE_KEY, BASE_KEY);
    private static final TexturedModel.Provider ORE = createDefault(IndustriaModels::ore, ORE_MODEL);
    private static final TexturedModel.Provider STONE = createDefault(IndustriaModels::stoneOre, ORE_MODEL);
    private static final TexturedModel.Provider DEEPSLATE = createDefault(IndustriaModels::deepslateOre, ORE_MODEL);
    public static final TexturedModel.Provider NETHER = createDefault(IndustriaModels::netherOre, ORE_MODEL);
    public static final TexturedModel.Provider END = createDefault(IndustriaModels::endOre, ORE_MODEL);

    private IndustriaModels() {
    }

    public static void generate(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        IndustriaModels generator = new IndustriaModels();
        generator.generateBlockStateModels(blockModels);
        generator.generateItemModels(itemModels);
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
        BlockModelDefinitionGenerator pipeSupplier = createPipeBlockModelDefinitionCreator(block, name, true);
        blockStateModelGenerator.blockStateOutput.accept(pipeSupplier);
    }

    private static void registerLegacyPipe(BlockModelGenerators blockStateModelGenerator, Block block, String name) {
        BlockModelDefinitionGenerator pipeSupplier = createPipeBlockModelDefinitionCreator(block, name, false);
        blockStateModelGenerator.blockStateOutput.accept(pipeSupplier);
    }

    private static void registerConveyor(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.blockStateOutput.accept(createConveyorBlockModelDefinitionCreator(ModBlocks.CONVEYOR.get(), "conveyor"));
    }

    private static void registerSplitterConveyor(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.blockStateOutput.accept(createSplitterConveyorBlockModelDefinitionCreator(ModBlocks.SPLITTER_CONVEYOR.get(), "splitter_conveyor"));
    }

    private static void registerMergerConveyor(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.blockStateOutput.accept(createMergerConveyorBlockModelDefinitionCreator(ModBlocks.MERGER_CONVEYOR.get(), "merger_conveyor"));
    }

    private static void registerAlternatorConveyor(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.blockStateOutput.accept(createAlternatorConveyorBlockModelDefinitionCreator(ModBlocks.ALTERNATOR_CONVEYOR.get(), "alternator_conveyor"));
    }

    private static void registerFeederConveyor(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.blockStateOutput.accept(createFeederConveyorBlockModelDefinitionCreator(ModBlocks.FEEDER_CONVEYOR.get(), "feeder_conveyor"));
    }

    private static void registerHatchConveyor(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.blockStateOutput.accept(createHatchConveyorBlockModelDefinitionCreator(ModBlocks.HATCH_CONVEYOR.get(), "hatch_conveyor"));
    }

    private static void registerSideInjectorConveyor(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.blockStateOutput.accept(createSideInjectorConveyorBlockModelDefinitionCreator(ModBlocks.SIDE_INJECTOR_CONVEYOR.get(), "side_injector_conveyor"));
    }

    private static void registerLadderConveyor(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.blockStateOutput.accept(createLadderConveyorBlockModelDefinitionCreator(ModBlocks.LADDER_CONVEYOR.get(), "ladder_conveyor"));
    }

    private static void registerFilterConveyor(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.blockStateOutput.accept(createFilterConveyorBlockModelDefinitionCreator(ModBlocks.FILTER_CONVEYOR.get(), "filter_conveyor"));
    }

    private static void registerMagneticConveyor(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.blockStateOutput.accept(createMagneticConveyorBlockModelDefinitionCreator(ModBlocks.MAGNETIC_CONVEYOR.get(), "magnetic_conveyor"));
    }

    private static void registerDetectorConveyor(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.blockStateOutput.accept(createDetectorConveyorBlockModelDefinitionCreator(ModBlocks.DETECTOR_CONVEYOR.get()));
    }

    private static void registerCountConveyor(BlockModelGenerators blockStateModelGenerator) {
        registerFacingOnlyConveyor(blockStateModelGenerator, ModBlocks.COUNT_CONVEYOR.get(), Industria.id("block/conveyor"));
    }

    private static void registerDelayConveyor(BlockModelGenerators blockStateModelGenerator) {
        registerFacingOnlyConveyor(blockStateModelGenerator, ModBlocks.DELAY_CONVEYOR.get(), Industria.id("block/conveyor"));
    }

    private static void registerContainmentConveyor(BlockModelGenerators blockStateModelGenerator) {
        registerFacingOnlyConveyor(blockStateModelGenerator, ModBlocks.CONTAINMENT_CONVEYOR.get(), Industria.id("block/conveyor"));
    }

    private static void registerDropChuteConveyor(BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.blockStateOutput.accept(createDropChuteConveyorBlockModelDefinitionCreator(ModBlocks.DROP_CHUTE_CONVEYOR.get(), "drop_chute_conveyor"));
    }

    public static MultiVariant createWeightedVariant(Identifier id, Variant.SimpleModelState modelState) {
        return new MultiVariant(WeightedList.of(new Variant(id, modelState)));
    }

    private static void registerRandomBauxiteBlock(BlockModelGenerators blockModelGenerator, Block block) {
        WeightedList.Builder<Variant> variants = WeightedList.builder();
        for (int index = 0; index < 3; index++) {
            String suffix = index == 0 ? "" : "_" + index;
            Identifier texture = Industria.id("block/bauxite_ore" + suffix);
            TextureMapping textureMapping = TextureMapping.cube(new Material(texture, false));
            Identifier model = index == 0
                    ? ModelTemplates.CUBE_ALL.create(block, textureMapping, blockModelGenerator.modelOutput)
                    : ModelTemplates.CUBE_ALL.createWithSuffix(block, suffix, textureMapping, blockModelGenerator.modelOutput);
            variants.add(new Variant(model));
        }

        blockModelGenerator.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block,
                new MultiVariant(variants.build())));
    }

    private static BlockModelDefinitionGenerator createSolarPanelBlockModelDefinitionCreator(SolarPanelBlock block) {
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Variant.SimpleModelState rotation = rotationFor(direction);
            for (boolean powered : List.of(false, true)) {
                String powerlessSuffix = powered ? "" : "_powerless";
                Identifier normalModelId = Industria.id("block/solar_panel" + powerlessSuffix);
                Identifier stairModelId = Industria.id("block/solar_panel_stair" + powerlessSuffix);

                generator.with(new ConditionBuilder()
                                .term(BlockStateProperties.HORIZONTAL_FACING, direction)
                                .term(SolarPanelBlock.ON_STAIR, false)
                                .term(SolarPanelBlock.POWERED, powered),
                        createWeightedVariant(normalModelId, rotation));
                generator.with(new ConditionBuilder()
                                .term(BlockStateProperties.HORIZONTAL_FACING, direction)
                                .term(SolarPanelBlock.ON_STAIR, true)
                                .term(SolarPanelBlock.POWERED, powered),
                        createWeightedVariant(stairModelId, rotationFor(direction.getOpposite())));
            }
        }

        return generator;
    }

    private static BlockModelDefinitionGenerator createPipeBlockModelDefinitionCreator(Block block, String name,
                                                                                         boolean separatedModels) {
        Identifier centerModelId = Industria.id("block/" + name + (separatedModels ? "_center" : "_dot"));
        Identifier continuousModelId = Industria.id("block/" + name + (separatedModels ? "_continuous" : ""));
        Identifier continuousUpModelId = Industria.id("block/" + name + (separatedModels ? "_continuous_up" : ""));
        Identifier continuousDownModelId = Industria.id("block/" + name + (separatedModels ? "_continuous_down" : ""));
        Variant.SimpleModelState upState = separatedModels
                ? Variant.SimpleModelState.DEFAULT
                : Variant.SimpleModelState.DEFAULT.withX(Quadrant.R270);
        Variant.SimpleModelState downState = separatedModels
                ? Variant.SimpleModelState.DEFAULT
                : Variant.SimpleModelState.DEFAULT.withX(Quadrant.R90);

        return MultiPartGenerator.multiPart(block)
                .with(createWeightedVariant(centerModelId, Variant.SimpleModelState.DEFAULT))
                .with(new ConditionBuilder().term(PipeBlock.NORTH, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(continuousModelId, Variant.SimpleModelState.DEFAULT))
                .with(new ConditionBuilder().term(PipeBlock.EAST, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(continuousModelId,
                                Variant.SimpleModelState.DEFAULT
                                        .withY(Quadrant.R90)))
                .with(new ConditionBuilder().term(PipeBlock.SOUTH, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(continuousModelId,
                                Variant.SimpleModelState.DEFAULT
                                        .withY(Quadrant.R180)))
                .with(new ConditionBuilder().term(PipeBlock.WEST, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(continuousModelId,
                                Variant.SimpleModelState.DEFAULT
                                        .withY(Quadrant.R270)))
                .with(new ConditionBuilder().term(PipeBlock.UP, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(continuousUpModelId, upState))
                .with(new ConditionBuilder().term(PipeBlock.DOWN, PipeBlock.ConnectorType.PIPE),
                        createWeightedVariant(continuousDownModelId, downState));
    }

    private static BlockModelDefinitionGenerator createConveyorBlockModelDefinitionCreator(BasicConveyorBlock block, String name) {
        Identifier straightModelId = Industria.id("block/" + name);
        Identifier upModelId = Industria.id("block/" + name + "_up");
        Identifier downModelId = Industria.id("block/" + name + "_down");
        Identifier turnLeftModelId = Industria.id("block/" + name + "_turn_left");
        Identifier turnRightModelId = Industria.id("block/" + name + "_turn_right");

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Variant.SimpleModelState state = rotationFor(direction);
            generator.with(new ConditionBuilder()
                            .term(BasicConveyorBlock.FACING, direction)
                            .term(BasicConveyorBlock.SHAPE, BasicConveyorBlock.ConveyorShape.STRAIGHT),
                    createWeightedVariant(straightModelId, state));
            generator.with(new ConditionBuilder()
                            .term(BasicConveyorBlock.FACING, direction)
                            .term(BasicConveyorBlock.SHAPE, BasicConveyorBlock.ConveyorShape.UP),
                    createWeightedVariant(upModelId, state));
            generator.with(new ConditionBuilder()
                            .term(BasicConveyorBlock.FACING, direction)
                            .term(BasicConveyorBlock.SHAPE, BasicConveyorBlock.ConveyorShape.DOWN),
                    createWeightedVariant(downModelId, state));
            generator.with(new ConditionBuilder()
                            .term(BasicConveyorBlock.FACING, direction)
                            .term(BasicConveyorBlock.SHAPE, BasicConveyorBlock.ConveyorShape.TURN_LEFT),
                    createWeightedVariant(turnLeftModelId, state));
            generator.with(new ConditionBuilder()
                            .term(BasicConveyorBlock.FACING, direction)
                            .term(BasicConveyorBlock.SHAPE, BasicConveyorBlock.ConveyorShape.TURN_RIGHT),
                    createWeightedVariant(turnRightModelId, state));
        }

        return generator;
    }

    private static BlockModelDefinitionGenerator createSplitterConveyorBlockModelDefinitionCreator(SplitterConveyorBlock block, String name) {
        Identifier modelId = Industria.id("block/" + name);

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            generator.with(new ConditionBuilder()
                            .term(SplitterConveyorBlock.FACING, direction),
                    createWeightedVariant(modelId, rotationFor(direction)));
        }

        return generator;
    }

    private static BlockModelDefinitionGenerator createFilterConveyorBlockModelDefinitionCreator(FilterConveyorBlock block, String name) {
        Identifier modelId = Industria.id("block/" + name);

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            generator.with(new ConditionBuilder()
                            .term(FilterConveyorBlock.FACING, direction),
                    createWeightedVariant(modelId, rotationFor(direction)));
        }

        return generator;
    }

    private static BlockModelDefinitionGenerator createMergerConveyorBlockModelDefinitionCreator(MergerConveyorBlock block, String name) {
        Identifier modelId = Industria.id("block/" + name);

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            generator.with(new ConditionBuilder()
                            .term(MergerConveyorBlock.FACING, direction),
                    createWeightedVariant(modelId, rotationFor(direction)));
        }

        return generator;
    }

    private static BlockModelDefinitionGenerator createFeederConveyorBlockModelDefinitionCreator(FeederConveyorBlock block, String name) {
        Identifier modelId = Industria.id("block/" + name);

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            generator.with(new ConditionBuilder()
                            .term(FeederConveyorBlock.FACING, direction),
                    createWeightedVariant(modelId, rotationFor(direction)));
        }

        return generator;
    }

    private static BlockModelDefinitionGenerator createAlternatorConveyorBlockModelDefinitionCreator(AlternatorConveyorBlock block, String name) {
        Identifier modelId = Industria.id("block/" + name);

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            generator.with(new ConditionBuilder()
                            .term(AlternatorConveyorBlock.FACING, direction),
                    createWeightedVariant(modelId, rotationFor(direction)));
        }

        return generator;
    }

    private static BlockModelDefinitionGenerator createHatchConveyorBlockModelDefinitionCreator(HatchConveyorBlock block, String name) {
        Identifier modelId = Industria.id("block/" + name);

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            generator.with(new ConditionBuilder()
                            .term(HatchConveyorBlock.FACING, direction),
                    createWeightedVariant(modelId, rotationFor(direction)));
        }

        return generator;
    }

    private static BlockModelDefinitionGenerator createSideInjectorConveyorBlockModelDefinitionCreator(SideInjectorConveyorBlock block, String name) {
        Identifier modelId = Industria.id("block/" + name);

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            generator.with(new ConditionBuilder()
                            .term(SideInjectorConveyorBlock.FACING, direction),
                    createWeightedVariant(modelId, rotationFor(direction)));
        }

        return generator;
    }

    private static BlockModelDefinitionGenerator createLadderConveyorBlockModelDefinitionCreator(LadderConveyorBlock block, String name) {
        Identifier modelId = Industria.id("block/" + name);
        Identifier topModelId = Industria.id("block/" + name + "_top");

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            generator.with(new ConditionBuilder()
                            .term(LadderConveyorBlock.FACING, direction)
                            .term(LadderConveyorBlock.LINE_POSITION, LadderConveyorBlock.LinePosition.BOTTOM)
                            .term(LadderConveyorBlock.UPWARD, true),
                    createWeightedVariant(modelId, rotationFor(direction).withX(Quadrant.R180)));
            generator.with(new ConditionBuilder()
                            .term(LadderConveyorBlock.FACING, direction)
                            .term(LadderConveyorBlock.LINE_POSITION, LadderConveyorBlock.LinePosition.MIDDLE)
                            .term(LadderConveyorBlock.UPWARD, true),
                    createWeightedVariant(modelId, rotationFor(direction).withX(Quadrant.R180)));
            generator.with(new ConditionBuilder()
                            .term(LadderConveyorBlock.FACING, direction)
                            .term(LadderConveyorBlock.LINE_POSITION, LadderConveyorBlock.LinePosition.TOP)
                            .term(LadderConveyorBlock.UPWARD, true),
                    createWeightedVariant(topModelId, rotationFor(direction).withX(Quadrant.R180)));
            generator.with(new ConditionBuilder()
                            .term(LadderConveyorBlock.FACING, direction)
                            .term(LadderConveyorBlock.LINE_POSITION, LadderConveyorBlock.LinePosition.SINGLE)
                            .term(LadderConveyorBlock.UPWARD, true),
                    createWeightedVariant(topModelId, rotationFor(direction).withX(Quadrant.R180)));
            generator.with(new ConditionBuilder()
                            .term(LadderConveyorBlock.FACING, direction)
                            .term(LadderConveyorBlock.LINE_POSITION, LadderConveyorBlock.LinePosition.BOTTOM)
                            .term(LadderConveyorBlock.UPWARD, false),
                    createWeightedVariant(modelId, rotationFor(direction.getOpposite())));
            generator.with(new ConditionBuilder()
                            .term(LadderConveyorBlock.FACING, direction)
                            .term(LadderConveyorBlock.LINE_POSITION, LadderConveyorBlock.LinePosition.MIDDLE)
                            .term(LadderConveyorBlock.UPWARD, false),
                    createWeightedVariant(modelId, rotationFor(direction.getOpposite())));
            generator.with(new ConditionBuilder()
                            .term(LadderConveyorBlock.FACING, direction)
                            .term(LadderConveyorBlock.LINE_POSITION, LadderConveyorBlock.LinePosition.TOP)
                            .term(LadderConveyorBlock.UPWARD, false),
                    createWeightedVariant(topModelId, rotationFor(direction.getOpposite())));
            generator.with(new ConditionBuilder()
                            .term(LadderConveyorBlock.FACING, direction)
                            .term(LadderConveyorBlock.LINE_POSITION, LadderConveyorBlock.LinePosition.SINGLE)
                            .term(LadderConveyorBlock.UPWARD, false),
                    createWeightedVariant(topModelId, rotationFor(direction.getOpposite())));
        }

        return generator;
    }

    private static BlockModelDefinitionGenerator createMagneticConveyorBlockModelDefinitionCreator(MagneticConveyorBlock block, String name) {
        Identifier modelId = Industria.id("block/" + name);

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            generator.with(new ConditionBuilder()
                            .term(MagneticConveyorBlock.FACING, direction),
                    createWeightedVariant(modelId, rotationFor(direction)));
        }

        return generator;
    }

    private static BlockModelDefinitionGenerator createDetectorConveyorBlockModelDefinitionCreator(DetectorConveyorBlock block) {
        Identifier modelId = Industria.id("block/conveyor");

        return createFacingOnlyConveyorBlockModelDefinitionCreator(block, modelId);
    }

    private static BlockModelDefinitionGenerator createDropChuteConveyorBlockModelDefinitionCreator(DropChuteConveyorBlock block, String name) {
        Identifier modelId = Industria.id("block/" + name);

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            generator.with(new ConditionBuilder()
                            .term(DropChuteConveyorBlock.FACING, direction),
                    createWeightedVariant(modelId, rotationFor(direction)));
        }

        return generator;
    }

    private static void registerFacingOnlyConveyor(BlockModelGenerators blockStateModelGenerator, Block block, Identifier modelId) {
        blockStateModelGenerator.blockStateOutput.accept(createFacingOnlyConveyorBlockModelDefinitionCreator(block, modelId));
    }

    private static BlockModelDefinitionGenerator createFacingOnlyConveyorBlockModelDefinitionCreator(Block block, Identifier modelId) {
        MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            generator.with(new ConditionBuilder()
                            .term(AbstractHorizontalConveyorBlock.FACING, direction),
                    createWeightedVariant(modelId, rotationFor(direction)));
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

    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        // Aluminium
        registerRandomBauxiteBlock(blockStateModelGenerator, ModBlocks.BAUXITE_ORE.get());
        blockStateModelGenerator.createTrivialCube(ModBlocks.RAW_BAUXITE_BLOCK.get());
        blockStateModelGenerator.createTrivialCube(ModBlocks.ALUMINIUM_BLOCK.get());

        // Silver
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.ARGENTITE_ORE.get(), "stone");
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.DEEPSLATE_ARGENTITE_ORE.get(), "deepslate");
        blockStateModelGenerator.createTrivialCube(ModBlocks.RAW_ARGENTITE_BLOCK.get());
        blockStateModelGenerator.createTrivialCube(ModBlocks.SILVER_BLOCK.get());

        // Lead
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.GALENA_ORE.get(), "stone");
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.DEEPSLATE_GALENA_ORE.get(), "deepslate");
        blockStateModelGenerator.createTrivialCube(ModBlocks.RAW_GALENA_BLOCK.get());
        blockStateModelGenerator.createTrivialCube(ModBlocks.LEAD_BLOCK.get());

        // Titanium
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.ILMENITE_ORE.get(), "stone");
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.DEEPSLATE_ILMENITE_ORE.get(), "deepslate");
        blockStateModelGenerator.createTrivialCube(ModBlocks.RAW_ILMENITE_BLOCK.get());
        blockStateModelGenerator.createTrivialCube(ModBlocks.TITANIUM_BLOCK.get());

        // Zinc
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.SPHALERITE_ORE.get(), "stone");
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.DEEPSLATE_SPHALERITE_ORE.get(), "deepslate");
        blockStateModelGenerator.createTrivialCube(ModBlocks.RAW_SPHALERITE_BLOCK.get());
        blockStateModelGenerator.createTrivialCube(ModBlocks.ZINC_BLOCK.get());

        // Cobalt
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.COBALTITE_ORE.get(), "stone");
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.DEEPSLATE_COBALTITE_ORE.get(), "deepslate");
        blockStateModelGenerator.createTrivialCube(ModBlocks.RAW_COBALTITE_BLOCK.get());
        blockStateModelGenerator.createTrivialCube(ModBlocks.COBALT_BLOCK.get());

        // Nickel
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.PENTLANDITE_ORE.get(), "stone");
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.DEEPSLATE_PENTLANDITE_ORE.get(), "deepslate");
        blockStateModelGenerator.createTrivialCube(ModBlocks.RAW_PENTLANDITE_BLOCK.get());
        blockStateModelGenerator.createTrivialCube(ModBlocks.NICKEL_BLOCK.get());

        // Iridium
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.IRIDIUM_ORE.get(), "stone");
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.DEEPSLATE_IRIDIUM_ORE.get(), "deepslate");
        blockStateModelGenerator.createTrivialCube(ModBlocks.IRIDIUM_BLOCK.get());

        // Tin
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.CASSITERITE_ORE.get(), "stone");
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.DEEPSLATE_CASSITERITE_ORE.get(), "deepslate");
        blockStateModelGenerator.createTrivialCube(ModBlocks.RAW_CASSITERITE_BLOCK.get());
        blockStateModelGenerator.createTrivialCube(ModBlocks.TIN_BLOCK.get());

        // Pyrite
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.NETHER_PYRITE_ORE.get(), "nether");
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.END_PYRITE_ORE.get(), "end");
        blockStateModelGenerator.createTrivialCube(ModBlocks.PYRITE_BLOCK.get());

        // Steel
        blockStateModelGenerator.createTrivialCube(ModBlocks.STEEL_BLOCK.get());

        // Quartz
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.QUARTZ_ORE.get(), "stone");
        registerSimpleOreBlock(blockStateModelGenerator, ModBlocks.DEEPSLATE_QUARTZ_ORE.get(), "deepslate");

        blockStateModelGenerator.createFurnace(ModBlocks.ALLOY_FURNACE.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockStateModelGenerator.createFurnace(ModBlocks.THERMAL_GENERATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        createBattery(blockStateModelGenerator, ModBlocks.BASIC_BATTERY.get());
        createBattery(blockStateModelGenerator, ModBlocks.ADVANCED_BATTERY.get());
        createBattery(blockStateModelGenerator, ModBlocks.ELITE_BATTERY.get());
        createBattery(blockStateModelGenerator, ModBlocks.ULTIMATE_BATTERY.get());
        createBattery(blockStateModelGenerator, ModBlocks.CREATIVE_BATTERY.get());
        blockStateModelGenerator.createFurnace(ModBlocks.COMBUSTION_GENERATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockStateModelGenerator.blockStateOutput.accept(createSolarPanelBlockModelDefinitionCreator(ModBlocks.SOLAR_PANEL.get()));
        blockStateModelGenerator.registerSimpleItemModel(ModBlocks.SOLAR_PANEL.get(), Industria.id("block/solar_panel"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.ADVANCED_SOLAR_PANEL.get(),
                Industria.id("block/advanced_solar_panel"));
        blockStateModelGenerator.createNonTemplateModelBlock(ModFluids.CRUDE_OIL.block().get());
        blockStateModelGenerator.createNonTemplateModelBlock(ModFluids.DIRTY_SODIUM_ALUMINATE.block().get());
        blockStateModelGenerator.createNonTemplateModelBlock(ModFluids.SODIUM_ALUMINATE.block().get());
        blockStateModelGenerator.createNonTemplateModelBlock(ModFluids.MOLTEN_ALUMINIUM.block().get());
        blockStateModelGenerator.createNonTemplateModelBlock(ModFluids.MOLTEN_CRYOLITE.block().get());
        blockStateModelGenerator.createNonTemplateModelBlock(ModFluids.FORMIC_ACID.block().get());
        blockStateModelGenerator.createNonTemplateModelBlock(ModFluids.DILUTED_FORMIC_ACID.block().get());
        blockStateModelGenerator.createNonTemplateModelBlock(ModBlocks.DRILL_TUBE.get());
        blockStateModelGenerator.registerSimpleItemModel(ModBlocks.DRILL_TUBE.get(), Industria.id("block/drill_tube"));
        blockStateModelGenerator.createFurnace(ModBlocks.ELECTRIC_FURNACE.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockStateModelGenerator.createNonTemplateModelBlock(ModBlocks.INDUCTION_HEATER.get());
        blockStateModelGenerator.createNonTemplateHorizontalBlock(ModBlocks.FLUID_PUMP.get());
        blockStateModelGenerator.createNonTemplateModelBlock(ModBlocks.FLUID_TANK.get());
        blockStateModelGenerator.createNonTemplateHorizontalBlock(ModBlocks.TREE_TAP.get());

        // Multiblock controllers and segments should have a visible fallback block model while unformed.
        registerCustomCube(blockStateModelGenerator, ModBlocks.ARC_FURNACE.get(), Industria.id("block/steel_block"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.CENTRIFUGAL_CONCENTRATOR.get(), Industria.id("block/steel_block"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.CLARIFIER.get(), Industria.id("block/clarifier"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.CRYSTALLIZER.get(), Industria.id("block/crystallizer"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.DIGESTER.get(), Industria.id("block/digester"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.DRILL.get(), Industria.id("block/drill_frame"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.ELECTROLYZER.get(), Industria.id("block/electrolyzer"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.MIXER.get(), Industria.id("block/mixer"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.OIL_PUMP_JACK.get(), Industria.id("block/oil_pump_jack"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.ROTARY_KILN.get(), Industria.id("block/rotary_kiln"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.ROTARY_KILN_CONTROLLER.get(), Industria.id("block/rotary_kiln"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.SHAKING_TABLE.get(), Industria.id("block/shaking_table"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.UPGRADE_STATION.get(), Industria.id("block/upgrade_station"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.AGITATOR.get(), Industria.id("block/agitator"));
        registerCustomCube(blockStateModelGenerator, ModBlocks.DISTILLATION_TOWER.get(), Industria.id("block/steel_block"));

        registerPipe(blockStateModelGenerator, ModBlocks.CABLE.get(), "cable");
        registerPipe(blockStateModelGenerator, ModBlocks.FLUID_PIPE.get(), "fluid_pipe");
        registerPipe(blockStateModelGenerator, ModBlocks.SLURRY_PIPE.get(), "slurry_pipe");
        registerPipe(blockStateModelGenerator, ModBlocks.GAS_PIPE.get(), "gas_pipe");
        registerConveyor(blockStateModelGenerator);
        registerSplitterConveyor(blockStateModelGenerator);
        registerMergerConveyor(blockStateModelGenerator);
        registerAlternatorConveyor(blockStateModelGenerator);
        registerFeederConveyor(blockStateModelGenerator);
        registerHatchConveyor(blockStateModelGenerator);
        registerSideInjectorConveyor(blockStateModelGenerator);
        registerLadderConveyor(blockStateModelGenerator);
        blockStateModelGenerator.registerSimpleItemModel(ModBlocks.SIDE_INJECTOR_CONVEYOR.get(), Industria.id("block/side_injector_conveyor"));
        blockStateModelGenerator.registerSimpleItemModel(ModBlocks.LADDER_CONVEYOR.get(), Industria.id("block/ladder_conveyor"));
        registerFilterConveyor(blockStateModelGenerator);
        registerMagneticConveyor(blockStateModelGenerator);
        registerDetectorConveyor(blockStateModelGenerator);
        blockStateModelGenerator.registerSimpleItemModel(ModBlocks.DETECTOR_CONVEYOR.get(), Industria.id("block/conveyor"));
        registerDropChuteConveyor(blockStateModelGenerator);
        blockStateModelGenerator.registerSimpleItemModel(ModBlocks.DROP_CHUTE_CONVEYOR.get(), Industria.id("block/drop_chute_conveyor"));
        registerCountConveyor(blockStateModelGenerator);
        blockStateModelGenerator.registerSimpleItemModel(ModBlocks.COUNT_CONVEYOR.get(), Industria.id("block/conveyor"));
        registerDelayConveyor(blockStateModelGenerator);
        blockStateModelGenerator.registerSimpleItemModel(ModBlocks.DELAY_CONVEYOR.get(), Industria.id("block/conveyor"));
        registerContainmentConveyor(blockStateModelGenerator);
        blockStateModelGenerator.registerSimpleItemModel(ModBlocks.CONTAINMENT_CONVEYOR.get(), Industria.id("block/conveyor"));
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

    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        itemModelGenerator.itemModelOutput.accept(ModItems.SIMPLE_DRILL_HEAD.get(), ItemModelUtils.specialModel(ModelLocationUtils.getModelLocation(ModItems.SIMPLE_DRILL_HEAD.get()), new DrillHeadItemRenderer.Unbaked()));
        itemModelGenerator.itemModelOutput.accept(ModItems.BLOCK_BUILDER_DRILL_HEAD.get(), ItemModelUtils.specialModel(ModelLocationUtils.getModelLocation(ModItems.BLOCK_BUILDER_DRILL_HEAD.get()), new DrillHeadItemRenderer.Unbaked()));
        itemModelGenerator.itemModelOutput.accept(ModItems.SEISMIC_SCANNER.get(), ItemModelUtils.plainModel(BuiltinEntityModelBuilder.getItemModelLocation(ModItems.SEISMIC_SCANNER.get())));
        itemModelGenerator.itemModelOutput.accept(ModItems.MULTIBLOCK_EXPORTER.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(ModItems.MULTIBLOCK_EXPORTER.get())));
        itemModelGenerator.itemModelOutput.accept(ModItems.EMPTY_MOB_JAR.get(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(ModItems.EMPTY_MOB_JAR.get())));
        itemModelGenerator.itemModelOutput.accept(ModBlocks.WELLHEAD.get().asItem(), ItemModelUtils.plainModel(Industria.id("block/invisible")));

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.ADVANCED_SOLAR_PANEL.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(
                        AdvancedSolarPanelModel.LAYER_LOCATION,
                        AdvancedSolarPanelModel.TEXTURE_LOCATION
                ),
                BuiltinEntityModelBuilder.defaultBlock());

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.WIND_TURBINE.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(WindTurbineModel.LAYER_LOCATION, WindTurbineModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                .copyModifyGui(displaySettings -> {
                    displaySettings.setTranslation(-2.5f, -2.5f, 0);
                    displaySettings.setScale(0.5f, 0.5f, 0.5f);
                }));

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.OIL_PUMP_JACK.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(OilPumpJackModel.LAYER_LOCATION, OilPumpJackModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                .copyModifyGui(displaySettings -> {
                    displaySettings.setTranslation(-1.5f, -2.75f, 0);
                    displaySettings.setScale(0.275f, 0.275f, 0.275f);
                }));

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.DRILL.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(DrillFrameModel.LAYER_LOCATION, DrillFrameModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                .copyModifyGui(displaySettings -> {
                    displaySettings.setTranslation(-2.5f, -2.5f, 0);
                    displaySettings.setScale(0.5f, 0.5f, 0.5f);
                }));

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.UPGRADE_STATION.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(UpgradeStationModel.LAYER_LOCATION, UpgradeStationModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                .copyModifyGui(displaySettings -> {
                    displaySettings.setTranslation(-1.5f, -2.75f, 0);
                    displaySettings.setScale(0.275f, 0.275f, 0.275f);
                }));

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.MOTOR.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(MotorModel.LAYER_LOCATION, MotorModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock());

        BuiltinEntityModelBuilder.write(itemModelGenerator, ModItems.SEISMIC_SCANNER.get());
        BuiltinEntityModelBuilder.write(itemModelGenerator, (ItemLike) ModItems.SIMPLE_DRILL_HEAD.get(),
                Industria.id("block/simple_drill_head"),
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyAll(displaySettings ->
                                displaySettings.rotate(180, 180, 0))
                        .copyModifyGui(displaySettings ->
                                displaySettings.rotate(0, 180, 0)));

        BuiltinEntityModelBuilder.write(itemModelGenerator, (ItemLike) ModItems.BLOCK_BUILDER_DRILL_HEAD.get(),
                Industria.id("block/simple_drill_head"),
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyAll(displaySettings ->
                                displaySettings.rotate(180, 180, 0))
                        .copyModifyGui(displaySettings ->
                                displaySettings.rotate(0, 180, 0)));

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.MIXER.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(MixerModel.LAYER_LOCATION, MixerModel.CLOSED_TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.DIGESTER.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(DigesterModel.LAYER_LOCATION, DigesterModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.CLARIFIER.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(ClarifierModel.LAYER_LOCATION, ClarifierModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.CRYSTALLIZER.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(CrystallizerModel.LAYER_LOCATION, CrystallizerModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        generateSpecialItemModel(itemModelGenerator, ModItems.ROTARY_KILN.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(RotaryKilnModel.LAYER_LOCATION, RotaryKilnModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-2.5f, -2.75f, 0);
                            displaySettings.setScale(0.1375f, 0.1375f, 0.1375f);
                        }));

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.ELECTROLYZER.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(ElectrolyzerModel.LAYER_LOCATION, ElectrolyzerModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.SHAKING_TABLE.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(ShakingTableModel.LAYER_LOCATION, ShakingTableModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.CENTRIFUGAL_CONCENTRATOR.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(CentrifugalConcentratorModel.LAYER_LOCATION, CentrifugalConcentratorModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.ARC_FURNACE.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(ArcFurnaceModel.LAYER_LOCATION, ArcFurnaceModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.TREE_TAP.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(TreeTapModel.LAYER_LOCATION, TreeTapModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
                        }));

//        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.AGITATOR.get(),
//                new IndustriaBlockEntityItemRenderer.Unbaked(AgitatorModel.LAYER_LOCATION, AgitatorModel.TEXTURE_LOCATION),
//                BuiltinEntityModelBuilder.defaultBlock()
//                        .copyModifyGui(displaySettings -> {
//                            displaySettings.setTranslation(-1.5f, -2.75f, 0);
//                            displaySettings.setScale(0.275f, 0.275f, 0.275f);
//                        }));
        generateSpecialBlockItemModel(itemModelGenerator, ModBlocks.DISTILLATION_TOWER.get(),
                new IndustriaBlockEntityItemRenderer.Unbaked(DistillationTowerModel.LAYER_LOCATION, DistillationTowerModel.TEXTURE_LOCATION),
                BuiltinEntityModelBuilder.defaultBlock()
                        .copyModifyGui(displaySettings -> {
                            displaySettings.setTranslation(-2.0f, -2.75f, 0);
                            displaySettings.setScale(0.225f, 0.225f, 0.225f);
                        }));

        final List<Item> exclusionList = List.of(
                ModItems.SEISMIC_SCANNER.get(),
                ModItems.SIMPLE_DRILL_HEAD.get(),
                ModItems.BLOCK_BUILDER_DRILL_HEAD.get(),
                ModItems.MULTIBLOCK_EXPORTER.get(),
                ModItems.EMPTY_MOB_JAR.get(),
                ModWoodSets.RUBBER.boatItem().get(),
                ModWoodSets.RUBBER.chestBoatItem().get()
        );

        final Set<Item> simpleBlockItemExclusionList = Set.of(
                ModBlocks.ADVANCED_BATTERY.get().asItem(),
                ModBlocks.ADVANCED_SOLAR_PANEL.get().asItem(),
                ModBlocks.ARC_FURNACE.get().asItem(),
                ModBlocks.BASIC_BATTERY.get().asItem(),
                ModBlocks.CENTRIFUGAL_CONCENTRATOR.get().asItem(),
                ModBlocks.CLARIFIER.get().asItem(),
                ModBlocks.CONTAINMENT_CONVEYOR.get().asItem(),
                ModBlocks.COUNT_CONVEYOR.get().asItem(),
                ModBlocks.CREATIVE_BATTERY.get().asItem(),
                ModBlocks.CRYSTALLIZER.get().asItem(),
                ModBlocks.DELAY_CONVEYOR.get().asItem(),
                ModBlocks.DETECTOR_CONVEYOR.get().asItem(),
                ModBlocks.DIGESTER.get().asItem(),
                ModBlocks.DISTILLATION_TOWER.get().asItem(),
                ModBlocks.DRILL.get().asItem(),
                ModBlocks.DRILL_TUBE.get().asItem(),
                ModBlocks.DROP_CHUTE_CONVEYOR.get().asItem(),
                ModBlocks.ELECTROLYZER.get().asItem(),
                ModBlocks.ELITE_BATTERY.get().asItem(),
                ModBlocks.LADDER_CONVEYOR.get().asItem(),
                ModBlocks.MIXER.get().asItem(),
                ModBlocks.MOTOR.get().asItem(),
                ModBlocks.OIL_PUMP_JACK.get().asItem(),
                ModBlocks.SHAKING_TABLE.get().asItem(),
                ModBlocks.SIDE_INJECTOR_CONVEYOR.get().asItem(),
                ModBlocks.SOLAR_PANEL.get().asItem(),
                ModBlocks.TREE_TAP.get().asItem(),
                ModBlocks.ULTIMATE_BATTERY.get().asItem(),
                ModBlocks.UPGRADE_STATION.get().asItem(),
                ModBlocks.WELLHEAD.get().asItem(),
                ModBlocks.WIND_TURBINE.get().asItem(),
                ModWoodSets.RUBBER.button().item().get(),
                ModWoodSets.RUBBER.door().item().get(),
                ModWoodSets.RUBBER.fence().item().get(),
                ModWoodSets.RUBBER.hangingSignItem().get(),
                ModWoodSets.RUBBER.leaves().item().get(),
                ModWoodSets.RUBBER.log().item().get(),
                ModWoodSets.RUBBER.sapling().item().get(),
                ModWoodSets.RUBBER.signItem().get(),
                ModWoodSets.RUBBER.slab().item().get(),
                ModWoodSets.RUBBER.stairs().item().get(),
                ModWoodSets.RUBBER.strippedLog().item().get(),
                ModWoodSets.RUBBER.strippedWood().item().get(),
                ModWoodSets.RUBBER.trapdoor().item().get(),
                ModWoodSets.RUBBER.wood().item().get(),
                ModItems.ROTARY_KILN.get()
        );

        BuiltInRegistries.ITEM.listElementIds().filter(key -> key.identifier().getNamespace().equals(Industria.MOD_ID))
                .map(BuiltInRegistries.ITEM::getValue)
                .filter(Objects::nonNull)
                .filter(entry -> entry instanceof BlockItem)
                .filter(entry -> !simpleBlockItemExclusionList.contains(entry))
                .forEach(entry -> {
                    Identifier itemId = BuiltInRegistries.ITEM.getKey(entry);
                    Identifier modelId = entry == ModBlocks.CRUSHER.get().asItem()
                            ? itemId.withPath(path -> "item/" + path)
                            : BuiltInRegistries.BLOCK.getKey(((BlockItem) entry).getBlock()).withPath(path -> "block/" + path);

                    itemModelGenerator.itemModelOutput.accept(entry, ItemModelUtils.plainModel(modelId));
                });

        BuiltInRegistries.ITEM.listElementIds().filter(key -> key.identifier().getNamespace().equals(Industria.MOD_ID))
                .map(BuiltInRegistries.ITEM::getValue)
                .filter(entry -> !(entry instanceof BlockItem))
                .filter(entry -> !exclusionList.contains(entry))
                .filter(Objects::nonNull)
                .forEach(entry -> itemModelGenerator.generateFlatItem(entry, FLAT_ITEM));
    }

    private void createBattery(BlockModelGenerators blockStateModelGenerator, BatteryBlock block) {
        blockStateModelGenerator.woodProvider(block).logWithHorizontal(block);
    }

    private static void generateSpecialBlockItemModel(ItemModelGenerators itemModelGenerator, Block block,
                                                      IndustriaBlockEntityItemRenderer.Unbaked renderer,
                                                      BuiltinEntityModelBuilder.DefaultDisplaySettingsBuilder displaySettings) {
        generateSpecialItemModel(itemModelGenerator, block.asItem(), renderer, displaySettings);
    }

    private static void generateSpecialItemModel(ItemModelGenerators itemModelGenerator, Item item,
                                                 IndustriaBlockEntityItemRenderer.Unbaked renderer,
                                                 BuiltinEntityModelBuilder.DefaultDisplaySettingsBuilder displaySettings) {
        itemModelGenerator.itemModelOutput.accept(item,
                ItemModelUtils.specialModel(BuiltinEntityModelBuilder.getItemModelLocation(item), renderer));
        BuiltinEntityModelBuilder.write(itemModelGenerator, (ItemLike) item, textureToSprite(renderer.texture()), displaySettings);
    }

    private static Identifier textureToSprite(Identifier texture) {
        return texture.withPath(path -> path.replaceFirst("^textures/", "").replaceFirst("\\.png$", ""));
    }
}
