package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.Block;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.TexturedModel;
import net.minecraft.data.loottable.BlockLootTableGenerator;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;

import java.util.function.Function;

import static dev.turtywurty.industria.datagen.IndustriaRecipeProvider.hasTag;

public class WoodSetDatagen {
    public static void generateBlockLootTables(WoodRegistrySet woodSet, FabricBlockLootTableProvider provider) {
        provider.addDrop(woodSet.planks);
        provider.addDrop(woodSet.log);
        provider.addDrop(woodSet.strippedLog);
        provider.addDrop(woodSet.strippedWood);
        provider.addDrop(woodSet.wood);
        provider.addDrop(woodSet.sapling);
        provider.addDrop(woodSet.stairs);
        provider.addDrop(woodSet.slab, provider::slabDrops);
        provider.addDrop(woodSet.fence);
        provider.addDrop(woodSet.fenceGate);
        provider.addDrop(woodSet.door);
        provider.addDrop(woodSet.trapdoor);
        provider.addDrop(woodSet.pressurePlate);
        provider.addDrop(woodSet.button);
        provider.addDrop(woodSet.sign, woodSet.signItem);
        provider.addDrop(woodSet.wallSign, woodSet.signItem);
        provider.addDrop(woodSet.hangingSign, woodSet.hangingSignItem);
        provider.addDrop(woodSet.wallHangingSign, woodSet.hangingSignItem);

        provider.addDrop(woodSet.leaves,
                leavesBlock -> provider.leavesDrops(leavesBlock, woodSet.sapling, BlockLootTableGenerator.SAPLING_DROP_CHANCE));
    }

    public static void generateEnglishLanguage(WoodRegistrySet woodSet, FabricLanguageProvider.TranslationBuilder translationBuilder) {
        String typeCaseName = snakeToTypeCase(woodSet.getName());
        translationBuilder.add(woodSet.planks, typeCaseName + " Planks");
        translationBuilder.add(woodSet.log, typeCaseName + " Log");
        translationBuilder.add(woodSet.strippedLog, "Stripped " + typeCaseName + " Log");
        translationBuilder.add(woodSet.strippedWood, "Stripped " + typeCaseName + " Wood");
        translationBuilder.add(woodSet.wood, typeCaseName + " Wood");
        translationBuilder.add(woodSet.leaves, typeCaseName + " Leaves");
        translationBuilder.add(woodSet.sapling, typeCaseName + " Sapling");
        translationBuilder.add(woodSet.stairs, typeCaseName + " Stairs");
        translationBuilder.add(woodSet.slab, typeCaseName + " Slab");
        translationBuilder.add(woodSet.fence, typeCaseName + " Fence");
        translationBuilder.add(woodSet.fenceGate, typeCaseName + " Fence Gate");
        translationBuilder.add(woodSet.door, typeCaseName + " Door");
        translationBuilder.add(woodSet.trapdoor, typeCaseName + " Trapdoor");
        translationBuilder.add(woodSet.pressurePlate, typeCaseName + " Pressure Plate");
        translationBuilder.add(woodSet.button, typeCaseName + " Button");
        translationBuilder.add(woodSet.sign, typeCaseName + " Sign");
        translationBuilder.add(woodSet.wallSign, typeCaseName + " Wall Sign");
        translationBuilder.add(woodSet.hangingSign, typeCaseName + " Hanging Sign");
        translationBuilder.add(woodSet.wallHangingSign, typeCaseName + " Wall Hanging Sign");
        translationBuilder.add(woodSet.boatItem, typeCaseName + " Boat");
        translationBuilder.add(woodSet.chestBoatItem, typeCaseName + " Chest Boat");
        translationBuilder.add(woodSet.boatEntityType, typeCaseName + " Boat");
        translationBuilder.add(woodSet.chestBoatEntityType, typeCaseName + " Chest Boat");
        translationBuilder.add(woodSet.signItem, typeCaseName + " Sign");
        translationBuilder.add(woodSet.hangingSignItem, typeCaseName + " Hanging Sign");
    }

    public static void generateBlockStateAndModels(WoodRegistrySet woodSet, BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.createLogTexturePool(woodSet.log)
                .log(woodSet.log)
                .wood(woodSet.wood);
        blockStateModelGenerator.createLogTexturePool(woodSet.strippedLog)
                .log(woodSet.strippedLog)
                .wood(woodSet.strippedWood);
        blockStateModelGenerator.registerTintedBlockAndItem(woodSet.leaves, TexturedModel.LEAVES, 0x00BB0A);
        blockStateModelGenerator.registerTintableCross(woodSet.sapling, BlockStateModelGenerator.CrossType.NOT_TINTED);
        blockStateModelGenerator.registerHangingSign(woodSet.strippedLog, woodSet.hangingSign, woodSet.wallHangingSign);
        blockStateModelGenerator.registerCubeAllModelTexturePool(woodSet.planks)
                .family(woodSet.createBlockFamily());
    }

    public static void generateRecipes(WoodRegistrySet woodSet, RecipeGenerator generator, RecipeExporter exporter, RegistryEntryLookup<Item> registries) {
        ShapelessRecipeJsonBuilder.create(registries, RecipeCategory.BUILDING_BLOCKS, woodSet.planks, 4)
                .input(Ingredient.ofTag(registries.getOrThrow(woodSet.logsItemTag)))
                .criterion(hasTag(woodSet.logsItemTag), generator.conditionsFromTag(woodSet.logsItemTag))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(registries, RecipeCategory.DECORATIONS, woodSet.hangingSignItem, 6)
                .input('P', woodSet.planks)
                .input('C', ConventionalItemTags.CHAINS)
                .pattern("C C")
                .pattern("PPP")
                .pattern("PPP")
                .criterion(RecipeGenerator.hasItem(woodSet.planks), generator.conditionsFromItem(woodSet.planks))
                .criterion(hasTag(ConventionalItemTags.CHAINS), generator.conditionsFromTag(ConventionalItemTags.CHAINS))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(registries, RecipeCategory.TRANSPORTATION, woodSet.boatItem)
                .input('P', woodSet.planks)
                .pattern("P P")
                .pattern("PPP")
                .criterion(RecipeGenerator.hasItem(woodSet.planks), generator.conditionsFromItem(woodSet.planks))
                .offerTo(exporter);

        ShapelessRecipeJsonBuilder.create(registries, RecipeCategory.TRANSPORTATION, woodSet.chestBoatItem)
                .input(Ingredient.ofTag(registries.getOrThrow(woodSet.logsItemTag)))
                .input(Ingredient.ofTag(registries.getOrThrow(ConventionalItemTags.WOODEN_CHESTS)))
                .criterion(hasTag(woodSet.logsItemTag), generator.conditionsFromTag(woodSet.logsItemTag))
                .criterion(hasTag(ConventionalItemTags.WOODEN_CHESTS), generator.conditionsFromTag(ConventionalItemTags.WOODEN_CHESTS))
                .offerTo(exporter);

        generator.generateFamily(woodSet.createBlockFamily(), FeatureSet.empty());
    }

    public static void generateItemTags(WoodRegistrySet woodSet, Function<TagKey<Item>, ProvidedTagBuilder<Item, Item>> provider) {
        provider.apply(woodSet.logsItemTag)
                .add(woodSet.log.asItem())
                .add(woodSet.strippedLog.asItem())
                .add(woodSet.wood.asItem())
                .add(woodSet.strippedWood.asItem());

        provider.apply(ItemTags.LOGS_THAT_BURN)
                .addTag(woodSet.logsItemTag);

        provider.apply(ItemTags.PLANKS)
                .add(woodSet.planks.asItem());

        provider.apply(ItemTags.LEAVES)
                .add(woodSet.leaves.asItem());

        provider.apply(ItemTags.SAPLINGS)
                .add(woodSet.sapling.asItem());

        provider.apply(ItemTags.WOODEN_BUTTONS)
                .add(woodSet.button.asItem());

        provider.apply(ItemTags.WOODEN_DOORS)
                .add(woodSet.door.asItem());

        provider.apply(ItemTags.WOODEN_FENCES)
                .add(woodSet.fence.asItem());

        provider.apply(ItemTags.FENCE_GATES)
                .add(woodSet.fenceGate.asItem());

        provider.apply(ItemTags.WOODEN_PRESSURE_PLATES)
                .add(woodSet.pressurePlate.asItem());

        provider.apply(ItemTags.WOODEN_TRAPDOORS)
                .add(woodSet.trapdoor.asItem());

        provider.apply(ItemTags.WOODEN_STAIRS)
                .add(woodSet.stairs.asItem());

        provider.apply(ItemTags.WOODEN_SLABS)
                .add(woodSet.slab.asItem());

        provider.apply(ItemTags.SIGNS)
                .add(woodSet.sign.asItem());

        provider.apply(ItemTags.HANGING_SIGNS)
                .add(woodSet.hangingSign.asItem());

        provider.apply(ItemTags.BOATS)
                .add(woodSet.boatItem);

        provider.apply(ItemTags.CHEST_BOATS)
                .add(woodSet.chestBoatItem);
    }

    public static void generateBlockTags(WoodRegistrySet woodSet, Function<TagKey<Block>, ProvidedTagBuilder<Block, Block>> provider) {
        provider.apply(woodSet.logsBlockTag)
                .add(woodSet.log)
                .add(woodSet.strippedLog)
                .add(woodSet.wood)
                .add(woodSet.strippedWood);

        provider.apply(BlockTags.LOGS_THAT_BURN)
                .addTag(woodSet.logsBlockTag);

        provider.apply(BlockTags.PLANKS)
                .add(woodSet.planks);

        provider.apply(BlockTags.LEAVES)
                .add(woodSet.leaves);

        provider.apply(BlockTags.SAPLINGS)
                .add(woodSet.sapling);

        provider.apply(BlockTags.WOODEN_BUTTONS)
                .add(woodSet.button);

        provider.apply(BlockTags.WOODEN_DOORS)
                .add(woodSet.door);

        provider.apply(BlockTags.WOODEN_FENCES)
                .add(woodSet.fence);

        provider.apply(BlockTags.FENCE_GATES)
                .add(woodSet.fenceGate);

        provider.apply(BlockTags.WOODEN_PRESSURE_PLATES)
                .add(woodSet.pressurePlate);

        provider.apply(BlockTags.WOODEN_TRAPDOORS)
                .add(woodSet.trapdoor);

        provider.apply(BlockTags.WOODEN_STAIRS)
                .add(woodSet.stairs);

        provider.apply(BlockTags.WOODEN_SLABS)
                .add(woodSet.slab);

        provider.apply(BlockTags.STANDING_SIGNS)
                .add(woodSet.sign);

        provider.apply(BlockTags.WALL_SIGNS)
                .add(woodSet.wallSign);

        provider.apply(BlockTags.CEILING_HANGING_SIGNS)
                .add(woodSet.hangingSign);

        provider.apply(BlockTags.WALL_HANGING_SIGNS)
                .add(woodSet.wallHangingSign);
    }

    public static void generateEntityTags(WoodRegistrySet woodSet, Function<TagKey<EntityType<?>>, ProvidedTagBuilder<EntityType<?>, EntityType<?>>> provider) {
        provider.apply(EntityTypeTags.BOAT)
                .add(woodSet.boatEntityType)
                .add(woodSet.chestBoatEntityType);
    }

    private static String snakeToTypeCase(String str) {
        StringBuilder result = new StringBuilder();
        boolean toUpperCase = true;
        for (char c : str.toCharArray()) {
            if (c == '_') {
                toUpperCase = true;
            } else {
                result.append(toUpperCase ? Character.toUpperCase(c) : c);
                toUpperCase = false;
            }
        }

        return result.toString();
    }
}
