package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.slurryapi.SlurryApi;
import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;

public class ModSlurries {
    public static final RegistrationHandle<Slurry, Slurry> BAUXITE_SLURRY = register("bauxite_slurry");
    public static final RegistrationHandle<Slurry, Slurry> CLAY_SLURRY = register("clay_slurry");

    public static RegistrationHandle<Slurry, Slurry> register(String name) {
        return SlurryApi.register(Industria.id(name));
    }

    public static void init() {
    }
}
