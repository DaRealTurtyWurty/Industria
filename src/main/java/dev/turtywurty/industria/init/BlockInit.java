package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.*;
import dev.turtywurty.industria.util.enums.ElectricityTypes;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;


import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;

public class BlockInit {

    public static final List<BatteryBlock> BATTERIES = new ArrayList();

    public static final AlloyFurnaceBlock ALLOY_FURNACE = registerWithItemCopy("alloy_furnace",
            AlloyFurnaceBlock::new, Blocks.FURNACE, AbstractBlock.Settings::nonOpaque);

    public static final ThermalGeneratorBlock THERMAL_GENERATOR = registerWithItemCopy("thermal_generator",
            ThermalGeneratorBlock::new, Blocks.FURNACE, AbstractBlock.Settings::nonOpaque);


    public static void registerBatteries(){
        for(ElectricityTypes types : ElectricityTypes.values()){
            BATTERIES.add(registerWithItemCopy(types.getId()+"_battery",
                    settings -> new BatteryBlock(settings, types), Blocks.IRON_BLOCK, settings -> settings));
        }
    }

    public static final CombustionGeneratorBlock COMBUSTION_GENERATOR = registerWithItemCopy("combustion_generator",
            CombustionGeneratorBlock::new, Blocks.FURNACE, AbstractBlock.Settings::nonOpaque);

    public static final SolarPanelBlock SOLAR_PANEL = registerWithItemCopy("solar_panel",
            SolarPanelBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final CrusherBlock CRUSHER = registerWithItemCopy("crusher",
            CrusherBlock::new, Blocks.FURNACE, settings -> settings.luminance(value -> 0).nonOpaque());

    public static final CableBlock CABLE = registerWithItemCopy("cable",
            CableBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final WindTurbineBlock WIND_TURBINE = registerWithItemCopy("wind_turbine",
            WindTurbineBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final OilPumpJackBlock OIL_PUMP_JACK = registerWithItemCopy("oil_pump_jack",
            OilPumpJackBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final MultiblockBlock MULTIBLOCK_BLOCK = registerWithCopy("multiblock",
            MultiblockBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final DrillBlock DRILL = registerWithItemCopy("drill",
            DrillBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final MotorBlock MOTOR = registerWithItemCopy("motor",
            MotorBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final DrillTubeBlock DRILL_TUBE = registerWithItemCopy("drill_tube",
            DrillTubeBlock::new, Blocks.LIGHT_GRAY_CONCRETE, AbstractBlock.Settings::nonOpaque);

    public static final UpgradeStationBlock UPGRADE_STATION = registerWithItemCopy("upgrade_station",
            UpgradeStationBlock::new, Blocks.ANVIL, AbstractBlock.Settings::nonOpaque);

    public static final ElectricFurnaceBlock ELECTRIC_FURNACE = registerWithItemCopy("electric_furnace",
            ElectricFurnaceBlock::new, Blocks.FURNACE, settings -> settings);

    public static <T extends Block> T register(String name, Function<AbstractBlock.Settings, T> constructor, Function<AbstractBlock.Settings, AbstractBlock.Settings> settingsApplier) {
        return registerBlock(name, constructor.apply(
                settingsApplier.apply(AbstractBlock.Settings.create()
                        .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Industria.id(name))))));
    }

    public static <T extends Block> T registerWithCopy(String name, Function<AbstractBlock.Settings, T> constructor, Block toCopy, Function<AbstractBlock.Settings, AbstractBlock.Settings> settingsApplier) {
        return registerBlock(name, constructor.apply(
                settingsApplier.apply(AbstractBlock.Settings.copy(toCopy)
                        .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Industria.id(name))))));
    }

    public static <T extends Block> T registerWithItemCopy(String name, Function<AbstractBlock.Settings, T> constructor, Block toCopy, Function<AbstractBlock.Settings, AbstractBlock.Settings> settingsApplier) {
        T registeredBlock = registerBlock(name, constructor.apply(
                settingsApplier.apply(AbstractBlock.Settings.copy(toCopy)
                        .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Industria.id(name))))));
        ItemInit.register(name, settings -> new BlockItem(registeredBlock, settings), Item.Settings::useBlockPrefixedTranslationKey);
        return registeredBlock;
    }

    private static <T extends Block> T registerBlock(String name, T block) {
        return Registry.register(Registries.BLOCK, Industria.id(name), block);
    }

    public static void init() {
        registerBatteries();
    }
}
