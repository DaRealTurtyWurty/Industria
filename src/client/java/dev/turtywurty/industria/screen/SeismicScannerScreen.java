package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.ComponentTypeInit;
import dev.turtywurty.industria.item.SeismicScannerItem;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
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
        if(this.stack.has(ComponentTypeInit.FLUID_POCKETS)) {
            this.fluidPockets.addAll(this.stack.get(ComponentTypeInit.FLUID_POCKETS).pockets());
        }
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawString(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, this.y + 8, 0x404040, false);

        if (this.fluidPockets.isEmpty()) {
            renderMenuBackground(context);
            context.drawString(this.font, Component.literal("No fluid pockets found"), this.width / 2, this.height / 2, 0xFFFFFF, true);
            return;
        }

        if(this.minecraft == null)
            return;

        // Render fluid pockets
        Player player = this.minecraft.player;

        if (player == null)
            return;


        Matrix3x2fStack matrixStack = context.pose();
        for (WorldFluidPocketsState.FluidPocket fluidPocket : this.fluidPockets) {
            BlockState blockState = fluidPocket.fluidState().createLegacyBlock();
            Set<BlockPos> positions = fluidPocket.fluidPositions().keySet();

            int minX = fluidPocket.minX();
            int minY = fluidPocket.minY();
            int maxX = fluidPocket.maxX();
            int maxY = fluidPocket.maxY();

            int centerX = (minX + maxX) / 2 - minX;
            int centerY = (minY + maxY) / 2 - minY;

            matrixStack.pushMatrix();
            matrixStack.translate(centerX, centerY);
            matrixStack.rotate(135 * Mth.DEG_TO_RAD);
            matrixStack.rotate(-45 * Mth.DEG_TO_RAD);
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
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack stack = this.minecraft.player.getMainHandItem();
            if (!stack.is(this.stack.getItem())) {
                onClose();
            }
        } else {
            onClose();
        }
    }
}
