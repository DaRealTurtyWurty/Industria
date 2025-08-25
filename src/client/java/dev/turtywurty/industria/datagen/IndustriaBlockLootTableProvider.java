package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.util.WoodRegistrySet;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class IndustriaBlockLootTableProvider extends FabricBlockLootTableProvider {
    public IndustriaBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            WoodSetDatagen.generateBlockLootTables(woodSet, this);
        }

        addDrop(BlockInit.BAUXITE_ORE, block -> oreDrops(block, ItemInit.BAUXITE));
        addDrop(BlockInit.DEEPSLATE_BAUXITE_ORE, block -> oreDrops(block, ItemInit.BAUXITE));
        addDrop(BlockInit.RAW_BAUXITE_BLOCK);
        addDrop(BlockInit.ALUMINIUM_BLOCK);

        addDrop(BlockInit.ARGENTITE_ORE, block -> oreDrops(block, ItemInit.ARGENTITE));
        addDrop(BlockInit.DEEPSLATE_ARGENTITE_ORE, block -> oreDrops(block, ItemInit.ARGENTITE));
        addDrop(BlockInit.RAW_ARGENTITE_BLOCK);
        addDrop(BlockInit.SILVER_BLOCK);

        addDrop(BlockInit.GALENA_ORE, block -> oreDrops(block, ItemInit.GALENA));
        addDrop(BlockInit.DEEPSLATE_GALENA_ORE, block -> oreDrops(block, ItemInit.GALENA));
        addDrop(BlockInit.RAW_GALENA_BLOCK);
        addDrop(BlockInit.LEAD_BLOCK);

        addDrop(BlockInit.ILMENITE_ORE, block -> oreDrops(block, ItemInit.ILMENITE));
        addDrop(BlockInit.DEEPSLATE_ILMENITE_ORE, block -> oreDrops(block, ItemInit.ILMENITE));
        addDrop(BlockInit.RAW_ILMENITE_BLOCK);
        addDrop(BlockInit.TITANIUM_BLOCK);

        addDrop(BlockInit.SPHALERITE_ORE, block -> oreDrops(block, ItemInit.SPHALERITE));
        addDrop(BlockInit.DEEPSLATE_SPHALERITE_ORE, block -> oreDrops(block, ItemInit.SPHALERITE));
        addDrop(BlockInit.RAW_SPHALERITE_BLOCK);
        addDrop(BlockInit.ZINC_BLOCK);

        addDrop(BlockInit.COBALTITE_ORE, block -> oreDrops(block, ItemInit.COBALTITE));
        addDrop(BlockInit.DEEPSLATE_COBALTITE_ORE, block -> oreDrops(block, ItemInit.COBALTITE));
        addDrop(BlockInit.RAW_COBALTITE_BLOCK);
        addDrop(BlockInit.COBALT_BLOCK);

        addDrop(BlockInit.PENTLANDITE_ORE, block -> oreDrops(block, ItemInit.PENTLANDITE));
        addDrop(BlockInit.DEEPSLATE_PENTLANDITE_ORE, block -> oreDrops(block, ItemInit.PENTLANDITE));
        addDrop(BlockInit.RAW_PENTLANDITE_BLOCK);
        addDrop(BlockInit.NICKEL_BLOCK);

        addDrop(BlockInit.IRIDIUM_ORE);
        addDrop(BlockInit.DEEPSLATE_IRIDIUM_ORE);
        addDrop(BlockInit.IRIDIUM_BLOCK);

        addDrop(BlockInit.CASSITERITE_ORE, block -> oreDrops(block, ItemInit.CASSITERITE));
        addDrop(BlockInit.DEEPSLATE_CASSITERITE_ORE, block -> oreDrops(block, ItemInit.CASSITERITE));
        addDrop(BlockInit.RAW_CASSITERITE_BLOCK);
        addDrop(BlockInit.TIN_BLOCK);

        addDrop(BlockInit.NETHER_PYRITE_ORE, block -> oreDrops(block, ItemInit.PYRITE));
        addDrop(BlockInit.END_PYRITE_ORE, block -> oreDrops(block, ItemInit.PYRITE));
        addDrop(BlockInit.PYRITE_BLOCK);

        addDrop(BlockInit.STEEL_BLOCK);

        addDrop(BlockInit.QUARTZ_ORE, block -> oreDrops(block, Items.QUARTZ));
        addDrop(BlockInit.DEEPSLATE_QUARTZ_ORE, block -> oreDrops(block, Items.QUARTZ));

        addDrop(BlockInit.ALLOY_FURNACE);
        addDrop(BlockInit.THERMAL_GENERATOR);
        addDrop(BlockInit.BASIC_BATTERY);
        addDrop(BlockInit.ADVANCED_BATTERY);
        addDrop(BlockInit.ELITE_BATTERY);
        addDrop(BlockInit.ULTIMATE_BATTERY);
        addDrop(BlockInit.CREATIVE_BATTERY);
        addDrop(BlockInit.COMBUSTION_GENERATOR);
        addDrop(BlockInit.SOLAR_PANEL);
        addDrop(BlockInit.CRUSHER);
        addDrop(BlockInit.WIND_TURBINE);
        addDrop(BlockInit.OIL_PUMP_JACK);
        addDrop(BlockInit.DRILL);
        addDrop(BlockInit.MOTOR);
        addDrop(BlockInit.DRILL_TUBE);
        addDrop(BlockInit.UPGRADE_STATION);
        addDrop(BlockInit.ELECTRIC_FURNACE);
        addDrop(BlockInit.FRACTIONAL_DISTILLATION_CONTROLLER);
        addDrop(BlockInit.FRACTIONAL_DISTILLATION_TOWER);
        addDrop(BlockInit.INDUCTION_HEATER);
        addDrop(BlockInit.FLUID_PUMP);

        addDrop(BlockInit.CABLE);
        addDrop(BlockInit.FLUID_PIPE);
        addDrop(BlockInit.SLURRY_PIPE);
        addDrop(BlockInit.HEAT_PIPE);

        addDrop(BlockInit.MIXER);
        addDrop(BlockInit.DIGESTER);
        addDrop(BlockInit.CLARIFIER);
        addDrop(BlockInit.CRYSTALLIZER);
        addDrop(BlockInit.ROTARY_KILN_CONTROLLER, ItemInit.ROTARY_KILN);
        addDrop(BlockInit.ROTARY_KILN, ItemInit.ROTARY_KILN);
        addDrop(BlockInit.FLUID_TANK);
        addDrop(BlockInit.SHAKING_TABLE);
        addDrop(BlockInit.CENTRIFUGAL_CONCENTRATOR);
        addDrop(BlockInit.ARC_FURNACE);
    }
}
