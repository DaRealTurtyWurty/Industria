package dev.turtywurty.industria.command;

import com.mojang.brigadier.context.CommandContext;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class ResetPipeNetworksCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        ServerLevel serverWorld = context.getSource().getLevel();
        WorldPipeNetworks worldPipeNetworks = WorldPipeNetworks.getOrCreate(serverWorld);

        int managerCount = 0;
        int networkCount = 0;
        int pipeCount = 0;
        for (PipeNetworkManager<?, ?> manager : worldPipeNetworks.getPipeNetworkManagers()) {
            managerCount++;
            networkCount += manager.getNetworks().size();
            pipeCount += manager.getPipeToNetworkId().size();
            manager.clear();
        }

        worldPipeNetworks.setDirty();

        final int finalManagerCount = managerCount;
        final int finalNetworkCount = networkCount;
        final int finalPipeCount = pipeCount;

        context.getSource().sendSuccess(
                () -> Component.literal("Reset pipe networks: cleared " + finalNetworkCount + " network(s) across " + finalManagerCount + " manager(s), removing " + finalPipeCount + " mapped pipe(s)."),
                false);

        return networkCount;
    }
}
