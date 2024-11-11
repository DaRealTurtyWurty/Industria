package dev.turtywurty.industria.util;

import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;

public interface VertexOffsets {
    int STRIDE = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeByte() / 4;
    int POSITION = getOffset(VertexFormatElement.POSITION);
    int COLOR = getOffset(VertexFormatElement.COLOR);
    int UV0 = getOffset(VertexFormatElement.UV_0);
    int UV1 = getOffset(VertexFormatElement.UV_1);
    int UV2 = getOffset(VertexFormatElement.UV_2);
    int NORMAL = getOffset(VertexFormatElement.NORMAL);

    private static int getOffset(VertexFormatElement element) {
        if(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.has(element)) {
            return VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getOffset(element) / 4;
        }

        return -1;
    }
}
