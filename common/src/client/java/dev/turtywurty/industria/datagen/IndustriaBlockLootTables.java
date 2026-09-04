package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class IndustriaBlockLootTables implements LootTableSubProvider {
    private final LootBuilders builders;
    private BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output;

    public IndustriaBlockLootTables(HolderLookup.Provider registries) {
        this.builders = new LootBuilders(registries);
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        this.output = output;
        generateTables();
        this.output = null;
    }

    private void generateTables() {
        add(ModBlocks.BAUXITE_ORE.get(), block -> createOreDrop(block, ModItems.BAUXITE.get()));
        dropSelf(ModBlocks.RAW_BAUXITE_BLOCK.get());
        dropSelf(ModBlocks.ALUMINIUM_BLOCK.get());

        add(ModBlocks.ARGENTITE_ORE.get(), block -> createOreDrop(block, ModItems.ARGENTITE.get()));
        add(ModBlocks.DEEPSLATE_ARGENTITE_ORE.get(), block -> createOreDrop(block, ModItems.ARGENTITE.get()));
        dropSelf(ModBlocks.RAW_ARGENTITE_BLOCK.get());
        dropSelf(ModBlocks.SILVER_BLOCK.get());

        add(ModBlocks.GALENA_ORE.get(), block -> createOreDrop(block, ModItems.GALENA.get()));
        add(ModBlocks.DEEPSLATE_GALENA_ORE.get(), block -> createOreDrop(block, ModItems.GALENA.get()));
        dropSelf(ModBlocks.RAW_GALENA_BLOCK.get());
        dropSelf(ModBlocks.LEAD_BLOCK.get());

        add(ModBlocks.ILMENITE_ORE.get(), block -> createOreDrop(block, ModItems.ILMENITE.get()));
        add(ModBlocks.DEEPSLATE_ILMENITE_ORE.get(), block -> createOreDrop(block, ModItems.ILMENITE.get()));
        dropSelf(ModBlocks.RAW_ILMENITE_BLOCK.get());
        dropSelf(ModBlocks.TITANIUM_BLOCK.get());

        add(ModBlocks.SPHALERITE_ORE.get(), block -> createOreDrop(block, ModItems.SPHALERITE.get()));
        add(ModBlocks.DEEPSLATE_SPHALERITE_ORE.get(), block -> createOreDrop(block, ModItems.SPHALERITE.get()));
        dropSelf(ModBlocks.RAW_SPHALERITE_BLOCK.get());
        dropSelf(ModBlocks.ZINC_BLOCK.get());

        add(ModBlocks.COBALTITE_ORE.get(), block -> createOreDrop(block, ModItems.COBALTITE.get()));
        add(ModBlocks.DEEPSLATE_COBALTITE_ORE.get(), block -> createOreDrop(block, ModItems.COBALTITE.get()));
        dropSelf(ModBlocks.RAW_COBALTITE_BLOCK.get());
        dropSelf(ModBlocks.COBALT_BLOCK.get());

        add(ModBlocks.PENTLANDITE_ORE.get(), block -> createOreDrop(block, ModItems.PENTLANDITE.get()));
        add(ModBlocks.DEEPSLATE_PENTLANDITE_ORE.get(), block -> createOreDrop(block, ModItems.PENTLANDITE.get()));
        dropSelf(ModBlocks.RAW_PENTLANDITE_BLOCK.get());
        dropSelf(ModBlocks.NICKEL_BLOCK.get());

        dropSelf(ModBlocks.IRIDIUM_ORE.get());
        dropSelf(ModBlocks.DEEPSLATE_IRIDIUM_ORE.get());
        dropSelf(ModBlocks.IRIDIUM_BLOCK.get());

        add(ModBlocks.CASSITERITE_ORE.get(), block -> createOreDrop(block, ModItems.CASSITERITE.get()));
        add(ModBlocks.DEEPSLATE_CASSITERITE_ORE.get(), block -> createOreDrop(block, ModItems.CASSITERITE.get()));
        dropSelf(ModBlocks.RAW_CASSITERITE_BLOCK.get());
        dropSelf(ModBlocks.TIN_BLOCK.get());

        add(ModBlocks.NETHER_PYRITE_ORE.get(), block -> createOreDrop(block, ModItems.PYRITE.get()));
        add(ModBlocks.END_PYRITE_ORE.get(), block -> createOreDrop(block, ModItems.PYRITE.get()));
        dropSelf(ModBlocks.PYRITE_BLOCK.get());

        dropSelf(ModBlocks.STEEL_BLOCK.get());

        add(ModBlocks.QUARTZ_ORE.get(), block -> createOreDrop(block, Items.QUARTZ));
        add(ModBlocks.DEEPSLATE_QUARTZ_ORE.get(), block -> createOreDrop(block, Items.QUARTZ));

        dropSelf(ModBlocks.ALLOY_FURNACE.get());
        dropSelf(ModBlocks.THERMAL_GENERATOR.get());
        dropSelf(ModBlocks.BASIC_BATTERY.get());
        dropSelf(ModBlocks.ADVANCED_BATTERY.get());
        dropSelf(ModBlocks.ELITE_BATTERY.get());
        dropSelf(ModBlocks.ULTIMATE_BATTERY.get());
        dropSelf(ModBlocks.CREATIVE_BATTERY.get());
        dropSelf(ModBlocks.COMBUSTION_GENERATOR.get());
        dropSelf(ModBlocks.SOLAR_PANEL.get());
        dropSelf(ModBlocks.ADVANCED_SOLAR_PANEL.get());
        dropSelf(ModBlocks.CRUSHER.get());
        dropSelf(ModBlocks.WIND_TURBINE.get());
        dropSelf(ModBlocks.OIL_PUMP_JACK.get());
        dropSelf(ModBlocks.DRILL.get());
        dropSelf(ModBlocks.MOTOR.get());
        dropSelf(ModBlocks.DRILL_TUBE.get());
        dropSelf(ModBlocks.UPGRADE_STATION.get());
        dropSelf(ModBlocks.ELECTRIC_FURNACE.get());
        dropSelf(ModBlocks.INDUCTION_HEATER.get());
        dropSelf(ModBlocks.FLUID_PUMP.get());

        dropSelf(ModBlocks.CABLE.get());
        dropSelf(ModBlocks.FLUID_PIPE.get());
        dropSelf(ModBlocks.SLURRY_PIPE.get());
        dropSelf(ModBlocks.GAS_PIPE.get());
        dropSelf(ModBlocks.CONVEYOR.get());
        dropSelf(ModBlocks.SPLITTER_CONVEYOR.get());
        dropSelf(ModBlocks.MERGER_CONVEYOR.get());
        dropSelf(ModBlocks.ALTERNATOR_CONVEYOR.get());
        dropSelf(ModBlocks.FEEDER_CONVEYOR.get());
        dropSelf(ModBlocks.HATCH_CONVEYOR.get());
        dropSelf(ModBlocks.SIDE_INJECTOR_CONVEYOR.get());
        dropSelf(ModBlocks.LADDER_CONVEYOR.get());
        dropSelf(ModBlocks.FILTER_CONVEYOR.get());
        dropSelf(ModBlocks.MAGNETIC_CONVEYOR.get());
        dropSelf(ModBlocks.DETECTOR_CONVEYOR.get());
        dropSelf(ModBlocks.DROP_CHUTE_CONVEYOR.get());
        dropSelf(ModBlocks.COUNT_CONVEYOR.get());
        dropSelf(ModBlocks.DELAY_CONVEYOR.get());
        dropSelf(ModBlocks.CONTAINMENT_CONVEYOR.get());

        dropSelf(ModBlocks.MIXER.get());
        dropSelf(ModBlocks.DIGESTER.get());
        dropSelf(ModBlocks.CLARIFIER.get());
        dropSelf(ModBlocks.CRYSTALLIZER.get());
        dropOther(ModBlocks.ROTARY_KILN_CONTROLLER.get(), ModItems.ROTARY_KILN.get());
        dropOther(ModBlocks.ROTARY_KILN.get(), ModItems.ROTARY_KILN.get());
        dropSelf(ModBlocks.FLUID_TANK.get());
        dropSelf(ModBlocks.SHAKING_TABLE.get());
        dropSelf(ModBlocks.CENTRIFUGAL_CONCENTRATOR.get());
        dropSelf(ModBlocks.ARC_FURNACE.get());
        dropSelf(ModBlocks.TREE_TAP.get());
        dropSelf(ModBlocks.AGITATOR.get());
        dropSelf(ModBlocks.DISTILLATION_TOWER.get());
    }

    private void add(Block block, Function<Block, LootTable.Builder> factory) {
        accept(block, factory.apply(block));
    }

    private void dropSelf(Block block) {
        accept(block, this.builders.self(block));
    }

    private void dropOther(Block block, Item item) {
        accept(block, this.builders.other(item));
    }

    private LootTable.Builder createOreDrop(Block block, Item item) {
        return this.builders.ore(block, item);
    }

    private void accept(Block block, LootTable.Builder table) {
        this.output.accept(block.getLootTable().orElseThrow(), table);
    }

    private static final class LootBuilders extends BlockLootSubProvider {
        private LootBuilders(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
        }

        private LootTable.Builder self(Block block) {
            return createSingleItemTable(block);
        }

        private LootTable.Builder other(Item item) {
            return createSingleItemTable(item);
        }

        private LootTable.Builder ore(Block block, Item item) {
            return createOreDrop(block, item);
        }

        @Override
        public void generate() {
        }
    }
}
