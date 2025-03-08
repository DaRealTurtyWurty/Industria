package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.EnergyWidget;
import dev.turtywurty.industria.screenhandler.MixerScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.World;

public class MixerScreen extends HandledScreen<MixerScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/mixer.png");

    public MixerScreen(MixerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundWidth = 200;
        this.backgroundHeight = 194;
        this.playerInventoryTitleX = 20;
        this.playerInventoryTitleY = this.backgroundHeight - 92;
    }

    @Override
    protected void init() {
        super.init();

        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;

        addDrawable(new EnergyWidget.Builder(this.handler.getBlockEntity().getEnergyStorage())
                .bounds(this.x + 8, this.y + 10, 16, 89)
                .color(0xFFD4AF37)
                .build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(context, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        ScreenUtils.drawTexture(context, TEXTURE, this.x + 113, this.y + 36, 200, 0, this.handler.getProgressScaled(), 17);

        drawInputTank(context);
        drawOutputTank(context);
    }

    private void drawInputTank(DrawContext context) {
        SingleFluidStorage inputTank = this.handler.getBlockEntity().getInputFluidTank();
        Fluid fluid = inputTank.variant.getFluid();
        long fluidAmount = inputTank.getAmount();
        long fluidCapacity = inputTank.getCapacity();
        int fluidBarHeight = Math.round((float) fluidAmount / fluidCapacity * 66);

        FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if (fluidRenderHandler == null || fluidAmount <= 0)
            return;

        BlockPos pos = this.handler.getBlockEntity().getPos();
        FluidState fluidState = fluid.getDefaultState();
        World world = this.handler.getBlockEntity().getWorld();

        Sprite stillTexture = fluidRenderHandler.getFluidSprites(world, pos, fluidState)[0];
        int tintColor = fluidRenderHandler.getFluidColor(world, pos, fluidState);

        float red = (tintColor >> 16 & 0xFF) / 255.0F;
        float green = (tintColor >> 8 & 0xFF) / 255.0F;
        float blue = (tintColor & 0xFF) / 255.0F;
        context.drawSpriteStretched(RenderLayer::getGuiTextured, stillTexture, this.x + 34, this.y + 10 + 66 - fluidBarHeight, 20, fluidBarHeight, ColorHelper.fromFloats(1.0F, red, green, blue));
    }

    private void drawOutputTank(DrawContext context) {
        SingleFluidStorage outputTank = this.handler.getBlockEntity().getOutputFluidTank();
        Fluid fluid = outputTank.variant.getFluid();
        long fluidAmount = outputTank.getAmount();
        long fluidCapacity = outputTank.getCapacity();
        int fluidBarHeight = Math.round((float) fluidAmount / fluidCapacity * 66);

        FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if (fluidRenderHandler == null || fluidAmount <= 0)
            return;

        BlockPos pos = this.handler.getBlockEntity().getPos();
        FluidState fluidState = fluid.getDefaultState();
        World world = this.handler.getBlockEntity().getWorld();

        Sprite stillTexture = fluidRenderHandler.getFluidSprites(world, pos, fluidState)[0];
        int tintColor = fluidRenderHandler.getFluidColor(world, pos, fluidState);

        float red = (tintColor >> 16 & 0xFF) / 255.0F;
        float green = (tintColor >> 8 & 0xFF) / 255.0F;
        float blue = (tintColor & 0xFF) / 255.0F;
        context.drawSpriteStretched(RenderLayer::getGuiTextured, stillTexture, this.x + 168, this.y + 10 + 66 - fluidBarHeight, 20, fluidBarHeight, ColorHelper.fromFloats(1.0F, red, green, blue));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        SingleFluidStorage tank = null;
        Fluid fluid = null;
        if (isPointWithinBounds(34, 10, 20, 66, mouseX, mouseY)) {
            tank = this.handler.getBlockEntity().getInputFluidTank();
            fluid = tank.variant.getFluid();
        } else if (isPointWithinBounds(168, 10, 20, 66, mouseX, mouseY)) {
            tank = this.handler.getBlockEntity().getOutputFluidTank();
            fluid = tank.variant.getFluid();
        }

        if(tank != null) {
            long fluidAmount = tank.getAmount();
            long fluidCapacity = tank.getCapacity();

            if (fluid != null && fluidAmount > 0) {
                context.drawTooltip(this.textRenderer, Text.translatable(fluid.getDefaultState().getBlockState().getBlock().getTranslationKey()), mouseX, mouseY);
                context.drawTooltip(this.textRenderer, Text.literal(((int) (((float) fluidAmount / FluidConstants.BUCKET) * 1000)) + " / " + ((int) (((float) fluidCapacity / FluidConstants.BUCKET) * 1000)) + " mB"), mouseX, mouseY + 10);
            }
        }
    }
}
