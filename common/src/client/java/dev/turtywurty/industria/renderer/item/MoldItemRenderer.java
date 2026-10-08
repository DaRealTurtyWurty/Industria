package dev.turtywurty.industria.renderer.item;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.component.MoldComponent;
import dev.turtywurty.industria.init.ModDataComponentTypes;
import dev.turtywurty.industria.molding.MoldItemTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MoldItemRenderer implements SpecialModelRenderer<MoldItemRenderer.RenderData> {
    private static final Map<RenderData, Identifier> TEXTURES = new HashMap<>();
    private static int nextTextureId;

    @Override
    public void submit(MoldItemRenderer.@Nullable RenderData renderData, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean glint, int outlineColor) {
        if (renderData == null)
            return;

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(getTexture(renderData)), (pose, vertexConsumer) -> {
            vertex(vertexConsumer, pose, 0, 0, 0, 1, light, overlay);
            vertex(vertexConsumer, pose, 1, 0, 1, 1, light, overlay);
            vertex(vertexConsumer, pose, 1, 1, 1, 0, light, overlay);
            vertex(vertexConsumer, pose, 0, 1, 0, 0, light, overlay);
        });
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int light, int overlay) {
        buffer.addVertex(pose.pose(), x, y, 0.5f)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0, 0, 1);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> vertices) {
        vertices.accept(new Vector3f(0, 0, 0.5f));
        vertices.accept(new Vector3f(1, 1, 0.5f));
    }

    @Override
    public @Nullable RenderData extractArgument(ItemStack itemStack) {
        MoldComponent data = itemStack.get(ModDataComponentTypes.MOLD.get());
        if (data == null)
            return null;

        return new RenderData(itemStack.getItem(), data);
    }

    public static void clearTextures() {
        for (Identifier identifier : TEXTURES.values()) {
            if (identifier.equals(MissingTextureAtlasSprite.getLocation()))
                continue;

            Minecraft.getInstance().getTextureManager().release(identifier);
        }

        TEXTURES.clear();
    }

    private static Identifier getTexture(RenderData data) {
        Identifier id = TEXTURES.get(data);
        if (id != null)
            return id;

        try {
            NativeImage image = MoldItemTextures.createTexture(data.item(), data.mold());
            var texture = new DynamicTexture(() -> "Mold item texture", image);
            id = Industria.id("dynamic/mold/" + nextTextureId++);
            Minecraft.getInstance().getTextureManager().register(id, texture);
            TEXTURES.put(data, id);
            return id;
        } catch (IOException exception) {
            id = MissingTextureAtlasSprite.getLocation();
            TEXTURES.put(data, id);
            return id;
        }
    }

    public record RenderData(Item item, MoldComponent mold) {
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<RenderData> {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

        @Override
        public @NonNull SpecialModelRenderer<RenderData> bake(BakingContext bakingContext) {
            return new MoldItemRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<RenderData>> type() {
            return CODEC;
        }
    }
}
