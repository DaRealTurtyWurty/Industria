package dev.turtywurty.industria.mixin;

import com.mojang.logging.LogUtils;
import dev.turtywurty.industria.testworld.IndustriaTestWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

@Mixin(MinecraftServer.class)
public abstract class TestWorldServerMixin {
    @Shadow @Final
    protected LevelStorageSource.LevelStorageAccess storageSource;

    @Unique
    private boolean industria$isDisposableTestWorld() {
        var server = (MinecraftServer) (Object) this;
        return !server.isDedicatedServer()
                && IndustriaTestWorld.WORLD_ID.equals(this.storageSource.getLevelId())
                && IndustriaTestWorld.WORLD_ID.equals(server.getWorldData().getLevelName());
    }

    @Inject(method = {"saveEverything", "saveAllChunks"}, at = @At("HEAD"), cancellable = true)
    private void industria$skipTestWorldSave(boolean silent, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (industria$isDisposableTestWorld())
            cir.setReturnValue(false);
    }

    @Inject(method = "createLevels", at = @At("RETURN"))
    private void industria$disableTestWorldSaving(CallbackInfo ci) {
        if (industria$isDisposableTestWorld()) {
            for (ServerLevel level : ((MinecraftServer) (Object) this).getAllLevels())
                level.noSave = true;
        }
    }

    @Redirect(method = "stopServer", at = @At(value = "FIELD",
            target = "Lnet/minecraft/server/level/ServerLevel;noSave:Z", opcode = Opcodes.PUTFIELD))
    private void industria$keepTestWorldUnsaved(ServerLevel level, boolean noSave) {
        level.noSave = noSave || industria$isDisposableTestWorld();
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;close()V"))
    private void industria$deleteTestWorld(CallbackInfo ci) {
        if (!industria$isDisposableTestWorld())
            return;

        try {
            this.storageSource.deleteLevel();
        } catch (IOException exception) {
            LogUtils.getLogger().error("Failed to delete Industria test world; it will be retried on next launch", exception);
        }
    }
}
