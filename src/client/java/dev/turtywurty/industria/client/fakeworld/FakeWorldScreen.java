package dev.turtywurty.industria.client.fakeworld;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Simple GUI that draws the client-only world into a boxed area.
 */
public class FakeWorldScreen extends Screen {
    private FakeWorldScene scene;

    public FakeWorldScreen() {
        super(Text.literal("Fake World Preview"));
    }

    @Override
    protected void init() {
        this.scene = new FakeWorldScene();
    }

    @Override
    public void tick() {
        if (this.scene != null) {
            this.scene.tick();
        }
    }

    @Override
    public void close() {
        if (this.scene != null) {
            this.scene.close();
        }
        super.close();
        this.scene = null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.scene != null) {
            int padding = 16;
            int areaX = padding;
            int areaY = padding;
            int areaWidth = this.width - padding * 2;
            int areaHeight = this.height - padding * 2;

            this.scene.render(context, areaX, areaY, areaWidth, areaHeight, delta);
            context.drawBorder(areaX, areaY, areaWidth, areaHeight, 0xAAFFFFFF);
            context.drawTextWithShadow(this.textRenderer, Text.literal("Client-only world preview"), areaX + 4, areaY + 4, 0xFFFFFF);
            context.drawTextWithShadow(this.textRenderer, Text.literal("Press ESC to return"), areaX + 4, areaY + 16, 0xCCCCCC);
        }

        super.render(context, mouseX, mouseY, delta);
    }
}
