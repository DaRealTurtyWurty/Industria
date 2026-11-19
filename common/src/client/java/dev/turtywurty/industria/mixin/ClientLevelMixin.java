package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.data.ClientShaftRotations;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "unload", at = @At("TAIL"))
    private void industria$unloadShafts(LevelChunk chunk, CallbackInfo ci) {
        ClientShaftRotations.onChunkUnloaded((ClientLevel) (Object) this, chunk.getPos());
    }
}
