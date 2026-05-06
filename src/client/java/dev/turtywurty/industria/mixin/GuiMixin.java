package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.init.AttachmentTypeInit;
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
        int stomachDestructionLevel = player.getAttachedOrGet(AttachmentTypeInit.STOMACH_DESTRUCTION_ATTACHMENT, () -> 0);
        int maxFood = 20 - stomachDestructionLevel * 2;
        return (maxFood + 1) / 2;
    }
}