package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.multiblock.VariedBlockList;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldScene;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldSceneBuilder;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BlockEntryListWidget extends EntryListWidget<BlockEntryListWidget.Entry> {
    private static final int ICON_SIZE = 16;
    private Consumer<Entry> selectionListener = entry -> {
    };

    public BlockEntryListWidget(MinecraftClient client, int x, int y, int width, int height, int itemHeight) {
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
    protected int getScrollbarX() {
        return getX() + this.width - SCROLLBAR_WIDTH;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public void setSelected(Entry entry) {
        super.setSelected(entry);
        this.selectionListener.accept(entry);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
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
        if (entry == getSelectedOrNull()) {
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
        return addEntry(new BlockEntry(this.client, block));
    }

    public int addBlockState(BlockState state) {
        return addEntry(new BlockStateEntry(this.client, state));
    }

    public int addBlockTag(TagKey<Block> tagKey) {
        return addEntry(new BlockTagEntry(this.client, tagKey));
    }

    public abstract static class Entry extends EntryListWidget.Entry<Entry> implements AutoCloseable {
        protected final MinecraftClient client;
        private final Text primaryText;
        private final Text secondaryText;
        private final FakeWorldScene scene;

        protected Entry(MinecraftClient client, Consumer<FakeWorldSceneBuilder.SceneContext> populator, Text primaryText, Text secondaryText) {
            this.client = client;
            this.primaryText = primaryText;
            this.secondaryText = secondaryText;

            this.scene = FakeWorldSceneBuilder.create()
                    .camera(new Vec3d(-3.0, 2.5, 3.0), 225.0F, 25.0F)
                    .populate(populator)
                    .build();
            this.scene.setScaleMultiplier(2.5F);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int renderSize = Math.max(20, Math.min(getHeight() - 2, ICON_SIZE + 14));
            this.scene.setAnchor(BlockPos.ORIGIN, renderSize / 2, renderSize / 2);
            int previewX = getX() + 6;
            int previewY = getY() + (getHeight() - renderSize) / 2;
            this.scene.render(context, previewX, previewY, renderSize, renderSize, deltaTicks);

            int textX = previewX + renderSize + 2;
            int primaryY = getY() + 2;
            int secondaryY = primaryY + this.client.textRenderer.fontHeight + 2;

            ScreenUtils.drawTextTruncated(context, this.primaryText, textX, primaryY, getWidth() - ICON_SIZE, 0xFFFFFFFF, false);
            if (!this.secondaryText.getString().isEmpty()) {
                context.getMatrices().pushMatrix();
                context.getMatrices().translate(textX, secondaryY);
                context.getMatrices().scale(0.875F, 0.875F);
                ScreenUtils.drawTextTruncated(context, this.secondaryText, 0, 0, (int) ((getWidth() - ICON_SIZE) / 0.875f), 0xFFAAAAAA, false);
                context.getMatrices().popMatrix();
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

        public BlockEntry(MinecraftClient client, Block block) {
            super(client, ctx -> populateScene(block, ctx),
                    block.getName(), Text.literal(Registries.BLOCK.getId(block).toString()));
            this.block = block;
        }

        private static void populateScene(Block block, FakeWorldSceneBuilder.SceneContext ctx) {
            ctx.addBlock(BlockPos.ORIGIN, block.getDefaultState());
        }

        public Block getBlock() {
            return this.block;
        }
    }

    public static class BlockStateEntry extends Entry {
        private final BlockState blockState;

        public BlockStateEntry(MinecraftClient client, BlockState blockState) {
            super(client, ctx -> populateScene(blockState, ctx),
                    blockState.getBlock().getName(), formatProperties(blockState));
            this.blockState = blockState;
        }

        private static void populateScene(BlockState state, FakeWorldSceneBuilder.SceneContext ctx) {
            ctx.addBlock(BlockPos.ORIGIN, state);
        }

        public BlockState getBlockState() {
            return this.blockState;
        }
    }

    public static class BlockTagEntry extends Entry {
        private final TagKey<Block> tagKey;

        public BlockTagEntry(MinecraftClient client, TagKey<Block> tagKey) {
            super(client, ctx -> populateScene(tagKey, ctx),
                    Text.literal("#" + tagKey.id()), describeTag(tagKey));
            this.tagKey = tagKey;
        }

        private static void populateScene(TagKey<Block> tagKey, FakeWorldSceneBuilder.SceneContext ctx) {
            VariedBlockList variedBlockList = VariedBlockList.Builder.create()
                    .addTag(tagKey)
                    .build();
            ctx.addVariedBlockList(BlockPos.ORIGIN, variedBlockList);
        }

        public TagKey<Block> getTagKey() {
            return this.tagKey;
        }
    }

    private static Text describeTag(TagKey<Block> tagKey) {
        Optional<RegistryEntryList.Named<Block>> list = Registries.BLOCK.getOptional(tagKey);
        if (list.isEmpty())
            return Text.literal("Empty tag"); // TODO: localization

        int count = 0;
        for (RegistryEntry<Block> ignored : list.get()) {
            count++;
        }

        String descriptor = count == 1 ? "1 block" : count + " blocks"; // TODO: localization
        return Text.literal(descriptor);
    }

    private static Text formatProperties(BlockState state) {
        Map<Property<?>, Comparable<?>> entries = state.getEntries();
        if (entries.isEmpty())
            return Text.empty();

        String formatted = entries.entrySet().stream()
                .map(entry -> entry.getKey().getName() + "=" + formatProperty(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
        return Text.literal(formatted);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String formatProperty(Property property, Comparable<?> value) {
        return property.name(value);
    }
}
