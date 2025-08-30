package dev.turtywurty.industria.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

/**
 * Represents a type of port in a multiblock structure, defining its transfer type and whether it is an input or output port.
 *
 * @param transferType the transfer type associated with this port
 * @param isInput      indicates if this port is an input port
 * @param isOutput     indicates if this port is an output port
 */
public record PortType(TransferType<?, ?, ?> transferType, boolean isInput, boolean isOutput) {
    public static final MapCodec<PortType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TransferType.CODEC.fieldOf("transfer_type").forGetter(PortType::transferType),
            Codec.BOOL.optionalFieldOf("is_input", false).forGetter(PortType::isInput),
            Codec.BOOL.optionalFieldOf("is_output", false).forGetter(PortType::isOutput)
    ).apply(instance, PortType::new));

    public PortType {
        Objects.requireNonNull(transferType, "Transfer type cannot be null");
        if (!isInput && !isOutput)
            throw new IllegalArgumentException("At least one of isInput or isOutput must be true");
    }

    /**
     * Creates a new PortType builder with the specified transfer type.
     *
     * @param transferType the transfer type for the port
     * @return a new Builder instance
     */
    public static Builder builder(TransferType<?, ?, ?> transferType) {
        return new Builder().transferType(transferType);
    }

    /**
     * Creates a PortType that is an input port for the specified transfer type.
     *
     * @param transferType the transfer type for the port
     * @return a PortType instance configured as an input port
     */
    public static PortType input(TransferType<?, ?, ?> transferType) {
        return builder(transferType).input(true).build();
    }

    /**
     * Creates a PortType that is an output port for the specified transfer type.
     *
     * @param transferType the transfer type for the port
     * @return a PortType instance configured as an output port
     */
    public static PortType output(TransferType<?, ?, ?> transferType) {
        return builder(transferType).output(true).build();
    }

    /**
     * Creates a PortType that is both an input and output port for the specified transfer type.
     *
     * @param transferType the transfer type for the port
     * @return a PortType instance configured as both an input and output port
     */
    public static PortType io(TransferType<?, ?, ?> transferType) {
        return builder(transferType).io(true, true).build();
    }

    /**
     * Builder class for constructing PortType instances.
     */
    public static class Builder {
        private TransferType<?, ?, ?> transferType;
        private boolean isInput = false;
        private boolean isOutput = false;

        /**
         * Sets the transfer type for this port type.
         *
         * @param transferType the transfer type to set
         * @return this Builder instance for chaining
         */
        public Builder transferType(TransferType<?, ?, ?> transferType) {
            this.transferType = transferType;
            return this;
        }

        /**
         * Sets whether this port type is an input port.
         *
         * @param isInput true if this port is an input, false otherwise
         * @return this Builder instance for chaining
         */
        public Builder input(boolean isInput) {
            this.isInput = isInput;
            return this;
        }

        /**
         * Sets whether this port type is an output port.
         *
         * @param isOutput true if this port is an output, false otherwise
         * @return this Builder instance for chaining
         */
        public Builder output(boolean isOutput) {
            this.isOutput = isOutput;
            return this;
        }

        /**
         * Sets both input and output flags for this port type.
         *
         * @param isInput  true if this port is an input, false otherwise
         * @param isOutput true if this port is an output, false otherwise
         * @return this Builder instance for chaining
         */
        public Builder io(boolean isInput, boolean isOutput) {
            this.isInput = isInput;
            this.isOutput = isOutput;
            return this;
        }

        /**
         * Builds and returns a new PortType instance with the specified parameters.
         *
         * @return a new PortType instance
         */
        public PortType build() {
            return new PortType(transferType, isInput, isOutput);
        }
    }
}
