package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.recipes.*;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

import static dev.turtywurty.industria.datagen.IndustriaRecipeProvider.hasTag;

public class WoodSetDatagen {
    public static void generateBlockLootTables(WoodRegistrySet woodSet, FabricBlockLootSubProvider provider) {
        provider.dropSelf(woodSet.planks);
        provider.dropSelf(woodSet.log);
        provider.dropSelf(woodSet.strippedLog);
        provider.dropSelf(woodSet.strippedWood);
        provider.dropSelf(woodSet.wood);
        provider.dropSelf(woodSet.sapling);
        provider.dropSelf(woodSet.stairs);
        provider.add(woodSet.slab, provider::createSlabItemTable);
        provider.dropSelf(woodSet.fence);
        provider.dropSelf(woodSet.fenceGate);
        provider.dropSelf(woodSet.door);
        provider.dropSelf(woodSet.trapdoor);
        provider.dropSelf(woodSet.pressurePlate);
        provider.dropSelf(woodSet.button);
        provider.dropOther(woodSet.sign, woodSet.signItem);
        provider.dropOther(woodSet.wallSign, woodSet.signItem);
        provider.dropOther(woodSet.hangingSign, woodSet.hangingSignItem);
        provider.dropOther(woodSet.wallHangingSign, woodSet.hangingSignItem);

        provider.add(woodSet.leaves,
                leavesBlock -> provider.createLeavesDrops(leavesBlock, woodSet.sapling, BlockLootSubProvider.NORMAL_LEAVES_SAPLING_CHANCES));
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

    public static void generateBlockStateAndModels(WoodRegistrySet woodSet, BlockModelGenerators blockStateModelGenerator) {
        blockStateModelGenerator.woodProvider(woodSet.log)
                .logWithHorizontal(woodSet.log)
                .wood(woodSet.wood);
        blockStateModelGenerator.woodProvider(woodSet.strippedLog)
                .logWithHorizontal(woodSet.strippedLog)
                .wood(woodSet.strippedWood);
        blockStateModelGenerator.createTintedLeaves(woodSet.leaves, TexturedModel.LEAVES, 0x00BB0A);
        blockStateModelGenerator.createCrossBlockWithDefaultItem(woodSet.sapling, BlockModelGenerators.PlantType.NOT_TINTED);
        blockStateModelGenerator.createHangingSign(woodSet.strippedLog, woodSet.hangingSign, woodSet.wallHangingSign);
        blockStateModelGenerator.family(woodSet.planks)
                .generateFor(woodSet.createBlockFamily());
    }

    public static void generateRecipes(WoodRegistrySet woodSet, RecipeProvider generator, RecipeOutput exporter, HolderGetter<Item> registries) {
        ShapelessRecipeBuilder.shapeless(registries, RecipeCategory.BUILDING_BLOCKS, woodSet.planks, 4)
                .requires(Ingredient.of(registries.getOrThrow(woodSet.logsItemTag)))
                .unlockedBy(hasTag(woodSet.logsItemTag), generator.has(woodSet.logsItemTag))
                .save(exporter);

        ShapedRecipeBuilder.shaped(registries, RecipeCategory.DECORATIONS, woodSet.hangingSignItem, 6)
                .define('P', woodSet.planks)
                .define('C', ConventionalItemTags.CHAINS)
                .pattern("C C")
                .pattern("PPP")
                .pattern("PPP")
                .unlockedBy(RecipeProvider.getHasName(woodSet.planks), generator.has(woodSet.planks))
                .unlockedBy(hasTag(ConventionalItemTags.CHAINS), generator.has(ConventionalItemTags.CHAINS))
                .save(exporter);

        ShapedRecipeBuilder.shaped(registries, RecipeCategory.TRANSPORTATION, woodSet.boatItem)
                .define('P', woodSet.planks)
                .pattern("P P")
                .pattern("PPP")
                .unlockedBy(RecipeProvider.getHasName(woodSet.planks), generator.has(woodSet.planks))
                .save(exporter);

        ShapelessRecipeBuilder.shapeless(registries, RecipeCategory.TRANSPORTATION, woodSet.chestBoatItem)
                .requires(Ingredient.of(registries.getOrThrow(woodSet.logsItemTag)))
                .requires(Ingredient.of(registries.getOrThrow(ConventionalItemTags.WOODEN_CHESTS)))
                .unlockedBy(hasTag(woodSet.logsItemTag), generator.has(woodSet.logsItemTag))
                .unlockedBy(hasTag(ConventionalItemTags.WOODEN_CHESTS), generator.has(ConventionalItemTags.WOODEN_CHESTS))
                .save(exporter);

        generator.generateRecipes(woodSet.createBlockFamily(), FeatureFlagSet.of());
    }

    public static void generateItemTags(WoodRegistrySet woodSet, Function<TagKey<Item>, TagAppender<Item, Item>> provider) {
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

    public static void generateBlockTags(WoodRegistrySet woodSet, Function<TagKey<Block>, TagAppender<Block, Block>> provider) {
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

    public static void generateEntityTags(WoodRegistrySet woodSet, Function<TagKey<EntityType<?>>, TagAppender<EntityType<?>, EntityType<?>>> provider) {
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
