package dev.turtywurty.industria.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;

public interface VertexOffsets {
    int STRIDE = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
    int POSITION = getOffset("Position");
    int COLOR = getOffset("Color");
    int UV0 = getOffset("UV0");
    int UV1 = getOffset("UV1");
    int UV2 = getOffset("UV2");
    int NORMAL = getOffset("Normal");

    private static int getOffset(String name) {
        VertexFormatElement element = DefaultVertexFormat.BLOCK.getElement(name);
        return element == null ? -1 : element.offset() / 4;
    }
}
