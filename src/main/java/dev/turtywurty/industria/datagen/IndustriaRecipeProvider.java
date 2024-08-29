package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.datagen.builder.AlloyFurnaceRecipeBuilder;
import dev.turtywurty.industria.datagen.builder.CrusherRecipeBuilder;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.util.CountedIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class IndustriaRecipeProvider extends FabricRecipeProvider {
    public IndustriaRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BlockInit.ALLOY_FURNACE)
                .pattern("AAA")
                .pattern("ABA")
                .pattern("AAA")
                .input('A', Blocks.COPPER_BLOCK)
                .input('B', Blocks.IRON_BLOCK)
                .criterion(hasTag(ConventionalItemTags.STORAGE_BLOCKS_COPPER), conditionsFromTag(ConventionalItemTags.STORAGE_BLOCKS_COPPER))
                .criterion(hasTag(ConventionalItemTags.STORAGE_BLOCKS_IRON), conditionsFromTag(ConventionalItemTags.STORAGE_BLOCKS_IRON))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BlockInit.THERMAL_GENERATOR)
                .pattern("ABA")
                .pattern("CDC")
                .pattern("AEA")
                .input('A', Blocks.IRON_BLOCK)
                .input('B', Blocks.FURNACE)
                .input('C', Blocks.REDSTONE_BLOCK)
                .input('D', Blocks.COAL_BLOCK)
                .input('E', Blocks.IRON_BARS)
                .criterion(hasTag(ConventionalItemTags.STORAGE_BLOCKS_IRON), conditionsFromTag(ConventionalItemTags.STORAGE_BLOCKS_IRON))
                .criterion(hasTag(ConventionalItemTags.PLAYER_WORKSTATIONS_FURNACES), conditionsFromTag(ConventionalItemTags.PLAYER_WORKSTATIONS_FURNACES))
                .criterion(hasTag(ConventionalItemTags.STORAGE_BLOCKS_REDSTONE), conditionsFromTag(ConventionalItemTags.STORAGE_BLOCKS_REDSTONE))
                .criterion(hasTag(ConventionalItemTags.STORAGE_BLOCKS_COAL), conditionsFromTag(ConventionalItemTags.STORAGE_BLOCKS_COAL))
                .criterion(hasItem(Blocks.IRON_BARS), conditionsFromItem(Blocks.IRON_BARS))
                .offerTo(exporter);

        offerAlloySmelting(exporter, RecipeCategory.MISC,
                CountedIngredient.fromTag(1, ConventionalItemTags.IRON_INGOTS),
                CountedIngredient.fromTag(4, ItemTags.COALS),
                ItemInit.STEEL_INGOT.getDefaultStack(),
                400);

        offerCrusher(exporter, RecipeCategory.MISC,
                CountedIngredient.ofItems(1, Items.STONE),
                new OutputItemStack(Items.COBBLESTONE, 1, 1),
                new OutputItemStack(Items.GRAVEL, 1, 0.25F),
                10,
                "stone");

        offerCrusher(exporter, RecipeCategory.MISC,
                CountedIngredient.ofItems(1, Items.COBBLESTONE),
                new OutputItemStack(Items.GRAVEL, 1, 1),
                new OutputItemStack(Items.FLINT, 1, 0.1F),
                10,
                "cobblestone");

        offerCrusher(exporter, RecipeCategory.MISC,
                CountedIngredient.ofItems(1, Items.GRAVEL),
                new OutputItemStack(Items.SAND, 1, 1),
                new OutputItemStack(Items.FLINT, 1, 0.1F),
                10,
                "gravel");

        offerCrusher(exporter, RecipeCategory.MISC,
                CountedIngredient.ofItems(1, Items.SAND),
                new OutputItemStack(Items.GUNPOWDER, 1, 0.35F),
                OutputItemStack.EMPTY,
                10,
                "sand");

        offerCrusher(exporter, RecipeCategory.MISC,
                CountedIngredient.ofItems(1, Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE),
                new OutputItemStack(Items.COAL, 1, 1),
                new OutputItemStack(Items.COAL, 1, 0.15F),
                10,
                "coal_ore");

        offerCrusher(exporter, RecipeCategory.MISC,
                CountedIngredient.ofItems(1, Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE),
                new OutputItemStack(Items.REDSTONE, UniformIntProvider.create(2, 5), 1),
                new OutputItemStack(Items.REDSTONE, 1, 0.15F),
                10,
                "redstone_ore");

        offerCrusher(exporter, RecipeCategory.MISC,
                CountedIngredient.ofItems(1, Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE),
                new OutputItemStack(Items.LAPIS_LAZULI, UniformIntProvider.create(1, 4), 1),
                new OutputItemStack(Items.LAPIS_LAZULI, UniformIntProvider.create(1, 2), 0.15F),
                10,
                "lapis_ore");

        offerCrusher(exporter, RecipeCategory.MISC,
                CountedIngredient.ofItems(1, Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE),
                new OutputItemStack(Items.DIAMOND, 1, 1),
                new OutputItemStack(Items.DIAMOND, 1, 0.15F),
                10,
                "diamond_ore");

        offerCrusher(exporter, RecipeCategory.MISC,
                CountedIngredient.ofItems(1, Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE),
                new OutputItemStack(Items.EMERALD, 1, 1),
                new OutputItemStack(Items.EMERALD, 1, 0.15F),
                10,
                "emerald_ore");

        offerCrusher(exporter, RecipeCategory.MISC,
                CountedIngredient.fromTag(1, ConventionalItemTags.QUARTZ_ORES),
                new OutputItemStack(Items.QUARTZ, 1, 1),
                new OutputItemStack(Items.QUARTZ, UniformIntProvider.create(1, 3), 0.15F),
                10,
                "quartz_ore");

        offerCrusher(exporter, RecipeCategory.MISC,
                CountedIngredient.ofItems(1, Items.NETHER_GOLD_ORE),
                new OutputItemStack(Items.GOLD_NUGGET, UniformIntProvider.create(2, 6), 1),
                new OutputItemStack(Items.GOLD_NUGGET, UniformIntProvider.create(1, 3), 0.15F),
                10,
                "nether_gold_ore");

//        offerCrusher(exporter, RecipeCategory.MISC,
//                CountedIngredient.fromTag(1, ConventionalItemTags.IRON_RAW_MATERIALS),
//                ItemInit.IRON_DUST.getDefaultStack(),
//                1.0F,
//                ItemInit.IRON_DUST.getDefaultStack(),
//                0.1F,
//                200);

//        offerCrusher(exporter, RecipeCategory.MISC,
//                CountedIngredient.fromTag(1, ConventionalItemTags.GOLD_ORES),
//                ItemInit.GOLD_DUST.getDefaultStack(),
//                1.0F,
//                ItemInit.GOLD_DUST.getDefaultStack(),
//                0.1F,
//                200);

//        offerCrusher(exporter, RecipeCategory.MISC,
//                CountedIngredient.fromTag(1, ConventionalItemTags.COPPER_RAW_MATERIALS),
//                ItemInit.COPPER_DUST.getDefaultStack(),
//                1.0F,
//                ItemInit.COPPER_DUST.getDefaultStack(),
//                0.1F,
//                200);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, BlockInit.CABLE, 8)
                .pattern("III")
                .pattern("IRI")
                .pattern("III")
                .input('I', ConventionalItemTags.IRON_INGOTS)
                .input('R', ConventionalItemTags.REDSTONE_DUSTS)
                .criterion(hasTag(ConventionalItemTags.IRON_INGOTS), conditionsFromTag(ConventionalItemTags.IRON_INGOTS))
                .criterion(hasTag(ConventionalItemTags.REDSTONE_DUSTS), conditionsFromTag(ConventionalItemTags.REDSTONE_DUSTS))
                .offerTo(exporter);
    }

    private static void offerAlloySmelting(RecipeExporter exporter, RecipeCategory category, Ingredient inputA, int inputACount, Ingredient inputB, int inputBCount, ItemStack output, int smeltTime) {
        offerAlloySmelting(exporter, category, new CountedIngredient(inputA, inputACount), new CountedIngredient(inputB, inputBCount), output, smeltTime);
    }

    private static void offerAlloySmelting(RecipeExporter exporter, RecipeCategory category, CountedIngredient inputA, CountedIngredient inputB, ItemStack output, int smeltTime) {
        offerAlloySmelting(exporter, category, inputA, inputB, output, smeltTime, getRecipeName(output.getItem()));
    }

    private static void offerAlloySmelting(RecipeExporter exporter, RecipeCategory category, CountedIngredient inputA, CountedIngredient inputB, ItemStack output, int smeltTime, String name) {
        new AlloyFurnaceRecipeBuilder(inputA, inputB, output, smeltTime, category).offerTo(exporter, Industria.id("alloy_" + name));
    }

    private static void offerCrusher(RecipeExporter exporter, RecipeCategory category, Ingredient input, OutputItemStack outputA, OutputItemStack outputB, int processTime, String name) {
        offerCrusher(exporter, category, new CountedIngredient(input, 1), outputA, outputB, processTime, name);
    }

    private static void offerCrusher(RecipeExporter exporter, RecipeCategory category, CountedIngredient input, OutputItemStack outputA, OutputItemStack outputB, int processTime, String name) {
        new CrusherRecipeBuilder(input, outputA, outputB, processTime, category).offerTo(exporter, Industria.id("crusher_" + name));
    }

    private static @NotNull String hasTag(@NotNull TagKey<Item> tag) {
        return "has_" + tag.id().toString();
    }
}
