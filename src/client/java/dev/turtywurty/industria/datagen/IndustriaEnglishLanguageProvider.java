package dev.turtywurty.industria.datagen;

import dev.turtywurty.fabricslurryapi.api.Slurry;
import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.SlurryVariantAttributes;
import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.init.*;
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

import java.util.concurrent.CompletableFuture;

public class IndustriaEnglishLanguageProvider extends FabricLanguageProvider {
    public IndustriaEnglishLanguageProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    private static void addText(TranslationBuilder translationBuilder, Text text, String value) {
        if (text.getContent() instanceof TranslatableTextContent translatableTextContent) {
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

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        addText(translationBuilder, ItemGroupInit.MAIN_TITLE, "Industria");

        for (WoodRegistrySet woodSet : WoodRegistrySet.getWoodSets()) {
            WoodSetDatagen.generateEnglishLanguage(woodSet, translationBuilder);
        }

        translationBuilder.add(BlockInit.ALLOY_FURNACE, "Alloy Furnace");
        addText(translationBuilder, AlloyFurnaceBlockEntity.TITLE, "Alloy Furnace");
        translationBuilder.add(ItemInit.STEEL_INGOT, "Steel Ingot");
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
        translationBuilder.add(BlockInit.MOTOR, "Motor");
        addText(translationBuilder, MotorBlockEntity.TITLE, "Motor");
        translationBuilder.add(ItemInit.BLOCK_BUILDER_DRILL_HEAD, "Block Builder Drill Head");
        translationBuilder.add(BlockInit.DRILL_TUBE, "Drill Tube");
        addDamageType(translationBuilder, registryLookup, DamageTypeInit.DRILL, "%1$s was drilled to death");
        translationBuilder.add(BlockInit.UPGRADE_STATION, "Upgrade Station");
        addText(translationBuilder, UpgradeStationBlockEntity.TITLE, "Upgrade Station");
        translationBuilder.add(BlockInit.ELECTRIC_FURNACE, "Electric Furnace");
        addText(translationBuilder, ElectricFurnaceBlockEntity.TITLE, "Electric Furnace");
        translationBuilder.add(BlockInit.FRACTIONAL_DISTILLATION_CONTROLLER, "Fractional Distillation Controller");
        addText(translationBuilder, FractionalDistillationControllerBlockEntity.TITLE, "Fractional Distillation Controller");
        translationBuilder.add(BlockInit.FRACTIONAL_DISTILLATION_TOWER, "Fractional Distillation Tower");
        translationBuilder.add(BlockInit.INDUCTION_HEATER, "Induction Heater");
        addText(translationBuilder, InductionHeaterBlockEntity.TITLE, "Induction Heater");

        translationBuilder.add(BlockInit.CABLE, "Cable");
        translationBuilder.add(BlockInit.FLUID_PIPE, "Fluid Pipe");
        translationBuilder.add(BlockInit.SLURRY_PIPE, "Slurry Pipe");
        translationBuilder.add(BlockInit.HEAT_PIPE, "Heat Pipe");

        translationBuilder.add(BlockInit.FLUID_PUMP, "Fluid Pump");
        addText(translationBuilder, FluidPumpBlockEntity.TITLE, "Fluid Pump");

        translationBuilder.add(BlockInit.BAUXITE_ORE, "Bauxite Ore");
        translationBuilder.add(BlockInit.CASSITERITE_ORE, "Cassiterite Ore");
        translationBuilder.add(BlockInit.ZINC_ORE, "Zinc Ore");
        translationBuilder.add(BlockInit.DEEPSLATE_BAUXITE_ORE, "Deepslate Bauxite Ore");
        translationBuilder.add(BlockInit.DEEPSLATE_CASSITERITE_ORE, "Deepslate Cassiterite Ore");
        translationBuilder.add(BlockInit.DEEPSLATE_ZINC_ORE, "Deepslate Zinc Ore");

        translationBuilder.add(BlockInit.ALUMINIUM_BLOCK, "Aluminium Block");
        translationBuilder.add(BlockInit.TIN_BLOCK, "Tin Block");
        translationBuilder.add(BlockInit.ZINC_BLOCK, "Zinc Block");

        translationBuilder.add(BlockInit.RAW_BAUXITE_BLOCK, "Raw Bauxite Block");
        translationBuilder.add(BlockInit.RAW_CASSITERITE_BLOCK, "Raw Cassiterite Block");
        translationBuilder.add(BlockInit.RAW_ZINC_BLOCK, "Raw Zinc Block");

        translationBuilder.add(ItemInit.RAW_BAUXITE, "Raw Bauxite");
        translationBuilder.add(ItemInit.RAW_CASSITERITE, "Raw Cassiterite");
        translationBuilder.add(ItemInit.RAW_ZINC, "Raw Zinc");

        translationBuilder.add(ItemInit.ALUMINIUM_INGOT, "Aluminium Ingot");
        translationBuilder.add(ItemInit.TIN_INGOT, "Tin Ingot");
        translationBuilder.add(ItemInit.ZINC_INGOT, "Zinc Ingot");

        translationBuilder.add(ItemInit.ALUMINIUM_NUGGET, "Aluminium Nugget");
        translationBuilder.add(ItemInit.TIN_NUGGET, "Tin Nugget");
        translationBuilder.add(ItemInit.ZINC_NUGGET, "Zinc Nugget");

        translationBuilder.add(ItemInit.SODIUM_HYDROXIDE, "Sodium Hydroxide");

        translationBuilder.add(BlockInit.MIXER, "Mixer");
        addText(translationBuilder, MixerBlockEntity.TITLE, "Mixer");

        translationBuilder.add(ItemInit.SODIUM_ALUMINATE, "Sodium Aluminate");

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

        translationBuilder.add(ItemInit.RED_MUD, "Red Mud");
        translationBuilder.add(FluidInit.CRUDE_OIL.block(), "Crude Oil");
        translationBuilder.add(FluidInit.CRUDE_OIL.bucket(), "Bucket of Crude Oil");
        translationBuilder.add(FluidInit.DIRTY_SODIUM_ALUMINATE.block(), "Dirty Sodium Aluminate");
        translationBuilder.add(FluidInit.DIRTY_SODIUM_ALUMINATE.bucket(), "Bucket of Dirty Sodium Aluminate");
        translationBuilder.add(FluidInit.SODIUM_ALUMINATE.block(), "Sodium Aluminate");
        translationBuilder.add(FluidInit.SODIUM_ALUMINATE.bucket(), "Bucket of Sodium Aluminate");
        translationBuilder.add(FluidInit.MOLTEN_ALUMINIUM.block(), "Molten Aluminium");
        translationBuilder.add(FluidInit.MOLTEN_ALUMINIUM.bucket(), "Bucket of Molten Aluminium");
        translationBuilder.add(FluidInit.MOLTEN_CRYOLITE.block(), "Molten Cryolite");
        translationBuilder.add(FluidInit.MOLTEN_CRYOLITE.bucket(), "Bucket of Molten Cryolite");
        translationBuilder.add(ItemInit.ALUMINIUM_HYDROXIDE, "Aluminium Hydroxide");
        translationBuilder.add(ItemInit.SODIUM_CARBONATE, "Sodium Carbonate");
        translationBuilder.add(ItemInit.ALUMINA, "Alumina");
        translationBuilder.add(ItemInit.ROTARY_KILN, "Rotary Kiln");
        translationBuilder.add(ItemInit.CRYOLITE, "Cryolite");
        translationBuilder.add(ItemInit.CARBON_ROD, "Carbon Rod");

        translationBuilder.add(BlockInit.SHAKING_TABLE, "Shaking Table");
        addText(translationBuilder, ShakingTableBlockEntity.TITLE, "Shaking Table");

        translationBuilder.add(ItemInit.CRUSHED_CASSITERITE, "Crushed Cassiterite");
        translationBuilder.add(ItemInit.CASSITERITE_CONCENTRATE, "Cassiterite Concentrate");

        addSlurry(translationBuilder, SlurryInit.BAUXITE_SLURRY, "Bauxite Slurry");
        addSlurry(translationBuilder, SlurryInit.CLAY_SLURRY, "Clay Slurry");

        translationBuilder.add(BlockInit.CENTRIFUGAL_CONCENTRATOR, "Centrifugal Concentrator");
        addText(translationBuilder, CentrifugalConcentratorBlockEntity.TITLE, "Centrifugal Concentrator");
    }
}
