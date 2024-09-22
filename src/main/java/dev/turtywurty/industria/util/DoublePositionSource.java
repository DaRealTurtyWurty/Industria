package dev.turtywurty.industria.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.init.PositionSourceTypeInit;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.PositionSourceType;

import java.util.Optional;

/**
 * A {@link PositionSource} that represents a position in the world using three doubles.
 */
public record DoublePositionSource(double x, double y, double z) implements PositionSource {
    public static final MapCodec<DoublePositionSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.fieldOf("x").forGetter(DoublePositionSource::x),
            Codec.DOUBLE.fieldOf("y").forGetter(DoublePositionSource::y),
            Codec.DOUBLE.fieldOf("z").forGetter(DoublePositionSource::z)
    ).apply(instance, DoublePositionSource::new));

    public static final PacketCodec<ByteBuf, DoublePositionSource> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, DoublePositionSource::x,
            PacketCodecs.DOUBLE, DoublePositionSource::y,
            PacketCodecs.DOUBLE, DoublePositionSource::z,
            DoublePositionSource::new);

    @Override
    public Optional<Vec3d> getPos(World world) {
        return Optional.of(new Vec3d(this.x, this.y, this.z));
    }

    @Override
    public PositionSourceType<? extends PositionSource> getType() {
        return PositionSourceTypeInit.DOUBLE_POSITION_SOURCE;
    }

    public static class Type implements PositionSourceType<DoublePositionSource> {
        @Override
        public MapCodec<DoublePositionSource> getCodec() {
            return CODEC;
        }

        @Override
        public PacketCodec<? super RegistryByteBuf, DoublePositionSource> getPacketCodec() {
            return PACKET_CODEC;
        }
    }
}
