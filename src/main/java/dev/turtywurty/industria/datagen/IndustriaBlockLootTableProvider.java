package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ItemInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class IndustriaBlockLootTableProvider extends FabricBlockLootTableProvider {
    public IndustriaBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
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

        oreDrops(BlockInit.BAUXITE_ORE, ItemInit.RAW_BAUXITE);
        oreDrops(BlockInit.TIN_ORE, ItemInit.RAW_TIN);
        oreDrops(BlockInit.ZINC_ORE, ItemInit.RAW_ZINC);
        oreDrops(BlockInit.DEEPSLATE_BAUXITE_ORE, ItemInit.RAW_BAUXITE);
        oreDrops(BlockInit.DEEPSLATE_TIN_ORE, ItemInit.RAW_TIN);
        oreDrops(BlockInit.DEEPSLATE_ZINC_ORE, ItemInit.RAW_ZINC);

        addDrop(BlockInit.ALUMINIUM_BLOCK);
        addDrop(BlockInit.TIN_BLOCK);
        addDrop(BlockInit.ZINC_BLOCK);

        addDrop(BlockInit.RAW_BAUXITE_BLOCK);
        addDrop(BlockInit.RAW_TIN_BLOCK);
        addDrop(BlockInit.RAW_ZINC_BLOCK);

        addDrop(BlockInit.MIXER);
        addDrop(BlockInit.DIGESTER);
        addDrop(BlockInit.CLARIFIER);
    }
}
