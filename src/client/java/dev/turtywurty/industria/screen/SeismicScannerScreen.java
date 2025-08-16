package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.ComponentTypeInit;
import dev.turtywurty.industria.item.SeismicScannerItem;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SeismicScannerScreen extends Screen {
    private static final Identifier TEXTURE = Industria.id("textures/gui/seismic_scanner.png");

    private final int backgroundWidth = 176;
    private final int backgroundHeight = 166;
    private int x, y;

    private final ItemStack stack;
    private final List<WorldFluidPocketsState.FluidPocket> fluidPockets = new ArrayList<>();

    public SeismicScannerScreen(ItemStack stack) {
        super(SeismicScannerItem.TITLE);

        this.stack = stack;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    protected void init() {
        super.init();
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;

        this.fluidPockets.clear();
        if(this.stack.contains(ComponentTypeInit.FLUID_POCKETS)) {
            this.fluidPockets.addAll(this.stack.get(ComponentTypeInit.FLUID_POCKETS).pockets());
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, this.title, this.width / 2 - this.textRenderer.getWidth(this.title) / 2, this.y + 8, 0x404040, false);

        if (this.fluidPockets.isEmpty()) {
            renderDarkening(context);
            context.drawText(this.textRenderer, Text.literal("No fluid pockets found"), this.width / 2, this.height / 2, 0xFFFFFF, true);
            return;
        }

        if(this.client == null)
            return;

        // Render fluid pockets
        PlayerEntity player = this.client.player;

        if (player == null)
            return;


        Matrix3x2fStack matrixStack = context.getMatrices();
        for (WorldFluidPocketsState.FluidPocket fluidPocket : this.fluidPockets) {
            BlockState blockState = fluidPocket.fluidState().getBlockState();
            Set<BlockPos> positions = fluidPocket.fluidPositions().keySet();

            int minX = fluidPocket.minX();
            int minY = fluidPocket.minY();
            int maxX = fluidPocket.maxX();
            int maxY = fluidPocket.maxY();

            int centerX = (minX + maxX) / 2 - minX;
            int centerY = (minY + maxY) / 2 - minY;

            matrixStack.pushMatrix();
            matrixStack.translate(centerX, centerY);
            matrixStack.rotate(135 * MathHelper.RADIANS_PER_DEGREE);
            matrixStack.rotate(-45 * MathHelper.RADIANS_PER_DEGREE);
            matrixStack.translate(-centerX, -centerY);
            for (BlockPos pos : positions) {
                int x = pos.getX() - fluidPocket.minX();
                int y = pos.getY() - fluidPocket.minY();
                int z = -(pos.getZ() - fluidPocket.minZ());

//                new BlockStateScreenElement(blockState)
//                        .scale(12)
//                        .localPos(x, y, z)
//                        .pos(this.x + 96, this.y - 550)
//                        .render(context, mouseX, mouseY, delta);
            }
            matrixStack.popMatrix();
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.client != null && this.client.player != null) {
            ItemStack stack = this.client.player.getMainHandStack();
            if (!stack.isOf(this.stack.getItem())) {
                close();
            }
        } else {
            close();
        }
    }
}
