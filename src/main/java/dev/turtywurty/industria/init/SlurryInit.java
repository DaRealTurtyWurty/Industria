package dev.turtywurty.industria.init;

import dev.turtywurty.fabricslurryapi.FabricSlurryApi;
import dev.turtywurty.fabricslurryapi.api.Slurry;
import dev.turtywurty.industria.Industria;

public class SlurryInit {
    public static final Slurry BAUXITE_SLURRY = register("bauxite_slurry");

    public static Slurry register(String name) {
        return FabricSlurryApi.register(Industria.id(name));
    }

    public static void init() {}
}
