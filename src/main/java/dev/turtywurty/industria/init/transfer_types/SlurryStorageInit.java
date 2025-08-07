package dev.turtywurty.industria.init.transfer_types;

import dev.turtywurty.fabricslurryapi.api.storage.SlurryStorage;
import dev.turtywurty.industria.blockentity.CentrifugalConcentratorBlockEntity;
import dev.turtywurty.industria.blockentity.DigesterBlockEntity;
import dev.turtywurty.industria.blockentity.MixerBlockEntity;
import dev.turtywurty.industria.blockentity.ShakingTableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import net.minecraft.server.world.ServerWorld;

public class SlurryStorageInit {
    public static void init() {
        SlurryStorage.SIDED.registerForBlockEntity(MixerBlockEntity::getSlurryProvider, BlockEntityTypeInit.MIXER);
        SlurryStorage.SIDED.registerForBlockEntity(DigesterBlockEntity::getSlurryProvider, BlockEntityTypeInit.DIGESTER);
        SlurryStorage.SIDED.registerForBlockEntity(ShakingTableBlockEntity::getSlurryProvider, BlockEntityTypeInit.SHAKING_TABLE);
        SlurryStorage.SIDED.registerForBlockEntity(CentrifugalConcentratorBlockEntity::getSlurryProvider, BlockEntityTypeInit.CENTRIFUGAL_CONCENTRATOR);

        SlurryStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (world instanceof ServerWorld serverWorld) {
                return WorldPipeNetworks.getOrCreate(serverWorld).getStorage(TransferType.SLURRY, pos);
            }

            return null;
        }, BlockInit.SLURRY_PIPE);
    }
}
