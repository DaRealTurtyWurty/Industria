package dev.turtywurty.industria.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.PlayerSkin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class CompositePlayerTextureManager {
    private static final CompositePlayerTextureManager INSTANCE = new CompositePlayerTextureManager();
    private final Map<CacheKey, Identifier> cache = new HashMap<>();

    private CompositePlayerTextureManager() {
    }

    public Identifier getOrCreate(PlayerSkin skin, Object data, Function<Object, Identifier> otherIdFunction, int targetWidth, int targetHeight, boolean placeBelow) {
        Minecraft minecraft = Minecraft.getInstance();
        TextureManager textureManager = minecraft.getTextureManager();
        ResourceManager resourceManager = minecraft.getResourceManager();
        Identifier skinId = skin.body().texturePath();
        Identifier otherId = otherIdFunction.apply(data);
        var key = new CacheKey(skinId, data);
        if (cache.containsKey(key))
            return cache.get(key);

        NativeImage skinImage = loadTexturePixels(textureManager, resourceManager, skinId);
        NativeImage otherImage = loadTexturePixels(textureManager, resourceManager, otherId);
        var combined = new NativeImage(targetWidth, targetHeight, true);

        skinImage.copyRect(0, 0, 0, 0, 64, 64, false, false);
        otherImage.copyRect(0, 0, placeBelow ? 0 : 64, placeBelow ? 64 : 0, otherImage.getWidth(), otherImage.getHeight(), false, false);

        Identifier customId = Identifier.fromNamespaceAndPath(otherId.getNamespace(), otherId.getPath() + "_composite_" + skinId.getPath());
        textureManager.register(customId, new DynamicTexture(customId::toString, combined));
        cache.put(key, customId);
        return customId;
    }

    public NativeImage loadTexturePixels(TextureManager textureManager, ResourceManager resourceManager, Identifier textureId) {
        AbstractTexture texture = textureManager.getTexture(textureId);

        if (texture instanceof DynamicTexture dynamic) {
            NativeImage src = dynamic.getPixels();
            var copy = new NativeImage(src.getWidth(), src.getHeight(), true);
            copy.copyFrom(src);
            return copy;
        }

        try (TextureContents contents = TextureContents.load(resourceManager, textureId)) {
            NativeImage src = contents.image();
            var copy = new NativeImage(src.getWidth(), src.getHeight(), true);
            copy.copyFrom(src);
            return copy;
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load texture: " + textureId, exception);
        }
    }

    public static CompositePlayerTextureManager getInstance() {
        return INSTANCE;
    }

    private record CacheKey(Identifier skinId, Object data) {
    }
}
