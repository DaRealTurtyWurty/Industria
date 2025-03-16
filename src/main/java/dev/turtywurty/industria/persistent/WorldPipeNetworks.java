package dev.turtywurty.industria.persistent;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class WorldPipeNetworks extends PersistentState {
    private static final Type<WorldPipeNetworks> TYPE = new Type<>(
            WorldPipeNetworks::new,
            WorldPipeNetworks::readNbt,
            null
    );

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.put("Energy", PipeNetworkManager.ENERGY.writeNbt(registries));
        nbt.put("Fluid", PipeNetworkManager.FLUID.writeNbt(registries));
        nbt.put("Heat", PipeNetworkManager.HEAT.writeNbt(registries));
        nbt.put("Slurry", PipeNetworkManager.SLURRY.writeNbt(registries));

        return nbt;
    }

    public static WorldPipeNetworks readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        var state = new WorldPipeNetworks();

        PipeNetworkManager.ENERGY.readNbt(nbt.getCompound("Energy"), registries);
        PipeNetworkManager.FLUID.readNbt(nbt.getCompound("Fluid"), registries);
        PipeNetworkManager.HEAT.readNbt(nbt.getCompound("Heat"), registries);
        PipeNetworkManager.SLURRY.readNbt(nbt.getCompound("Slurry"), registries);

        return state;
    }

    public static WorldPipeNetworks getOrCreate(ServerWorld serverWorld) {
        if(serverWorld.getRegistryKey() != World.OVERWORLD) // TODO: Remove when networks are dimension-sensitive
            return new WorldPipeNetworks();

        PersistentStateManager persistentStateManager = serverWorld.getPersistentStateManager();
        return persistentStateManager.getOrCreate(TYPE, Industria.MOD_ID + ".pipe_networks");
    }
}
