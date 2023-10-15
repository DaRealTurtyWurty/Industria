package dev.turtywurty.industria;

import com.mojang.logging.LogUtils;
import dev.turtywurty.industria.init.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Industria.MOD_ID)
public class Industria {
    public static final String MOD_ID = "industria";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Industria() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ItemInit.ITEMS.register(bus);
        BlockInit.BLOCKS.register(bus);
        BlockEntityTypeInit.BLOCK_ENTITIES.register(bus);
        MenuTypeInit.MENUS.register(bus);
        CreativeModeTabInit.CREATIVE_TABS.register(bus);
        EntityTypeInit.ENTITIES.register(bus);
    }
}
