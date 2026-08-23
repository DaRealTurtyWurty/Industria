package dev.turtywurty.industria.neoforge;

import dev.turtywurty.industria.IndustriaClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = IndustriaNeoForge.MOD_ID, dist = Dist.CLIENT)
public final class IndustriaNeoForgeClient {
    public IndustriaNeoForgeClient(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(NeoForgeIndustriaTestWorldButton::onScreenInit);
        IndustriaClient.onInitializeClient();
        modBus.addListener(FMLClientSetupEvent.class,
                event -> event.enqueueWork(IndustriaClient::onRegistriesAppliedClient));
    }
}
