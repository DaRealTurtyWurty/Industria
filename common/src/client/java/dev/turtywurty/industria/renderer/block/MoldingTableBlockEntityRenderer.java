package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.MoldingTableBlockEntity;
import dev.turtywurty.industria.molding.MoldCastSurface;
import dev.turtywurty.industria.molding.MoldPatterns;
import dev.turtywurty.industria.molding.MoldTableVisualState;
import dev.turtywurty.industria.molding.MoldWorldMesh;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public class MoldingTableBlockEntityRenderer extends IndustriaBlockEntityRenderer<MoldingTableBlockEntity,
        MoldingTableBlockEntityRenderer.MoldingTableRenderState> {
    private static final float CAST_SIZE = 14f / 16f;
    private static final float CAST_TOP = 15f / 16f + CAST_SIZE * MoldWorldMesh.THICKNESS + 0.001f;
    private final Map<MoldingTableBlockEntity, CachedMesh> meshes = new WeakHashMap<>();

    public MoldingTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public MoldingTableRenderState createRenderState() {
        return new MoldingTableRenderState();
    }

    @Override
    public void extractRenderState(MoldingTableBlockEntity blockEntity, MoldingTableRenderState state,
                                   float tickProgress, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.mesh = null;
        state.sprite = null;
        ItemStack stack = blockEntity.getInventory().getItem(0);
        if (stack.isEmpty()) {
            this.meshes.remove(blockEntity);
            return;
        }

        state.updateItemRenderState(0, this, blockEntity, stack, ItemDisplayContext.NONE);
        Material.Baked material = state.itemRenderStates.getFirst().pickParticleMaterial(RandomSource.create(0));
        if (material == null)
            return;

        TextureAtlasSprite sprite = material.sprite();
        MoldTableVisualState visual = blockEntity.getVisualState();
        if (visual != null && !visual.matches(stack)) {
            visual = null;
        }

        CachedMesh cached = this.meshes.get(blockEntity);
        if (cached == null || cached.sprite != sprite || !Objects.equals(cached.visual, visual)
                || !ItemStack.isSameItemSameComponents(cached.stack, stack)) {
            cached = new CachedMesh(stack.copy(), sprite, visual, buildMesh(sprite, visual));
            this.meshes.put(blockEntity, cached);
        }

        state.mesh = cached.mesh;
        state.sprite = sprite;
    }

    private static MoldWorldMesh buildMesh(TextureAtlasSprite sprite, @Nullable MoldTableVisualState visual) {
        var surface = new MoldCastSurface();
        if (visual != null) {
            MoldPatterns.find(visual.moldId()).ifPresent(pattern -> {
                for (int i = 0; i < visual.completedPolygons(); i++) {
                    surface.removePolygon(pattern, i);
                }

                for (MoldTableVisualState.Stroke stroke : visual.strokes()) {
                    surface.carve(stroke.from(), stroke.to(), stroke.radius());
                }
            });
        }
        int width = sprite.contents().width();
        int height = sprite.contents().height();
        boolean[] opaque = new boolean[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                opaque[y * width + x] = !sprite.contents().isTransparent(0, x, y);
            }
        }

        return new MoldWorldMesh(surface.snapshot(), width, height, opaque);
    }

    @Override
    protected void onRender(MoldingTableRenderState state, PoseStack matrices, SubmitNodeCollector queue,
                            int light, int overlay) {
        MoldWorldMesh mesh = state.mesh;
        TextureAtlasSprite sprite = state.sprite;
        if (mesh == null || sprite == null)
            return;

        matrices.pushPose();
        matrices.translate(0, 1.5f - CAST_TOP, 0);
        matrices.mulPose(Axis.XP.rotationDegrees(90));
        matrices.scale(CAST_SIZE, CAST_SIZE, CAST_SIZE);
        queue.submitCustomGeometry(matrices, RenderTypes.entityCutout(sprite.atlasLocation()), (pose, buffer) -> {
            for (MoldWorldMesh.Vertex vertex : mesh.vertices()) {
                int shade = Math.round(vertex.shade() * 255);
                buffer.addVertex(pose, vertex.x(), vertex.y(), vertex.z())
                        .setColor(shade, shade, shade, 255)
                        .setUv(Mth.lerp(vertex.u(), sprite.getU0(), sprite.getU1()),
                                Mth.lerp(vertex.v(), sprite.getV0(), sprite.getV1()))
                        .setOverlay(overlay)
                        .setLight(light)
                        .setNormal(pose, vertex.nx(), vertex.ny(), vertex.nz());
            }
        });
        matrices.popPose();
    }

    public static class MoldingTableRenderState extends IndustriaBlockEntityRenderState {
        private @Nullable MoldWorldMesh mesh;
        private @Nullable TextureAtlasSprite sprite;

        public MoldingTableRenderState() {
            super(1);
        }
    }

    private record CachedMesh(ItemStack stack, TextureAtlasSprite sprite,
                              @Nullable MoldTableVisualState visual, MoldWorldMesh mesh) {
    }
}
