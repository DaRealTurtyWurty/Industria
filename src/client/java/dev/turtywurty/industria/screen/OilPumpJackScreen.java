package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.network.OilPumpJackSetRunningPayload;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.ToggleButton;
import dev.turtywurty.industria.screenhandler.OilPumpJackScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class OilPumpJackScreen extends HandledScreen<OilPumpJackScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/oil_pump_jack.png");

    public OilPumpJackScreen(OilPumpJackScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        addDrawable(new EnergyWidget.Builder(this.handler.getBlockEntity().getEnergyStorage())
                .bounds(this.x + 8, this.y + 16, 16, 54)
                .color(0xFFD4AF37)
                .build());

        addDrawableChild(new ToggleButton.Builder(this.x + 32, this.y + 16)
                .onPress(($, toggled) ->
                        ClientPlayNetworking.send(new OilPumpJackSetRunningPayload(toggled)))
                .textures(ToggleButton.DEFAULT_COLOURED_TEXTURES)
                .toggledByDefault(this.handler.getBlockEntity().isRunning())
                .build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
