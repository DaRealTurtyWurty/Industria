package dev.turtywurty.industria.persistent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.ConveyorNetworkManager;
import dev.turtywurty.industria.network.conveyor.AddConveyorNetworkPayload;
import dev.turtywurty.turtymultiloader.network.NetworkService;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class LevelConveyorNetworks extends SavedData {
    private static final NetworkService NETWORK = NetworkService.get();

    public static final Codec<LevelConveyorNetworks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConveyorNetworkManager.CODEC.fieldOf("network_manager").forGetter(LevelConveyorNetworks::getNetworkManager)
    ).apply(instance, LevelConveyorNetworks::new));

    private static final SavedDataType<LevelConveyorNetworks> TYPE = new SavedDataType<>(
            Industria.id("conveyor_networks"),
            LevelConveyorNetworks::new,
            CODEC,
            null
    );

    public static LevelConveyorNetworks getOrCreate(ServerLevel serverWorld) {
        SavedDataStorage persistentStateManager = serverWorld.getDataStorage();
        return persistentStateManager.computeIfAbsent(TYPE);
    }

    private final ConveyorNetworkManager networkManager;

    public LevelConveyorNetworks() {
        this(new ConveyorNetworkManager());
    }

    public LevelConveyorNetworks(ConveyorNetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public static void syncToClient(ServerPlayer player) {
        LevelConveyorNetworks levelConveyorNetworks = getOrCreate(player.level());
        for (ConveyorNetwork network : levelConveyorNetworks.getNetworks()) {
            NETWORK.sendToPlayer(player, new AddConveyorNetworkPayload(player.level().dimension(), network));
        }
    }

    public Set<ConveyorNetwork> getNetworks() {
        return this.networkManager.getNetworks();
    }

    @Nullable
    public ConveyorNetwork getNetwork(BlockPos pos) {
        return this.networkManager.getNetworkAt(pos);
    }

    @Nullable
    public ResourceStorage<ResourceVariant<Item>> getStorage(Level level, BlockPos pos) {
        ConveyorNetwork network = getNetwork(pos);
        if (network != null)
            return network.getItemStorage(level, pos);

        return null;
    }

    public ConveyorNetworkManager getNetworkManager() {
        return networkManager;
    }
}
