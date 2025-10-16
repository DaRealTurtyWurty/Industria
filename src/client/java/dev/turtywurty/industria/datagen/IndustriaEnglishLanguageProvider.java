package dev.turtywurty.industria.datagen;

import dev.turtywurty.fabricslurryapi.api.Slurry;
import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.SlurryVariantAttributes;
import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.init.*;
import dev.turtywurty.industria.init.worldgen.BiomeInit;
import dev.turtywurty.industria.item.SeismicScannerItem;
import dev.turtywurty.industria.util.WoodRegistrySet;
import dev.turtywurty.industria.util.enums.TextEnum;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.world.biome.Biome;

import java.util.concurrent.CompletableFuture;

public class IndustriaEnglishLanguageProvider extends FabricLanguageProvider {
    public IndustriaEnglishLanguageProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        addText(translationBuilder, ItemGroupInit.MAIN_TITLE, "Industria");
        addBiome(translationBuilder, BiomeInit.LUMEN_DEPTHS, "The Lumen Depths");
        addBiome(translationBuilder, BiomeInit.REACTOR_BASIN, "The Reactor Basin");
        addBiome(translationBuilder, BiomeInit.LUMINOUS_GROVE, "The Luminous Grove");

        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            WoodSetDatagen.generateEnglishLanguage(woodSet, translationBuilder);
        }

        translationBuilder.add(BlockInit.ALLOY_FURNACE, "Alloy Furnace");
        addText(translationBuilder, AlloyFurnaceBlockEntity.TITLE, "Alloy Furnace");

        translationBuilder.add(BlockInit.THERMAL_GENERATOR, "Thermal Generator");
        addText(translationBuilder, ThermalGeneratorBlockEntity.TITLE, "Thermal Generator");

        translationBuilder.add(BlockInit.BASIC_BATTERY, "Basic Battery");
        translationBuilder.add(BlockInit.ADVANCED_BATTERY, "Advanced Battery");
        translationBuilder.add(BlockInit.ELITE_BATTERY, "Elite Battery");
        translationBuilder.add(BlockInit.ULTIMATE_BATTERY, "Ultimate Battery");
        translationBuilder.add(BlockInit.CREATIVE_BATTERY, "Creative Battery");
        addText(translationBuilder, BatteryBlockEntity.TITLE, "Battery");
        addText(translationBuilder, BatteryBlockEntity.CHARGE_MODE_BUTTON_TOOLTIP_TEXT, "Charge/Discharge");

        translationBuilder.add(BlockInit.COMBUSTION_GENERATOR, "Combustion Generator");
        addText(translationBuilder, CombustionGeneratorBlockEntity.TITLE, "Combustion Generator");

        translationBuilder.add(BlockInit.SOLAR_PANEL, "Solar Panel");
        addText(translationBuilder, SolarPanelBlockEntity.TITLE, "Solar Panel");

        translationBuilder.add(BlockInit.CRUSHER, "Crusher");
        addText(translationBuilder, CrusherBlockEntity.TITLE, "Crusher");

        translationBuilder.add(BlockInit.WIND_TURBINE, "Wind Turbine");
        addText(translationBuilder, WindTurbineBlockEntity.TITLE, "Wind Turbine");

        translationBuilder.add(BlockInit.OIL_PUMP_JACK, "Oil Pump Jack");
        addText(translationBuilder, OilPumpJackBlockEntity.TITLE, "Oil Pump Jack");

        translationBuilder.add(ItemInit.SEISMIC_SCANNER, "Seismic Scanner");
        addText(translationBuilder, SeismicScannerItem.TITLE, "Seismic Scanner");

        translationBuilder.add(BlockInit.DRILL, "Drill");
        addText(translationBuilder, DrillBlockEntity.TITLE, "Drill");
        translationBuilder.add(ItemInit.SIMPLE_DRILL_HEAD, "Simple Drill Head");
        addTextEnum(translationBuilder, DrillBlockEntity.OverflowMethod.SPILLAGE, "Spillage");
        addTextEnum(translationBuilder, DrillBlockEntity.OverflowMethod.VOID, "Void");
        addTextEnum(translationBuilder, DrillBlockEntity.OverflowMethod.PAUSE, "Pause");
        translationBuilder.add(ItemInit.BLOCK_BUILDER_DRILL_HEAD, "Block Builder Drill Head");
        translationBuilder.add(BlockInit.DRILL_TUBE, "Drill Tube");
        addDamageType(translationBuilder, registryLookup, DamageTypeInit.DRILL, "%1$s was drilled to death");

        translationBuilder.add(BlockInit.MOTOR, "Motor");
        addText(translationBuilder, MotorBlockEntity.TITLE, "Motor");

        translationBuilder.add(BlockInit.UPGRADE_STATION, "Upgrade Station");
        addText(translationBuilder, UpgradeStationBlockEntity.TITLE, "Upgrade Station");

        translationBuilder.add(BlockInit.ELECTRIC_FURNACE, "Electric Furnace");
        addText(translationBuilder, ElectricFurnaceBlockEntity.TITLE, "Electric Furnace");

        translationBuilder.add(BlockInit.FRACTIONAL_DISTILLATION_CONTROLLER, "Fractional Distillation Controller");
        addText(translationBuilder, FractionalDistillationControllerBlockEntity.TITLE, "Fractional Distillation Controller");
        translationBuilder.add(BlockInit.FRACTIONAL_DISTILLATION_TOWER, "Fractional Distillation Tower");

        translationBuilder.add(BlockInit.INDUCTION_HEATER, "Induction Heater");
        addText(translationBuilder, InductionHeaterBlockEntity.TITLE, "Induction Heater");

        translationBuilder.add(BlockInit.FLUID_PUMP, "Fluid Pump");
        addText(translationBuilder, FluidPumpBlockEntity.TITLE, "Fluid Pump");

        translationBuilder.add(BlockInit.MIXER, "Mixer");
        addText(translationBuilder, MixerBlockEntity.TITLE, "Mixer");

        translationBuilder.add(BlockInit.DIGESTER, "Digester");
        addText(translationBuilder, DigesterBlockEntity.TITLE, "Digester");

        translationBuilder.add(BlockInit.CLARIFIER, "Clarifier");
        addText(translationBuilder, ClarifierBlockEntity.TITLE, "Clarifier");

        translationBuilder.add(BlockInit.CRYSTALLIZER, "Crystallizer");
        addText(translationBuilder, CrystallizerBlockEntity.TITLE, "Crystallizer");

        translationBuilder.add(BlockInit.ROTARY_KILN_CONTROLLER, "Rotary Kiln Controller");
        translationBuilder.add(BlockInit.ROTARY_KILN, "Rotary Kiln");

        translationBuilder.add(BlockInit.ELECTROLYZER, "Electrolyzer");
        addText(translationBuilder, ElectrolyzerBlockEntity.TITLE, "Electrolyzer");

        translationBuilder.add(BlockInit.FLUID_TANK, "Fluid Tank");
        addText(translationBuilder, FluidTankBlockEntity.TITLE, "Fluid Tank");

        translationBuilder.add(ItemInit.ROTARY_KILN, "Rotary Kiln");

        translationBuilder.add(BlockInit.SHAKING_TABLE, "Shaking Table");
        addText(translationBuilder, ShakingTableBlockEntity.TITLE, "Shaking Table");

        translationBuilder.add(BlockInit.CENTRIFUGAL_CONCENTRATOR, "Centrifugal Concentrator");
        addText(translationBuilder, CentrifugalConcentratorBlockEntity.TITLE, "Centrifugal Concentrator");

        translationBuilder.add(BlockInit.CABLE, "Cable");
        translationBuilder.add(BlockInit.FLUID_PIPE, "Fluid Pipe");
        translationBuilder.add(BlockInit.SLURRY_PIPE, "Slurry Pipe");
        translationBuilder.add(BlockInit.HEAT_PIPE, "Heat Pipe");

        translationBuilder.add(FluidInit.CRUDE_OIL.block(), "Crude Oil");
        translationBuilder.add(FluidInit.CRUDE_OIL.bucket(), "Bucket of Crude Oil");
        addSlurry(translationBuilder, SlurryInit.CLAY_SLURRY, "Clay Slurry");

        // Aluminium
        translationBuilder.add(ItemInit.BAUXITE, "Bauxite");
        translationBuilder.add(ItemInit.CRUSHED_BAUXITE, "Crushed Bauxite");
        translationBuilder.add(ItemInit.SODIUM_ALUMINATE, "Sodium Aluminate");
        translationBuilder.add(ItemInit.ALUMINIUM_HYDROXIDE, "Aluminium Hydroxide");
        translationBuilder.add(ItemInit.ALUMINA, "Alumina");
        translationBuilder.add(ItemInit.ALUMINIUM_INGOT, "Aluminium Ingot");
        translationBuilder.add(ItemInit.ALUMINIUM_NUGGET, "Aluminium Nugget");
        translationBuilder.add(ItemInit.ALUMINIUM_PLATE, "Aluminium Plate");
        translationBuilder.add(FluidInit.DIRTY_SODIUM_ALUMINATE.block(), "Dirty Sodium Aluminate");
        translationBuilder.add(FluidInit.DIRTY_SODIUM_ALUMINATE.bucket(), "Bucket of Dirty Sodium Aluminate");
        translationBuilder.add(FluidInit.SODIUM_ALUMINATE.block(), "Sodium Aluminate");
        translationBuilder.add(FluidInit.SODIUM_ALUMINATE.bucket(), "Bucket of Sodium Aluminate");
        translationBuilder.add(FluidInit.MOLTEN_ALUMINIUM.block(), "Molten Aluminium");
        translationBuilder.add(FluidInit.MOLTEN_ALUMINIUM.bucket(), "Bucket of Molten Aluminium");
        addSlurry(translationBuilder, SlurryInit.BAUXITE_SLURRY, "Bauxite Slurry");
        translationBuilder.add(BlockInit.BAUXITE_ORE, "Bauxite Ore");
        translationBuilder.add(BlockInit.DEEPSLATE_BAUXITE_ORE, "Deepslate Bauxite Ore");
        translationBuilder.add(BlockInit.RAW_BAUXITE_BLOCK, "Raw Bauxite Block");
        translationBuilder.add(BlockInit.ALUMINIUM_BLOCK, "Block of Aluminium");

        // Silver
        translationBuilder.add(ItemInit.ARGENTITE, "Argentite");
        translationBuilder.add(ItemInit.CRUSHED_ARGENTITE, "Crushed Argentite");
        translationBuilder.add(ItemInit.ARGENTITE_CONCENTRATE, "Argentite Concentrate");
        translationBuilder.add(ItemInit.LEAD_BULLION, "Lead Bullion");
        translationBuilder.add(ItemInit.DORE_SILVER, "Doré Silver");
        translationBuilder.add(ItemInit.SILVER_INGOT, "Silver Ingot");
        translationBuilder.add(ItemInit.SILVER_NUGGET, "Silver Nugget");
        translationBuilder.add(BlockInit.ARGENTITE_ORE, "Argentite Ore");
        translationBuilder.add(BlockInit.DEEPSLATE_ARGENTITE_ORE, "Deepslate Argentite Ore");
        translationBuilder.add(BlockInit.RAW_ARGENTITE_BLOCK, "Raw Argentite Block");
        translationBuilder.add(BlockInit.SILVER_BLOCK, "Block of Silver");

        // Lead
        translationBuilder.add(ItemInit.GALENA, "Galena");
        translationBuilder.add(ItemInit.CRUSHED_GALENA, "Crushed Galena");
        translationBuilder.add(ItemInit.GALENA_CONCENTRATE, "Galena Concentrate");
        translationBuilder.add(ItemInit.TETRAGONAL_LITHARGE, "Tetragonal Litharge");
        translationBuilder.add(ItemInit.LEAD_INGOT, "Lead Ingot");
        translationBuilder.add(ItemInit.LEAD_NUGGET, "Lead Nugget");
        translationBuilder.add(BlockInit.GALENA_ORE, "Galena Ore");
        translationBuilder.add(BlockInit.DEEPSLATE_GALENA_ORE, "Deepslate Galena Ore");
        translationBuilder.add(BlockInit.RAW_GALENA_BLOCK, "Raw Galena Block");
        translationBuilder.add(BlockInit.LEAD_BLOCK, "Block of Lead");

        // Titanium
        translationBuilder.add(ItemInit.ILMENITE, "Ilmenite");
        translationBuilder.add(ItemInit.CRUSHED_ILMENITE, "Crushed Ilmenite");
        translationBuilder.add(ItemInit.ILMENITE_CONCENTRATE, "Ilmenite Concentrate");
        translationBuilder.add(ItemInit.TITANIUM_TETRACHLORIDE, "Titanium Tetrachloride");
        translationBuilder.add(ItemInit.TITANIUM_INGOT, "Titanium Ingot");
        translationBuilder.add(ItemInit.TITANIUM_NUGGET, "Titanium Nugget");
        translationBuilder.add(ItemInit.TITANIUM_PLATE, "Titanium Plate");
        translationBuilder.add(BlockInit.TITANIUM_BLOCK, "Block of Titanium");
        translationBuilder.add(BlockInit.ILMENITE_ORE, "Ilmenite Ore");
        translationBuilder.add(BlockInit.DEEPSLATE_ILMENITE_ORE, "Deepslate Ilmenite Ore");
        translationBuilder.add(BlockInit.RAW_ILMENITE_BLOCK, "Raw Ilmenite Block");

        // Zinc
        translationBuilder.add(ItemInit.SPHALERITE, "Sphalerite");
        translationBuilder.add(ItemInit.CRUSHED_SPHALERITE, "Crushed Sphalerite");
        translationBuilder.add(ItemInit.SPHALERITE_CONCENTRATE, "Sphalerite Concentrate");
        translationBuilder.add(ItemInit.ZINC_CALCINE, "Zinc Calcine");
        translationBuilder.add(ItemInit.ZINC_INGOT, "Zinc Ingot");
        translationBuilder.add(ItemInit.ZINC_NUGGET, "Zinc Nugget");
        translationBuilder.add(BlockInit.SPHALERITE_ORE, "Sphalerite Ore");
        translationBuilder.add(BlockInit.DEEPSLATE_SPHALERITE_ORE, "Deepslate Sphalerite Ore");
        translationBuilder.add(BlockInit.RAW_SPHALERITE_BLOCK, "Raw Sphalerite Block");
        translationBuilder.add(BlockInit.ZINC_BLOCK, "Zinc Block");

        // Cobalt
        translationBuilder.add(ItemInit.COBALTITE, "Cobaltite");
        translationBuilder.add(ItemInit.CRUSHED_COBALTITE, "Crushed Cobaltite");
        translationBuilder.add(ItemInit.COBALT_INGOT, "Cobalt Ingot");
        translationBuilder.add(ItemInit.COBALT_NUGGET, "Cobalt Nugget");
        translationBuilder.add(BlockInit.COBALTITE_ORE, "Cobaltite Ore");
        translationBuilder.add(BlockInit.DEEPSLATE_COBALTITE_ORE, "Deepslate Cobaltite Ore");
        translationBuilder.add(BlockInit.RAW_COBALTITE_BLOCK, "Raw Cobaltite Block");
        translationBuilder.add(BlockInit.COBALT_BLOCK, "Block of Cobalt");

        // Lithium
        translationBuilder.add(ItemInit.CRUSHED_SPODUMENE, "Crushed Spodumene");
        translationBuilder.add(ItemInit.SPODUMENE_CONCENTRATE, "Spodumene Concentrate");
        translationBuilder.add(ItemInit.LITHIUM_CARBONATE, "Lithium Carbonate");
        translationBuilder.add(ItemInit.LITHIUM_INGOT, "Lithium Ingot");
        translationBuilder.add(ItemInit.LITHIUM_NUGGET, "Lithium Nugget");

        // Nickel
        translationBuilder.add(ItemInit.PENTLANDITE, "Pentlandite");
        translationBuilder.add(ItemInit.CRUSHED_PENTLANDITE, "Crushed Pentlandite");
        translationBuilder.add(ItemInit.PENTLANDITE_CONCENTRATE, "Pentlandite Concentrate");
        translationBuilder.add(ItemInit.NICKEL_INGOT, "Nickel Ingot");
        translationBuilder.add(ItemInit.NICKEL_NUGGET, "Nickel Nugget");
        translationBuilder.add(BlockInit.PENTLANDITE_ORE, "Pentlandite Ore");
        translationBuilder.add(BlockInit.DEEPSLATE_PENTLANDITE_ORE, "Deepslate Pentlandite Ore");
        translationBuilder.add(BlockInit.RAW_PENTLANDITE_BLOCK, "Raw Pentlandite Block");
        translationBuilder.add(BlockInit.NICKEL_BLOCK, "Block of Nickel");

        // Iridium
        translationBuilder.add(ItemInit.IRIDIUM_INGOT, "Iridium Ingot");
        translationBuilder.add(ItemInit.IRIDIUM_NUGGET, "Iridium Nugget");
        translationBuilder.add(BlockInit.IRIDIUM_ORE, "Iridium Ore");
        translationBuilder.add(BlockInit.DEEPSLATE_IRIDIUM_ORE, "Deepslate Iridium Ore");
        translationBuilder.add(BlockInit.IRIDIUM_BLOCK, "Block of Iridium");

        // Silicon
        translationBuilder.add(ItemInit.CRUSHED_QUARTZ, "Crushed Quartz");
        translationBuilder.add(ItemInit.SILICON_ROD, "Silicon Rod");
        translationBuilder.add(ItemInit.SILICON_INGOT, "Silicon Ingot");
        translationBuilder.add(ItemInit.SILICON_PELLET, "Silicon Pellet");

        // Tin
        translationBuilder.add(ItemInit.CASSITERITE, "Cassiterite");
        translationBuilder.add(ItemInit.CRUSHED_CASSITERITE, "Crushed Cassiterite");
        translationBuilder.add(ItemInit.CASSITERITE_CONCENTRATE, "Cassiterite Concentrate");
        translationBuilder.add(ItemInit.TIN_INGOT, "Tin Ingot");
        translationBuilder.add(ItemInit.TIN_NUGGET, "Tin Nugget");
        translationBuilder.add(BlockInit.CASSITERITE_ORE, "Cassiterite Ore");
        translationBuilder.add(BlockInit.DEEPSLATE_CASSITERITE_ORE, "Deepslate Cassiterite Ore");
        translationBuilder.add(BlockInit.RAW_CASSITERITE_BLOCK, "Raw Cassiterite Block");
        translationBuilder.add(BlockInit.TIN_BLOCK, "Tin Block");

        // Rubber
        translationBuilder.add(ItemInit.COAGULATED_LATEX, "Coagulated Latex");
        translationBuilder.add(ItemInit.RAW_RUBBER, "Raw Rubber");
        translationBuilder.add(ItemInit.RUBBER, "Rubber");

        // Sulfur
        translationBuilder.add(ItemInit.PYRITE, "Pyrite");
        translationBuilder.add(ItemInit.CRUSHED_SULFUR, "Crushed Sulfur");
        translationBuilder.add(ItemInit.SULFUR, "Sulfur");
        translationBuilder.add(BlockInit.NETHER_PYRITE_ORE, "Nether Pyrite Ore");
        translationBuilder.add(BlockInit.END_PYRITE_ORE, "End Pyrite Ore");
        translationBuilder.add(BlockInit.PYRITE_BLOCK, "Block of Pyrite");

        // Steel
        translationBuilder.add(ItemInit.STEEL_INGOT, "Steel Ingot");
        translationBuilder.add(ItemInit.STEEL_NUGGET, "Steel Nugget");
        translationBuilder.add(BlockInit.STEEL_BLOCK, "Block of Steel");

        // Sodium
        translationBuilder.add(ItemInit.SODIUM_HYDROXIDE, "Sodium Hydroxide");
        translationBuilder.add(ItemInit.SODIUM_CARBONATE, "Sodium Carbonate");

        // Quartz
        translationBuilder.add(BlockInit.QUARTZ_ORE, "Quartz Ore");
        translationBuilder.add(BlockInit.DEEPSLATE_QUARTZ_ORE, "Deepslate Quartz Ore");

        // Miscellaneous
        translationBuilder.add(ItemInit.RED_MUD, "Red Mud");
        translationBuilder.add(ItemInit.CRYOLITE, "Cryolite");
        translationBuilder.add(FluidInit.MOLTEN_CRYOLITE.block(), "Molten Cryolite");
        translationBuilder.add(FluidInit.MOLTEN_CRYOLITE.bucket(), "Bucket of Molten Cryolite");
        translationBuilder.add(ItemInit.CARBON_ROD, "Carbon Rod");
    }

    private static void addText(TranslationBuilder translationBuilder, Text text, String value) {
        if(text.getContent() instanceof TranslatableTextContent translatableTextContent) {
            translationBuilder.add(translatableTextContent.getKey(), value);
        } else {
            throw new IllegalArgumentException("Text must be translatable! " + text);
        }
    }

    private static void addTextEnum(TranslationBuilder translationBuilder, TextEnum textEnum, String value) {
        addText(translationBuilder, textEnum.getAsText(), value);
    }

    private static void addDamageType(TranslationBuilder translationBuilder, RegistryWrapper.WrapperLookup regLookup, RegistryKey<DamageType> key, String value) {
        RegistryEntryLookup<DamageType> lookup = regLookup.getOrThrow(RegistryKeys.DAMAGE_TYPE);
        DamageType damageType = lookup.getOrThrow(key).value();
        translationBuilder.add("death.attack." + damageType.msgId(), value);
    }

    private static void addSlurry(TranslationBuilder translationBuilder, Slurry slurry, String value) {
        Text name = SlurryVariantAttributes.getName(SlurryVariant.of(slurry));
        if (name.getContent() instanceof TranslatableTextContent translatableTextContent) {
            translationBuilder.add(translatableTextContent.getKey(), value);
        } else {
            throw new IllegalArgumentException("Slurry name must be translatable! " + name);
        }
    }

    private static void addBiome(TranslationBuilder translationBuilder, RegistryKey<Biome> key, String value) {
        translationBuilder.add(key.getValue().toTranslationKey("biome"), value);
    }
}
