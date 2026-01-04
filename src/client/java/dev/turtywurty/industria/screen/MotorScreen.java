package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.network.SetMotorTargetRPMPayload;
import dev.turtywurty.industria.screenhandler.MotorScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import team.reborn.energy.api.EnergyStorage;

public class MotorScreen extends AbstractContainerScreen<MotorScreenHandler> implements MotorRPMHandler {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/motor.png");

    private EditBox targetRPMField;

    public MotorScreen(MotorScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 176, 86);
        this.inventoryLabelY = Integer.MAX_VALUE;
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.targetRPMField = addRenderableWidget(createWidget(this.font, this.leftPos + 10, this.topPos + 10, 100, 20));
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        listenForUpdates();
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int energy = Mth.ceil(this.menu.getEnergyPercentage() * 66);
        context.fill(this.leftPos + 144, this.topPos + 10 + 66 - energy, this.leftPos + 144 + 20, this.topPos + 10 + 66, 0xFFD4AF37);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int energy = Mth.ceil(this.menu.getEnergyPercentage() * 66);
        if (isHovering(144, 10 + 66 - energy, 20, energy, mouseX, mouseY)) {
            EnergyStorage energyStorage = this.menu.getBlockEntity().getEnergyStorage();
            context.setTooltipForNextFrame(this.font, Component.literal(energyStorage.getAmount() + " / " + energyStorage.getCapacity() + " FE"), mouseX, mouseY);
        }
    }

    @Override
    public void setTargetRPM(int targetRPM) {
        this.menu.getBlockEntity().setTargetRotationSpeed(targetRPM / 60f);
        ClientPlayNetworking.send(new SetMotorTargetRPMPayload(targetRPM));
    }

    @Override
    public int getTargetRPM() {
        return this.menu.getTargetRPM();
    }

    @Override
    public EditBox getTargetRPMField() {
        return this.targetRPMField;
    }
}
