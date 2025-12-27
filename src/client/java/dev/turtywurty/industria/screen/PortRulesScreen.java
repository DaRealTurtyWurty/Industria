package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.PieceData;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldScene;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldSceneBuilder;
import dev.turtywurty.industria.screen.widget.FakeWorldWidget;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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

    public static final Text TITLE = Text.translatable("screen." + Industria.MOD_ID + ".port_rules.title");
    public static final Text SIDES_TITLE = Text.translatable("screen." + Industria.MOD_ID + ".port_rules.sides_title");
    public static final Text SIDE_NORTH = Text.translatable("screen." + Industria.MOD_ID + ".port_rules.side_north");
    public static final Text SIDE_SOUTH = Text.translatable("screen." + Industria.MOD_ID + ".port_rules.side_south");
    public static final Text SIDE_WEST = Text.translatable("screen." + Industria.MOD_ID + ".port_rules.side_west");
    public static final Text SIDE_EAST = Text.translatable("screen." + Industria.MOD_ID + ".port_rules.side_east");
    public static final Text SIDE_UP = Text.translatable("screen." + Industria.MOD_ID + ".port_rules.side_up");
    public static final Text SIDE_DOWN = Text.translatable("screen." + Industria.MOD_ID + ".port_rules.side_down");

    private int leftPos, topPos;
    private final int imgWidth = 176, imgHeight = 166;
    private final int titleX = 8, titleY = 6;
    private final Screen parent;
    private final PieceData pieceData;
    private final Runnable onClose;
    private FakeWorldScene scene;
    private FakeWorldWidget fakeWorldWidget;
    private Vec3d cameraTarget;
    private Direction selectedSide = Direction.NORTH;
    private final Map<Direction, ButtonWidget> sideButtons = new EnumMap<>(Direction.class);

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
                .populate(ctx -> ctx.addVariedBlockList(BlockPos.ORIGIN, this.pieceData.variedBlockList))
                .build();

        this.fakeWorldWidget = addDrawableChild(new FakeWorldWidget.Builder()
                .scene(this.scene)
                .position(this.leftPos + 8, this.topPos + 18)
                .size(PREVIEW_SIZE, PREVIEW_SIZE)
                .enableInteraction(true)
                .build());
        this.scene.setAnchor(BlockPos.ORIGIN, PREVIEW_SIZE / 2, PREVIEW_SIZE / 2);
        this.scene.setScaleMultiplier(5f);

        initSideButtons();

        addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, button -> close())
                .dimensions(this.leftPos + this.imgWidth / 2 - 50, this.topPos + this.imgHeight - 30, 100, 20)
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

    private void addSideButton(Direction direction, Text label, int x, int y, int width, int height) {
        ButtonWidget button = addDrawableChild(ButtonWidget.builder(label, btn -> selectSide(direction))
                .dimensions(x, y, width, height)
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
        for (Map.Entry<Direction, ButtonWidget> entry : this.sideButtons.entrySet()) {
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

        Vec3d current = this.scene.getCameraPosition();
        if (current.squaredDistanceTo(this.cameraTarget) <= CAMERA_SNAP_DISTANCE_SQ) {
            setCameraAt(this.cameraTarget);
            return;
        }

        var next = new Vec3d(
                MathHelper.lerp(CAMERA_LERP, current.x, this.cameraTarget.x),
                MathHelper.lerp(CAMERA_LERP, current.y, this.cameraTarget.y),
                MathHelper.lerp(CAMERA_LERP, current.z, this.cameraTarget.z)
        );
        setCameraAt(next);
    }

    private void setCameraAt(Vec3d position) {
        var center = new Vec3d(0.5, 0.5, 0.5);
        Vec3d toCenter = center.subtract(position).normalize();
        float yaw = (float) (Math.toDegrees(Math.atan2(toCenter.z, toCenter.x)) + 90.0);
        float pitch = (float) -Math.toDegrees(Math.asin(toCenter.y));
        this.scene.setCamera(position, yaw, pitch);
    }

    private static CameraPose computeCameraPose(Direction direction) {
        var center = new Vec3d(0.5, 0.5, 0.5);
        var offset = new Vec3d(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ())
                .multiply(CAMERA_DISTANCE);
        Vec3d position = center.add(offset);
        Vec3d toCenter = center.subtract(position).normalize();
        float yaw = (float) (Math.toDegrees(Math.atan2(toCenter.z, toCenter.x)) + 90.0);
        float pitch = (float) -Math.toDegrees(Math.asin(toCenter.y));
        return new CameraPose(position, yaw, pitch);
    }

    private record CameraPose(Vec3d position, float yaw, float pitch) {
    }

    @Override
    public void close() {
        if (this.fakeWorldWidget != null && this.scene != null) {
            this.scene.close();
            this.fakeWorldWidget = null;
            this.scene = null;
        }

        MinecraftClient client = this.client;
        if (client != null) {
            client.setScreen(this.parent);
        }

        this.onClose.run();
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
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
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
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
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawText(this.textRenderer, this.title,
                this.leftPos + this.titleX,
                this.topPos + this.titleY,
                0xFF404040, false);
        context.drawText(this.textRenderer, SIDES_TITLE,
                this.leftPos + this.imgWidth + BORDER_SLICE,
                this.topPos + 10,
                0xFF404040, false);
    }
}
