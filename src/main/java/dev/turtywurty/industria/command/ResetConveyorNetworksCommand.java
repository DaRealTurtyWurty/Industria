package dev.turtywurty.industria.command;

import com.mojang.brigadier.context.CommandContext;
import dev.turtywurty.industria.persistent.LevelConveyorNetworks;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class ResetConveyorNetworksCommand {
    public static int execute(CommandContext<CommandSourceStack> context) {
        ServerLevel serverLevel = context.getSource().getLevel();
        LevelConveyorNetworks levelConveyorNetworks = LevelConveyorNetworks.getOrCreate(serverLevel);
        int networkCount = levelConveyorNetworks.getNetworkManager().getNetworks().size();
        int conveyorCount = levelConveyorNetworks.getNetworkManager().getConveyorToNetworkId().size();

        levelConveyorNetworks.getNetworkManager().clear();
        levelConveyorNetworks.setDirty();

        context.getSource().sendSuccess(
                () -> Component.literal("Reset conveyor networks: cleared " + networkCount + " network(s) and " + conveyorCount + " mapped conveyor(s)."),
                false);

        return networkCount;
    }
}
