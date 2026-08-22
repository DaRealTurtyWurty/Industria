package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.*;
import dev.turtywurty.industria.conveyor.block.impl.*;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockBlock;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockIOBlock;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import dev.turtywurty.turtymultiloader.registration.VanillaBlockPredicates;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModBlocks {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final RegistrationHandle<Block, Block> BAUXITE_ORE = registerWithItemCopy("bauxite_ore",
            Block::new, Blocks.IRON_ORE);

    public static final RegistrationHandle<Block, Block> DEEPSLATE_BAUXITE_ORE = registerWithItemCopy("deepslate_bauxite_ore",
            Block::new, Blocks.DEEPSLATE_IRON_ORE);

    public static final RegistrationHandle<Block, Block> RAW_BAUXITE_BLOCK = registerWithItemCopy("raw_bauxite_block",
            Block::new, Blocks.RAW_IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> ALUMINIUM_BLOCK = registerWithItemCopy("aluminium_block",
            Block::new, Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> ARGENTITE_ORE = registerWithItemCopy("argentite_ore",
            Block::new, Blocks.IRON_ORE);

    public static final RegistrationHandle<Block, Block> DEEPSLATE_ARGENTITE_ORE = registerWithItemCopy("deepslate_argentite_ore",
            Block::new, Blocks.DEEPSLATE_IRON_ORE);

    public static final RegistrationHandle<Block, Block> RAW_ARGENTITE_BLOCK = registerWithItemCopy("raw_argentite_block",
            Block::new, Blocks.RAW_IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> SILVER_BLOCK = registerWithItemCopy("silver_block",
            Block::new, Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> GALENA_ORE = registerWithItemCopy("galena_ore",
            Block::new, Blocks.IRON_ORE);

    public static final RegistrationHandle<Block, Block> DEEPSLATE_GALENA_ORE = registerWithItemCopy("deepslate_galena_ore",
            Block::new, Blocks.DEEPSLATE_IRON_ORE);

    public static final RegistrationHandle<Block, Block> RAW_GALENA_BLOCK = registerWithItemCopy("raw_galena_block",
            Block::new, Blocks.RAW_IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> LEAD_BLOCK = registerWithItemCopy("lead_block",
            Block::new, Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> ILMENITE_ORE = registerWithItemCopy("ilmenite_ore",
            Block::new, Blocks.IRON_ORE);

    public static final RegistrationHandle<Block, Block> DEEPSLATE_ILMENITE_ORE = registerWithItemCopy("deepslate_ilmenite_ore",
            Block::new, Blocks.DEEPSLATE_IRON_ORE);

    public static final RegistrationHandle<Block, Block> RAW_ILMENITE_BLOCK = registerWithItemCopy("raw_ilmenite_block",
            Block::new, Blocks.RAW_IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> TITANIUM_BLOCK = registerWithItemCopy("titanium_block",
            Block::new, Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> SPHALERITE_ORE = registerWithItemCopy("sphalerite_ore",
            Block::new, Blocks.IRON_ORE);

    public static final RegistrationHandle<Block, Block> DEEPSLATE_SPHALERITE_ORE = registerWithItemCopy("deepslate_sphalerite_ore",
            Block::new, Blocks.DEEPSLATE_IRON_ORE);

    public static final RegistrationHandle<Block, Block> RAW_SPHALERITE_BLOCK = registerWithItemCopy("raw_sphalerite_block",
            Block::new, Blocks.RAW_IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> ZINC_BLOCK = registerWithItemCopy("zinc_block",
            Block::new, Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> COBALTITE_ORE = registerWithItemCopy("cobaltite_ore",
            Block::new, Blocks.IRON_ORE);

    public static final RegistrationHandle<Block, Block> DEEPSLATE_COBALTITE_ORE = registerWithItemCopy("deepslate_cobaltite_ore",
            Block::new, Blocks.DEEPSLATE_IRON_ORE);

    public static final RegistrationHandle<Block, Block> RAW_COBALTITE_BLOCK = registerWithItemCopy("raw_cobaltite_block",
            Block::new, Blocks.RAW_IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> COBALT_BLOCK = registerWithItemCopy("cobalt_block",
            Block::new, Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> PENTLANDITE_ORE = registerWithItemCopy("pentlandite_ore",
            Block::new, Blocks.IRON_ORE);

    public static final RegistrationHandle<Block, Block> DEEPSLATE_PENTLANDITE_ORE = registerWithItemCopy("deepslate_pentlandite_ore",
            Block::new, Blocks.DEEPSLATE_IRON_ORE);

    public static final RegistrationHandle<Block, Block> RAW_PENTLANDITE_BLOCK = registerWithItemCopy("raw_pentlandite_block",
            Block::new, Blocks.RAW_IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> NICKEL_BLOCK = registerWithItemCopy("nickel_block",
            Block::new, Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> IRIDIUM_ORE = registerWithItemCopy("iridium_ore",
            Block::new, Blocks.IRON_ORE);

    public static final RegistrationHandle<Block, Block> DEEPSLATE_IRIDIUM_ORE = registerWithItemCopy("deepslate_iridium_ore",
            Block::new, Blocks.DEEPSLATE_IRON_ORE);

    public static final RegistrationHandle<Block, Block> IRIDIUM_BLOCK = registerWithItemCopy("iridium_block",
            Block::new, Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> CASSITERITE_ORE = registerWithItemCopy("cassiterite_ore",
            Block::new, Blocks.IRON_ORE);

    public static final RegistrationHandle<Block, Block> DEEPSLATE_CASSITERITE_ORE = registerWithItemCopy("deepslate_cassiterite_ore",
            Block::new, Blocks.DEEPSLATE_IRON_ORE);

    public static final RegistrationHandle<Block, Block> RAW_CASSITERITE_BLOCK = registerWithItemCopy("raw_cassiterite_block",
            Block::new, Blocks.RAW_IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> TIN_BLOCK = registerWithItemCopy("tin_block",
            Block::new, Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> NETHER_PYRITE_ORE = registerWithItemCopy("nether_pyrite_ore",
            settings -> new DropExperienceBlock(UniformInt.of(4, 6), settings), Blocks.NETHER_GOLD_ORE);

    public static final RegistrationHandle<Block, Block> END_PYRITE_ORE = registerWithItemCopy("end_pyrite_ore",
            settings -> new DropExperienceBlock(UniformInt.of(4, 6),
                    settings.lightLevel(_ -> 7).noOcclusion()), Blocks.END_STONE);

    public static final RegistrationHandle<Block, Block> PYRITE_BLOCK = registerWithItemCopy("pyrite_block",
            Block::new, Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, Block> STEEL_BLOCK = registerWithItemCopy("steel_block",
            Block::new, Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, DropExperienceBlock> QUARTZ_ORE = registerWithItemCopy("quartz_ore",
            settings -> new DropExperienceBlock(UniformInt.of(2, 5), settings), Blocks.NETHER_QUARTZ_ORE,
            settings -> settings.sound(SoundType.STONE).lightLevel(_ -> 3).strength(3.0F).mapColor(MapColor.STONE));

    public static final RegistrationHandle<Block, DropExperienceBlock> DEEPSLATE_QUARTZ_ORE = registerWithItemCopy("deepslate_quartz_ore",
            settings -> new DropExperienceBlock(UniformInt.of(2, 5),
                    settings.lightLevel(_ -> 3).noOcclusion()), Blocks.NETHER_QUARTZ_ORE,
            settings -> settings.sound(SoundType.DEEPSLATE).lightLevel(_ -> 3).strength(4.5F, 3.0F).mapColor(MapColor.DEEPSLATE));

    public static final RegistrationHandle<Block, AlloyFurnaceBlock> ALLOY_FURNACE = registerWithItemCopy("alloy_furnace",
            AlloyFurnaceBlock::new, Blocks.FURNACE, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, ThermalGeneratorBlock> THERMAL_GENERATOR = registerWithItemCopy("thermal_generator",
            ThermalGeneratorBlock::new, Blocks.FURNACE, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, BatteryBlock> BASIC_BATTERY = registerWithItemCopy("basic_battery",
            settings -> new BatteryBlock(settings, BatteryBlock.BatteryLevel.BASIC), Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, BatteryBlock> ADVANCED_BATTERY = registerWithItemCopy("advanced_battery",
            settings -> new BatteryBlock(settings, BatteryBlock.BatteryLevel.ADVANCED), Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, BatteryBlock> ELITE_BATTERY = registerWithItemCopy("elite_battery",
            settings -> new BatteryBlock(settings, BatteryBlock.BatteryLevel.ELITE), Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, BatteryBlock> ULTIMATE_BATTERY = registerWithItemCopy("ultimate_battery",
            settings -> new BatteryBlock(settings, BatteryBlock.BatteryLevel.ULTIMATE), Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, BatteryBlock> CREATIVE_BATTERY = registerWithItemCopy("creative_battery",
            settings -> new BatteryBlock(settings, BatteryBlock.BatteryLevel.CREATIVE), Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, CombustionGeneratorBlock> COMBUSTION_GENERATOR = registerWithItemCopy("combustion_generator",
            CombustionGeneratorBlock::new, Blocks.FURNACE, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, SolarPanelBlock> SOLAR_PANEL = registerWithItemCopy("solar_panel",
            properties -> new SolarPanelBlock(properties, false), Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, CrusherBlock> CRUSHER = registerWithItemCopy("crusher",
            CrusherBlock::new, Blocks.FURNACE, settings -> settings.lightLevel(_ -> 0).noOcclusion());

    public static final RegistrationHandle<Block, WindTurbineBlock> WIND_TURBINE = registerWithItemCopy("wind_turbine",
            WindTurbineBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, OilPumpJackBlock> OIL_PUMP_JACK = registerWithItemCopy("oil_pump_jack",
            OilPumpJackBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, DrillBlock> DRILL = registerWithItemCopy("drill",
            DrillBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, MotorBlock> MOTOR = registerWithItemCopy("motor",
            MotorBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, DrillTubeBlock> DRILL_TUBE = registerWithItemCopy("drill_tube",
            DrillTubeBlock::new, Blocks.LIGHT_GRAY_CONCRETE, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, UpgradeStationBlock> UPGRADE_STATION = registerWithItemCopy("upgrade_station",
            UpgradeStationBlock::new, Blocks.ANVIL, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, ElectricFurnaceBlock> ELECTRIC_FURNACE = registerWithItemCopy("electric_furnace",
            ElectricFurnaceBlock::new, Blocks.FURNACE,
            settings -> settings.lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0));

    public static final RegistrationHandle<Block, InductionHeaterBlock> INDUCTION_HEATER = registerWithItemCopy("induction_heater",
            InductionHeaterBlock::new, Blocks.IRON_BLOCK);

    public static final RegistrationHandle<Block, CableBlock> CABLE = registerWithItemCopy("cable",
            CableBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, FluidPipeBlock> FLUID_PIPE = registerWithItemCopy("fluid_pipe",
            FluidPipeBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, SlurryPipeBlock> SLURRY_PIPE = registerWithItemCopy("slurry_pipe",
            SlurryPipeBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, GasPipeBlock> GAS_PIPE = registerWithItemCopy("gas_pipe",
            GasPipeBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, FluidPumpBlock> FLUID_PUMP = registerWithItemCopy("fluid_pump",
            FluidPumpBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, MixerBlock> MIXER = registerWithItemCopy("mixer",
            MixerBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, DigesterBlock> DIGESTER = registerWithItemCopy("digester",
            DigesterBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, AutoMultiblockBlock> AUTO_MULTIBLOCK_BLOCK = registerWithCopy("auto_multiblock",
            AutoMultiblockBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, AutoMultiblockIOBlock> AUTO_MULTIBLOCK_IO = registerWithCopy("auto_multiblock_io",
            AutoMultiblockIOBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, ClarifierBlock> CLARIFIER = registerWithItemCopy("clarifier",
            ClarifierBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, CrystallizerBlock> CRYSTALLIZER = registerWithItemCopy("crystallizer",
            CrystallizerBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, RotaryKilnControllerBlock> ROTARY_KILN_CONTROLLER = registerWithCopy("rotary_kiln_controller",
            RotaryKilnControllerBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, RotaryKilnBlock> ROTARY_KILN = registerWithCopy("rotary_kiln",
            RotaryKilnBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, ElectrolyzerBlock> ELECTROLYZER = registerWithItemCopy("electrolyzer",
            ElectrolyzerBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, FluidTankBlock> FLUID_TANK = registerWithItemCopy("fluid_tank",
            FluidTankBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, WellheadBlock> WELLHEAD = registerWithItemCopy("wellhead",
            WellheadBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, ShakingTableBlock> SHAKING_TABLE = registerWithItemCopy("shaking_table",
            ShakingTableBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, CentrifugalConcentratorBlock> CENTRIFUGAL_CONCENTRATOR = registerWithItemCopy("centrifugal_concentrator",
            CentrifugalConcentratorBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, ArcFurnaceBlock> ARC_FURNACE = registerWithItemCopy("arc_furnace",
            ArcFurnaceBlock::new, Blocks.FURNACE, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, BasicConveyorBlock> CONVEYOR = registerWithItemCopy("conveyor",
            BasicConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, SplitterConveyorBlock> SPLITTER_CONVEYOR = registerWithItemCopy("splitter_conveyor",
            SplitterConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, MergerConveyorBlock> MERGER_CONVEYOR = registerWithItemCopy("merger_conveyor",
            MergerConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, AlternatorConveyorBlock> ALTERNATOR_CONVEYOR = registerWithItemCopy("alternator_conveyor",
            AlternatorConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, FeederConveyorBlock> FEEDER_CONVEYOR = registerWithItemCopy("feeder_conveyor",
            FeederConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, HatchConveyorBlock> HATCH_CONVEYOR = registerWithItemCopy("hatch_conveyor",
            HatchConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, SideInjectorConveyorBlock> SIDE_INJECTOR_CONVEYOR = registerWithItemCopy("side_injector_conveyor",
            SideInjectorConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, LadderConveyorBlock> LADDER_CONVEYOR = registerWithItemCopy("ladder_conveyor",
            LadderConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, FilterConveyorBlock> FILTER_CONVEYOR = registerWithItemCopy("filter_conveyor",
            FilterConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, MagneticConveyorBlock> MAGNETIC_CONVEYOR = registerWithItemCopy("magnetic_conveyor",
            MagneticConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, DetectorConveyorBlock> DETECTOR_CONVEYOR = registerWithItemCopy("detector_conveyor",
            DetectorConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, DropChuteConveyorBlock> DROP_CHUTE_CONVEYOR = registerWithItemCopy("drop_chute_conveyor",
            DropChuteConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, CountConveyorBlock> COUNT_CONVEYOR = registerWithItemCopy("count_conveyor",
            CountConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, DelayConveyorBlock> DELAY_CONVEYOR = registerWithItemCopy("delay_conveyor",
            DelayConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, ContainmentConveyorBlock> CONTAINMENT_CONVEYOR = registerWithItemCopy("containment_conveyor",
            ContainmentConveyorBlock::new, Blocks.IRON_BLOCK,
            settings -> settings.noOcclusion().isRedstoneConductor(VanillaBlockPredicates::never));

    public static final RegistrationHandle<Block, TreeTapBlock> TREE_TAP = registerWithItemCopy("tree_tap",
            TreeTapBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, AgitatorBlock> AGITATOR = registerWithItemCopy("agitator",
            AgitatorBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, DistillationTowerBlock> DISTILLATION_TOWER = registerWithItemCopy("distillation_tower",
            DistillationTowerBlock::new, Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static final RegistrationHandle<Block, SolarPanelBlock> ADVANCED_SOLAR_PANEL = registerWithItemCopy("advanced_solar_panel",
            properties -> new SolarPanelBlock(properties, true), Blocks.IRON_BLOCK, BlockBehaviour.Properties::noOcclusion);

    public static <T extends Block> RegistrationHandle<Block, T> register(String name, Function<BlockBehaviour.Properties, T> constructor, Function<BlockBehaviour.Properties, BlockBehaviour.Properties> settingsApplier) {
        return registerBlock(name, () -> constructor.apply(
                settingsApplier.apply(BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, Industria.id(name))))));
    }

    public static <T extends Block> RegistrationHandle<Block, T> registerWithCopy(String name, Function<BlockBehaviour.Properties, T> constructor, Block toCopy) {
        return registerWithCopy(name, constructor, toCopy, settings -> settings);
    }

    public static <T extends Block> RegistrationHandle<Block, T> registerWithCopy(String name, Function<BlockBehaviour.Properties, T> constructor, Block toCopy, Function<BlockBehaviour.Properties, BlockBehaviour.Properties> settingsApplier) {
        return registerBlock(name, () -> constructor.apply(
                settingsApplier.apply(BlockBehaviour.Properties.ofFullCopy(toCopy)
                        .setId(ResourceKey.create(Registries.BLOCK, Industria.id(name))))));
    }

    public static <T extends Block> RegistrationHandle<Block, T> registerWithItemCopy(String name, Function<BlockBehaviour.Properties, T> constructor, Block toCopy) {
        return registerWithItemCopy(name, constructor, toCopy, Function.identity());
    }

    public static <T extends Block> RegistrationHandle<Block, T> registerWithItemCopy(String name, Function<BlockBehaviour.Properties, T> constructor, Block toCopy, Function<BlockBehaviour.Properties, BlockBehaviour.Properties> settingsApplier) {
        RegistrationHandle<Block, T> registeredBlock = registerBlock(name, () -> constructor.apply(
                settingsApplier.apply(BlockBehaviour.Properties.ofFullCopy(toCopy)
                        .setId(ResourceKey.create(Registries.BLOCK, Industria.id(name))))));
        ModItems.register(name, settings -> new BlockItem(registeredBlock.get(), settings), Item.Properties::useBlockDescriptionPrefix);
        return registeredBlock;
    }

    private static <T extends Block> RegistrationHandle<Block, T> registerBlock(String name, Supplier<T> block) {
        return REGISTRIES.registerBlock(Industria.id(name), block);
    }

    public static void init() {
    }
}
