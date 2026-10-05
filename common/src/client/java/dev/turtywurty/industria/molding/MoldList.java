package dev.turtywurty.industria.molding;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class MoldList extends ObjectSelectionList<MoldList.MoldEntry> {
    private final Consumer<MoldPattern> onSelected;

    public MoldList(Minecraft minecraft, int x, int y, int width, int height, Consumer<MoldPattern> onSelected) {
        super(minecraft, width, height, y, 14);
        setX(x);
        this.onSelected = onSelected;

        for (MoldPattern pattern : MoldPatterns.ALL) {
            addEntry(new MoldEntry(pattern));
        }
    }

    @Override
    public void setSelected(@Nullable MoldList.MoldEntry selected) {
        if (selected == getSelected())
            return;

        super.setSelected(selected);
        if (selected != null) {
            this.onSelected.accept(selected.pattern);
        }
    }

    @Override
    public int getRowWidth() {
        return getWidth() - 12;
    }

    public static class MoldEntry extends Entry<MoldEntry> {
        private final MoldPattern pattern;
        private final Component label;

        public MoldEntry(MoldPattern pattern) {
            this.pattern = pattern;
            this.label = Component.translatable("mold." + pattern.id().getNamespace() + "." + pattern.id().getPath());
        }

        @Override
        public Component getNarration() {
            return this.label;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
            graphics.text(Minecraft.getInstance().font, this.label, getContentX() + 2, getContentY() + 2, 0xFFFFFFFF, false);
        }

        public MoldPattern getPattern() {
            return this.pattern;
        }
    }
}
