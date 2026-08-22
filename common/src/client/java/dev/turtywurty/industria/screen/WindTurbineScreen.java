package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.WindTurbineBlockEntity;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.List;

public class WindTurbineScreen extends Screen {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/wind_turbine.png");
    private static final int BACKGROUND_WIDTH = 176;
    private static final int BACKGROUND_HEIGHT = 90;

    private final WindTurbineBlockEntity blockEntity;
    private int leftPos, topPos;

    public WindTurbineScreen(WindTurbineBlockEntity blockEntity) {
        super(WindTurbineBlockEntity.TITLE);
        this.blockEntity = blockEntity;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - BACKGROUND_WIDTH) / 2;
        this.topPos = (this.height - BACKGROUND_HEIGHT) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        int energySize = Mth.ceil(getEnergyPercent() * 66);
        context.fill(this.leftPos + 144, this.topPos + 10 + 66 - energySize, this.leftPos + 164, this.topPos + 76, 0xFFD4AF37);

        int energyOutputSize = Mth.ceil(getEnergyPerTickPercent() * 21);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + 36, this.topPos + 33, 176, 0, energyOutputSize, 21);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.text(this.font, this.title, this.leftPos + (BACKGROUND_WIDTH - this.font.width(this.title)) / 2, this.topPos + 6, 0xFF404040, false);
        extractTooltips(context, mouseX, mouseY);
    }

    private void extractTooltips(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        if (isHovering(144, 10, 20, 66, mouseX, mouseY)) {
            context.setTooltipForNextFrame(this.font, Component.literal("Energy: " + getEnergy() + " / " + getMaxEnergy() + " FE"), mouseX, mouseY);
        }

        if (isHovering(36, 33, 21, 21, mouseX, mouseY)) {
            context.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.literal("Energy Output: %s FE/t".formatted(getEnergyPerTick())),
                    Component.literal("Wind Capture: %d%%".formatted((int) Mth.clamp(getEnergyPerTickPercent() * 100, 0, 100)))
            ), mouseX, mouseY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= this.leftPos + x && mouseX < this.leftPos + x + width
                && mouseY >= this.topPos + y && mouseY < this.topPos + y + height;
    }

    private long getEnergy() {
        return this.blockEntity.getEnergyStorage().getAmount();
    }

    private long getMaxEnergy() {
        return this.blockEntity.getEnergyStorage().getCapacity();
    }

    private float getEnergyPercent() {
        long energy = getEnergy();
        long maxEnergy = getMaxEnergy();
        if (maxEnergy == 0 || energy == 0)
            return 0.0F;

        return Mth.clamp((float) energy / (float) maxEnergy, 0.0F, 1.0F);
    }

    private int getEnergyPerTick() {
        return this.blockEntity.getEnergyOutput();
    }

    private float getEnergyPerTickPercent() {
        int output = getEnergyPerTick();
        if (output == 0)
            return 0.0F;

        return Mth.clamp((float) output / 500.0F, 0.0F, 1.0F);
    }
}
