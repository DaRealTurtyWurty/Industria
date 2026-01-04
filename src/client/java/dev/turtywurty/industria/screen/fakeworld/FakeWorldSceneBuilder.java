package dev.turtywurty.industria.screen.fakeworld;

import dev.turtywurty.industria.multiblock.VariedBlockList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Builder that prepares shared fake-world data and populates blocks/entities.
 */
public final class FakeWorldSceneBuilder {
    private final Minecraft client = Minecraft.getInstance();
    private Vec3 cameraPos = new Vec3(2.5, 65.0, 4.0);
    private float cameraYaw = 200.0F;
    private float cameraPitch = -15.0F;
    private Consumer<SceneContext> populator = ctx -> {
    };
    private Consumer<SceneTickContext> tickHandler = ctx -> {
    };

    public static FakeWorldSceneBuilder create() {
        return new FakeWorldSceneBuilder();
    }

    public FakeWorldSceneBuilder camera(Vec3 position, float yaw, float pitch) {
        this.cameraPos = position;
        this.cameraYaw = yaw;
        this.cameraPitch = pitch;
        return this;
    }

    public FakeWorldSceneBuilder populate(Consumer<SceneContext> populator) {
        this.populator = populator;
        return this;
    }

    public FakeWorldSceneBuilder onTick(Consumer<SceneTickContext> tickHandler) {
        this.tickHandler = tickHandler;
        return this;
    }

    public FakeWorldScene build() {
        FakeWorldBuilder.Result result = FakeWorldBuilder.create(this.client);
        var context = new SceneContext(result.world());
        this.populator.accept(context);

        var cameraChunk = new ChunkPos(BlockPos.containing(this.cameraPos));
        result.world().getChunkSource().updateViewCenter(cameraChunk.x, cameraChunk.z);

        Set<BlockPos> allPositions = new HashSet<>();
        for (FakeWorldScene.PlacedBlock placed : context.blocks) {
            allPositions.add(placed.pos());
        }

        for (FakeWorldScene.PlacedFluid placed : context.fluids) {
            allPositions.add(placed.pos());
        }

        for (FakeWorldScene.PlacedVariedBlockList placed : context.variedBlockLists) {
            allPositions.add(placed.pos());
        }

        for (Entity entity : context.entities) {
            allPositions.add(entity.blockPosition());
        }

        List<ChunkPos> allChunks = new ArrayList<>();
        allPositions.stream()
                .map(ChunkPos::new)
                .distinct()
                .forEach(allChunks::add);

        for (ChunkPos chunkPos : allChunks) {
            int index = result.world().getChunkSource().storage.getIndex(chunkPos.x, chunkPos.z);
            result.world().getChunkSource().storage.replace(index, new LevelChunk(
                    result.world(),
                    chunkPos
            ));
        }

        // Place blocks into the fake world so renderers (fluids, lighting) have proper context.
        context.applyToWorld(result.world());
        var builtScene = new BuiltScene(
                result,
                List.copyOf(context.blocks),
                List.copyOf(context.fluids),
                List.copyOf(context.variedBlockLists),
                List.copyOf(context.entities),
                List.copyOf(context.nameplates),
                this.cameraPos,
                this.cameraYaw,
                this.cameraPitch,
                this.tickHandler
        );

        return new FakeWorldScene(builtScene, builtScene.tickHandler());
    }

    /**
     * Represents the constructed scene that a base scene can consume.
     */
    public record BuiltScene(
            FakeWorldBuilder.Result result,
            List<FakeWorldScene.PlacedBlock> blocks,
            List<FakeWorldScene.PlacedFluid> fluids,
            List<FakeWorldScene.PlacedVariedBlockList> variedBlockLists,
            List<Entity> entities,
            List<FakeWorldScene.Nameplate> nameplates,
            Vec3 cameraPos,
            float cameraYaw,
            float cameraPitch,
            Consumer<SceneTickContext> tickHandler
    ) {
    }

    /**
     * Allows callers to add blocks/entities to the scene during population.
     */
    public static final class SceneContext {
        private final ClientLevel world;
        private final List<FakeWorldScene.PlacedBlock> blocks = new ArrayList<>();
        private final List<Entity> entities = new ArrayList<>();
        private final List<FakeWorldScene.PlacedFluid> fluids = new ArrayList<>();
        private final List<FakeWorldScene.PlacedVariedBlockList> variedBlockLists = new ArrayList<>();
        private final List<FakeWorldScene.Nameplate> nameplates = new ArrayList<>();

        SceneContext(ClientLevel world) {
            this.world = world;
        }

        public ClientLevel world() {
            return this.world;
        }

        public void addBlock(BlockPos pos, BlockState state) {
            this.blocks.add(new FakeWorldScene.PlacedBlock(pos, state));
        }

        public void addFluid(BlockPos pos, FluidState state) {
            this.fluids.add(new FakeWorldScene.PlacedFluid(pos, state));
        }

        public void addVariedBlockList(BlockPos pos, VariedBlockList blockList) {
            this.variedBlockLists.add(new FakeWorldScene.PlacedVariedBlockList(pos, blockList));
        }

        public void addEntity(Entity entity) {
            this.entities.add(entity);
        }

        public void addNameplate(Vec3 position, Component text, float yOffset) {
            this.nameplates.add(new FakeWorldScene.Nameplate(position, text, yOffset));
        }

        public void addNameplate(Vec3 position, Component text) {
            addNameplate(position, text, 0.0F);
        }

        public SceneTickContext tickContext() {
            return new SceneTickContext(this.world, List.copyOf(this.entities));
        }

        void applyToWorld(ClientLevel world) {
            for (FakeWorldScene.PlacedBlock placed : this.blocks) {
                BlockState state = placed.state();
                BlockPos pos = placed.pos();
                world.setBlockAndUpdate(pos, state);
                if (state.hasBlockEntity()) {
                    if (state.getBlock() instanceof EntityBlock provider) {
                        var blockEntity = provider.newBlockEntity(pos, state);
                        if (blockEntity != null) {
                            world.setBlockEntity(blockEntity);
                        }
                    }
                }
            }

            for (FakeWorldScene.PlacedFluid placed : this.fluids) {
                world.setBlock(placed.pos(), placed.state().createLegacyBlock(), Block.UPDATE_CLIENTS);
            }
        }
    }

    public record SceneTickContext(ClientLevel world, List<Entity> entities) {
    }
}
