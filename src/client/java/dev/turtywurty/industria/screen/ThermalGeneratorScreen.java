package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screenhandler.ThermalGeneratorScreenHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ThermalGeneratorScreen extends HandledScreen<ThermalGeneratorScreenHandler> {
    private static final Identifier TEXTURE = Industria.id("textures/gui/container/thermal_generator.png");

    public ThermalGeneratorScreen(ThermalGeneratorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        long energy = this.handler.getEnergy();
        long maxEnergy = this.handler.getMaxEnergy();
        int energyBarHeight = Math.round((float) energy / maxEnergy * 60);
        context.fill(this.x + 8, this.y + 8 + 60 - energyBarHeight, this.x + 16, this.y + 68, 0xFFD4AF37);

        Fluid fluid = this.handler.getFluid();
        long fluidAmount = this.handler.getFluidAmount();
        long fluidCapacity = this.handler.getFluidCapacity();
        int fluidBarHeight = Math.round((float) fluidAmount / fluidCapacity * 60);

        FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if(fluidRenderHandler == null || fluidAmount <= 0)
            return;

        BlockPos pos = this.handler.getBlockEntity().getPos();
        FluidState fluidState = fluid.getDefaultState();
        World world = this.handler.getBlockEntity().getWorld();

        Sprite stillTexture = fluidRenderHandler.getFluidSprites(world, pos, fluidState)[0];
        int tintColor = fluidRenderHandler.getFluidColor(world, pos, fluidState);

        float red = (tintColor >> 16 & 0xFF) / 255.0F;
        float green = (tintColor >> 8 & 0xFF) / 255.0F;
        float blue = (tintColor & 0xFF) / 255.0F;
        context.drawSprite(this.x + 146, this.y + 8 + 60 - fluidBarHeight, 0, 16, fluidBarHeight, stillTexture, red, green, blue, 1.0F);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        if (isPointWithinBounds(146, 8, 16, 60, mouseX, mouseY)) {
            Fluid fluid = this.handler.getFluid();
            long fluidAmount = this.handler.getFluidAmount();
            long fluidCapacity = this.handler.getFluidCapacity();
            if (fluid != null && fluidAmount > 0) {
                context.drawTooltip(this.textRenderer, Text.translatable(fluid.getDefaultState().getBlockState().getBlock().getTranslationKey()), mouseX, mouseY);
                context.drawTooltip(this.textRenderer, Text.literal(fluidAmount + " / " + fluidCapacity + " mB"), mouseX, mouseY + 10);
            }
        }

        if (isPointWithinBounds(8, 8, 8, 60, mouseX, mouseY)) {
            long energy = this.handler.getEnergy();
            long maxEnergy = this.handler.getMaxEnergy();
            context.drawTooltip(this.textRenderer, Text.literal(energy + " / " + maxEnergy + " FE"), mouseX, mouseY);
        }
    }
}
