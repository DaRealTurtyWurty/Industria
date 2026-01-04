package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.registry.ArmPositionRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// TODO: Don't grab the stack from the player, but instead find a way to get the stack from the entity
@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
    @Shadow
    @Final
    public ModelPart leftArm;

    @Shadow
    @Final
    public ModelPart rightArm;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;setupAttackAnimation(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V"))
    private void industria$setAngles(HumanoidRenderState bipedEntityRenderState, CallbackInfo callback) {
        List<ArmPositionRegistry.DynamicArmPosition> positions = ArmPositionRegistry.getArmPosition(Minecraft.getInstance().player.getItemInHand(bipedEntityRenderState.useItemHand));
        for (ArmPositionRegistry.DynamicArmPosition position : positions) {
            position.apply(bipedEntityRenderState, this.leftArm, this.rightArm);
        }
    }
}
