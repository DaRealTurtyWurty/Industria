package dev.turtywurty.industria.init;

import dev.turtywurty.industria.conveyor.*;
import dev.turtywurty.industria.renderer.conveyor.FeederConveyorSpecialRenderer;
import dev.turtywurty.industria.renderer.conveyor.HatchConveyorSpecialRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ConveyorSpecialRendererInit {
    private ConveyorSpecialRendererInit() {
    }

    private static final Map<BlockState, Function<BlockState, ConveyorSpecialRendererEntry>> RENDERERS = new ConcurrentHashMap<>();

    public static ConveyorSpecialRendererEntry getRenderer(BlockState state) {
        return ReloadListener.INSTANCE.getRenderer(state);
    }

    public static void register(BlockState state, Function<BlockState, ConveyorSpecialRendererEntry> renderer) {
        RENDERERS.put(state, renderer);
    }

    public static void register(Block block, Function<BlockState, ConveyorSpecialRendererEntry> renderer) {
        block.getStateDefinition().getPossibleStates().forEach(state -> register(state, renderer));
    }

    public static void registerSimple(Block block, Supplier<ConveyorSpecialRendererEntry> renderer) {
        register(block, _ -> renderer.get());
    }

    public static void init() {
        registerSimple(BlockInit.FEEDER_CONVEYOR, () ->
                ConveyorSpecialRendererEntry.builder(FeederConveyorSpecialRenderer.INSTANCE)
                        .afterItemRendering(true)
                        .build());
        registerSimple(BlockInit.HATCH_CONVEYOR, () ->
                ConveyorSpecialRendererEntry.builder(HatchConveyorSpecialRenderer.INSTANCE)
                        .afterItemRendering(true)
                        .build());
    }

    public record RenderContext(LevelRenderContext levelRenderContext, float partialTick, long gameTime,
                                ConveyorNetworkManager manager, ConveyorNetwork network,
                                ConveyorNetworkStorage networkStorage, BlockPos conveyorPos, int lightCoords,
                                ConveyorStorage conveyorStorage, BlockState conveyorState,
                                AtomicReference<Map<ConveyorItem, Pair<List<Vector3d>, Float>>> itemRenderData) {
    }

    public record ConveyorSpecialRendererEntry(ConveyorSpecialRenderer renderer, boolean overrideItemRendering,
                                               boolean afterItemRendering) {
        public ConveyorSpecialRendererEntry {
            if (renderer == null)
                throw new IllegalArgumentException("Renderer cannot be null");
        }

        public static Builder builder(ConveyorSpecialRenderer renderer) {
            return new Builder(renderer);
        }

        public static class Builder {
            private final ConveyorSpecialRenderer renderer;
            private boolean overrideItemRendering = false;
            private boolean afterItemRendering = false;

            public Builder(ConveyorSpecialRenderer renderer) {
                this.renderer = renderer;
            }

            public Builder overrideItemRendering(boolean overrideItemRendering) {
                this.overrideItemRendering = overrideItemRendering;
                return this;
            }

            public Builder afterItemRendering(boolean afterItemRendering) {
                this.afterItemRendering = afterItemRendering;
                return this;
            }

            public ConveyorSpecialRendererEntry build() {
                return new ConveyorSpecialRendererEntry(renderer, overrideItemRendering, afterItemRendering);
            }
        }
    }

    @FunctionalInterface
    public interface ConveyorSpecialRenderer extends ResourceManagerReloadListener {
        void render(RenderContext context);

        @Override
        default void onResourceManagerReload(ResourceManager resourceManager) {
            // NO-OP by default, override if you need to reload resources for the renderer
        }
    }

    public static class ReloadListener implements ResourceManagerReloadListener {
        public static final ReloadListener INSTANCE = new ReloadListener();

        private final Map<BlockState, ConveyorSpecialRendererEntry> cachedRenderers = new ConcurrentHashMap<>();

        public ConveyorSpecialRendererEntry getRenderer(BlockState state) {
            return this.cachedRenderers.get(state);
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            this.cachedRenderers.clear();
            RENDERERS.forEach((state, rendererFunc) -> {
                ConveyorSpecialRendererEntry entry = rendererFunc.apply(state);
                entry.renderer().onResourceManagerReload(resourceManager);
                this.cachedRenderers.put(state, entry);
            });
        }
    }
}
