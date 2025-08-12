package dev.turtywurty.industria.util;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

public interface ValueIOSerializable {
    void readData(ReadView view);
    void writeData(WriteView view);
}
