package dev.turtywurty.industria.screen;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.menu.SmelteryControllerMenu;
import dev.turtywurty.industria.screen.widget.FluidWidget;
import dev.turtywurty.industria.screen.widget.util.Orientation;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class SmelteryControllerScreen extends AbstractContainerScreen<SmelteryControllerMenu> {
    public static final Component TEMPERATURE_LABEL = Component.translatable("gui." + Industria.MOD_ID + ".smeltery.temperature");
    public static final Component FORMED_LABEL = Component.translatable("gui." + Industria.MOD_ID + ".smeltery.formed");
    public static final Component INCOMPLETE_LABEL = Component.translatable("gui." + Industria.MOD_ID + ".smeltery.incomplete");
    public static final Component CHAMBERS_LABEL = Component.translatable("gui." + Industria.MOD_ID + ".smeltery.chambers");
    public static final Component CHAMBER_LABEL = Component.translatable("gui." + Industria.MOD_ID + ".smeltery.chamber");
    public static final Component SELECTED_CHAMBER_LABEL = Component.translatable("gui." + Industria.MOD_ID + ".smeltery.selected_chamber");
    public static final Component NO_CHAMBERS_LABEL = Component.translatable("gui." + Industria.MOD_ID + ".smeltery.no_chambers");
    public static final Component SIZE_LABEL = Component.translatable("gui." + Industria.MOD_ID + ".smeltery.size");
    public static final Component EMPTY_LABEL = Component.translatable("gui." + Industria.MOD_ID + ".smeltery.empty");
    public static final Component AMOUNT_LABEL = Component.translatable("gui." + Industria.MOD_ID + ".smeltery.amount");
    public static final Component CAPACITY_LABEL = Component.translatable("gui." + Industria.MOD_ID + ".smeltery.capacity");

    private static final Identifier TEXTURE = Industria.id("textures/gui/smeltery_controller.png");

    private final Button[] chamberButtons = new Button[5];
    private Button previousPageButton, nextPageButton;
    private int page;
    private BlockPos selectedHatchPos;

    public SmelteryControllerScreen(SmelteryControllerMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 300, 190);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        for (int row = 0; row < this.chamberButtons.length; row++) {
            int buttonRow = row;
            chamberButtons[buttonRow] = addRenderableWidget(Button.builder(Component.empty(), _ -> {
                        int index = this.page * 5 + buttonRow;
                        if (index < 0 || index >= this.menu.getChambers().size())
                            return;

                        this.selectedHatchPos = this.menu.getChambers().get(index).hatchPos();
                        refreshChamberButtons();
                    })
                    .pos(this.leftPos + 14, this.topPos + 62 + buttonRow * 20)
                    .size(92, 18)
                    .build());
        }

        this.previousPageButton = addRenderableWidget(Button.builder(Component.literal("<"), _ -> {
                    if (this.page > 0) {
                        this.page--;
                        refreshChamberButtons();
                    }
                })
                .pos(this.leftPos + 14, this.topPos + 164)
                .size(44, 14)
                .build());

        this.nextPageButton = addRenderableWidget(Button.builder(Component.literal(">"), _ -> {
                    if ((this.page + 1) * 5 < this.menu.getChambers().size()) {
                        this.page++;
                        refreshChamberButtons();
                    }
                })
                .pos(this.leftPos + 62, this.topPos + 164)
                .size(44, 14)
                .build());

        refreshChamberButtons();

        addRenderableOnly(new FluidWidget.Builder(
                () -> {
                    SmelteryControllerMenu.SmelteryChamberInfo selectedChamber = getSelectedChamber();
                    if (selectedChamber == null)
                        return FluidStack.EMPTY;

                    return selectedChamber.fluid();
                },
                () -> {
                    SmelteryControllerMenu.SmelteryChamberInfo selectedChamber = getSelectedChamber();
                    if (selectedChamber == null)
                        return 0;

                    return selectedChamber.capacity();
                })
                .bounds(this.leftPos + 129, this.topPos + 139, 154, 31)
                .posSupplier(this.menu::getControllerPos)
                .orientation(Orientation.VERTICAL)
                .build());
    }

    private void refreshChamberButtons() {
        int lastPage = Math.max(0, (this.menu.getChambers().size() - 1) / 5);
        this.page = Math.clamp(this.page, 0, lastPage);

        if (this.menu.getChambers().stream()
                .noneMatch(chamber -> chamber.hatchPos().equals(this.selectedHatchPos))) {
            this.selectedHatchPos = this.menu.getChambers().isEmpty()
                    ? null
                    : this.menu.getChambers().getFirst().hatchPos();
        }

        for (int row = 0; row < this.chamberButtons.length; row++) {
            int index = this.page * 5 + row;

            Button button = chamberButtons[row];
            button.visible = index < this.menu.getChambers().size();
            if (button.visible) {
                button.setMessage(translatable(CHAMBER_LABEL, index + 1));
                button.active = !this.menu.getChambers().get(index).hatchPos().equals(this.selectedHatchPos);
            }
        }

        this.previousPageButton.active = this.page > 0;
        this.nextPageButton.active = this.page < lastPage;
    }

    private SmelteryControllerMenu.SmelteryChamberInfo getSelectedChamber() {
        if (this.selectedHatchPos == null)
            return null;

        return this.menu.getChambers().stream()
                .filter(chamber -> chamber.hatchPos().equals(this.selectedHatchPos))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshChamberButtons();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        ScreenUtils.drawTexture(
                context, TEXTURE,
                this.leftPos, this.topPos,
                0, 0,
                this.imageWidth, this.imageHeight,
                512, 256
        );
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        graphics.text(this.font, this.title, this.titleLabelX, 8, 0xFF404040, false);
        graphics.text(this.font, translatable(TEMPERATURE_LABEL, Math.round(this.menu.getTemperature())), 8, 24, 0xFF404040, false);
        graphics.text(this.font, this.menu.isStructureReady() ? FORMED_LABEL : INCOMPLETE_LABEL, 220, 24, this.menu.isStructureReady() ? 0xFF80D080 : 0xFFFF9090, false);
        graphics.text(this.font, translatable(CHAMBERS_LABEL, this.menu.getChambers().size()), 14, 46, 0xFF404040, false);

        SmelteryControllerMenu.SmelteryChamberInfo selectedChamber = getSelectedChamber();
        if (selectedChamber == null) {
            graphics.text(this.font, NO_CHAMBERS_LABEL, 128, 46, 0xFF404040, false);
        } else {
            graphics.text(this.font, translatable(SELECTED_CHAMBER_LABEL, this.menu.getChambers().indexOf(selectedChamber) + 1), 128, 46, 0xFF404040, false);
            graphics.text(this.font, translatable(SIZE_LABEL, selectedChamber.width(), selectedChamber.height(), selectedChamber.depth()), 128, 64, 0xFF404040, false);

            FluidStack fluid = selectedChamber.fluid();
            graphics.text(this.font, fluid.isEmpty()
                            ? EMPTY_LABEL
                            : Component.translatable(fluid.variant().value().defaultFluidState().createLegacyBlock().getBlock().getDescriptionId()),
                    128, 82, 0xFF404040, false);

            double amountMb = fluid.amount() * 1000.0 / FluidAmounts.BUCKET;
            double capacityMb = selectedChamber.capacity() * 1000.0 / FluidAmounts.BUCKET;
            graphics.text(this.font, translatable(AMOUNT_LABEL, Math.round(amountMb)), 128, 100, 0xFF404040, false);
            graphics.text(this.font, translatable(CAPACITY_LABEL, Math.round(capacityMb)), 128, 114, 0xFF404040, false);
        }
    }

    private static Component translatable(Component key, Object... args) {
        return Component.translatable(
                key.getContents() instanceof TranslatableContents contents ? contents.getKey() : key.getString(),
                args
        );
    }
}
