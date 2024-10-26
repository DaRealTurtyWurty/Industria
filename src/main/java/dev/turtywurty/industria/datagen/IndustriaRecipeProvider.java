package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.datagen.builder.AlloyFurnaceRecipeBuilder;
import dev.turtywurty.industria.datagen.builder.CrusherRecipeBuilder;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeGenerator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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
    public RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter exporter) {
        return new RecipeGenerator(wrapperLookup, exporter) {
            @Override
            public void generate() {
                RegistryEntryLookup<Item> itemLookup = wrapperLookup.getOrThrow(RegistryKeys.ITEM);
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

    private static @NotNull String hasTag(@NotNull TagKey<Item> tag) {
        return "has_" + tag.id().toString();
    }

    @Override
    public String getName() {
        return "Industria Recipe Provider";
    }
}
