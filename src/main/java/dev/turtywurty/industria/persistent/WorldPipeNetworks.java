package dev.turtywurty.industria.persistent;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public class WorldPipeNetworks extends PersistentState {
    private static Type<WorldPipeNetworks> getType(ServerWorld serverWorld) {
        return new Type<>(
                () -> new WorldPipeNetworks(serverWorld),
                (nbtCompound, wrapperLookup) -> readNbt(serverWorld, nbtCompound, wrapperLookup),
                null
        );
    }

    private final ServerWorld serverWorld;

    public WorldPipeNetworks(ServerWorld serverWorld) {
        this.serverWorld = serverWorld;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        PipeNetworkManager.writeAllNbt(this.serverWorld, nbt, registries);
        return nbt;
    }

    public static WorldPipeNetworks readNbt(ServerWorld serverWorld, NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        var state = new WorldPipeNetworks(serverWorld);
        PipeNetworkManager.readAllNbt(serverWorld, nbt, registries);
        return state;
    }

    public static WorldPipeNetworks getOrCreate(ServerWorld serverWorld) {
        PersistentStateManager persistentStateManager = serverWorld.getPersistentStateManager();
        return persistentStateManager.getOrCreate(getType(serverWorld), Industria.MOD_ID + ".pipe_networks");
    }

    @Override
    public void setDirty(boolean dirty) {
        super.setDirty(dirty);

        if(!isDirty())
            return;

//        for (ServerPlayerEntity player : this.serverWorld.getPlayers()) {
//            PipeNetworkManager.sync(player);
//        }
    }
}
