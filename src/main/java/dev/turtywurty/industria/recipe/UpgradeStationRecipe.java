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
import dev.turtywurty.industria.util.IndustriaIngredient;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// 3x3 crafting grid
public record UpgradeStationRecipe(Map<Character, IndustriaIngredient> key, String[] pattern,
                                   ItemStack output) implements Recipe<RecipeSimpleInventory> {
    @Override
    public boolean matches(RecipeSimpleInventory input, World world) {
        ItemStack[][] grid = new ItemStack[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int index = i * 3 + j;
                grid[i][j] = input.getStackInSlot(index);
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
    public ItemStack craft(RecipeSimpleInventory input, RegistryWrapper.WrapperLookup registries) {
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
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        List<SlotDisplay> inputs = Arrays.stream(this.pattern)
                .map(s -> s.chars()
                        .mapToObj(c -> this.key.getOrDefault((char) c, IndustriaIngredient.EMPTY).toDisplay())
                        .toList())
                .map(SlotDisplay.CompositeSlotDisplay::new)
                .map(SlotDisplay.class::cast)
                .toList();

        return List.of(new UpgradeStationRecipeDisplay(
                inputs,
                new SlotDisplay.StackSlotDisplay(this.output),
                new SlotDisplay.ItemSlotDisplay(BlockInit.UPGRADE_STATION.asItem())));
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
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

        public static final Codec<Character> CHAR_CODEC = Codec.STRING.xmap(s -> s.charAt(0), String::valueOf);

        private static final Codec<Map<Character, IndustriaIngredient>> KEY_CODEC = Codec.unboundedMap(CHAR_CODEC, IndustriaIngredient.CODEC);
        private static final PacketCodec<RegistryByteBuf, Map<Character, IndustriaIngredient>> KEY_PACKET_CODEC = PacketCodec.of(
                (value, buf) -> buf.writeMap(value, (buf1, value1) -> buf.writeChar(value1),
                        (buf1, value1) -> IndustriaIngredient.PACKET_CODEC.encode((RegistryByteBuf) buf1, value1)),
                buf -> buf.readMap(ByteBuf::readChar,
                        buf1 -> IndustriaIngredient.PACKET_CODEC.decode((RegistryByteBuf) buf1)));

        public static final MapCodec<UpgradeStationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        KEY_CODEC.fieldOf("key").forGetter(UpgradeStationRecipe::key),
                        Codec.STRING.listOf().fieldOf("pattern").forGetter(recipe -> Arrays.asList(recipe.pattern)),
                        ItemStack.VALIDATED_CODEC.fieldOf("output").forGetter(UpgradeStationRecipe::output)
                ).apply(instance, (keys, pattern, output) -> new UpgradeStationRecipe(keys, pattern.toArray(new String[0]), output)));

        public static final PacketCodec<RegistryByteBuf, UpgradeStationRecipe> PACKET_CODEC = PacketCodec.tuple(
                KEY_PACKET_CODEC, UpgradeStationRecipe::key,
                PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING), recipe -> Arrays.asList(recipe.pattern),
                ItemStack.PACKET_CODEC, UpgradeStationRecipe::output,
                (key, pattern, output) -> new UpgradeStationRecipe(key, pattern.toArray(new String[0]), output));

        @Override
        public MapCodec<UpgradeStationRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, UpgradeStationRecipe> packetCodec() {
            return PACKET_CODEC;
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

        public static final PacketCodec<RegistryByteBuf, UpgradeStationRecipeDisplay> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.collection(ArrayList::new, SlotDisplay.PACKET_CODEC), UpgradeStationRecipeDisplay::inputs,
                SlotDisplay.PACKET_CODEC, UpgradeStationRecipeDisplay::output,
                SlotDisplay.PACKET_CODEC, UpgradeStationRecipeDisplay::craftingStation,
                UpgradeStationRecipeDisplay::new);

        public static final Serializer<UpgradeStationRecipeDisplay> SERIALIZER = new Serializer<>(CODEC, PACKET_CODEC);

        @Override
        public SlotDisplay result() {
            return this.output;
        }

        @Override
        public SlotDisplay craftingStation() {
            return this.craftingStation;
        }

        @Override
        public Serializer<? extends RecipeDisplay> serializer() {
            return SERIALIZER;
        }
    }
}
