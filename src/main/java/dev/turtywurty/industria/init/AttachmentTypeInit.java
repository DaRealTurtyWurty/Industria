package dev.turtywurty.industria.init;

import com.mojang.serialization.Codec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.MultiblockData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.fluid.FluidState;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class AttachmentTypeInit {
    public static final AttachmentType<Map<String, MultiblockData>> MULTIBLOCK_ATTACHMENT =
            AttachmentRegistry.create(Industria.id("multiblock"),
                    mapBuilder -> mapBuilder.persistent(Codec.unboundedMap(Codec.STRING, MultiblockData.CODEC))
                            /*.syncWith(PacketCodec.of((value, buf) -> {
                                buf.writeInt(value.size());
                                value.forEach((key, data) -> {
                                    buf.writeString(key);
                                    MultiblockData.PACKET_CODEC.encode(buf, data);
                                });
                            }, buf -> {
                                int size = buf.readInt();
                                Map<String, MultiblockData> map = new HashMap<>(size);
                                for (int i = 0; i < size; i++) {
                                    String key = buf.readString();
                                    MultiblockData data = MultiblockData.PACKET_CODEC.decode(buf);
                                    map.put(key, data);
                                }

                                return map;
                            }), AttachmentSyncPredicate.all())*/);

    public static final AttachmentType<Map<String, FluidState>> FLUID_MAP_ATTACHMENT =
            AttachmentRegistry.create(Industria.id("fluid_map"),
                    mapBuilder -> mapBuilder.persistent(Codec.unboundedMap(Codec.STRING, FluidState.CODEC))
                            /*.syncWith(PacketCodec.of((value, buf) -> {
                                buf.writeInt(value.size());
                                value.forEach((key, data) -> {
                                    buf.writeString(key);
                                    // TODO: Find a way to encode FluidState
                                });
                            }, buf -> {
                                int size = buf.readInt();
                                Map<String, FluidState> map = new HashMap<>(size);
                                for (int i = 0; i < size; i++) {
                                    String key = buf.readString();
                                    // TODO: Find a way to decode FluidState
                                    map.put(key, data);
                                }

                                return map;
                            }), AttachmentSyncPredicate.all())*/);

    public static void init() {}
}
