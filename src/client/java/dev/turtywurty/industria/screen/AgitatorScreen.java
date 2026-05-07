package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screen.widget.GasWidget;
import dev.turtywurty.industria.screen.widget.SelectEnumButton;
import dev.turtywurty.industria.screen.widget.SlurryWidget;
import dev.turtywurty.industria.screen.widget.util.Orientation;
import dev.turtywurty.industria.screenhandler.AgitatorScreenHandler;
import dev.turtywurty.industria.util.AgitatorPortType;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.Map;

public class AgitatorScreen extends AbstractContainerScreen<AgitatorScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/agitator.png");

    @SuppressWarnings("unchecked")
    private final SelectEnumButton<AgitatorPortType>[] inputButtons = new SelectEnumButton[3];
    private final FluidWidget[] inputFluidWidgets = new FluidWidget[3];
    private final GasWidget[] inputGasWidgets = new GasWidget[3];
    private final SlurryWidget[] inputSlurryWidgets = new SlurryWidget[3];
    private final FluidWidget[] outputFluidWidgets = new FluidWidget[2];
    private final GasWidget[] outputGasWidgets = new GasWidget[2];
    private final SlurryWidget[] outputSlurryWidgets = new SlurryWidget[2];

    public AgitatorScreen(AgitatorScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 201);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableOnly(new EnergyWidget.Builder(this.menu.getBlockEntity().getEnergyStorage())
                .bounds(this.leftPos + 8, this.topPos + 94, 160, 10)
                .orientation(Orientation.HORIZONTAL)
                .color(0xFFD4AF37)
                .build());

        for (int index = 0; index < 3; index++) {
            final int portIndex = index;
            int baseX = this.leftPos + 2 + (index * 32);
            int baseY = this.topPos + 8;
            int buttonX = baseX + 10;
            int buttonY = baseY + 12;

            this.inputButtons[index] = addRenderableWidget(new SelectEnumButton<>(
                    this.menu.getInputMode(index),
                    mode -> this.menu.setInputMode(portIndex, mode),
                    2,
                    buttonX, buttonY, 20, 20,
                    Map.of()));

            this.inputFluidWidgets[index] = new FluidWidget.Builder(this.menu.getBlockEntity().getInputFluidStorage(index))
                    .bounds(baseX + 13, buttonY + 21, 14, 47)
                    .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                    .orientation(Orientation.VERTICAL)
                    .build();
            this.inputGasWidgets[index] = new GasWidget.Builder(this.menu.getBlockEntity().getInputGasStorage(index))
                    .bounds(baseX + 13, buttonY + 21, 14, 47)
                    .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                    .orientation(Orientation.VERTICAL)
                    .build();
            this.inputSlurryWidgets[index] = new SlurryWidget.Builder(this.menu.getBlockEntity().getInputSlurryStorage(index))
                    .bounds(baseX + 13, buttonY + 21, 14, 47)
                    .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                    .orientation(Orientation.VERTICAL)
                    .build();
        }

        for (int index = 0; index < 2; index++) {
            int baseX = this.leftPos + 133 + (index * 22);
            int baseY = this.topPos + 41;

            this.outputFluidWidgets[index] = new FluidWidget.Builder(this.menu.getBlockEntity().getOutputFluidStorage(index))
                    .bounds(baseX, baseY, 14, 47)
                    .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                    .orientation(Orientation.VERTICAL)
                    .build();
            this.outputGasWidgets[index] = new GasWidget.Builder(this.menu.getBlockEntity().getOutputGasStorage(index))
                    .bounds(baseX, baseY, 14, 47)
                    .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                    .orientation(Orientation.VERTICAL)
                    .build();
            this.outputSlurryWidgets[index] = new SlurryWidget.Builder(this.menu.getBlockEntity().getOutputSlurryStorage(index))
                    .bounds(baseX, baseY, 14, 47)
                    .posSupplier(this.menu.getBlockEntity()::getBlockPos)
                    .orientation(Orientation.VERTICAL)
                    .build();
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        for (int index = 0; index < this.inputButtons.length; index++) {
            this.inputButtons[index].setValue(this.menu.getInputMode(index));
            this.inputButtons[index].active = this.menu.getBlockEntity().isInputPortEmpty(index);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        drawConditionalItemSlotFrame(context, 0, this.menu.getInputMode(0) == AgitatorPortType.ITEM, 176, 17, 18, 18, -1, -1);
        drawConditionalItemSlotFrame(context, 1, this.menu.getInputMode(1) == AgitatorPortType.ITEM, 176, 17, 18, 18, -1, -1);
        drawConditionalItemSlotFrame(context, 2, this.menu.getInputMode(2) == AgitatorPortType.ITEM, 176, 17, 18, 18, -1, -1);
        drawConditionalItemSlotFrame(context, 3, this.menu.getOutputMode(0) == AgitatorPortType.ITEM, 176, 17, 18, 18, -1, -1);
        drawConditionalItemSlotFrame(context, 4, this.menu.getOutputMode(1) == AgitatorPortType.ITEM, 176, 17, 18, 18, -1, -1);

        renderInputWidget(context, mouseX, mouseY, 0, this.menu.getInputMode(0));
        renderInputWidget(context, mouseX, mouseY, 1, this.menu.getInputMode(1));
        renderInputWidget(context, mouseX, mouseY, 2, this.menu.getInputMode(2));
        renderOutputWidget(context, mouseX, mouseY, 0, this.menu.getOutputMode(0));
        renderOutputWidget(context, mouseX, mouseY, 1, this.menu.getOutputMode(1));

        if (!this.menu.getBlockEntity().isInputPortEmpty(0)) {
            context.text(this.font, "Locked", this.leftPos + 22, this.topPos + 12, 0xFFFFFFFF, false);
        }

        if (!this.menu.getBlockEntity().isInputPortEmpty(1)) {
            context.text(this.font, "Locked", this.leftPos + 82, this.topPos + 12, 0xFFFFFFFF, false);
        }

        if (!this.menu.getBlockEntity().isInputPortEmpty(2)) {
            context.text(this.font, "Locked", this.leftPos + 142, this.topPos + 12, 0xFFFFFFFF, false);
        }

        if (this.menu.getMaxProgress() > 0 && this.menu.getProgress() > 0) {
            ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + 104, this.topPos + 37, 176, 0, (this.menu.getProgress() * 24) / this.menu.getMaxProgress(), 17, 256, 256);
        }
    }

    private void renderInputWidget(GuiGraphicsExtractor context, int mouseX, int mouseY, int index, AgitatorPortType mode) {
        switch (mode) {
            case ITEM -> {
            }
            case FLUID -> {
                drawWidgetFrame(context, this.inputFluidWidgets[index].getX(), this.inputFluidWidgets[index].getY(), 176, 35, 14, 47);
                this.inputFluidWidgets[index].extractRenderState(context, mouseX, mouseY, 0.0F);
            }
            case GAS -> {
                drawWidgetFrame(context, this.inputGasWidgets[index].getX(), this.inputGasWidgets[index].getY(), 176, 35, 14, 47);
                this.inputGasWidgets[index].extractRenderState(context, mouseX, mouseY, 0.0F);
            }
            case SLURRY -> {
                drawWidgetFrame(context, this.inputSlurryWidgets[index].getX(), this.inputSlurryWidgets[index].getY(), 176, 35, 14, 47);
                this.inputSlurryWidgets[index].extractRenderState(context, mouseX, mouseY, 0.0F);
            }
        }
    }

    private void renderOutputWidget(GuiGraphicsExtractor context, int mouseX, int mouseY, int index, AgitatorPortType mode) {
        switch (mode) {
            case ITEM -> {
            }
            case FLUID -> {
                drawWidgetFrame(context, this.outputFluidWidgets[index].getX(), this.outputFluidWidgets[index].getY(), 176, 35, 14, 47);
                this.outputFluidWidgets[index].extractRenderState(context, mouseX, mouseY, 0.0F);
            }
            case GAS -> {
                drawWidgetFrame(context, this.outputGasWidgets[index].getX(), this.outputGasWidgets[index].getY(), 176, 35, 14, 47);
                this.outputGasWidgets[index].extractRenderState(context, mouseX, mouseY, 0.0F);
            }
            case SLURRY -> {
                drawWidgetFrame(context, this.outputSlurryWidgets[index].getX(), this.outputSlurryWidgets[index].getY(), 176, 35, 14, 47);
                this.outputSlurryWidgets[index].extractRenderState(context, mouseX, mouseY, 0.0F);
            }
        }
    }

    private void drawConditionalItemSlotFrame(GuiGraphicsExtractor context, int slotIndex, boolean active, int u, int v, int width, int height, int offsetX, int offsetY) {
        if (!active)
            return;

        Slot slot = this.menu.slots.get(slotIndex);
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos + slot.x + offsetX, this.topPos + slot.y + offsetY, u, v, width, height);
    }

    private void drawWidgetFrame(GuiGraphicsExtractor context, int x, int y, int u, int v, int width, int height) {
        ScreenUtils.drawTexture(context, TEXTURE, x, y, u, v, width, height);
    }
}
