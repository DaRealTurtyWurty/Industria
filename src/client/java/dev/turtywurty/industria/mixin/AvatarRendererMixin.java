package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.AttachmentTypeInit;
import dev.turtywurty.industria.util.ICompositeTextureState;
import dev.turtywurty.industria.util.IStomachDestroyStage;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.IntStream;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
    @Unique
    private static final Int2ObjectMap<Identifier> STOMACH_DESTRUCTION_TEXTURES = Int2ObjectMaps.unmodifiable(new Int2ObjectArrayMap<>(
            IntStream.range(1, 10).toArray(),
            IntStream.range(1, 10)
                    .mapToObj(i -> Industria.id("textures/entity/avatar/stomach_destruction/stage_" + i + ".png"))
                    .toArray(Identifier[]::new)
    ));

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void industria$extractRenderState(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        int destroyStage = entity.getAttachedOrElse(AttachmentTypeInit.STOMACH_DESTRUCTION_ATTACHMENT, 0);
        ((IStomachDestroyStage) state).industria$setStomachDestroyStage(destroyStage);

        // TODO: Re-enable some day maybe?
//        if (destroyStage > 0) {
//            Identifier textureId = CompositePlayerTextureManager.getInstance().getOrCreate(state.skin, destroyStage, ds -> STOMACH_DESTRUCTION_TEXTURES.get((int) ds), 128, 128, true);
//            ((ICompositeTextureState) state).industria$setCompositeTexture(textureId);
//        } else {
//            ((ICompositeTextureState) state).industria$setCompositeTexture(null);
//        }
    }

    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/resources/Identifier;", at = @At("HEAD"), cancellable = true)
    private void industria$getTextureLocation(AvatarRenderState state, CallbackInfoReturnable<Identifier> ci) {
        if (((ICompositeTextureState) state).industria$getCompositeTexture() != null) {
            ci.setReturnValue(((ICompositeTextureState) state).industria$getCompositeTexture());
        }
    }
}
