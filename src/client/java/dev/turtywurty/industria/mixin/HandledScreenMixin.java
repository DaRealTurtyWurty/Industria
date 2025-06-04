package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.util.DebugRenderingRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    private HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;disablesDynamicDisplay()Z"))
    private void industria$drawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        if(DebugRenderingRegistry.debugRendering) {
            String text = String.valueOf(slot.id);
            context.drawText(this.textRenderer,
                    text,
                    slot.x + (this.textRenderer.getWidth(text) / 2),
                    slot.y + (this.textRenderer.fontHeight / 2),
                    0x101010,
                    false);
        }
    }
}
