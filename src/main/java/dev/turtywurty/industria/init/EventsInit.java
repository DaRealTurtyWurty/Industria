package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.command.ConfigCommand;
import dev.turtywurty.industria.command.ResetConveyorNetworksCommand;
import dev.turtywurty.industria.command.ResetPipeNetworksCommand;
import dev.turtywurty.industria.config.ServerConfig;
import dev.turtywurty.industria.consumeeffect.DestroyStomachConsumeEffect;
import dev.turtywurty.industria.event.StingBottleAfterDamageHandler;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockBlock;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.screenhandler.base.TickableScreenHandler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class EventsInit {
    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, _) -> {
            sender.sendPacket(WorldFluidPocketsState.createSyncPacket(handler.player.level()));
            WorldPipeNetworks.syncToClient(sender, handler.player.level());
            LevelConveyorNetworks.syncToClient(sender, handler.player.level());
        });

        PlayerBlockBreakEvents.AFTER.register((world, _, pos, _, _) -> {
            if (!(world instanceof ServerLevel serverWorld))
                return;

            WorldFluidPocketsState serverState = WorldFluidPocketsState.getServerState(serverWorld);
            if (serverState.removePosition(pos)) {
                WorldFluidPocketsState.sync(serverWorld);
            }
        });

        ServerLevelEvents.LOAD.register((server, world) -> {
            ServerConfig.onServerLoad(server);
            WorldPipeNetworks.getOrCreate(world);
        });

        ServerLevelEvents.UNLOAD.register((server, _) -> ServerConfig.onServerSave(server));

        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
            dispatcher.register(
                    Commands.literal(Industria.MOD_ID)
                            .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                            .then(Commands.literal("config").then(ConfigCommand.register()))
            );

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

        ServerTickEvents.START_LEVEL_TICK.register(level -> {
            AutoMultiblockBlock.SHAPE_CACHE.clear();

            for (PipeNetworkManager<?, ?> manager : WorldPipeNetworks.getOrCreate(level).getPipeNetworkManagers()) {
                manager.tick(level);
            }

            LevelConveyorNetworks.getOrCreate(level).getNetworkManager().tick(level);
        });

        ServerTickEvents.END_LEVEL_TICK.register(world -> {
            for (ServerPlayer player : world.players()) {
                if (player.containerMenu instanceof TickableScreenHandler tickable) {
                    tickable.tick(player);
                }
            }
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, _, _, _) ->
                StingBottleAfterDamageHandler.handle(entity, source));

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, _) -> {
            if (entity instanceof Player player) {
                if (player.getAttachedOrGet(AttachmentTypeInit.STOMACH_DESTRUCTION_ATTACHMENT, () -> 0) > 0) {
                    player.setAttached(AttachmentTypeInit.STOMACH_DESTRUCTION_ATTACHMENT, 0);

                    AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
                    if (attribute != null) {
                        attribute.removeModifier(DestroyStomachConsumeEffect.STOMACH_DESTRUCTION_HEALTH_MODIFIER);
                    }
                }
            }
        });
    }
}
