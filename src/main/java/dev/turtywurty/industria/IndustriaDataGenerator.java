package dev.turtywurty.industria;

import dev.turtywurty.industria.datagen.*;
import dev.turtywurty.industria.datagen.generator.IndustriaWorldGenerator;
import dev.turtywurty.industria.init.worldgen.ConfiguredFeatureInit;
import dev.turtywurty.industria.init.worldgen.PlacedFeatureInit;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class IndustriaDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(IndustriaEnglishLanguageProvider::new);
		pack.addProvider(IndustriaModelProvider::new);
		pack.addProvider(IndustriaBlockLootTableProvider::new);
		pack.addProvider(IndustriaRecipeProvider::new);
		pack.addProvider(IndustriaBlockTagProvider::new);
		pack.addProvider(IndustriaItemTagProvider::new);
		pack.addProvider(IndustriaWorldGenerator::new);
	}

	@Override
	public void buildRegistry(RegistryBuilder registryBuilder) {
		registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, ConfiguredFeatureInit::bootstrap);
		registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, PlacedFeatureInit::bootstrap);
	}
}
