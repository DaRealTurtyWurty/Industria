package dev.turtywurty.industria.neoforge;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.IndustriaDataGeneration;
import dev.turtywurty.turtymultiloader.neoforge.datagen.NeoForgeDataGeneration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Temporary NeoForge bootstrap while Industria content is moved into the common module. */
@Mod(IndustriaNeoForge.MOD_ID)
public final class IndustriaNeoForge {
    public static final String MOD_ID = "industria";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public IndustriaNeoForge(IEventBus modBus) {
        Industria.onInitialize();

        modBus.addListener(FMLCommonSetupEvent.class, event ->
                event.enqueueWork(Industria::onRegistriesApplied));
        modBus.addListener(GatherDataEvent.Client.class, event ->
                NeoForgeDataGeneration.run(event, IndustriaDataGeneration.SPEC));
        modBus.addListener(GatherDataEvent.Server.class, event ->
                NeoForgeDataGeneration.run(event, IndustriaDataGeneration.SPEC));

        LOGGER.info("Industria NeoForge loaded.");
    }
}
