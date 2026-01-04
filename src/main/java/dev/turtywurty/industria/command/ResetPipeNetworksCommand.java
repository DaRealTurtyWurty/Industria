package dev.turtywurty.industria.command;

import com.mojang.brigadier.context.CommandContext;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;

public class ResetPipeNetworksCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        ServerLevel serverWorld = context.getSource().getLevel();
        WorldPipeNetworks worldPipeNetworks = WorldPipeNetworks.getOrCreate(serverWorld);
        for (PipeNetworkManager<?, ?> manager : worldPipeNetworks.getPipeNetworkManagers()) {
            manager.clear();
        }

        worldPipeNetworks.setDirty();
        return 1;
    }
}
