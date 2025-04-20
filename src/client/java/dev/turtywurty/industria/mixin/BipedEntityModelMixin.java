package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.registry.ArmPositionRegistry;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin {
    @Shadow
    @Final
    public ModelPart leftArm;

    @Shadow
    @Final
    public ModelPart rightArm;

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;animateArms(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;F)V"))
    private void industria$setAngles(BipedEntityRenderState bipedEntityRenderState, CallbackInfo callback) {
        List<ArmPositionRegistry.DynamicArmPosition> positions = ArmPositionRegistry.getArmPosition(bipedEntityRenderState.getMainHandItemState());
        for (ArmPositionRegistry.DynamicArmPosition position : positions) {
            position.apply(bipedEntityRenderState, this.leftArm, this.rightArm);
        }
    }
}
