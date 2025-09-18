package dev.turtywurty.industria.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.util.ExtraCodecs;

import java.util.*;

/**
 * Represents a rule for matching ports in a multiblock structure.
 *
 * @param sides a set of LocalDirection representing the sides on which the port can be located
 * @param types a list of PortType that defines the types of ports that can be matched by this rule
 */
public record PortRule(Set<LocalDirection> sides, List<PortType> types) {
    public static final Codec<PortRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.setOf(LocalDirection.CODEC).fieldOf("sides").forGetter(PortRule::sides),
            ExtraCodecs.listOf(PortType.CODEC).fieldOf("types").forGetter(PortRule::types)
    ).apply(instance, PortRule::new));

    /**
     * Creates a new PortRule builder.
     *
     * @return a new Builder instance
     */
    public static PortRule.Builder of() {
        return new PortRule.Builder();
    }

    /**
     * Builder class for constructing PortRule instances.
     */
    public static class Builder {
        private final EnumSet<LocalDirection> sides = EnumSet.noneOf(LocalDirection.class);
        private final List<PortType> types = new ArrayList<>();

        /**
         * Adds the specified sides to the rule.
         *
         * @param sides the LocalDirection sides to add
         * @return this Builder instance for chaining
         */
        public PortRule.Builder on(LocalDirection... sides) {
            this.sides.addAll(Arrays.asList(sides));
            return this;
        }

        /**
         * Adds the specified PortType(s) to the rule.
         *
         * @param types the PortType(s) to add
         * @return this Builder instance for chaining
         */
        public PortRule.Builder types(PortType... types) {
            this.types.addAll(Arrays.asList(types));
            return this;
        }

        /**
         * Builds and returns a new PortRule instance with the specified parameters.
         *
         * @return a new PortRule instance
         */
        public PortRule build() {
            return new PortRule(EnumSet.copyOf(sides), List.copyOf(types));
        }
    }
}