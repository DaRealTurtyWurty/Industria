package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.ArcFurnaceBlockEntity;
import dev.turtywurty.industria.network.ArcFurnaceSetModePayload;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screen.widget.GasWidget;
import dev.turtywurty.industria.screen.widget.SelectEnumButton;
import dev.turtywurty.industria.screen.widget.util.Orientation;
import dev.turtywurty.industria.screenhandler.ArcFurnaceScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.Map;

public class ArcFurnaceScreen extends AbstractContainerScreen<ArcFurnaceScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/arc_furnace.png");

    private SelectEnumButton<ArcFurnaceBlockEntity.Mode> modeButton;

    public ArcFurnaceScreen(ArcFurnaceScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 201);

        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        addRenderableOnly(new EnergyWidget.Builder(this.menu.getBlockEntity().getEnergyStorage())
                .bounds(this.leftPos + 8, this.topPos + 18, 12, 52)
                .build());

        addRenderableOnly(new FluidWidget.Builder(this.menu.getBlockEntity().getFluidStorage())
                .bounds(this.leftPos + 26, this.topPos + 94, 138, 10)
                .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                .orientation(Orientation.HORIZONTAL)
                .build());

        addRenderableOnly(new GasWidget.Builder(this.menu.getBlockEntity().getGasStorage())
                .bounds(this.leftPos + 26, this.topPos + 80, 138, 10)
                .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                .orientation(Orientation.HORIZONTAL)
                .build());

        this.modeButton = addRenderableWidget(new SelectEnumButton<>(
                this.menu.getMode(),
                mode -> ClientPlayNetworking.send(new ArcFurnaceSetModePayload(mode)),
                ArcFurnaceBlockEntity.Mode.values().length,
                this.leftPos + 78, this.topPos + 82, 20, 20,
                Map.of()));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int temperature = this.menu.getTemperature();
        int temperatureColor = getTemperatureColor(temperature);
        int textX = this.leftPos + 8;
        int textY = this.topPos + 6;
        context.text(this.font, temperature + " C", textX, textY, temperatureColor, false);

        boolean alloying = this.menu.getMode() == ArcFurnaceBlockEntity.Mode.ALLOYING;
        float sharedProgress = alloying ? this.menu.getProgressPercent(0) : 0.0F;
        for (int index = 0; index < 9; index++) {
            Slot slot = this.menu.slots.get(index);
            if(!slot.hasItem())
                continue;

            float progress = alloying ? sharedProgress : this.menu.getProgressPercent(index);

            int x0 = this.leftPos + slot.x;
            int y0 = this.topPos + slot.y + 14;
            int x1 = x0 + (int) (16 * progress);
            int y1 = y0 + 2;

            context.fill(x0, y0, x1, y1, getProgressColor(progress));
        }
    }

    private static int getProgressColor(float progress) {
        progress = Math.clamp(progress, 0.0F, 1.0F);

        int red = (int) ((1.0F - progress) * 255.0F);
        int green = (int) (progress * 255.0F);

        return 0xFF000000
                | (red << 16)
                | (green << 8);
    }

    private static int getTemperatureColor(int temperature) {
        float progress = Mth.clamp(temperature / 250.0F, 0.0F, 1.0F);
        int red = 255;
        int green = (int) (255.0F * (1.0F - progress));

        return 0xFF000000
                | (red << 16)
                | (green << 8);
    }
}
