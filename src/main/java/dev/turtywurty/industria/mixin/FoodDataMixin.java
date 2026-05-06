package dev.turtywurty.industria.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.init.AttachmentTypeInit;
import dev.turtywurty.industria.util.IHasPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FoodData.class)
public abstract class FoodDataMixin implements IHasPlayer {
    @Shadow
    private int foodLevel;
    @Unique
    private Player industria$player;

    @Unique
    @Override
    public void industria$setPlayer(Player player) {
        this.industria$player = player;
        if (this.industria$player != null) {
            int stomachDestructionLevel = industria$getStomachDestructionLevel();
            this.foodLevel = Math.clamp(this.foodLevel, 0, 20 - stomachDestructionLevel * 2);
        }
    }

    @Unique
    @Override
    public Player industria$getPlayer() {
        return this.industria$player;
    }

    @Unique
    private int industria$getStomachDestructionLevel() {
        return this.industria$player.getAttachedOrElse(AttachmentTypeInit.STOMACH_DESTRUCTION_ATTACHMENT, 0);
    }

    @ModifyExpressionValue(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"))
    private int industria$clampFoodLevel(int original) {
        return Math.clamp(original, 0, 20 - industria$getStomachDestructionLevel() * 2);
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 20))
    private int industria$modifyFoodLevelExhaustionValue(int original) {
        return original - industria$getStomachDestructionLevel() * 2;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 18))
    private int industria$modifyFoodLevelRegenValue(int original) {
        return original - industria$getStomachDestructionLevel() * 2;
    }

    @ModifyConstant(method = "needsFood", constant = @Constant(intValue = 20))
    private int industria$modifyFoodLevelNeedsFoodValue(int original) {
        return original - industria$getStomachDestructionLevel() * 2;
    }

    @ModifyConstant(method = "readAdditionalSaveData", constant = @Constant(intValue = 20))
    private int industria$modifyFoodLevelReadAdditionalSaveDataValue(int original) {
        return original - industria$getStomachDestructionLevel() * 2;
    }
}
