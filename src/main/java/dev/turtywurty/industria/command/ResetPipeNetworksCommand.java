package dev.turtywurty.industria.command;

import com.mojang.brigadier.context.CommandContext;
import dev.turtywurty.industria.init.PipeNetworkManagerInit;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

public class ResetPipeNetworksCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerWorld serverWorld = context.getSource().getWorld();
        for (PipeNetworkManager<?, ?> manager : PipeNetworkManagerInit.PIPE_NETWORK_MANAGERS.stream().toList()) {
            manager.getPipeNetworksData(serverWorld).clear();
        }

        WorldPipeNetworks.getOrCreate(serverWorld).markDirty();
        return 1;
    }
}
