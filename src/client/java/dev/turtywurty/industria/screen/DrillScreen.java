package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.network.ChangeDrillOverflowModePayload;
import dev.turtywurty.industria.network.ChangeDrillingPayload;
import dev.turtywurty.industria.network.RetractDrillPayload;
import dev.turtywurty.industria.network.SetMotorTargetRPMPayload;
import dev.turtywurty.industria.screen.widget.SelectEnumButton;
import dev.turtywurty.industria.screenhandler.DrillScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import team.reborn.energy.api.EnergyStorage;

import java.util.HashMap;

public class DrillScreen extends HandledScreen<DrillScreenHandler> implements MotorRPMHandler {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/drill.png");

    private ButtonWidget drillButton;
    private ButtonWidget retractButton;
    private SelectEnumButton<DrillBlockEntity.OverflowMethod> overflowButton;
    private TextFieldWidget targetRPMField;

    public DrillScreen(DrillScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

        this.drillButton = addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
                    if (!this.overflowButton.isHoveredLastFrame()) {
                        ClientPlayNetworking.send(new ChangeDrillingPayload(!this.handler.getBlockEntity().isDrilling()));
                    }
                })
                .dimensions(this.x - 30, this.y + 16, 20, 20)
                .build());

        this.retractButton = addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
                    if (!this.overflowButton.isHoveredLastFrame()) {
                        ClientPlayNetworking.send(new RetractDrillPayload());
                    }
                })
                .dimensions(this.x - 30, this.y + 48, 20, 20)
                .build());

        this.overflowButton = addDrawableChild(new SelectEnumButton<>(
                this.handler.getBlockEntity().getOverflowMethod(),
                overflowMethod -> {
                    this.handler.getBlockEntity().setOverflowMethod(overflowMethod);
                    ClientPlayNetworking.send(new ChangeDrillOverflowModePayload(overflowMethod));
                },
                3,
                this.x + 40, this.y + 35, 20, 20,
                Util.make(new HashMap<>(), map -> {
                    for (DrillBlockEntity.OverflowMethod overflowMethod : DrillBlockEntity.OverflowMethod.values()) {
                        map.put(overflowMethod, Industria.id("textures/gui/widget/overflow_" + overflowMethod.getSerializedName() + ".png"));
                    }
                })));

        this.targetRPMField = addDrawableChild(createWidget(this.textRenderer, this.x + 32, this.y + 10, 25, 20));
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();

        boolean hasMotor = this.handler.getBlockEntity().hasMotor();
        if (this.drillButton != null) {
            this.drillButton.active = hasMotor;
        }

        if (this.retractButton != null) {
            this.retractButton.active = hasMotor;
        }

        listenForUpdates();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int energy = MathHelper.ceil(this.handler.getEnergyPercentage() * 66);
        context.fill(this.x + 10, this.y + 10 + 66 - energy, this.x + 10 + 20, this.y + 10 + 66, 0xFFD4AF37);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        int energy = MathHelper.ceil(this.handler.getEnergyPercentage() * 66);
        if (isPointWithinBounds(10, 10 + 66 - energy, 20, energy, mouseX, mouseY)) {
            EnergyStorage energyStorage = this.handler.getBlockEntity().getEnergyStorage();
            context.drawTooltip(this.textRenderer, Text.literal(energyStorage.getAmount() + " / " + energyStorage.getCapacity() + " FE"), mouseX, mouseY);
        }
    }

    @Override
    public void setTargetRPM(int targetRPM) {
        this.handler.getBlockEntity().setTargetRotationSpeed(targetRPM / 60f);
        ClientPlayNetworking.send(new SetMotorTargetRPMPayload(targetRPM));
    }

    @Override
    public int getTargetRPM() {
        return this.handler.getTargetRPM();
    }

    @Override
    public TextFieldWidget getTargetRPMField() {
        return this.targetRPMField;
    }
}
