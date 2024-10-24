package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.inventory.RecipeSimpleInventory;
import dev.turtywurty.industria.init.RecipeSerializerInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.util.CountedIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Pair;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record CrusherRecipe(CountedIngredient input, OutputItemStack outputA, OutputItemStack outputB, int processTime)
        implements Recipe<RecipeSimpleInventory>, CountedIngredient.CountedIngredientRecipe {
    @Override
    public List<CountedIngredient> getCountedIngredients() {
        return List.of(this.input);
    }

    @Override
    public boolean matches(RecipeSimpleInventory input, World world) {
        return this.input.test(input.getStack(0));
    }

    public Pair<ItemStack, ItemStack> assemble(RecipeSimpleInventory input, Random random) {
        return new Pair<>(this.outputA.createStack(random), this.outputB.createStack(random));
    }

    @Override
    public ItemStack craft(RecipeSimpleInventory input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack stack = input.getStack(0);
        stack.decrement(this.input.count());
        input.setStack(0, stack);
        return this.outputA.createStack(new LocalRandom(ThreadLocalRandom.current().nextLong()));
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeSimpleInventory>> getSerializer() {
        return RecipeSerializerInit.CRUSHER;
    }

    @Override
    public RecipeType<? extends Recipe<RecipeSimpleInventory>> getType() {
        return RecipeTypeInit.CRUSHER;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return null;
    }

    @Override
    public String getGroup() {
        return Industria.id("crusher").toString();
    }

    public static class Type implements RecipeType<CrusherRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }
    }

    public static class Serializer implements RecipeSerializer<CrusherRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        private static final MapCodec<CrusherRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CountedIngredient.CODEC.fieldOf("input").forGetter(CrusherRecipe::input),
                OutputItemStack.CODEC.fieldOf("output_a").forGetter(CrusherRecipe::outputA),
                OutputItemStack.CODEC.fieldOf("output_b").forGetter(CrusherRecipe::outputB),
                Codec.INT.fieldOf("process_time").forGetter(CrusherRecipe::processTime)
        ).apply(instance, CrusherRecipe::new));

        private static final PacketCodec<RegistryByteBuf, CrusherRecipe> PACKET_CODEC =
                PacketCodec.ofStatic((buf, recipe) -> {
                    CountedIngredient.PACKET_CODEC.encode(buf, recipe.input());
                    OutputItemStack.PACKET_CODEC.encode(buf, recipe.outputA());
                    OutputItemStack.PACKET_CODEC.encode(buf, recipe.outputB());
                    buf.writeVarInt(recipe.processTime());
                }, buf -> new CrusherRecipe(CountedIngredient.PACKET_CODEC.decode(buf),
                        OutputItemStack.PACKET_CODEC.decode(buf), OutputItemStack.PACKET_CODEC.decode(buf),
                        buf.readVarInt()));

        @Override
        public MapCodec<CrusherRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, CrusherRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
