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
import net.minecraft.world.gen.feature.PlacedFeature;

public class BiomeInit {
    public static final RegistryKey<Biome> LUMEN_DEPTHS = registerKey("the_lumen_depths");
    public static final RegistryKey<Biome> REACTOR_BASIN = registerKey("reactor_basin");
    public static final RegistryKey<Biome> LUMINOUS_GROVE = registerKey("luminous_grove");

    public static void bootstrap(Registerable<Biome> context) {
        RegistryEntryLookup<SoundEvent> soundEventLookup = context.getRegistryLookup(RegistryKeys.SOUND_EVENT);
        RegistryEntryLookup<ConfiguredCarver<?>> configuredCarverLookup = context.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER);
        RegistryEntryLookup<PlacedFeature> placedFeatureLookup = context.getRegistryLookup(RegistryKeys.PLACED_FEATURE);

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
                .generationSettings(createGenerationSettings(placedFeatureLookup, configuredCarverLookup))
                .build());

        context.register(REACTOR_BASIN, new Biome.Builder()
                .precipitation(true)
                .temperature(1.5f)
                .downfall(0.6f)
                .effects(new BiomeEffects.Builder()
                        .waterColor(0x1D3D5F)
                        .waterFogColor(0x102233)
                        .fogColor(0x4A7CA4)
                        .skyColor(0x5073A0)
                        .moodSound(new BiomeMoodSound(lookupSoundEvent(soundEventLookup, SoundEvents.ENTITY_ELDER_GUARDIAN_AMBIENT), 6000, 8, 1.5))
                        .music(MusicType.createIngameMusic(SoundEvents.MUSIC_DISC_OTHERSIDE))
                        .particleConfig(new BiomeParticleConfig(new DustParticleEffect(0x2FA8A0, 1.0F), 0.0015f))
                        .grassColor(0x3F7F60)
                        .foliageColor(0x2B5C42)
                        .build())
                .spawnSettings(new SpawnSettings.Builder()
                        .spawn(SpawnGroup.WATER_CREATURE, 8, new SpawnSettings.SpawnEntry(EntityType.GLOW_SQUID, 2, 4))
                        .build())
                .generationSettings(createGenerationSettings(placedFeatureLookup, configuredCarverLookup))
                .build());

        context.register(LUMINOUS_GROVE, new Biome.Builder()
                .precipitation(true)
                .temperature(0.8f)
                .downfall(0.9f)
                .effects(new BiomeEffects.Builder()
                        .waterColor(0x2C5F84)
                        .waterFogColor(0x163040)
                        .fogColor(0x99C4D5)
                        .skyColor(0xA3D2F2)
                        .moodSound(new BiomeMoodSound(SoundEvents.AMBIENT_CAVE, 4000, 6, 1.0))
                        .music(MusicType.createIngameMusic(SoundEvents.MUSIC_DISC_FAR))
                        .particleConfig(new BiomeParticleConfig(new DustParticleEffect(0x7FFFFF, 1.2F), 0.003f))
                        .grassColor(0x6BCB7D)
                        .foliageColor(0x58B271)
                        .build())
                .spawnSettings(new SpawnSettings.Builder()
                        .spawn(SpawnGroup.AMBIENT, 6, new SpawnSettings.SpawnEntry(EntityType.BAT, 2, 4))
                        .build())
                .generationSettings(createGenerationSettings(placedFeatureLookup, configuredCarverLookup))
                .build());
    }

    private static RegistryEntry.Reference<SoundEvent> lookupSoundEvent(RegistryEntryLookup<SoundEvent> lookup, SoundEvent event) {
        return lookup.getOrThrow(RegistryKey.of(RegistryKeys.SOUND_EVENT, event.id()));
    }

    private static GenerationSettings createGenerationSettings(RegistryEntryLookup<PlacedFeature> placedFeatureLookup,
                                                               RegistryEntryLookup<ConfiguredCarver<?>> configuredCarverLookup) {
        return new GenerationSettings.LookupBackedBuilder(placedFeatureLookup, configuredCarverLookup).build();
    }

    public static RegistryKey<Biome> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.BIOME, Industria.id(name));
    }
}
