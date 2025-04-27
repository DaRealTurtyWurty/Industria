package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screenhandler.SolarPanelScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class SolarPanelScreen extends HandledScreen<SolarPanelScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/solar_panel.png");

    public SolarPanelScreen(SolarPanelScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int energySize = MathHelper.ceil(this.handler.getEnergyPercent() * 66);
        context.fill(this.x + 144, this.y + 10 + 66 - energySize, this.x + 144 + 20, this.y + 10 + 66, 0xFFD4AF37);

        if(this.client == null || this.client.world == null)
            return;

        int energyOutputSize = MathHelper.ceil(this.handler.getEnergyPerTickPercent() * 21);
        ScreenUtils.drawTexture(context, TEXTURE, this.x + 36, this.y + 33 + 21 - energyOutputSize, 176, 21 - energyOutputSize, 21, energyOutputSize);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        if(isPointWithinBounds(144, 10, 20, 66, mouseX, mouseY)) {
            context.drawTooltip(this.textRenderer, Text.literal("Energy: " + this.handler.getEnergy() + " / " + this.handler.getMaxEnergy() + " FE"), mouseX, mouseY);
        }

        if(isPointWithinBounds(36, 33, 21, 21, mouseX, mouseY)) {
            List<Text> tooltip = new ArrayList<>(List.of(
                    Text.literal("Energy Output: %s FE/t".formatted(this.handler.getEnergyPerTick())),
                    Text.literal("Sunlight: %d%%".formatted((int) MathHelper.clamp(this.handler.getEnergyPerTickPercent() * 100, 0, 100)))
            ));


            if(this.client != null && this.client.world != null) {
                World world = this.client.world;

                List<Text> notices = new ArrayList<>();
                if(world.isNight()) {
                    notices.add(Text.literal("⚠ Night").withColor(0xFF5555));
                }

                if(world.isThundering()) {
                    notices.add(Text.literal("⚠ Thundering").withColor(0xFF5555));
                } else if(world.isRaining()) {
                    notices.add(Text.literal("⚠ Raining").withColor(0xFF5555));
                }

                int brightness = world.getLightLevel(LightType.SKY, this.handler.getBlockEntity().getPos().up());
                if(brightness < 15) {
                    notices.add(Text.literal("⚠ Low Light").withColor(0xFF5555));
                }

                if (!notices.isEmpty()) {
                    tooltip.add(Text.empty());
                    tooltip.addAll(notices);
                }
            }

            context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
        }
    }
}
