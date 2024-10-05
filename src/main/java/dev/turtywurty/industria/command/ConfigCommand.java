package dev.turtywurty.industria.command;

import com.mojang.brigadier.tree.CommandNode;
import dev.turtywurty.industria.config.ServerConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ConfigCommand {
    public static CommandNode<ServerCommandSource> register() {
        return CommandManager.literal("reload")
                .executes(context -> {
                    ServerConfig.onReload(context.getSource().getServer());
                    return 1;
                })
                .then(CommandManager.literal("save")
                        .executes(context -> {
                            ServerConfig.onServerSave(context.getSource().getServer());
                            return 1;
                        }))
                .build();
    }
}
