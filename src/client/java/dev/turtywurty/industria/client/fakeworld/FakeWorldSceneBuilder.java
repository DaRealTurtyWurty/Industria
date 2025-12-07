package dev.turtywurty.industria.client.fakeworld;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder that prepares shared fake-world data and populates blocks/entities.
 */
public final class FakeWorldSceneBuilder {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private Vec3d cameraPos = new Vec3d(2.5, 65.0, 4.0);
    private float cameraYaw = 200.0F;
    private float cameraPitch = -15.0F;
    private Consumer<SceneContext> populator = ctx -> {};
    private Consumer<SceneTickContext> tickHandler = ctx -> {};

    public static FakeWorldSceneBuilder create() {
        return new FakeWorldSceneBuilder();
    }

    public FakeWorldSceneBuilder camera(Vec3d position, float yaw, float pitch) {
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
        SceneContext context = new SceneContext(result.world());
        this.populator.accept(context);
        // Place blocks into the fake world so renderers (fluids, lighting) have proper context.
        context.applyToWorld(result.world());
        BuiltScene builtScene = new BuiltScene(
                result,
                List.copyOf(context.blocks),
                List.copyOf(context.fluids),
                List.copyOf(context.entities),
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
            List<Entity> entities,
            Vec3d cameraPos,
            float cameraYaw,
            float cameraPitch,
            Consumer<SceneTickContext> tickHandler
    ) {
    }

    /**
     * Allows callers to add blocks/entities to the scene during population.
     */
    public static final class SceneContext {
        private final ClientWorld world;
        private final List<FakeWorldScene.PlacedBlock> blocks = new ArrayList<>();
        private final List<Entity> entities = new ArrayList<>();
        private final List<FakeWorldScene.PlacedFluid> fluids = new ArrayList<>();

        SceneContext(ClientWorld world) {
            this.world = world;
        }

        public ClientWorld world() {
            return this.world;
        }

        public void addBlock(BlockPos pos, BlockState state) {
            this.blocks.add(new FakeWorldScene.PlacedBlock(pos, state));
        }

        public void addFluid(BlockPos pos, FluidState state) {
            this.fluids.add(new FakeWorldScene.PlacedFluid(pos, state));
        }

        public void addEntity(Entity entity) {
            this.entities.add(entity);
        }

        public SceneTickContext tickContext() {
            return new SceneTickContext(this.world, List.copyOf(this.entities));
        }

        void applyToWorld(ClientWorld world) {
            for (FakeWorldScene.PlacedBlock placed : this.blocks) {
                world.setBlockState(placed.pos(), placed.state());
            }
            for (FakeWorldScene.PlacedFluid placed : this.fluids) {
                world.setBlockState(placed.pos(), placed.state().getBlockState(), 2);
            }
        }
    }

    public record SceneTickContext(ClientWorld world, List<Entity> entities) {
    }
}
