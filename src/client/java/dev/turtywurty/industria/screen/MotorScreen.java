package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.network.SetMotorTargetRPMPayload;
import dev.turtywurty.industria.screenhandler.MotorScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import team.reborn.energy.api.EnergyStorage;

public class MotorScreen extends HandledScreen<MotorScreenHandler> implements MotorRPMHandler {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/motor.png");

    private TextFieldWidget targetRPMField;

    public MotorScreen(MotorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 86;
        this.playerInventoryTitleY = Integer.MAX_VALUE;
    }

    @Override
    protected void init() {
        super.init();

        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
        this.targetRPMField = addDrawableChild(createWidget(this.textRenderer, this.x + 10, this.y + 10, 100, 20));
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();

        listenForUpdates();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int energy = MathHelper.ceil(this.handler.getEnergyPercentage() * 66);
        context.fill(this.x + 144, this.y + 10 + 66 - energy, this.x + 144 + 20, this.y + 10 + 66, 0xFFD4AF37);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int energy = MathHelper.ceil(this.handler.getEnergyPercentage() * 66);
        if (isPointWithinBounds(144, 10 + 66 - energy, 20, energy, mouseX, mouseY)) {
            EnergyStorage energyStorage = this.handler.getBlockEntity().getEnergyStorage();
            context.drawTooltip(this.textRenderer, Text.literal(energyStorage.getAmount() + " / " + energyStorage.getCapacity() + " FE"), mouseX, mouseY);
        }
    }

    @Override
    public int getTargetRPM() {
        return this.handler.getTargetRPM();
    }

    @Override
    public void setTargetRPM(int targetRPM) {
        this.handler.getBlockEntity().setTargetRotationSpeed(targetRPM / 60f);
        ClientPlayNetworking.send(new SetMotorTargetRPMPayload(targetRPM));
    }

    @Override
    public TextFieldWidget getTargetRPMField() {
        return this.targetRPMField;
    }
}
