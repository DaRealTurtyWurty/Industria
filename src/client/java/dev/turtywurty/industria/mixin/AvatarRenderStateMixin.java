package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.util.IStomachDestroyStage;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements IStomachDestroyStage {
    @Unique
    public int stomachDestroyStage = 0;

    @Override
    public int industria$getStomachDestroyStage() {
        return stomachDestroyStage;
    }

    @Override
    public void industria$setStomachDestroyStage(int stage) {
        this.stomachDestroyStage = stage;
    }
}
