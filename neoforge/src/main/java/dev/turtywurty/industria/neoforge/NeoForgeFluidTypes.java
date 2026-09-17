package dev.turtywurty.industria.neoforge;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.fluid.FluidRegistryObject;
import dev.turtywurty.industria.fluid.IndustriaFluid;
import dev.turtywurty.industria.init.ModFluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class NeoForgeFluidTypes {
    private static final DeferredRegister<FluidType> TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Industria.MOD_ID);
    private static final Map<Identifier, Supplier<FluidType>> BY_SOURCE = new HashMap<>();

    private NeoForgeFluidTypes() {
    }

    public static void register(IEventBus modBus) {
        register(ModFluids.CRUDE_OIL);
        register(ModFluids.DIRTY_SODIUM_ALUMINATE);
        register(ModFluids.SODIUM_ALUMINATE);
        register(ModFluids.MOLTEN_ALUMINIUM);
        register(ModFluids.MOLTEN_CRYOLITE);
        register(ModFluids.LATEX);
        register(ModFluids.METHANOL);
        register(ModFluids.FORMIC_ACID);
        register(ModFluids.DILUTED_FORMIC_ACID);
        TYPES.register(modBus);
    }

    private static void register(FluidRegistryObject<?, ?> fluid) {
        Identifier id = fluid.still().id();
        BY_SOURCE.put(id, TYPES.register(id.getPath(), () -> new FluidType(
                FluidType.Properties.create().descriptionId("block." + id.getNamespace() + "." + id.getPath()))));
    }

    public static FluidType get(IndustriaFluid fluid) {
        Identifier id = BuiltInRegistries.FLUID.getKey(fluid.getSource());
        Supplier<FluidType> type = BY_SOURCE.get(id);
        if (type == null)
            throw new IllegalStateException("No NeoForge fluid type registered for " + id);
        return type.get();
    }
}
