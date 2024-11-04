package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.init.BlockInit;
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
        addDrop(BlockInit.CABLE);
        addDrop(BlockInit.WIND_TURBINE);
        addDrop(BlockInit.OIL_PUMP_JACK);
        addDrop(BlockInit.DRILL);
        addDrop(BlockInit.MOTOR);
        addDrop(BlockInit.DRILL_TUBE);
        addDrop(BlockInit.UPGRADE_STATION);
    }
}
