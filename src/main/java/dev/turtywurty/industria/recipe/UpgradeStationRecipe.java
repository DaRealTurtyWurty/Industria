package dev.turtywurty.industria.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.inventory.RecipeSimpleInventory;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.RecipeBookCategoryInit;
import dev.turtywurty.industria.init.RecipeSerializerInit;
import dev.turtywurty.industria.init.RecipeTypeInit;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.IndustriaIngredient;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// 3x3 crafting grid
public record UpgradeStationRecipe(Map<Character, IndustriaIngredient> key, String[] pattern,
                                   ItemStack output) implements Recipe<RecipeSimpleInventory> {
    @Override
    public boolean matches(RecipeSimpleInventory input, Level world) {
        ItemStack[][] grid = new ItemStack[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int index = i * 3 + j;
                grid[i][j] = input.getItem(index);
            }
        }

        // Find the maximum width of the pattern
        int maxPatternWidth = Arrays.stream(pattern).mapToInt(String::length).max().orElse(0);
        int patternHeight = pattern.length;

        // Check for matches at each possible starting position
        for (int startRow = 0; startRow <= 3 - patternHeight; startRow++) {
            for (int startCol = 0; startCol <= 3 - maxPatternWidth; startCol++) {
                boolean matches = true;

                for (int row = 0; row < patternHeight; row++) {
                    String rowPattern = pattern[row];

                    for (int col = 0; col < maxPatternWidth; col++) {
                        char key = col < rowPattern.length() ? rowPattern.charAt(col) : ' ';
                        if (key == ' ')
                            continue;

                        IndustriaIngredient expectedIngredient = this.key.get(key);
                        ItemStack currentStack = grid[startRow + row][startCol + col];

                        // Check if the expected ingredient matches the current stack
                        if (expectedIngredient == null || currentStack == null || !expectedIngredient.testForRecipe(currentStack)) {
                            matches = false;
                            break;
                        }
                    }

                    if (!matches)
                        break;
                }

                if (matches)
                    return true;
            }
        }

        return false;
    }

    public IndustriaIngredient getIngredient(int slotIndex) {
        int row = slotIndex / 3;
        int col = slotIndex % 3;

        if (row >= 0 && row < this.pattern.length) {
            String rowPattern = this.pattern[row];
            if (col >= 0 && col < rowPattern.length()) {
                return this.key.get(rowPattern.charAt(col));
            }
        }

        return IndustriaIngredient.EMPTY;
    }

    @Override
    public ItemStack assemble(RecipeSimpleInventory input, HolderLookup.Provider registries) {
        return this.output.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeSimpleInventory>> getSerializer() {
        return RecipeSerializerInit.UPGRADE_STATION;
    }

    @Override
    public RecipeType<? extends Recipe<RecipeSimpleInventory>> getType() {
        return RecipeTypeInit.UPGRADE_STATION;
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
    public List<RecipeDisplay> display() {
        List<SlotDisplay> inputs = Arrays.stream(this.pattern)
                .map(s -> s.chars()
                        .mapToObj(c -> this.key.getOrDefault((char) c, IndustriaIngredient.EMPTY).toDisplay())
                        .toList())
                .map(SlotDisplay.Composite::new)
                .map(SlotDisplay.class::cast)
                .toList();

        return List.of(new UpgradeStationRecipeDisplay(
                inputs,
                new SlotDisplay.ItemStackSlotDisplay(this.output),
                new SlotDisplay.ItemSlotDisplay(BlockInit.UPGRADE_STATION.asItem())));
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategoryInit.UPGRADE_STATION;
    }

    public boolean doesCenterStackMatch(ItemStack centerStack) {
        return this.key.get(this.pattern[1].charAt(1)).testForRecipe(centerStack);
    }

    public static class Type implements RecipeType<UpgradeStationRecipe> {
        public static final Type INSTANCE = new Type();

        private Type() {}

        @Override
        public String toString() {
            return Industria.id("upgrade_station").toString();
        }
    }

    public static class Serializer implements RecipeSerializer<UpgradeStationRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        private static final Codec<Map<Character, IndustriaIngredient>> KEY_CODEC = Codec.unboundedMap(ExtraCodecs.CHAR_CODEC, IndustriaIngredient.CODEC);
        private static final StreamCodec<RegistryFriendlyByteBuf, Map<Character, IndustriaIngredient>> KEY_STREAM_CODEC = StreamCodec.ofMember(
                (value, buf) -> buf.writeMap(value, (buf1, value1) -> buf.writeChar(value1),
                        (buf1, value1) -> IndustriaIngredient.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf1, value1)),
                buf -> buf.readMap(ByteBuf::readChar,
                        buf1 -> IndustriaIngredient.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf1)));

        public static final MapCodec<UpgradeStationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        KEY_CODEC.fieldOf("key").forGetter(UpgradeStationRecipe::key),
                        Codec.STRING.listOf().fieldOf("pattern").forGetter(recipe -> Arrays.asList(recipe.pattern)),
                        ItemStack.STRICT_CODEC.fieldOf("output").forGetter(UpgradeStationRecipe::output)
                ).apply(instance, (keys, pattern, output) -> new UpgradeStationRecipe(keys, pattern.toArray(new String[0]), output)));

        public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeStationRecipe> STREAM_CODEC = StreamCodec.composite(
                KEY_STREAM_CODEC, UpgradeStationRecipe::key,
                ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), recipe -> Arrays.asList(recipe.pattern),
                ItemStack.STREAM_CODEC, UpgradeStationRecipe::output,
                (key, pattern, output) -> new UpgradeStationRecipe(key, pattern.toArray(new String[0]), output));

        @Override
        public MapCodec<UpgradeStationRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, UpgradeStationRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public record UpgradeStationRecipeDisplay(List<SlotDisplay> inputs, SlotDisplay output,
                                              SlotDisplay craftingStation) implements RecipeDisplay {
        public static final MapCodec<UpgradeStationRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        SlotDisplay.CODEC.listOf().fieldOf("inputs").forGetter(UpgradeStationRecipeDisplay::inputs),
                        SlotDisplay.CODEC.fieldOf("output").forGetter(UpgradeStationRecipeDisplay::output),
                        SlotDisplay.CODEC.fieldOf("craftingStation").forGetter(UpgradeStationRecipeDisplay::craftingStation)
                ).apply(instance, UpgradeStationRecipeDisplay::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeStationRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.collection(ArrayList::new, SlotDisplay.STREAM_CODEC), UpgradeStationRecipeDisplay::inputs,
                SlotDisplay.STREAM_CODEC, UpgradeStationRecipeDisplay::output,
                SlotDisplay.STREAM_CODEC, UpgradeStationRecipeDisplay::craftingStation,
                UpgradeStationRecipeDisplay::new);

        public static final net.minecraft.world.item.crafting.display.RecipeDisplay.Type SERIALIZER = new net.minecraft.world.item.crafting.display.RecipeDisplay.Type(CODEC, STREAM_CODEC);

        @Override
        public SlotDisplay result() {
            return this.output;
        }

        @Override
        public SlotDisplay craftingStation() {
            return this.craftingStation;
        }

        @Override
        public net.minecraft.world.item.crafting.display.RecipeDisplay.Type type() {
            return SERIALIZER;
        }
    }
}
