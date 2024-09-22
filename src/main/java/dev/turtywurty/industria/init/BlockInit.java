package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BlockInit {
    public static final AlloyFurnaceBlock ALLOY_FURNACE = registerWithItem("alloy_furnace",
            new AlloyFurnaceBlock(AbstractBlock.Settings.copy(Blocks.FURNACE)));

    public static final ThermalGeneratorBlock THERMAL_GENERATOR = registerWithItem("thermal_generator",
            new ThermalGeneratorBlock(AbstractBlock.Settings.copy(Blocks.FURNACE)));

    public static final BatteryBlock BASIC_BATTERY = registerWithItem("basic_battery",
            new BatteryBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK), BatteryBlock.BatteryLevel.BASIC));

    public static final BatteryBlock ADVANCED_BATTERY = registerWithItem("advanced_battery",
            new BatteryBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK), BatteryBlock.BatteryLevel.ADVANCED));

    public static final BatteryBlock ELITE_BATTERY = registerWithItem("elite_battery",
            new BatteryBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK), BatteryBlock.BatteryLevel.ELITE));

    public static final BatteryBlock ULTIMATE_BATTERY = registerWithItem("ultimate_battery",
            new BatteryBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK), BatteryBlock.BatteryLevel.ULTIMATE));

    public static final BatteryBlock CREATIVE_BATTERY = registerWithItem("creative_battery",
            new BatteryBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK), BatteryBlock.BatteryLevel.CREATIVE));

    public static final CombustionGeneratorBlock COMBUSTION_GENERATOR = registerWithItem("combustion_generator",
            new CombustionGeneratorBlock(AbstractBlock.Settings.copy(Blocks.FURNACE)));

    public static final SolarPanelBlock SOLAR_PANEL = registerWithItem("solar_panel",
            new SolarPanelBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).nonOpaque()));

    public static final CrusherBlock CRUSHER = registerWithItem("crusher",
            new CrusherBlock(AbstractBlock.Settings.copy(Blocks.FURNACE).luminance(value -> 0).nonOpaque()));

    public static final CableBlock CABLE = registerWithItem("cable",
            new CableBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).nonOpaque()));

    public static final WindTurbineBlock WIND_TURBINE = registerWithItem("wind_turbine",
            new WindTurbineBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).nonOpaque()));

    public static final OilPumpJackBlock OIL_PUMP_JACK = registerWithItem("oil_pump_jack",
            new OilPumpJackBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).nonOpaque()));

    public static final MultiblockBlock MULTIBLOCK_BLOCK = register("multiblock",
            new MultiblockBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).nonOpaque()));

    public static final DrillBlock DRILL = registerWithItem("drill",
            new DrillBlock(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK).nonOpaque()));

    public static Block register(String name, AbstractBlock.Settings settings) {
        return register(name, new Block(settings));
    }

    public static <T extends Block> T registerWithItem(String name, T block) {
        return registerWithItem(name, block, new Item.Settings());
    }

    public static <T extends Block> T registerWithItem(String name, T block, Item.Settings itemSettings) {
        return registerWithItem(name, block, new BlockItem(block, itemSettings));
    }

    public static <T extends Block> T registerWithItem(String name, T block, Item item) {
        T registeredBlock = register(name, block);
        ItemInit.register(name, item);
        return registeredBlock;
    }

    public static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, Industria.id(name), block);
    }

    public static void init() {
    }
}
