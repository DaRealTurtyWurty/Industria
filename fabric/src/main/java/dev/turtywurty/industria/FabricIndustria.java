package dev.turtywurty.industria;

import dev.turtywurty.gasapi.GasApi;
import dev.turtywurty.multiblocklib.MultiblockLib;
import dev.turtywurty.slurryapi.SlurryApi;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.fabricmc.api.ModInitializer;

public class FabricIndustria implements ModInitializer {
    @Override
    public void onInitialize() {
        GasApi.initialize();
        SlurryApi.initialize();
        MultiblockLib.initialize();
        RegistryService.get().apply();

        Industria.onInitialize();
        Industria.onRegistriesApplied();
    }
}
