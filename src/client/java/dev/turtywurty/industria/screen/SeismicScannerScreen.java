package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.ComponentTypeInit;
import dev.turtywurty.industria.item.SeismicScannerItem;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.util.BlockStateScreenElement;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class SeismicScannerScreen extends Screen {
    private static final Identifier TEXTURE = Industria.id("textures/gui/seismic_scanner.png");

    private final int backgroundWidth = 176;
    private final int backgroundHeight = 166;
    private int x, y;

    private final ItemStack stack;

    public SeismicScannerScreen(ItemStack stack) {
        super(SeismicScannerItem.TITLE);

        this.stack = stack;
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, this.title, this.width / 2 - this.textRenderer.getWidth(this.title) / 2, this.y + 8, 0x404040, false);

        List<WorldFluidPocketsState.FluidPocket> fluidPockets = this.stack.get(ComponentTypeInit.FLUID_POCKETS);
        if (fluidPockets == null || fluidPockets.isEmpty()) {
            renderDarkening(context);
            context.drawText(this.textRenderer, Text.literal("No fluid pockets found"), this.width / 2, this.height / 2, 0xFFFFFF, true);
            return;
        }

        if(this.client == null)
            return;

        // Render fluid pockets
        BlockRenderManager blockRenderManager = this.client.getBlockRenderManager();
        World world = this.client.world;
        PlayerEntity player = this.client.player;

        if (world == null || player == null)
            return;

        for (WorldFluidPocketsState.FluidPocket fluidPocket : fluidPockets) {
            FluidState fluidState = fluidPocket.fluidState();
            BlockState blockState = fluidState.getBlockState();
            List<BlockPos> positions = fluidPocket.fluidPositions();

            for (BlockPos pos : positions) {
                int x, y, z;

                x = (pos.getX() - player.getBlockPos().getX());
                y = (pos.getZ() - player.getBlockPos().getZ());
                z = (pos.getY() - player.getBlockPos().getY());

                new BlockStateScreenElement(blockState)
                        .scale(8)
                        .localPos(x, y, z)
                        .rotateAroundCenter(22.5, 45, 0)
                        .pos(this.x + 128, this.y + 128)
                        .render(context, mouseX, mouseY, delta);
            }
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
