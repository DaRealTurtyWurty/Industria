package dev.turtywurty.industria.testworld;

import dev.turtywurty.turtymultiloader.event.Events;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;

public final class IndustriaTestWorld {
    public static final String WORLD_ID = "Industria Test World";

    private IndustriaTestWorld() {
    }

    public static void init() {
        Events.onServerStarted(IndustriaTestWorld::configureSpawn);
    }

    private static void configureSpawn(MinecraftServer server) {
        if (!WORLD_ID.equals(server.getWorldData().getLevelName()))
            return;

        ServerLevel overworld = server.overworld();
        int spawnY = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
        overworld.setRespawnData(LevelData.RespawnData.of(
                Level.OVERWORLD,
                new BlockPos(0, spawnY, 0),
                0.0F,
                0.0F));
        server.getGameRules().set(GameRules.RESPAWN_RADIUS, 0, server);
    }
}
