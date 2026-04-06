package dev.turtywurty.industria.conveyor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.util.ExtraCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class ConveyorNetworkStorage {
    private static final Codec<Map<BlockPos, ConveyorStorage>> STORAGE_MAP_CODEC =
            Codec.unboundedMap(ExtraCodecs.BLOCK_POS_STRING_CODEC, ConveyorStorage.CODEC.codec());
    private static final StreamCodec<RegistryFriendlyByteBuf, Map<BlockPos, ConveyorStorage>> STORAGE_MAP_STREAM_CODEC = ByteBufCodecs.map(
            HashMap::new,
            BlockPos.STREAM_CODEC,
            ConveyorStorage.STREAM_CODEC
    );

    public static final MapCodec<ConveyorNetworkStorage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            STORAGE_MAP_CODEC.fieldOf("storages").forGetter(network -> network.storages)
    ).apply(instance, networks -> {
        var storage = new ConveyorNetworkStorage();
        storage.storages.putAll(networks);
        return storage;
    }));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConveyorNetworkStorage> STREAM_CODEC = StreamCodec.composite(
            STORAGE_MAP_STREAM_CODEC, ConveyorNetworkStorage::getStorages,
            (storages) -> {
                var storage = new ConveyorNetworkStorage();
                storage.storages.putAll(storages);
                return storage;
            }
    );

    private final Map<BlockPos, ConveyorStorage> storages = new ConcurrentHashMap<>();

    public Map<BlockPos, ConveyorStorage> getStorages() {
        return storages;
    }

    public void addItems(Level level, BlockPos pos, ConveyorItem... items) {
        ConveyorStorage storage = this.storages.computeIfAbsent(pos, k -> new ConveyorStorage(level, pos));
        storage.addItems(items);
    }

    public void addItems(Level level, BlockPos pos, ItemStack... stacks) {
        ConveyorStorage storage = this.storages.computeIfAbsent(pos, k -> new ConveyorStorage(level, pos));
        Stream.of(stacks).map(stack -> new ConveyorItem(pos, stack)).forEach(storage::addItem);
    }

    public ConveyorStorage getStorageAt(BlockGetter level, BlockPos pos) {
        return storages.computeIfAbsent(pos, _ -> new ConveyorStorage(level, pos));
    }

    public void removeItems(Level level, BlockPos pos, UUID... itemId) {
        ConveyorStorage storage = getStorageAt(level, pos);
        storage.getItems().removeIf(item -> Stream.of(itemId).anyMatch(id -> id.equals(item.getId())));
    }

    public void clearItemsAt(BlockPos... pos) {
        Stream.of(pos).forEach(this.storages::remove);
    }

    public void loadFrom(ConveyorNetworkStorage storage) {
        this.storages.clear();
        this.storages.putAll(storage.getStorages());
    }
}
