package dev.turtywurty.industria.util;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

public interface ViewSerializable {
    void readData(ReadView view);

    void writeData(WriteView view);
}
