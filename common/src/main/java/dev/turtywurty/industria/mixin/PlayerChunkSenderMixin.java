package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.mechanical.MechanicalNetworkManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerChunkSender.class)
public abstract class PlayerChunkSenderMixin {
    @Inject(method = "sendChunk", at = @At("TAIL"))
    private static void industria$sendShafts(
        ServerGamePacketListenerImpl connection,
        ServerLevel level,
        LevelChunk chunk,
        CallbackInfo ci
    ) {
        MechanicalNetworkManager.getOrCreate(level).onChunkSent(level, connection.player, chunk.getPos());
    }
}
