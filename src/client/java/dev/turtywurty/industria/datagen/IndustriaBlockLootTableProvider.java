package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class IndustriaBlockLootTableProvider extends FabricBlockLootSubProvider {
    public IndustriaBlockLootTableProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            WoodSetDatagen.generateBlockLootTables(woodSet, this);
        }

        add(BlockInit.BAUXITE_ORE, block -> createOreDrop(block, ItemInit.BAUXITE));
        add(BlockInit.DEEPSLATE_BAUXITE_ORE, block -> createOreDrop(block, ItemInit.BAUXITE));
        dropSelf(BlockInit.RAW_BAUXITE_BLOCK);
        dropSelf(BlockInit.ALUMINIUM_BLOCK);

        add(BlockInit.ARGENTITE_ORE, block -> createOreDrop(block, ItemInit.ARGENTITE));
        add(BlockInit.DEEPSLATE_ARGENTITE_ORE, block -> createOreDrop(block, ItemInit.ARGENTITE));
        dropSelf(BlockInit.RAW_ARGENTITE_BLOCK);
        dropSelf(BlockInit.SILVER_BLOCK);

        add(BlockInit.GALENA_ORE, block -> createOreDrop(block, ItemInit.GALENA));
        add(BlockInit.DEEPSLATE_GALENA_ORE, block -> createOreDrop(block, ItemInit.GALENA));
        dropSelf(BlockInit.RAW_GALENA_BLOCK);
        dropSelf(BlockInit.LEAD_BLOCK);

        add(BlockInit.ILMENITE_ORE, block -> createOreDrop(block, ItemInit.ILMENITE));
        add(BlockInit.DEEPSLATE_ILMENITE_ORE, block -> createOreDrop(block, ItemInit.ILMENITE));
        dropSelf(BlockInit.RAW_ILMENITE_BLOCK);
        dropSelf(BlockInit.TITANIUM_BLOCK);

        add(BlockInit.SPHALERITE_ORE, block -> createOreDrop(block, ItemInit.SPHALERITE));
        add(BlockInit.DEEPSLATE_SPHALERITE_ORE, block -> createOreDrop(block, ItemInit.SPHALERITE));
        dropSelf(BlockInit.RAW_SPHALERITE_BLOCK);
        dropSelf(BlockInit.ZINC_BLOCK);

        add(BlockInit.COBALTITE_ORE, block -> createOreDrop(block, ItemInit.COBALTITE));
        add(BlockInit.DEEPSLATE_COBALTITE_ORE, block -> createOreDrop(block, ItemInit.COBALTITE));
        dropSelf(BlockInit.RAW_COBALTITE_BLOCK);
        dropSelf(BlockInit.COBALT_BLOCK);

        add(BlockInit.PENTLANDITE_ORE, block -> createOreDrop(block, ItemInit.PENTLANDITE));
        add(BlockInit.DEEPSLATE_PENTLANDITE_ORE, block -> createOreDrop(block, ItemInit.PENTLANDITE));
        dropSelf(BlockInit.RAW_PENTLANDITE_BLOCK);
        dropSelf(BlockInit.NICKEL_BLOCK);

        dropSelf(BlockInit.IRIDIUM_ORE);
        dropSelf(BlockInit.DEEPSLATE_IRIDIUM_ORE);
        dropSelf(BlockInit.IRIDIUM_BLOCK);

        add(BlockInit.CASSITERITE_ORE, block -> createOreDrop(block, ItemInit.CASSITERITE));
        add(BlockInit.DEEPSLATE_CASSITERITE_ORE, block -> createOreDrop(block, ItemInit.CASSITERITE));
        dropSelf(BlockInit.RAW_CASSITERITE_BLOCK);
        dropSelf(BlockInit.TIN_BLOCK);

        add(BlockInit.NETHER_PYRITE_ORE, block -> createOreDrop(block, ItemInit.PYRITE));
        add(BlockInit.END_PYRITE_ORE, block -> createOreDrop(block, ItemInit.PYRITE));
        dropSelf(BlockInit.PYRITE_BLOCK);

        dropSelf(BlockInit.STEEL_BLOCK);

        add(BlockInit.QUARTZ_ORE, block -> createOreDrop(block, Items.QUARTZ));
        add(BlockInit.DEEPSLATE_QUARTZ_ORE, block -> createOreDrop(block, Items.QUARTZ));

        dropSelf(BlockInit.ALLOY_FURNACE);
        dropSelf(BlockInit.THERMAL_GENERATOR);
        dropSelf(BlockInit.BASIC_BATTERY);
        dropSelf(BlockInit.ADVANCED_BATTERY);
        dropSelf(BlockInit.ELITE_BATTERY);
        dropSelf(BlockInit.ULTIMATE_BATTERY);
        dropSelf(BlockInit.CREATIVE_BATTERY);
        dropSelf(BlockInit.COMBUSTION_GENERATOR);
        dropSelf(BlockInit.SOLAR_PANEL);
        dropSelf(BlockInit.CRUSHER);
        dropSelf(BlockInit.WIND_TURBINE);
        dropSelf(BlockInit.OIL_PUMP_JACK);
        dropSelf(BlockInit.DRILL);
        dropSelf(BlockInit.MOTOR);
        dropSelf(BlockInit.DRILL_TUBE);
        dropSelf(BlockInit.UPGRADE_STATION);
        dropSelf(BlockInit.ELECTRIC_FURNACE);
        dropSelf(BlockInit.FRACTIONAL_DISTILLATION_CONTROLLER);
        dropSelf(BlockInit.FRACTIONAL_DISTILLATION_TOWER);
        dropSelf(BlockInit.INDUCTION_HEATER);
        dropSelf(BlockInit.FLUID_PUMP);

        dropSelf(BlockInit.CABLE);
        dropSelf(BlockInit.FLUID_PIPE);
        dropSelf(BlockInit.SLURRY_PIPE);
        dropSelf(BlockInit.HEAT_PIPE);

        dropSelf(BlockInit.MIXER);
        dropSelf(BlockInit.DIGESTER);
        dropSelf(BlockInit.CLARIFIER);
        dropSelf(BlockInit.CRYSTALLIZER);
        dropOther(BlockInit.ROTARY_KILN_CONTROLLER, ItemInit.ROTARY_KILN);
        dropOther(BlockInit.ROTARY_KILN, ItemInit.ROTARY_KILN);
        dropSelf(BlockInit.FLUID_TANK);
        dropSelf(BlockInit.SHAKING_TABLE);
        dropSelf(BlockInit.CENTRIFUGAL_CONCENTRATOR);
        dropSelf(BlockInit.ARC_FURNACE);
    }
}
