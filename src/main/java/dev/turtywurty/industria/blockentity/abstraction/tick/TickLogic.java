package dev.turtywurty.industria.blockentity.abstraction.tick;

import dev.turtywurty.industria.blockentity.abstraction.BlockEntityFields;
import dev.turtywurty.industria.blockentity.abstraction.IndustriaBlockEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public record TickLogic<T extends IndustriaBlockEntity<T>, F extends BlockEntityFields<T>>(
        List<TickOperation<T, F>> operations) {
    public void run(T blockEntity, F fields) {
        for (TickOperation<T, F> operation : this.operations) {
            operation.run(blockEntity, fields);
        }
    }

    public static final class TickLogicBuilder<T extends IndustriaBlockEntity<T>, F extends BlockEntityFields<T>> {
        private final List<TickOperation<T, F>> operations = new ArrayList<>();

        public TickLogicBuilder<T, F> addOperation(BiConsumer<T, F> operation) {
            return addOperation(new TickOperation.Default<>(operation));
        }

        public TickLogicBuilder<T, F> condition(BiPredicate<T, F> condition, TickOperation<T, F> operation, TickOperation<T, F> elseOperation) {
            return addOperation(new TickOperation.Conditional<>(condition, operation, elseOperation));
        }

        public TickLogicBuilder<T, F> condition(BiPredicate<T, F> condition, TickOperation<T, F> operation) {
            return addOperation(new TickOperation.Conditional<>(condition, operation));
        }

        public TickLogicBuilder<T, F> condition(BiPredicate<T, F> condition, BiConsumer<T, F> operation, BiConsumer<T, F> elseOperation) {
            return addOperation(new TickOperation.Conditional<>(condition, new TickOperation.Default<>(operation), new TickOperation.Default<>(elseOperation)));
        }

        public TickLogicBuilder<T, F> condition(BiPredicate<T, F> condition, BiConsumer<T, F> operation) {
            return addOperation(new TickOperation.Conditional<>(condition, new TickOperation.Default<>(operation)));
        }

        @SafeVarargs
        public final TickLogicBuilder<T, F> sequence(TickOperation<T, F>... operations) {
            return addOperation(new TickOperation.Sequence<>(operations));
        }

        public TickLogicBuilder<T, F> repeat(int times, TickOperation<T, F> operation) {
            return addOperation(new TickOperation.Repeat<>(times, operation));
        }

        public TickLogicBuilder<T, F> repeat(int times, BiConsumer<T, F> operation) {
            return addOperation(new TickOperation.Repeat<>(times, new TickOperation.Default<>(operation)));
        }

        public TickLogicBuilder<T, F> whileCondition(BiPredicate<T, F> condition, TickOperation<T, F> operation) {
            return addOperation(new TickOperation.While<>(condition, operation));
        }

        public TickLogicBuilder<T, F> whileCondition(BiPredicate<T, F> condition, BiConsumer<T, F> operation) {
            return addOperation(new TickOperation.While<>(condition, new TickOperation.Default<>(operation)));
        }

        public TickLogicBuilder<T, F> delayed(int delay, TickOperation<T, F> operation) {
            return addOperation(new TickOperation.Delayed<>(delay, operation));
        }

        public TickLogicBuilder<T, F> delayed(int delay, BiConsumer<T, F> operation) {
            return addOperation(new TickOperation.Delayed<>(delay, new TickOperation.Default<>(operation)));
        }

        public TickLogicBuilder<T, F> addOperation(TickOperation<T, F> operation) {
            this.operations.add(operation);
            return this;
        }

        public TickLogicBuilder<T, F> addOperations(List<TickOperation<T, F>> operations) {
            this.operations.addAll(operations);
            return this;
        }

        @SafeVarargs
        public final TickLogicBuilder<T, F> addOperations(TickOperation<T, F>... operations) {
            return addOperations(Arrays.asList(operations));
        }

        public TickLogic<T, F> build() {
            return new TickLogic<>(this.operations);
        }
    }
}
