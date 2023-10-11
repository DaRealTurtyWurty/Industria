package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class TagInit {
    public static TagKey<Item> register(String name) {
        return ItemTags.create(new ResourceLocation(Industria.MOD_ID, name));
    }
}
