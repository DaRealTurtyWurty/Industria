package dev.turtywurty.industria.screen.widget;

import dev.turtywurty.industria.multiblock.VariedBlockList;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldScene;
import dev.turtywurty.industria.screen.fakeworld.FakeWorldSceneBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

public class PaletteEntryListWidget extends EntryListWidget<PaletteEntryListWidget.Entry> {
    private final Map<Character, Entry> entriesByChar = new HashMap<>();
    private Consumer<Entry> selectionListener = entry -> {
    };

    public PaletteEntryListWidget(MinecraftClient client, int x, int y, int width, int height, int itemHeight) {
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
    public void setSelected(Entry entry) {
        super.setSelected(entry);
        this.selectionListener.accept(entry);
    }

    @Override
    public int addEntry(Entry entry) {
        Entry existing = this.entriesByChar.get(entry.getPaletteChar());
        if (existing != null) {
            existing.updateFrom(entry);
            entry.close();
            return this.children().indexOf(existing);
        }

        this.entriesByChar.put(entry.getPaletteChar(), entry);
        return super.addEntry(entry);
    }

    public void addOrUpdateEntry(char paletteChar, String name, VariedBlockList variedBlockList) {
        Entry entry = this.entriesByChar.get(paletteChar);
        if (entry != null) {
            entry.update(name, variedBlockList);
            return;
        }

        entry = new Entry(this.client, paletteChar, name, variedBlockList);
        addEntry(entry);
    }

    public void setSelectionListener(Consumer<Entry> selectionListener) {
        this.selectionListener = selectionListener == null ? entry -> {
        } : selectionListener;
    }

    public Entry getEntry(char paletteChar) {
        return this.entriesByChar.get(paletteChar);
    }

    public void removeEntriesNotIn(Set<Character> paletteChars) {
        List<Entry> toRemove = children().stream()
                .filter(entry -> !paletteChars.contains(entry.getPaletteChar()))
                .toList();

        for (Entry entry : toRemove) {
            removeEntry(entry);
            entry.close();
            if (entry == getSelectedOrNull()) {
                setSelected(null);
            }
        }
    }

    public void removeEntry(char paletteChar) {
        Entry entry = this.entriesByChar.remove(paletteChar);
        if (entry != null) {
            super.removeEntry(entry);
            entry.close();
            if (entry == getSelectedOrNull()) {
                setSelected(null);
            }
        }
    }

    @Override
    public void clearEntries() {
        List<Entry> entries = List.copyOf(children());
        super.clearEntries();
        this.entriesByChar.clear();
        entries.forEach(Entry::close);
    }

    @Override
    public void removeEntry(Entry entry) {
        this.entriesByChar.remove(entry.getPaletteChar(), entry);
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
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
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

    public static class Entry extends EntryListWidget.Entry<Entry> implements AutoCloseable {
        private static final int PREVIEW_PADDING = 2;
        private final FakeWorldScene scene;
        private final MinecraftClient client;
        private final char paletteChar;
        private VariedBlockList variedBlockList;
        private Text name;

        public Entry(MinecraftClient client, char paletteChar, String name, VariedBlockList variedBlockList) {
            this.client = client;
            this.paletteChar = paletteChar;
            this.name = Text.literal(name);
            this.variedBlockList = variedBlockList;
            this.scene = FakeWorldSceneBuilder.create()
                    .camera(new Vec3d(-3.0, 2.5, 3.0), 225.0F, 25.0F)
                    .populate(ctx -> ctx.addVariedBlockList(BlockPos.ORIGIN, variedBlockList))
                    .build();
            this.scene.setScaleMultiplier(2.5F);
        }

        public void tick() {
            this.scene.tick();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            int previewSize = Math.max(22, getHeight() - PREVIEW_PADDING * 2);
            int previewX = getX() + PREVIEW_PADDING + 6;
            int previewY = getY() + (getHeight() - previewSize) / 2;
            this.scene.setAnchor(BlockPos.ORIGIN, previewSize / 2, previewSize / 2);
            this.scene.render(context, previewX, previewY, previewSize, previewSize, deltaTicks);

            String charText = this.paletteChar == ' ' ? "_" : String.valueOf(this.paletteChar);
            int charX = previewX + previewSize + 2;
            int textY = getY() + (getHeight() - this.client.textRenderer.fontHeight) / 2 + 2;
            context.drawText(this.client.textRenderer, charText, charX, textY, 0xFFFFFFFF, false);

            int nameX = charX + this.client.textRenderer.getWidth(charText) + 6;
            int maxNameWidth = Math.max(0, getX() + getWidth() - nameX - 4);
            String displayName = this.name.getString();
            if (this.client.textRenderer.getWidth(displayName) > maxNameWidth && maxNameWidth > this.client.textRenderer.getWidth("...")) {
                int ellipsisWidth = this.client.textRenderer.getWidth("...");
                displayName = this.client.textRenderer.trimToWidth(displayName, maxNameWidth - ellipsisWidth) + "...";
            }

            context.drawText(this.client.textRenderer, displayName, nameX, textY, hovered ? 0xFFFFFFFF : 0xFFDDDDDD, false);
        }

        public void updateFrom(Entry other) {
            update(other.name.getString(), other.variedBlockList);
        }

        public void update(String name, VariedBlockList variedBlockList) {
            boolean variedBlockListChanged = !Objects.equals(this.variedBlockList, variedBlockList);
            boolean nameChanged = !Objects.equals(this.name.getString(), name);
            if (!variedBlockListChanged && !nameChanged)
                return;

            this.name = Text.literal(name);
            this.variedBlockList = variedBlockList;
            if (variedBlockListChanged) {
                this.scene.addVariedBlockList(BlockPos.ORIGIN, variedBlockList);
            }
        }

        public char getPaletteChar() {
            return this.paletteChar;
        }

        public String getName() {
            return this.name.getString();
        }

        public VariedBlockList getVariedBlockList() {
            return this.variedBlockList;
        }

        @Override
        public void close() {
            this.scene.close();
        }

        // TODO: Not sure that this is still needed
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
    }
}
