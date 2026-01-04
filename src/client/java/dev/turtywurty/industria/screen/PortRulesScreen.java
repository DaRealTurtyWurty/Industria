package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.PieceData;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldScene;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldSceneBuilder;
import dev.turtywurty.industria.screen.widget.FakeWorldWidget;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.EnumMap;
import java.util.Map;

public class PortRulesScreen extends Screen {
    private static final Identifier GUI_BORDER_TEXTURE = Industria.id("textures/gui/gui_border.png");
    private static final int BORDER_SLICE = 16;
    private static final int PREVIEW_SIZE = 80;
    private static final double CAMERA_DISTANCE = 4.5;
    private static final double CAMERA_SNAP_DISTANCE_SQ = 0.0001;
    private static final float CAMERA_LERP = 0.2F;

    public static final Component TITLE = Component.translatable("screen." + Industria.MOD_ID + ".port_rules.title");
    public static final Component SIDES_TITLE = Component.translatable("screen." + Industria.MOD_ID + ".port_rules.sides_title");
    public static final Component SIDE_NORTH = Component.translatable("screen." + Industria.MOD_ID + ".port_rules.side_north");
    public static final Component SIDE_SOUTH = Component.translatable("screen." + Industria.MOD_ID + ".port_rules.side_south");
    public static final Component SIDE_WEST = Component.translatable("screen." + Industria.MOD_ID + ".port_rules.side_west");
    public static final Component SIDE_EAST = Component.translatable("screen." + Industria.MOD_ID + ".port_rules.side_east");
    public static final Component SIDE_UP = Component.translatable("screen." + Industria.MOD_ID + ".port_rules.side_up");
    public static final Component SIDE_DOWN = Component.translatable("screen." + Industria.MOD_ID + ".port_rules.side_down");

    private int leftPos, topPos;
    private final int imgWidth = 176, imgHeight = 166;
    private final int titleX = 8, titleY = 6;
    private final Screen parent;
    private final PieceData pieceData;
    private final Runnable onClose;
    private FakeWorldScene scene;
    private FakeWorldWidget fakeWorldWidget;
    private Vec3 cameraTarget;
    private Direction selectedSide = Direction.NORTH;
    private final Map<Direction, Button> sideButtons = new EnumMap<>(Direction.class);

    public PortRulesScreen(Screen parent, PieceData pieceData, Runnable onClose) {
        super(TITLE);
        this.parent = parent;
        this.pieceData = pieceData;
        this.onClose = onClose;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - this.imgWidth) / 2;
        this.topPos = (this.height - this.imgHeight) / 2;

        if (this.scene != null) {
            this.scene.close();
            this.scene = null;
            this.fakeWorldWidget = null;
        }

        CameraPose initialPose = computeCameraPose(this.selectedSide);
        this.scene = FakeWorldSceneBuilder.create()
                .camera(initialPose.position(), initialPose.yaw(), initialPose.pitch())
                .populate(ctx -> ctx.addVariedBlockList(BlockPos.ZERO, this.pieceData.variedBlockList))
                .build();

        this.fakeWorldWidget = addRenderableWidget(new FakeWorldWidget.Builder()
                .scene(this.scene)
                .position(this.leftPos + 8, this.topPos + 18)
                .size(PREVIEW_SIZE, PREVIEW_SIZE)
                .enableInteraction(true)
                .build());
        this.scene.setAnchor(BlockPos.ZERO, PREVIEW_SIZE / 2, PREVIEW_SIZE / 2);
        this.scene.setScaleMultiplier(5f);

        initSideButtons();

        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> onClose())
                .bounds(this.leftPos + this.imgWidth / 2 - 50, this.topPos + this.imgHeight - 30, 100, 20)
                .build());
    }

    private void initSideButtons() {
        int startX = this.leftPos + this.imgWidth + BORDER_SLICE;
        int startY = this.topPos + 28;
        int buttonWidth = 72;
        int buttonHeight = 18;
        int spacingY = 4;

        addSideButton(Direction.NORTH, SIDE_NORTH, startX, startY, buttonWidth, buttonHeight);
        addSideButton(Direction.SOUTH, SIDE_SOUTH, startX, startY + (buttonHeight + spacingY), buttonWidth, buttonHeight);
        addSideButton(Direction.WEST, SIDE_WEST, startX, startY + 2 * (buttonHeight + spacingY), buttonWidth, buttonHeight);
        addSideButton(Direction.EAST, SIDE_EAST, startX, startY + 3 * (buttonHeight + spacingY), buttonWidth, buttonHeight);
        addSideButton(Direction.UP, SIDE_UP, startX, startY + 4 * (buttonHeight + spacingY), buttonWidth, buttonHeight);
        addSideButton(Direction.DOWN, SIDE_DOWN, startX, startY + 5 * (buttonHeight + spacingY), buttonWidth, buttonHeight);

        updateSideButtons();
    }

    private void addSideButton(Direction direction, Component label, int x, int y, int width, int height) {
        Button button = addRenderableWidget(Button.builder(label, btn -> selectSide(direction))
                .bounds(x, y, width, height)
                .build());
        this.sideButtons.put(direction, button);
    }

    private void selectSide(Direction direction) {
        if (this.selectedSide == direction)
            return;

        this.selectedSide = direction;
        this.cameraTarget = computeCameraPose(direction).position();
        updateSideButtons();
    }

    private void updateSideButtons() {
        for (Map.Entry<Direction, Button> entry : this.sideButtons.entrySet()) {
            entry.getValue().active = entry.getKey() != this.selectedSide;
        }
    }

    @Override
    public void tick() {
        if (this.fakeWorldWidget != null) {
            this.fakeWorldWidget.tick();
            updateCameraPan();
        }
    }

    private void updateCameraPan() {
        if (this.cameraTarget == null || this.scene == null)
            return;

        Vec3 current = this.scene.getCameraPosition();
        if (current.distanceToSqr(this.cameraTarget) <= CAMERA_SNAP_DISTANCE_SQ) {
            setCameraAt(this.cameraTarget);
            return;
        }

        var next = new Vec3(
                Mth.lerp(CAMERA_LERP, current.x, this.cameraTarget.x),
                Mth.lerp(CAMERA_LERP, current.y, this.cameraTarget.y),
                Mth.lerp(CAMERA_LERP, current.z, this.cameraTarget.z)
        );
        setCameraAt(next);
    }

    private void setCameraAt(Vec3 position) {
        var center = new Vec3(0.5, 0.5, 0.5);
        Vec3 toCenter = center.subtract(position).normalize();
        float yaw = (float) (Math.toDegrees(Math.atan2(toCenter.z, toCenter.x)) + 90.0);
        float pitch = (float) -Math.toDegrees(Math.asin(toCenter.y));
        this.scene.setCamera(position, yaw, pitch);
    }

    private static CameraPose computeCameraPose(Direction direction) {
        var center = new Vec3(0.5, 0.5, 0.5);
        var offset = new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ())
                .scale(CAMERA_DISTANCE);
        Vec3 position = center.add(offset);
        Vec3 toCenter = center.subtract(position).normalize();
        float yaw = (float) (Math.toDegrees(Math.atan2(toCenter.z, toCenter.x)) + 90.0);
        float pitch = (float) -Math.toDegrees(Math.asin(toCenter.y));
        return new CameraPose(position, yaw, pitch);
    }

    private record CameraPose(Vec3 position, float yaw, float pitch) {
    }

    @Override
    public void onClose() {
        if (this.fakeWorldWidget != null && this.scene != null) {
            this.scene.close();
            this.fakeWorldWidget = null;
            this.scene = null;
        }

        Minecraft client = this.minecraft;
        if (client != null) {
            client.setScreen(this.parent);
        }

        this.onClose.run();
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
            if (this.scene != null) {
                float sensitivity = 0.35F;
                this.scene.rotateCamera((float) (offsetX * sensitivity), (float) (offsetY * sensitivity));
                return true;
            }
        }

        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        super.renderBackground(context, mouseX, mouseY, deltaTicks);
        ScreenUtils.drawNineSlicedTexture(context, GUI_BORDER_TEXTURE,
                this.leftPos, this.topPos,
                this.imgWidth, this.imgHeight,
                0, 48, BORDER_SLICE);

        ScreenUtils.drawNineSlicedTexture(context, GUI_BORDER_TEXTURE,
                this.leftPos + this.imgWidth + 8, this.topPos,
                this.imgWidth / 2, this.imgHeight,
                0, 48, BORDER_SLICE);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawString(this.font, this.title,
                this.leftPos + this.titleX,
                this.topPos + this.titleY,
                0xFF404040, false);
        context.drawString(this.font, SIDES_TITLE,
                this.leftPos + this.imgWidth + BORDER_SLICE,
                this.topPos + 10,
                0xFF404040, false);
    }
}
