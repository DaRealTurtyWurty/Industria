package dev.turtywurty.industria.conveyor.block.impl.entity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.IndustriaBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorageHolder;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.FilterConveyorScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class FilterConveyorBlockEntity extends IndustriaBlockEntity implements BlockEntityWithGui<BlockPosPayload>, WrappedContainerStorageHolder, ConveyorFilterAccess {
    public static final Component TITLE = Industria.containerTitle("filter_conveyor");

    // This is purely to make the screen handler happy :)
    private final WrappedContainerStorage<SimpleContainer> wrappedContainerStorage = new WrappedContainerStorage<>();

    private ItemStack filterStack = ItemStack.EMPTY;
    private boolean blacklistMode = false;
    private boolean matchDurability = true;
    private boolean matchEnchantments = true;
    private boolean matchComponents = true;

    private TagKey<Item> filterTag = null;
    private boolean isTagFiltering = false;

    public FilterConveyorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockInit.FILTER_CONVEYOR, BlockEntityTypeInit.FILTER_CONVEYOR, pos, state);
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FilterConveyorScreenHandler(containerId, inventory, this, this.wrappedContainerStorage);
    }

    @Override
    public WrappedContainerStorage<?> getWrappedContainerStorage() {
        return this.wrappedContainerStorage;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("FilterStack", ItemStack.OPTIONAL_CODEC, this.filterStack);
        output.putBoolean("BlacklistMode", this.blacklistMode);
        output.putBoolean("MatchDurability", this.matchDurability);
        output.putBoolean("MatchEnchantments", this.matchEnchantments);
        output.putBoolean("MatchComponents", this.matchComponents);
        if (this.filterTag != null) {
            output.store("FilterTagKey", TagKey.codec(Registries.ITEM), this.filterTag);
        }
        output.putBoolean("IsTagFiltering", this.isTagFiltering);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.filterStack = input.read("FilterStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.blacklistMode = input.getBooleanOr("BlacklistMode", false);
        this.matchDurability = input.getBooleanOr("MatchDurability", true);
        this.matchEnchantments = input.getBooleanOr("MatchEnchantments", true);
        this.matchComponents = input.getBooleanOr("MatchComponents", true);
        this.filterTag = input.read("FilterTagKey", TagKey.codec(Registries.ITEM)).orElse(null);
        this.isTagFiltering = input.getBooleanOr("IsTagFiltering", false);
    }

    public boolean doesMatchFilter(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;

        if (this.isTagFiltering) {
            //noinspection SimplifiableConditionalExpression - This is more readable in my opinion
            return this.blacklistMode ?
                    !stack.is(this.filterTag) :
                    stack.is(this.filterTag);
        }

        if (this.filterStack.isEmpty())
            return false;

        //noinspection SimplifiableConditionalExpression - This is more readable in my opinion
        return this.blacklistMode ?
                !internalStackMatches(stack) :
                internalStackMatches(stack);
    }

    private boolean internalStackMatches(ItemStack stack) {
        if (!ItemStack.isSameItem(stack, this.filterStack))
            return false;

        if (this.matchComponents)
            return ItemStack.isSameItemSameComponents(stack, this.filterStack);

        if (this.matchDurability && stack.getDamageValue() != this.filterStack.getDamageValue())
            return false;

        //noinspection RedundantIfStatement - This is more readable in my opinion
        if (this.matchEnchantments && !Objects.equals(stack.get(EnchantmentHelper.getComponentType(stack)), this.filterStack.get(EnchantmentHelper.getComponentType(this.filterStack))))
            return false;

        return true;
    }

    @Override
    public boolean shouldWaitForEndTick() {
        return false;
    }

    @Override
    public boolean isTagFiltering() {
        return this.isTagFiltering;
    }

    @Override
    public void setTagFiltering(boolean tagFiltering) {
        this.isTagFiltering = tagFiltering;
        update();
    }

    @Override
    public ItemStack getFilterStack() {
        return this.filterStack;
    }

    @Override
    public void setFilterStack(ItemStack filterStack) {
        this.filterStack = filterStack.copyWithCount(1);
        update();
    }

    @Override
    public TagKey<Item> getFilterTag() {
        return this.filterTag;
    }

    @Override
    public void setFilterTag(TagKey<Item> filterTag) {
        this.filterTag = filterTag;
        update();
    }

    @Override
    public boolean isBlacklistMode() {
        return this.blacklistMode;
    }

    @Override
    public void setBlacklistMode(boolean blacklistMode) {
        this.blacklistMode = blacklistMode;
        update();
    }

    @Override
    public boolean isMatchDurability() {
        return this.matchDurability;
    }

    @Override
    public void setMatchDurability(boolean matchDurability) {
        this.matchDurability = matchDurability;
        update();
    }

    @Override
    public boolean isMatchEnchantments() {
        return this.matchEnchantments;
    }

    @Override
    public void setMatchEnchantments(boolean matchEnchantments) {
        this.matchEnchantments = matchEnchantments;
        update();
    }

    @Override
    public boolean isMatchComponents() {
        return this.matchComponents;
    }

    @Override
    public void setMatchComponents(boolean matchComponents) {
        this.matchComponents = matchComponents;
        update();
    }
}
