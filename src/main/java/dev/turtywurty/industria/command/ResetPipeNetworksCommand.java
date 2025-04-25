package dev.turtywurty.industria.command;

import com.mojang.brigadier.context.CommandContext;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

public class ResetPipeNetworksCommand {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerWorld serverWorld = context.getSource().getWorld();
        WorldPipeNetworks worldPipeNetworks = WorldPipeNetworks.getOrCreate(serverWorld);
        for (PipeNetworkManager<?, ?> manager : worldPipeNetworks.getPipeNetworkManagers()) {
            manager.clear();
        }

        worldPipeNetworks.markDirty();
        return 1;
    }
}
