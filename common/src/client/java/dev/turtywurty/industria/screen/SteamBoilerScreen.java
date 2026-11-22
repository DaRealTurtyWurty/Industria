package dev.turtywurty.industria.screen;

import dev.turtywurty.gasapi.client.GasRenderHandlerRegistry;
import dev.turtywurty.gasapi.client.SimpleGasRenderHandler;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.ModGases;
import dev.turtywurty.industria.menu.SteamBoilerMenu;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screen.widget.GasWidget;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Locale;
import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.industria.blockentity.SteamBoilerBlockEntity;
import net.minecraft.world.inventory.Slot;

public class SteamBoilerScreen extends AbstractContainerScreen<SteamBoilerMenu> {
    private static final String TRANSLATION_PREFIX = "container." + Industria.MOD_ID + ".steam_boiler.";
    public static final String BURN_RATE_KEY = TRANSLATION_PREFIX + "burn_rate";
    public static final String FUEL_KEY = TRANSLATION_PREFIX + "fuel";
    public static final String PRODUCTION_RATE_KEY = TRANSLATION_PREFIX + "production_rate";
    public static final String STEAM_KEY = TRANSLATION_PREFIX + "steam";
    public static final String TANK_CONTENTS_KEY = TRANSLATION_PREFIX + "tank_contents";
    public static final String TARGET_TEMPERATURE_KEY = TRANSLATION_PREFIX + "target";
    public static final String TEMPERATURE_KEY = TRANSLATION_PREFIX + "temperature";
    public static final String WATER_KEY = TRANSLATION_PREFIX + "water";

    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final int WATER_X = 20;
    private static final int STEAM_X = 136;
    private static final int TANK_Y = 30;
    private static final int TANK_WIDTH = 20;
    private static final int TANK_HEIGHT = 22;

    private static final Component WATER = Component.translatable(WATER_KEY);
    private static final Component FUEL = Component.translatable(FUEL_KEY);
    private static final Component STEAM = Component.translatable(STEAM_KEY);

    public SteamBoilerScreen(SteamBoilerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.inventoryLabelY = 74;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        Gas steam = ModGases.STEAM.get();
        if (GasRenderHandlerRegistry.get(steam) == null) {
            GasRenderHandlerRegistry.register(steam, new SimpleGasRenderHandler(0xFFD7E4EA));
        }

        SteamBoilerBlockEntity boiler = this.menu.getBlockEntity();
        addRenderableOnly(new FluidWidget.Builder(boiler.getWaterStorage())
            .bounds(this.leftPos + WATER_X, this.topPos + TANK_Y, TANK_WIDTH, TANK_HEIGHT)
            .posSupplier(boiler::getBlockPos)
            .build());
        addRenderableOnly(new GasWidget.Builder(boiler.getSteamStorage())
            .bounds(this.leftPos + STEAM_X, this.topPos + TANK_Y, TANK_WIDTH, TANK_HEIGHT)
            .posSupplier(boiler::getBlockPos)
            .build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        ScreenUtils.drawTexture(graphics, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth,
            this.imageHeight);
        graphics.fill(this.leftPos + 7, this.topPos + 16, this.leftPos + 169, this.topPos + 75, 0xFFC6C6C6);

        Slot fuelSlot = this.menu.getSlot(0);
        int fuelX = this.leftPos + fuelSlot.x;
        int fuelY = this.topPos + fuelSlot.y;
        ScreenUtils.drawTexture(graphics, BACKGROUND, fuelX - 1, fuelY - 1, 55, 16, 18, 18);
        int burnY = fuelY + 19;
        graphics.fill(fuelX, burnY, fuelX + 16, burnY + 3, 0xFF626262);
        int burnWidth = Mth.ceil(this.menu.getBurnProgress() * 16);
        if (burnWidth > 0) {
            graphics.fill(fuelX, burnY, fuelX + burnWidth, burnY + 3, 0xFFE6882F);
        }

        drawTankBackground(graphics, WATER_X);
        drawTankBackground(graphics, STEAM_X);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        drawLabel(graphics, WATER, WATER_X + TANK_WIDTH / 2, 21);
        drawLabel(graphics, FUEL, this.imageWidth / 2, 21);
        drawLabel(graphics, STEAM, STEAM_X + TANK_WIDTH / 2, 21);
        drawLabel(graphics, Component.translatable(TEMPERATURE_KEY, this.menu.getTemperature()), 48, 54);
        drawLabel(graphics, Component.translatable(TARGET_TEMPERATURE_KEY, this.menu.getTargetTemperature()), 128, 54);
        drawLabel(graphics, Component.translatable(PRODUCTION_RATE_KEY,
            formatRate(this.menu.getSteamProductionRate())), 48, 64);
        drawLabel(graphics, Component.translatable(BURN_RATE_KEY,
            formatRate(this.menu.getBurnRate())), 128, 64);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (isHovering(WATER_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
            drawTankTooltip(graphics, WATER, this.menu.getWaterAmount(), this.menu.getWaterCapacity(), mouseX, mouseY);
        } else if (isHovering(STEAM_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT, mouseX, mouseY)) {
            drawTankTooltip(graphics, STEAM, this.menu.getSteamAmount(), this.menu.getSteamCapacity(), mouseX, mouseY);
        }
    }

    private void drawTankBackground(GuiGraphicsExtractor graphics, int tankX) {
        int x = this.leftPos + tankX;
        int y = this.topPos + TANK_Y;
        int bottom = y + TANK_HEIGHT;
        graphics.fill(x - 1, y - 1, x + TANK_WIDTH + 1, bottom + 1, 0xFF626262);
        graphics.fill(x, y, x + TANK_WIDTH, bottom, 0xFF313131);
    }

    private void drawLabel(GuiGraphicsExtractor graphics, Component text, int centerX, int y) {
        graphics.text(this.font, text, centerX - this.font.width(text) / 2, y, 0xFF404040, false);
    }

    private void drawTankTooltip(
        GuiGraphicsExtractor graphics,
        Component tank,
        int amount,
        int capacity,
        int mouseX,
        int mouseY
    ) {
        Component contents = Component.translatable(TANK_CONTENTS_KEY, amount, capacity);
        graphics.setComponentTooltipForNextFrame(this.font, List.of(tank, contents), mouseX, mouseY);
    }

    private static String formatRate(float rate) {
        return String.format(Locale.ROOT, "%.2f", rate);
    }
}
