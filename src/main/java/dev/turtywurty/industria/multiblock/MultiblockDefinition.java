package dev.turtywurty.industria.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.util.ExtraCodecs;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AxisRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;

import java.util.*;

/**
 * Represents a multiblock structure definition.
 *
 * @param id             the unique identifier for this multiblock definition
 * @param size           the size of the multiblock structure in blocks
 * @param anchor         the position of the controller block within the multiblock structure
 * @param rotations      the allowed rotations for this multiblock structure
 * @param allowedMirrors the allowed mirror modes for this multiblock structure
 * @param palette        a mapping of characters to block predicates for validating block placements
 * @param pattern        the pattern defining the layout of blocks in the multiblock structure
 * @param portRules      rules defining how ports are configured within the multiblock structure
 */
public record MultiblockDefinition(Identifier id, Vec3i size, Vec3i anchor, Set<AxisRotation> rotations,
                                   Set<MirrorMode> allowedMirrors, Char2ObjectMap<BlockPredicate> palette,
                                   List<List<String>> pattern, Char2ObjectMap<PortRule> portRules,
                                   boolean replaceAirBlocks, boolean replaceReplaceableBlocks) {
    private static final Codec<Set<AxisRotation>> ROTATIONS_CODEC = ExtraCodecs.setOf(AxisRotation.CODEC);
    private static final Codec<Set<MirrorMode>> MIRROR_MODE_CODEC = ExtraCodecs.setOf(MirrorMode.CODEC);
    private static final Codec<Char2ObjectMap<BlockPredicate>> PALETTE_CODEC = Codec.unboundedMap(
            ExtraCodecs.CHAR_CODEC,
            BlockPredicate.BASE_CODEC
    ).xmap(Char2ObjectOpenHashMap::new, Char2ObjectOpenHashMap::new);
    private static final Codec<List<List<String>>> PATTERN_CODEC = Codec.list(
            Codec.list(Codec.STRING)
    ).xmap(List::copyOf, List::copyOf);
    private static final Codec<Char2ObjectMap<PortRule>> PORT_RULES_CODEC = Codec.unboundedMap(
            ExtraCodecs.CHAR_CODEC,
            PortRule.CODEC.codec()
    ).xmap(Char2ObjectOpenHashMap::new, Char2ObjectOpenHashMap::new);

    public static final MapCodec<MultiblockDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(MultiblockDefinition::id),
            Vec3i.CODEC.fieldOf("size").forGetter(MultiblockDefinition::size),
            Vec3i.CODEC.fieldOf("anchor").forGetter(MultiblockDefinition::anchor),
            ROTATIONS_CODEC.fieldOf("rotations").forGetter(MultiblockDefinition::rotations),
            MIRROR_MODE_CODEC.fieldOf("allowed_mirrors").forGetter(MultiblockDefinition::allowedMirrors),
            PALETTE_CODEC.fieldOf("palette").forGetter(MultiblockDefinition::palette),
            PATTERN_CODEC.fieldOf("pattern").forGetter(MultiblockDefinition::pattern),
            PORT_RULES_CODEC.fieldOf("port_rules").forGetter(MultiblockDefinition::portRules),
            Codec.BOOL.fieldOf("replace_air_blocks").orElse(false).forGetter(MultiblockDefinition::replaceAirBlocks),
            Codec.BOOL.fieldOf("replace_replaceable_blocks").orElse(false).forGetter(MultiblockDefinition::replaceReplaceableBlocks)
    ).apply(instance, MultiblockDefinition::new));

    public char getCharAt(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= size.getX() || y >= size.getY() || z >= size.getZ())
            throw new IndexOutOfBoundsException("Coordinates out of bounds for multiblock pattern: Coord:{" + x + ", " + y + ", " + z + "} Size:{" + size.getX() + ", " + size.getY() + ", " + size.getZ() + "}");

        List<String> row = pattern.get(y);
        if (row == null || z >= row.size())
            throw new IndexOutOfBoundsException("Row is null or z coordinate out of bounds for multiblock pattern");

        String column = row.get(z);
        if (column == null || x >= column.length())
            throw new IndexOutOfBoundsException("Column is null or x coordinate out of bounds for multiblock pattern");

        return column.charAt(x);
    }

    public boolean canHaveBlock(StructureWorldAccess world, BlockPos pos) {
        return palette.values().stream().anyMatch(predicate -> predicate.test(world, pos));
    }

    public static class Builder {
        private int sizeX, sizeY, sizeZ;
        private int anchorX, anchorY, anchorZ;
        private final Set<AxisRotation> rotations = new HashSet<>();
        private final Set<MirrorMode> allowedMirrors = new HashSet<>();
        private final Map<Character, BlockPredicate> palette = new HashMap<>();
        private final List<List<String>> pattern = new ArrayList<>();
        private final Map<Character, PortRule> portRules = new HashMap<>();

        private boolean replaceAirBlocks = false;
        private boolean replaceReplaceableBlocks = false;

        public Builder() {
            this.rotations.add(AxisRotation.R0);
            this.allowedMirrors.add(MirrorMode.NONE);
        }

        public Builder size(Vec3i size) {
            return size(size.getX(), size.getY(), size.getZ());
        }

        public Builder size(int x, int y, int z) {
            this.sizeX = x;
            this.sizeY = y;
            this.sizeZ = z;
            return this;
        }

        public Builder anchor(Vec3i anchor) {
            return anchor(anchor.getX(), anchor.getY(), anchor.getZ());
        }

        public Builder anchor(int x, int y, int z) {
            this.anchorX = x;
            this.anchorY = y;
            this.anchorZ = z;
            return this;
        }

        public Builder addRotation(AxisRotation... rotation) {
            Collections.addAll(this.rotations, rotation);
            return this;
        }

        public Builder addAllowedMirror(MirrorMode... mirror) {
            Collections.addAll(this.allowedMirrors, mirror);
            return this;
        }

        public Builder addPaletteEntry(char character, BlockPredicate predicate) {
            this.palette.put(character, predicate);
            return this;
        }

        public Builder addPatternRow(List<String> row) {
            this.pattern.add(row);
            return this;
        }

        public Builder addPortRule(char character, PortRule rule) {
            this.portRules.put(character, rule);
            return this;
        }

        public Builder replaceAirBlocks(boolean replaceAirBlocks) {
            this.replaceAirBlocks = replaceAirBlocks;
            return this;
        }

        public Builder replaceReplaceableBlocks(boolean replaceReplaceableBlocks) {
            this.replaceReplaceableBlocks = replaceReplaceableBlocks;
            return this;
        }

        public MultiblockDefinition build(Identifier id) {
            if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0)
                throw new IllegalStateException("Size must be set and greater than zero");

            if (anchorX < 0 || anchorY < 0 || anchorZ < 0 ||
                    anchorX >= sizeX || anchorY >= sizeY || anchorZ >= sizeZ)
                throw new IllegalStateException("Anchor must be within the bounds of the size");

            if (rotations.isEmpty())
                throw new IllegalStateException("At least one rotation must be specified");

            if (pattern.size() != sizeY)
                throw new IllegalStateException("Pattern height must match size Y");

            for (List<String> row : pattern) {
                if (row.size() != sizeZ)
                    throw new IllegalStateException("Each pattern row must match size Z");

                for (String column : row) {
                    if (column.length() != sizeX)
                        throw new IllegalStateException("Each pattern column must match size X");

                    for (char c : column.toCharArray()) {
                        if (!palette.containsKey(c))
                            throw new IllegalStateException("Palette must contain an entry for character: " + c);
                    }
                }
            }

            Char2ObjectMap<BlockPredicate> paletteMap = new Char2ObjectOpenHashMap<>(palette);
            Char2ObjectMap<PortRule> portRulesMap = new Char2ObjectOpenHashMap<>(portRules);

            return new MultiblockDefinition(
                    id,
                    new Vec3i(sizeX, sizeY, sizeZ),
                    new Vec3i(anchorX, anchorY, anchorZ),
                    Set.copyOf(rotations),
                    Set.copyOf(allowedMirrors),
                    paletteMap,
                    List.copyOf(pattern),
                    portRulesMap,
                    replaceAirBlocks,
                    replaceReplaceableBlocks
            );
        }
    }
}
