package dev.turtywurty.industria.datagen;

import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.slurryapi.api.SlurryVariant;
import dev.turtywurty.slurryapi.api.SlurryVariantAttributes;
import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.conveyor.block.impl.entity.ContainmentConveyorBlockEntity;
import dev.turtywurty.industria.conveyor.block.impl.entity.DetectorConveyorBlockEntity;
import dev.turtywurty.industria.conveyor.block.impl.entity.FilterConveyorBlockEntity;
import dev.turtywurty.industria.init.*;
import dev.turtywurty.industria.screen.ContainmentConveyorScreen;
import dev.turtywurty.industria.item.SeismicScannerItem;
import dev.turtywurty.industria.screen.DetectorConveyorScreen;
import dev.turtywurty.industria.screen.FilterConveyorScreen;
import dev.turtywurty.industria.util.enums.TextEnum;
import dev.turtywurty.turtymultiloader.datagen.language.LanguageGenerationContext;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class IndustriaEnglishLanguage {
    private IndustriaEnglishLanguage() {
    }

    public static void generate(LanguageGenerationContext translationBuilder) {
        addText(translationBuilder, ModCreativeModeTabs.MAIN_TITLE, "Industria");

        translationBuilder.add(ModBlocks.ALLOY_FURNACE.get(), "Alloy Furnace");
        addText(translationBuilder, AlloyFurnaceBlockEntity.TITLE, "Alloy Furnace");

        translationBuilder.add(ModBlocks.THERMAL_GENERATOR.get(), "Thermal Generator");
        addText(translationBuilder, ThermalGeneratorBlockEntity.TITLE, "Thermal Generator");

        translationBuilder.add(ModBlocks.BASIC_BATTERY.get(), "Basic Battery");
        translationBuilder.add(ModBlocks.ADVANCED_BATTERY.get(), "Advanced Battery");
        translationBuilder.add(ModBlocks.ELITE_BATTERY.get(), "Elite Battery");
        translationBuilder.add(ModBlocks.ULTIMATE_BATTERY.get(), "Ultimate Battery");
        translationBuilder.add(ModBlocks.CREATIVE_BATTERY.get(), "Creative Battery");
        addText(translationBuilder, BatteryBlockEntity.TITLE, "Battery");
        addText(translationBuilder, BatteryBlockEntity.CHARGE_MODE_BUTTON_TOOLTIP_TEXT, "Charge/Discharge");

        translationBuilder.add(ModBlocks.COMBUSTION_GENERATOR.get(), "Combustion Generator");
        addText(translationBuilder, CombustionGeneratorBlockEntity.TITLE, "Combustion Generator");

        translationBuilder.add(ModBlocks.SOLAR_PANEL.get(), "Solar Panel");
        translationBuilder.add(ModBlocks.ADVANCED_SOLAR_PANEL.get(), "Advanced Solar Panel");
        addText(translationBuilder, SolarPanelBlockEntity.TITLE, "Solar Panel");
        addText(translationBuilder, SolarPanelBlockEntity.ADVANCED_TITLE, "Advanced Solar Panel");

        translationBuilder.add(ModBlocks.CRUSHER.get(), "Crusher");
        addText(translationBuilder, CrusherBlockEntity.TITLE, "Crusher");

        translationBuilder.add(ModBlocks.WIND_TURBINE.get(), "Wind Turbine");
        addText(translationBuilder, WindTurbineBlockEntity.TITLE, "Wind Turbine");

        translationBuilder.add(ModBlocks.OIL_PUMP_JACK.get(), "Oil Pump Jack");
        addText(translationBuilder, OilPumpJackBlockEntity.TITLE, "Oil Pump Jack");

        translationBuilder.add(ModItems.SEISMIC_SCANNER.get(), "Seismic Scanner");
        addText(translationBuilder, SeismicScannerItem.TITLE, "Seismic Scanner");

        translationBuilder.add(ModBlocks.DRILL.get(), "Drill");
        addText(translationBuilder, DrillBlockEntity.TITLE, "Drill");
        translationBuilder.add(ModItems.SIMPLE_DRILL_HEAD.get(), "Simple Drill Head");
        addTextEnum(translationBuilder, DrillBlockEntity.OverflowMethod.SPILLAGE, "Spillage");
        addTextEnum(translationBuilder, DrillBlockEntity.OverflowMethod.VOID, "Void");
        addTextEnum(translationBuilder, DrillBlockEntity.OverflowMethod.PAUSE, "Pause");
        translationBuilder.add(ModItems.BLOCK_BUILDER_DRILL_HEAD.get(), "Block Builder Drill Head");
        translationBuilder.add(ModBlocks.DRILL_TUBE.get(), "Drill Tube");
        addDamageType(translationBuilder, ModDamageTypes.DRILL, "%1$s was drilled to death");

        translationBuilder.add(ModBlocks.MOTOR.get(), "Motor");
        addText(translationBuilder, MotorBlockEntity.TITLE, "Motor");

        translationBuilder.add(ModBlocks.UPGRADE_STATION.get(), "Upgrade Station");
        addText(translationBuilder, UpgradeStationBlockEntity.TITLE, "Upgrade Station");

        translationBuilder.add(ModBlocks.ELECTRIC_FURNACE.get(), "Electric Furnace");
        addText(translationBuilder, ElectricFurnaceBlockEntity.TITLE, "Electric Furnace");

        translationBuilder.add(ModBlocks.INDUCTION_HEATER.get(), "Induction Heater");
        addText(translationBuilder, InductionHeaterBlockEntity.TITLE, "Induction Heater");

        translationBuilder.add(ModBlocks.FLUID_PUMP.get(), "Fluid Pump");

        translationBuilder.add(ModBlocks.MIXER.get(), "Mixer");
        addText(translationBuilder, MixerBlockEntity.TITLE, "Mixer");

        translationBuilder.add(ModBlocks.DIGESTER.get(), "Digester");
        addText(translationBuilder, DigesterBlockEntity.TITLE, "Digester");

        translationBuilder.add(ModBlocks.CLARIFIER.get(), "Clarifier");
        addText(translationBuilder, ClarifierBlockEntity.TITLE, "Clarifier");

        translationBuilder.add(ModBlocks.CRYSTALLIZER.get(), "Crystallizer");
        addText(translationBuilder, CrystallizerBlockEntity.TITLE, "Crystallizer");

        translationBuilder.add(ModBlocks.ROTARY_KILN_CONTROLLER.get(), "Rotary Kiln Controller");
        translationBuilder.add(ModBlocks.ROTARY_KILN.get(), "Rotary Kiln");

        translationBuilder.add(ModBlocks.ELECTROLYZER.get(), "Electrolyzer");
        addText(translationBuilder, ElectrolyzerBlockEntity.TITLE, "Electrolyzer");

        translationBuilder.add(ModBlocks.ARC_FURNACE.get(), "Arc Furnace");
        addText(translationBuilder, ArcFurnaceBlockEntity.TITLE, "Arc Furnace");
        addTextEnum(translationBuilder, ArcFurnaceBlockEntity.Mode.BLASTING, "Blasting");
        addTextEnum(translationBuilder, ArcFurnaceBlockEntity.Mode.ALLOYING, "Alloying");
        addTextEnum(translationBuilder, ArcFurnaceBlockEntity.Mode.RECYCLING, "Recycling");

        translationBuilder.add(ModBlocks.FLUID_TANK.get(), "Fluid Tank");
        addText(translationBuilder, FluidTankBlockEntity.TITLE, "Fluid Tank");

        translationBuilder.add(ModItems.MULTIBLOCK_EXPORTER.get(), "Multiblock Exporter");
        translationBuilder.add(ModItems.ROTARY_KILN.get(), "Rotary Kiln");

        translationBuilder.add(ModBlocks.SHAKING_TABLE.get(), "Shaking Table");
        addText(translationBuilder, ShakingTableBlockEntity.TITLE, "Shaking Table");

        translationBuilder.add(ModBlocks.CENTRIFUGAL_CONCENTRATOR.get(), "Centrifugal Concentrator");
        addText(translationBuilder, CentrifugalConcentratorBlockEntity.TITLE, "Centrifugal Concentrator");

        translationBuilder.add(ModItems.WRENCH.get(), "Wrench");

        translationBuilder.add(ModBlocks.CABLE.get(), "Cable");
        translationBuilder.add(ModBlocks.FLUID_PIPE.get(), "Fluid Pipe");
        translationBuilder.add(ModBlocks.SLURRY_PIPE.get(), "Slurry Pipe");
        translationBuilder.add(ModBlocks.GAS_PIPE.get(), "Gas Pipe");
        translationBuilder.add(ModBlocks.CONVEYOR.get(), "Conveyor");
        translationBuilder.add(ModBlocks.SPLITTER_CONVEYOR.get(), "Splitter Conveyor");
        translationBuilder.add(ModBlocks.MERGER_CONVEYOR.get(), "Merger Conveyor");
        translationBuilder.add(ModBlocks.ALTERNATOR_CONVEYOR.get(), "Alternator Conveyor");
        translationBuilder.add(ModBlocks.FEEDER_CONVEYOR.get(), "Feeder Conveyor");
        translationBuilder.add(ModBlocks.HATCH_CONVEYOR.get(), "Hatch Conveyor");
        translationBuilder.add(ModBlocks.SIDE_INJECTOR_CONVEYOR.get(), "Side Injector Conveyor");
        translationBuilder.add(ModBlocks.LADDER_CONVEYOR.get(), "Ladder Conveyor");
        translationBuilder.add(ModBlocks.FILTER_CONVEYOR.get(), "Filter Conveyor");
        addText(translationBuilder, FilterConveyorBlockEntity.TITLE, "Filter Conveyor");
        addText(translationBuilder, FilterConveyorScreen.BLACKLIST_MODE_LABEL, "Blacklist Mode");
        addText(translationBuilder, FilterConveyorScreen.MATCH_DURABILITY_LABEL, "Match Durability");
        addText(translationBuilder, FilterConveyorScreen.MATCH_ENCHANTMENTS_LABEL, "Match Enchantments");
        addText(translationBuilder, FilterConveyorScreen.MATCH_COMPONENTS_LABEL, "Match Components");
        translationBuilder.add(ModBlocks.MAGNETIC_CONVEYOR.get(),  "Magnetic Conveyor");
        translationBuilder.add(ModBlocks.DETECTOR_CONVEYOR.get(), "Detector Conveyor");
        addText(translationBuilder, DetectorConveyorBlockEntity.TITLE, "Detector Conveyor");
        addText(translationBuilder, DetectorConveyorScreen.BLACKLIST_MODE_LABEL, "Blacklist Mode");
        addText(translationBuilder, DetectorConveyorScreen.MATCH_DURABILITY_LABEL, "Match Durability");
        addText(translationBuilder, DetectorConveyorScreen.MATCH_ENCHANTMENTS_LABEL, "Match Enchantments");
        addText(translationBuilder, DetectorConveyorScreen.MATCH_COMPONENTS_LABEL, "Match Components");
        translationBuilder.add(ModBlocks.DROP_CHUTE_CONVEYOR.get(), "Drop Chute Conveyor");
        translationBuilder.add(ModBlocks.COUNT_CONVEYOR.get(), "Count Conveyor");
        translationBuilder.add(ModBlocks.DELAY_CONVEYOR.get(), "Delay Conveyor");
        translationBuilder.add(ModBlocks.CONTAINMENT_CONVEYOR.get(), "Containment Conveyor");
        addText(translationBuilder, ContainmentConveyorBlockEntity.TITLE, "Containment Conveyor");
        addText(translationBuilder, Component.translatable("container.industria.containment_conveyor.status.capturing"), "Capturing");
        addText(translationBuilder, Component.translatable("container.industria.containment_conveyor.status.idle"), "Waiting for mob");
        addText(translationBuilder, ContainmentConveyorScreen.PROGRESS_TOOLTIP_TEXT, "Progress: %s%%");
        translationBuilder.add(ModBlocks.TREE_TAP.get(), "Tree Tap");
        translationBuilder.add(ModBlocks.AGITATOR.get(), "Agitator");
        addText(translationBuilder, AgitatorBlockEntity.TITLE, "Agitator");
        translationBuilder.add(ModBlocks.DISTILLATION_TOWER.get(), "Distillation Tower");
        addText(translationBuilder, DistillationTowerBlockEntity.TITLE, "Distillation Tower");

        translationBuilder.add(ModFluids.CRUDE_OIL.block().get(), "Crude Oil");
        translationBuilder.add(ModFluids.CRUDE_OIL.bucket().get(), "Bucket of Crude Oil");
        addSlurry(translationBuilder, ModSlurries.CLAY_SLURRY, "Clay Slurry");

        // Aluminium
        translationBuilder.add(ModItems.BAUXITE.get(), "Bauxite");
        translationBuilder.add(ModItems.CRUSHED_BAUXITE.get(), "Crushed Bauxite");
        translationBuilder.add(ModItems.SODIUM_ALUMINATE.get(), "Sodium Aluminate");
        translationBuilder.add(ModItems.ALUMINIUM_HYDROXIDE.get(), "Aluminium Hydroxide");
        translationBuilder.add(ModItems.ALUMINA.get(), "Alumina");
        translationBuilder.add(ModItems.ALUMINIUM_INGOT.get(), "Aluminium Ingot");
        translationBuilder.add(ModItems.ALUMINIUM_NUGGET.get(), "Aluminium Nugget");
        translationBuilder.add(ModItems.ALUMINIUM_PLATE.get(), "Aluminium Plate");
        translationBuilder.add(ModFluids.DIRTY_SODIUM_ALUMINATE.block().get(), "Dirty Sodium Aluminate");
        translationBuilder.add(ModFluids.DIRTY_SODIUM_ALUMINATE.bucket().get(), "Bucket of Dirty Sodium Aluminate");
        translationBuilder.add(ModFluids.SODIUM_ALUMINATE.block().get(), "Sodium Aluminate");
        translationBuilder.add(ModFluids.SODIUM_ALUMINATE.bucket().get(), "Bucket of Sodium Aluminate");
        translationBuilder.add(ModFluids.MOLTEN_ALUMINIUM.block().get(), "Molten Aluminium");
        translationBuilder.add(ModFluids.MOLTEN_ALUMINIUM.bucket().get(), "Bucket of Molten Aluminium");
        addSlurry(translationBuilder, ModSlurries.BAUXITE_SLURRY, "Bauxite Slurry");
        translationBuilder.add(ModBlocks.BAUXITE_ORE.get(), "Bauxite Ore");
        translationBuilder.add(ModBlocks.DEEPSLATE_BAUXITE_ORE.get(), "Deepslate Bauxite Ore");
        translationBuilder.add(ModBlocks.RAW_BAUXITE_BLOCK.get(), "Raw Bauxite Block");
        translationBuilder.add(ModBlocks.ALUMINIUM_BLOCK.get(), "Block of Aluminium");

        // Silver
        translationBuilder.add(ModItems.ARGENTITE.get(), "Argentite");
        translationBuilder.add(ModItems.CRUSHED_ARGENTITE.get(), "Crushed Argentite");
        translationBuilder.add(ModItems.ARGENTITE_CONCENTRATE.get(), "Argentite Concentrate");
        translationBuilder.add(ModItems.LEAD_BULLION.get(), "Lead Bullion");
        translationBuilder.add(ModItems.DORE_SILVER.get(), "Doré Silver");
        translationBuilder.add(ModItems.SILVER_INGOT.get(), "Silver Ingot");
        translationBuilder.add(ModItems.SILVER_NUGGET.get(), "Silver Nugget");
        translationBuilder.add(ModBlocks.ARGENTITE_ORE.get(), "Argentite Ore");
        translationBuilder.add(ModBlocks.DEEPSLATE_ARGENTITE_ORE.get(), "Deepslate Argentite Ore");
        translationBuilder.add(ModBlocks.RAW_ARGENTITE_BLOCK.get(), "Raw Argentite Block");
        translationBuilder.add(ModBlocks.SILVER_BLOCK.get(), "Block of Silver");

        // Lead
        translationBuilder.add(ModItems.GALENA.get(), "Galena");
        translationBuilder.add(ModItems.CRUSHED_GALENA.get(), "Crushed Galena");
        translationBuilder.add(ModItems.GALENA_CONCENTRATE.get(), "Galena Concentrate");
        translationBuilder.add(ModItems.TETRAGONAL_LITHARGE.get(), "Tetragonal Litharge");
        translationBuilder.add(ModItems.LEAD_INGOT.get(), "Lead Ingot");
        translationBuilder.add(ModItems.LEAD_NUGGET.get(), "Lead Nugget");
        translationBuilder.add(ModBlocks.GALENA_ORE.get(), "Galena Ore");
        translationBuilder.add(ModBlocks.DEEPSLATE_GALENA_ORE.get(), "Deepslate Galena Ore");
        translationBuilder.add(ModBlocks.RAW_GALENA_BLOCK.get(), "Raw Galena Block");
        translationBuilder.add(ModBlocks.LEAD_BLOCK.get(), "Block of Lead");

        // Titanium
        translationBuilder.add(ModItems.ILMENITE.get(), "Ilmenite");
        translationBuilder.add(ModItems.CRUSHED_ILMENITE.get(), "Crushed Ilmenite");
        translationBuilder.add(ModItems.ILMENITE_CONCENTRATE.get(), "Ilmenite Concentrate");
        translationBuilder.add(ModItems.TITANIUM_TETRACHLORIDE.get(), "Titanium Tetrachloride");
        translationBuilder.add(ModItems.TITANIUM_INGOT.get(), "Titanium Ingot");
        translationBuilder.add(ModItems.TITANIUM_NUGGET.get(), "Titanium Nugget");
        translationBuilder.add(ModItems.TITANIUM_PLATE.get(), "Titanium Plate");
        translationBuilder.add(ModBlocks.TITANIUM_BLOCK.get(), "Block of Titanium");
        translationBuilder.add(ModBlocks.ILMENITE_ORE.get(), "Ilmenite Ore");
        translationBuilder.add(ModBlocks.DEEPSLATE_ILMENITE_ORE.get(), "Deepslate Ilmenite Ore");
        translationBuilder.add(ModBlocks.RAW_ILMENITE_BLOCK.get(), "Raw Ilmenite Block");

        // Zinc
        translationBuilder.add(ModItems.SPHALERITE.get(), "Sphalerite");
        translationBuilder.add(ModItems.CRUSHED_SPHALERITE.get(), "Crushed Sphalerite");
        translationBuilder.add(ModItems.SPHALERITE_CONCENTRATE.get(), "Sphalerite Concentrate");
        translationBuilder.add(ModItems.ZINC_CALCINE.get(), "Zinc Calcine");
        translationBuilder.add(ModItems.ZINC_INGOT.get(), "Zinc Ingot");
        translationBuilder.add(ModItems.ZINC_NUGGET.get(), "Zinc Nugget");
        translationBuilder.add(ModBlocks.SPHALERITE_ORE.get(), "Sphalerite Ore");
        translationBuilder.add(ModBlocks.DEEPSLATE_SPHALERITE_ORE.get(), "Deepslate Sphalerite Ore");
        translationBuilder.add(ModBlocks.RAW_SPHALERITE_BLOCK.get(), "Raw Sphalerite Block");
        translationBuilder.add(ModBlocks.ZINC_BLOCK.get(), "Zinc Block");

        // Cobalt
        translationBuilder.add(ModItems.COBALTITE.get(), "Cobaltite");
        translationBuilder.add(ModItems.CRUSHED_COBALTITE.get(), "Crushed Cobaltite");
        translationBuilder.add(ModItems.COBALT_INGOT.get(), "Cobalt Ingot");
        translationBuilder.add(ModItems.COBALT_NUGGET.get(), "Cobalt Nugget");
        translationBuilder.add(ModBlocks.COBALTITE_ORE.get(), "Cobaltite Ore");
        translationBuilder.add(ModBlocks.DEEPSLATE_COBALTITE_ORE.get(), "Deepslate Cobaltite Ore");
        translationBuilder.add(ModBlocks.RAW_COBALTITE_BLOCK.get(), "Raw Cobaltite Block");
        translationBuilder.add(ModBlocks.COBALT_BLOCK.get(), "Block of Cobalt");

        // Lithium
        translationBuilder.add(ModItems.CRUSHED_SPODUMENE.get(), "Crushed Spodumene");
        translationBuilder.add(ModItems.SPODUMENE_CONCENTRATE.get(), "Spodumene Concentrate");
        translationBuilder.add(ModItems.LITHIUM_CARBONATE.get(), "Lithium Carbonate");
        translationBuilder.add(ModItems.LITHIUM_INGOT.get(), "Lithium Ingot");
        translationBuilder.add(ModItems.LITHIUM_NUGGET.get(), "Lithium Nugget");

        // Nickel
        translationBuilder.add(ModItems.PENTLANDITE.get(), "Pentlandite");
        translationBuilder.add(ModItems.CRUSHED_PENTLANDITE.get(), "Crushed Pentlandite");
        translationBuilder.add(ModItems.PENTLANDITE_CONCENTRATE.get(), "Pentlandite Concentrate");
        translationBuilder.add(ModItems.NICKEL_INGOT.get(), "Nickel Ingot");
        translationBuilder.add(ModItems.NICKEL_NUGGET.get(), "Nickel Nugget");
        translationBuilder.add(ModBlocks.PENTLANDITE_ORE.get(), "Pentlandite Ore");
        translationBuilder.add(ModBlocks.DEEPSLATE_PENTLANDITE_ORE.get(), "Deepslate Pentlandite Ore");
        translationBuilder.add(ModBlocks.RAW_PENTLANDITE_BLOCK.get(), "Raw Pentlandite Block");
        translationBuilder.add(ModBlocks.NICKEL_BLOCK.get(), "Block of Nickel");

        // Iridium
        translationBuilder.add(ModItems.IRIDIUM_INGOT.get(), "Iridium Ingot");
        translationBuilder.add(ModItems.IRIDIUM_NUGGET.get(), "Iridium Nugget");
        translationBuilder.add(ModBlocks.IRIDIUM_ORE.get(), "Iridium Ore");
        translationBuilder.add(ModBlocks.DEEPSLATE_IRIDIUM_ORE.get(), "Deepslate Iridium Ore");
        translationBuilder.add(ModBlocks.IRIDIUM_BLOCK.get(), "Block of Iridium");

        // Silicon
        translationBuilder.add(ModItems.CRUSHED_QUARTZ.get(), "Crushed Quartz");
        translationBuilder.add(ModItems.SILICON_ROD.get(), "Silicon Rod");
        translationBuilder.add(ModItems.SILICON_INGOT.get(), "Silicon Ingot");
        translationBuilder.add(ModItems.SILICON_PELLET.get(), "Silicon Pellet");

        // Tin
        translationBuilder.add(ModItems.CASSITERITE.get(), "Cassiterite");
        translationBuilder.add(ModItems.CRUSHED_CASSITERITE.get(), "Crushed Cassiterite");
        translationBuilder.add(ModItems.CASSITERITE_CONCENTRATE.get(), "Cassiterite Concentrate");
        translationBuilder.add(ModItems.TIN_INGOT.get(), "Tin Ingot");
        translationBuilder.add(ModItems.TIN_NUGGET.get(), "Tin Nugget");
        translationBuilder.add(ModBlocks.CASSITERITE_ORE.get(), "Cassiterite Ore");
        translationBuilder.add(ModBlocks.DEEPSLATE_CASSITERITE_ORE.get(), "Deepslate Cassiterite Ore");
        translationBuilder.add(ModBlocks.RAW_CASSITERITE_BLOCK.get(), "Raw Cassiterite Block");
        translationBuilder.add(ModBlocks.TIN_BLOCK.get(), "Tin Block");

        // Rubber
        translationBuilder.add(ModItems.COAGULATED_LATEX.get(), "Coagulated Latex");
        translationBuilder.add(ModItems.RAW_RUBBER.get(), "Raw Rubber");
        translationBuilder.add(ModItems.RUBBER.get(), "Rubber");

        // Sulfur
        translationBuilder.add(ModItems.PYRITE.get(), "Pyrite");
        translationBuilder.add(ModItems.CRUSHED_SULFUR.get(), "Crushed Sulfur");
        translationBuilder.add(ModItems.SULFUR.get(), "Sulfur");
        translationBuilder.add(ModBlocks.NETHER_PYRITE_ORE.get(), "Nether Pyrite Ore");
        translationBuilder.add(ModBlocks.END_PYRITE_ORE.get(), "End Pyrite Ore");
        translationBuilder.add(ModBlocks.PYRITE_BLOCK.get(), "Block of Pyrite");

        // Steel
        translationBuilder.add(ModItems.STEEL_INGOT.get(), "Steel Ingot");
        translationBuilder.add(ModItems.STEEL_NUGGET.get(), "Steel Nugget");
        translationBuilder.add(ModBlocks.STEEL_BLOCK.get(), "Block of Steel");

        // Sodium
        translationBuilder.add(ModItems.SODIUM_HYDROXIDE.get(), "Sodium Hydroxide");
        translationBuilder.add(ModItems.SODIUM_CARBONATE.get(), "Sodium Carbonate");

        // Quartz
        translationBuilder.add(ModBlocks.QUARTZ_ORE.get(), "Quartz Ore");
        translationBuilder.add(ModBlocks.DEEPSLATE_QUARTZ_ORE.get(), "Deepslate Quartz Ore");

        // Miscellaneous
        translationBuilder.add(ModItems.RED_MUD.get(), "Red Mud");
        translationBuilder.add(ModItems.CRYOLITE.get(), "Cryolite");
        translationBuilder.add(ModFluids.MOLTEN_CRYOLITE.block().get(), "Molten Cryolite");
        translationBuilder.add(ModFluids.MOLTEN_CRYOLITE.bucket().get(), "Bucket of Molten Cryolite");
        translationBuilder.add(ModItems.CARBON_ROD.get(), "Carbon Rod");
        translationBuilder.add(ModItems.BOTTLE_FORMIC_ACID.get(), "Bottle of Formic Acid");
        translationBuilder.add(ModFluids.LATEX.block().get(), "Latex");
        translationBuilder.add(ModFluids.LATEX.bucket().get(), "Bucket of Latex");
        translationBuilder.add(ModFluids.METHANOL.block().get(), "Methanol");
        translationBuilder.add(ModFluids.METHANOL.bucket().get(), "Bucket of Methanol");
        translationBuilder.add(ModFluids.FORMIC_ACID.block().get(), "Formic Acid");
        translationBuilder.add(ModFluids.FORMIC_ACID.bucket().get(), "Bucket of Formic Acid");
        translationBuilder.add(ModFluids.DILUTED_FORMIC_ACID.block().get(), "Diluted Formic Acid");
        translationBuilder.add(ModFluids.DILUTED_FORMIC_ACID.bucket().get(), "Bucket of Diluted Formic Acid");
    }

    private static void addText(LanguageGenerationContext translationBuilder, Component text, String value) {
        if (text.getContents() instanceof TranslatableContents translatableTextContent) {
            translationBuilder.add(translatableTextContent.getKey(), value);
        } else {
            throw new IllegalArgumentException("Text must be translatable! " + text);
        }
    }

    private static void addTextEnum(LanguageGenerationContext translationBuilder, TextEnum textEnum, String value) {
        addText(translationBuilder, textEnum.getAsText(), value);
    }

    private static void addDamageType(LanguageGenerationContext translationBuilder, ResourceKey<DamageType> key, String value) {
        translationBuilder.add("death.attack." + key.identifier().toLanguageKey(), value);
    }

    private static void addSlurry(LanguageGenerationContext translationBuilder,
                                  RegistrationHandle<Slurry, Slurry> slurry, String value) {
        Component name = SlurryVariantAttributes.getName(SlurryVariant.of(slurry.holder()));
        if (name.getContents() instanceof TranslatableContents translatableTextContent) {
            translationBuilder.add(translatableTextContent.getKey(), value);
        } else {
            throw new IllegalArgumentException("Slurry name must be translatable! " + name);
        }
    }
}
