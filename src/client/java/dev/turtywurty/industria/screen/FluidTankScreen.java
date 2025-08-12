package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.network.FluidTankChangeExtractModePayload;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screenhandler.FluidTankScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FluidTankScreen extends HandledScreen<FluidTankScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/fluid_tank.png");

    private ToggleButtonWidget toggleButton;

    public FluidTankScreen(FluidTankScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 174;
    }

    @Override
    protected void init() {
        super.init();
        this.playerInventoryTitleY = this.backgroundHeight - 94;

        addDrawable(new FluidWidget.Builder(this.handler.getBlockEntity().getFluidTank())
                .bounds(this.x + 79, this.y + 14, 20, 46)
                .posSupplier(() -> this.handler.getBlockEntity().getPos())
                .build());

        this.toggleButton = addDrawableChild(new ToggleButtonWidget(this.x + 8, this.y + 14, 20, 20, this.handler.getBlockEntity().isExtractMode()) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                boolean mouseClicked = super.mouseClicked(mouseX, mouseY, button);
                if (mouseClicked) {
                    this.toggled = !this.toggled;
                }

                return mouseClicked;
            }

            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                ClientPlayNetworking.send(new FluidTankChangeExtractModePayload(!this.toggled));
            }

            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                if (this.toggled) {
                    ScreenUtils.drawTexture(context, TEXTURE, getX(), getY(), 176, 0, 20, 20);
                } else {
                    ScreenUtils.drawTexture(context, TEXTURE, getX(), getY(), 176, 20, 20, 20);
                }
            }
        });
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        if (this.toggleButton != null)
            this.toggleButton.setToggled(this.handler.getBlockEntity().isExtractMode());
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
