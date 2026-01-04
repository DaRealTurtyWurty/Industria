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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import team.reborn.energy.api.EnergyStorage;

import java.util.HashMap;

public class DrillScreen extends AbstractContainerScreen<DrillScreenHandler> implements MotorRPMHandler {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/drill.png");

    private Button drillButton;
    private Button retractButton;
    private SelectEnumButton<DrillBlockEntity.OverflowMethod> overflowButton;
    private EditBox targetRPMField;

    public DrillScreen(DrillScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        this.drillButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                    if (!this.overflowButton.isHoveredLastFrame()) {
                        ClientPlayNetworking.send(new ChangeDrillingPayload(!this.menu.getBlockEntity().isDrilling()));
                    }
                })
                .bounds(this.leftPos - 30, this.topPos + 16, 20, 20)
                .build());

        this.retractButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                    if (!this.overflowButton.isHoveredLastFrame()) {
                        ClientPlayNetworking.send(new RetractDrillPayload());
                    }
                })
                .bounds(this.leftPos - 30, this.topPos + 48, 20, 20)
                .build());

        this.overflowButton = addRenderableWidget(new SelectEnumButton<>(
                this.menu.getBlockEntity().getOverflowMethod(),
                overflowMethod -> {
                    this.menu.getBlockEntity().setOverflowMethod(overflowMethod);
                    ClientPlayNetworking.send(new ChangeDrillOverflowModePayload(overflowMethod));
                },
                3,
                this.leftPos + 40, this.topPos + 35, 20, 20,
                Util.make(new HashMap<>(), map -> {
                    for (DrillBlockEntity.OverflowMethod overflowMethod : DrillBlockEntity.OverflowMethod.values()) {
                        map.put(overflowMethod, Industria.id("textures/gui/widget/overflow_" + overflowMethod.getSerializedName() + ".png"));
                    }
                })));

        this.targetRPMField = addRenderableWidget(createWidget(this.font, this.leftPos + 32, this.topPos + 10, 25, 20));
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        boolean hasMotor = this.menu.getBlockEntity().hasMotor();
        if (this.drillButton != null) {
            this.drillButton.active = hasMotor;
        }

        if (this.retractButton != null) {
            this.retractButton.active = hasMotor;
        }

        listenForUpdates();
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int energy = Mth.ceil(this.menu.getEnergyPercentage() * 66);
        context.fill(this.leftPos + 10, this.topPos + 10 + 66 - energy, this.leftPos + 10 + 20, this.topPos + 10 + 66, 0xFFD4AF37);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);

        int energy = Mth.ceil(this.menu.getEnergyPercentage() * 66);
        if (isHovering(10, 10 + 66 - energy, 20, energy, mouseX, mouseY)) {
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
