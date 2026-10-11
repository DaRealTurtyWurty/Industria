package dev.turtywurty.industria.renderer.block;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.blockentity.CastingTableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.component.MoldComponent;
import dev.turtywurty.industria.init.ModDataComponentTypes;
import dev.turtywurty.industria.molding.MoldCastSurface;
import dev.turtywurty.industria.molding.MoldGeometry;
import dev.turtywurty.industria.molding.MoldPattern;
import dev.turtywurty.industria.molding.MoldWorldMesh;
import dev.turtywurty.industria.renderer.item.MoldItemRenderer;
import dev.turtywurty.industria.state.IndustriaBlockEntityRenderState;
import dev.turtywurty.industria.util.FluidRenderUtils;
import dev.turtywurty.turtymultiloader.transfer.fluid.FluidVariantAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class CastingTableBlockEntityRenderer extends IndustriaBlockEntityRenderer<CastingTableBlockEntity,
        CastingTableBlockEntityRenderer.CastingTableRenderState> {
    private static final MoldCastSurface.Snapshot FLAT = new MoldCastSurface().snapshot();
    private static final float TRAY_HEIGHT = 15f / 16f;
    private static final float MOLD_SIZE = 14f / 16f;
    private static final float MOLD_BOTTOM = TRAY_HEIGHT + 0.001f;
    private static final float MOLD_HEIGHT = MOLD_SIZE * MoldWorldMesh.THICKNESS;
    private static final float MOLD_TOP = MOLD_BOTTOM + MOLD_HEIGHT;
    private final Map<MoldItemRenderer.RenderData, PieceMesh> pieces = new HashMap<>();
    private final Map<MoldPattern, CavityMesh> cavities = new HashMap<>();
    private final Map<TextureAtlasSprite, MoldWorldMesh> outputMeshes = new WeakHashMap<>();

    public CastingTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public CastingTableRenderState createRenderState() {
        return new CastingTableRenderState();
    }

    @Override
    public void extractRenderState(CastingTableBlockEntity blockEntity, CastingTableRenderState state,
                                   float tickProgress, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.pieces.clear();
        for (int slot = 0; slot < 2; slot++) {
            ItemStack stack = blockEntity.getInputInventory().getItem(slot);
            MoldComponent mold = stack.get(ModDataComponentTypes.MOLD.get());
            if (!stack.isEmpty() && mold != null) {
                state.pieces.add(getPiece(new MoldItemRenderer.RenderData(stack.getItem(), mold)));
            }
        }

        Optional<MoldPattern> mold = blockEntity.getAssembledMold();
        state.assembled = mold.isPresent();
        state.cavity = mold.isPresent() && !state.pieces.isEmpty()
                ? getCavity(mold.get(), state.pieces.getFirst()) : null;
        SyncingFluidStorage fluid = blockEntity.getFluidStorage();
        long required = blockEntity.getRequiredFluidAmount();
        state.fill = fill(fluid.getAmount(), required > 0 ? required : fluid.getCapacity());
        state.fluidSprite = null;
        FluidRenderUtils.GuiFluidRenderData fluidData = FluidRenderUtils.getRenderData(
                fluid.getResource(), blockEntity.getLevel(), blockEntity.getBlockPos());
        if (fluidData != null && fluid.getAmount() > 0) {
            state.fluidSprite = fluidData.stillSprite();
            state.fluidColor = fluidData.tintColor();
            int luminosity = Math.max((state.lightCoords >> 4) & 15,
                    FluidVariantAttributes.getLuminance(fluid.getResource()));
            state.fluidLight = (state.lightCoords & 0xF00000) | (luminosity << 4);
        }

        ItemStack output = blockEntity.getOutputInventory().getItem(0);
        state.hasOutput = !output.isEmpty();
        state.cooling = state.hasOutput ? 1 : blockEntity.getCoolingFraction();
        state.outputSprite = null;
        state.outputMesh = null;
        ItemStack visible = state.hasOutput ? output : blockEntity.getCastingItemStack();
        state.updateItemRenderState(0, this, blockEntity, visible, ItemDisplayContext.NONE);
        Material.Baked outputMaterial = state.itemRenderStates.getFirst().pickParticleMaterial(RandomSource.create(0));
        if (!visible.isEmpty() && outputMaterial != null) {
            state.outputSprite = outputMaterial.sprite();
            state.outputMesh = this.outputMeshes.computeIfAbsent(state.outputSprite,
                    CastingTableBlockEntityRenderer::createOutputMesh);
        }
    }

    private PieceMesh getPiece(MoldItemRenderer.RenderData data) {
        Identifier texture = MoldItemRenderer.getTexture(data);
        PieceMesh cached = this.pieces.get(data);
        if (cached != null && cached.texture.equals(texture))
            return cached;

        AbstractTexture loaded = Minecraft.getInstance().getTextureManager().getTexture(texture);
        int width = 16;
        int height = 16;
        boolean[] opaque;
        if (loaded instanceof DynamicTexture dynamic && dynamic.getPixels() != null) {
            NativeImage image = dynamic.getPixels();
            width = image.getWidth();
            height = image.getHeight();
            opaque = new boolean[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    opaque[y * width + x] = image.getPixel(x, y) >>> 24 != 0;
                }
            }
        } else {
            opaque = new boolean[width * height];
            Arrays.fill(opaque, true);
        }

        cached = new PieceMesh(texture, new MoldWorldMesh(FLAT, width, height, opaque), width, height);
        this.pieces.put(data, cached);
        return cached;
    }

    private MoldWorldMesh getCavity(MoldPattern pattern, PieceMesh material) {
        CavityMesh cached = this.cavities.get(pattern);
        if (cached != null && cached.texture.equals(material.texture))
            return cached.mesh;

        boolean[] opaque = new boolean[material.width * material.height];
        try (NativeImage mask = MoldGeometry.createMask(pattern, pattern.polygons().size(), material.width, material.height)) {
            for (int y = 0; y < material.height; y++) {
                for (int x = 0; x < material.width; x++) {
                    opaque[y * material.width + x] = mask.getPixel(x, y) == 0;
                }
            }
        }

        var mesh = new MoldWorldMesh(FLAT, material.width, material.height, opaque);
        this.cavities.put(pattern, new CavityMesh(material.texture, mesh));
        return mesh;
    }

    private static MoldWorldMesh createOutputMesh(TextureAtlasSprite sprite) {
        int width = sprite.contents().width();
        int height = sprite.contents().height();
        boolean[] opaque = new boolean[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                opaque[y * width + x] = !sprite.contents().isTransparent(0, x, y);
            }
        }

        return new MoldWorldMesh(FLAT, width, height, opaque);
    }

    @Override
    protected void onRender(CastingTableRenderState state, PoseStack matrices, SubmitNodeCollector queue,
                            int light, int overlay) {
        boolean separate = !state.assembled && state.pieces.size() > 1;
        for (int i = 0; i < state.pieces.size(); i++) {
            PieceMesh piece = state.pieces.get(i);
            float size = MOLD_SIZE * (separate ? 0.48f : 1);
            float x = separate ? (i == 0 ? -0.225f : 0.225f) : 0;
            float top = MOLD_BOTTOM + size * MoldWorldMesh.THICKNESS;
            draw(meshState(piece.mesh, RenderTypes.entityCutout(piece.texture), null, -1, light),
                    matrices, queue, x, top, size, size, overlay);
        }

        if (state.cavity != null && state.fluidSprite != null && state.fill > 0 && !state.hasOutput) {
            float top = MOLD_BOTTOM + MOLD_HEIGHT * state.fill;
            draw(meshState(state.cavity, RenderTypes.entityTranslucent(state.fluidSprite.atlasLocation()),
                            state.fluidSprite, state.fluidColor, state.fluidLight),
                    matrices, queue, 0, top, MOLD_SIZE, MOLD_SIZE * state.fill, overlay);
        }

        if (state.outputMesh != null && state.outputSprite != null && state.cooling > 0) {
            int alpha = Math.round(state.cooling * 255);
            draw(meshState(state.outputMesh, RenderTypes.entityTranslucent(state.outputSprite.atlasLocation()),
                            state.outputSprite, (alpha << 24) | 0xFFFFFF, light),
                    matrices, queue, 0, MOLD_TOP + 0.001f, MOLD_SIZE, MOLD_SIZE, overlay);
        }
    }

    private static MeshDraw meshState(MoldWorldMesh mesh, RenderType type, @Nullable TextureAtlasSprite sprite,
                                      int color, int light) {
        return new MeshDraw(mesh, type, sprite, color, light);
    }

    private static void draw(MeshDraw draw, PoseStack matrices, SubmitNodeCollector queue,
                             float x, float top, float size, float depthSize, int overlay) {
        matrices.pushPose();
        matrices.translate(x, 1.5f - top, 0);
        matrices.mulPose(Axis.XP.rotationDegrees(90));
        matrices.scale(size, size, depthSize);
        queue.submitCustomGeometry(matrices, draw.type, (pose, buffer) -> {
            for (MoldWorldMesh.Vertex vertex : draw.mesh.vertices()) {
                float u = draw.sprite == null ? vertex.u() : Mth.lerp(vertex.u(), draw.sprite.getU0(), draw.sprite.getU1());
                float v = draw.sprite == null ? vertex.v() : Mth.lerp(vertex.v(), draw.sprite.getV0(), draw.sprite.getV1());
                buffer.addVertex(pose, vertex.x(), vertex.y(), vertex.z())
                        .setColor(draw.color)
                        .setUv(u, v)
                        .setOverlay(overlay)
                        .setLight(draw.light)
                        .setNormal(pose, vertex.nx(), vertex.ny(), vertex.nz());
            }
        });
        matrices.popPose();
    }

    public static float receivingHeight(CastingTableBlockEntity table) {
        if (table.getAssembledMold().isEmpty())
            return TRAY_HEIGHT;

        long required = table.getRequiredFluidAmount();
        return MOLD_BOTTOM + MOLD_HEIGHT * fill(table.getFluidStorage().getAmount(),
                required > 0 ? required : table.getFluidStorage().getCapacity());
    }

    private static float fill(long amount, long required) {
        return required <= 0 ? 0 : Math.clamp((float) amount / required, 0, 1);
    }

    public static class CastingTableRenderState extends IndustriaBlockEntityRenderState {
        private final List<PieceMesh> pieces = new ArrayList<>();
        private @Nullable MoldWorldMesh cavity;
        private boolean assembled;
        private float fill;
        private @Nullable TextureAtlasSprite fluidSprite;
        private int fluidColor;
        private int fluidLight;
        private boolean hasOutput;
        private float cooling;
        private @Nullable TextureAtlasSprite outputSprite;
        private @Nullable MoldWorldMesh outputMesh;

        public CastingTableRenderState() {
            super(1);
        }
    }

    private record PieceMesh(Identifier texture, MoldWorldMesh mesh, int width, int height) {
    }

    private record CavityMesh(Identifier texture, MoldWorldMesh mesh) {
    }

    private record MeshDraw(MoldWorldMesh mesh, RenderType type, @Nullable TextureAtlasSprite sprite, int color,
                            int light) {
    }
}
