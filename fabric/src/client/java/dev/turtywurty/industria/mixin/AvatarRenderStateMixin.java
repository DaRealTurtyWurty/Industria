package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.util.ICompositeTextureState;
import dev.turtywurty.industria.util.IStomachDestroyStage;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements IStomachDestroyStage, ICompositeTextureState {
    @Unique
    public int stomachDestroyStage = 0;
    @Unique
    public Identifier compositeTexture = null;

    @Override
    public int industria$getStomachDestroyStage() {
        return stomachDestroyStage;
    }

    @Override
    public void industria$setStomachDestroyStage(int stage) {
        this.stomachDestroyStage = stage;
    }

    @Override
    public void industria$setCompositeTexture(Identifier texture) {
        this.compositeTexture = texture;
    }

    @Override
    public Identifier industria$getCompositeTexture() {
        return this.compositeTexture;
    }
}
