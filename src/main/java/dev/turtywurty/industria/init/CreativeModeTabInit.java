package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeModeTabInit {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Industria.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .icon(ItemInit.LITHIUM.get()::getDefaultInstance)
                    .title(Component.literal("Industria"))
                    .withSearchBar()
                    .displayItems((pParameters, pOutput) -> {
                        for (RegistryObject<Item> entry : ItemInit.ITEMS.getEntries()) {
                            pOutput.accept(entry.get());
                        }
                    })
                    .build());
}
