package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.MultiblockBlock;
import dev.turtywurty.industria.command.ConfigCommand;
import dev.turtywurty.industria.command.ResetPipeNetworksCommand;
import dev.turtywurty.industria.config.ServerConfig;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import dev.turtywurty.industria.screenhandler.base.TickableScreenHandler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class EventsInit {
    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            sender.sendPacket(WorldFluidPocketsState.createSyncPacket(handler.player.getWorld()));
            WorldPipeNetworks.syncToClient(sender, handler.player.getWorld());
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld serverWorld))
                return;

            WorldFluidPocketsState serverState = WorldFluidPocketsState.getServerState(serverWorld);
            if (serverState.removePosition(pos)) {
                WorldFluidPocketsState.sync(serverWorld);
            }
        });

        ServerWorldEvents.LOAD.register((server, world) -> {
            ServerConfig.onServerLoad(server);
            WorldPipeNetworks.getOrCreate(world);
        });

        ServerWorldEvents.UNLOAD.register((server, world) -> ServerConfig.onServerSave(server));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal(Industria.MOD_ID)
                            .requires(source -> source.hasPermissionLevel(3))
                            .then(CommandManager.literal("config").then(ConfigCommand.register()))
            );

            dispatcher.register(
                    CommandManager.literal(Industria.MOD_ID)
                            .requires(source -> source.hasPermissionLevel(3))
                            .then(CommandManager.literal("reset_pipe_networks").executes(ResetPipeNetworksCommand::execute).build())
            );
        });

        ServerTickEvents.START_WORLD_TICK.register(world -> {
            MultiblockBlock.SHAPE_CACHE.clear();

            for (PipeNetworkManager<?, ?> manager : WorldPipeNetworks.getOrCreate(world).getPipeNetworkManagers()) {
                manager.tick(world);
            }
        });

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (player.currentScreenHandler instanceof TickableScreenHandler tickable) {
                    tickable.tick(player);
                }
            }
        });
    }
}
