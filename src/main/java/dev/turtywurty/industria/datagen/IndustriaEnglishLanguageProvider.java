package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.block.BatteryBlock;
import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.init.*;
import dev.turtywurty.industria.item.SeismicScannerItem;
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

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        addText(translationBuilder, ItemGroupInit.MAIN_TITLE, "Industria");

        translationBuilder.add(BlockInit.ALLOY_FURNACE, "Alloy Furnace");
        addText(translationBuilder, AlloyFurnaceBlockEntity.TITLE, "Alloy Furnace");
        translationBuilder.add(ItemInit.STEEL_INGOT, "Steel Ingot");
        translationBuilder.add(BlockInit.THERMAL_GENERATOR, "Thermal Generator");
        addText(translationBuilder, ThermalGeneratorBlockEntity.TITLE, "Thermal Generator");
        for(BatteryBlock block : BlockInit.BATTERIES){
            translationBuilder.add(block, block.getLevel().getName()+" Battery");
        }

        addText(translationBuilder, BatteryBlockEntity.TITLE, "Battery");
        addText(translationBuilder, BatteryBlockEntity.CHARGE_MODE_BUTTON_TOOLTIP_TEXT, "Charge/Discharge");
        translationBuilder.add(BlockInit.COMBUSTION_GENERATOR, "Combustion Generator");
        addText(translationBuilder, CombustionGeneratorBlockEntity.TITLE, "Combustion Generator");
        translationBuilder.add(BlockInit.SOLAR_PANEL, "Solar Panel");
        addText(translationBuilder, SolarPanelBlockEntity.TITLE, "Solar Panel");
        translationBuilder.add(BlockInit.CRUSHER, "Crusher");
        addText(translationBuilder, CrusherBlockEntity.TITLE, "Crusher");
        translationBuilder.add(BlockInit.CABLE, "Cable");
        translationBuilder.add(BlockInit.WIND_TURBINE, "Wind Turbine");
        addText(translationBuilder, WindTurbineBlockEntity.TITLE, "Wind Turbine");
        translationBuilder.add(FluidInit.CRUDE_OIL_BLOCK, "Crude Oil");
        translationBuilder.add(FluidInit.CRUDE_OIL_BUCKET, "Bucket of Crude Oil");
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
}
