package dev.turtywurty.industria.command;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.Set;

public class TpDimCommand {
    public static CommandNode<ServerCommandSource> register() {
        return CommandManager.literal("tpdim")
                .then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            ServerWorld dimension = DimensionArgumentType.getDimensionArgument(context, "dimension");
                            ServerPlayerEntity player = source.getPlayer();
                            if(player == null) {
                                source.sendError(Text.literal("This command can only be executed by a player."));
                                return 0;
                            }

                            boolean result = player.teleport(dimension, player.getX(), player.getY(), player.getZ(), Set.of(), player.getYaw(), player.getPitch(), false);
                            return result ? 1 : 0;
                        }))
                .build();
    }
}
