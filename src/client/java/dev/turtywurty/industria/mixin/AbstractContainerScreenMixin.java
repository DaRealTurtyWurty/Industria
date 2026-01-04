package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.util.DebugRenderingRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
    private AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;isFake()Z"))
    private void industria$drawSlot(GuiGraphics context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (DebugRenderingRegistry.debugRendering) {
            String text = String.valueOf(slot.index);
            context.drawString(this.font,
                    text,
                    slot.x + (this.font.width(text) / 2),
                    slot.y + (this.font.lineHeight / 2),
                    0x101010,
                    false);
        }
    }
}
