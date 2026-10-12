package dev.turtywurty.industria.item;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.component.MoldComponent;
import dev.turtywurty.industria.init.ModDataComponentTypes;
import dev.turtywurty.industria.init.ModFluids;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.industria.molding.MoldPattern;
import dev.turtywurty.industria.molding.MoldPatterns;
import dev.turtywurty.industria.molding.MoldPiece;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.Optional;

public class MoldItem extends Item {
    private final int maxUses;

    public MoldItem(Properties properties, int maxUses) {
        super(maxUses > 1 ? properties.durability(maxUses) : properties);
        this.maxUses = maxUses;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public ItemStack createStack(Identifier moldId, MoldPiece piece) {
        var stack = new ItemStack(this);
        stack.set(ModDataComponentTypes.MOLD.get(), new MoldComponent(moldId, piece));
        return stack;
    }

    public boolean canCast(Fluid fluid) {
        if (this != ModItems.GOLD_MOLD.get())
            return true;

        return fluid != ModFluids.MOLTEN_GOLD.still().get() && fluid != ModFluids.MOLTEN_GOLD.flowing().get();
    }

    public static Optional<MoldItem> fromBlank(ItemStack stack) {
        if (stack.is(ModItems.BLANK_SAND_CAST.get()))
            return Optional.of(ModItems.SAND_MOLD.get());
        else if (stack.is(ModItems.BLANK_RED_SAND_CAST.get()))
            return Optional.of(ModItems.RED_SAND_MOLD.get());
        else if (stack.is(ModItems.BLANK_CLAY_CAST.get()))
            return Optional.of(ModItems.CLAY_MOLD.get());
        else if (stack.is(ModItems.BLANK_GOLD_CAST.get()))
            return Optional.of(ModItems.GOLD_MOLD.get());
        else
            return Optional.empty();
    }

    public List<ItemStack> createStacks(MoldPattern pattern) {
        if (pattern.split())
            return List.of(createStack(pattern.id(), MoldPiece.HALF_1), createStack(pattern.id(), MoldPiece.HALF_2));

        return List.of(createStack(pattern.id(), MoldPiece.WHOLE));
    }

    @Override
    public Component getName(ItemStack itemStack) {
        Component baseName = super.getName(itemStack);
        MoldComponent data = itemStack.get(ModDataComponentTypes.MOLD.get());
        if (data == null)
            return baseName;

        Optional<MoldPattern> pattern = MoldPatterns.find(data.moldId());
        if (pattern.isEmpty())
            return baseName;

        Component name = Component.translatable(
            "item." + Industria.MOD_ID + ".mold.named",
            baseName,
            pattern.get().getDisplayName());
        if (data.piece() == MoldPiece.WHOLE)
            return name;

        return Component.translatable(
            "item." + Industria.MOD_ID + ".mold.half",
            name,
            data.piece() == MoldPiece.HALF_1 ? 1 : 2);
    }
}
