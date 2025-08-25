package dev.turtywurty.industria.datagen;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.gas.GasStack;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.datagen.builder.*;
import dev.turtywurty.industria.init.*;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.Blocks;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class IndustriaRecipeProvider extends FabricRecipeProvider {
    public IndustriaRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter exporter) {
        return new RecipeGenerator(wrapperLookup, exporter) {
            @Override
            public void generate() {
                RegistryEntryLookup<Item> itemLookup = wrapperLookup.getOrThrow(RegistryKeys.ITEM);

                for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
                    WoodSetDatagen.generateRecipes(woodSet, this, exporter, itemLookup);
                }

                createShaped(RecipeCategory.MISC, BlockInit.ALLOY_FURNACE)
                        .pattern("AAA")
                        .pattern("ABA")
                        .pattern("AAA")
                        .input('A', ConventionalItemTags.STORAGE_BLOCKS_COPPER)
                        .input('B', ConventionalItemTags.STORAGE_BLOCKS_IRON)
                        .criterion(hasTag(ConventionalItemTags.STORAGE_BLOCKS_COPPER), conditionsFromTag(ConventionalItemTags.STORAGE_BLOCKS_COPPER))
                        .criterion(hasTag(ConventionalItemTags.STORAGE_BLOCKS_IRON), conditionsFromTag(ConventionalItemTags.STORAGE_BLOCKS_IRON))
                        .offerTo(exporter);

                createShaped(RecipeCategory.MISC, BlockInit.THERMAL_GENERATOR)
                        .pattern("ABA")
                        .pattern("CDC")
                        .pattern("AEA")
                        .input('A', ConventionalItemTags.STORAGE_BLOCKS_IRON)
                        .input('B', ConventionalItemTags.PLAYER_WORKSTATIONS_FURNACES)
                        .input('C', ConventionalItemTags.STORAGE_BLOCKS_REDSTONE)
                        .input('D', ConventionalItemTags.STORAGE_BLOCKS_COAL)
                        .input('E', Blocks.IRON_BARS)
                        .criterion(hasTag(ConventionalItemTags.STORAGE_BLOCKS_IRON), conditionsFromTag(ConventionalItemTags.STORAGE_BLOCKS_IRON))
                        .criterion(hasTag(ConventionalItemTags.PLAYER_WORKSTATIONS_FURNACES), conditionsFromTag(ConventionalItemTags.PLAYER_WORKSTATIONS_FURNACES))
                        .criterion(hasTag(ConventionalItemTags.STORAGE_BLOCKS_REDSTONE), conditionsFromTag(ConventionalItemTags.STORAGE_BLOCKS_REDSTONE))
                        .criterion(hasTag(ConventionalItemTags.STORAGE_BLOCKS_COAL), conditionsFromTag(ConventionalItemTags.STORAGE_BLOCKS_COAL))
                        .criterion(hasItem(Blocks.IRON_BARS), conditionsFromItem(Blocks.IRON_BARS))
                        .offerTo(exporter);

                offerAlloySmelting(exporter, RecipeCategory.MISC,
                        new IndustriaIngredient(itemLookup.getOrThrow(ConventionalItemTags.IRON_INGOTS), 1),
                        new IndustriaIngredient(itemLookup.getOrThrow(ItemTags.COALS), 4),
                        ItemInit.STEEL_INGOT.getDefaultStack(),
                        400);

                offerCrusher(exporter, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.STONE),
                        new OutputItemStack(Items.COBBLESTONE, 1, 1),
                        new OutputItemStack(Items.GRAVEL, 1, 0.25F),
                        10,
                        "stone");

                offerCrusher(exporter, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.COBBLESTONE),
                        new OutputItemStack(Items.GRAVEL, 1, 1),
                        new OutputItemStack(Items.FLINT, 1, 0.1F),
                        10,
                        "cobblestone");

                offerCrusher(exporter, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.GRAVEL),
                        new OutputItemStack(Items.SAND, 1, 1),
                        new OutputItemStack(Items.FLINT, 1, 0.1F),
                        10,
                        "gravel");

                offerCrusher(exporter, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.SAND),
                        new OutputItemStack(Items.GUNPOWDER, 1, 0.35F),
                        OutputItemStack.EMPTY,
                        10,
                        "sand");

                offerCrusher(exporter, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE),
                        new OutputItemStack(Items.COAL, 1, 1),
                        new OutputItemStack(Items.COAL, 1, 0.15F),
                        10,
                        "coal_ore");

                offerCrusher(exporter, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE),
                        new OutputItemStack(Items.REDSTONE, UniformIntProvider.create(2, 5), 1),
                        new OutputItemStack(Items.REDSTONE, 1, 0.15F),
                        10,
                        "redstone_ore");

                offerCrusher(exporter, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE),
                        new OutputItemStack(Items.LAPIS_LAZULI, UniformIntProvider.create(1, 4), 1),
                        new OutputItemStack(Items.LAPIS_LAZULI, UniformIntProvider.create(1, 2), 0.15F),
                        10,
                        "lapis_ore");

                offerCrusher(exporter, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE),
                        new OutputItemStack(Items.DIAMOND, 1, 1),
                        new OutputItemStack(Items.DIAMOND, 1, 0.15F),
                        10,
                        "diamond_ore");

                offerCrusher(exporter, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE),
                        new OutputItemStack(Items.EMERALD, 1, 1),
                        new OutputItemStack(Items.EMERALD, 1, 0.15F),
                        10,
                        "emerald_ore");

                offerCrusher(exporter, RecipeCategory.MISC,
                        new IndustriaIngredient(itemLookup.getOrThrow(ConventionalItemTags.QUARTZ_ORES), 1),
                        new OutputItemStack(Items.QUARTZ, 1, 1),
                        new OutputItemStack(Items.QUARTZ, UniformIntProvider.create(1, 3), 0.15F),
                        10,
                        "quartz_ore");

                offerCrusher(exporter, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.NETHER_GOLD_ORE),
                        new OutputItemStack(Items.GOLD_NUGGET, UniformIntProvider.create(2, 6), 1),
                        new OutputItemStack(Items.GOLD_NUGGET, UniformIntProvider.create(1, 3), 0.15F),
                        10,
                        "nether_gold_ore");

//        offerCrusher(exporter, RecipeCategory.MISC,
//                new IndustriaIngredient(itemLookup.getOrThrow(ConventionalItemTags.IRON_RAW_MATERIALS), 1),
//                ItemInit.IRON_DUST.getDefaultStack(),
//                1.0F,
//                ItemInit.IRON_DUST.getDefaultStack(),
//                0.1F,
//                200);

//        offerCrusher(exporter, RecipeCategory.MISC,
//                new IndustriaIngredient(itemLookup.getOrThrow(ConventionalItemTags.GOLD_ORES), 1),
//                ItemInit.GOLD_DUST.getDefaultStack(),
//                1.0F,
//                ItemInit.GOLD_DUST.getDefaultStack(),
//                0.1F,
//                200);

//        offerCrusher(exporter, RecipeCategory.MISC,
//                new IndustriaIngredient(itemLookup.getOrThrow(ConventionalItemTags.COPPER_RAW_MATERIALS), 1),
//                ItemInit.COPPER_DUST.getDefaultStack(),
//                1.0F,
//                ItemInit.COPPER_DUST.getDefaultStack(),
//                0.1F,
//                200);

                createShaped(RecipeCategory.MISC, BlockInit.CABLE, 8)
                        .pattern("III")
                        .pattern("IRI")
                        .pattern("III")
                        .input('I', ConventionalItemTags.IRON_INGOTS)
                        .input('R', ConventionalItemTags.REDSTONE_DUSTS)
                        .criterion(hasTag(ConventionalItemTags.IRON_INGOTS), conditionsFromTag(ConventionalItemTags.IRON_INGOTS))
                        .criterion(hasTag(ConventionalItemTags.REDSTONE_DUSTS), conditionsFromTag(ConventionalItemTags.REDSTONE_DUSTS))
                        .offerTo(exporter);

                offerReversibleCompactingRecipes(RecipeCategory.MISC, ItemInit.ALUMINIUM_INGOT, RecipeCategory.BUILDING_BLOCKS, BlockInit.ALUMINIUM_BLOCK);
                offerReversibleCompactingRecipes(RecipeCategory.MISC, ItemInit.TIN_INGOT, RecipeCategory.BUILDING_BLOCKS, BlockInit.TIN_BLOCK);
                offerReversibleCompactingRecipes(RecipeCategory.MISC, ItemInit.ZINC_INGOT, RecipeCategory.BUILDING_BLOCKS, BlockInit.ZINC_BLOCK);

                offerReversibleCompactingRecipes(RecipeCategory.MISC, ItemInit.ALUMINIUM_NUGGET, RecipeCategory.MISC, ItemInit.ALUMINIUM_INGOT, "aluminium_nugget_to_ingot", null, "aluminium_ingot_to_nugget", null);
                offerReversibleCompactingRecipes(RecipeCategory.MISC, ItemInit.TIN_NUGGET, RecipeCategory.MISC, ItemInit.TIN_INGOT, "tin_nugget_to_ingot", null, "tin_ingot_to_nugget", null);
                offerReversibleCompactingRecipes(RecipeCategory.MISC, ItemInit.ZINC_NUGGET, RecipeCategory.MISC, ItemInit.ZINC_INGOT, "zinc_nugget_to_ingot", null, "zinc_ingot_to_nugget", null);

                offerReversibleCompactingRecipes(RecipeCategory.MISC, ItemInit.BAUXITE, RecipeCategory.BUILDING_BLOCKS, BlockInit.RAW_BAUXITE_BLOCK);
                offerReversibleCompactingRecipes(RecipeCategory.MISC, ItemInit.CASSITERITE, RecipeCategory.BUILDING_BLOCKS, BlockInit.RAW_CASSITERITE_BLOCK);
                offerReversibleCompactingRecipes(RecipeCategory.MISC, ItemInit.SPHALERITE, RecipeCategory.BUILDING_BLOCKS, BlockInit.RAW_SPHALERITE_BLOCK);

                List<ItemConvertible> aluminiumOres = List.of(BlockInit.BAUXITE_ORE, BlockInit.DEEPSLATE_BAUXITE_ORE, ItemInit.BAUXITE);
                List<ItemConvertible> tinOres = List.of(BlockInit.CASSITERITE_ORE, BlockInit.DEEPSLATE_CASSITERITE_ORE, ItemInit.CASSITERITE);
                List<ItemConvertible> zincOres = List.of(BlockInit.SPHALERITE_ORE, BlockInit.DEEPSLATE_SPHALERITE_ORE, ItemInit.SPHALERITE);

                offerSmelting(aluminiumOres, RecipeCategory.MISC, ItemInit.ALUMINIUM_INGOT, 0.7F, 200, "aluminium_ingot");
                offerSmelting(tinOres, RecipeCategory.MISC, ItemInit.TIN_INGOT, 0.7F, 200, "tin_ingot");
                offerSmelting(zincOres, RecipeCategory.MISC, ItemInit.ZINC_INGOT, 0.7F, 200, "zinc_ingot");

                offerBlasting(aluminiumOres, RecipeCategory.MISC, ItemInit.ALUMINIUM_INGOT, 0.7F, 100, "aluminium_ingot");
                offerBlasting(tinOres, RecipeCategory.MISC, ItemInit.TIN_INGOT, 0.7F, 100, "tin_ingot");
                offerBlasting(zincOres, RecipeCategory.MISC, ItemInit.ZINC_INGOT, 0.7F, 100, "zinc_ingot");

                offerMixer(exporter, RecipeCategory.MISC, List.of(
                                new IndustriaIngredient(4, ItemInit.BAUXITE),
                                new IndustriaIngredient(1, ItemInit.SODIUM_HYDROXIDE)),
                        new FluidStack(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET),
                        170, 180,
                        OutputItemStack.EMPTY,
                        new SlurryStack(SlurryVariant.of(SlurryInit.BAUXITE_SLURRY), FluidConstants.BUCKET),
                        200, "bauxite_to_bauxite_slurry");

                offerDigester(exporter, new SlurryStack(SlurryVariant.of(SlurryInit.BAUXITE_SLURRY), FluidConstants.BUCKET),
                        new FluidStack(FluidVariant.of(FluidInit.DIRTY_SODIUM_ALUMINATE.still()), FluidConstants.BOTTLE),
                        200, "bauxite_to_dirty_sodium_aluminate");

                offerClarifier(exporter, new FluidStack(FluidVariant.of(FluidInit.DIRTY_SODIUM_ALUMINATE.still()), FluidConstants.BUCKET),
                        new FluidStack(FluidVariant.of(FluidInit.SODIUM_ALUMINATE.still()), FluidConstants.BOTTLE),
                        new OutputItemStack(ItemInit.RED_MUD, UniformIntProvider.create(1, 3), 1),
                        500, "dirty_sodium_aluminate_to_sodium_aluminate");

                offerCrystallizerRecipe(exporter, new FluidStack(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET * 5),
                        new FluidStack(FluidVariant.of(FluidInit.SODIUM_ALUMINATE.still()), FluidConstants.BUCKET),
                        new IndustriaIngredient(1, ItemInit.ALUMINIUM_HYDROXIDE),
                        new OutputItemStack(ItemInit.ALUMINIUM_HYDROXIDE, 8, 1),
                        new OutputItemStack(ItemInit.SODIUM_CARBONATE, UniformIntProvider.create(8, 16), 0.75F),
                        false, 5, 1000, "aluminium_hydroxide");

                offerRotaryKilnRecipe(exporter,
                        new IndustriaIngredient(1, ItemInit.ALUMINIUM_HYDROXIDE),
                        new OutputItemStack(ItemInit.ALUMINA, 1, 1),
                        1200);

                offerElectrolyzerRecipe(exporter,
                        new IndustriaIngredient(3, ItemInit.ALUMINA),
                        new IndustriaIngredient(1, ItemInit.CARBON_ROD),
                        new IndustriaIngredient(1, Items.COAL),
                        new IndustriaIngredient(9, ItemInit.CRYOLITE),
                        new FluidStack(FluidVariant.of(FluidInit.MOLTEN_CRYOLITE.still()), FluidConstants.BUCKET),
                        new FluidStack(FluidVariant.of(FluidInit.MOLTEN_ALUMINIUM.still()), FluidConstants.BUCKET * 2),
                        new GasStack(GasInit.CARBON_DIOXIDE, FluidConstants.INGOT),
                        2_000, 10_000, 1_000);

                offerCrusher(exporter, RecipeCategory.MISC,
                        new IndustriaIngredient(1, ItemInit.CASSITERITE),
                        new OutputItemStack(ItemInit.CRUSHED_CASSITERITE, 2, 1),
                        new OutputItemStack(ItemInit.CRUSHED_CASSITERITE, 1, (3 / 10F)),
                        100, "raw_cassiterite");

                offerShakingTableRecipe(exporter,
                        new IndustriaIngredient(1, ItemInit.CRUSHED_CASSITERITE),
                        new OutputItemStack(ItemInit.CASSITERITE_CONCENTRATE, 1, 1),
                        new SlurryStack(SlurryVariant.of(SlurryInit.CLAY_SLURRY), FluidConstants.BUCKET / 2),
                        200, 4, RecipeCategory.MISC);

                offerCentrifugalConcentratorRecipe(exporter,
                        new IndustriaIngredient(1, ItemInit.CASSITERITE_CONCENTRATE),
                        new OutputItemStack(ItemInit.CASSITERITE_CONCENTRATE, 1, 1),
                        new SlurryStack(SlurryVariant.of(SlurryInit.CLAY_SLURRY), FluidConstants.BUCKET / 4),
                        200, 500, RecipeCategory.MISC);
            }
        };
    }

    private static void offerAlloySmelting(RecipeExporter exporter, RecipeCategory category, IndustriaIngredient inputA, IndustriaIngredient inputB, ItemStack output, int smeltTime) {
        offerAlloySmelting(exporter, category, inputA, inputB, output, smeltTime, RecipeGenerator.getRecipeName(output.getItem()));
    }

    private static void offerAlloySmelting(RecipeExporter exporter, RecipeCategory category, IndustriaIngredient inputA, IndustriaIngredient inputB, ItemStack output, int smeltTime, String name) {
        new AlloyFurnaceRecipeBuilder(inputA, inputB, output, smeltTime, category).offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Industria.id("alloy_" + name)));
    }

    private static void offerCrusher(RecipeExporter exporter, RecipeCategory category, IndustriaIngredient input, OutputItemStack outputA, OutputItemStack outputB, int processTime, String name) {
        new CrusherRecipeBuilder(input, outputA, outputB, processTime, category).offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Industria.id("crusher_" + name)));
    }

    private static void offerMixer(RecipeExporter exporter, RecipeCategory category, List<IndustriaIngredient> inputs, @Nullable FluidStack inputFluid, int minTemperature, int maxTemperature, OutputItemStack output, @Nullable SlurryStack outputSlurry, int processTime, String name) {
        new MixerRecipeBuilder(inputs, inputFluid, minTemperature, maxTemperature, output, outputSlurry, processTime, category).offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Industria.id("mixer_" + name)));
    }

    private static void offerDigester(RecipeExporter exporter, SlurryStack inputSlurry, FluidStack outputFluid, int processTime, String name) {
        new DigesterRecipeBuilder(inputSlurry, outputFluid, processTime).offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Industria.id("digester_" + name)));
    }

    private static void offerClarifier(RecipeExporter exporter, FluidStack inputFluid, FluidStack outputFluid, OutputItemStack outputItem, int processTime, String name) {
        new ClarifierRecipeBuilder(inputFluid, outputFluid, outputItem, processTime).offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Industria.id("clarifier_" + name)));
    }

    private static void offerCrystallizerRecipe(RecipeExporter exporter, FluidStack waterFluid, FluidStack crystalFluid, IndustriaIngredient catalyst, OutputItemStack output, OutputItemStack byproduct, boolean requiresCatalyst, int catalystUses, int processTime, String name) {
        new CrystallizerRecipeBuilder(waterFluid, crystalFluid, catalyst, output, byproduct, requiresCatalyst, catalystUses, processTime).offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Industria.id("crystallizer_" + name)));
    }

    private static void offerRotaryKilnRecipe(RecipeExporter exporter, IndustriaIngredient input, OutputItemStack output, int requiredTemperature) {
        new RotaryKilnRecipeBuilder(input, output, requiredTemperature).offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Industria.id("rotary_kiln_" + RecipeGenerator.getRecipeName(output.item()))));
    }

    private static void offerElectrolyzerRecipe(RecipeExporter exporter,
                                                IndustriaIngredient input,
                                                IndustriaIngredient anode, IndustriaIngredient cathode,
                                                IndustriaIngredient electrolyteItem, FluidStack electrolyteFluid,
                                                FluidStack outputFluid, GasStack outputGas,
                                                int processTime, int energyCost, int temperature) {
        new ElectrolyzerRecipeBuilder(input, anode, cathode, electrolyteItem, electrolyteFluid, outputFluid, outputGas, processTime, energyCost, temperature)
                .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Industria.id("electrolyzer_" + getRecipeName(outputFluid.variant().getFluid()))));
    }

    private static void offerShakingTableRecipe(RecipeExporter exporter, IndustriaIngredient input, OutputItemStack output, @Nullable SlurryStack outputSlurry, int processTime, int frequency, RecipeCategory category) {
        new ShakingTableRecipeBuilder(input, output, outputSlurry, processTime, frequency, category)
                .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Industria.id("shaking_table_" + RecipeGenerator.getRecipeName(output.item()))));
    }

    private static void offerCentrifugalConcentratorRecipe(RecipeExporter exporter, IndustriaIngredient input, OutputItemStack output, @Nullable SlurryStack outputSlurry, int processTime, int rpm, RecipeCategory category) {
        new CentrifugalConcentratorRecipeBuilder(input, output, outputSlurry, processTime, rpm, category)
                .offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Industria.id("centrifugal_concentrator_" + RecipeGenerator.getRecipeName(output.item()))));
    }

    public static String getRecipeName(Fluid fluid) {
        return Registries.FLUID.getId(fluid).getPath();
    }

    public static @NotNull String hasTag(@NotNull TagKey<Item> tag) {
        return "has_" + tag.id().toString();
    }

    @Override
    public String getName() {
        return "Industria Recipe Provider";
    }
}
