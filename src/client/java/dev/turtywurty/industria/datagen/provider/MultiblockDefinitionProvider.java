package dev.turtywurty.industria.datagen.provider;

import dev.turtywurty.industria.init.IndustriaRegistries;
import dev.turtywurty.industria.multiblock.MultiblockDefinition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record MultiblockDefinitionProvider(Path path, CompletableFuture<HolderLookup.Provider> registriesFuture,
                                           String modId) implements DataProvider {
    public MultiblockDefinitionProvider(PackOutput path, CompletableFuture<HolderLookup.Provider> registriesFuture, String modId) {
        this(path.getOutputFolder(PackOutput.Target.DATA_PACK).resolve("multiblock_definitions"), registriesFuture, modId);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return this.registriesFuture.thenCompose(registries -> {
            HolderLookup.RegistryLookup<MultiblockDefinition> multiblockDefinitions = registries.lookupOrThrow(IndustriaRegistries.MULTIBLOCK_DEFINITION_KEY);
            List<Holder.Reference<MultiblockDefinition>> referenceList = multiblockDefinitions.listElementIds()
                    .filter(registryKey -> registryKey.identifier().getNamespace().equals(this.modId))
                    .map(multiblockDefinitions::getOrThrow)
                    .toList();

            return CompletableFuture.allOf(referenceList.stream()
                    .map(entry -> {
                        Identifier id = entry.unwrapKey().orElseThrow().identifier();
                        MultiblockDefinition definition = entry.value();
                        Path filePath = resolvePath(id);

                        return DataProvider.saveStable(writer, registries, MultiblockDefinition.CODEC.codec(), definition, filePath);
                    })
                    .toArray(CompletableFuture[]::new));
        });
    }

    private Path resolvePath(Identifier id) {
        return this.path.resolve(id.getNamespace()).resolve(id.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Multiblock Definitions";
    }
}
