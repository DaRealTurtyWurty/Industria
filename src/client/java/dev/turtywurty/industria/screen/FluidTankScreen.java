package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.network.FluidTankChangeExtractModePayload;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screenhandler.FluidTankScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class FluidTankScreen extends AbstractContainerScreen<FluidTankScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/fluid_tank.png");

    private CycleButton<Boolean> toggleButton;

    public FluidTankScreen(FluidTankScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 174);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = this.imageHeight - 94;

        addRenderableOnly(new FluidWidget.Builder(this.menu.getBlockEntity().getFluidTank())
                .bounds(this.leftPos + 79, this.topPos + 14, 20, 46)
                .posSupplier(() -> this.menu.getBlockEntity().getBlockPos())
                .build());

        this.toggleButton = addRenderableWidget(CycleButton.onOffBuilder(this.menu.getBlockEntity().isExtractMode())
                .displayOnlyValue()
                .withSprite((button, value) -> value ? Identifier.withDefaultNamespace("widget/locked_button") : Identifier.withDefaultNamespace("widget/unlocked_button"))
                .create(this.leftPos + 8, this.topPos + 14, 20, 20, Component.empty(),
                        (button, value) ->
                                ClientPlayNetworking.send(new FluidTankChangeExtractModePayload(!value))));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.toggleButton != null)
            this.toggleButton.setValue(this.menu.getBlockEntity().isExtractMode());
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
