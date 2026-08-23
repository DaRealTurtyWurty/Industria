package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.inventory.RecipeSimpleInventory;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModRecipeBookCategories;
import dev.turtywurty.industria.init.ModRecipeSerializers;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.util.IndustriaIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public record AlloyingRecipe(IndustriaIngredient inputA, IndustriaIngredient inputB, ItemStackTemplate output,
                             int smeltTime) implements Recipe<RecipeSimpleInventory> {
    @Override
    public boolean matches(RecipeSimpleInventory input, Level world) {
        ItemStack matchedA = ItemStack.EMPTY;
        ItemStack matchedB = ItemStack.EMPTY;

        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty())
                continue;

            if (matchedA.isEmpty() && this.inputA.testForRecipe(stack)) {
                matchedA = stack;
                continue;
            }

            if (matchedB.isEmpty() && this.inputB.testForRecipe(stack)) {
                matchedB = stack;
                continue;
            }

            return false;
        }

        return !matchedA.isEmpty() && !matchedB.isEmpty();
    }

    @Override
    public ItemStack assemble(RecipeSimpleInventory inventory) {
        int slotA = -1;
        int slotB = -1;

        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (slotA == -1 && this.inputA.testForRecipe(stack)) {
                slotA = slot;
            } else if (slotB == -1 && this.inputB.testForRecipe(stack)) {
                slotB = slot;
            }
        }

        if (slotA != -1) {
            ItemStack stackA = inventory.getItem(slotA);
            stackA.shrink(this.inputA.stackData().count());
            inventory.setItem(slotA, stackA);
        }

        if (slotB != -1) {
            ItemStack stackB = inventory.getItem(slotB);
            stackB.shrink(this.inputB.stackData().count());
            inventory.setItem(slotB, stackB);
        }

        return this.output.create();
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeSimpleInventory>> getSerializer() {
        return ModRecipeSerializers.ALLOYING.get();
    }

    @Override
    public RecipeType<? extends Recipe<RecipeSimpleInventory>> getType() {
        return ModRecipeTypes.ALLOYING.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.ALLOYING.get();
    }

    @Override
    public String group() {
        return Industria.id("alloy_furnace").toString();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new AlloyFurnaceRecipeDisplay(
                this.inputA.toDisplay(),
                this.inputB.toDisplay(),
                SlotDisplay.AnyFuel.INSTANCE,
                new SlotDisplay.ItemStackSlotDisplay(this.output),
                new SlotDisplay.ItemSlotDisplay(ModBlocks.ALLOY_FURNACE.get().asItem()),
                this.smeltTime
        ));
    }

    public static class Type implements RecipeType<AlloyingRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("alloy_furnace").toString();
        }
    }

    private static final MapCodec<AlloyingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IndustriaIngredient.CODEC.fieldOf("inputA").forGetter(AlloyingRecipe::inputA),
            IndustriaIngredient.CODEC.fieldOf("inputB").forGetter(AlloyingRecipe::inputB),
            ItemStackTemplate.CODEC.fieldOf("output").forGetter(AlloyingRecipe::output),
            Codec.INT.fieldOf("smelt_time").forGetter(AlloyingRecipe::smeltTime)
    ).apply(instance, AlloyingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> STREAM_CODEC =
            StreamCodec.composite(IndustriaIngredient.STREAM_CODEC, AlloyingRecipe::inputA,
                    IndustriaIngredient.STREAM_CODEC, AlloyingRecipe::inputB,
                    ItemStackTemplate.STREAM_CODEC, AlloyingRecipe::output,
                    ByteBufCodecs.INT, AlloyingRecipe::smeltTime,
                    AlloyingRecipe::new);

    public static final RecipeSerializer<AlloyingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public record AlloyFurnaceRecipeDisplay(SlotDisplay inputA, SlotDisplay inputB, SlotDisplay fuel,
                                            SlotDisplay result, SlotDisplay craftingStation,
                                            int processTime) implements RecipeDisplay {
        public static final MapCodec<AlloyFurnaceRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        SlotDisplay.CODEC.fieldOf("inputA").forGetter(AlloyFurnaceRecipeDisplay::inputA),
                        SlotDisplay.CODEC.fieldOf("inputB").forGetter(AlloyFurnaceRecipeDisplay::inputB),
                        SlotDisplay.CODEC.fieldOf("fuel").forGetter(AlloyFurnaceRecipeDisplay::fuel),
                        SlotDisplay.CODEC.fieldOf("result").forGetter(AlloyFurnaceRecipeDisplay::result),
                        SlotDisplay.CODEC.fieldOf("craftingStation").forGetter(AlloyFurnaceRecipeDisplay::craftingStation),
                        Codec.INT.fieldOf("processTime").forGetter(AlloyFurnaceRecipeDisplay::processTime)
                ).apply(instance, AlloyFurnaceRecipeDisplay::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AlloyFurnaceRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, AlloyFurnaceRecipeDisplay::inputA,
                SlotDisplay.STREAM_CODEC, AlloyFurnaceRecipeDisplay::inputB,
                SlotDisplay.STREAM_CODEC, AlloyFurnaceRecipeDisplay::fuel,
                SlotDisplay.STREAM_CODEC, AlloyFurnaceRecipeDisplay::result,
                SlotDisplay.STREAM_CODEC, AlloyFurnaceRecipeDisplay::craftingStation,
                ByteBufCodecs.INT, AlloyFurnaceRecipeDisplay::processTime,
                AlloyFurnaceRecipeDisplay::new
        );

        public static final RecipeDisplay.Type<AlloyFurnaceRecipeDisplay> SERIALIZER = new RecipeDisplay.Type<>(CODEC, STREAM_CODEC);

        @Override
        public SlotDisplay craftingStation() {
            return this.craftingStation;
        }

        @Override
        public RecipeDisplay.Type<AlloyFurnaceRecipeDisplay> type() {
            return SERIALIZER;
        }
    }
}
