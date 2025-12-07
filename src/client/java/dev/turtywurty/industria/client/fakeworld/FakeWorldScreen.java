package dev.turtywurty.industria.client.fakeworld;

import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Simple GUI that draws the client-only world into a boxed area.
 */
public class FakeWorldScreen extends Screen {
    private FakeWorldScene scene;

    public FakeWorldScreen() {
        super(Text.literal("Fake World Preview"));
    }

    @Override
    protected void init() {
        this.scene = FakeWorldSceneBuilder.create()
                .camera(new Vec3d(2.5, 66.0, 7.0), 200.0F, -18.0F)
                .populate(ctx -> {
                    int y = 63;
                    for (int x = 0; x < 5; x++) {
                        for (int z = 0; z < 5; z++) {
                            boolean border = x == 0 || z == 0 || x == 4 || z == 4;
                            var block = border ? Blocks.POLISHED_DEEPSLATE : Blocks.SMOOTH_QUARTZ;
                            ctx.addBlock(new BlockPos(x, y, z), block.getDefaultState());
                        }
                    }

                    ctx.addBlock(new BlockPos(2, y, 2), Blocks.SEA_LANTERN.getDefaultState());
                    for (int i = 1; i <= 3; i++) {
                        if(i == 2) continue;
                        ctx.addBlock(new BlockPos(2, y + 1, i), Blocks.LIGHT_BLUE_STAINED_GLASS.getDefaultState());
                        ctx.addBlock(new BlockPos(i, y + 1, 2), Blocks.LIGHT_BLUE_STAINED_GLASS.getDefaultState());
                    }
                    ctx.addFluid(new BlockPos(2, y + 1, 2), Fluids.WATER.getDefaultState());

                    ctx.addBlock(new BlockPos(1, y + 1, 1), Blocks.DARK_OAK_FENCE.getDefaultState());
                    ctx.addBlock(new BlockPos(3, y + 1, 3), Blocks.DARK_OAK_FENCE.getDefaultState());
                    ctx.addBlock(new BlockPos(1, y + 2, 1), Blocks.LANTERN.getDefaultState());
                    ctx.addBlock(new BlockPos(3, y + 2, 3), Blocks.SOUL_LANTERN.getDefaultState());

                    var world = ctx.world();
                    var armorStand = new ArmorStandEntity(world, 2.5, y + 1.0, 2.5);
                    armorStand.setCustomName(Text.literal("Client-only entity"));
                    armorStand.setNoGravity(true);
                    armorStand.setInvisible(false);
                    ctx.addEntity(armorStand);
                })
                .onTick(tick -> tick.entities().forEach(entity -> {
                    if (entity instanceof ArmorStandEntity armorStand) {
                        armorStand.setYaw(armorStand.getYaw() + 1.5F);
                        armorStand.setHeadYaw(armorStand.getYaw());
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
    public void close() {
        if (this.scene != null) {
            this.scene.close();
        }
        super.close();
        this.scene = null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.scene != null) {
            int padding = 16;
            int areaX = padding;
            int areaY = padding;
            int areaWidth = this.width - padding * 2;
            int areaHeight = this.height - padding * 2;

            // Keep the centerpiece aligned.
            this.scene.setAnchor(new BlockPos(2, 64, 2), areaWidth / 2, areaHeight / 2);
            this.scene.render(context, areaX, areaY, areaWidth, areaHeight, delta);
            context.drawBorder(areaX, areaY, areaWidth, areaHeight, 0xAAFFFFFF);
            context.drawTextWithShadow(this.textRenderer, Text.literal("Client-only world preview"), areaX + 4, areaY + 4, 0xFFFFFFFF);
            context.drawTextWithShadow(this.textRenderer, Text.literal("Press ESC to return"), areaX + 4, areaY + 16, 0xFFCCCCCC);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scene != null && button == 0) {
            float sensitivity = 0.35F;
            this.scene.rotateCamera((float) (deltaX * sensitivity), (float) (deltaY * sensitivity));
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
