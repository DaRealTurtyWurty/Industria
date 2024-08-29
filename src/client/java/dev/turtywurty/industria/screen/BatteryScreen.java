package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.BatteryBlockEntity;
import dev.turtywurty.industria.network.BatteryChargeModePayload;
import dev.turtywurty.industria.screenhandler.BatteryScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class BatteryScreen extends HandledScreen<BatteryScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/battery.png");

    public BatteryScreen(BatteryScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

        BatteryBlockEntity blockEntity = this.handler.getBlockEntity();

        var toggle = addDrawableChild(new ToggleButtonWidget(this.x + 144, this.y + 10, 20, 20,
                blockEntity.getChargeMode() == BatteryBlockEntity.ChargeMode.CHARGE) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                ClientPlayNetworking.send(new BatteryChargeModePayload(BatteryScreen.this.handler.getChargeMode().next()));
            }
        });

        toggle.setTooltip(Tooltip.of(BatteryBlockEntity.CHARGE_MODE_BUTTON_TOOLTIP_TEXT));
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int energy = MathHelper.ceil(this.handler.getEnergyPercent() * 66);
        context.fill(this.x + 144, this.y + 10 + 66 - energy, this.x + 144 + 20, this.y + 10 + 66, 0xFFD4AF37);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        int energy = MathHelper.ceil(this.handler.getEnergyPercent() * 66);
        if (isPointWithinBounds(144, 10 + 66 - energy, 20, energy, mouseX, mouseY)) {
            context.drawTooltip(this.textRenderer, Text.literal(this.handler.getEnergy() + " / " + this.handler.getMaxEnergy() + " FE"), mouseX, mouseY);
        }
    }
}
