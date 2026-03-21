package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.screen.widget.AutoCompleteEditBox;
import dev.turtywurty.industria.screen.widget.AutoCompleteProvider;
import dev.turtywurty.industria.screen.widget.ToggleButton;
import dev.turtywurty.industria.screenhandler.DetectorConveyorScreenHandler;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

public class DetectorConveyorScreen extends AbstractContainerScreen<DetectorConveyorScreenHandler> implements AutoCompleteProvider<TagKey<Item>> {
    public static final Component BLACKLIST_MODE_LABEL = Component.translatable("container." + Industria.MOD_ID + ".detector_conveyor.blacklist_mode");
    public static final Component MATCH_DURABILITY_LABEL = Component.translatable("container." + Industria.MOD_ID + ".detector_conveyor.match_durability");
    public static final Component MATCH_ENCHANTMENTS_LABEL = Component.translatable("container." + Industria.MOD_ID + ".detector_conveyor.match_enchantments");
    public static final Component MATCH_COMPONENTS_LABEL = Component.translatable("container." + Industria.MOD_ID + ".detector_conveyor.match_components");

    private static final Identifier TEXTURE_STACK_MODE = Industria.id("textures/gui/container/filter_conveyor_stack_mode.png");
    private static final Identifier TEXTURE_TAG_MODE = Industria.id("textures/gui/container/filter_conveyor_tag_mode.png");

    private AutoCompleteEditBox<TagKey<Item>> tagFilterEditBox;

    public DetectorConveyorScreen(DetectorConveyorScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 190);
    }

    @Override
    protected void init() {
        super.init();
        this.tagFilterEditBox = null;

        addRenderableWidget(new ToggleButton.Builder(this.leftPos + 8, this.topPos + 16)
                .toggledByDefault(this.menu.isTagFiltering())
                .textures(ToggleButton.DEFAULT_COLOURED_TEXTURES)
                .onPress((_, pressed) -> {
                    this.menu.setTagFiltering(pressed);
                    rebuildWidgets();
                })
                .build());

        if (this.menu.isTagFiltering()) {
            initTagKeyFilteringWidgets();
        } else {
            initStackFilteringWidgets();
        }
    }

    private void initTagKeyFilteringWidgets() {
        addRenderableWidget(Checkbox.builder(BLACKLIST_MODE_LABEL, this.font)
                .onValueChange((_, selected) -> this.menu.setBlacklistMode(selected))
                .selected(this.menu.isBlacklistMode())
                .pos(this.leftPos + 62, this.topPos + 16)
                .maxWidth(88)
                .build());

        this.tagFilterEditBox = addRenderableWidget(AutoCompleteEditBox.builder(this.font, this)
                .bounds(this.leftPos + 8, this.topPos + 36, 128, 20)
                .maxVisibleSuggestions(10)
                .value(this.menu.getFilterTag() == null ? "" : this.menu.getFilterTag().location().toString())
                .responder(string -> {
                    string = string.trim().replaceFirst("^#", "");
                    if (string.isBlank())
                        return;

                    Identifier identifier = Identifier.tryParse(string);
                    if (identifier == null)
                        return;

                    TagKey<Item> tagKey = TagKey.create(Registries.ITEM, identifier);
                    BuiltInRegistries.ITEM.listTagIds()
                            .filter(existingTagKey -> existingTagKey.equals(tagKey))
                            .findFirst()
                            .ifPresent(this.menu::setFilterTag);
                })
                .build());
    }

    @Override
    protected void setInitialFocus() {
        if (this.menu.isTagFiltering() && this.tagFilterEditBox != null) {
            setInitialFocus(this.tagFilterEditBox);
            return;
        }

        super.setInitialFocus();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape())
            return super.keyPressed(event);

        if (this.tagFilterEditBox != null)
            return this.tagFilterEditBox.keyPressed(event)
                    || this.tagFilterEditBox.canConsumeInput()
                    || super.keyPressed(event);

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.tagFilterEditBox != null
                && this.tagFilterEditBox.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
            return true;

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.tagFilterEditBox != null && !this.tagFilterEditBox.isMouseOver(event.x(), event.y())) {
            this.tagFilterEditBox.dismissSuggestions();
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected boolean isHovering(int left, int top, int width, int height, double mouseX, double mouseY) {
        if (this.tagFilterEditBox != null && this.tagFilterEditBox.isMouseOverSuggestions(mouseX, mouseY))
            return false;

        return super.isHovering(left, top, width, height, mouseX, mouseY);
    }

    private void initStackFilteringWidgets() {
        addRenderableWidget(Checkbox.builder(BLACKLIST_MODE_LABEL, this.font)
                .onValueChange((_, selected) -> this.menu.setBlacklistMode(selected))
                .selected(this.menu.isBlacklistMode())
                .pos(this.leftPos + 62, this.topPos + 18)
                .maxWidth(88)
                .build());

        addRenderableWidget(Checkbox.builder(MATCH_DURABILITY_LABEL, this.font)
                .onValueChange((_, selected) -> this.menu.setMatchDurability(selected))
                .selected(this.menu.isMatchDurability())
                .pos(this.leftPos + 62, this.topPos + 37)
                .maxWidth(88)
                .build());

        addRenderableWidget(Checkbox.builder(MATCH_ENCHANTMENTS_LABEL, this.font)
                .onValueChange((_, selected) -> this.menu.setMatchEnchantments(selected))
                .selected(this.menu.isMatchEnchantments())
                .pos(this.leftPos + 62, this.topPos + 56)
                .maxWidth(88)
                .build());

        addRenderableWidget(Checkbox.builder(MATCH_COMPONENTS_LABEL, this.font)
                .onValueChange((_, selected) -> this.menu.setMatchComponents(selected))
                .selected(this.menu.isMatchComponents())
                .pos(this.leftPos + 62, this.topPos + 75)
                .maxWidth(88)
                .build());
    }

    @Override
    public Collection<TagKey<Item>> getSuggestions(String input) {
        Stream<TagKey<Item>> tagKeyStream = BuiltInRegistries.ITEM.listTagIds();
        return tagKeyStream.filter(tagKey -> tagKey.location().toString().contains(input)).toList();
    }

    @Override
    public Function<TagKey<Item>, String> suggestionToString() {
        return tagKey -> "#" + tagKey.location();
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.menu.isTagFiltering())
            return super.mouseReleased(event);

        ItemStack carriedStack = this.menu.getCarried();
        if (isMouseOverPosition(event.x(), event.y(), this.leftPos + 8, this.topPos + 47, 16, 16)) {
            if (carriedStack.isEmpty()) {
                this.menu.setFilterStack(ItemStack.EMPTY);
                return super.mouseReleased(event);
            }

            this.menu.setFilterStack(carriedStack);
            return super.mouseReleased(event);
        }

        return super.mouseReleased(event);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float tickDelta, int mouseX, int mouseY) {
        ScreenUtils.drawTexture(graphics,
                this.menu.isTagFiltering() ? TEXTURE_TAG_MODE : TEXTURE_STACK_MODE,
                this.leftPos, this.topPos,
                0, 0,
                this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderSlots(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderSlots(graphics, mouseX, mouseY);
        if (this.menu.isTagFiltering())
            return;

        int x = 8;
        int y = 47;
        if (isMouseOverPosition(mouseX, mouseY, this.leftPos + x, this.topPos + y, 16, 16)) {
            graphics.fill(x, y, x + 16, y + 16, -2130706433);
        }

        if (!this.menu.getFilterStack().isEmpty()) {
            renderFloatingItem(graphics, this.menu.getFilterStack(), x, y, "");
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        super.render(graphics, mouseX, mouseY, tickDelta);
        if (this.menu.isTagFiltering())
            return;

        if (isMouseOverPosition(mouseX, mouseY, this.leftPos + 8, this.topPos + 47, 16, 16) && !this.menu.getFilterStack().isEmpty()) {
            ItemStack item = this.menu.getFilterStack();
            graphics.setTooltipForNextFrame(
                    this.font,
                    getTooltipFromContainerItem(item),
                    item.getTooltipImage(),
                    mouseX, mouseY,
                    item.get(DataComponents.TOOLTIP_STYLE)
            );
        }
    }

    private static boolean isMouseOverPosition(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
