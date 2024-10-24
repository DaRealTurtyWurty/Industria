package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.inventory.RecipeSimpleInventory;
import dev.turtywurty.industria.init.RecipeSerializerInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.util.CountedIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.List;

public record AlloyFurnaceRecipe(CountedIngredient inputA, CountedIngredient inputB, ItemStack output, int smeltTime)
        implements Recipe<RecipeSimpleInventory>, CountedIngredient.CountedIngredientRecipe {
    @Override
    public boolean matches(RecipeSimpleInventory input, World world) {
        return (this.inputA.test(input.getStack(0)) && this.inputB.test(input.getStack(1))) ||
                (this.inputA.test(input.getStack(1)) && this.inputB.test(input.getStack(0)));
    }

    @Override
    public ItemStack craft(RecipeSimpleInventory inventory, RegistryWrapper.WrapperLookup lookup) {
        // extract the inventory stacks
        ItemStack stackA = this.inputA.test(inventory.getStack(0)) ? inventory.getStack(0) : inventory.getStack(1);
        ItemStack stackB = this.inputB.test(inventory.getStack(0)) ? inventory.getStack(0) : inventory.getStack(1);

        // remove the input stacks
        stackA.decrement(this.inputA.count());
        stackB.decrement(this.inputB.count());

        // set the stacks back into the inventory
        inventory.setStack(getRealSlotIndex(0), stackA);
        inventory.setStack(getRealSlotIndex(1), stackB);

        return this.output.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeSimpleInventory>> getSerializer() {
        return RecipeSerializerInit.ALLOY_FURNACE;
    }

    @Override
    public RecipeType<? extends Recipe<RecipeSimpleInventory>> getType() {
        return RecipeTypeInit.ALLOY_FURNACE;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        return null;
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return null;
    }

    @Override
    public String getGroup() {
        return Industria.id("alloy_furnace").toString();
    }

//    @Override
//    public DefaultedList<Ingredient> getIngredients() {
//        return DefaultedList.copyOf(Ingredient.EMPTY,
//                Ingredient.ofStacks(this.inputA.getMatchingStacks().toArray(new ItemStack[0])),
//                Ingredient.ofStacks(this.inputB.getMatchingStacks().toArray(new ItemStack[0])));
//    }

    @Override
    public List<CountedIngredient> getCountedIngredients() {
        return List.of(CountedIngredient.EMPTY, inputA, inputB);
    }

    @Override
    public int getRealSlotIndex(int index) {
        if (index == 0) return 2;
        return index - 1;
    }

    public static class Type implements RecipeType<AlloyFurnaceRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }
    }

    public static class Serializer implements RecipeSerializer<AlloyFurnaceRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        private static final MapCodec<AlloyFurnaceRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CountedIngredient.CODEC.fieldOf("inputA").forGetter(AlloyFurnaceRecipe::inputA),
                CountedIngredient.CODEC.fieldOf("inputB").forGetter(AlloyFurnaceRecipe::inputB),
                ItemStack.CODEC.fieldOf("output").forGetter(AlloyFurnaceRecipe::output),
                Codec.INT.fieldOf("smelt_time").forGetter(AlloyFurnaceRecipe::smeltTime)
        ).apply(instance, AlloyFurnaceRecipe::new));

        private static final PacketCodec<RegistryByteBuf, AlloyFurnaceRecipe> PACKET_CODEC =
                PacketCodec.ofStatic((buf, recipe) -> {
                    CountedIngredient.PACKET_CODEC.encode(buf, recipe.inputA());
                    CountedIngredient.PACKET_CODEC.encode(buf, recipe.inputB());
                    ItemStack.PACKET_CODEC.encode(buf, recipe.output());
                    buf.writeVarInt(recipe.smeltTime());
                }, buf -> new AlloyFurnaceRecipe(
                        CountedIngredient.PACKET_CODEC.decode(buf),
                        CountedIngredient.PACKET_CODEC.decode(buf),
                        ItemStack.PACKET_CODEC.decode(buf),
                        buf.readVarInt()));

        @Override
        public MapCodec<AlloyFurnaceRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, AlloyFurnaceRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
