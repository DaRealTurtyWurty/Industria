package dev.turtywurty.industria.blockentity.abstraction.tick;

import dev.turtywurty.industria.blockentity.abstraction.BlockEntityFields;
import dev.turtywurty.industria.blockentity.abstraction.IndustriaBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public interface TickOperation<T extends IndustriaBlockEntity<T>, F extends BlockEntityFields<T>> {
    void run(T blockEntity, F fields);

    class Empty<T extends IndustriaBlockEntity<T>, F extends BlockEntityFields<T>> implements TickOperation<T, F> {
        public static final Empty<?, ?> INSTANCE = new Empty<>();

        @Override
        public void run(T blockEntity, F fields) {}
    }

    class Default<T extends IndustriaBlockEntity<T>, F extends BlockEntityFields<T>> implements TickOperation<T, F> {
        private final BiConsumer<T, F> operation;

        public Default(BiConsumer<T, F> operation) {
            this.operation = operation;
        }

        @Override
        public void run(T blockEntity, F fields) {
            this.operation.accept(blockEntity, fields);
        }
    }

    class Conditional<T extends IndustriaBlockEntity<T>, F extends BlockEntityFields<T>> implements TickOperation<T, F> {
        private final BiPredicate<T, F> condition;
        private final TickOperation<T, F> operation;
        private final @Nullable TickOperation<T, F> elseOperation;

        public Conditional(BiPredicate<T, F> condition, TickOperation<T, F> operation) {
            this(condition, operation, null);
        }

        public Conditional(BiPredicate<T, F> condition, TickOperation<T, F> operation, @Nullable TickOperation<T, F> elseOperation) {
            this.condition = condition;
            this.operation = operation;
            this.elseOperation = elseOperation;
        }

        @Override
        public void run(T blockEntity, F fields) {
            if (this.condition.test(blockEntity, fields)) {
                this.operation.run(blockEntity, fields);
            } else if (this.elseOperation != null) {
                this.elseOperation.run(blockEntity, fields);
            }
        }
    }

    class Sequence<T extends IndustriaBlockEntity<T>, F extends BlockEntityFields<T>> implements TickOperation<T, F> {
        private final TickOperation<T, F>[] operations;

        @SafeVarargs
        public Sequence(TickOperation<T, F>... operations) {
            this.operations = operations;
        }

        @Override
        public void run(T blockEntity, F fields) {
            for (TickOperation<T, F> operation : this.operations) {
                operation.run(blockEntity, fields);
            }
        }
    }

    class Repeat<T extends IndustriaBlockEntity<T>, F extends BlockEntityFields<T>> implements TickOperation<T, F> {
        private final int times;
        private final TickOperation<T, F> operation;

        public Repeat(int times, TickOperation<T, F> operation) {
            this.times = times;
            this.operation = operation;
        }

        @Override
        public void run(T blockEntity, F fields) {
            for (int i = 0; i < this.times; i++) {
                this.operation.run(blockEntity, fields);
            }
        }
    }

    class While<T extends IndustriaBlockEntity<T>, F extends BlockEntityFields<T>> implements TickOperation<T, F> {
        private final BiPredicate<T, F> condition;
        private final TickOperation<T, F> operation;

        public While(BiPredicate<T, F> condition, TickOperation<T, F> operation) {
            this.condition = condition;
            this.operation = operation;
        }

        @Override
        public void run(T blockEntity, F fields) {
            while (this.condition.test(blockEntity, fields)) {
                this.operation.run(blockEntity, fields);
            }
        }
    }

    class Delayed<T extends IndustriaBlockEntity<T>, F extends BlockEntityFields<T>> implements TickOperation<T, F> {
        private final int delay;
        private final TickOperation<T, F> operation;

        public Delayed(int delay, TickOperation<T, F> operation) {
            this.delay = delay;
            this.operation = operation;
        }

        @Override
        public void run(T blockEntity, F fields) {
            if (fields.getFieldValueInt("ticks", blockEntity) % this.delay == 0) {
                this.operation.run(blockEntity, fields);
            }
        }
    }
}
