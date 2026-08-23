package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.init.ModAttachmentTypes;
import dev.turtywurty.turtymultiloader.attachment.AttachmentService;
import dev.turtywurty.turtymultiloader.attachment.AttachmentTarget;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @ModifyConstant(method = "extractFood", constant = @Constant(intValue = 10))
    private int industria$modifyFoodIconCount(int original, GuiGraphicsExtractor graphics, Player player) {
        int stomachDestructionLevel = AttachmentService.get()
                .get(AttachmentTarget.entity(player), ModAttachmentTypes.STOMACH_DESTRUCTION_ATTACHMENT)
                .orElse(0);
        int maxFood = 20 - stomachDestructionLevel * 2;
        return (maxFood + 1) / 2;
    }
}
