package dev.turtywurty.industria.datagen;

import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.FluidInit;
import dev.turtywurty.industria.init.ItemGroupInit;
import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.item.SeismicScannerItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
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
        translationBuilder.add(BlockInit.CABLE, "Cable");
        translationBuilder.add(BlockInit.WIND_TURBINE, "Wind Turbine");
        addText(translationBuilder, WindTurbineBlockEntity.TITLE, "Wind Turbine");
        translationBuilder.add(FluidInit.CRUDE_OIL_BLOCK, "Crude Oil");
        translationBuilder.add(FluidInit.CRUDE_OIL_BUCKET, "Bucket of Crude Oil");
        translationBuilder.add(BlockInit.OIL_PUMP_JACK, "Oil Pump Jack");
        addText(translationBuilder, OilPumpJackBlockEntity.TITLE, "Oil Pump Jack");
        translationBuilder.add(ItemInit.SEISMIC_SCANNER, "Seismic Scanner");
        addText(translationBuilder, SeismicScannerItem.TITLE, "Seismic Scanner");
    }

    private static void addText(TranslationBuilder translationBuilder, Text text, String value) {
        if(text.getContent() instanceof TranslatableTextContent translatableTextContent) {
            translationBuilder.add(translatableTextContent.getKey(), value);
        } else {
            throw new IllegalArgumentException("Text must be translatable! " + text);
        }
    }
}
