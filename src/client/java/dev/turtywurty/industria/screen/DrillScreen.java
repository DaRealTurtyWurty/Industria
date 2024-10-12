package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import dev.turtywurty.industria.network.ChangeDrillOverflowModePayload;
import dev.turtywurty.industria.network.ChangeDrillingPayload;
import dev.turtywurty.industria.network.RetractDrillPayload;
import dev.turtywurty.industria.screen.widget.SelectEnumButton;
import dev.turtywurty.industria.screenhandler.DrillScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class DrillScreen extends HandledScreen<DrillScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/drill.png");


    public DrillScreen(DrillScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addDrawableChild(ButtonWidget.builder(Text.empty(), button ->
                        ClientPlayNetworking.send(new ChangeDrillingPayload(!this.handler.getBlockEntity().isDrilling())))
                .dimensions(this.x + 10, this.y + 16, 20, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.empty(), button -> ClientPlayNetworking.send(new RetractDrillPayload()))
                .dimensions(this.x + 10, this.y + 48, 20, 20)
                .build());

        addDrawableChild(new SelectEnumButton<>(
                this.handler.getBlockEntity().getOverflowMethod(),
                overflowMethod -> {
                    this.handler.getBlockEntity().setOverflowMethod(overflowMethod);
                    ClientPlayNetworking.send(new ChangeDrillOverflowModePayload(overflowMethod));
                },
                3,
                this.x + 40, this.y + 30, 20, 20,
                new HashMap<>() {{
                    for (DrillBlockEntity.OverflowMethod overflowMethod : DrillBlockEntity.OverflowMethod.values()) {
                        put(overflowMethod, Industria.id("textures/gui/widget/overflow_" + overflowMethod.getSerializedName() + ".png"));
                    }
                }}));
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
