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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.function.Function;

public class BlockInit {
    public static final AlloyFurnaceBlock ALLOY_FURNACE = registerWithItemCopy("alloy_furnace",
            AlloyFurnaceBlock::new, Blocks.FURNACE, AbstractBlock.Settings::nonOpaque);

    public static final ThermalGeneratorBlock THERMAL_GENERATOR = registerWithItemCopy("thermal_generator",
            ThermalGeneratorBlock::new, Blocks.FURNACE, AbstractBlock.Settings::nonOpaque);

    public static final BatteryBlock BASIC_BATTERY = registerWithItemCopy("basic_battery",
            settings -> new BatteryBlock(settings, BatteryBlock.BatteryLevel.BASIC), Blocks.IRON_BLOCK, settings -> settings);

    public static final BatteryBlock ADVANCED_BATTERY = registerWithItemCopy("advanced_battery",
            settings -> new BatteryBlock(settings, BatteryBlock.BatteryLevel.ADVANCED), Blocks.IRON_BLOCK, settings -> settings);

    public static final BatteryBlock ELITE_BATTERY = registerWithItemCopy("elite_battery",
            settings -> new BatteryBlock(settings, BatteryBlock.BatteryLevel.ELITE), Blocks.IRON_BLOCK, settings -> settings);

    public static final BatteryBlock ULTIMATE_BATTERY = registerWithItemCopy("ultimate_battery",
            settings -> new BatteryBlock(settings, BatteryBlock.BatteryLevel.ULTIMATE), Blocks.IRON_BLOCK, settings -> settings);

    public static final BatteryBlock CREATIVE_BATTERY = registerWithItemCopy("creative_battery",
            settings -> new BatteryBlock(settings, BatteryBlock.BatteryLevel.CREATIVE), Blocks.IRON_BLOCK, settings -> settings);

    public static final CombustionGeneratorBlock COMBUSTION_GENERATOR = registerWithItemCopy("combustion_generator",
            CombustionGeneratorBlock::new, Blocks.FURNACE, AbstractBlock.Settings::nonOpaque);

    public static final SolarPanelBlock SOLAR_PANEL = registerWithItemCopy("solar_panel",
            SolarPanelBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final CrusherBlock CRUSHER = registerWithItemCopy("crusher",
            CrusherBlock::new, Blocks.FURNACE, settings -> settings.luminance(value -> 0).nonOpaque());

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

    public static final FractionalDistillationControllerBlock FRACTIONAL_DISTILLATION_CONTROLLER = registerWithItemCopy("fractional_distillation_controller",
            FractionalDistillationControllerBlock::new, Blocks.IRON_BLOCK, settings -> settings);

    public static final InductionHeaterBlock INDUCTION_HEATER = registerWithItemCopy("induction_heater",
            InductionHeaterBlock::new, Blocks.IRON_BLOCK, settings -> settings);

    public static final FractionalDistillationTowerBlock FRACTIONAL_DISTILLATION_TOWER = registerWithItemCopy("fractional_distillation_tower",
            FractionalDistillationTowerBlock::new, Blocks.IRON_BLOCK, settings -> settings);

    public static final CableBlock CABLE = registerWithItemCopy("cable",
            CableBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final FluidPipeBlock FLUID_PIPE = registerWithItemCopy("fluid_pipe",
            FluidPipeBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final SlurryPipeBlock SLURRY_PIPE = registerWithItemCopy("slurry_pipe",
            SlurryPipeBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final FluidPumpBlock FLUID_PUMP = registerWithItemCopy("fluid_pump",
            FluidPumpBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final HeatPipeBlock HEAT_PIPE = registerWithItemCopy("heat_pipe",
            HeatPipeBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final Block BAUXITE_ORE = registerWithItemCopy("bauxite_ore",
            Block::new, Blocks.IRON_ORE, settings -> settings);

    public static final Block CASSITERITE_ORE = registerWithItemCopy("cassiterite_ore",
            Block::new, Blocks.IRON_ORE, settings -> settings);

    public static final Block ZINC_ORE = registerWithItemCopy("zinc_ore",
            Block::new, Blocks.IRON_ORE, settings -> settings);

    public static final Block DEEPSLATE_BAUXITE_ORE = registerWithItemCopy("deepslate_bauxite_ore",
            Block::new, Blocks.DEEPSLATE_IRON_ORE, settings -> settings);

    public static final Block DEEPSLATE_CASSITERITE_ORE = registerWithItemCopy("deepslate_cassiterite_ore",
            Block::new, Blocks.DEEPSLATE_IRON_ORE, settings -> settings);

    public static final Block DEEPSLATE_ZINC_ORE = registerWithItemCopy("deepslate_zinc_ore",
            Block::new, Blocks.DEEPSLATE_IRON_ORE, settings -> settings);

    public static final Block ALUMINIUM_BLOCK = registerWithItemCopy("aluminium_block",
            Block::new, Blocks.IRON_BLOCK, settings -> settings);

    public static final Block TIN_BLOCK = registerWithItemCopy("tin_block",
            Block::new, Blocks.IRON_BLOCK, settings -> settings);

    public static final Block ZINC_BLOCK = registerWithItemCopy("zinc_block",
            Block::new, Blocks.IRON_BLOCK, settings -> settings);

    public static final Block RAW_BAUXITE_BLOCK = registerWithItemCopy("raw_bauxite_block",
            Block::new, Blocks.RAW_IRON_BLOCK, settings -> settings);

    public static final Block RAW_CASSITERITE_BLOCK = registerWithItemCopy("raw_cassiterite_block",
            Block::new, Blocks.RAW_IRON_BLOCK, settings -> settings);

    public static final Block RAW_ZINC_BLOCK = registerWithItemCopy("raw_zinc_block",
            Block::new, Blocks.RAW_IRON_BLOCK, settings -> settings);

    public static final MixerBlock MIXER = registerWithItemCopy("mixer",
            MixerBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final DigesterBlock DIGESTER = registerWithItemCopy("digester",
            DigesterBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final MultiblockIOBlock MULTIBLOCK_IO = registerWithCopy("multiblock_io",
            MultiblockIOBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final ClarifierBlock CLARIFIER = registerWithItemCopy("clarifier",
            ClarifierBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final CrystallizerBlock CRYSTALLIZER = registerWithItemCopy("crystallizer",
            CrystallizerBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final RotaryKilnControllerBlock ROTARY_KILN_CONTROLLER = registerWithCopy("rotary_kiln_controller",
            RotaryKilnControllerBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final RotaryKilnBlock ROTARY_KILN = registerWithCopy("rotary_kiln",
            RotaryKilnBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final ElectrolyzerBlock ELECTROLYZER = registerWithItemCopy("electrolyzer",
            ElectrolyzerBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final FluidTankBlock FLUID_TANK = registerWithItemCopy("fluid_tank",
            FluidTankBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final WellheadBlock WELLHEAD = registerWithItemCopy("wellhead",
            WellheadBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final ShakingTableBlock SHAKING_TABLE = registerWithItemCopy("shaking_table",
            ShakingTableBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static final CentrifugalConcentratorBlock CENTRIFUGAL_CONCENTRATOR = registerWithItemCopy("centrifugal_concentrator",
            CentrifugalConcentratorBlock::new, Blocks.IRON_BLOCK, AbstractBlock.Settings::nonOpaque);

    public static <T extends Block> T register(String name, Function<AbstractBlock.Settings, T> constructor, Function<AbstractBlock.Settings, AbstractBlock.Settings> settingsApplier) {
        return registerBlock(name, constructor.apply(
                settingsApplier.apply(AbstractBlock.Settings.create()
                        .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Industria.id(name))))));
    }

    public static <T extends Block> T registerWithCopy(String name, Function<AbstractBlock.Settings, T> constructor, Block toCopy) {
        return registerWithCopy(name, constructor, toCopy, settings -> settings);
    }

    public static <T extends Block> T registerWithCopy(String name, Function<AbstractBlock.Settings, T> constructor, Block toCopy, Function<AbstractBlock.Settings, AbstractBlock.Settings> settingsApplier) {
        return registerBlock(name, constructor.apply(
                settingsApplier.apply(AbstractBlock.Settings.copy(toCopy)
                        .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Industria.id(name))))));
    }

    public static <T extends Block> T registerWithItemCopy(String name, Function<AbstractBlock.Settings, T> constructor, Block toCopy) {
        return registerWithItemCopy(name, constructor, toCopy, settings -> settings);
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
    }
}
