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
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class IndustriaRecipeProvider extends FabricRecipeProvider {
    public IndustriaRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public RecipeProvider createRecipeProvider(HolderLookup.Provider wrapperLookup, RecipeOutput exporter) {
        return new RecipeProvider(wrapperLookup, exporter) {
            @Override
            public void buildRecipes() {
                HolderGetter<Item> itemLookup = wrapperLookup.lookupOrThrow(Registries.ITEM);

                for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
                    WoodSetDatagen.generateRecipes(woodSet, this, output, itemLookup);
                }

                shaped(RecipeCategory.MISC, BlockInit.ALLOY_FURNACE)
                        .pattern("AAA")
                        .pattern("ABA")
                        .pattern("AAA")
                        .define('A', ConventionalItemTags.STORAGE_BLOCKS_COPPER)
                        .define('B', ConventionalItemTags.STORAGE_BLOCKS_IRON)
                        .unlockedBy(hasTag(ConventionalItemTags.STORAGE_BLOCKS_COPPER), has(ConventionalItemTags.STORAGE_BLOCKS_COPPER))
                        .unlockedBy(hasTag(ConventionalItemTags.STORAGE_BLOCKS_IRON), has(ConventionalItemTags.STORAGE_BLOCKS_IRON))
                        .save(output);

                shaped(RecipeCategory.MISC, BlockInit.THERMAL_GENERATOR)
                        .pattern("ABA")
                        .pattern("CDC")
                        .pattern("AEA")
                        .define('A', ConventionalItemTags.STORAGE_BLOCKS_IRON)
                        .define('B', ConventionalItemTags.PLAYER_WORKSTATIONS_FURNACES)
                        .define('C', ConventionalItemTags.STORAGE_BLOCKS_REDSTONE)
                        .define('D', ConventionalItemTags.STORAGE_BLOCKS_COAL)
                        .define('E', Blocks.IRON_BARS)
                        .unlockedBy(hasTag(ConventionalItemTags.STORAGE_BLOCKS_IRON), has(ConventionalItemTags.STORAGE_BLOCKS_IRON))
                        .unlockedBy(hasTag(ConventionalItemTags.PLAYER_WORKSTATIONS_FURNACES), has(ConventionalItemTags.PLAYER_WORKSTATIONS_FURNACES))
                        .unlockedBy(hasTag(ConventionalItemTags.STORAGE_BLOCKS_REDSTONE), has(ConventionalItemTags.STORAGE_BLOCKS_REDSTONE))
                        .unlockedBy(hasTag(ConventionalItemTags.STORAGE_BLOCKS_COAL), has(ConventionalItemTags.STORAGE_BLOCKS_COAL))
                        .unlockedBy(getHasName(Blocks.IRON_BARS), has(Blocks.IRON_BARS))
                        .save(output);

                offerAlloySmelting(output, RecipeCategory.MISC,
                        new IndustriaIngredient(itemLookup.getOrThrow(ConventionalItemTags.IRON_INGOTS), 1),
                        new IndustriaIngredient(itemLookup.getOrThrow(ItemTags.COALS), 4),
                        ItemInit.STEEL_INGOT.getDefaultInstance(),
                        400);

                offerCrusher(output, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.STONE),
                        new OutputItemStack(Items.COBBLESTONE, 1, 1),
                        new OutputItemStack(Items.GRAVEL, 1, 0.25F),
                        100,
                        "stone");

                offerCrusher(output, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.COBBLESTONE),
                        new OutputItemStack(Items.GRAVEL, 1, 1),
                        new OutputItemStack(Items.FLINT, 1, 0.1F),
                        100,
                        "cobblestone");

                offerCrusher(output, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.GRAVEL),
                        new OutputItemStack(Items.SAND, 1, 1),
                        new OutputItemStack(Items.FLINT, 1, 0.1F),
                        100,
                        "gravel");

                offerCrusher(output, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.SAND),
                        new OutputItemStack(Items.GUNPOWDER, 1, 0.35F),
                        OutputItemStack.EMPTY,
                        100,
                        "sand");

                offerCrusher(output, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE),
                        new OutputItemStack(Items.COAL, 1, 1),
                        new OutputItemStack(Items.COAL, 1, 0.15F),
                        100,
                        "coal_ore");

                offerCrusher(output, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE),
                        new OutputItemStack(Items.REDSTONE, UniformInt.of(2, 5), 1),
                        new OutputItemStack(Items.REDSTONE, 1, 0.15F),
                        100,
                        "redstone_ore");

                offerCrusher(output, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE),
                        new OutputItemStack(Items.LAPIS_LAZULI, UniformInt.of(1, 4), 1),
                        new OutputItemStack(Items.LAPIS_LAZULI, UniformInt.of(1, 2), 0.15F),
                        100,
                        "lapis_ore");

                offerCrusher(output, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE),
                        new OutputItemStack(Items.DIAMOND, 1, 1),
                        new OutputItemStack(Items.DIAMOND, 1, 0.15F),
                        100,
                        "diamond_ore");

                offerCrusher(output, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE),
                        new OutputItemStack(Items.EMERALD, 1, 1),
                        new OutputItemStack(Items.EMERALD, 1, 0.15F),
                        100,
                        "emerald_ore");

                offerCrusher(output, RecipeCategory.MISC,
                        new IndustriaIngredient(itemLookup.getOrThrow(ConventionalItemTags.QUARTZ_ORES), 1),
                        new OutputItemStack(Items.QUARTZ, 1, 1),
                        new OutputItemStack(Items.QUARTZ, UniformInt.of(1, 3), 0.15F),
                        100,
                        "quartz_ore");

                offerCrusher(output, RecipeCategory.MISC,
                        new IndustriaIngredient(1, Items.NETHER_GOLD_ORE),
                        new OutputItemStack(Items.GOLD_NUGGET, UniformInt.of(2, 6), 1),
                        new OutputItemStack(Items.GOLD_NUGGET, UniformInt.of(1, 3), 0.15F),
                        100,
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

                shaped(RecipeCategory.MISC, BlockInit.CABLE, 8)
                        .pattern("III")
                        .pattern("IRI")
                        .pattern("III")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('R', ConventionalItemTags.REDSTONE_DUSTS)
                        .unlockedBy(hasTag(ConventionalItemTags.IRON_INGOTS), has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy(hasTag(ConventionalItemTags.REDSTONE_DUSTS), has(ConventionalItemTags.REDSTONE_DUSTS))
                        .save(output);

                nineBlockStorageRecipes(RecipeCategory.MISC, ItemInit.ALUMINIUM_INGOT, RecipeCategory.BUILDING_BLOCKS, BlockInit.ALUMINIUM_BLOCK);
                nineBlockStorageRecipes(RecipeCategory.MISC, ItemInit.TIN_INGOT, RecipeCategory.BUILDING_BLOCKS, BlockInit.TIN_BLOCK);
                nineBlockStorageRecipes(RecipeCategory.MISC, ItemInit.ZINC_INGOT, RecipeCategory.BUILDING_BLOCKS, BlockInit.ZINC_BLOCK);

                nineBlockStorageRecipes(RecipeCategory.MISC, ItemInit.ALUMINIUM_NUGGET, RecipeCategory.MISC, ItemInit.ALUMINIUM_INGOT, "aluminium_nugget_to_ingot", null, "aluminium_ingot_to_nugget", null);
                nineBlockStorageRecipes(RecipeCategory.MISC, ItemInit.TIN_NUGGET, RecipeCategory.MISC, ItemInit.TIN_INGOT, "tin_nugget_to_ingot", null, "tin_ingot_to_nugget", null);
                nineBlockStorageRecipes(RecipeCategory.MISC, ItemInit.ZINC_NUGGET, RecipeCategory.MISC, ItemInit.ZINC_INGOT, "zinc_nugget_to_ingot", null, "zinc_ingot_to_nugget", null);

                nineBlockStorageRecipes(RecipeCategory.MISC, ItemInit.BAUXITE, RecipeCategory.BUILDING_BLOCKS, BlockInit.RAW_BAUXITE_BLOCK);
                nineBlockStorageRecipes(RecipeCategory.MISC, ItemInit.CASSITERITE, RecipeCategory.BUILDING_BLOCKS, BlockInit.RAW_CASSITERITE_BLOCK);
                nineBlockStorageRecipes(RecipeCategory.MISC, ItemInit.SPHALERITE, RecipeCategory.BUILDING_BLOCKS, BlockInit.RAW_SPHALERITE_BLOCK);

                List<ItemLike> aluminiumOres = List.of(BlockInit.BAUXITE_ORE, BlockInit.DEEPSLATE_BAUXITE_ORE, ItemInit.BAUXITE);
                List<ItemLike> tinOres = List.of(BlockInit.CASSITERITE_ORE, BlockInit.DEEPSLATE_CASSITERITE_ORE, ItemInit.CASSITERITE);
                List<ItemLike> zincOres = List.of(BlockInit.SPHALERITE_ORE, BlockInit.DEEPSLATE_SPHALERITE_ORE, ItemInit.SPHALERITE);

                oreSmelting(aluminiumOres, RecipeCategory.MISC, ItemInit.ALUMINIUM_INGOT, 0.7F, 200, "aluminium_ingot");
                oreSmelting(tinOres, RecipeCategory.MISC, ItemInit.TIN_INGOT, 0.7F, 200, "tin_ingot");
                oreSmelting(zincOres, RecipeCategory.MISC, ItemInit.ZINC_INGOT, 0.7F, 200, "zinc_ingot");

                oreBlasting(aluminiumOres, RecipeCategory.MISC, ItemInit.ALUMINIUM_INGOT, 0.7F, 100, "aluminium_ingot");
                oreBlasting(tinOres, RecipeCategory.MISC, ItemInit.TIN_INGOT, 0.7F, 100, "tin_ingot");
                oreBlasting(zincOres, RecipeCategory.MISC, ItemInit.ZINC_INGOT, 0.7F, 100, "zinc_ingot");

                offerMixer(output, RecipeCategory.MISC, List.of(
                                new IndustriaIngredient(4, ItemInit.BAUXITE),
                                new IndustriaIngredient(1, ItemInit.SODIUM_HYDROXIDE)),
                        new FluidStack(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET),
                        170, 180,
                        OutputItemStack.EMPTY,
                        new SlurryStack(SlurryVariant.of(SlurryInit.BAUXITE_SLURRY), FluidConstants.BUCKET),
                        200, "bauxite_to_bauxite_slurry");

                offerDigester(output, new SlurryStack(SlurryVariant.of(SlurryInit.BAUXITE_SLURRY), FluidConstants.BUCKET),
                        new FluidStack(FluidVariant.of(FluidInit.DIRTY_SODIUM_ALUMINATE.still()), FluidConstants.BOTTLE),
                        200, "bauxite_to_dirty_sodium_aluminate");

                offerClarifier(output, new FluidStack(FluidVariant.of(FluidInit.DIRTY_SODIUM_ALUMINATE.still()), FluidConstants.BUCKET),
                        new FluidStack(FluidVariant.of(FluidInit.SODIUM_ALUMINATE.still()), FluidConstants.BOTTLE),
                        new OutputItemStack(ItemInit.RED_MUD, UniformInt.of(1, 3), 1),
                        500, "dirty_sodium_aluminate_to_sodium_aluminate");

                offerCrystallizerRecipe(output, new FluidStack(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET * 5),
                        new FluidStack(FluidVariant.of(FluidInit.SODIUM_ALUMINATE.still()), FluidConstants.BUCKET),
                        new IndustriaIngredient(1, ItemInit.ALUMINIUM_HYDROXIDE),
                        new OutputItemStack(ItemInit.ALUMINIUM_HYDROXIDE, 8, 1),
                        new OutputItemStack(ItemInit.SODIUM_CARBONATE, UniformInt.of(8, 16), 0.75F),
                        false, 5, 1000, "aluminium_hydroxide");

                offerRotaryKilnRecipe(output,
                        new IndustriaIngredient(1, ItemInit.ALUMINIUM_HYDROXIDE),
                        new OutputItemStack(ItemInit.ALUMINA, 1, 1),
                        1200);

                offerElectrolyzerRecipe(output,
                        new IndustriaIngredient(3, ItemInit.ALUMINA),
                        new IndustriaIngredient(1, ItemInit.CARBON_ROD),
                        new IndustriaIngredient(1, Items.COAL),
                        new IndustriaIngredient(9, ItemInit.CRYOLITE),
                        new FluidStack(FluidVariant.of(FluidInit.MOLTEN_CRYOLITE.still()), FluidConstants.BUCKET),
                        new FluidStack(FluidVariant.of(FluidInit.MOLTEN_ALUMINIUM.still()), FluidConstants.BUCKET * 2),
                        new GasStack(GasInit.CARBON_DIOXIDE, FluidConstants.NUGGET),
                        2_000, 10_000, 1_000);

                offerCrusher(output, RecipeCategory.MISC,
                        new IndustriaIngredient(1, ItemInit.CASSITERITE),
                        new OutputItemStack(ItemInit.CRUSHED_CASSITERITE, 2, 1),
                        new OutputItemStack(ItemInit.CRUSHED_CASSITERITE, 1, (3 / 10F)),
                        100, "raw_cassiterite");

                offerShakingTableRecipe(output,
                        new IndustriaIngredient(1, ItemInit.CRUSHED_CASSITERITE),
                        new OutputItemStack(ItemInit.CASSITERITE_CONCENTRATE, 1, 1),
                        new SlurryStack(SlurryVariant.of(SlurryInit.CLAY_SLURRY), FluidConstants.BUCKET / 2),
                        200, 4, RecipeCategory.MISC);

                offerCentrifugalConcentratorRecipe(output,
                        new IndustriaIngredient(1, ItemInit.CASSITERITE_CONCENTRATE),
                        new OutputItemStack(ItemInit.CASSITERITE_CONCENTRATE, 1, 1),
                        new SlurryStack(SlurryVariant.of(SlurryInit.CLAY_SLURRY), FluidConstants.BUCKET / 4),
                        200, 500, RecipeCategory.MISC);
            }
        };
    }

    private static void offerAlloySmelting(RecipeOutput exporter, RecipeCategory category, IndustriaIngredient inputA, IndustriaIngredient inputB, ItemStack output, int smeltTime) {
        offerAlloySmelting(exporter, category, inputA, inputB, output, smeltTime, RecipeProvider.getSimpleRecipeName(output.getItem()));
    }

    private static void offerAlloySmelting(RecipeOutput exporter, RecipeCategory category, IndustriaIngredient inputA, IndustriaIngredient inputB, ItemStack output, int smeltTime, String name) {
        new AlloyFurnaceRecipeBuilder(inputA, inputB, output, smeltTime, category).save(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("alloy_" + name)));
    }

    private static void offerCrusher(RecipeOutput exporter, RecipeCategory category, IndustriaIngredient input, OutputItemStack outputA, OutputItemStack outputB, int processTime, String name) {
        new CrusherRecipeBuilder(input, outputA, outputB, processTime, category).save(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("crusher_" + name)));
    }

    private static void offerMixer(RecipeOutput exporter, RecipeCategory category, List<IndustriaIngredient> inputs, @Nullable FluidStack inputFluid, int minTemperature, int maxTemperature, OutputItemStack output, @Nullable SlurryStack outputSlurry, int processTime, String name) {
        new MixerRecipeBuilder(inputs, inputFluid, minTemperature, maxTemperature, output, outputSlurry, processTime, category).save(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("mixer_" + name)));
    }

    private static void offerDigester(RecipeOutput exporter, SlurryStack inputSlurry, FluidStack outputFluid, int processTime, String name) {
        new DigesterRecipeBuilder(inputSlurry, outputFluid, processTime).offerTo(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("digester_" + name)));
    }

    private static void offerClarifier(RecipeOutput exporter, FluidStack inputFluid, FluidStack outputFluid, OutputItemStack outputItem, int processTime, String name) {
        new ClarifierRecipeBuilder(inputFluid, outputFluid, outputItem, processTime).offerTo(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("clarifier_" + name)));
    }

    private static void offerCrystallizerRecipe(RecipeOutput exporter, FluidStack waterFluid, FluidStack crystalFluid, IndustriaIngredient catalyst, OutputItemStack output, OutputItemStack byproduct, boolean requiresCatalyst, int catalystUses, int processTime, String name) {
        new CrystallizerRecipeBuilder(waterFluid, crystalFluid, catalyst, output, byproduct, requiresCatalyst, catalystUses, processTime).offerTo(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("crystallizer_" + name)));
    }

    private static void offerRotaryKilnRecipe(RecipeOutput exporter, IndustriaIngredient input, OutputItemStack output, int requiredTemperature) {
        new RotaryKilnRecipeBuilder(input, output, requiredTemperature).offerTo(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("rotary_kiln_" + RecipeProvider.getSimpleRecipeName(output.item()))));
    }

    private static void offerElectrolyzerRecipe(RecipeOutput exporter,
                                                IndustriaIngredient input,
                                                IndustriaIngredient anode, IndustriaIngredient cathode,
                                                IndustriaIngredient electrolyteItem, FluidStack electrolyteFluid,
                                                FluidStack outputFluid, GasStack outputGas,
                                                int processTime, int energyCost, int temperature) {
        new ElectrolyzerRecipeBuilder(input, anode, cathode, electrolyteItem, electrolyteFluid, outputFluid, outputGas, processTime, energyCost, temperature)
                .offerTo(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("electrolyzer_" + getRecipeName(outputFluid.variant().getFluid()))));
    }

    private static void offerShakingTableRecipe(RecipeOutput exporter, IndustriaIngredient input, OutputItemStack output, @Nullable SlurryStack outputSlurry, int processTime, int frequency, RecipeCategory category) {
        new ShakingTableRecipeBuilder(input, output, outputSlurry, processTime, frequency, category)
                .save(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("shaking_table_" + RecipeProvider.getSimpleRecipeName(output.item()))));
    }

    private static void offerCentrifugalConcentratorRecipe(RecipeOutput exporter, IndustriaIngredient input, OutputItemStack output, @Nullable SlurryStack outputSlurry, int processTime, int rpm, RecipeCategory category) {
        new CentrifugalConcentratorRecipeBuilder(input, output, outputSlurry, processTime, rpm, category)
                .save(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("centrifugal_concentrator_" + RecipeProvider.getSimpleRecipeName(output.item()))));
    }

    public static String getRecipeName(Fluid fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid).getPath();
    }

    public static @NotNull String hasTag(@NotNull TagKey<Item> tag) {
        return "has_" + tag.location().toString();
    }

    @Override
    public String getName() {
        return "Industria Recipe Provider";
    }
}
