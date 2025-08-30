package dev.turtywurty.industria;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.turtywurty.industria.init.*;
import dev.turtywurty.industria.init.transfer_types.TransferTypesInit;
import dev.turtywurty.industria.init.worldgen.BiomeModificationInit;
import dev.turtywurty.industria.init.worldgen.FeatureInit;
import dev.turtywurty.industria.init.worldgen.TrunkPlacerTypeInit;
import dev.turtywurty.industria.util.ExtraPacketCodecs;
import net.fabricmc.api.ModInitializer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Use ServerRecipeManager.createCachedMatchGetter
// TODO: Test all the mixins to see what i broke lol
// TODO: Add maintainence modes to machines to let you replace certain components
public class Industria implements ModInitializer {
    public static final String MOD_ID = "industria";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static Text containerTitle(String name) {
        return Text.translatable("container." + MOD_ID + "." + name);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Loading Industria...");

        IndustriaRegistries.init();
        ItemInit.init();
        BlockInit.init();
        BlockEntityTypeInit.init();
        ScreenHandlerTypeInit.init();
        RecipeTypeInit.init();
        RecipeSerializerInit.init();
        ItemGroupInit.init();
        BiomeModificationInit.init();
        FeatureInit.init();
        FluidInit.init();
        AttachmentTypeInit.init();
        PositionSourceTypeInit.init();
        ComponentTypeInit.init();
        EntityTypeInit.init();
        RecipeBookCategoryInit.init();
        MultiblockTypeInit.init();
        MultiblockDefinitionInit.init();
        SlurryInit.init();
        GasInit.init();
        WoodSetInit.init();
        TrunkPlacerTypeInit.init();
        PipeNetworkTypeInit.init();
        PipeNetworkManagerTypeInit.init();
        ExtraPacketCodecs.registerDefaults();
        TransferTypesInit.init();
        PayloadTypeInit.init();
        PacketReceiverInit.init();
        EventsInit.init();
        FluidAttributesInit.init();
        FluidDataInit.init();
        GasAttributesInit.init();

        LOGGER.info("Industria has finished loading!");
    }
}