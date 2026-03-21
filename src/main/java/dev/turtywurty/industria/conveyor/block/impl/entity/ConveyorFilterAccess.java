package dev.turtywurty.industria.conveyor.block.impl.entity;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ConveyorFilterAccess {
    ItemStack getFilterStack();

    void setFilterStack(ItemStack filterStack);

    TagKey<Item> getFilterTag();

    void setFilterTag(TagKey<Item> filterTag);

    boolean isBlacklistMode();

    void setBlacklistMode(boolean blacklistMode);

    boolean isMatchDurability();

    void setMatchDurability(boolean matchDurability);

    boolean isMatchEnchantments();

    void setMatchEnchantments(boolean matchEnchantments);

    boolean isMatchComponents();

    void setMatchComponents(boolean matchComponents);

    boolean isTagFiltering();

    void setTagFiltering(boolean tagFiltering);
}
