package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.block.BatteryBlock;
import dev.turtywurty.industria.init.BlockInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
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
        for(BatteryBlock block : BlockInit.BATTERIES){
            addDrop(block);
        }

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
        addDrop(BlockInit.ELECTRIC_FURNACE);
    }
}
