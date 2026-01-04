package dev.turtywurty.industria;

import dev.turtywurty.industria.datagen.*;
import dev.turtywurty.industria.datagen.generator.IndustriaDamageTypeGenerator;
import dev.turtywurty.industria.datagen.generator.IndustriaWorldGenerator;
import dev.turtywurty.industria.init.DamageTypeInit;
import dev.turtywurty.industria.init.worldgen.ConfiguredFeatureInit;
import dev.turtywurty.industria.init.worldgen.PlacedFeatureInit;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

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
		pack.addProvider(IndustriaFluidTagProvider::new);
		pack.addProvider(IndustriaEntityTypeTagProvider::new);
		pack.addProvider(IndustriaWorldGenerator::new);
		pack.addProvider(IndustriaDamageTypeGenerator::new);
        //pack.addProvider((output, registriesFuture) -> new MultiblockDefinitionProvider(output, registriesFuture, Industria.MOD_ID));
	}

	@Override
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
		registryBuilder.add(Registries.CONFIGURED_FEATURE, ConfiguredFeatureInit::bootstrap);
		registryBuilder.add(Registries.PLACED_FEATURE, PlacedFeatureInit::bootstrap);
		registryBuilder.add(Registries.DAMAGE_TYPE, DamageTypeInit::bootstrap);
        //registryBuilder.addRegistry(IndustriaRegistries.MULTIBLOCK_DEFINITION_KEY, MultiblockDefinitionInit::bootstrap);
	}
}
