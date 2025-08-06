package dev.turtywurty.industria.util;

import java.util.function.BooleanSupplier;

public enum IndeterminateBoolean {
    TRUE,
    FALSE,
    INDETERMINATE;

    public boolean isTrue() {
        return this == TRUE;
    }

    public boolean isFalse() {
        return this == FALSE;
    }

    public boolean isIndeterminate() {
        return this == INDETERMINATE;
    }

    public boolean evaluate(boolean condition) {
        return evaluate(() -> condition);
    }

    public boolean evaluate(BooleanSupplier condition) {
        if (isIndeterminate())
            return condition.getAsBoolean();

        return isTrue();
    }
}
