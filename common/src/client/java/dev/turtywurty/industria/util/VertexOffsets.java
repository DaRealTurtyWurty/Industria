package dev.turtywurty.industria.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public interface VertexOffsets {
    int STRIDE = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
    int POSITION = getOffset(VertexFormatElement.POSITION);
    int COLOR = getOffset(VertexFormatElement.COLOR);
    int UV0 = getOffset(VertexFormatElement.UV0);
    int UV1 = getOffset(VertexFormatElement.UV1);
    int UV2 = getOffset(VertexFormatElement.UV2);
    int NORMAL = getOffset(VertexFormatElement.NORMAL);

    private static int getOffset(VertexFormatElement element) {
        if(DefaultVertexFormat.BLOCK.contains(element)) {
            return DefaultVertexFormat.BLOCK.getOffset(element) / 4;
        }

        return -1;
    }
}
