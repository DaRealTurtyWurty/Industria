package dev.turtywurty.industria;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.turtywurty.industria.init.*;
import dev.turtywurty.industria.init.transfer_types.ModTransferProviders;
import dev.turtywurty.industria.init.worldgen.ModBiomeModifications;
import dev.turtywurty.industria.init.worldgen.ModConfiguredFeatures;
import dev.turtywurty.industria.init.worldgen.ModFeatures;
import dev.turtywurty.industria.init.worldgen.ModPlacedFeatures;
import dev.turtywurty.industria.init.worldgen.ModTrunkPlacerTypes;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import dev.turtywurty.turtymultiloader.worldgen.WorldGeneration;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Use ServerRecipeManager.createCachedMatchGetter
// TODO: Test all the mixins to see what i broke lol
// TODO: Add maintenance modes to machines to let you replace certain components
public class Industria {
    public static final String MOD_ID = "industria";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static boolean postRegistryInitialized;

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static Component containerTitle(String name) {
        return Component.translatable("container." + MOD_ID + "." + name);
    }

    public static void onInitialize() {
        LOGGER.info("Loading Industria...");

        ModItems.init();
        ModBlocks.init();
        ModBlockEntityTypes.init();
        ModMenuTypes.init();
        ModRecipeTypes.init();
        ModRecipeSerializers.init();
        ModCreativeModeTabs.init();
        WorldGeneration.registerBootstrap(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap);
        WorldGeneration.registerBootstrap(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap);
        ModBiomeModifications.init();
        ModFeatures.init();
        ModFluids.init();
        ModAttachmentTypes.init();
        ModPositionSourceTypes.init();
        ModDataComponentTypes.init();
        ModEntityTypes.init();
        ModRecipeBookCategories.init();
        ModSlurries.init();
        ModGases.init();
        ModWoodSets.init();
        ModTrunkPlacerTypes.init();
        ModPipeNetworkTypes.init();
        ModPipeNetworkManagerTypes.init();
        ModPayloadTypes.init();
        ModEventHandlers.init();
        ModConsumeEffectTypes.init();

        RegistryService.get().apply();

        LOGGER.info("Industria has finished loading!");
    }

    public static synchronized void onRegistriesApplied() {
        if (postRegistryInitialized)
            return;

        postRegistryInitialized = true;
        ModTransferProviders.init();
        ModMultiblockLibIntegration.init();
        ModFluidAttributes.init();
        ModFluidData.init();
        ModGasAttributes.init();
    }
}
