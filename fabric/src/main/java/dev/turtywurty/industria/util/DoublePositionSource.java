package dev.turtywurty.industria.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.init.PositionSourceTypeInit;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.phys.Vec3;

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

    public static final StreamCodec<ByteBuf, DoublePositionSource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, DoublePositionSource::x,
            ByteBufCodecs.DOUBLE, DoublePositionSource::y,
            ByteBufCodecs.DOUBLE, DoublePositionSource::z,
            DoublePositionSource::new);

    @Override
    public Optional<Vec3> getPosition(Level world) {
        return Optional.of(new Vec3(this.x, this.y, this.z));
    }

    @Override
    public PositionSourceType<? extends PositionSource> getType() {
        return PositionSourceTypeInit.DOUBLE_POSITION_SOURCE;
    }

    public static class Type implements PositionSourceType<DoublePositionSource> {
        @Override
        public MapCodec<DoublePositionSource> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, DoublePositionSource> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
