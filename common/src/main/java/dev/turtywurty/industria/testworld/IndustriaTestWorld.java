package dev.turtywurty.industria.testworld;

import dev.turtywurty.turtymultiloader.event.Events;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;

@SuppressWarnings("resource")
public final class IndustriaTestWorld {
    public static final String WORLD_ID = "Industria Test World";

    private IndustriaTestWorld() {
    }

    public static void init() {
        Events.onServerStarted(server -> {
            if (!isTestWorld(server))
                return;

            ServerLevel overworld = server.overworld();
            int spawnY = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
            var context = new TestWorldContext(server, overworld, new BlockPos(0, spawnY, 0));

            configureWorld(context);
            IndustriaTestWorldGenerator.generate(context);
        });

        Events.onEndLevelTick(TestWorldScheduler::tick);
    }

    private static void configureWorld(TestWorldContext context) {
        context.overworld().setRespawnData(LevelData.RespawnData.of(
                Level.OVERWORLD,
                context.origin(),
                0.0F,
                0.0F
        ));

        context.overworld().getGameRules().set(GameRules.RESPAWN_RADIUS, 0, context.server());
        context.overworld().getGameRules().set(GameRules.SPAWN_MOBS, false, context.server());
        context.overworld().getGameRules().set(GameRules.ADVANCE_TIME, false, context.server());
        context.overworld().getGameRules().set(GameRules.ADVANCE_WEATHER, false, context.server());

        ServerClockManager clockManager = context.server().clockManager();
        clockManager.setTotalTicks(context.overworld().dimensionTypeRegistration().value().defaultClock().orElseThrow(), 6000L);
    }

    private static boolean isTestWorld(MinecraftServer server) {
        return WORLD_ID.equals(server.getWorldData().getLevelName());
    }
}
