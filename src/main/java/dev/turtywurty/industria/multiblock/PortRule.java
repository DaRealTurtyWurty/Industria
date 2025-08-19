package dev.turtywurty.industria.multiblock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public record PortRule(Predicate<LocalPos> positionMatch, EnumSet<LocalDirection> sides, List<PortType> types) {
    public static Builder when(Predicate<LocalPos> positionMatch) {
        return new Builder(positionMatch);
    }

    public static class Builder {
        private final Predicate<LocalPos> positionMatch;
        private final EnumSet<LocalDirection> sides = EnumSet.noneOf(LocalDirection.class);
        private final List<PortType> types = new ArrayList<>();

        public Builder(Predicate<LocalPos> positionMatch) {
            this.positionMatch = positionMatch;
        }

        public Builder on(LocalDirection... sides) {
            this.sides.addAll(Arrays.asList(sides));
            return this;
        }

        public Builder types(PortType... types) {
            this.types.addAll(Arrays.asList(types));
            return this;
        }

        public PortRule build() {
            return new PortRule(positionMatch, EnumSet.copyOf(sides), List.copyOf(types));
        }
    }
}
