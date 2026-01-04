package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.command.ConfigCommand;
import dev.turtywurty.industria.command.ResetPipeNetworksCommand;
import dev.turtywurty.industria.config.ServerConfig;
import dev.turtywurty.industria.multiblock.old.AutoMultiblockBlock;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.screenhandler.base.TickableScreenHandler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class EventsInit {
    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            sender.sendPacket(WorldFluidPocketsState.createSyncPacket(handler.player.level()));
            WorldPipeNetworks.syncToClient(sender, handler.player.level());
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
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

        ServerLevelEvents.UNLOAD.register((server, world) -> ServerConfig.onServerSave(server));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
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
        });

        ServerTickEvents.START_LEVEL_TICK.register(world -> {
            AutoMultiblockBlock.SHAPE_CACHE.clear();

            for (PipeNetworkManager<?, ?> manager : WorldPipeNetworks.getOrCreate(world).getPipeNetworkManagers()) {
                manager.tick(world);
            }
        });

        ServerTickEvents.END_LEVEL_TICK.register(world -> {
            for (ServerPlayer player : world.players()) {
                if (player.containerMenu instanceof TickableScreenHandler tickable) {
                    tickable.tick(player);
                }
            }
        });
    }
}
