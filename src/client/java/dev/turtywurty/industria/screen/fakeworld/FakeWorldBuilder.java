package dev.turtywurty.industria.screen.fakeworld;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.session.telemetry.TelemetrySender;
import net.minecraft.client.session.telemetry.WorldSession;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.ServerLinks;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.Map;

/**
 * Creates a stand-alone {@link ClientWorld} that never talks to a server.
 */
public final class FakeWorldBuilder {
    private FakeWorldBuilder() {
    }

    public static Result create(MinecraftClient client) {
        var profile = new GameProfile(client.getSession().getUuidOrNull(), client.getSession().getUsername());
        DynamicRegistryManager.Immutable registryManager = client.world != null
                ? client.world.getRegistryManager().toImmutable()
                : DynamicRegistryManager.of(Registries.REGISTRIES);

        var connectionState = new ClientConnectionState(
                profile,
                new WorldSession(TelemetrySender.NOOP, true, null, "fake_world"),
                registryManager,
                FeatureSet.empty(),
                null,
                null,
                null,
                Map.of(),
                null,
                Map.of(),
                ServerLinks.EMPTY
        );

        var connection = new ClientConnection(NetworkSide.CLIENTBOUND);
        ClientPlayNetworkHandler networkHandler = new ClientPlayNetworkHandler(client, connection, connectionState);

        RegistryEntry<DimensionType> dimensionType = connectionState
                .receivedRegistries()
                .getOrThrow(RegistryKeys.DIMENSION_TYPE)
                .getOrThrow(DimensionTypes.OVERWORLD);
        var worldRenderer = new WorldRenderer(
                client,
                client.getEntityRenderDispatcher(),
                client.getBlockEntityRenderDispatcher(),
                client.getBufferBuilders()
        );

        var properties = new ClientWorld.Properties(Difficulty.NORMAL, false, false);
        var world = new ClientWorld(
                networkHandler,
                properties,
                World.OVERWORLD,
                dimensionType,
                8,
                8,
                worldRenderer,
                false,
                Random.create().nextLong(),
                63
        );
        worldRenderer.setWorld(world);

        var player = new ClientPlayerEntity(
                client,
                world,
                networkHandler,
                new StatHandler(),
                new ClientRecipeBook(),
                PlayerInput.DEFAULT,
                false
        );
        player.setYaw(180.0F);
        player.setPitch(15.0F);
        player.setNoGravity(true);
        world.addEntity(player);

        return new Result(world, networkHandler, player);
    }

    public record Result(ClientWorld world, ClientPlayNetworkHandler networkHandler, ClientPlayerEntity player) {
    }
}
