package dev.turtywurty.industria.init.worldgen;

import dev.turtywurty.industria.Industria;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.biome.*;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;

public class BiomeInit {
    public static final RegistryKey<Biome> LUMEN_DEPTHS = registerKey("the_lumen_depths");

    public static void bootstrap(Registerable<Biome> context) {
        RegistryEntryLookup<SoundEvent> soundEventLookup = context.getRegistryLookup(RegistryKeys.SOUND_EVENT);
        RegistryEntryLookup<ConfiguredCarver<?>> configuredCarverLookup = context.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER);
        RegistryEntryLookup<PlacedFeature> placedFeatureLookup = context.getRegistryLookup(RegistryKeys.PLACED_FEATURE);

        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(placedFeatureLookup, configuredCarverLookup);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, true);

        context.register(LUMEN_DEPTHS, new Biome.Builder()
                .precipitation(false)
                .temperature(0.3f)
                .downfall(0.8f)
                .effects(new BiomeEffects.Builder()
                        .waterColor(0x3F76E4)
                        .waterFogColor(0x050533)
                        .fogColor(0xC0D8FF)
                        .skyColor(0x77ADFF)
                        .moodSound(new BiomeMoodSound(lookupSoundEvent(soundEventLookup, SoundEvents.ENTITY_GOAT_SCREAMING_AMBIENT), 6000, 8, 2.0))
                        .music(MusicType.createIngameMusic(lookupSoundEvent(soundEventLookup, SoundEvents.ENTITY_CAT_PURREOW)))
                        .particleConfig(new BiomeParticleConfig(new DustParticleEffect(0x000FF, 1.0F), 0.002f))
                        .grassColor(0x88BB55)
                        .foliageColor(0x88BB55)
                        .build())
                .spawnSettings(new SpawnSettings.Builder()
                        .spawn(SpawnGroup.AXOLOTLS, 8, new SpawnSettings.SpawnEntry(EntityType.AXOLOTL, 1, 2))
                        .build())
                        .generationSettings(lookupBackedBuilder.build())
                .build());
    }

    private static RegistryEntry.Reference<SoundEvent> lookupSoundEvent(RegistryEntryLookup<SoundEvent> lookup, SoundEvent event) {
        return lookup.getOrThrow(RegistryKey.of(RegistryKeys.SOUND_EVENT, event.id()));
    }

    public static RegistryKey<Biome> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.BIOME, Industria.id(name));
    }
}
