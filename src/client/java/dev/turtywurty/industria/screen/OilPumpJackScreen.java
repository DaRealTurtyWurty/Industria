package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.network.OilPumpJackSetRunningPayload;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.ToggleButton;
import dev.turtywurty.industria.screenhandler.OilPumpJackScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class OilPumpJackScreen extends AbstractContainerScreen<OilPumpJackScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/oil_pump_jack.png");

    public OilPumpJackScreen(OilPumpJackScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        addRenderableOnly(new EnergyWidget.Builder(this.menu.getBlockEntity().getEnergyStorage())
                .bounds(this.leftPos + 8, this.topPos + 16, 16, 54)
                .color(0xFFD4AF37)
                .build());

        addRenderableWidget(new ToggleButton.Builder(this.leftPos + 32, this.topPos + 16)
                .onPress(($, toggled) ->
                        ClientPlayNetworking.send(new OilPumpJackSetRunningPayload(toggled)))
                .textures(ToggleButton.DEFAULT_COLOURED_TEXTURES)
                .toggledByDefault(this.menu.getBlockEntity().isRunning())
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }
}
