package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.command.ResetConveyorNetworksCommand;
import dev.turtywurty.industria.command.ResetPipeNetworksCommand;
import dev.turtywurty.industria.consumeeffect.DestroyStomachConsumeEffect;
import dev.turtywurty.industria.menu.base.TickableScreenHandler;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.util.StingBottleAfterDamageHandler;
import dev.turtywurty.turtymultiloader.attachment.AttachmentService;
import dev.turtywurty.turtymultiloader.attachment.AttachmentTarget;
import dev.turtywurty.turtymultiloader.event.Events;
import dev.turtywurty.turtymultiloader.network.NetworkService;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class ModEventHandlers {
    public static void init() {
        Events.onPlayerJoin(sender -> {
            NetworkService.get().sendToPlayer(sender, WorldFluidPocketsState.createSyncPacket(sender.level()));
            WorldPipeNetworks.syncToClient(sender);
            LevelConveyorNetworks.syncToClient(sender);
        });

        Events.onBlockBroken((level, _, pos, _, _) -> {
            if (!(level instanceof ServerLevel serverLevel))
                return;

            WorldFluidPocketsState serverState = WorldFluidPocketsState.getServerState(serverLevel);
            if (serverState.removePosition(pos)) {
                WorldFluidPocketsState.sync(serverLevel);
            }
        });

        Events.onCommandRegistration(dispatcher -> {
            dispatcher.register(
                    Commands.literal(Industria.MOD_ID)
                            .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                            .then(Commands.literal("reset_pipe_networks").executes(ResetPipeNetworksCommand::execute).build())
            );

            dispatcher.register(
                    Commands.literal(Industria.MOD_ID)
                            .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                            .then(Commands.literal("reset_conveyor_networks").executes(ResetConveyorNetworksCommand::execute).build())
            );
        });

        Events.onStartLevelTick(level -> {
            for (PipeNetworkManager<?, ?> manager : WorldPipeNetworks.getOrCreate(level).getPipeNetworkManagers()) {
                manager.tick(level);
            }

            LevelConveyorNetworks.getOrCreate(level).getNetworkManager().tick(level);
        });

        Events.onEndLevelTick(world -> {
            for (ServerPlayer player : world.players()) {
                if (player.containerMenu instanceof TickableScreenHandler tickable) {
                    tickable.tick(player);
                }
            }
        });

        Events.onLivingDamaged((entity, source, _) ->
                StingBottleAfterDamageHandler.handle(entity, source));

        Events.onLivingKilled((entity, _) -> {
            if (entity instanceof Player player) {
                AttachmentService attachmentService = AttachmentService.get();
                AttachmentTarget attachmentTarget = AttachmentTarget.entity(player);
                if (attachmentService.getOrCreate(attachmentTarget, ModAttachmentTypes.STOMACH_DESTRUCTION_ATTACHMENT, 0) > 0) {
                    attachmentService.set(attachmentTarget, ModAttachmentTypes.STOMACH_DESTRUCTION_ATTACHMENT, 0);

                    AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
                    if (attribute != null) {
                        attribute.removeModifier(DestroyStomachConsumeEffect.STOMACH_DESTRUCTION_HEALTH_MODIFIER);
                    }
                }
            }
        });
    }
}
