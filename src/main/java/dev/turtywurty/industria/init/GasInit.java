package dev.turtywurty.industria.init;

import dev.turtywurty.gasapi.GasApi;
import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.industria.Industria;

public class GasInit {
    public static final Gas OXYGEN = register("oxygen");
    public static final Gas HYDROGEN = register("hydrogen");
    public static final Gas CARBON_DIOXIDE = register("carbon_dioxide");
    public static final Gas METHANE = register("methane");

    public static Gas register(String name) {
        return GasApi.register(Industria.id(name));
    }

    public static void init() {}
}
