package dev.turtywurty.industria.init;

import dev.turtywurty.gasapi.GasApi;
import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;

public class ModGases {
    public static final RegistrationHandle<Gas, Gas> OXYGEN = register("oxygen");
    public static final RegistrationHandle<Gas, Gas> HYDROGEN = register("hydrogen");
    public static final RegistrationHandle<Gas, Gas> CARBON_DIOXIDE = register("carbon_dioxide");
    public static final RegistrationHandle<Gas, Gas> METHANE = register("methane");
    public static final RegistrationHandle<Gas, Gas> CARBON_MONOXIDE = register("carbon_monoxide");

    public static RegistrationHandle<Gas, Gas> register(String name) {
        return GasApi.register(Industria.id(name));
    }

    public static void init() {
    }
}
