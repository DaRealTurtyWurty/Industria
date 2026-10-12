package dev.turtywurty.industria.molding;

import com.mojang.blaze3d.platform.NativeImage;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.component.MoldComponent;
import dev.turtywurty.industria.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.io.InputStream;

public class MoldItemTextures {
    public static Identifier getMaterialTexture(Item item) {
        if (item == ModItems.SAND_MOLD.get())
            return Industria.id("textures/item/blank_sand_cast.png");
        else if (item == ModItems.RED_SAND_MOLD.get())
            return Industria.id("textures/item/blank_red_sand_cast.png");
        else if (item == ModItems.CLAY_MOLD.get())
            return Industria.id("textures/item/blank_clay_cast.png");
        else if (item == ModItems.GOLD_MOLD.get())
            return Industria.id("textures/item/blank_gold_cast.png");
        else if (item == ModItems.CERAMIC_MOLD.get())
            return Industria.id("textures/item/blank_ceramic_cast.png");
        else
            return null;
    }

    public static NativeImage createTexture(Item item, MoldComponent data) throws IOException {
        MoldPattern pattern = MoldPatterns.find(data.moldId()).orElseThrow();
        Identifier materialTexture = getMaterialTexture(item);
        if (materialTexture == null)
            throw new IllegalArgumentException(
                "Item" + item.getDescriptionId() + " does not have a corresponding material texture");

        try (InputStream stream = Minecraft.getInstance().getResourceManager()
            .getResourceOrThrow(materialTexture).open();
            NativeImage material = NativeImage.read(stream)) {
            return MoldGeometry.createItemTexture(material, pattern, data.piece());
        }
    }
}
