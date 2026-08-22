package dev.turtywurty.industria.util;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface ViewSerializable {
    void readData(ValueInput view);

    void writeData(ValueOutput view);
}
