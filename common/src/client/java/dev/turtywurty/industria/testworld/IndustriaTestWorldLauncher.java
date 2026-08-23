package dev.turtywurty.industria.testworld;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public final class IndustriaTestWorldLauncher {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component VANILLA_BUTTON_LABEL = Component.literal("Create Test World");
    private static final Component INDUSTRIA_BUTTON_LABEL = Component.literal("Industria Test");
    private static final LevelSettings LEVEL_SETTINGS = new LevelSettings(
            IndustriaTestWorld.WORLD_ID,
            GameType.CREATIVE,
            new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false),
            true,
            WorldDataConfiguration.DEFAULT);
    private static final WorldOptions WORLD_OPTIONS = new WorldOptions(0L, false, false);

    private IndustriaTestWorldLauncher() {
    }

    public static boolean isVanillaTestWorldButton(Button button) {
        return button.getMessage().equals(VANILLA_BUTTON_LABEL);
    }

    public static Button replacementFor(Button original, Screen titleScreen) {
        return createButton(titleScreen, original.getX(), original.getY(), original.getWidth(), original.getHeight());
    }

    public static Button createButton(Screen titleScreen, int x, int y, int width, int height) {
        return Button.builder(INDUSTRIA_BUTTON_LABEL, _ -> open(titleScreen))
                .bounds(x, y, width, height)
                .build();
    }

    public static void open(Screen titleScreen) {
        Minecraft minecraft = Minecraft.getInstance();
        try (LevelStorageSource.LevelStorageAccess access = minecraft.getLevelSource().createAccess(IndustriaTestWorld.WORLD_ID)) {
            if (access.hasWorldData()) {
                minecraft.createWorldOpenFlows().openWorld(IndustriaTestWorld.WORLD_ID, () -> minecraft.setScreen(titleScreen));
                return;
            }
        } catch (IOException exception) {
            SystemToast.onWorldAccessFailure(minecraft, IndustriaTestWorld.WORLD_ID);
            LOGGER.warn("Failed to access Industria test world", exception);
            return;
        }

        minecraft.createWorldOpenFlows().createFreshLevel(
                IndustriaTestWorld.WORLD_ID,
                LEVEL_SETTINGS,
                WORLD_OPTIONS,
                IndustriaTestWorldLauncher::createDimensions,
                titleScreen);
    }

    private static WorldDimensions createDimensions(HolderLookup.Provider registries) {
        var settings = new FlatLevelGeneratorSettings(
                Optional.empty(),
                FlatLevelGeneratorSettings.getDefaultBiome(registries.lookupOrThrow(Registries.BIOME)),
                FlatLevelGeneratorSettings.createLakesList(registries.lookupOrThrow(Registries.PLACED_FEATURE)));
        settings.getLayersInfo().addAll(List.of(
                new FlatLayerInfo(1, Blocks.BEDROCK),
                new FlatLayerInfo(5, Blocks.STONE),
                new FlatLayerInfo(3, Blocks.DIRT),
                new FlatLayerInfo(1, Blocks.GRASS_BLOCK)));
        settings.updateLayers();

        return WorldPresets.createFlatWorldDimensions(registries)
                .replaceOverworldGenerator(registries, new FlatLevelSource(settings));
    }
}
