package dev.turtywurty.industria.screen.widget;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface AutoCompleteProvider<T> {
    Collection<T> getSuggestions(String input);

    default Function<T, String> suggestionToString() {
        return Objects::toString;
    }
}
