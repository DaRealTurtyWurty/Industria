package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.multiblock.VariedBlockList;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldScene;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldSceneBuilder;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BlockEntryListWidget extends AbstractSelectionList<BlockEntryListWidget.Entry> {
    private static final int ICON_SIZE = 16;
    private Consumer<Entry> selectionListener = entry -> {
    };

    public BlockEntryListWidget(Minecraft client, int x, int y, int width, int height, int itemHeight) {
        super(client, width, height, y, itemHeight);
        setX(x);
    }

    @Override
    public int getRowLeft() {
        return getX();
    }

    @Override
    public int getRowWidth() {
        return this.width - SCROLLBAR_WIDTH - 8;
    }

    @Override
    protected int scrollBarX() {
        return getX() + this.width - SCROLLBAR_WIDTH;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }

    @Override
    public void setSelected(Entry entry) {
        super.setSelected(entry);
        this.selectionListener.accept(entry);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
            Entry entry = getEntryAtPosition(click.x(), click.y());
            if (entry != null) {
                setSelected(entry);
                setFocused(entry);
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public void removeEntry(Entry entry) {
        if (entry == getSelected()) {
            setSelected(null);
        }
    }

    public void setSelectionListener(Consumer<Entry> selectionListener) {
        this.selectionListener = selectionListener == null
                ? entry -> {}
                : selectionListener;
    }

    @Override
    public void clearEntries() {
        super.clearEntries();
        setSelected(null);
    }

    public void tickEntries() {
        for (Entry entry : children()) {
            entry.tick();
        }
    }

    public void closeEntries() {
        for (Entry entry : children()) {
            entry.close();
        }
    }

    public int addBlock(Block block) {
        return addEntry(new BlockEntry(this.minecraft, block));
    }

    public int addBlockState(BlockState state) {
        return addEntry(new BlockStateEntry(this.minecraft, state));
    }

    public int addBlockTag(TagKey<Block> tagKey) {
        return addEntry(new BlockTagEntry(this.minecraft, tagKey));
    }

    public abstract static class Entry extends AbstractSelectionList.Entry<Entry> implements AutoCloseable {
        protected final Minecraft client;
        private final Component primaryText;
        private final Component secondaryText;
        private final FakeWorldScene scene;

        protected Entry(Minecraft client, Consumer<FakeWorldSceneBuilder.SceneContext> populator, Component primaryText, Component secondaryText) {
            this.client = client;
            this.primaryText = primaryText;
            this.secondaryText = secondaryText;

            this.scene = FakeWorldSceneBuilder.create()
                    .camera(new Vec3(-3.0, 2.5, 3.0), 225.0F, 25.0F)
                    .populate(populator)
                    .build();
            this.scene.setScaleMultiplier(2.5F);
        }

        @Override
        public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int renderSize = Math.max(20, Math.min(getHeight() - 2, ICON_SIZE + 14));
            this.scene.setAnchor(BlockPos.ZERO, renderSize / 2, renderSize / 2);
            int previewX = getX() + 6;
            int previewY = getY() + (getHeight() - renderSize) / 2;
            this.scene.render(context, previewX, previewY, renderSize, renderSize, deltaTicks);

            int textX = previewX + renderSize + 2;
            int primaryY = getY() + 2;
            int secondaryY = primaryY + this.client.font.lineHeight + 2;

            ScreenUtils.drawTextTruncated(context, this.primaryText, textX, primaryY, getWidth() - ICON_SIZE, 0xFFFFFFFF, false);
            if (!this.secondaryText.getString().isEmpty()) {
                context.pose().pushMatrix();
                context.pose().translate(textX, secondaryY);
                context.pose().scale(0.875F, 0.875F);
                ScreenUtils.drawTextTruncated(context, this.secondaryText, 0, 0, (int) ((getWidth() - ICON_SIZE) / 0.875f), 0xFFAAAAAA, false);
                context.pose().popMatrix();
            }
        }



        // TODO: Unsure if this is needed anymore
//        @Override
//        public void drawBorder(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
//            int left = x + 7;
//            int right = x + entryWidth + 6;
//            int top = y - 2;
//            int bottom = y + entryHeight + 2;
//            int hoverColor = 0x66000000;
//
//            if (hovered) {
//                context.fill(left, top, right, bottom, hoverColor);
//            }
//        }

        public void tick() {
            this.scene.tick();
        }

        @Override
        public void close() {
            this.scene.close();
        }
    }

    public static class BlockEntry extends Entry {
        private final Block block;

        public BlockEntry(Minecraft client, Block block) {
            super(client, ctx -> populateScene(block, ctx),
                    block.getName(), Component.literal(BuiltInRegistries.BLOCK.getKey(block).toString()));
            this.block = block;
        }

        private static void populateScene(Block block, FakeWorldSceneBuilder.SceneContext ctx) {
            ctx.addBlock(BlockPos.ZERO, block.defaultBlockState());
        }

        public Block getBlock() {
            return this.block;
        }
    }

    public static class BlockStateEntry extends Entry {
        private final BlockState blockState;

        public BlockStateEntry(Minecraft client, BlockState blockState) {
            super(client, ctx -> populateScene(blockState, ctx),
                    blockState.getBlock().getName(), formatProperties(blockState));
            this.blockState = blockState;
        }

        private static void populateScene(BlockState state, FakeWorldSceneBuilder.SceneContext ctx) {
            ctx.addBlock(BlockPos.ZERO, state);
        }

        public BlockState getBlockState() {
            return this.blockState;
        }
    }

    public static class BlockTagEntry extends Entry {
        private final TagKey<Block> tagKey;

        public BlockTagEntry(Minecraft client, TagKey<Block> tagKey) {
            super(client, ctx -> populateScene(tagKey, ctx),
                    Component.literal("#" + tagKey.location()), describeTag(tagKey));
            this.tagKey = tagKey;
        }

        private static void populateScene(TagKey<Block> tagKey, FakeWorldSceneBuilder.SceneContext ctx) {
            VariedBlockList variedBlockList = VariedBlockList.Builder.create()
                    .addTag(tagKey)
                    .build();
            ctx.addVariedBlockList(BlockPos.ZERO, variedBlockList);
        }

        public TagKey<Block> getTagKey() {
            return this.tagKey;
        }
    }

    private static Component describeTag(TagKey<Block> tagKey) {
        Optional<HolderSet.Named<Block>> list = BuiltInRegistries.BLOCK.get(tagKey);
        if (list.isEmpty())
            return Component.literal("Empty tag"); // TODO: localization

        int count = 0;
        for (Holder<Block> ignored : list.get()) {
            count++;
        }

        String descriptor = count == 1 ? "1 block" : count + " blocks"; // TODO: localization
        return Component.literal(descriptor);
    }

    private static Component formatProperties(BlockState state) {
        Map<Property<?>, Comparable<?>> entries = state.getValues();
        if (entries.isEmpty())
            return Component.empty();

        String formatted = entries.entrySet().stream()
                .map(entry -> entry.getKey().getName() + "=" + formatProperty(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
        return Component.literal(formatted);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String formatProperty(Property property, Comparable<?> value) {
        return property.getName(value);
    }
}
