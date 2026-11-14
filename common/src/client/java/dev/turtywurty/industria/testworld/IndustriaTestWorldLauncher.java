package dev.turtywurty.industria.testworld;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
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
import org.jetbrains.annotations.Nullable;

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

    public static boolean isIndustriaTestWorldButton(Button button) {
        return button.getMessage().equals(INDUSTRIA_BUTTON_LABEL);
    }

    public static @Nullable Button createFallbackButton(Screen screen, List<? extends AbstractWidget> widgets) {
        if (widgets.stream().filter(Button.class::isInstance).map(Button.class::cast)
            .anyMatch(IndustriaTestWorldLauncher::isIndustriaTestWorldButton))
            return null;

        Button singleplayer = findButton(widgets, "menu.singleplayer");
        Button realms = findButton(widgets, "menu.online");
        if (singleplayer == null || realms == null)
            return null;

        int firstRowY = singleplayer.getY();
        int lastRowY = realms.getY();
        var menuWidgets = widgets.stream()
            .filter(widget -> widget.getHeight() >= 20 && widget.getY() >= firstRowY)
            .toList();
        var rows = menuWidgets.stream().map(AbstractWidget::getY).distinct().sorted().toList();
        int insertedRow = rows.indexOf(lastRowY) + 1;
        int rowCount = rows.size() + 1;
        // Keep the menu below the logo and above the footer, tightening gaps on small screens.
        int bottom = screen.height - 16;
        int top = Math.max(89, Math.min(firstRowY, bottom - 20 - 24 * (rowCount - 1)));
        int spacing = Math.min(24, (bottom - top - 20) / (rowCount - 1));
        for (AbstractWidget widget : menuWidgets) {
            int row = rows.indexOf(widget.getY());
            if (row >= insertedRow) {
                row++;
            }
            widget.setY(top + row * spacing);
        }
        return createButton(screen, singleplayer.getX(), top + insertedRow * spacing,
            singleplayer.getWidth(), singleplayer.getHeight());
    }

    private static @Nullable Button findButton(List<? extends AbstractWidget> widgets, String translationKey) {
        Component label = Component.translatable(translationKey);
        return widgets.stream().filter(Button.class::isInstance).map(Button.class::cast)
            .filter(button -> button.getMessage().equals(label)).findFirst().orElse(null);
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
        try (LevelStorageSource.LevelStorageAccess access = minecraft.getLevelSource()
            .createAccess(IndustriaTestWorld.WORLD_ID)) {
            access.deleteLevel();
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

        return WorldPresets.createNormalWorldDimensions(registries)
            .replaceOverworldGenerator(registries, new FlatLevelSource(settings));
    }
}
