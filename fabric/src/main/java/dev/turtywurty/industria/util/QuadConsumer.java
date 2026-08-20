package dev.turtywurty.industria.util;

/**
 * Represents an operation that accepts four input arguments and returns no result.
 * This is the four-arity specialization of {@link java.util.function.Consumer}.
 *
 * @param <A> the type of the first argument to the operation
 * @param <B> the type of the second argument to the operation
 * @param <C> the type of the third argument to the operation
 * @param <D> the type of the fourth argument to the operation
 * @see java.util.function.Consumer
 * @see java.util.function.BiConsumer
 * @see org.apache.commons.lang3.function.TriConsumer
 */
@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {
    void accept(A a, B b, C c, D d);
}