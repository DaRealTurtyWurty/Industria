package dev.turtywurty.industria.screen.fakeworld;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.ServerLinks;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.Map;
import java.util.UUID;

/**
 * Creates a stand-alone {@link ClientLevel} that never talks to a server.
 */
public final class FakeWorldBuilder {
    private FakeWorldBuilder() {
    }

    public static Result create(Minecraft client) {
        var profile = new GameProfile(client.getUser().getProfileId(), client.getUser().getName());
        RegistryAccess.Frozen registryManager = client.level != null
                ? client.level.registryAccess().freeze()
                : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

        UUID playerUUID = profile.id() == null ? UUID.randomUUID() : profile.id();

        var connectionState = new CommonListenerCookie(
                new LevelLoadTracker(),
                profile,
                new WorldSessionTelemetryManager(TelemetryEventSender.DISABLED, true, null, "fake_world"),
                registryManager,
                FeatureFlagSet.of(),
                null,
                null,
                null,
                Map.of(),
                null,
                Map.of(),
                ServerLinks.EMPTY,
                Map.of(playerUUID, new PlayerInfo(profile, false)),
                false
        );

        var connection = new Connection(PacketFlow.CLIENTBOUND);
        ClientPacketListener networkHandler = new ClientPacketListener(client, connection, connectionState);

        Holder<DimensionType> dimensionType = connectionState
                .receivedRegistries()
                .lookupOrThrow(Registries.DIMENSION_TYPE)
                .getOrThrow(BuiltinDimensionTypes.OVERWORLD);
        var worldRenderer = new LevelRenderer(
                client,
                client.getEntityRenderDispatcher(),
                client.getBlockEntityRenderDispatcher(),
                client.renderBuffers(),
                new LevelRenderState(),
                client.gameRenderer.getFeatureRenderDispatcher()
        );

        var properties = new ClientLevel.ClientLevelData(Difficulty.NORMAL, false, false);
        var world = new ClientLevel(
                networkHandler,
                properties,
                Level.OVERWORLD,
                dimensionType,
                8,
                8,
                worldRenderer,
                false,
                RandomSource.create().nextLong(),
                63
        );
        worldRenderer.setLevel(world);

        var player = new LocalPlayer(
                client,
                world,
                networkHandler,
                new StatsCounter(),
                new ClientRecipeBook(),
                Input.EMPTY,
                false
        );
        player.setUUID(playerUUID);
        player.setYRot(180.0F);
        player.setXRot(15.0F);
        player.setNoGravity(true);
        world.addEntity(player);

        return new Result(world, networkHandler, player);
    }

    public record Result(ClientLevel world, ClientPacketListener networkHandler, LocalPlayer player) {
    }
}
