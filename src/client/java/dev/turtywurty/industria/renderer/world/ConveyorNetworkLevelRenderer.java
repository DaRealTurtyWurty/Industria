package dev.turtywurty.industria.renderer.world;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.conveyor.*;
import dev.turtywurty.industria.conveyor.block.ConveyorLike;
import dev.turtywurty.industria.conveyor.block.ConveyorOutput;
import dev.turtywurty.industria.conveyor.block.ConveyorTopology;
import dev.turtywurty.industria.conveyor.block.impl.BasicConveyorBlock;
import dev.turtywurty.industria.data.ClientConveyorNetworks;
import dev.turtywurty.industria.init.ConveyorAnchorProviderInit;
import dev.turtywurty.industria.init.ConveyorSpecialRendererInit;
import dev.turtywurty.industria.util.DebugRenderingRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.mixin.client.rendering.ModelPartAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3d;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ConveyorNetworkLevelRenderer implements IndustriaLevelRenderer {
    public static final String DEFAULT_ANCHOR_ROUTE = "default";

    private static final Map<Model<?>, Map<String, ModelPart>> MODEL_PARTS_CACHE = new Object2ObjectOpenHashMap<>();
    private static final Map<BlockState, Map<String, List<Vector3d>>> ITEM_ANCHORS_CACHE = new Object2ObjectOpenHashMap<>();
    private static final Map<UUID, SmoothedProgressState> ITEM_PROGRESS_SMOOTHING = new Object2ObjectOpenHashMap<>();
    private static final Map<UUID, SmoothedDirectionState> ITEM_DIRECTION_SMOOTHING = new Object2ObjectOpenHashMap<>();

    private static void loadAnchorCache() {
        ConveyorAnchorProviderInit.getAnchorProviders().forEach((block, provider) -> {
            ImmutableList<BlockState> possibleStates = block.getStateDefinition().getPossibleStates();
            for (BlockState state : possibleStates) {
                if (!ITEM_ANCHORS_CACHE.containsKey(state)) {
                    Map<String, Model<?>> models = provider.apply(state);
                    Map<String, List<Vector3d>> anchorsByRoute = new Object2ObjectOpenHashMap<>();
                    models.forEach((routeId, model) -> anchorsByRoute.put(routeId, calculateItemAnchors(model, state)));
                    ITEM_ANCHORS_CACHE.put(state, anchorsByRoute);
                }
            }
        });
    }

    private static Map<String, ModelPart> getModelParts(Model<?> model) {
        if (MODEL_PARTS_CACHE.containsKey(model))
            return MODEL_PARTS_CACHE.get(model);

        Map<String, ModelPart> parts = MODEL_PARTS_CACHE.computeIfAbsent(model, _ -> new Object2ObjectOpenHashMap<>());
        collectModelParts(model.root(), parts);
        return parts;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void collectModelParts(ModelPart part, Map<String, ModelPart> parts) {
        ((ModelPartAccessor) (Object) part).fabric$callForEachChild((name, child) -> {
            parts.putIfAbsent(name, child);
            collectModelParts(child, parts);
        });
    }

    private static List<Vector3d> calculateItemAnchors(Model<?> model, BlockState state) {
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.SOUTH;
        boolean mirrorTurnRight = state.hasProperty(BasicConveyorBlock.SHAPE)
                && state.getValue(BasicConveyorBlock.SHAPE) == BasicConveyorBlock.ConveyorShape.TURN_RIGHT;

        Map<String, ModelPart> parts = getModelParts(model);
        return parts.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("anchor"))
                .map(entry -> Map.entry(entry.getKey().substring("anchor".length()), entry.getValue()))
                .map(entry -> {
                    String name = entry.getKey();
                    if (name.isEmpty()) {
                        Industria.LOGGER.warn("Model part name for conveyor item anchor is empty. Expected format: 'anchorX' where X is an integer index.");
                        return null;
                    }

                    try {
                        return Map.entry(Integer.parseInt(name), entry.getValue());
                    } catch (NumberFormatException exception) {
                        Industria.LOGGER.warn("Failed to parse index from model part name: {}", name);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(entry -> {
                    ModelPart part = entry.getValue();
                    var anchor = new Vector3d(
                            part.x / 16.0,
                            part.y / 16.0,
                            part.z / 16.0
                    );
                    if (mirrorTurnRight) {
                        anchor.x = -anchor.x;
                    }

                    return rotateAnchorForFacing(anchor, facing);
                })
                .toList();
    }

    private static Vector3d rotateAnchorForFacing(Vector3d anchor, Direction facing) {
        return switch (facing) {
            case EAST -> new Vector3d(anchor.z, anchor.y, -anchor.x);
            case SOUTH -> new Vector3d(anchor);
            case WEST -> new Vector3d(-anchor.z, anchor.y, anchor.x);
            default -> new Vector3d(-anchor.x, anchor.y, -anchor.z);
        };
    }

    @SuppressWarnings("resource")
    @Override
    public void render(LevelRenderContext context) {
        SubmitNodeCollector nodeCollector = context.submitNodeCollector();
        PoseStack poseStack = context.poseStack();

        Entity cameraEntity = context.gameRenderer().getMainCamera().entity();
        if (cameraEntity == null)
            return;

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null)
            return;

        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        long gameTime = level.getGameTime();
        Set<UUID> visibleItems = new HashSet<>();

        ResourceKey<Level> dimension = level.dimension();
        ConveyorNetworkManager manager = ClientConveyorNetworks.get(dimension);
        if (manager == null) {
            ITEM_PROGRESS_SMOOTHING.clear();
            ITEM_DIRECTION_SMOOTHING.clear();
            return;
        }

        for (ConveyorNetwork network : manager.getNetworks()) {
            if (DebugRenderingRegistry.debugRendering) {
                renderConveyorNetwork(network, level);
            }

            ConveyorNetworkStorage networkStorage = network.getStorage();
            for (Map.Entry<BlockPos, ConveyorStorage> storageEntry : networkStorage.getStorages().entrySet()) {
                BlockPos conveyorPos = storageEntry.getKey();
                ConveyorStorage conveyorStorage = storageEntry.getValue();
                BlockState state = level.getBlockState(conveyorPos);
                if (!(state.getBlock() instanceof ConveyorLike))
                    continue;

                int lightCoords = LevelRenderer.getLightCoords(level, conveyorPos);

                ConveyorSpecialRendererInit.ConveyorSpecialRendererEntry rendererEntry = ConveyorSpecialRendererInit.getRenderer(state);
                ConveyorSpecialRendererInit.RenderContext renderContext = null;
                if (rendererEntry != null) {
                    renderContext = new ConveyorSpecialRendererInit.RenderContext(
                            context, partialTick, gameTime, manager, network, networkStorage, conveyorPos, lightCoords,
                            conveyorStorage, state, new AtomicReference<>());
                }

                if (rendererEntry != null && !rendererEntry.afterItemRendering()) {
                    if (rendererEntry.overrideItemRendering()) {
                        rendererEntry.renderer().render(renderContext);
                        continue;
                    } else {
                        rendererEntry.renderer().render(renderContext);
                    }
                }

                Map<String, List<Vector3d>> itemAnchorsByRoute;
                if (ITEM_ANCHORS_CACHE.containsKey(state)) {
                    itemAnchorsByRoute = ITEM_ANCHORS_CACHE.get(state);
                } else {
                    Industria.LOGGER.warn("No item anchors found for conveyor block state: {}.", state);
                    continue;
                }

                Vec3 pos = conveyorPos.getCenter();
                Camera camera = minecraft.gameRenderer.getMainCamera();
                Vec3 cameraPos = camera.position();
                pos = pos.subtract(cameraPos);

                poseStack.pushPose();
                poseStack.translate(pos.x, pos.y, pos.z);

                Map<ConveyorItem, Pair<List<Vector3d>, Float>> itemRenderData = new HashMap<>();
                for (ConveyorItem conveyorItem : conveyorStorage.getItems()) {
                    List<Vector3d> itemAnchors = resolveItemAnchors(conveyorItem, itemAnchorsByRoute);
                    float smoothedProgress = getSmoothedProgress(conveyorItem, conveyorPos, gameTime, partialTick, visibleItems);
                    renderConveyorItem(conveyorItem, conveyorPos, smoothedProgress, itemAnchors, poseStack, nodeCollector, lightCoords, gameTime, partialTick);
                    if (rendererEntry != null && rendererEntry.afterItemRendering()) {
                        itemRenderData.put(conveyorItem, Pair.of(itemAnchors, smoothedProgress));
                    }
                }

                if (rendererEntry != null && rendererEntry.afterItemRendering()) {
                    renderContext.itemRenderData().set(itemRenderData);
                    rendererEntry.renderer().render(renderContext);
                }

                poseStack.popPose();
            }
        }

        ITEM_PROGRESS_SMOOTHING.keySet().removeIf(id -> !visibleItems.contains(id));
        ITEM_DIRECTION_SMOOTHING.keySet().removeIf(id -> !visibleItems.contains(id));
    }

    private void renderConveyorNetwork(ConveyorNetwork network, ClientLevel level) {
        for (BlockPos conveyor : network.getConveyors()) {
            BlockState state = level.getBlockState(conveyor);
            if (!(state.getBlock() instanceof ConveyorLike conveyorBlock))
                continue;

            ConveyorTopology topology = conveyorBlock.getTopology(level, conveyor, state);
            for (ConveyorOutput output : topology.outputs()) {
                Gizmos.arrow(Vec3.atCenterOf(conveyor), Vec3.atCenterOf(output.deliveryPos()), 0xFFFF0000);
            }
        }
    }

    private static List<Vector3d> resolveItemAnchors(ConveyorItem conveyorItem, Map<String, List<Vector3d>> itemAnchorsByRoute) {
        if (itemAnchorsByRoute.isEmpty())
            return List.of();

        String selectedOutputId = conveyorItem.getSelectedOutputId();
        if (selectedOutputId != null) {
            List<Vector3d> routedAnchors = itemAnchorsByRoute.get(selectedOutputId);
            if (routedAnchors != null)
                return routedAnchors;
        }

        String selectedAnchorRouteId = conveyorItem.getSelectedAnchorRouteId();
        if (selectedAnchorRouteId != null) {
            List<Vector3d> routedAnchors = itemAnchorsByRoute.get(selectedAnchorRouteId);
            if (routedAnchors != null)
                return routedAnchors;
        }

        List<Vector3d> defaultAnchors = itemAnchorsByRoute.get(DEFAULT_ANCHOR_ROUTE);
        if (defaultAnchors != null)
            return defaultAnchors;

        return itemAnchorsByRoute.values().iterator().next();
    }

    private static float getSmoothedProgress(ConveyorItem conveyorItem, BlockPos conveyorPos, long gameTime, float partialTick, Set<UUID> visibleItems) {
        UUID itemId = conveyorItem.getId();
        visibleItems.add(itemId);

        int syncedProgress = conveyorItem.getProgress();
        SmoothedProgressState state = ITEM_PROGRESS_SMOOTHING.get(itemId);
        if (state == null || !state.conveyorPos.equals(conveyorPos)) {
            state = new SmoothedProgressState(conveyorPos, syncedProgress, syncedProgress, gameTime);
            ITEM_PROGRESS_SMOOTHING.put(itemId, state);
            return syncedProgress;
        }

        if (syncedProgress != state.currentSyncedProgress) {
            state.previousSyncedProgress = state.currentSyncedProgress;
            state.currentSyncedProgress = syncedProgress;
            state.lastSyncGameTime = gameTime;
        }

        float elapsedTicks = (float) (gameTime - state.lastSyncGameTime) + partialTick;
        float interpolation = Math.clamp(elapsedTicks, 0.0F, 1.0F);
        return state.previousSyncedProgress + (state.currentSyncedProgress - state.previousSyncedProgress) * interpolation;
    }

    private static void renderConveyorItem(ConveyorItem conveyorItem, BlockPos conveyorPos, float progressTicks, List<Vector3d> itemAnchors,
                                           PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, long gameTime, float partialTick) {
        ItemStack stack = conveyorItem.getStack();
        float progress = progressTicks / (float) ConveyorStorage.MAX_PROGRESS;
        float clampedProgress = Math.clamp(progress, 0.0F, 1.0F);
        Vector3d anchorOffset = interpolateAnchorPosition(itemAnchors, clampedProgress);

        var itemPos = new Vec3(anchorOffset.x, anchorOffset.y, anchorOffset.z);
        poseStack.pushPose();
        poseStack.translate(itemPos.x, itemPos.y + 0.01D, itemPos.z);

        if (itemAnchors.size() > 1) {
            Vector3d rawTravelDirection = sampleTravelDirection(itemAnchors, clampedProgress);
            Vector3d smoothedDirection = getSmoothedTravelDirection(
                    conveyorItem.getId(),
                    conveyorPos,
                    rawTravelDirection,
                    gameTime,
                    partialTick
            );

            double lengthSquared = smoothedDirection.lengthSquared();
            if (lengthSquared > 1.0E-6D) {
                double horizontalLength = Math.sqrt(smoothedDirection.x * smoothedDirection.x + smoothedDirection.z * smoothedDirection.z);
                float yaw = (float) Math.toDegrees(Math.atan2(smoothedDirection.x, smoothedDirection.z));
                float pitch = (float) -Math.toDegrees(Math.atan2(smoothedDirection.y, horizontalLength));
                poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
                poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            }
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        renderItem(stack, poseStack, nodeCollector, lightCoords);

        poseStack.popPose();
    }

    private static Vector3d sampleTravelDirection(List<Vector3d> itemAnchors, float normalizedProgress) {
        if (itemAnchors.size() <= 1)
            return new Vector3d(0.0D, 0.0D, 0.0D);

        float clampedProgress = Math.clamp(normalizedProgress, 0.0F, 1.0F);
        float segmentSize = 1.0F / (itemAnchors.size() - 1);
        float fromProgress = Math.max(0.0F, clampedProgress - segmentSize * 0.5F);
        float toProgress = Math.min(1.0F, clampedProgress + segmentSize * 0.5F);
        if (toProgress <= fromProgress) {
            toProgress = Math.min(1.0F, fromProgress + segmentSize);
        }

        Vector3d from = interpolateAnchorPosition(itemAnchors, fromProgress);
        Vector3d to = interpolateAnchorPosition(itemAnchors, toProgress);
        return new Vector3d(to).sub(from);
    }

    private static Vector3d getSmoothedTravelDirection(UUID itemId, BlockPos conveyorPos, Vector3d rawDirection, long gameTime, float partialTick) {
        SmoothedDirectionState state = ITEM_DIRECTION_SMOOTHING.get(itemId);
        Vector3d normalizedRaw = normalizeOrNull(rawDirection);
        if (state == null) {
            Vector3d initialDirection = normalizedRaw != null ? normalizedRaw : new Vector3d(0.0D, 0.0D, 1.0D);
            state = new SmoothedDirectionState(conveyorPos, new Vector3d(initialDirection), new Vector3d(initialDirection), gameTime);
            ITEM_DIRECTION_SMOOTHING.put(itemId, state);
            return new Vector3d(initialDirection);
        }

        if (!state.conveyorPos.equals(conveyorPos)) {
            state.conveyorPos = conveyorPos;
            state.previousDirection = new Vector3d(state.currentDirection);
            if (normalizedRaw != null) {
                state.currentDirection = new Vector3d(normalizedRaw);
            }
            state.lastConveyorChangeGameTime = gameTime;
        } else if (normalizedRaw != null) {
            state.currentDirection = new Vector3d(normalizedRaw);
        }

        float elapsedTicks = (float) (gameTime - state.lastConveyorChangeGameTime) + partialTick;
        float blend = Math.clamp(elapsedTicks, 0.0F, 1.0F);
        Vector3d blended = new Vector3d(
                state.previousDirection.x + (state.currentDirection.x - state.previousDirection.x) * blend,
                state.previousDirection.y + (state.currentDirection.y - state.previousDirection.y) * blend,
                state.previousDirection.z + (state.currentDirection.z - state.previousDirection.z) * blend
        );
        Vector3d normalizedBlended = normalizeOrNull(blended);
        return normalizedBlended != null ? normalizedBlended : new Vector3d(state.currentDirection);
    }

    private static Vector3d normalizeOrNull(Vector3d direction) {
        double lengthSquared = direction.lengthSquared();
        if (lengthSquared <= 1.0E-6D)
            return null;

        return new Vector3d(direction).normalize();
    }

    public static Vector3d interpolateAnchorPosition(List<Vector3d> itemAnchors, float normalizedProgress) {
        if (itemAnchors.isEmpty())
            return new Vector3d(0, 0, 0);

        if (itemAnchors.size() == 1)
            return itemAnchors.getFirst();

        float clampedProgress = Math.clamp(normalizedProgress, 0.0F, 1.0F);
        double anchorPosition = clampedProgress * (itemAnchors.size() - 1);
        int previousAnchorIndex = (int) Math.floor(anchorPosition);
        int nextAnchorIndex = Math.min(previousAnchorIndex + 1, itemAnchors.size() - 1);
        double anchorLerp = anchorPosition - previousAnchorIndex;

        Vector3d previousAnchor = itemAnchors.get(previousAnchorIndex);
        Vector3d nextAnchor = itemAnchors.get(nextAnchorIndex);
        return new Vector3d(
                previousAnchor.x + (nextAnchor.x - previousAnchor.x) * anchorLerp,
                previousAnchor.y + (nextAnchor.y - previousAnchor.y) * anchorLerp,
                previousAnchor.z + (nextAnchor.z - previousAnchor.z) * anchorLerp
        );
    }

    private static void renderItem(ItemStack stack, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemModelResolver modelResolver = minecraft.getItemModelResolver();

        var itemState = new ItemStackRenderState();
        modelResolver.updateForTopItem(itemState, stack, ItemDisplayContext.GROUND, minecraft.level, null, 0);
        itemState.submit(poseStack, nodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }

    private static final class SmoothedProgressState {
        private final BlockPos conveyorPos;
        private int previousSyncedProgress;
        private int currentSyncedProgress;
        private long lastSyncGameTime;

        private SmoothedProgressState(BlockPos conveyorPos, int previousSyncedProgress, int currentSyncedProgress, long lastSyncGameTime) {
            this.conveyorPos = conveyorPos;
            this.previousSyncedProgress = previousSyncedProgress;
            this.currentSyncedProgress = currentSyncedProgress;
            this.lastSyncGameTime = lastSyncGameTime;
        }
    }

    private static final class SmoothedDirectionState {
        private BlockPos conveyorPos;
        private Vector3d previousDirection;
        private Vector3d currentDirection;
        private long lastConveyorChangeGameTime;

        private SmoothedDirectionState(BlockPos conveyorPos, Vector3d previousDirection, Vector3d currentDirection, long lastConveyorChangeGameTime) {
            this.conveyorPos = conveyorPos;
            this.previousDirection = previousDirection;
            this.currentDirection = currentDirection;
            this.lastConveyorChangeGameTime = lastConveyorChangeGameTime;
        }
    }

    public static class ReloadListener implements ResourceManagerReloadListener {
        public static final ReloadListener INSTANCE = new ReloadListener();

        private ReloadListener() {
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            MODEL_PARTS_CACHE.clear();
            ITEM_ANCHORS_CACHE.clear();
            ITEM_PROGRESS_SMOOTHING.clear();
            ITEM_DIRECTION_SMOOTHING.clear();
            loadAnchorCache();
        }
    }
}
