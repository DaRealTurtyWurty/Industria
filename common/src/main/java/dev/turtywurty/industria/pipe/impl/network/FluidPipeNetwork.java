package dev.turtywurty.industria.pipe.impl.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.init.ModPipeNetworkTypes;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkType;
import dev.turtywurty.industria.pipe.PipeStorageContent;
import dev.turtywurty.industria.pipe.PipeStorageUtil;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraStreamCodecs;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariantCodecs;
import dev.turtywurty.turtymultiloader.transfer.storage.SimpleSingleSlotStorage;
import dev.turtywurty.turtymultiloader.transfer.unit.Units;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;

import java.util.Set;
import java.util.UUID;

public class FluidPipeNetwork extends PipeNetwork<ResourceVariant<Fluid>> {
    public static final MapCodec<FluidPipeNetwork> CODEC = PipeNetwork.createCodec(
            PipeStorageContent.codec(ResourceVariantCodecs.FLUID).fieldOf("storageContent")
                    .forGetter(network -> new PipeStorageContent<>(network.storage.resource(0), network.storage.amount(0))),
            (storage, storageContent) -> PipeStorageUtil.restore(
                    (FluidNetworkStorage) storage, storageContent.variant(), storageContent.amount()),
            FluidPipeNetwork::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidPipeNetwork> STREAM_CODEC =
            PipeNetwork.createPacketCodec(
                    PipeStorageContent.streamCodec(ResourceVariantCodecs.FLUID_STREAM),
                    network -> new PipeStorageContent<>(network.storage.resource(0), network.storage.amount(0)),
                    (storage, storageContent) -> PipeStorageUtil.restore(
                            (FluidNetworkStorage) storage, storageContent.variant(), storageContent.amount()),
                    FluidPipeNetwork::new);

    public static final Codec<Set<FluidPipeNetwork>> SET_CODEC = ExtraCodecs.setOf(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Set<FluidPipeNetwork>> SET_STREAM_CODEC =
            ExtraStreamCodecs.setOf(STREAM_CODEC);

    public FluidPipeNetwork(UUID id) {
        super(id, TransferType.FLUID);
    }

    @Override
    protected FluidNetworkStorage createStorage() {
        return new FluidNetworkStorage();
    }

    @Override
    protected PipeNetworkType<ResourceVariant<Fluid>, FluidPipeNetwork> getType() {
        return ModPipeNetworkTypes.FLUID.get();
    }

    public long getFluidAmount() {
        return this.storage.amount(0);
    }

    public ResourceVariant<Fluid> getFluidVariant() {
        return this.storage.resource(0);
    }

    public final class FluidNetworkStorage extends SimpleSingleSlotStorage<ResourceVariant<Fluid>> {
        private FluidNetworkStorage() {
            super(ResourceTypes.FLUID);
        }

        @Override
        protected long getCapacity(ResourceVariant<Fluid> resource) {
            return PipeStorageUtil.scaledCapacity(FluidPipeNetwork.this.pipes.size(), Units.FLUID_BUCKET.numerator());
        }
    }
}
