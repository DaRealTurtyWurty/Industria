package dev.turtywurty.industria.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Temporary NeoForge bootstrap. Industria content remains Fabric-only until it is extracted into the common module.
 */
@Mod(IndustriaNeoForge.MOD_ID)
public final class IndustriaNeoForge {
    public static final String MOD_ID = "industria";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public IndustriaNeoForge(IEventBus modBus) {
        LOGGER.info("Industria NeoForge scaffolding loaded; common content has not been ported yet.");
    }
}
