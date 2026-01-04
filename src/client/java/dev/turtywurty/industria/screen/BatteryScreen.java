package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.BatteryBlockEntity;
import dev.turtywurty.industria.network.BatteryChargeModePayload;
import dev.turtywurty.industria.screenhandler.BatteryScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class BatteryScreen extends AbstractContainerScreen<BatteryScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/battery.png");

    public BatteryScreen(BatteryScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        BatteryBlockEntity blockEntity = this.menu.getBlockEntity();

        addRenderableWidget(CycleButton.onOffBuilder(blockEntity.getChargeMode() == BatteryBlockEntity.ChargeMode.CHARGE)
                .displayOnlyValue()
                .create(this.leftPos + 144, this.topPos + 10, 20, 20, BatteryBlockEntity.CHARGE_MODE_BUTTON_TOOLTIP_TEXT,
                        (button, value) ->
                                ClientPlayNetworking.send(new BatteryChargeModePayload(BatteryScreen.this.menu.getChargeMode().next()))));
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int energy = Mth.ceil(this.menu.getEnergyPercent() * 66);
        context.fill(this.leftPos + 144, this.topPos + 10 + 66 - energy, this.leftPos + 144 + 20, this.topPos + 10 + 66, 0xFFD4AF37);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);

        int energy = Mth.ceil(this.menu.getEnergyPercent() * 66);
        if (isHovering(144, 10 + 66 - energy, 20, energy, mouseX, mouseY)) {
            context.setTooltipForNextFrame(this.font, Component.literal(this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"), mouseX, mouseY);
        }
    }
}
