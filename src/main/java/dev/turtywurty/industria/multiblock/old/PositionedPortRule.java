package dev.turtywurty.industria.multiblock.old;

import dev.turtywurty.industria.multiblock.LocalDirection;
import dev.turtywurty.industria.multiblock.LocalPos;
import dev.turtywurty.industria.multiblock.PortType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * Represents a rule for matching ports in a multiblock structure.
 *
 * @param positionMatch a predicate that matches the position of the port
 * @param sides an EnumSet of LocalDirection representing the sides on which the port can be located
 * @param types a list of PortType that defines the types of ports that can be matched by this rule
 */
public record PositionedPortRule(Predicate<LocalPos> positionMatch, EnumSet<LocalDirection> sides, List<PortType> types) {
    /**
     * Creates a new PortRule builder with the specified position match predicate.
     *
     * @param positionMatch a predicate that matches the position of the port
     * @return a new Builder instance
     */
    public static Builder when(Predicate<LocalPos> positionMatch) {
        return new Builder(positionMatch);
    }

    /**
     * Builder class for constructing PortRule instances.
     */
    public static class Builder {
        private final Predicate<LocalPos> positionMatch;
        private final EnumSet<LocalDirection> sides = EnumSet.noneOf(LocalDirection.class);
        private final List<PortType> types = new ArrayList<>();

        /**
         * Constructs a Builder with the specified position match predicate.
         *
         * @param positionMatch a predicate that matches the position of the port
         */
        public Builder(Predicate<LocalPos> positionMatch) {
            this.positionMatch = positionMatch;
        }

        /**
         * Adds the specified sides to the rule.
         *
         * @param sides the LocalDirection sides to add
         * @return this Builder instance for chaining
         */
        public Builder on(LocalDirection... sides) {
            this.sides.addAll(Arrays.asList(sides));
            return this;
        }

        /**
         * Adds the specified PortType(s) to the rule.
         *
         * @param types the PortType(s) to add
         * @return this Builder instance for chaining
         */
        public Builder types(PortType... types) {
            this.types.addAll(Arrays.asList(types));
            return this;
        }

        /**
         * Builds and returns a new PortRule instance with the specified parameters.
         *
         * @return a new PortRule instance
         */
        public PositionedPortRule build() {
            return new PositionedPortRule(positionMatch, EnumSet.copyOf(sides), List.copyOf(types));
        }
    }
}
