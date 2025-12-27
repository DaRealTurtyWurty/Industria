package dev.turtywurty.industria.renderer.block;

import dev.turtywurty.industria.blockentity.RotaryKilnControllerBlockEntity;
import dev.turtywurty.industria.blockentity.RotaryKilnControllerBlockEntity.InputRecipeEntry;
import dev.turtywurty.industria.model.RotaryKilnModel;
import dev.turtywurty.industria.state.RotaryKilnControllerRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RotaryKilnBlockEntityRenderer extends IndustriaBlockEntityRenderer<RotaryKilnControllerBlockEntity, RotaryKilnControllerRenderState> {
    public static final Map<BlockPos, RendererData> BLOCK_POS_RENDERER_DATA_MAP = new ConcurrentHashMap<>();

    private static final float BARREL_ANGULAR_VELOCITY = 1.5f;
    private static final float MIN_FRICTION = 2f;
    private static final float MAX_FRICTION = 4f;
    private static final float MIN_RESTITUTION = 0.2f;
    private static final float MAX_RESTITUTION = 0.4f;
    private static final float ITEM_START_Z = -1.5f;
    private static final float ITEM_END_OFFSET_Z = 0.25f;
    private static final float GRAVITY = 9.8f;

    private final RotaryKilnModel model;

    public RotaryKilnBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        this.model = new RotaryKilnModel(context.getLayerModelPart(RotaryKilnModel.LAYER_LOCATION));
    }

    @Override
    public RotaryKilnControllerRenderState createRenderState() {
        return new RotaryKilnControllerRenderState();
    }

    @Override
    public void updateRenderState(RotaryKilnControllerBlockEntity blockEntity, RotaryKilnControllerRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.kilnSegments.clear();
        state.kilnSegments.addAll(blockEntity.getKilnSegments());

        state.recipes.clear();
        state.recipes.addAll(blockEntity.getRecipes());

        for (int i = 0; i < state.recipes.size(); i++) {
            state.updateItemRenderState(i, this, blockEntity, state.recipes.get(i).inputStack());
        }
    }

    @Override
    protected void onRender(RotaryKilnControllerRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        RenderLayer renderLayer = this.model.getLayer(RotaryKilnModel.TEXTURE_LOCATION);

        ClientWorld clientWorld = MinecraftClient.getInstance().world;
        if (clientWorld == null)
            return;

        RendererData rendererData = BLOCK_POS_RENDERER_DATA_MAP.computeIfAbsent(state.pos, pos -> new RendererData());

        queue.submitModel(this.model,
                new RotaryKilnModel.RotaryKilnModelRenderState(
                        state.kilnSegments.size(),
                        rendererData.barrelBody.getAngle() + (float) Math.PI / 8f),
                matrices, renderLayer,
                light, overlay, 0, state.crumblingOverlay);

        matrices.translate(0, -1, 0);
        renderItems(rendererData, state, state.tickProgress, matrices, queue, light, overlay);
    }

    private void renderItems(RendererData rendererData, RotaryKilnControllerRenderState state, float tickDelta, MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay) {
        Map<InputRecipeEntry, Body> recipeToBodyMap = rendererData.recipeToBodyMap;

        World box2dWorld = rendererData.box2dWorld;

        long now = System.nanoTime();
        float deltaTime = (now - rendererData.lastRenderTime) / 1_000_000_000f; // seconds
        rendererData.lastRenderTime = now;
        box2dWorld.step(deltaTime, 6, 2);

        Iterator<Map.Entry<InputRecipeEntry, Body>> iterator = recipeToBodyMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<InputRecipeEntry, Body> entry = iterator.next();

            if (!state.recipes.contains(entry.getKey())) {
                box2dWorld.destroyBody(entry.getValue());
                iterator.remove();
            }
        }

        List<InputRecipeEntry> recipes = state.recipes;
        for (int i = 0; i < recipes.size(); i++) {
            InputRecipeEntry recipe = recipes.get(i);
            ItemStack itemStack = recipe.inputStack();

            Body body = recipeToBodyMap.computeIfAbsent(recipe, r -> createNewItemBody(box2dWorld, itemStack));

            float rawProgress = recipe.getProgress() + tickDelta;

            int numKilnSegments = Math.min(state.kilnSegments.size(), 15);
            float z = MathHelper.map(rawProgress / 100f / numKilnSegments, 0, 1, ITEM_START_Z, -numKilnSegments + ITEM_END_OFFSET_Z);

            matrices.push();
            matrices.translate(body.getPosition().x, body.getPosition().y, z);
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.multiply(Direction.WEST.getRotationQuaternion());
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(body.getAngle()));
            state.renderItemRenderState(i, matrices, queue);
            matrices.pop();
        }
    }

    private Body createNewItemBody(World box2dWorld, ItemStack itemStack) {
        boolean blockItem = itemStack.getItem() instanceof BlockItem;

        var squareDef = new BodyDef();
        squareDef.type = BodyType.DYNAMIC;
        squareDef.position.set(new Vec2(0, 0));
        squareDef.bullet = true;
        Body box = box2dWorld.createBody(squareDef);
        box.setGravityScale(1f);
        box.setLinearVelocity(new Vec2(((float) Math.random() - 0.5f) * 2f, ((float) Math.random() - 0.5f) * 2f));

        var squareShape = new PolygonShape();
        if (blockItem) {
            squareShape.setAsBox(0.25f, 0.25f);
        } else {
            squareShape.setAsBox(0.25f, 0.01f);
        }

        var squareFixture = new FixtureDef();
        squareFixture.shape = squareShape;
        squareFixture.density = 0.5f;
        squareFixture.friction = MathHelper.lerp((float) Math.random(), MIN_FRICTION, MAX_FRICTION);
        squareFixture.restitution = MathHelper.lerp((float) Math.random(), MIN_RESTITUTION, MAX_RESTITUTION);
        squareFixture.filter.categoryBits = 0x0002;
        squareFixture.filter.maskBits = 0x0001;

        box.createFixture(squareFixture);

        return box;
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    public static final class RendererData {
        private final Map<InputRecipeEntry, Body> recipeToBodyMap = new HashMap<>();
        private final World box2dWorld;
        private final Body barrelBody;

        private long lastRenderTime = System.nanoTime();

        public RendererData() {
            box2dWorld = new World(new Vec2(0, GRAVITY));

            var barrelDef = new BodyDef();
            barrelDef.type = BodyType.KINEMATIC;
            barrelDef.position.set(0, 0);

            barrelBody = box2dWorld.createBody(barrelDef);
            barrelBody.setGravityScale(0f);
            barrelBody.setAngularVelocity(BARREL_ANGULAR_VELOCITY);

            float barrelSize = 1.5f;
            Vec2[] barrelVertices = new Vec2[8];
            for (int i = 0; i < 8; i++) {
                float angle = (float) (Math.PI / 4 * i);
                barrelVertices[i] = new Vec2(barrelSize * (float) Math.cos(angle), barrelSize * (float) Math.sin(angle));
            }

            var barrelShape = new ChainShape();
            barrelShape.createLoop(barrelVertices, barrelVertices.length);

            var barrelFixture = new FixtureDef();
            barrelFixture.shape = barrelShape;
            barrelFixture.density = 1f;
            barrelFixture.friction = 5f;
            barrelFixture.restitution = 0.4f;
            barrelFixture.filter.categoryBits = 0x0001;
            barrelFixture.filter.maskBits = 0xFFFF;
            barrelBody.createFixture(barrelFixture);
        }
    }
}
