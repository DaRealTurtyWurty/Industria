package dev.turtywurty.industria.datagen;

import dev.turtywurty.slurryapi.api.SlurryVariant;
import dev.turtywurty.gasapi.api.GasVariant;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.gas.GasStack;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.datagen.builder.*;
import dev.turtywurty.industria.init.*;
import dev.turtywurty.industria.recipe.AgitatorRecipe;
import dev.turtywurty.industria.util.AgitatorPortType;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.turtymultiloader.datagen.convention.ConventionTags;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
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
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class IndustriaRecipes {
    private IndustriaRecipes() {
    }

    public static void generate(HolderLookup.Provider wrapperLookup, RecipeOutput exporter) {
        new RecipeProvider(wrapperLookup, exporter) {
            @Override
            public void buildRecipes() {
                HolderGetter<Item> itemLookup = wrapperLookup.lookupOrThrow(Registries.ITEM);

                shaped(RecipeCategory.MISC, ModBlocks.ALLOY_FURNACE.get())
                        .pattern("AAA")
                        .pattern("ABA")
                        .pattern("AAA")
                        .define('A', ConventionalItemTags.STORAGE_BLOCKS_COPPER)
                        .define('B', ConventionalItemTags.STORAGE_BLOCKS_IRON)
                        .unlockedBy(hasTag(ConventionalItemTags.STORAGE_BLOCKS_COPPER), has(ConventionalItemTags.STORAGE_BLOCKS_COPPER))
                        .unlockedBy(hasTag(ConventionalItemTags.STORAGE_BLOCKS_IRON), has(ConventionalItemTags.STORAGE_BLOCKS_IRON))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.THERMAL_GENERATOR.get())
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
                        new ItemStackTemplate(ModItems.STEEL_INGOT.get()),
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
//                ModItems.IRON_DUST.get().getDefaultStack(),
//                1.0F,
//                ModItems.IRON_DUST.get().getDefaultStack(),
//                0.1F,
//                200);

//        offerCrusher(exporter, RecipeCategory.MISC,
//                new IndustriaIngredient(itemLookup.getOrThrow(ConventionalItemTags.GOLD_ORES), 1),
//                ModItems.GOLD_DUST.get().getDefaultStack(),
//                1.0F,
//                ModItems.GOLD_DUST.get().getDefaultStack(),
//                0.1F,
//                200);

//        offerCrusher(exporter, RecipeCategory.MISC,
//                new IndustriaIngredient(itemLookup.getOrThrow(ConventionalItemTags.COPPER_RAW_MATERIALS), 1),
//                ModItems.COPPER_DUST.get().getDefaultStack(),
//                1.0F,
//                ModItems.COPPER_DUST.get().getDefaultStack(),
//                0.1F,
//                200);

                shaped(RecipeCategory.MISC, ModBlocks.CABLE.get(), 8)
                        .pattern("III")
                        .pattern("IRI")
                        .pattern("III")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('R', ConventionalItemTags.REDSTONE_DUSTS)
                        .unlockedBy(hasTag(ConventionalItemTags.IRON_INGOTS), has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy(hasTag(ConventionalItemTags.REDSTONE_DUSTS), has(ConventionalItemTags.REDSTONE_DUSTS))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.GAS_PIPE.get(), 8)
                        .pattern("III")
                        .pattern("IGI")
                        .pattern("III")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('G', Items.GLASS_BOTTLE)
                        .unlockedBy(hasTag(ConventionalItemTags.IRON_INGOTS), has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy(getHasName(Items.GLASS_BOTTLE), has(Items.GLASS_BOTTLE))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.CONVEYOR.get(), 8)
                        .pattern("III")
                        .pattern("RRR")
                        .pattern("III")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('R', ModItems.RUBBER.get())
                        .unlockedBy(hasTag(ConventionalItemTags.IRON_INGOTS), has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy(getHasName(ModItems.RUBBER.get()), has(ModItems.RUBBER.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.SPLITTER_CONVEYOR.get(), 8)
                        .pattern("CCC")
                        .pattern("CQC")
                        .pattern("CCC")
                        .define('C', ModBlocks.CONVEYOR.get())
                        .define('Q', ConventionalItemTags.QUARTZ_GEMS)
                        .unlockedBy(hasTag(ConventionalItemTags.QUARTZ_GEMS), has(ConventionalItemTags.QUARTZ_GEMS))
                        .unlockedBy(getHasName(ModBlocks.CONVEYOR.get()), has(ModBlocks.CONVEYOR.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.MERGER_CONVEYOR.get(), 8)
                        .pattern("CCC")
                        .pattern("CLC")
                        .pattern("CCC")
                        .define('C', ModBlocks.CONVEYOR.get())
                        .define('L', ConventionalItemTags.LAPIS_GEMS)
                        .unlockedBy(hasTag(ConventionalItemTags.LAPIS_GEMS), has(ConventionalItemTags.LAPIS_GEMS))
                        .unlockedBy(getHasName(ModBlocks.CONVEYOR.get()), has(ModBlocks.CONVEYOR.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.ALTERNATOR_CONVEYOR.get(), 8)
                        .pattern("CCC")
                        .pattern("CRC")
                        .pattern("CCC")
                        .define('C', ModBlocks.CONVEYOR.get())
                        .define('R', Items.LEVER)
                        .unlockedBy(getHasName(Items.LEVER), has(Items.LEVER))
                        .unlockedBy(getHasName(ModBlocks.CONVEYOR.get()), has(ModBlocks.CONVEYOR.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.FEEDER_CONVEYOR.get(), 8)
                        .pattern("CCC")
                        .pattern("CRC")
                        .pattern("CCC")
                        .define('C', ModBlocks.CONVEYOR.get())
                        .define('R', ConventionalItemTags.REDSTONE_DUSTS)
                        .unlockedBy(hasTag(ConventionalItemTags.REDSTONE_DUSTS), has(ConventionalItemTags.REDSTONE_DUSTS))
                        .unlockedBy(getHasName(ModBlocks.CONVEYOR.get()), has(ModBlocks.CONVEYOR.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.HATCH_CONVEYOR.get(), 8)
                        .pattern("CCC")
                        .pattern("CHC")
                        .pattern("CCC")
                        .define('C', ModBlocks.CONVEYOR.get())
                        .define('H', Items.HOPPER)
                        .unlockedBy(getHasName(Items.HOPPER), has(Items.HOPPER))
                        .unlockedBy(getHasName(ModBlocks.CONVEYOR.get()), has(ModBlocks.CONVEYOR.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.SIDE_INJECTOR_CONVEYOR.get(), 8)
                        .pattern("CCC")
                        .pattern("CPC")
                        .pattern("CCC")
                        .define('C', ModBlocks.CONVEYOR.get())
                        .define('P', Items.PISTON)
                        .unlockedBy(getHasName(Items.PISTON), has(Items.PISTON))
                        .unlockedBy(getHasName(ModBlocks.CONVEYOR.get()), has(ModBlocks.CONVEYOR.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.LADDER_CONVEYOR.get(), 8)
                        .pattern("CCC")
                        .pattern("CLC")
                        .pattern("CCC")
                        .define('C', ModBlocks.CONVEYOR.get())
                        .define('L', Items.LADDER)
                        .unlockedBy(getHasName(Items.LADDER), has(Items.LADDER))
                        .unlockedBy(getHasName(ModBlocks.CONVEYOR.get()), has(ModBlocks.CONVEYOR.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.DISTILLATION_TOWER.get())
                        .pattern("IGI")
                        .pattern("TMT")
                        .pattern("IPI")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('G', Blocks.GLASS)
                        .define('T', ModBlocks.FLUID_TANK.get())
                        .define('M', ModBlocks.MOTOR.get())
                        .define('P', ModBlocks.FLUID_PIPE.get())
                        .unlockedBy(hasTag(ConventionalItemTags.IRON_INGOTS), has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy(getHasName(Blocks.GLASS), has(Blocks.GLASS))
                        .unlockedBy(getHasName(ModBlocks.FLUID_TANK.get()), has(ModBlocks.FLUID_TANK.get()))
                        .unlockedBy(getHasName(ModBlocks.MOTOR.get()), has(ModBlocks.MOTOR.get()))
                        .unlockedBy(getHasName(ModBlocks.FLUID_PIPE.get()), has(ModBlocks.FLUID_PIPE.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.FILTER_CONVEYOR.get(), 8)
                        .pattern("CCC")
                        .pattern("CPC")
                        .pattern("CCC")
                        .define('C', ModBlocks.CONVEYOR.get())
                        .define('P', Items.PAPER)
                        .unlockedBy(getHasName(Items.PAPER), has(Items.PAPER))
                        .unlockedBy(getHasName(ModBlocks.CONVEYOR.get()), has(ModBlocks.CONVEYOR.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.MAGNETIC_CONVEYOR.get(), 8)
                        .pattern("CCC")
                        .pattern("CPC")
                        .pattern("CCC")
                        .define('C', ModBlocks.CONVEYOR.get())
                        .define('P', Items.COMPASS)
                        .unlockedBy(getHasName(Items.COMPASS), has(Items.COMPASS))
                        .unlockedBy(getHasName(ModBlocks.CONVEYOR.get()), has(ModBlocks.CONVEYOR.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.DETECTOR_CONVEYOR.get(), 8)
                        .pattern("CCC")
                        .pattern("CDC")
                        .pattern("CCC")
                        .define('C', ModBlocks.CONVEYOR.get())
                        .define('D', Items.COMPARATOR)
                        .unlockedBy(getHasName(Items.COMPARATOR), has(Items.COMPARATOR))
                        .unlockedBy(getHasName(ModBlocks.CONVEYOR.get()), has(ModBlocks.CONVEYOR.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.DROP_CHUTE_CONVEYOR.get(), 8)
                        .pattern("CCC")
                        .pattern("CDC")
                        .pattern("CCC")
                        .define('C', ModBlocks.CONVEYOR.get())
                        .define('D', Items.DROPPER)
                        .unlockedBy(getHasName(Items.DROPPER), has(Items.DROPPER))
                        .unlockedBy(getHasName(ModBlocks.CONVEYOR.get()), has(ModBlocks.CONVEYOR.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.COUNT_CONVEYOR.get(), 8)
                        .pattern("CCC")
                        .pattern("CDC")
                        .pattern("CCC")
                        .define('C', ModBlocks.CONVEYOR.get())
                        .define('D', Items.COMPARATOR)
                        .unlockedBy(getHasName(Items.COMPARATOR), has(Items.COMPARATOR))
                        .unlockedBy(getHasName(ModBlocks.CONVEYOR.get()), has(ModBlocks.CONVEYOR.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.DELAY_CONVEYOR.get(), 8)
                        .pattern("CCC")
                        .pattern("CRC")
                        .pattern("CCC")
                        .define('C', ModBlocks.CONVEYOR.get())
                        .define('R', Items.REPEATER)
                        .unlockedBy(getHasName(Items.REPEATER), has(Items.REPEATER))
                        .unlockedBy(getHasName(ModBlocks.CONVEYOR.get()), has(ModBlocks.CONVEYOR.get()))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.CONTAINMENT_CONVEYOR.get(), 8)
                        .pattern("CCC")
                        .pattern("CMC")
                        .pattern("CCC")
                        .define('C', ModBlocks.CONVEYOR.get())
                        .define('M', ModItems.EMPTY_MOB_JAR.get())
                        .unlockedBy(getHasName(ModItems.EMPTY_MOB_JAR.get()), has(ModItems.EMPTY_MOB_JAR.get()))
                        .unlockedBy(getHasName(ModBlocks.CONVEYOR.get()), has(ModBlocks.CONVEYOR.get()))
                        .save(output);

                nineBlockStorageRecipes(RecipeCategory.MISC, ModItems.ALUMINIUM_INGOT.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.ALUMINIUM_BLOCK.get());
                nineBlockStorageRecipes(RecipeCategory.MISC, ModItems.TIN_INGOT.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.TIN_BLOCK.get());
                nineBlockStorageRecipes(RecipeCategory.MISC, ModItems.ZINC_INGOT.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.ZINC_BLOCK.get());

                nineBlockStorageRecipes(RecipeCategory.MISC, ModItems.ALUMINIUM_NUGGET.get(), RecipeCategory.MISC, ModItems.ALUMINIUM_INGOT.get(), "aluminium_nugget_to_ingot", null, "aluminium_ingot_to_nugget", null);
                nineBlockStorageRecipes(RecipeCategory.MISC, ModItems.TIN_NUGGET.get(), RecipeCategory.MISC, ModItems.TIN_INGOT.get(), "tin_nugget_to_ingot", null, "tin_ingot_to_nugget", null);
                nineBlockStorageRecipes(RecipeCategory.MISC, ModItems.ZINC_NUGGET.get(), RecipeCategory.MISC, ModItems.ZINC_INGOT.get(), "zinc_nugget_to_ingot", null, "zinc_ingot_to_nugget", null);

                nineBlockStorageRecipes(RecipeCategory.MISC, ModItems.BAUXITE.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.RAW_BAUXITE_BLOCK.get());
                nineBlockStorageRecipes(RecipeCategory.MISC, ModItems.CASSITERITE.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.RAW_CASSITERITE_BLOCK.get());
                nineBlockStorageRecipes(RecipeCategory.MISC, ModItems.SPHALERITE.get(), RecipeCategory.BUILDING_BLOCKS, ModBlocks.RAW_SPHALERITE_BLOCK.get());

                List<ItemLike> aluminiumOres = List.of(ModBlocks.BAUXITE_ORE.get(), ModItems.BAUXITE.get());
                List<ItemLike> tinOres = List.of(ModBlocks.CASSITERITE_ORE.get(), ModBlocks.DEEPSLATE_CASSITERITE_ORE.get(), ModItems.CASSITERITE.get());
                List<ItemLike> zincOres = List.of(ModBlocks.SPHALERITE_ORE.get(), ModBlocks.DEEPSLATE_SPHALERITE_ORE.get(), ModItems.SPHALERITE.get());

                oreSmelting(aluminiumOres, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.ALUMINIUM_INGOT.get(), 0.7F, 200, "aluminium_ingot");
                oreSmelting(tinOres, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.TIN_INGOT.get(), 0.7F, 200, "tin_ingot");
                oreSmelting(zincOres, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.ZINC_INGOT.get(), 0.7F, 200, "zinc_ingot");

                oreBlasting(aluminiumOres, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.ALUMINIUM_INGOT.get(), 0.7F, 100, "aluminium_ingot");
                oreBlasting(tinOres, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.TIN_INGOT.get(), 0.7F, 100, "tin_ingot");
                oreBlasting(zincOres, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.ZINC_INGOT.get(), 0.7F, 100, "zinc_ingot");

                offerCrusher(output, RecipeCategory.MISC,
                        new IndustriaIngredient(1, ModItems.ALUMINIUM_INGOT.get()),
                        new OutputItemStack(ModItems.ALUMINIUM_POWDER.get(), 1, 1),
                        OutputItemStack.EMPTY,
                        100, "aluminium_ingot_to_powder");

                offerMixer(output, RecipeCategory.MISC, List.of(
                                new IndustriaIngredient(4, ModItems.BAUXITE.get()),
                                new IndustriaIngredient(1, ModItems.SODIUM_HYDROXIDE.get())),
                        new FluidStack(ResourceTypes.FLUID.of(Fluids.WATER.builtInRegistryHolder()), FluidAmounts.BUCKET),
                        170, 180,
                        OutputItemStack.EMPTY,
                        new SlurryStack(SlurryVariant.of(ModSlurries.BAUXITE_SLURRY.holder()), FluidAmounts.BUCKET),
                        200, "bauxite_to_bauxite_slurry");

                offerDigester(output, new SlurryStack(SlurryVariant.of(ModSlurries.BAUXITE_SLURRY.holder()), FluidAmounts.BUCKET),
                        new FluidStack(ResourceTypes.FLUID.of(ModFluids.DIRTY_SODIUM_ALUMINATE.still().holder()), FluidAmounts.BOTTLE),
                        200, "bauxite_to_dirty_sodium_aluminate");

                offerClarifier(output, new FluidStack(ResourceTypes.FLUID.of(ModFluids.DIRTY_SODIUM_ALUMINATE.still().holder()), FluidAmounts.BUCKET),
                        new FluidStack(ResourceTypes.FLUID.of(ModFluids.SODIUM_ALUMINATE.still().holder()), FluidAmounts.BOTTLE),
                        new OutputItemStack(ModItems.RED_MUD.get(), UniformInt.of(1, 3), 1),
                        500, "dirty_sodium_aluminate_to_sodium_aluminate");

                offerCrystallizerRecipe(output, new FluidStack(ResourceTypes.FLUID.of(Fluids.WATER.builtInRegistryHolder()), FluidAmounts.BUCKET * 5),
                        new FluidStack(ResourceTypes.FLUID.of(ModFluids.SODIUM_ALUMINATE.still().holder()), FluidAmounts.BUCKET),
                        new IndustriaIngredient(1, ModItems.ALUMINIUM_HYDROXIDE.get()),
                        new OutputItemStack(ModItems.ALUMINIUM_HYDROXIDE.get(), 8, 1),
                        new OutputItemStack(ModItems.SODIUM_CARBONATE.get(), UniformInt.of(8, 16), 0.75F),
                        false, 5, 1000, "aluminium_hydroxide");

                offerRotaryKilnRecipe(output,
                        new IndustriaIngredient(1, ModItems.ALUMINIUM_HYDROXIDE.get()),
                        new OutputItemStack(ModItems.ALUMINA.get(), 1, 1),
                        1200);

                offerElectrolyzerRecipe(output,
                        new IndustriaIngredient(3, ModItems.ALUMINA.get()),
                        new IndustriaIngredient(1, ModItems.CARBON_ROD.get()),
                        new IndustriaIngredient(1, Items.COAL),
                        new IndustriaIngredient(9, ModItems.CRYOLITE.get()),
                        new FluidStack(ResourceTypes.FLUID.of(ModFluids.MOLTEN_CRYOLITE.still().holder()), FluidAmounts.BUCKET),
                        new FluidStack(ResourceTypes.FLUID.of(ModFluids.MOLTEN_ALUMINIUM.still().holder()), FluidAmounts.BUCKET * 2),
                        new GasStack(GasVariant.of(ModGases.CARBON_DIOXIDE.holder()), FluidAmounts.NUGGET),
                        2_000, 10_000, 1_000);

                offerCrusher(output, RecipeCategory.MISC,
                        new IndustriaIngredient(1, ModItems.CASSITERITE.get()),
                        new OutputItemStack(ModItems.CRUSHED_CASSITERITE.get(), 2, 1),
                        new OutputItemStack(ModItems.CRUSHED_CASSITERITE.get(), 1, (3 / 10F)),
                        100, "raw_cassiterite");

                offerShakingTableRecipe(output,
                        new IndustriaIngredient(1, ModItems.CRUSHED_CASSITERITE.get()),
                        new OutputItemStack(ModItems.CASSITERITE_CONCENTRATE.get(), 1, 1),
                        new SlurryStack(SlurryVariant.of(ModSlurries.CLAY_SLURRY.holder()), FluidAmounts.BUCKET / 2),
                        200, 4, RecipeCategory.MISC);

                offerCentrifugalConcentratorRecipe(output,
                        new IndustriaIngredient(1, ModItems.CASSITERITE_CONCENTRATE.get()),
                        new OutputItemStack(ModItems.CASSITERITE_CONCENTRATE.get(), 1, 1),
                        new SlurryStack(SlurryVariant.of(ModSlurries.CLAY_SLURRY.holder()), FluidAmounts.BUCKET / 4),
                        200, 500, RecipeCategory.MISC);

                offerAgitatorRecipe(output,
                        List.of(
                                new AgitatorRecipe.AgitatorInput(AgitatorPortType.GAS, IndustriaIngredient.EMPTY, FluidStack.EMPTY,
                                        new GasStack(GasVariant.of(ModGases.CARBON_MONOXIDE.holder()), FluidAmounts.BOTTLE), SlurryStack.EMPTY),
                                new AgitatorRecipe.AgitatorInput(AgitatorPortType.FLUID, IndustriaIngredient.EMPTY,
                                        new FluidStack(ResourceTypes.FLUID.of(ModFluids.METHANOL.still().holder()), FluidAmounts.BOTTLE),
                                        GasStack.EMPTY, SlurryStack.EMPTY),
                                new AgitatorRecipe.AgitatorInput(AgitatorPortType.ITEM, IndustriaIngredient.EMPTY,
                                        FluidStack.EMPTY, GasStack.EMPTY, SlurryStack.EMPTY)
                        ),
                        List.of(
                                new AgitatorRecipe.AgitatorOutput(AgitatorPortType.FLUID, OutputItemStack.EMPTY,
                                        new FluidStack(ResourceTypes.FLUID.of(ModFluids.DILUTED_FORMIC_ACID.still().holder()), FluidAmounts.BOTTLE),
                                        GasStack.EMPTY, SlurryStack.EMPTY),
                                new AgitatorRecipe.AgitatorOutput(AgitatorPortType.GAS, OutputItemStack.EMPTY,
                                        FluidStack.EMPTY, new GasStack(GasVariant.of(ModGases.HYDROGEN.holder()), FluidAmounts.BOTTLE), SlurryStack.EMPTY)
                        ),
                        200, 1_000, "carbon_monoxide_and_methanol");

                offerDistillationTower(output,
                        new FluidStack(ResourceTypes.FLUID.of(ModFluids.DILUTED_FORMIC_ACID.still().holder()), FluidAmounts.BUCKET),
                        new FluidStack(ResourceTypes.FLUID.of(ModFluids.FORMIC_ACID.still().holder()), FluidAmounts.BOTTLE * 3),
                        new FluidStack(ResourceTypes.FLUID.of(Fluids.WATER.builtInRegistryHolder()), FluidAmounts.BOTTLE),
                        240,
                        40,
                        "diluted_formic_acid");
            }
        }.buildRecipes();
    }

    private static void offerAlloySmelting(RecipeOutput exporter, RecipeCategory category, IndustriaIngredient inputA, IndustriaIngredient inputB, ItemStackTemplate output, int smeltTime) {
        offerAlloySmelting(exporter, category, inputA, inputB, output, smeltTime, getSimpleRecipeName(output.item().value()));
    }

    private static void offerAlloySmelting(RecipeOutput exporter, RecipeCategory category, IndustriaIngredient inputA, IndustriaIngredient inputB, ItemStackTemplate output, int smeltTime, String name) {
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
        new RotaryKilnRecipeBuilder(input, output, requiredTemperature).offerTo(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("rotary_kiln_" + getSimpleRecipeName(output.item()))));
    }

    private static void offerElectrolyzerRecipe(RecipeOutput exporter,
                                                IndustriaIngredient input,
                                                IndustriaIngredient anode, IndustriaIngredient cathode,
                                                IndustriaIngredient electrolyteItem, FluidStack electrolyteFluid,
                                                FluidStack outputFluid, GasStack outputGas,
                                                int processTime, int energyCost, int temperature) {
        new ElectrolyzerRecipeBuilder(input, anode, cathode, electrolyteItem, electrolyteFluid, outputFluid, outputGas, processTime, energyCost, temperature)
                .offerTo(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("electrolyzer_" + getRecipeName(outputFluid.variant().value()))));
    }

    private static void offerShakingTableRecipe(RecipeOutput exporter, IndustriaIngredient input, OutputItemStack output, @Nullable SlurryStack outputSlurry, int processTime, int frequency, RecipeCategory category) {
        new ShakingTableRecipeBuilder(input, output, outputSlurry, processTime, frequency, category)
                .save(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("shaking_table_" + getSimpleRecipeName(output.item()))));
    }

    private static void offerCentrifugalConcentratorRecipe(RecipeOutput exporter, IndustriaIngredient input, OutputItemStack output, @Nullable SlurryStack outputSlurry, int processTime, int rpm, RecipeCategory category) {
        new CentrifugalConcentratorRecipeBuilder(input, output, outputSlurry, processTime, rpm, category)
                .save(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("centrifugal_concentrator_" + getSimpleRecipeName(output.item()))));
    }

    private static void offerAgitatorRecipe(RecipeOutput exporter,
                                            List<AgitatorRecipe.AgitatorInput> inputs,
                                            List<AgitatorRecipe.AgitatorOutput> outputs,
                                            int processTime, int energyCost, String name) {
        new AgitatorRecipeBuilder(inputs, outputs, processTime, energyCost)
                .offerTo(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("agitator_" + name)));
    }

    private static void offerDistillationTower(RecipeOutput exporter, FluidStack inputFluid, FluidStack primaryOutputFluid,
                                               FluidStack secondaryOutputFluid, int processTime, int energyCost, String name) {
        new DistillationTowerRecipeBuilder(inputFluid, primaryOutputFluid, secondaryOutputFluid, processTime, energyCost)
                .offerTo(exporter, ResourceKey.create(Registries.RECIPE, Industria.id("distillation_tower_" + name)));
    }

    public static String getRecipeName(Fluid fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid).getPath();
    }

    private static String getSimpleRecipeName(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }

    public static @NotNull String hasTag(@NotNull TagKey<Item> tag) {
        return "has_" + tag.location().toString();
    }

    private static final class ConventionalItemTags {
        private static final TagKey<Item> COPPER_RAW_MATERIALS = ConventionTags.item("raw_materials/copper").key();
        private static final TagKey<Item> GOLD_ORES = ConventionTags.item("ores/gold").key();
        private static final TagKey<Item> IRON_INGOTS = ConventionTags.item("ingots/iron").key();
        private static final TagKey<Item> IRON_RAW_MATERIALS = ConventionTags.item("raw_materials/iron").key();
        private static final TagKey<Item> LAPIS_GEMS = ConventionTags.item("gems/lapis").key();
        private static final TagKey<Item> PLAYER_WORKSTATIONS_FURNACES = ConventionTags.item("player_workstations/furnaces").key();
        private static final TagKey<Item> QUARTZ_GEMS = ConventionTags.item("gems/quartz").key();
        private static final TagKey<Item> QUARTZ_ORES = ConventionTags.item("ores/quartz").key();
        private static final TagKey<Item> REDSTONE_DUSTS = ConventionTags.item("dusts/redstone").key();
        private static final TagKey<Item> STORAGE_BLOCKS_COAL = ConventionTags.item("storage_blocks/coal").key();
        private static final TagKey<Item> STORAGE_BLOCKS_COPPER = ConventionTags.item("storage_blocks/copper").key();
        private static final TagKey<Item> STORAGE_BLOCKS_IRON = ConventionTags.item("storage_blocks/iron").key();
        private static final TagKey<Item> STORAGE_BLOCKS_REDSTONE = ConventionTags.item("storage_blocks/redstone").key();

        private ConventionalItemTags() {
        }
    }
}
