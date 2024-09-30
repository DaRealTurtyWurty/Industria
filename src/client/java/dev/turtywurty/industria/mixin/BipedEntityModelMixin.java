package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.registry.ArmPositionRegistry;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin {
    @Shadow @Final public ModelPart leftArm;

    @Shadow @Final public ModelPart rightArm;

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;animateArms(Lnet/minecraft/entity/LivingEntity;F)V"))
    private void industria$setAngles(LivingEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo callback) {
        List<ArmPositionRegistry.DynamicArmPosition> positions = ArmPositionRegistry.getArmPosition(entity.getMainHandStack());
        for (ArmPositionRegistry.DynamicArmPosition position : positions) {
            position.apply(entity, this.leftArm, this.rightArm, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        }
    }
}
