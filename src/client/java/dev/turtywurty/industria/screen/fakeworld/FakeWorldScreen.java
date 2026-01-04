package dev.turtywurty.industria.screen.fakeworld;

import dev.turtywurty.industria.multiblock.VariedBlockList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/**
 * Simple GUI that draws the client-only world into a boxed area.
 */
public class FakeWorldScreen extends Screen {
    private FakeWorldScene scene;

    public FakeWorldScreen() {
        super(Component.literal("Fake World Preview"));
    }

    @Override
    protected void init() {
        this.scene = FakeWorldSceneBuilder.create()
                .camera(new Vec3(2.5, 66.0, 7.0), 200.0F, -18.0F)
                .populate(ctx -> {
                    int y = 63;
                    for (int x = 0; x < 5; x++) {
                        for (int z = 0; z < 5; z++) {
                            boolean border = x == 0 || z == 0 || x == 4 || z == 4;
                            var block = border ? Blocks.POLISHED_DEEPSLATE : Blocks.SMOOTH_QUARTZ;
                            ctx.addBlock(new BlockPos(x, y, z), block.defaultBlockState());
                        }
                    }

                    ctx.addBlock(new BlockPos(2, y, 2), Blocks.SEA_LANTERN.defaultBlockState());
                    for (int i = 1; i <= 3; i++) {
                        if (i == 2) continue;
                        ctx.addBlock(new BlockPos(2, y + 1, i), Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState());
                        ctx.addBlock(new BlockPos(i, y + 1, 2), Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState());
                    }

                    ctx.addFluid(new BlockPos(3, y + 1, 1), Fluids.WATER.defaultFluidState());

                    ctx.addBlock(new BlockPos(1, y + 1, 1), Blocks.DARK_OAK_FENCE.defaultBlockState());
                    ctx.addBlock(new BlockPos(3, y + 1, 3), Blocks.DARK_OAK_FENCE.defaultBlockState());
                    ctx.addBlock(new BlockPos(1, y + 2, 1), Blocks.LANTERN.defaultBlockState());
                    ctx.addBlock(new BlockPos(3, y + 2, 3), Blocks.SOUL_LANTERN.defaultBlockState());
                    ctx.addBlock(new BlockPos(2, y + 3, 2), Blocks.ENCHANTING_TABLE.defaultBlockState());

                    ctx.addVariedBlockList(new BlockPos(2, y - 1, 2), VariedBlockList.Builder.create()
                            .addTag(BlockTags.TERRACOTTA)
                            .build());

                    var world = ctx.world();
                    var enderDragon = new EnderDragon(EntityType.ENDER_DRAGON, world);
                    enderDragon.snapTo(2.5, y + 5.0, 2.5, 0.0F, 0.0F);
                    enderDragon.setCustomName(Component.literal("Client-only entity"));
                    enderDragon.setNoGravity(true);
                    enderDragon.setInvisible(false);
                    ctx.addEntity(enderDragon);
                })
                .onTick(tick -> tick.entities().forEach(entity -> {
                    if (entity instanceof EnderDragon enderDragon) {
                        enderDragon.setYRot(enderDragon.getYRot() + 1.5F);
                        enderDragon.setYHeadRot(enderDragon.getYRot());
                    }
                }))
                .build();
    }

    @Override
    public void tick() {
        if (this.scene != null) {
            this.scene.tick();
        }
    }

    @Override
    public void onClose() {
        if (this.scene != null) {
            this.scene.close();
        }

        super.onClose();
        this.scene = null;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (this.scene != null) {
            int padding = 16;
            int areaX = padding;
            int areaY = padding;
            int areaWidth = this.width - padding * 2;
            int areaHeight = this.height - padding * 2;

            // Keep the centerpiece aligned.
            this.scene.setAnchor(new BlockPos(2, 64, 2), areaWidth / 2, areaHeight / 2);
            this.scene.render(context, areaX, areaY, areaWidth, areaHeight, delta);
            context.renderOutline(areaX, areaY, areaWidth, areaHeight, 0xAAFFFFFF);
            context.drawString(this.font, Component.literal("Client-only world preview"), areaX + 4, areaY + 4, 0xFFFFFFFF);
            context.drawString(this.font, Component.literal("Press ESC to return"), areaX + 4, areaY + 16, 0xFFCCCCCC);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (this.scene != null && click.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
            float sensitivity = 0.35F;
            this.scene.rotateCamera((float) (offsetX * sensitivity), (float) (offsetY * sensitivity));
            return true;
        }

        return super.mouseDragged(click, offsetX, offsetY);
    }
}
