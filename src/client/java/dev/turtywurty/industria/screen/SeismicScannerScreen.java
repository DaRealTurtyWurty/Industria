package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SeismicScannerScreen extends Screen {
    private static final Text TITLE = Text.translatable("screen." + Industria.MOD_ID + ".seismic_scanner");
    private static final Identifier TEXTURE = Industria.id("textures/gui/seismic_scanner.png");

    private final int backgroundWidth = 176;
    private final int backgroundHeight = 166;
    private int x, y;

    private boolean isOilBelow = false;

    private final ItemStack stack;

    public SeismicScannerScreen(ItemStack stack) {
        super(TITLE);

        this.stack = stack;
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, this.title, this.width / 2, 8, 0x404040, false);

        if (this.isOilBelow) {
            context.drawText(this.textRenderer, Text.literal("Oil is below you!"), this.x + 8, this.y + 8, 0x404040, false);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.client != null && this.client.player != null) {
            ItemStack stack = this.client.player.getMainHandStack();
            if (!stack.isOf(this.stack.getItem())) {
                close();
            }
        } else {
            close();
        }
    }
}
