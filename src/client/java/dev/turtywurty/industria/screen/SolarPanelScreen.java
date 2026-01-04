package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screenhandler.SolarPanelScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import java.util.ArrayList;
import java.util.List;

public class SolarPanelScreen extends AbstractContainerScreen<SolarPanelScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/solar_panel.png");

    public SolarPanelScreen(SolarPanelScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int energySize = Mth.ceil(this.menu.getEnergyPercent() * 66);
        context.fill(this.leftPos + 144, this.topPos + 10 + 66 - energySize, this.leftPos + 144 + 20, this.topPos + 10 + 66, 0xFFD4AF37);

        if(this.minecraft == null || this.minecraft.level == null)
            return;

        int energyOutputSize = Mth.ceil(this.menu.getEnergyPerTickPercent() * 21);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + 36, this.topPos + 33 + 21 - energyOutputSize, 176, 21 - energyOutputSize, 21, energyOutputSize);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);

        if(isHovering(144, 10, 20, 66, mouseX, mouseY)) {
            context.setTooltipForNextFrame(this.font, Component.literal("Energy: " + this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"), mouseX, mouseY);
        }

        if(isHovering(36, 33, 21, 21, mouseX, mouseY)) {
            List<Component> tooltip = new ArrayList<>(List.of(
                    Component.literal("Energy Output: %s FE/t".formatted(this.menu.getEnergyPerTick())),
                    Component.literal("Sunlight: %d%%".formatted((int) Mth.clamp(this.menu.getEnergyPerTickPercent() * 100, 0, 100)))
            ));


            if(this.minecraft != null && this.minecraft.level != null) {
                Level world = this.minecraft.level;

                List<Component> notices = new ArrayList<>();
                if(world.isDarkOutside()) {
                    notices.add(Component.literal("⚠ Night").withColor(0xFF5555));
                }

                if(world.isThundering()) {
                    notices.add(Component.literal("⚠ Thundering").withColor(0xFF5555));
                } else if(world.isRaining()) {
                    notices.add(Component.literal("⚠ Raining").withColor(0xFF5555));
                }

                int brightness = world.getBrightness(LightLayer.SKY, this.menu.getBlockEntity().getBlockPos().above());
                if(brightness < 15) {
                    notices.add(Component.literal("⚠ Low Light").withColor(0xFF5555));
                }

                if (!notices.isEmpty()) {
                    tooltip.add(Component.empty());
                    tooltip.addAll(notices);
                }
            }

            context.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        }
    }
}
