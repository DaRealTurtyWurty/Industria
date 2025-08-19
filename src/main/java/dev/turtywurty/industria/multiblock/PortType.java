package dev.turtywurty.industria.multiblock;

import java.util.Objects;

public record PortType(TransferType<?, ?, ?> transferType, boolean isInput, boolean isOutput) {
    public PortType {
        Objects.requireNonNull(transferType, "Transfer type cannot be null");
        if (!isInput && !isOutput)
            throw new IllegalArgumentException("At least one of isInput or isOutput must be true");
    }

    public static Builder builder(TransferType<?, ?, ?> transferType) {
        return new Builder().transferType(transferType);
    }

    public static PortType input(TransferType<?, ?, ?> transferType) {
        return builder(transferType).isInput(true).build();
    }

    public static PortType output(TransferType<?, ?, ?> transferType) {
        return builder(transferType).isOutput(true).build();
    }

    public static PortType io(TransferType<?, ?, ?> transferType) {
        return builder(transferType).io(true, true).build();
    }

    public static class Builder {
        private TransferType<?, ?, ?> transferType;
        private boolean isInput = false;
        private boolean isOutput = false;

        public Builder transferType(TransferType<?, ?, ?> transferType) {
            this.transferType = transferType;
            return this;
        }

        public Builder isInput(boolean isInput) {
            this.isInput = isInput;
            return this;
        }

        public Builder isOutput(boolean isOutput) {
            this.isOutput = isOutput;
            return this;
        }

        public Builder io(boolean isInput, boolean isOutput) {
            this.isInput = isInput;
            this.isOutput = isOutput;
            return this;
        }

        public PortType build() {
            return new PortType(transferType, isInput, isOutput);
        }
    }
}
