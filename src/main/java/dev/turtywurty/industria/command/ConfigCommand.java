package dev.turtywurty.industria.command;

import com.mojang.brigadier.tree.CommandNode;
import dev.turtywurty.industria.config.ServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ConfigCommand {
    public static CommandNode<CommandSourceStack> register() {
        return Commands.literal("reload")
                .executes(context -> {
                    ServerConfig.onReload(context.getSource().getServer());
                    return 1;
                })
                .then(Commands.literal("save")
                        .executes(context -> {
                            ServerConfig.onServerSave(context.getSource().getServer());
                            return 1;
                        }))
                .build();
    }
}
