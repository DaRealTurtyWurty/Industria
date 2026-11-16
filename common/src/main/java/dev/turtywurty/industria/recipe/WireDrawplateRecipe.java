package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.industria.init.ModRecipeBookCategories;
import dev.turtywurty.industria.init.ModRecipeSerializers;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.recipe.input.SingleItemStackRecipeInput;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record WireDrawplateRecipe(IndustriaIngredient input, OutputItemStack output, int duration)
    implements Recipe<SingleItemStackRecipeInput> {
    @Override
    public boolean matches(SingleItemStackRecipeInput input, Level world) {
        return this.input.testForRecipe(input.stack());
    }

    @Override
    public ItemStack assemble(SingleItemStackRecipeInput input) {
        return this.output.createStack(new SingleThreadedRandomSource(ThreadLocalRandom.current().nextLong()));
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
    public String group() {
        return Industria.id("wire_drawplate").toString();
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleItemStackRecipeInput>> getSerializer() {
        return ModRecipeSerializers.WIRE_DRAWPLATE.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleItemStackRecipeInput>> getType() {
        return ModRecipeTypes.WIRE_DRAWPLATE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new WireDrawplateRecipeDisplay(
            input.toDisplay(),
            new SlotDisplay.ItemSlotDisplay(ModItems.WIRE_DRAWPLATE.get()),
            output.toDisplay(),
            duration));
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeBookCategories.WIRE_DRAWPLATE.get();
    }

    public static class Type implements RecipeType<WireDrawplateRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {
        }

        @Override
        public String toString() {
            return Industria.id("wire_drawplate").toString();
        }
    }

    private static final MapCodec<WireDrawplateRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        IndustriaIngredient.CODEC.fieldOf("input").forGetter(WireDrawplateRecipe::input),
        OutputItemStack.CODEC.fieldOf("output").forGetter(WireDrawplateRecipe::output),
        Codec.INT.fieldOf("duration").forGetter(WireDrawplateRecipe::duration))
        .apply(instance, WireDrawplateRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, WireDrawplateRecipe> STREAM_CODEC = StreamCodec.composite(
        IndustriaIngredient.STREAM_CODEC, WireDrawplateRecipe::input,
        OutputItemStack.STREAM_CODEC, WireDrawplateRecipe::output,
        ByteBufCodecs.VAR_INT, WireDrawplateRecipe::duration,
        WireDrawplateRecipe::new);

    public static final RecipeSerializer<WireDrawplateRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public record WireDrawplateRecipeDisplay(
        SlotDisplay input,
        SlotDisplay craftingStation,
        SlotDisplay result,
        int duration
    ) implements RecipeDisplay {
        private static final MapCodec<WireDrawplateRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                SlotDisplay.CODEC.fieldOf("input").forGetter(WireDrawplateRecipeDisplay::input),
                SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(WireDrawplateRecipeDisplay::craftingStation),
                SlotDisplay.CODEC.fieldOf("result").forGetter(WireDrawplateRecipeDisplay::result),
                Codec.INT.fieldOf("duration").forGetter(WireDrawplateRecipeDisplay::duration))
            .apply(instance, WireDrawplateRecipeDisplay::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, WireDrawplateRecipeDisplay> STREAM_CODEC = StreamCodec
            .composite(
                SlotDisplay.STREAM_CODEC, WireDrawplateRecipeDisplay::input,
                SlotDisplay.STREAM_CODEC, WireDrawplateRecipeDisplay::craftingStation,
                SlotDisplay.STREAM_CODEC, WireDrawplateRecipeDisplay::result,
                ByteBufCodecs.VAR_INT, WireDrawplateRecipeDisplay::duration,
                WireDrawplateRecipeDisplay::new);

        private static final Type<WireDrawplateRecipeDisplay> SERIALIZER = new Type<>(CODEC,
            STREAM_CODEC);

        @Override
        public Type<WireDrawplateRecipeDisplay> type() {
            return SERIALIZER;
        }
    }
}
